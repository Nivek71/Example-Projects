package nivek71.api.minigame.player.kit;

import nivek71.api.minigame.MiniGameInfo;
import nivek71.api.minigame.MiniGamePlugin;
import nivek71.api.minigame.lobby.Lobby;
import nivek71.api.minigame.player.MiniGamePlayer;
import nivek71.api.utility.Helpers;
import nivek71.api.utility.input.InventoryGUI;
import nivek71.api.utility.rule.rules.StandardRule;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class KitManager implements Cloneable {
    private PlayerKitType<?> defaultKit;
    private List<PlayerKitType<?>> playerKitTypes = new ArrayList<>();

    public KitManager(PlayerKitType<?> defaultKit, PlayerKitType<?>... extraKits) {
        Validate.notNull(defaultKit, "defaultKit cannot be null");
        this.defaultKit = defaultKit;
        playerKitTypes.add(defaultKit);
        playerKitTypes.addAll(Arrays.asList(extraKits));
    }

    public static PlayerKitType<?> getDefaultKit(MiniGameInfo<?, ?> info, UUID uuid) {
        // todo - use database to load default kit
        return info.getKitManager().getDefaultKit();
    }

    public static void setDefaultKit(MiniGameInfo<?, ?> info, UUID uuid, PlayerKitType<?> kitType) {
        Validate.isTrue(info.getKitManager().playerKitTypes.contains(kitType), "cannot set default kit to kit not contained by info's KitManager");
        // todo - use database to set default kit
    }

    public PlayerKitType<?> getDefaultKit() {
        return defaultKit;
    }

    public PlayerKitType getKitFromName(String name) {
        for (PlayerKitType<?> kitType : playerKitTypes) {
            if (kitType.getName().equals(name))
                return kitType;
        }
        return null;
    }

    public Collection<PlayerKitType<?>> getAvailableKits() {
        return playerKitTypes;
    }

    public void removeKits(PlayerKitType<?>... kits) {
        Validate.notNull(kits, "kits cannot be null");
        List<PlayerKitType<?>> copy = new ArrayList<>(playerKitTypes);
        copy.removeAll(Arrays.asList(kits)); // can't compare lengths in case kits contains a kit not present in set
        Validate.isTrue(!copy.isEmpty(), "Cannot remove all kits from KitManager; must have at least one (default) kit.");
        playerKitTypes = copy;

        if (!playerKitTypes.contains(defaultKit))
            defaultKit = playerKitTypes.iterator().next();
    }

    public void addKits(PlayerKitType<?>... kits) {
        Validate.notNull(kits, "kits cannot be null");
        playerKitTypes.addAll(Arrays.asList(kits));
    }

    public void showKitSelectionInventoryTo(Player showPlayer) {
        MiniGamePlayer miniGamePlayer = Lobby.getMiniGamePlayer(showPlayer);
        InventoryGUI.createGUI("Available Kits", 27, playerKitTypes, kitType -> {
            ItemStack item = kitType.getDisplayItem();
            if (miniGamePlayer.getQueuedKitType().equals(kitType)) {
                Helpers.getItemStack(item, (itemStack, itemMeta) -> itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS));
                item.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
            }
            return item;
        }, kitType -> (loc, event) -> {
            if (miniGamePlayer != null) {
                Player player = miniGamePlayer.getPlayer();
                if (StandardRule.PLAYER_QUEUE_KIT.getRuleState(miniGamePlayer)) {
                    miniGamePlayer.setKitType(kitType);
                    player.playSound(player.getLocation(), Sound.BLOCK_METAL_HIT, 1, 1);
                    player.sendMessage(ChatColor.GREEN + "You switched your kit to " + kitType.getName());
                } else {
                    player.sendMessage(ChatColor.RED + "You cannot change your kit now.");
                }
                Helpers.runLater(MiniGamePlugin.getPlugin(), player::closeInventory);
            }
            return false;
        }).disposeAfter(showPlayer);
    }

    @Override
    public KitManager clone() {
        try {
            return (KitManager) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
    }
}

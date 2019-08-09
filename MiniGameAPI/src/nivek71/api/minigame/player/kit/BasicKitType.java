package nivek71.api.minigame.player.kit;

import nivek71.api.minigame.player.MiniGamePlayer;
import nivek71.api.utility.Helpers;
import nivek71.api.utility.types.ArmorType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class BasicKitType extends PlayerKitType<PlayerKit> {
    private ItemStack helmet = Helpers.AIR_ITEM_STACK;
    private ItemStack chestPlate = Helpers.AIR_ITEM_STACK;
    private ItemStack leggings = Helpers.AIR_ITEM_STACK;
    private ItemStack boots = Helpers.AIR_ITEM_STACK;
    private Map<Integer, ItemStack> items = new HashMap<>();
    private Map<PotionEffectType, Integer> potionEffects = new HashMap<>();

    private static final BiFunction<MiniGamePlayer, BasicKitType, PlayerKit> factory = (player, kitType) -> new PlayerKit(kitType) {
        @Override
        public void giveTo(MiniGamePlayer miniGamePlayer) {
            super.giveTo(miniGamePlayer);
            Player player = miniGamePlayer.getPlayer();
            PlayerInventory inventory = player.getInventory();
            inventory.setHelmet(kitType.helmet);
            inventory.setChestplate(kitType.chestPlate);
            inventory.setLeggings(kitType.leggings);
            inventory.setBoots(kitType.boots);
            for (Map.Entry<Integer, ItemStack> entry : kitType.items.entrySet()) {
                inventory.setItem(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<PotionEffectType, Integer> entry : kitType.potionEffects.entrySet()) {
                player.addPotionEffect(new PotionEffect(entry.getKey(), 100000, entry.getValue()));
            }
        }

        @Override
        public void removeFrom(MiniGamePlayer miniGamePlayer) {
            super.removeFrom(miniGamePlayer);
        }
    };

    public static BasicKitType createKit(String kitName, Material displayMaterial, String kitDescription, ItemStack helmet, ItemStack chestPlate, ItemStack leggings, ItemStack boots, ItemStack... inventory) {
        BasicKitType kitType = new BasicKitType(kitName, displayMaterial, kitDescription);
        kitType.setHelmet(helmet);
        kitType.setChestPlate(chestPlate);
        kitType.setLeggings(leggings);
        kitType.setBoots(boots);
        for (int i = 0; i < inventory.length; i++)
            kitType.setItem(i, inventory[i]);
        return kitType;
    }

    public static BasicKitType createKit(String kitName, Material displayMaterial, String kitDescription, ArmorType armorType, boolean unbreakable, ItemStack... inventory) {
        return createKit(kitName, displayMaterial, kitDescription,
                Helpers.getItemStack(armorType.getHelmet(), unbreakable),
                Helpers.getItemStack(armorType.getChestPlate(), unbreakable),
                Helpers.getItemStack(armorType.getLeggings(), unbreakable),
                Helpers.getItemStack(armorType.getBoots(), unbreakable),
                inventory);
    }

    // todo - ability api
    public BasicKitType(String kitName, Material displayMaterial, String kitDescription, BiFunction<MiniGamePlayer, BasicKitType, PlayerKit> kitFactory) {
        super(kitName, displayMaterial, kitDescription, kitFactory);
    }

    public BasicKitType(String kitName, Material displayMaterial, String kitDescription) {
        this(kitName, displayMaterial, kitDescription, factory);
    }

    public ItemStack getHelmet() {
        return helmet;
    }

    public BasicKitType setHelmet(ItemStack helmet) {
        this.helmet = helmet;
        return this;
    }

    public ItemStack getChestPlate() {
        return chestPlate;
    }

    public BasicKitType setChestPlate(ItemStack chestPlate) {
        this.chestPlate = chestPlate;
        return this;
    }

    public ItemStack getLeggings() {
        return leggings;
    }

    public BasicKitType setLeggings(ItemStack leggings) {
        this.leggings = leggings;
        return this;
    }

    public ItemStack getBoots() {
        return boots;
    }

    public BasicKitType setBoots(ItemStack boots) {
        this.boots = boots;
        return this;
    }

    public Map<Integer, ItemStack> getItems() {
        return items;
    }

    public BasicKitType setItems(Map<Integer, ItemStack> items) {
        this.items = items;
        return this;
    }

    public BasicKitType setItem(int i, ItemStack item) {
        items.put(i, item);
        return this;
    }

    public Map<PotionEffectType, Integer> getPotionEffects() {
        return potionEffects;
    }

    public BasicKitType addPotionEffect(PotionEffectType type, int level) {
        potionEffects.put(type, level);
        return this;
    }

    public void setPotionEffects(Map<PotionEffectType, Integer> potionEffects) {
        this.potionEffects = potionEffects;
    }
}

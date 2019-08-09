package nivek71.api.minigame.player.kit;

import nivek71.api.minigame.player.MiniGamePlayer;
import nivek71.api.utility.Helpers;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiFunction;

public class PlayerKitType<T extends PlayerKit> {
    private final String kitName;
    private Material displayMaterial;
    private String kitDescription;
    private BiFunction<MiniGamePlayer, ? extends PlayerKitType<T>, T> kitFactory;

    public PlayerKitType(String kitName, Material displayMaterial, String kitDescription, BiFunction<MiniGamePlayer, ? extends PlayerKitType<T>, T> kitFactory) {
        this.kitName = kitName;
        this.displayMaterial = displayMaterial;
        this.kitDescription = kitDescription;
        this.kitFactory = kitFactory;
    }

    public PlayerKitType(String kitName, Material displayMaterial, BiFunction<MiniGamePlayer, ? extends PlayerKitType<T>, T> kitFactory) {
        this(kitName, displayMaterial, "", kitFactory);
    }

    public String getName() {
        return kitName;
    }

    public BiFunction<MiniGamePlayer, ? extends PlayerKitType<T>, T> getKitFactory() {
        return kitFactory;
    }

    public void setKitFactory(BiFunction<MiniGamePlayer, PlayerKitType<T>, T> kitFactory) {
        this.kitFactory = kitFactory;
    }

    public Material getDisplayMaterial() {
        return displayMaterial;
    }

    public String getKitDescription() {
        return kitDescription;
    }

    public ItemStack getDisplayItem() {
        return Helpers.getItemStack(displayMaterial, ChatColor.GREEN + kitName, false, true, Helpers.splitOnWords(ChatColor.GRAY.toString(), kitDescription));
    }
}

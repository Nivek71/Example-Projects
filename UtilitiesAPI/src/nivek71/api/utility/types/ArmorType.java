package nivek71.api.utility.types;

import org.bukkit.Material;

import static org.bukkit.Material.*;

public enum ArmorType {
    LEATHER(ToolType.WOOD, LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS),
    GOLD(ToolType.GOLD, GOLDEN_HELMET, GOLDEN_CHESTPLATE, GOLDEN_LEGGINGS, GOLDEN_BOOTS),
    CHAINMAIL(ToolType.STONE, CHAINMAIL_HELMET, CHAINMAIL_CHESTPLATE, CHAINMAIL_LEGGINGS, CHAINMAIL_BOOTS),
    IRON(ToolType.IRON, IRON_HELMET, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS),
    DIAMOND(ToolType.DIAMOND, DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS);

    private final ToolType weaponEquivalent;
    private final Material helmet, chestPlate, leggings, boots;

    ArmorType(ToolType weaponEquivalent, Material helmet, Material chestPlate, Material leggings, Material boots) {
        this.weaponEquivalent = weaponEquivalent;
        this.helmet = helmet;
        this.chestPlate = chestPlate;
        this.leggings = leggings;
        this.boots = boots;
    }

    public ToolType getWeaponEquivalent() {
        return weaponEquivalent;
    }

    public Material getHelmet() {
        return helmet;
    }

    public Material getChestPlate() {
        return chestPlate;
    }

    public Material getLeggings() {
        return leggings;
    }

    public Material getBoots() {
        return boots;
    }
}

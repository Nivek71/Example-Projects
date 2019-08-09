package nivek71.api.utility.types;

public enum ToolType {
    WOOD(ArmorType.LEATHER),
    GOLD(ArmorType.GOLD),
    STONE(ArmorType.CHAINMAIL),
    IRON(ArmorType.IRON),
    DIAMOND(ArmorType.DIAMOND);

    private final ArmorType armorEquivalent;

    ToolType(ArmorType armorEquivalent) {
        this.armorEquivalent = armorEquivalent;
    }

    public ArmorType getArmorEquivalent() {
        return armorEquivalent;
    }
}

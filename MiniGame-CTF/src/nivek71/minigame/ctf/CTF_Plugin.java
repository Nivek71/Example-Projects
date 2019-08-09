package nivek71.minigame.ctf;

import nivek71.api.minigame.MiniGameInfo;
import nivek71.api.minigame.map.MiniGameMap;
import nivek71.api.minigame.player.kit.BasicKitType;
import nivek71.api.minigame.player.kit.KitManager;
import nivek71.api.utility.Helpers;
import nivek71.api.utility.types.ArmorType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

public class CTF_Plugin extends JavaPlugin {
    public static KitManager CTF_KIT_MANAGER;
    public static MiniGameInfo<CaptureTheFlag, CTF_MapConfiguration> CTF_INFO;
    public static MiniGameMap CTF_MAP;
    public static CTF_MapConfiguration CTF_MAP_CONFIGURATION;

    public static CTF_Plugin getPlugin() {
        return JavaPlugin.getPlugin(CTF_Plugin.class);
    }

    @Override
    public void onEnable() {
        CTF_KIT_MANAGER = new KitManager(
                // test text wrapping for description
                BasicKitType.createKit("Warrior", Material.IRON_CHESTPLATE, "wioefoiwef iowef jiowefj ioewfg jeriog jioerg jeirogje riogjeiorgj ioerg jioejrgi oergjieriog jioergjriog jeiorjgioerg joerigjoe irgjeroig jeriojg eirgj oeir goierjg ioerg jioerjg ioerjgioerg jeriojgreoigjioergjioerjgoeirjgoiergergjioergjioerogjeiorgjioerjgioerogeriogjiergegioreger",
                        ArmorType.IRON, true, Helpers.getItemStack(Material.IRON_SWORD, true)),
                BasicKitType.createKit("Archer", Material.BOW, "", ArmorType.CHAINMAIL, true, Helpers.getItemStack(Material.STONE_SWORD, true),
                        Helpers.getItemStack(Material.BOW, true), new ItemStack(Material.ARROW, 32)),
                BasicKitType.createKit("Assassin", Material.DIAMOND_SWORD, "", ArmorType.LEATHER, true, Helpers.getItemStack(Material.DIAMOND_SWORD, true),
                        Helpers.getItemStack(Material.BOW, true), new ItemStack(Material.ARROW, 8)).addPotionEffect(PotionEffectType.SPEED, 1)
        );
        CTF_INFO = new MiniGameInfo<>("Capture The Flag", CaptureTheFlag.class, 2, CTF_KIT_MANAGER, CaptureTheFlag::new);
        CTF_MAP = new MiniGameMap("CTF-Example", new Location(Bukkit.getWorlds().get(0),
                1000, 0, 1000), new Location(Bukkit.getWorlds().get(0), 1100, 256, 1100), new Location(Bukkit.getWorlds().get(0), 1050, 125, 1050), 120);
        CTF_MAP_CONFIGURATION = new CTF_MapConfiguration(CTF_MAP, true, 3,
                new CTF_Team("Blue", ChatColor.AQUA, new Location(Bukkit.getWorlds().get(0), 1050, 100, 1005), new Location(Bukkit.getWorlds().get(0), 1050, 100, 1010)),
                new CTF_Team("Red", ChatColor.RED, new Location(Bukkit.getWorlds().get(0), 1050, 100, 1095), new Location(Bukkit.getWorlds().get(0), 1050, 100, 1090)));
    }
}

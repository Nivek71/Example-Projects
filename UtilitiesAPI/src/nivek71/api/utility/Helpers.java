package nivek71.api.utility;

import com.connorlinfoot.actionbarapi.ActionBarAPI;
import nivek71.api.utility.functions.ConsumerEx;
import nivek71.api.utility.functions.FunctionEx;
import nivek71.api.utility.functions.RunnableEx;
import nivek71.api.utility.functions.SupplierEx;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Helpers {
    public static final String MINECRAFT_VERSION;
    public static final Class<? extends Player> CRAFT_PLAYER_CLASS;

    static {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        MINECRAFT_VERSION = name.substring(name.lastIndexOf('.') + 1) + ".";
        CRAFT_PLAYER_CLASS = helper(Logger.fetchOrLog(() -> getCraftClass("entity.CraftPlayer")));
    }

    // used to bypass type checks
    // use at your own risk !!
    public static <T> T helper(Object object) {
        //noinspection unchecked
        return (T) object;
    }

    public static void setMaxHealth(LivingEntity entity, double maxHealth) {
        entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
    }

    public static void restorePlayer(Player player) {
        player.setFoodLevel(20);
        player.setHealth(20);
        setMaxHealth(player, 20);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        if (player.getGameMode() != GameMode.CREATIVE)
            player.setAllowFlight(false);
    }

    // dynamic events
    public static <T extends Event> void registerDynamicEvent(Plugin plugin, Listener listener, Class<T> eventCl, ConsumerEx<T> handler, EventPriority priority, boolean ignoreCancelled) {
        Bukkit.getPluginManager().registerEvent(eventCl, listener, priority, (l, event) -> {
            // if this check is not here, a ClassCastException is thrown for some events
            // I believe (though have not checked) that it is because EntityDamageEvent shares its handlers
            // with its subclasses
            if (eventCl.isAssignableFrom(event.getClass()))
                //noinspection unchecked
                Logger.tryOrLog(() -> handler.accept((T) event));
        }, plugin, ignoreCancelled);
    }

    public static <T extends Event> Listener registerDynamicEvent(Plugin plugin, Class<T> eventCl, ConsumerEx<T> handler, EventPriority priority, boolean ignoreCancelled) {
        Listener listener = new Listener() {
        };
        registerDynamicEvent(plugin, listener, eventCl, handler, priority, ignoreCancelled);
        return listener;
    }

    public static <T extends Event> Listener registerDynamicEvent(Plugin plugin, Class<T> eventCl, ConsumerEx<T> handler) {
        return registerDynamicEvent(plugin, eventCl, handler, EventPriority.NORMAL, false);
    }

    // player cleanup
    private static List<FunctionEx<Player, Boolean>> playerCleanupFunctions = new ArrayList<>();

    static {
        UtilityPlugin.registerOnEnableFunction(() -> registerDynamicEvent(UtilityPlugin.getPlugin(), PlayerQuitEvent.class, event -> {
            for (int i = playerCleanupFunctions.size() - 1; i >= 0; i--) {
                FunctionEx<Player, Boolean> cleanup = playerCleanupFunctions.get(i);
                if (Logger.fetchOrLog(() -> cleanup.applyEx(event.getPlayer()), () -> false)) {
                    playerCleanupFunctions.remove(i);
                }
            }
        }));
    }

    public static void registerRemovablePlayerCleanup(FunctionEx<Player, Boolean> cleanupFunction) {
        playerCleanupFunctions.add(cleanupFunction);
    }

    public static void registerPlayerCleanup(ConsumerEx<Player> onQuit) {
        registerRemovablePlayerCleanup(player -> {
            onQuit.accept(player);
            return false;
        });
    }

    public static void registerRemovablePlayerCleanup(Collection<Player> collection, boolean removeOnEmpty) {
        registerRemovablePlayerCleanup(player -> {
            collection.remove(player);
            return removeOnEmpty && collection.isEmpty();
        });
    }

    public static void registerPlayerQuitCleanup(Collection<Player> collection) {
        registerRemovablePlayerCleanup(collection, false);
    }

    // invisible players
    private static Set<Player> invisiblePlayers = new HashSet<>();

    static {
        UtilityPlugin.registerOnEnableFunction(() -> registerPlayerQuitCleanup(invisiblePlayers));

        // todo - prevent projectiles from hitting player (re-launch when hit?)
        // if new projectile is launched from same location, may cause problems with custom arrows
    }

    public static void setInvisible(Plugin plugin, Player player, boolean invisible) {
        if (invisible) {
            if (invisiblePlayers.add(player)) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.hidePlayer(plugin, player);
                }
            }
        } else {
            if (invisiblePlayers.remove(player)) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.showPlayer(plugin, player);
                }
            }
        }
    }

    public static boolean isInvisible(Player player) {
        return invisiblePlayers.contains(player);
    }

    // immobile players
    private static Set<Player> immobilePlayers = new HashSet<>();

    static {
        UtilityPlugin.registerOnEnableFunction(() -> {
            registerPlayerQuitCleanup(immobilePlayers);

            registerDynamicEvent(UtilityPlugin.getPlugin(), PlayerMoveEvent.class, event -> {
                if (immobilePlayers.contains(event.getPlayer()) && (event.getFrom().getX() != event.getTo().getX() || event.getTo().getZ() != event.getTo().getZ())) {
                    // set player back to previous x and z, but allow player to move direction and y
                    event.getPlayer().teleport(new Location(event.getFrom().getWorld(), event.getFrom().getX(), event.getTo().getY(), event.getFrom().getZ(), event.getTo().getYaw(), event.getTo().getPitch()));
                }
            }, EventPriority.HIGH, true);
        });
    }

    public static void setImmobile(Player player, boolean immobile) {
        if (immobile)
            immobilePlayers.add(player);
        else immobilePlayers.remove(player);
    }

    // runnables
    public static BukkitRunnable getRunnable(SupplierEx<Boolean> ex, BiConsumer<BukkitRunnable, Exception> failureCallback) {
        return new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (!ex.getEx())
                        cancel();
                } catch (Exception ex) {
                    failureCallback.accept(this, ex);
                }
            }
        };
    }

    public static BukkitRunnable getRunnable(SupplierEx<Boolean> run) {
        return getRunnable(run, (runnable, ex) -> {
            Logger.logException(ex);
            runnable.cancel();
        });
    }

    public static BukkitRunnable getRunnable(RunnableEx ex, BiConsumer<BukkitRunnable, Exception> failureCallback) {
        return getRunnable(() -> {
            ex.runEx();
            return true;
        }, failureCallback);
    }

    public static BukkitRunnable getRunnable(RunnableEx ex) {
        return getRunnable(() -> {
            ex.runEx();
            return true;
        });
    }

    public static BukkitTask runLater(Plugin plugin, RunnableEx ex, BiConsumer<BukkitRunnable, Exception> failureCallback, int delay) {
        return getRunnable(ex, failureCallback).runTaskLater(plugin, delay);
    }

    public static BukkitTask runLater(Plugin plugin, RunnableEx ex, int delay) {
        return getRunnable(ex).runTaskLater(plugin, delay);
    }

    public static BukkitTask runLater(Plugin plugin, RunnableEx ex) {
        return runLater(plugin, ex, 1);
    }

    public static BukkitTask runAsync(Plugin plugin, RunnableEx ex, BiConsumer<BukkitRunnable, Exception> failureCallback) {
        return getRunnable(ex, failureCallback).runTaskAsynchronously(plugin);
    }

    public static BukkitTask runAsync(Plugin plugin, RunnableEx ex) {
        return getRunnable(ex).runTaskAsynchronously(plugin);
    }

    public static BukkitTask runLaterAsync(Plugin plugin, RunnableEx ex, BiConsumer<BukkitRunnable, Exception> failureCallback, int delay) {
        return getRunnable(ex, failureCallback).runTaskLaterAsynchronously(plugin, delay);
    }

    public static BukkitTask runLaterAsync(Plugin plugin, RunnableEx ex, int delay) {
        return getRunnable(ex).runTaskLaterAsynchronously(plugin, delay);
    }

    public static BukkitTask runTaskAsync(Plugin plugin, SupplierEx<Boolean> ex, BiConsumer<BukkitRunnable, Exception> failureCallback, int delay, int period) {
        return getRunnable(ex, failureCallback).runTaskTimerAsynchronously(plugin, delay, period);
    }

    public static BukkitTask runTaskAsync(Plugin plugin, SupplierEx<Boolean> ex, int delay, int period) {
        return getRunnable(ex).runTaskTimerAsynchronously(plugin, delay, period);
    }

    public static BukkitTask runTaskTimer(Plugin plugin, SupplierEx<Boolean> ex, BiConsumer<BukkitRunnable, Exception> failureCallback, int delay, int period) {
        return getRunnable(ex, failureCallback).runTaskTimer(plugin, delay, period);
    }

    public static BukkitTask runTaskTimer(Plugin plugin, SupplierEx<Boolean> ex, int delay, int period) {
        return getRunnable(ex).runTaskTimer(plugin, delay, period);
    }

    public static LivingEntity getAttacker(Entity entity) {
        if (entity instanceof LivingEntity)
            return (LivingEntity) entity;
        if (entity instanceof Projectile) {
            Projectile projectile = (Projectile) entity;
            if (projectile.getShooter() instanceof LivingEntity)
                return (LivingEntity) projectile.getShooter();
        }
        return null;
    }

    public static <T> T notNullOrCompute(T value, Supplier<T> supplier) {
        if (value == null)
            return supplier.get();
        return value;
    }

    public static Material chatColorToWoolColor(ChatColor color) {
        switch (color) {
            case BLACK:
                return Material.BLACK_WOOL;
            case BLUE:
            case DARK_BLUE:
                return Material.BLUE_WOOL;
            case DARK_GREEN:
                return Material.GREEN_WOOL;
            case DARK_AQUA:
                return Material.CYAN_WOOL;
            case RED:
            case DARK_RED:
                return Material.RED_WOOL;
            case DARK_PURPLE:
                return Material.PURPLE_WOOL;
            case GOLD:
                return Material.ORANGE_WOOL;
            case GRAY:
                return Material.LIGHT_GRAY_WOOL;
            case DARK_GRAY:
                return Material.GRAY_WOOL;
            case GREEN:
                return Material.LIME_WOOL;
            case AQUA:
                return Material.LIGHT_BLUE_WOOL;
            case LIGHT_PURPLE:
                return Material.MAGENTA_WOOL;
            case YELLOW:
                return Material.YELLOW_WOOL;
            case WHITE:
            case MAGIC:
            case BOLD:
            case STRIKETHROUGH:
            case UNDERLINE:
            case ITALIC:
            case RESET:
        }
        return Material.WHITE_WOOL;
    }

    public static final ItemStack AIR_ITEM_STACK = new ItemStack(Material.AIR);

    public static ItemStack getItemStack(ItemStack item, BiConsumer<ItemStack, ItemMeta> handler) {
        ItemMeta meta = item.getItemMeta();
        handler.accept(item, meta);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getItemStack(Material type, BiConsumer<ItemStack, ItemMeta> handler) {
        return getItemStack(new ItemStack(type), handler);
    }

    public static ItemStack getItemStack(Material material, String name, boolean unbreakable, boolean hideAttributes, String... lore) {
        return getItemStack(material, (item, meta) -> {
            if (name != null)
                meta.setDisplayName(ChatColor.RESET + name);
            meta.setUnbreakable(unbreakable);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            if (hideAttributes)
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.setLore(Arrays.asList(lore));
        });
    }

    public static ItemStack getItemStack(Material material, boolean unbreakable) {
        return getItemStack(material, null, unbreakable, false);
    }

    public static String[] splitOnWords(String prefix, String string, int maxLineLength) {
        List<String> lines = new ArrayList<>();
        int lineBeginning = 0;
        int lineEnd = 0;
        while (string.length() > lineBeginning) {
            lineEnd = lineEnd + maxLineLength;

            if (lineEnd >= string.length())
                lineEnd = string.length();
            else while (Character.isWhitespace(string.charAt(lineEnd))) {
                if (--lineEnd == lineBeginning) {
                    // split whole word
                    lineEnd = lineEnd + maxLineLength;
                    break;
                }
            }

            lines.add(prefix + string.substring(lineBeginning, lineEnd));
            lineBeginning = lineEnd;
        }

        return lines.toArray(new String[0]);
    }

    public static String[] splitOnWords(String prefix, String string) {
        return splitOnWords(prefix, string, 30);
    }

    public static Color colorToChatColor(ChatColor color) {
        switch (color) {
            case BLACK:
                return Color.BLACK;
            case DARK_BLUE:
                return Color.BLUE;
            case DARK_GREEN:
                return Color.GREEN;
            case DARK_AQUA:
                return Color.fromRGB(0, 153, 153);
            case DARK_RED:
                return Color.fromRGB(179);
            case DARK_PURPLE:
                return Color.PURPLE;
            case GOLD:
                return Color.fromRGB(209, 174, 0);
            case GRAY:
                return Color.fromRGB(169, 169, 169);
            case DARK_GRAY:
                return Color.fromRGB(105, 105, 105);
            case BLUE:
                return Color.fromRGB(51, 153, 255);
            case GREEN:
                return Color.fromRGB(106, 255, 77);
            case AQUA:
                return Color.AQUA;
            case RED:
                return Color.fromRGB(255, 77, 77);
            case LIGHT_PURPLE:
                return Color.fromRGB(255, 77, 196);
            case YELLOW:
                return Color.fromRGB(255, 255, 77);
            case WHITE:
                return Color.fromRGB(255, 255, 255);
            case MAGIC:
            case BOLD:
            case STRIKETHROUGH:
            case UNDERLINE:
            case ITALIC:
            case RESET:
            default:
                return Color.WHITE;
        }
    }

    public static FireworkEffect getFireworkEffect(ChatColor color, ChatColor fadeColor) {
        return FireworkEffect.builder().withColor(colorToChatColor(color)).with(FireworkEffect.Type.BALL_LARGE).withFade(colorToChatColor(fadeColor)).build();
    }

    public static Firework launchFirework(Location location, Consumer<FireworkMeta> consumer) {
        Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta meta = firework.getFireworkMeta();
        consumer.accept(meta);
        firework.setFireworkMeta(meta);
        return firework;
    }

    public static Firework launchFirework(Location location, FireworkEffect... effects) {
        return launchFirework(location, meta -> meta.addEffects(effects));
    }

    public static Firework launchFirework(Location location, ChatColor color, ChatColor fadeColor) {
        return launchFirework(location, getFireworkEffect(color, fadeColor));
    }

    public static Class<?> getCraftClass(String name) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + MINECRAFT_VERSION + name);
    }

    public static boolean isActionBarEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("ActionBarAPI");
    }

    public static void removeActionBar(Player player) {
        if (isActionBarEnabled())
            ActionBarAPI.sendActionBar(player, "", 0);
    }

    public static void actionBarOrChat(Player player, String message, int duration) {
        if (isActionBarEnabled())
            ActionBarAPI.sendActionBar(player, message, duration);
        else player.sendMessage(message);
    }

    public static void actionBarOrChat(Player player, String message) {
        actionBarOrChat(player, message, -1);
    }

    public static int random(int min, int max) {
        return (int) (Math.random() * (max - min) + min);
    }
}

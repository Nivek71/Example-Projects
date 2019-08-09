package nivek71.api.minigame.events;

import nivek71.api.minigame.MiniGamePlugin;
import nivek71.api.minigame.lobby.Lobby;
import nivek71.api.minigame.player.DamageProfile;
import nivek71.api.minigame.player.MiniGamePlayer;
import nivek71.api.utility.Helpers;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;

public final class EventHelper {
    public static void load() {
        MiniGamePlugin.registerOnEnableFunction(() -> {
            Plugin plugin = MiniGamePlugin.getPlugin();

            // calls MiniGamePlayerAttackEvent and MiniGamePlayerAttackPlayerEvent
            Helpers.registerDynamicEvent(plugin, EntityDamageByEntityEvent.class, event -> {
                MiniGamePlayer player = Lobby.getMiniGamePlayer(Helpers.getAttacker(event.getDamager()));
                if (player != null && event.getEntity() instanceof LivingEntity) {
                    MiniGamePlayer hurt = Lobby.getMiniGamePlayer(event.getEntity());
                    MiniGamePlayerAttackEvent attackEvent = hurt == null ? new MiniGamePlayerAttackEvent(player, (LivingEntity) event.getEntity(),
                            event.getDamage()) : new MiniGamePlayerAttackPlayerEvent(player, hurt, event.getDamage());
                    Bukkit.getPluginManager().callEvent(attackEvent);
                    event.setCancelled(attackEvent.isCancelled());
                }
            }, EventPriority.HIGH, true);

            // calls MiniGamePlayerHurtEvent, and handles player deaths
            Helpers.registerDynamicEvent(plugin, EntityDamageEvent.class, event -> {
                MiniGamePlayer player = Lobby.getMiniGamePlayer(event.getEntity());
                if (player != null) {
                    if (event.getFinalDamage() >= player.getPlayer().getHealth()) {
                        DamageProfile.logEntry(event);
                        player.killPlayer();
                        // player should not take this damage, whether or not they actually died
                        event.setCancelled(true);
                    } else {
                        MiniGamePlayerHurtEvent hurtEvent = new MiniGamePlayerHurtEvent(player, event.getFinalDamage());
                        Bukkit.getPluginManager().callEvent(hurtEvent);
                        event.setCancelled(hurtEvent.isCancelled());
                    }
                }
            }, EventPriority.HIGH, true);
        });
    }
}

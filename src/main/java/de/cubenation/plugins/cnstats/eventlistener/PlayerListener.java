package de.cubenation.plugins.cnstats.eventlistener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.cubenation.plugins.cnstats.services.TimeService;

/**
 * Handle events for all Player related events
 * 
 * @since 1.0
 */
public class PlayerListener implements Listener {
    private final TimeService timeService;

    public PlayerListener(TimeService timeService) {
        this.timeService = timeService;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        timeService.setLogOut(event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        timeService.addLogIn(event.getPlayer().getName());
    }
}

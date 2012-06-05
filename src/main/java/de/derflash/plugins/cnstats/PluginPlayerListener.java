package de.derflash.plugins.cnstats;

import java.util.Date;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handle events for all Player related events
 * @author TJ09
 */
public class PluginPlayerListener implements Listener {    
	CNStats plugin;

	PluginPlayerListener(CNStats p) {
		this.plugin = p;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Logins oldLogin = plugin.getDatabase().find(Logins.class).setMaxRows(1).orderBy("id desc").where().ieq("playerName", event.getPlayer().getName()).findUnique();
        if (oldLogin != null) {
        	oldLogin.setLogoutTime(new Date());
            plugin.getDatabase().save(oldLogin);
        }
	}
	
	@EventHandler
	public void onPlayerJoin (PlayerJoinEvent event) {
		Player player = event.getPlayer();
		
		Logins login = new Logins();
		login.setPlayer(player);
		login.setLoginTime(new Date());
		
        plugin.getDatabase().save(login);

	}

}


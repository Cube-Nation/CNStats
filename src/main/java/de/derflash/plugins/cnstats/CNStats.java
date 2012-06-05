package de.derflash.plugins.cnstats;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceException;

import org.bukkit.plugin.java.JavaPlugin;

public class CNStats extends JavaPlugin {
	
	
    public void onDisable() {
        System.out.println(this + " is now disabled!");
    }

    public void onEnable() {        
        setupDatabase();

        new PluginPlayerListener(this);

    }
    
    private void setupDatabase() {
        try {
            getDatabase().find(Logins.class).findRowCount();
        } catch (PersistenceException ex) {
            System.out.println("Installing database for " + getDescription().getName() + " due to first time usage");
            installDDL();
        }
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(Logins.class);
        return list;
    }
}

package de.derflash.plugins.cnstats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.PersistenceException;

import org.bukkit.plugin.java.JavaPlugin;

public class CNStats extends JavaPlugin {
	
    HashMap<String, Logins> tempLogins = new HashMap<String, Logins>();
    ArrayList<Logins> finalLogins = new ArrayList<Logins>();

    public void onDisable() {
        saveFinalLogins();
        saveTempLogins();
    }

    public void onEnable() {        
        setupDatabase();

        new PluginPlayerListener(this);
        
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                saveFinalLogins();
            }
        }, 12000L, 12000L);

    }
    
    private void saveFinalLogins() {
        final Logins[] _finalLogins = finalLogins.toArray(new Logins[0]);
        getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
            public void run() {
                getDatabase().save(_finalLogins);
            }
        });
        finalLogins.clear();
    }
    
    private void saveTempLogins() {
        final Logins[] _tempLogins = tempLogins.values().toArray(new Logins[0]);
        getServer().getScheduler().runTaskAsynchronously(this, new Runnable() {
            public void run() {
                getDatabase().save(_tempLogins);
            }
        });
        tempLogins.clear();
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

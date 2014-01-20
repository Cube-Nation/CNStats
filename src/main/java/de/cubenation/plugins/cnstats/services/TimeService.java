package de.cubenation.plugins.cnstats.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.OptimisticLockException;

import org.apache.commons.lang.Validate;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.QueryIterator;

import de.cubenation.plugins.cnstats.model.OnlineTime;
import de.cubenation.plugins.cnstats.model.exception.NoLastOnlineTimeFoundException;
import de.cubenation.plugins.utils.BukkitUtils;
import de.cubenation.plugins.utils.pluginapi.ScheduleManager;

/**
 * With this service, the player online times can be managed.
 * 
 * @since 1.1
 */
public class TimeService {
    // external services
    private final EbeanServer conn;
    private final Logger log;

    private final HashMap<String, OnlineTime> openTimes = new HashMap<String, OnlineTime>();
    private final ArrayList<OnlineTime> closedTimes = new ArrayList<OnlineTime>();

    /**
     * Initial with external services.
     * 
     * @param plugin
     *            Plugin for add asynchron bukkit task.
     * @param conn
     *            EbeanServer for database connection
     * @param log
     *            Logger for unexpected errors
     * 
     * @since 1.1
     */
    public TimeService(EbeanServer conn, Logger log) {
        this.conn = conn;
        this.log = log;
    }

    /**
     * Saves and clear cached online times with log out date is set in an
     * asyncron bukkit task.
     * 
     * @since 1.1
     */
    public final void saveClosedTimes() {
        saveClosedTimes(false);
    }

    /**
     * Saves and clear cached online times with log out date is set in an
     * asyncron bukkit task.
     * 
     * @param now
     *            If true, not in a asyncron bukkit task.
     * @since 1.1
     */
    public final void saveClosedTimes(boolean now) {
        synchronized (closedTimes) {
            if (!now) {
                final ArrayList<OnlineTime> closedTimesCopy = new ArrayList<OnlineTime>(closedTimes);
                ScheduleManager.runTaskAsynchronously(new Thread("ClosedTimeSaver") {
                    @Override
                    public void run() {
                        saveClosedTimesCache(closedTimesCopy);
                    }
                });
            } else {
                saveClosedTimesCache(closedTimes);
            }

            closedTimes.clear();
        }
    }

    private boolean saveClosedTimesCache(Collection<OnlineTime> closedTimes) {
        Validate.notEmpty(closedTimes, "closed times cannot be null or empty");

        try {
            conn.save(closedTimes.iterator());

            return true;
        } catch (OptimisticLockException e) {
            log.log(Level.SEVERE, "error on save closed times", e);

            return false;
        }
    }

    /**
     * Saves and clear cached online times with log out date is not set in an
     * asyncron bukkit task.
     * 
     * @since 1.1
     */
    public final void saveOpenTimes() {
        saveOpenTimes(false);
    }

    /**
     * Saves and clear cached online times with log out date is not set in an
     * asyncron bukkit task.
     * 
     * @param now
     *            If true, not in a asyncron bukkit task.
     * 
     * @since 1.1
     */
    public final void saveOpenTimes(boolean now) {
        synchronized (openTimes) {
            if (!now) {
                final ArrayList<OnlineTime> openTimesCopy = new ArrayList<OnlineTime>(openTimes.values());
                ScheduleManager.runTaskAsynchronously(new Thread("OpenTimeSaver") {
                    @Override
                    public void run() {
                        saveOpenTimesCache(openTimesCopy);
                    }
                });
            } else {
                saveOpenTimesCache(openTimes.values());
            }

            openTimes.clear();
        }
    }

    private boolean saveOpenTimesCache(Collection<OnlineTime> openTimes) {
        Validate.notEmpty(openTimes, "open times cannot be null or empty");

        try {
            conn.save(openTimes.iterator());

            return true;
        } catch (OptimisticLockException e) {
            log.log(Level.SEVERE, "error on save open times", e);

            return false;
        }
    }

    /**
     * Set log out time for a player to cache. If it's not cached before, it
     * will be read from database.
     * 
     * @param playerName
     *            case-insensitive player name
     * @return True, if successful, otherwise false. Also false, if an online
     *         time was not exists in cache or database.
     * 
     * @since 1.1
     */
    public final boolean setLogOut(String playerName) {
        Validate.notEmpty(playerName, "player name cannot be null or empty");

        synchronized (openTimes) {
            OnlineTime loginTime = openTimes.get(playerName);
            if (loginTime == null) {
                loginTime = conn.find(OnlineTime.class).where().ieq("player_name", playerName).orderBy("id desc").setMaxRows(1).findUnique();
            }

            if (loginTime != null) {
                loginTime.setLogoutTime(new Date());
                synchronized (closedTimes) {
                    closedTimes.add(loginTime);
                }
                openTimes.remove(playerName);

                return true;
            }
        }

        return false;
    }

    /**
     * Set log in time for a player to cache.
     * 
     * @param playerName
     *            case-insensitive player name
     * @return True, if successful, otherwise false.
     * 
     * @since 1.1
     */
    public final boolean addLogIn(String playerName) {
        Validate.notEmpty(playerName, "player name cannot be null or empty");

        OnlineTime login = new OnlineTime();
        login.setPlayerName(playerName);
        login.setLoginTime(new Date());

        synchronized (openTimes) {
            openTimes.put(playerName, login);
        }

        return true;
    }

    /**
     * Calculate online time for player.
     * 
     * @param playerName
     *            case-insensitive player name
     * @return Player online time in hours.
     * 
     * @since 1.1
     */
    public int getOnlineTime(String playerName) {
        Validate.notEmpty(playerName, "player name cannot be null or empty");

        QueryIterator<OnlineTime> findIterate = conn.find(OnlineTime.class).where().ieq("player_name", playerName).isNotNull("login_time")
                .isNotNull("logout_time").findIterate();

        float milliTime = 0;
        // database
        while (findIterate.hasNext()) {
            OnlineTime time = findIterate.next();
            milliTime += time.getLogoutTime().getTime() - time.getLoginTime().getTime();
        }

        // cached closed
        synchronized (closedTimes) {
            for (OnlineTime time : closedTimes) {
                if (time.getPlayerName().equalsIgnoreCase(playerName)) {
                    milliTime += time.getLogoutTime().getTime() - time.getLoginTime().getTime();
                }
            }
        }

        // cached open
        synchronized (openTimes) {
            if (openTimes.containsKey(playerName)) {
                milliTime += new Date().getTime() - openTimes.get(playerName).getLoginTime().getTime();
            }
        }

        int hours = 0;
        if (milliTime > 0) {
            hours = Math.round(milliTime / (3600 * 1000));
        }

        return hours;
    }

    /**
     * Return the last online time for a player.
     * 
     * @param playerName
     *            case-insensitive player name
     * @return last online time
     * @throws NoLastOnlineTimeFoundException
     *             if no time was found for player
     * 
     * @since 1.1
     */
    public Date getLastLoginTime(String playerName) throws NoLastOnlineTimeFoundException {
        Validate.notEmpty(playerName, "player name cannot be null or empty");

        if (BukkitUtils.isPlayerOnline(playerName)) {
            return new Date();
        }

        synchronized (openTimes) {
            if (openTimes.containsKey(playerName)) {
                return new Date();
            }
        }

        synchronized (closedTimes) {
            Date lastDate = null;
            for (OnlineTime time : closedTimes) {
                if (time.getPlayerName().equalsIgnoreCase(playerName)) {
                    if (lastDate == null || time.getLogoutTime().after(lastDate)) {
                        lastDate = time.getLogoutTime();
                    }
                }
            }
            if (lastDate != null) {
                return lastDate;
            }
        }

        OnlineTime onlineTime = conn.find(OnlineTime.class).where().ieq("player_name", playerName).orderBy("logout_time desc").setMaxRows(1).findUnique();

        if (onlineTime != null) {
            return onlineTime.getLogoutTime();
        }

        throw new NoLastOnlineTimeFoundException(playerName);
    }
}

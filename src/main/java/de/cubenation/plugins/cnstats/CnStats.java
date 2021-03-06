package de.cubenation.plugins.cnstats;

import java.util.List;

import org.bukkit.event.Listener;

import de.cubenation.plugins.cnstats.commands.OnlineTimeCommand;
import de.cubenation.plugins.cnstats.eventlistener.PlayerListener;
import de.cubenation.plugins.cnstats.model.OnlineTime;
import de.cubenation.plugins.cnstats.services.TimeService;
import de.cubenation.plugins.utils.pluginapi.BasePlugin;
import de.cubenation.plugins.utils.pluginapi.CommandSet;
import de.cubenation.plugins.utils.pluginapi.ScheduleTask;

public class CnStats extends BasePlugin {
    TimeService timeService;

    @Override
    protected void initialCustomServices() {
        timeService = new TimeService(getDatabase(), getLogger());
    }

    @Override
    protected void stopCustomServices() {
        timeService.saveClosedTimes(true);
        timeService.saveOpenTimes(true);
    }

    @Override
    protected void registerCustomEventListeners(List<Listener> list) {
        list.add(new PlayerListener(timeService));
    }

    @Override
    protected void registerScheduledTasks(List<ScheduleTask> list) {
        list.add(new ScheduleTask(new Thread("ClosedOnlineTimeSaver") {
            @Override
            public void run() {
                timeService.saveClosedTimes();
            }
        }, 60 * 60 * 20, 60 * 60 * 20)); // every 10 minutes
    }

    @Override
    protected void registerCommands(List<CommandSet> list) {
        list.add(new CommandSet(OnlineTimeCommand.class, timeService, chatService));
    }

    @Override
    protected void registerDatabaseModel(List<Class<?>> list) {
        list.add(OnlineTime.class);
    }

    /**
     * Return the plugin local used service for player online times.
     * 
     * @return
     * 
     * @since 1.1
     */
    public final TimeService getTimeService() {
        return timeService;
    }
}

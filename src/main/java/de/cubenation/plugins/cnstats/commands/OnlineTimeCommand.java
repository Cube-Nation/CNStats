package de.cubenation.plugins.cnstats.commands;

import org.bukkit.entity.Player;

import de.cubenation.plugins.cnstats.services.TimeService;
import de.cubenation.plugins.utils.chatapi.ChatService;
import de.cubenation.plugins.utils.commandapi.annotation.Asynchron;
import de.cubenation.plugins.utils.commandapi.annotation.Command;
import de.cubenation.plugins.utils.commandapi.annotation.CommandPermissions;

public class OnlineTimeCommand {
    private TimeService timeService;
    private ChatService chatService;

    public OnlineTimeCommand(TimeService timeService, ChatService chatService) {
        this.timeService = timeService;
        this.chatService = chatService;
    }

    @Command(main = "onlinetime", max = 0, help = "Zeigt deine gesammte Spielzeit auf dem Server an")
    @CommandPermissions("cnbase.onlinetime")
    @Asynchron
    public void onlineTime(Player player) {
        int onlineH = timeService.getOnlineTime(player.getName());

        chatService.one(player, "player.onlineTime", onlineH);
    }
}

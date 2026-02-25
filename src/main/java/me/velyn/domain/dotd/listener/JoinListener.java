package me.velyn.domain.dotd.listener;

import me.velyn.domain.dotd.ConfigManager;
import me.velyn.domain.dotd.PlayerDomainCache;
import me.velyn.domain.dotd.Scheduler;
import me.velyn.domain.dotd.actions.JoinMessageAction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.Optional;
import java.util.logging.Logger;

public class JoinListener implements Listener {

    private final ConfigManager configManager;
    private final PlayerDomainCache playerDomainCache;
    private final Scheduler scheduler;
    private final Logger log;

    public JoinListener(ConfigManager configManager, PlayerDomainCache playerDomainCache, Scheduler scheduler, Logger log) {
        this.configManager = configManager;
        this.playerDomainCache = playerDomainCache;
        this.scheduler = scheduler;
        this.log = log;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        String rawHostName = event.getHostname();
        if (configManager.isDebugLog()) {
            log.info(String.format("Received PlayerLoginEvent with HostName '%s'", rawHostName));
        }

        String[] parts = rawHostName.split(":");

        String hostName;
        if (parts.length == 1) {
            hostName = rawHostName;
        } else {
            hostName = parts[0];
        }

        if (configManager.isDebugLog()) {
            log.info(String.format("Sanitized HostName is '%s'", hostName));
        }
        playerDomainCache.add(player, hostName);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Optional<String> hostName = playerDomainCache.get(player);

        if (configManager.isDebugLog()) {
            log.info(String.format("Received PlayerJoinEvent and read '%s' from LoginEvent Cache", hostName.orElse(null)));
        }

        if (hostName.isEmpty()) {
            return;
        }

        Optional<JoinMessageAction> actionOpt = configManager.getDomainAction(hostName.get(), JoinMessageAction.class);
        if (actionOpt.isEmpty()) {
            return;
        }
        JoinMessageAction action = actionOpt.get();
        if (action.getDelayTicks() <= 0) {
            sendMsg(player, action);
        } else {
            scheduler.runAsyncLater(() -> sendMsg(player, action), action.getDelayTicks());
        }
    }

    private static void sendMsg(Player player, JoinMessageAction action) {
        player.sendMessage(action.getText());
    }
}

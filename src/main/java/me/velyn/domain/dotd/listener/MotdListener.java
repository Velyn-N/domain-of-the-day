package me.velyn.domain.dotd.listener;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import me.velyn.domain.dotd.ConfigManager;
import me.velyn.domain.dotd.actions.MotdAction;
import me.velyn.domain.dotd.actions.ServerIconAction;
import me.velyn.domain.dotd.actions.ServerListPlayersAction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class MotdListener implements Listener {

    private final ConfigManager configManager;
    private final Logger log;

    public MotdListener(ConfigManager configManager, Logger log) {
        this.configManager = configManager;
        this.log = log;
    }

    @EventHandler
    public void onLegacyMOTDRequest(ServerListPingEvent event) {
        String hostName = resolveHostName(event);
        defaultHandling(event, hostName);

        if (event instanceof PaperServerListPingEvent paperEvent) {
            configManager.getDomainAction(hostName, ServerListPlayersAction.class)
                    .ifPresent(slpAction -> handlePlayerAction(slpAction, paperEvent));
        } else if (configManager.isDebugLog()) {
            configManager.getDomainAction(hostName, ServerListPlayersAction.class)
                    .ifPresent(slpAction -> log.info(String.format("Not a Paper Event, cannot handle ServerListPlayersAction: %s", slpAction.getClass().getName())));
        }
    }

    @EventHandler
    public void handlePaper(PaperServerListPingEvent event) {
        String hostName = resolveHostName(event);
        defaultHandling(event, hostName);
        configManager.getDomainAction(hostName, ServerListPlayersAction.class)
                .ifPresent(slpAction -> handlePlayerAction(slpAction, event));
    }

    private String resolveHostName(ServerListPingEvent event) {
        String hostName = event.getHostname();
        if (hostName.isEmpty() && event instanceof PaperServerListPingEvent paperEvent) {
            if (configManager.isDebugLog()) {
                log.warning("getHostname() returned empty, falling back to getClient().getVirtualHost()");
            }
            try {
                InetSocketAddress virtualHost = paperEvent.getClient().getVirtualHost();
                hostName = virtualHost == null ? null : virtualHost.getHostString();
            } catch (Exception e) {
                if (configManager.isDebugLog()) {
                    log.warning(String.format("Failed to resolve virtual host from Paper event: %s", e));
                }
            }
        }
        return hostName;
    }

    private void defaultHandling(ServerListPingEvent event, String hostName) {
        if (configManager.isDebugLog()) {
            log.info(String.format("Received MOTD Ping for Domain '%s'", hostName));
        }
        configManager.getDomainAction(hostName, MotdAction.class)
                     .ifPresent(motdAction -> event.motd(motdAction.getMotd()));
        configManager.getDomainAction(hostName, ServerIconAction.class)
                .ifPresent(iconAction -> event.setServerIcon(iconAction.getIcon()));
    }

    private void handlePlayerAction(ServerListPlayersAction action, PaperServerListPingEvent event) {
        if (action.shouldHide()) {
            event.setHidePlayers(true);
        } else if (!action.getFakePlayers().isEmpty()) {
            List<PaperServerListPingEvent.ListedPlayerInfo> playerSample = event.getListedPlayers();
            playerSample.clear();
            for (String fakePlayer : action.getFakePlayers()) {
                playerSample.add(new PaperServerListPingEvent.ListedPlayerInfo(fakePlayer, UUID.randomUUID()));
            }
        }
        if (action.hasOnlinePlayerMod()) {
            event.setNumPlayers(action.getOnlinePlayers());
        }
        if (action.hasMaxPlayerMod()) {
            event.setMaxPlayers(action.getMaxPlayers());
        }
    }
}

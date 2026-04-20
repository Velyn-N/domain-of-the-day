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
        String hostName = null;
        PaperServerListPingEvent paperEvent = null;
        if ("com.destroystokyo.paper.network.StandardPaperServerListPingEventImpl"
                .equals(event.getClass().getCanonicalName())) {
            try {
                Object status = event.getClass().getMethod("getClient").invoke(event);
                InetSocketAddress virtualHost = (InetSocketAddress) status.getClass().getMethod("getVirtualHost")
                        .invoke(status);
                hostName = virtualHost.getHostString();
            } catch (Exception e) {
                if (configManager.isDebugLog()) {
                    log.warning(String.format("Error while using Paper Workaround: %s", e));
                }
            }
            try {
                paperEvent = (PaperServerListPingEvent) event;
            } catch (Exception e) {
                if (configManager.isDebugLog()) {
                    log.warning(String.format("Event is not a PaperServerListPingEvent: %s", e));
                }
            }
        }
        if (hostName == null) {
            if (configManager.isDebugLog()) {
                log.warning("Paper Workaround for reading MOTD Ping Hostnames did not work, falling back to API Method...");
            }
            hostName = event.getHostname();
        }

        defaultHandling(event, hostName);

        final PaperServerListPingEvent finPaperEvent = paperEvent;
        configManager.getDomainAction(hostName, ServerListPlayersAction.class)
                .ifPresent(slpAction -> {
                    if (finPaperEvent == null) {
                        if (configManager.isDebugLog()) {
                            log.info(String.format("Not a Paper Event, cannot handle ServerListPlayersAction: %s", slpAction.getClass().getName()));
                        }
                    } else {
                        handlePlayerAction(slpAction, finPaperEvent);
                    }
                });
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

    @EventHandler
    public void handlePaper(PaperServerListPingEvent event) {
        String hostName = event.getHostname();

        defaultHandling(event, hostName);
        configManager.getDomainAction(hostName, ServerListPlayersAction.class)
                .ifPresent(slpAction -> handlePlayerAction(slpAction, event));
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

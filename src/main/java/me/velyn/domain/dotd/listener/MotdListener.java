package me.velyn.domain.dotd.listener;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import me.velyn.domain.dotd.ConfigManager;
import me.velyn.domain.dotd.actions.MotdAction;
import me.velyn.domain.dotd.actions.ServerIconAction;
import me.velyn.domain.dotd.actions.ServerListPlayersAction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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
    public void onMOTDRequest(PaperServerListPingEvent event) {
        String hostName = event.getHostname();

        if (configManager.isDebugLog()) {
            log.info(String.format("Received MOTD Ping for Domain '%s'", hostName));
        }
        configManager.getDomainAction(hostName, MotdAction.class)
                     .ifPresent(motdAction -> event.motd(motdAction.getMotd()));
        configManager.getDomainAction(hostName, ServerIconAction.class)
                .ifPresent(iconAction -> event.setServerIcon(iconAction.getIcon()));
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

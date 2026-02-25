package me.velyn.domain.dotd.listener;

import me.velyn.domain.dotd.ConfigManager;
import me.velyn.domain.dotd.actions.*;

import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.util.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

public class MotdListener implements Listener {

    private final ConfigManager configManager;
    private final Logger log;

    public MotdListener(ConfigManager configManager, Logger log) {
        this.configManager = configManager;
        this.log = log;
    }

    @EventHandler
    public void onMOTDRequest(ServerListPingEvent event) {
        String hostName = getPaperHostName(event);

        if (configManager.isDebugLog()) {
            log.info(String.format("Received MOTD Ping for Domain '%s'", hostName));
        }
        configManager.getDomainAction(hostName, MotdAction.class)
                     .ifPresent(motdAction -> event.motd(motdAction.getMotd()));
        configManager.getDomainAction(hostName, ServerIconAction.class)
                .ifPresent(iconAction -> event.setServerIcon(iconAction.getIcon()));
    }

    private String getPaperHostName(ServerListPingEvent event) {
        if ("com.destroystokyo.paper.network.StandardPaperServerListPingEventImpl"
                .equals(event.getClass().getCanonicalName())) {
            try {
                Object status = event.getClass().getMethod("getClient").invoke(event);
                InetSocketAddress virtualHost = (InetSocketAddress) status.getClass().getMethod("getVirtualHost")
                        .invoke(status);
                return virtualHost.getHostString();
            } catch (Exception e) {
                if (configManager.isDebugLog()) {
                    log.warning(String.format("Error while using Paper Workaround: %s", e));
                }
            }
        }
        if (configManager.isDebugLog()) {
            log.warning("Paper Workaround for reading MOTD Ping Hostnames did not work, falling back to API Method...");
        }
        return event.getHostname();
    }
}

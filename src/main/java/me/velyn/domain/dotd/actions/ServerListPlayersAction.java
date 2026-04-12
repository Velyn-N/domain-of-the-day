package me.velyn.domain.dotd.actions;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Optional;

public final class ServerListPlayersAction implements DomainAction {

    private boolean shouldHide;
    private List<String> fakePlayers;
    private Integer onlinePlayers;
    private Integer maxPlayers;

    public boolean shouldHide() {
        return shouldHide;
    }

    public List<String> getFakePlayers() {
        return fakePlayers;
    }

    public boolean hasOnlinePlayerMod() {
        return onlinePlayers != null;
    }

    public Integer getOnlinePlayers() {
        return onlinePlayers;
    }

    public boolean hasMaxPlayerMod() {
        return maxPlayers != null;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    @Override
    public Optional<DomainAction> readFromConfig(ConfigurationSection config) {
        ConfigurationSection serverListPlayersSection = config.getConfigurationSection("serverlist-players");
        if (serverListPlayersSection == null) {
            return Optional.empty();
        }
        shouldHide = serverListPlayersSection.getBoolean("should-hide", false);
        fakePlayers = serverListPlayersSection.getStringList("fake-players");
        onlinePlayers = serverListPlayersSection.getInt("online-players");
        maxPlayers = serverListPlayersSection.getInt("max-players");
        return Optional.of(this);
    }

    @Override
    public String toString() {
        return "ServerListPlayersAction{" +
                "shouldHide=" + shouldHide +
                ", fakePlayers=" + fakePlayers +
                '}';
    }
}

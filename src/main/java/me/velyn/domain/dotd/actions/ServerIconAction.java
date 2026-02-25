package me.velyn.domain.dotd.actions;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.bukkit.*;
import org.bukkit.configuration.*;
import org.bukkit.util.*;

import me.velyn.domain.dotd.*;
import net.kyori.adventure.text.serializer.plain.*;

public final class ServerIconAction implements DomainAction {

    private CachedServerIcon icon;

    public CachedServerIcon getIcon() {
        return icon;
    }

    @Override
    public Optional<DomainAction> readFromConfig(ConfigurationSection config) {
        throw new UnsupportedOperationException("This Domain Action uses the expanded method");
    }

    @Override
    public Optional<DomainAction> readFromConfig(ConfigurationSection config, DotDMain main, Logger log) {
        String iconPath = config.getString("icon");
        if (iconPath == null) {
            return Optional.empty();
        }

        File iconFile = new File(main.getDataFolder(), iconPath);

        if (!iconFile.exists()) {
            log.warning("Icon file not found: " + iconPath);
            return Optional.empty();
        }

        try {
            this.icon = Bukkit.loadServerIcon(iconFile);
        } catch (Exception e) {
            log.severe("Failed to load icon from file '" + iconPath + "': " + e.getMessage());
            return Optional.empty();
        }
        return Optional.of(this);
    }

    @Override
    public String toString() {
        return "ServerIconAction{" +
                "icon=" + icon.toString() +
                '}';
    }
}

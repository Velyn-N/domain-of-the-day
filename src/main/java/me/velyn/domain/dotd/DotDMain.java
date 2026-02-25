package me.velyn.domain.dotd;

import me.velyn.domain.dotd.command.DotDCommand;
import me.velyn.domain.dotd.listener.JoinListener;
import me.velyn.domain.dotd.listener.MotdListener;
import me.velyn.domain.dotd.listener.QuitListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class DotDMain extends JavaPlugin {

    private Logger log;

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        super.onEnable();
        log = getLogger();

        saveDefaultConfig();

        configManager = new ConfigManager(log, this);
        configManager.applyConfigValues(getConfig());
        log.info("Config has been parsed");

        PlayerDomainCache playerDomainCache = new PlayerDomainCache();

        Scheduler scheduler = new Scheduler(this);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new MotdListener(configManager, getLogger()), this);
        pm.registerEvents(new JoinListener(configManager, playerDomainCache, scheduler, getLogger()), this);
        pm.registerEvents(new QuitListener(playerDomainCache), this);

        String fallbackPrefix = this.getName().replace("-", "");
        getServer().getCommandMap().register(fallbackPrefix, new DotDCommand(this, playerDomainCache));

        log.info("Plugin initialized");
    }

    public void reload() {
        log.info("Reloading...");
        reloadConfig();
        configManager.applyConfigValues(getConfig());
        log.info("Reloaded!");
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}

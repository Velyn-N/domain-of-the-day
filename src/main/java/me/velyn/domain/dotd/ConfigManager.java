package me.velyn.domain.dotd;

import me.velyn.domain.dotd.actions.DomainAction;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.logging.Logger;

public class ConfigManager {
    private static final String DEFAULT_DOMAIN = "default";
    private static final String CUSTOM_BLOCK = "custom";
    public static final String DEBUG_LOG_PROP = "debug-log";

    private final Logger log;
    private final DotDMain main;

    private boolean isDebugLog = false;

    private final Map<String, List<DomainAction>> domainActions;

    public ConfigManager(Logger logger, DotDMain main) {
        this.log = logger;
        this.main = main;
        domainActions = new HashMap<>();
    }

    public void applyConfigValues(ConfigurationSection config) {
        this.isDebugLog = config.getBoolean(DEBUG_LOG_PROP, false);

        readBlock(config, DEFAULT_DOMAIN);

        ConfigurationSection customs = config.getConfigurationSection(CUSTOM_BLOCK);
        if (customs != null) {
            customs.getKeys(false).forEach(key -> readBlock(customs, key));
        }

        if (isDebugLog()) {
            log.info("---------------------------------------------");
            log.info("Loaded the following DomainActions:");
            log.info("---------------------------------------------");
            domainActions.forEach((key, value) -> {
                log.info(String.format("Domain: %s", key));
                value.forEach(action -> log.info(String.format("Action: %s", action)));
                log.info("---------------------------------------------");
            });
        }
    }

    private void readBlock(ConfigurationSection config, String blockKey) {
        ConfigurationSection confBlock = config.getConfigurationSection(blockKey);
        if (confBlock == null) {
            log.info(String.format("Config Block %s appears to be empty", blockKey));
            return;
        }
        List<DomainAction> actions = DomainAction.parseConfig(confBlock, main, log);
        if (actions.isEmpty()) {
            log.info(String.format("Config Block %s appears to contain no valid Actions!", blockKey));
            return;
        }
        List<String> domains = confBlock.getStringList("domains");
        if (domains.isEmpty()) {
            log.info(String.format("Config Block %s does not specify domains, using the blockKey as Domain", blockKey));
            domainActions.put(blockKey.toLowerCase(), actions);
        } else {
            domains.forEach(domain -> domainActions.put(domain.toLowerCase(), actions));
        }
    }

    public boolean isDebugLog() {
        return isDebugLog;
    }

    public List<DomainAction> getDomainActions(String domain) {
        return domainActions.getOrDefault(domain.toLowerCase(), domainActions.getOrDefault(DEFAULT_DOMAIN, new ArrayList<>()));
    }

    public <T> Optional<T> getDomainAction(String domain, Class<T> type) {
        return getDomainActions(domain)
                .stream()
                .filter(type::isInstance)
                .map(type::cast)
                .findAny();
    }
}

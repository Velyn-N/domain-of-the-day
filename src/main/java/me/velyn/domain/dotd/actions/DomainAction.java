package me.velyn.domain.dotd.actions;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Optional;
import java.util.logging.*;
import java.util.stream.Stream;

import me.velyn.domain.dotd.*;

public sealed interface DomainAction permits JoinMessageAction, MotdAction, ServerIconAction {

    Optional<DomainAction> readFromConfig(ConfigurationSection config);

    default Optional<DomainAction> readFromConfig(ConfigurationSection config, DotDMain main, Logger log) {
        return readFromConfig(config);
    }

    static List<DomainAction> parseConfig(ConfigurationSection config, DotDMain main, Logger log) {
        return Stream.of(
                new MotdAction(),
                new JoinMessageAction(),
                new ServerIconAction()
               ).map(impl -> impl.readFromConfig(config, main, log))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }
}

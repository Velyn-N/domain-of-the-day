package me.velyn.domain.dotd.actions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Optional;

public final class PreventJoinAction implements DomainAction {

    private boolean enabled;

    private Component message;

    public boolean isEnabled() {
        return enabled;
    }

    public Component getMessage() {
        return message;
    }

    @Override
    public Optional<DomainAction> readFromConfig(ConfigurationSection config) {
        ConfigurationSection preventJoinSection = config.getConfigurationSection("preventjoin");
        if (preventJoinSection == null) {
            return Optional.empty();
        }
        this.enabled = preventJoinSection.getBoolean("enabled", false);
        if (!this.enabled) {
            return Optional.empty();
        }
        String text = preventJoinSection.getString("message");
        if (text == null || text.isEmpty()) {
            this.message = Component.text("You cannot join this server!");
        } else {
            this.message = MiniMessage.miniMessage().deserialize(text);
        }
        return Optional.of(this);
    }

    @Override
    public String toString() {
        return "PreventJoinAction{" +
                "enabled=" + enabled +
                ", text=" + PlainTextComponentSerializer.plainText().serialize(message) +
                '}';
    }
}

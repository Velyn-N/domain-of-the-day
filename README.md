# "Domain of the Day" Paper Plugin

A Plugin for [Paper](https://papermc.io) Minecraft Servers that detects the Domain used by a Player and sends them Messages depending on which one they use.

## What is this (originally) for?

When moving your Minecraft Server from one Domain to another you will need to notify your players.<br>

This plugin might help you in the early stages of a move where you want to gracefully tell your players to update their Server Lists.

## Features

The Plugin supports per-Domain MOTDs and Join Messages.

All these aspects of the plugin can be customized in the config file and live-reloaded using the `/dotd` command.
The Command requires the `dotd.admin` Permission.

The default config can be seen [here](./src/main/resources/config.yml).

The `/dotd players` Command allows you to see which Domains your players used for joining your server.

## Configuration

Here is an example of a fully specced out Configuration:

```yaml
    # Number of this configuration, can also be a name or something else
    1:
      # The Domains that will be affected by this Block
      domains:
        - example.com
        - sub.example.com
      # The "Message of the Day" (Text shown in the Server List).
      # Supports MiniMessage Format: https://docs.advntr.dev/minimessage/format.html
      motd: "<gray>A Minecraft Server"
      
      # A Message send to the Player's Chat after they join
      joinmessage:
        # The text that will be sent to the Player.
        # Supports MiniMessage Format: https://docs.advntr.dev/minimessage/format.html
        text: "<yellow>Welcome back!"
        # Delay for the Text to be sent in seconds.
        # 0 or smaller sends the Message immediately
        delay: 2
        
      # References an Image placed inside this plugins Data folder under "images/server-icon.png"
      icon: "images/server-icon.png"
      
      # Enabling this prevents Players from joining your server from this domain
      preventjoin:
        enabled: false
        # The text that will be displayed to rejected players
        # Supports MiniMessage Format: https://docs.advntr.dev/minimessage/format.html
        message: "<red>You are not allowed to join this server!"
        
      # Modifies the Player preview in the Serverlist
      serverlist-players:
        # If this is enabled no Player preview will be shown
        should-hide: false
        # Simulates fake Players on your server so you can write funny texts
        fake-players:
          - "Welcome to this"
          - "Minecraft Server"
        # Modifies the Online Player count in the Serverlist
        online-players: 2
        # Modifies the Maximum Player count in the Serverlist
        max-players: 99
```

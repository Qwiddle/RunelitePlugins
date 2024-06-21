package com.spinplugins.PlankBuddy;

import net.runelite.client.config.*;

@ConfigGroup("PlankBuddyConfig")
public interface PlankBuddyConfig extends Config {
    @ConfigSection(
            name = "Plugin Configuration",
            description = "Configure how to handles game tick delays, 1 game tick equates to roughly 600ms",
            position = 1
    )
    String pluginConfig = "pluginConfig";

    @ConfigSection(
            name = "Game Tick Configuration",
            description = "Configure how to handles game tick delays, 1 game tick equates to roughly 600ms",
            position = 2,
            closedByDefault = true
    )
    String delayTickConfig = "delayTickConfig";

    @ConfigItem(
            keyName = "Toggle",
            name = "Toggle",
            description = "",
            position = 0,
            section = pluginConfig
    )
    default Keybind toggle() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            keyName = "planksPerInventory",
            name = "Planks Per Inventory",
            description = "How many logs to withdraw per bank run.",
            position = 1,
            section = pluginConfig
    )
    default int planksPerInventory() {
        return 27;
    }

    @ConfigItem(
            keyName = "coinsThreshold",
            name = "Coins Threshold",
            description = "The plugin will go into a paused state while the player has less than the amount of gold specified.",
            position = 2,
            section = pluginConfig
    )
    default int stopAtCoinAmount() {
        return planksPerInventory() * 250;
    }

    @Range(
            max = 10
    )
    @ConfigItem(
            keyName = "tickDelayMin",
            name = "Game Tick Min",
            description = "",
            position = 2,
            section = delayTickConfig
    )
    default int tickDelayMin() {
        return 1;
    }

    @Range(
            max = 10
    )
    @ConfigItem(
            keyName = "tickDelayMax",
            name = "Game Tick Max",
            description = "",
            position = 3,
            section = delayTickConfig
    )
    default int tickDelayMax() {
        return 3;
    }

    @ConfigItem(
            keyName = "tickDelayEnabled",
            name = "Tick delay",
            description = "enables some tick delays",
            position = 4,
            section = delayTickConfig
    )
    default boolean tickDelay() {
        return true;
    }
}
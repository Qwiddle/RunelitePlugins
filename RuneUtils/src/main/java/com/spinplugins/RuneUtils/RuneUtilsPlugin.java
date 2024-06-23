package com.spinplugins.RuneUtils;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(name = "<html><font color=\"#FFFFFF\">[OP]</font> RuneUtils</html>",
        description = "open-source botting utility plugin.",
        tags = {"rune","bot","utility"})
@Slf4j
public class RuneUtilsPlugin extends Plugin {
    @Override
    protected void startUp() {
        log.info("RuneUtils started!");
    }

    @Override
    protected void shutDown() {
        log.info("RuneUtils stopped.");
    }
}

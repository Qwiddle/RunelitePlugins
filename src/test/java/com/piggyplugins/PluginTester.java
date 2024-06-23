package com.piggyplugins;

import com.spinplugins.IronBuddy.IronBuddyPlugin;
import com.spinplugins.RuneUtils.RuneUtilsPlugin;
import com.spinplugins.SkillBuddy.SkillBuddyPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PluginTester {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(
                IronBuddyPlugin.class,
                RuneUtilsPlugin.class
        );
        RuneLite.main(args);
    }
}
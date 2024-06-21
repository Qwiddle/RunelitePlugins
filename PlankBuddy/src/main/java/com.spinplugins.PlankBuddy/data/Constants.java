package com.spinplugins.PlankBuddy.data;

import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldPoint;

import java.awt.*;

public interface Constants {
    static final String PLUGIN_COLOR = String.format("#%02X%02X%02X", Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue());

    WorldPoint WC_GUILD_LUMBERYARD = new WorldPoint(1624, 3500, 0);
    WorldPoint WC_BANK = new WorldPoint(1592, 3475, 0);

    int OAK_PLANK_ID = ItemID.OAK_PLANK;
    int OAK_LOGS_ID = ItemID.OAK_LOGS;
    int GOLD_ID = ItemID.COINS_995;
}
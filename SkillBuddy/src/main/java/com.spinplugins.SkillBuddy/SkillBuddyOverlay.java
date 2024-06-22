package com.spinplugins.SkillBuddy;

import com.example.EthanApiPlugin.Collections.Inventory;
import com.example.EthanApiPlugin.Collections.TileObjects;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.geometry.RectangleUnion;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.*;
import net.runelite.client.util.AsyncBufferedImage;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Optional;

public class SkillBuddyOverlay extends OverlayPanel {
    private final Client client;
    private final SkillBuddyPlugin plugin;

    public static BufferedImage combineImages (AsyncBufferedImage... images) {
        // Load three images from disk

        int totalWidth = 0;
        int maxHeight = 0;

        for (BufferedImage image : images) {
            totalWidth += image.getWidth();
            maxHeight = Math.max(maxHeight, image.getHeight());
        }

        // Create a new buffered image
        BufferedImage combined = new BufferedImage(totalWidth, maxHeight, AsyncBufferedImage.TYPE_INT_ARGB);
        Graphics2D g = combined.createGraphics();

        int currentWidth = 0;

        for (AsyncBufferedImage image : images) {
            g.drawImage(image, currentWidth, 0, null);
            currentWidth += image.getWidth();
        }

        g.dispose();

        return combined;
    }

    @Inject
    private SkillBuddyOverlay(Client client, SkillBuddyPlugin plugin) {
        this.client = client;
        this.plugin = plugin;
        setPosition(OverlayPosition.BOTTOM_LEFT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);

        panelComponent.setOrientation(ComponentOrientation.VERTICAL);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        panelComponent.setPreferredSize(new Dimension(250, 360));
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("SkillBuddy 2.0")
                .color(new Color(214, 143, 49))
                .build());
        panelComponent.getChildren().add(LineComponent.builder()
                .left("Status")
                .leftColor(Color.YELLOW)
                .right(plugin.isStarted() ? "Running" : "Stopped")
                .build());
        panelComponent.getChildren().add(LineComponent.builder()
                .left(plugin.isStarted() ? "Runtime" : "Break timer")
                .leftColor(Color.WHITE)
                .right(plugin.getRuntime())
                .build());
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Modules")
                .color(new Color(214, 143, 49))
                .build());
        this.plugin.ModuleManager.getModules().forEach(buddyModule -> {
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(buddyModule.getName())
                    .leftColor(Color.YELLOW)
                    .right(buddyModule.isRunning ? "Running" : "Stopped")
                    .rightColor(buddyModule.isRunning ? Color.GREEN : Color.RED)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Task")
                    .leftColor(Color.YELLOW)
                    .right(buddyModule.getTask())
                    .rightColor(buddyModule.isRunning ? Color.GREEN : Color.RED)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(" ")
                    .right(" ")
                    .build());
            panelComponent.getChildren().add(
                    SplitComponent.builder()
                        .first(new ImageComponent(plugin.getImage(ItemID.OAK_PLANK, buddyModule.plankAmount)))
                        .orientation(ComponentOrientation.HORIZONTAL)
                        .second(new ImageComponent(plugin.getImage(ItemID.COINS_8890, buddyModule.coinAmount)))
                        .build()
            );
        });


        return super.render(graphics);
    }
}
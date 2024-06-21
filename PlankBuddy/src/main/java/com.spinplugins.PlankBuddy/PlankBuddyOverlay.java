package com.spinplugins.PlankBuddy;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class PlankBuddyOverlay extends OverlayPanel {
    private final Client client;
    private final PlankBuddyPlugin plugin;

    @Inject
    private PlankBuddyOverlay(Client client, PlankBuddyPlugin plugin) {
        this.client = client;
        this.plugin = plugin;
        setPosition(OverlayPosition.BOTTOM_LEFT);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        panelComponent.setPreferredSize(new Dimension(200, 360));
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("PlankBuddy")
                .color(new Color(214, 143, 49))
                .build());
        this.panelComponent.getChildren().add(TitleComponent.builder()
                .text(this.plugin.started ? "Running" : "Paused")
                .color(this.plugin.started ? Color.GREEN : Color.RED)
                .build());
        this.panelComponent.getChildren().add(LineComponent.builder()
                .left("Action:")
                .leftColor(Color.YELLOW)
                .right(this.plugin.playerState.name())
                .rightColor(Color.WHITE)
                .build());
        this.panelComponent.getChildren().add(TitleComponent.builder()
                .text("Debug")
                .color(new Color(214, 143, 49))
                .build());
        this.panelComponent.getChildren().add(LineComponent.builder()
                .left("Coins:")
                .leftColor(Color.YELLOW)
                .right(String.valueOf(this.plugin.playerTotalCoins))
                .rightColor(Color.WHITE)
                .build());
        this.panelComponent.getChildren().add(LineComponent.builder()
                .left("Logs remaining:")
                .leftColor(Color.YELLOW)
                .right(String.valueOf(this.plugin.playerTotalLogs))
                .rightColor(Color.WHITE)
                .build());
        this.panelComponent.getChildren().add(LineComponent.builder()
                .left("Planks made:")
                .leftColor(Color.YELLOW)
                .right(String.valueOf(this.plugin.playerTotalPlanks))
                .rightColor(Color.WHITE)
                .build());

        return super.render(graphics);
    }
}
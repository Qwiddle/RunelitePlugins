package com.spinplugins.SkillBuddy;

import com.example.EthanApiPlugin.EthanApiPlugin;
import com.piggyplugins.PiggyUtils.BreakHandler.ReflectBreakHandler;
import com.spinplugins.SkillBuddy.data.State;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.spinplugins.SkillBuddy.modules.SkillBuddyModule;
import com.spinplugins.SkillBuddy.panel.SkillBuddyPanel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;

@PluginDescriptor(
        name = "<html><font color=\"#ffffdd\"><b><font color=\"#e8762a\">[$] </font>SkillBuddy</b></font></html>",
        description = "AIO Home for your most boring skills. Once you go with SkillBuddy you'll never want to skill again.",
            enabledByDefault = true,
        tags = {"spin", "plugin"}
)
@Slf4j
public class SkillBuddyPlugin extends Plugin {
    @Inject
    private Client client;
    @Inject
    private SkillBuddyConfig config;
    @Inject
    private SkillBuddyOverlay overlay;
    @Inject
    private KeyManager keyManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ClientThread clientThread;
    @Inject
    private ClientToolbar clientToolbar;
    @Inject
    private ReflectBreakHandler breakHandler;
    static ItemManager itemManager = RuneLite.getInjector().getInstance(ItemManager.class);
    SkillBuddyModuleManager ModuleManager = new SkillBuddyModuleManager();
    private SkillBuddyPanel panel;
    private NavigationButton navButton;

    private State playerState;
    private Instant timer = Instant.now();
    private Instant lastBreakTimer = Instant.now();

    @Getter
    private boolean started = false;
    private long pauseTime = 0;
    private int timeout = 0;

    @Provides
    private SkillBuddyConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(SkillBuddyConfig.class);
    }

    private void setupPanel() {
        final BufferedImage panelIcon = ImageUtil.loadImageResource(getClass(), "panel_icon.png");

        panel = injector.getInstance(SkillBuddyPanel.class);
        navButton = NavigationButton.builder()
                .tooltip("SkillBuddy")
                .icon(panelIcon)
                .priority(4)
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);
    }

    @Override
    protected void startUp() throws Exception {
        breakHandler.registerPlugin(this);
        keyManager.registerKeyListener(toggle);
        overlayManager.add(overlay);

        timer = Instant.now();
        timeout = 0;

        this.pauseTime = 0;
        this.lastBreakTimer = null;

        if(ModuleManager.getModules().isEmpty()) {
            log.info("No modules enabled. Looking for modules to enable.");
            ModuleManager.loadModules("ConstructionModule");
        }

        setupPanel();
    }

    @Override
    protected void shutDown() throws Exception {
        keyManager.unregisterKeyListener(toggle);
        overlayManager.remove(overlay);
        breakHandler.stopPlugin(this);
        breakHandler.unregisterPlugin(this);
        timeout = 0;
        clientToolbar.removeNavigation(navButton);
        lastBreakTimer = null;
        pauseTime = 0;
        timer = null;
    }

    public String getElapsedTime() {
        Duration duration;

        if (!this.started) {
            duration = Duration.between(this.lastBreakTimer, Instant.now());
        } else {
            duration = Duration.between(this.timer, Instant.now());
        }

        long durationInMillis = duration.toMillis();
        return formatTime(durationInMillis);
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        if (!started || breakHandler.isBreakActive(this)) {
            return;
        }

        if (timeout > 0) {
            timeout--;
            return;
        }

        if (!EthanApiPlugin.loggedIn() || !started) {
            return;
        }

        ModuleManager.tickBuddyModules();

        playerState = getState();
        handleState();
    }

    public AsyncBufferedImage getImage(Integer itemId, Integer amount)
    {
        ItemComposition itemComposition = itemManager.getItemComposition(itemId);
        final AsyncBufferedImage image = itemManager.getImage(itemId, amount, itemComposition.isStackable());
        return image;
    }

    public SkillBuddyModule getFirstModule() {
        return ModuleManager.getModules().get(0);
    }

    private State getState() {
        if(client.getLocalPlayer().getAnimation() != -1) {
            return State.ANIMATING;
        }

        return State.WAITING;
    }

    private void handleState() {
        switch (playerState) {
            case ANIMATING:
                break;
            case WAITING:
                break;
            case BREAK:
                break;
            case TIMEOUT:
                break;
            default:
                break;
        }
    }

    private final HotkeyListener toggle = new HotkeyListener(() -> config.toggle()) {
        @Override
        public void hotkeyPressed() {
            toggle();
        }
    };

    public String formatTime(long time) {
        long second = time / 1000L % 60L;
        long minute = time / 60000L % 60L;
        long hour = time / 3600000L % 24L;
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    public String getRuntime() {
        if (!started) {
            return this.formatTime(this.getElapsedTimeMs(this.lastBreakTimer));
        } else {
            return this.getElapsedTime();
        }
    }

    public long getElapsedTimeMs(Instant time) {
        if(time == null) {
            return 0;
        }

        Duration duration = Duration.between(time, Instant.now());
        return duration.toMillis();
    }

    public void toggle() {
        if (!EthanApiPlugin.loggedIn()) {
            return;
        }

        if(started) {
            this.lastBreakTimer = Instant.now();
            breakHandler.stopPlugin(this);
            ModuleManager.getModules().forEach(SkillBuddyModule::toggle);

        } else {
            this.pauseTime = getElapsedTimeMs(this.lastBreakTimer);
            this.timer = Instant.now().minusMillis(this.pauseTime);
            breakHandler.startPlugin(this);
            ModuleManager.getModules().forEach(SkillBuddyModule::toggle);
        }

        started = !started;
    }
}
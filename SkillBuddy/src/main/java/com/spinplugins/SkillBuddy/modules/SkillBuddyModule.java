package com.spinplugins.SkillBuddy.modules;

import com.example.EthanApiPlugin.EthanApiPlugin;
import net.runelite.api.KeyCode;
import net.runelite.client.config.Keybind;
import net.runelite.client.util.HotkeyListener;

import java.awt.event.InputEvent;
import java.time.Duration;
import java.time.Instant;

import static java.awt.event.KeyEvent.VK_0;

public class SkillBuddyModule {
    public boolean isRunning = false;
    public Integer plankAmount = 1;
    public Integer coinAmount = 1;
    protected Instant timer;
    protected long pauseTime;
    public Keybind toggleKeybind = new Keybind(KeyCode.KC_0, InputEvent.CTRL_DOWN_MASK);

    public SkillBuddyModule() {
        this.timer = Instant.now();
    }

    public String getTask() {
        return "No task";
    }

    public String getName() {
        return "Unnamed Module";
    }

    public void toggle() {
        isRunning = !isRunning;
    }

    public final HotkeyListener toggle = new HotkeyListener(() -> toggleKeybind) {
        @Override
        public void hotkeyPressed() {
            toggle();
        }
    };

    public void initialize() {
        isRunning = true;
        startModule();
    }

    public void stopModule() {
        this.pauseTime = this.getElapsedTimeMs();
    }

    public void startModule() {
        this.timer = Instant.now();
    }

    public long getElapsedTimeMs() {
        Duration duration = Duration.between(this.timer, Instant.now());
        return duration.toMillis() + this.pauseTime;
    }

    public String getElapsedTime() {
        if (!this.isRunning) {
            long second = this.pauseTime / 1000L % 60L;
            long minute = this.pauseTime / 60000L % 60L;
            long hour = this.pauseTime / 3600000L % 24L;
            return String.format("%02d:%02d:%02d", hour, minute, second);
        } else {
            Duration duration = Duration.between(this.timer, Instant.now());
            long durationInMillis = duration.toMillis() + this.pauseTime;
            long second = durationInMillis / 1000L % 60L;
            long minute = durationInMillis / 60000L % 60L;
            long hour = durationInMillis / 3600000L % 24L;
            return String.format("%02d:%02d:%02d", hour, minute, second);
        }
    }

    public void startCrafting() {
        System.out.println("Crafting started");
        isRunning = true;
    }

    public void stopCrafting() {
        System.out.println("Crafting stopped");
        isRunning = false;
    }

    public void handleGameTick() {
        if(!isRunning) {
            return;
        }
    }
}

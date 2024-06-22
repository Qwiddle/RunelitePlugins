package com.spinplugins.SkillBuddy.modules;

import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;

@Slf4j
public class CraftingModule extends SkillBuddyModule {
    public String getName() {
        return "Crafting";
    }

    public BufferedImage getIcon() {
        return null;
    }

    @Override
    public void handleGameTick() {
        log.info("CraftingModule: handleGameTick() called");
    }
}

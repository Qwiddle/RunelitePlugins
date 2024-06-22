package com.spinplugins.SkillBuddy;

import com.spinplugins.SkillBuddy.modules.SkillBuddyModule;
import lombok.Getter;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class SkillBuddyModuleManager {
    private List<SkillBuddyModule> modules = new ArrayList<>();

    public SkillBuddyModule loadModule(String className) {
        try {
            Class<?> clazz = Class.forName("com.spinplugins.SkillBuddy.modules." + className);
            return (SkillBuddyModule) clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void loadModules(String... moduleClasses) {
        for (String className : moduleClasses) {
            SkillBuddyModule module = loadModule(className);
            if (module != null) {
                System.out.println("Module: " + module.getClass().getName());
                module.initialize();
                modules.add(module);
            }
        }
    }

    public void tickBuddyModules() {
        for (SkillBuddyModule module : modules) {
            module.handleGameTick();
        }
    }
}
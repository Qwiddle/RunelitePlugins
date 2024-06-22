package com.piggyplugins.PiggyUtils.strategy;


import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Getter
public class TaskManager {
    private static final Logger log = LoggerFactory.getLogger(TaskManager.class);
    private List<AbstractTask> tasks = new LinkedList<>();

    public void addTask(AbstractTask task) {
        log.debug("Adding task: " + task.getClass().getSimpleName());
        tasks.add(task);
    }
    public void removeTask(AbstractTask task) {
        tasks.remove(task);
    }
    public void clearTasks() {
        tasks.clear();
    }

    public boolean hasTasks() {
        return !tasks.isEmpty();
    }

    public void runTasks() {
        for (AbstractTask task : tasks) {
            if (task.validate()) {
                task.execute();
                break;
            }
        }
    }
}

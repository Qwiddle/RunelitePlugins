package com.spinplugins.PlankBuddy.task;

import com.spinplugins.PlankBuddy.data.State;
import com.spinplugins.PlankBuddy.data.Task;

import lombok.Getter;

@Getter
public class TaskModel {
    private Task task;
    private State state;
    public State PlankBuddyState = State.WAITING;

    public TaskModel(Task task) {
        this.task = task;
    }

    public State getState() {
        return State.WAITING;
    }

    public State onTick() {
        return State.WAITING;
    }

    public void handleState() {
        //
    }
}

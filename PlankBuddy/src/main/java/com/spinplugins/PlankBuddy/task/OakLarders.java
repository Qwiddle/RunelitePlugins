package com.spinplugins.PlankBuddy.task;

import com.example.EthanApiPlugin.Collections.*;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.BankInteraction;
import com.example.InteractionApi.BankInventoryInteraction;
import com.example.InteractionApi.NPCInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.example.Packets.WidgetPackets;
import com.spinplugins.PlankBuddy.PlankBuddyConfig;
import com.spinplugins.PlankBuddy.data.Constants;
import com.spinplugins.PlankBuddy.data.State;
import com.spinplugins.PlankBuddy.data.Task;
import net.runelite.api.NPC;
import net.runelite.api.TileObject;
import net.runelite.api.widgets.Widget;

import java.util.HashSet;
import java.util.Optional;

public class OakLarders extends TaskModel {
    private PlankBuddyConfig config;
    public State PlankBuddyState = State.WAITING;

    public Integer totalPlanks = 0;
    public Integer totalLogs = 0;
    public Integer coinsAmount = 0;
    public Integer timeout = 0;

    public boolean checkedBank = false;
    public boolean bankPin = false;

    public OakLarders(Task task, PlankBuddyConfig config) {
        super(task);
        this.config = config;
    }

    public State onTick() {
        PlankBuddyState = getTaskState();
        return PlankBuddyState;
    }

    public State getTaskState() {
        if (Bank.isOpen()) {
            if (Inventory.search().withId(Constants.OAK_PLANK_ID).first().isPresent()) {
                return State.DEPOSIT_PLANK;
            } else if (Inventory.search().withId(Constants.OAK_PLANK_ID).first().isEmpty() && Inventory.search().withId(Constants.OAK_LOGS_ID).first().isEmpty()) {
                totalLogs = getLogsFromBank();

                if (totalLogs <= config.planksPerInventory()) {
                    timeout = 10;
                    return State.TIMEOUT;
                }

                return State.GET_LOGS;
            }
        } else if (Inventory.getItemAmount(Constants.OAK_PLANK_ID) >= config.planksPerInventory()) {
            return State.GO_BANK;
        }

        if (Inventory.search().withId(Constants.OAK_LOGS_ID).first().isPresent()) {
            return State.BUY_PLANK;
        }

        if (!Inventory.full() && totalLogs <= config.planksPerInventory() && Inventory.search().withId(Constants.OAK_PLANK_ID).first().isEmpty()) {
            return State.GO_BANK;
        }

        return State.WAITING;
    }

    public int getLogsFromBank() {
        Optional<Widget> BankedLogs = Bank.search().withId(Constants.OAK_LOGS_ID).first();
        int logs = 0;

        if (Bank.isOpen() && BankedLogs.isPresent()) {
                logs = BankedLogs.get().getItemQuantity();

                if (logs < config.planksPerInventory()) {
                    EthanApiPlugin.sendClientMessage("Not enough logs left in bank.");
                } else {
                    BankInteraction.withdrawX(BankedLogs.get(), config.planksPerInventory());

                }
        } else if(!Bank.isOpen()) {
            if (accessBank()) {
                return 0;
            } else {
                EthanApiPlugin.sendClientMessage("Failed to access bank.");
            }
        }

        return logs;
    }

    private void bankItems() {
        if (Bank.isOpen()) {
            if (Inventory.search().withId(Constants.OAK_PLANK_ID).first().isPresent()) {
                BankInventoryInteraction.useItem(Constants.OAK_PLANK_ID, "Deposit-All");
            } else if (Inventory.search().withId(Constants.OAK_PLANK_ID).first().isEmpty() && Inventory.search().withId(Constants.OAK_LOGS_ID).first().isEmpty()) {
                getLogsFromBank();
            }
        } else {
            accessBank();
        }
    }

    public boolean accessBank() {
        if (!checkedBank) {
            checkedBank = true;
        }

        if (Bank.isOpen()) {
            return true;
        }

        if (Widgets.search().withId(13959169).first().isPresent()) {
            bankPin = true;
            return false;
        }

        if (Widgets.search().withId(786445).first().isEmpty()) {
            Optional<TileObject> BankChest = TileObjects.search().withName("Bank chest").nearestToPlayer();

            if (BankChest.isPresent()) {
                TileObjectInteraction.interact(BankChest.get(), "Use");
                return true;
            }

            Optional<TileObject> BankObject = TileObjects.search().withName("Bank").nearestToPlayer();

            if (BankObject.isPresent()) {
                TileObjectInteraction.interact(BankObject.get(), "Bank");
                return true;
            }

            Optional<NPC> BankNPC = NPCs.search().withAction("Bank").nearestToPlayer();

            if (BankNPC.isPresent()) {
                if (EthanApiPlugin.pathToGoal(BankNPC.get().getWorldLocation(), new HashSet<>()) != null) {
                    NPCInteraction.interact(BankNPC.get(), "Bank");
                    return true;
                }
            }

            if (TileObjects.search().withAction("Bank").nearestToPlayer().isEmpty() && NPCs.search().withAction("Bank").nearestToPlayer().isEmpty()) {
                EthanApiPlugin.sendClientMessage("Bank is not found, move to an area with a bank.");
            }
        }

        return false;
    }

    private void buyPlanks() {
        Optional<NPC> sawmillOperator = NPCs.search().withId(3101).nearestToPlayer();
        Optional<Widget> oakLogs = Inventory.search().withId(Constants.OAK_LOGS_ID).first();

        if (sawmillOperator.isPresent() && oakLogs.isPresent()) {
            if (EthanApiPlugin.getClient().getWidget(17694735) != null) {
                int plankAmount = Inventory.getItemAmount(Constants.OAK_PLANK_ID);
                totalPlanks += plankAmount;
                WidgetPackets.queueResumePause(17694735, plankAmount);
            } else {
                NPCInteraction.interact(sawmillOperator.get(), "Buy-plank");
            }
        } else {
            EthanApiPlugin.sendClientMessage("Sawmill operator not found. Walking closer.");

            if (EthanApiPlugin.pathToGoal(Constants.WC_GUILD_LUMBERYARD, new HashSet<>()) != null) {
                MousePackets.queueClickPacket();
                MovementPackets.queueMovement(Constants.WC_GUILD_LUMBERYARD);
            }
        }
    }

    public void handleState() {
        switch (PlankBuddyState) {
            case GO_BANK:
                bankItems();
                break;
            case GET_LOGS:
                totalLogs = getLogsFromBank();
                break;
            case BUY_PLANK:
                buyPlanks();
                break;
            case DEPOSIT_PLANK:
                bankItems();
                break;
            case BREAK:
                break;
            default:
                break;
        }
    }
}

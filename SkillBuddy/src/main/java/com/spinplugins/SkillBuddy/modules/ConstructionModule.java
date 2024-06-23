package com.spinplugins.SkillBuddy.modules;

import com.example.EthanApiPlugin.Collections.*;
import com.example.EthanApiPlugin.EthanApiPlugin;
import com.example.InteractionApi.BankInteraction;
import com.example.InteractionApi.BankInventoryInteraction;
import com.example.InteractionApi.NPCInteraction;
import com.example.InteractionApi.TileObjectInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.MovementPackets;
import com.example.Packets.WidgetPackets;
import com.spinplugins.SkillBuddy.modules.data.State;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.KeyCode;
import net.runelite.api.NPC;
import net.runelite.api.TileObject;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.Keybind;
import net.runelite.client.util.HotkeyListener;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;

import static com.spinplugins.SkillBuddy.modules.data.Constants.*;
import static com.spinplugins.SkillBuddy.modules.data.Constants.WC_GUILD_LUMBERYARD;

@Slf4j
public class ConstructionModule extends SkillBuddyModule {
    public State playerState = State.WAITING;
    public boolean started, isRunning = false;
    public int playerTotalLogs = 0;
    public int playerTotalPlanks = 0;
    public int playerTotalCoins = 0;
    public int timeout = 0;

    public boolean bankPin = false;
    public boolean checkedBank = false;
    public int stopAtCoinAmount = 10000;
    public int planksPerInventory = 27;

    public boolean tickDelay = true;
    public int tickDelayMax = 3;
    public int tickDelayMin = 0;

    @Override
    public String getName() {
        return "Construction";
    }

    @Override
    public String getTask() {
        return this.playerState.name();
    }

    @Override
    public void handleGameTick() {
        if(this.isRunning != this.started) {
            this.isRunning = this.started;
        }

        coinAmount = Inventory.search().withName("Coins").first().isPresent() ? Inventory.search().withName("Coins").first().get().getItemQuantity() : 1;
        plankAmount = Inventory.search().withName("Oak plank").first().isPresent() ? Inventory.search().withId(OAK_PLANK_ID).first().get().getItemQuantity() : 1;

        super.handleGameTick();

        if(!EthanApiPlugin.loggedIn() || !started) {
            return;
        }

        if (timeout > 0) {
            timeout--;
            return;
        }
        if (!EthanApiPlugin.loggedIn() || !started) {
            return;
        }
        playerTotalCoins = getCoins();
        playerState = getState();
        handleState();
    }

    public void toggle() {
        if (!EthanApiPlugin.loggedIn()) {
            return;
        }
        started = !started;
    }

    private int getCoins() {
        int coins = 0;

        if (Inventory.search().withName("Coins").first().isPresent()) {
            coins = Inventory.search().withName("Coins").first().get().getItemQuantity();
        }

        return coins;
    }

    private void setTimeout() {
        Random random = new Random();
        timeout = random.nextInt(tickDelayMax - tickDelayMin + 1) + tickDelayMin;
    }

    private State getState() {
        if (EthanApiPlugin.isMoving() || EthanApiPlugin.getClient().getLocalPlayer().getAnimation() != -1) {
            return State.ANIMATING;
        }

        if (bankPin) {
            return State.BANK_PIN;
        }

        if (timeout > 0) {
            return State.TIMEOUT;
        }

        if (tickDelay) {
            setTimeout();
        }

        if (playerTotalCoins < stopAtCoinAmount) {
            return State.BREAK;
        }

        if (Bank.isOpen()) {
            if (Inventory.search().withId(OAK_PLANK_ID).first().isPresent()) {
                return State.DEPOSIT_PLANK;
            } else if (Inventory.search().withId(OAK_PLANK_ID).first().isEmpty() && Inventory.search().withId(OAK_LOGS_ID).first().isEmpty()) {
                playerTotalLogs = getLogsFromBank();

                if (playerTotalLogs <= planksPerInventory) {
                    timeout = 10;
                    return State.TIMEOUT;
                }

                return State.GET_LOGS;
            }
        } else if (Inventory.getItemAmount(OAK_PLANK_ID) >= planksPerInventory) {
            return State.GO_BANK;
        }

        if (Inventory.search().withId(OAK_LOGS_ID).first().isPresent()) {
            return State.BUY_PLANK;
        }

        if (!Inventory.full() && playerTotalLogs <= planksPerInventory && Inventory.search().withId(OAK_PLANK_ID).first().isEmpty()) {
            return State.GO_BANK;
        }

        return State.WAITING;
    }

    private void buyPlanks() {
        Optional<NPC> sawmillOperator = NPCs.search().withId(3101).nearestToPlayer();
        Optional<Widget> oakLogs = Inventory.search().withId(OAK_LOGS_ID).first();

        if (sawmillOperator.isPresent() && oakLogs.isPresent()) {
            if (EthanApiPlugin.getClient().getWidget(17694735) != null) {
                int plankAmount = Inventory.getItemAmount(OAK_PLANK_ID);
                playerTotalPlanks += plankAmount;
                WidgetPackets.queueResumePause(17694735, plankAmount);
            } else {
                NPCInteraction.interact(sawmillOperator.get(), "Buy-plank");
            }
        } else {
            EthanApiPlugin.sendClientMessage("Sawmill operator not found. Walking closer.");

            if (EthanApiPlugin.pathToGoal(WC_GUILD_LUMBERYARD, new HashSet<>()) != null) {
                MousePackets.queueClickPacket();
                MovementPackets.queueMovement(WC_GUILD_LUMBERYARD);
            }
        }
    }

    private void bankItems() {
        if (Bank.isOpen()) {
            if (Inventory.search().withId(OAK_PLANK_ID).first().isPresent()) {
                BankInventoryInteraction.useItem(OAK_PLANK_ID, "Deposit-All");
            } else if (Inventory.search().withId(OAK_PLANK_ID).first().isEmpty() && Inventory.search().withId(OAK_LOGS_ID).first().isEmpty()) {
                getLogsFromBank();
            }
        } else {
            accessBank();
        }
    }

    public int getLogsFromBank() {
        int logs = 0;

        if (Bank.isOpen()) {
            Optional<Widget> BankedLogs = Bank.search().withId(OAK_LOGS_ID).first();
            if (BankedLogs.isPresent()) {
                logs = BankedLogs.get().getItemQuantity();

                if (logs < planksPerInventory) {
                    EthanApiPlugin.sendClientMessage("Not enough logs left in bank.");
                } else {
                    BankInteraction.withdrawX(BankedLogs.get(), planksPerInventory);

                }
            }
        } else {
            if (accessBank()) {
                return 0;
            } else {
                EthanApiPlugin.sendClientMessage("Failed to access bank.");
            }
        }

        return logs;
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

    private void handleState() {
        switch (playerState) {
            case GO_BANK:
                bankItems();
                break;
            case GET_LOGS:
                playerTotalLogs = getLogsFromBank();
                break;
            case BUY_PLANK:
                buyPlanks();
                break;
            case DEPOSIT_PLANK:
                bankItems();
                break;
            case BREAK:
                started = !started;
                break;
            default:
                break;
        }
    }
}

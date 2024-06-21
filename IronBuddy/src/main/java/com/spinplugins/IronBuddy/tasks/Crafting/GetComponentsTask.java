package com.spinplugins.IronBuddy.tasks.Crafting;

import com.example.EthanApiPlugin.Collections.*;
import com.example.InteractionApi.BankInteraction;
import com.example.Packets.MousePackets;
import com.example.Packets.WidgetPackets;
import com.piggyplugins.PiggyUtils.API.ObjectUtil;
import com.piggyplugins.PiggyUtils.API.PlayerUtil;
import com.piggyplugins.PiggyUtils.strategy.AbstractTask;
import com.spinplugins.IronBuddy.IronBuddyConfig;
import com.spinplugins.IronBuddy.IronBuddyPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemID;
import net.runelite.api.widgets.Widget;

import java.util.HashSet;
import java.util.Optional;

@Slf4j
public class GetComponentsTask extends AbstractTask<IronBuddyPlugin, IronBuddyConfig> {
    public GetComponentsTask(IronBuddyPlugin plugin, IronBuddyConfig config) {
        super(plugin, config);
    }

    @Override
    public boolean validate() {
        switch (config.taskType()) {
            case CRAFTING_GLASS_ITEM: {
                break;
            }
            case CRAFTING_GLASS:
                return Inventory.getEmptySlots() >= 14 && (Inventory.getItemAmount(ItemID.BUCKET_OF_SAND) < 14 || Inventory.getItemAmount(config.seaweedType().getId()) < 14);
        }

        return false;
    }

    @Override
    public void execute() {
        if(plugin.isSmelting) {
            plugin.isSmelting = false;
        }

        Optional<Widget> mainContinueOpt = Widgets.search().withTextContains("Click here to continue").first();

        if (mainContinueOpt.isPresent()) {
            MousePackets.queueClickPacket();
            WidgetPackets.queueResumePause(mainContinueOpt.get().getId(), -1);
            return;
        }

        if(Bank.isOpen())  {
            Optional<Widget> bankItem = Optional.empty();

            if(Inventory.getEmptySlots() == 28) {
                log.info("Withdrawing 14 buckets of sand");
                bankItem = Bank.search().withId(ItemID.BUCKET_OF_SAND).first();
            } else if (!Inventory.full() && BankInventory.search().withId(ItemID.BUCKET_OF_SAND).first().isPresent()) {
                log.info("Withdrawing 14 soda ash");
                bankItem = Bank.search().withId(ItemID.SODA_ASH).first();
            }

            if (bankItem.isPresent()) {
                BankInteraction.withdrawX(bankItem.get(), 14);
            } else {
                log.info("No items found in bank");
            }
            return;
        }

        TileObjects.search().withName("Bank booth").nearestToPlayer().ifPresentOrElse(
            tileObject -> {
                log.info("Attempting to bank");
                interactObject(tileObject, "Bank");
            }
            , () -> log.info("No bank found")
        );
    }
}
package org.thewitcher.facto.container;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.thewitcher.facto.Utility;
import org.thewitcher.facto.block.pipe.PipeBlockEntity;
import org.thewitcher.facto.item.modules.IModule;
import org.thewitcher.facto.misc.FilterSlot;

import javax.annotation.Nullable;

public abstract class AbstractPipeContainer<T extends IModule> extends AbstractContainerMenu {

    public final PipeBlockEntity tile;
    public final T module;
    public final int moduleIndex;
    public final ItemStack moduleStack;

    public AbstractPipeContainer(@Nullable MenuType<?> type, int id, Player player, BlockPos pos, int moduleIndex) {
        super(type, id);
        this.tile = Utility.getBlockEntity(PipeBlockEntity.class, player.level(), pos);
        this.moduleStack = moduleIndex < 0 ? null : this.tile.modules.getStackInSlot(moduleIndex);
        this.module = moduleIndex < 0 ? null : (T) this.moduleStack.getItem();
        this.moduleIndex = moduleIndex;

        // needs to be done here so transferStackInSlot works correctly, bleh
        // this.addSlots();

        // for (var l = 0; l < 3; ++l)
        //     for (var j1 = 0; j1 < 9; ++j1)
        //         this.addSlot(new Slot(player.getInventory(), j1 + l * 9 + 9, 8 + j1 * 18, 89 + l * 18 + 32));
        // for (var i1 = 0; i1 < 9; ++i1)
        //     this.addSlot(new Slot(player.getInventory(), i1, 8 + i1 * 18, 147 + 32));
    }

    protected abstract void addSlots();

    @Override
    public ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
        return Utility.transferStackInSlot(this, this::moveItemStackTo, player, slotIndex, stack -> {
            if (stack.getItem() instanceof IModule)
                return Pair.of(0, 3);
            return null;
        });
    }

    @Override
    public void clicked(int slotId, int dragType, @NotNull ClickType clickTypeIn, @NotNull Player player) {
        if (FilterSlot.checkFilter(this, slotId))
            return;
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }
}
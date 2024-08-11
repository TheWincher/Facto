package org.thewitcher.facto.container;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.thewitcher.facto.Utility;
import org.thewitcher.facto.block.pipe.PipeBlockEntity;
import org.thewitcher.facto.item.modules.IModule;
import org.thewitcher.facto.misc.FilterSlot;

import javax.annotation.Nonnull;
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
        for (var l = 0; l < 3; ++l)
            for (var j1 = 0; j1 < 9; ++j1)
                this.addSlot(new Slot(player.getInventory(), j1 + l * 9 + 9, 8 + j1 * 18, 171 - l * 18 - 48));

        for (var i1 = 0; i1 < 9; ++i1)
            this.addSlot(new Slot(player.getInventory(), i1, 8 + i1 * 18, 171 - 24));

        this.addSlots();
    }

    protected abstract void addSlots();

    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int slotIndex) {
        return Utility.transferStackInSlot(this, this::moveItemStackTo, player, slotIndex, stack -> {
            if (stack.getItem() instanceof IModule)
                return Pair.of(0, 3);
            return null;
        });
    }

    @Override
    public void clicked(int slotId, int dragType, @Nonnull ClickType clickTypeIn, @Nonnull Player player) {
        if (FilterSlot.checkFilter(this, slotId))
            return;
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return true;
    }
}
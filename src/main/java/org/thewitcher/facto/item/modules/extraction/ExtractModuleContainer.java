package org.thewitcher.facto.item.modules.extraction;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.thewitcher.facto.container.AbstractPipeContainer;
import org.thewitcher.facto.misc.DirectionSelector;
import org.thewitcher.facto.misc.DirectionSelector.IDirectionContainer;
import org.thewitcher.facto.misc.ItemFilter;
import org.thewitcher.facto.misc.ItemFilter.IFilteredContainer;

public class ExtractModuleContainer extends AbstractPipeContainer<ExtractModuleItem> implements IFilteredContainer, IDirectionContainer {
    public ItemFilter filter;
    public DirectionSelector directionSelector;

    public ExtractModuleContainer(@Nullable MenuType<?> type, int id, Player player, BlockPos pos, int moduleIndex) {
        super(type, id, player, pos, moduleIndex);
    }

    @Override
    protected void addSlots() {
        this.filter = this.module.getItemFilter(this.moduleStack, this.tile);
        this.directionSelector = this.module.getDirectionSelector(this.moduleStack, this.tile);

        for (var slot : this.filter.getSlots((176 - this.module.filterSlots * 18) / 2 + 1, 17 + 32))
            this.addSlot(slot);
    }

    @Override
    public void removed(@NotNull Player playerIn) {
        super.removed(playerIn);
        this.filter.save();
        this.directionSelector.save();
    }

    @Override
    public ItemFilter getFilter() {
        return this.filter;
    }

    @Override
    public DirectionSelector getSelector() {
        return this.directionSelector;
    }
}

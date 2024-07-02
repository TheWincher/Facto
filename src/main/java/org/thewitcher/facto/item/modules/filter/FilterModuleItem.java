package org.thewitcher.facto.item.modules.filter;

import net.minecraft.world.item.ItemStack;
import org.thewitcher.facto.block.pipe.PipeBlockEntity;
import org.thewitcher.facto.item.modules.IModule;
import org.thewitcher.facto.item.modules.ModuleItem;

public class FilterModuleItem extends ModuleItem {
    public FilterModuleItem(String name) {
        super(name);
    }

    @Override
    public boolean isCompatible(ItemStack module, PipeBlockEntity tile, IModule other) {
        return false;
    }

    @Override
    public boolean hasContainer(ItemStack module, PipeBlockEntity tile) {
        return false;
    }
}

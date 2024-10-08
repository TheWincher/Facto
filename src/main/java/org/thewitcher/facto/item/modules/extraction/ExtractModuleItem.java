package org.thewitcher.facto.item.modules.extraction;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.thewitcher.facto.container.AbstractPipeContainer;
import org.thewitcher.facto.Registry;
import org.thewitcher.facto.block.pipe.PipeBlockEntity;
import org.thewitcher.facto.item.modules.IModule;
import org.thewitcher.facto.item.modules.ModuleItem;
import org.thewitcher.facto.item.modules.ModuleTier;
import org.thewitcher.facto.misc.DirectionSelector;
import org.thewitcher.facto.misc.ItemFilter;
import org.thewitcher.facto.network.PipeNetwork;

public class ExtractModuleItem extends ModuleItem {

    private final int maxExtraction;
    private final int speed;
    private final boolean preventOversending;
    public final int filterSlots;

    public ExtractModuleItem(String name, ModuleTier tier) {
        super(name);
        this.maxExtraction = tier.forTier(1, 8, 64);
        this.speed = tier.forTier(20, 15, 10);
        this.filterSlots = tier.forTier(3, 6 , 9);
        this.preventOversending = true;
    }

    @Override
    public void tick(ItemStack module, PipeBlockEntity tile) {
        if (!tile.shouldWorkNow(this.speed) || !tile.canWork())
            return;
        var filter = this.getItemFilter(module, tile);
        var dirSelector = this.getDirectionSelector(module, tile);
        for (var dir : dirSelector.directions()) {
            var handler = tile.getItemHandler(dir);
            if (handler == null)
                continue;
            var network = PipeNetwork.get(tile.getLevel());
            for (var j = 0; j < handler.getSlots(); j++) {
                var stack = handler.extractItem(j, this.maxExtraction, true);
                if (stack.isEmpty())
                    continue;
                if (!filter.isAllowed(stack, dir))
                    continue;
                var remain = network.routeItem(tile.getBlockPos(), tile.getBlockPos().relative(dir), stack, this.preventOversending);
                if (remain.getCount() != stack.getCount()) {
                    handler.extractItem(j, stack.getCount() - remain.getCount(), false);
                    return;
                }
            }
        }
    }

    @Override
    public boolean canNetworkSee(ItemStack module, PipeBlockEntity tile, Direction direction, IItemHandler handler) {
        return !this.getDirectionSelector(module, tile).has(direction);
    }

    @Override
    public boolean canAcceptItem(ItemStack module, PipeBlockEntity tile, ItemStack stack, Direction direction, IItemHandler destination) {
        return !this.getDirectionSelector(module, tile).has(direction);
    }

    @Override
    public boolean isCompatible(ItemStack module, PipeBlockEntity tile, IModule other) {
        return !(other instanceof ExtractModuleItem);
    }

    @Override
    public boolean hasContainer(ItemStack module, PipeBlockEntity tile) {
        return true;
    }

    @Override
    public AbstractPipeContainer<?> getContainer(ItemStack module, PipeBlockEntity tile, int windowId, Inventory inv, Player player, int moduleIndex) {
        return new ExtractModuleContainer(Registry.extractModuleContainer, windowId, player, tile.getBlockPos(), moduleIndex);
    }

    @Override
    public ItemFilter getItemFilter(ItemStack module, PipeBlockEntity tile) {
        return new ItemFilter(this.filterSlots, module, tile);
    }

    @Override
    public DirectionSelector getDirectionSelector(ItemStack module, PipeBlockEntity tile) {
        return new DirectionSelector(module, tile);
    }
}

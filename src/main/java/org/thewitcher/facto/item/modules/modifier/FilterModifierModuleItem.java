package org.thewitcher.facto.item.modules.modifier;

import joptsimple.internal.Strings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.thewitcher.facto.container.AbstractPipeContainer;
import org.thewitcher.facto.Registry;
import org.thewitcher.facto.block.pipe.PipeBlockEntity;
import org.thewitcher.facto.item.modules.IModule;
import org.thewitcher.facto.item.modules.ModuleItem;
import org.thewitcher.facto.misc.ItemEquality;

public class FilterModifierModuleItem extends ModuleItem {
    public final ItemEquality.Type type;

    public FilterModifierModuleItem(String name, ItemEquality.Type type) {
        super(name);
        this.type = type;
    }

    @Override
    public boolean isCompatible(ItemStack module, PipeBlockEntity tile, IModule other) {
        return other != this;
    }

    @Override
    public boolean hasContainer(ItemStack module, PipeBlockEntity tile) {
        return this.type == ItemEquality.Type.TAG;
    }

    @Override
    public AbstractPipeContainer<?> getContainer(ItemStack module, PipeBlockEntity tile, int windowId, Inventory inv, Player player, int moduleIndex) {
        return new FilterModifierModuleContainer(Registry.filterModifierModuleContainer, windowId, player, tile.getBlockPos(), moduleIndex);
    }

    public ItemEquality getEqualityType(ItemStack stack) {
        if (this.type == ItemEquality.Type.TAG) {
            return ItemEquality.tag(FilterModifierModuleItem.getFilterTag(stack));
        } else {
            return this.type.getDefaultInstance();
        }
    }

    public static ResourceLocation getFilterTag(ItemStack stack) {
        if (!stack.hasTag())
            return null;
        var tag = stack.getTag().getString("filter_tag");
        if (Strings.isNullOrEmpty(tag))
            return null;
        return new ResourceLocation(tag);
    }

    public static void setFilterTag(ItemStack stack, ResourceLocation tag) {
        stack.getOrCreateTag().putString("filter_tag", tag.toString());
    }
}

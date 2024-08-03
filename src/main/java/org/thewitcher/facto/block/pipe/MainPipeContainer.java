package org.thewitcher.facto.block.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;
import org.thewitcher.facto.container.AbstractPipeContainer;
import org.thewitcher.facto.item.modules.IModule;

public class MainPipeContainer extends AbstractPipeContainer<IModule> {
    public MainPipeContainer(@Nullable MenuType<?> type, int id, Player player, BlockPos pos) {
        super(type, id, player, pos, -1);
    }

    @Override
    protected void addSlots() {
        // for (var i = 0; i < 3; i++)
        //     this.addSlot(new SlotItemHandler(this.tile.modules, i, 62 + i * 18, 17 + 32));
    }
}

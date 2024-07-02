package org.thewitcher.facto.block.pipe;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.thewitcher.facto.container.AbstractPipeGui;

public class MainPipeGui extends AbstractPipeGui<MainPipeContainer> {

    public MainPipeGui(MainPipeContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
    }
}

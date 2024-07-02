package org.thewitcher.facto.item.modules.extraction;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.thewitcher.facto.container.AbstractPipeGui;


public class ExtractModuleGui extends AbstractPipeGui<ExtractModuleContainer> {
    public ExtractModuleGui(ExtractModuleContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void init() {
        super.init();
        for (var widget : this.menu.filter.getButtons(this, this.leftPos + this.imageWidth - 7, this.topPos + 17 + 32 + 20, true))
            this.addRenderableWidget(widget);
        this.addRenderableWidget(this.menu.directionSelector.getButton(this.leftPos + 7, this.topPos + 17 + 32 + 20));
    }
}

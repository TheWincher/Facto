package org.thewitcher.facto.container;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.thewitcher.facto.Facto;
import org.thewitcher.facto.Registry;
import org.thewitcher.facto.block.pipe.ModuleState;
import org.thewitcher.facto.block.pipe.PipeBlock;
import org.thewitcher.facto.item.modules.IModule;
import org.thewitcher.facto.item.modules.extraction.ExtractModuleItem;
import org.thewitcher.facto.packets.PacketButton;
import org.thewitcher.facto.packets.PacketHandler;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public abstract class AbstractPipeGui<T extends AbstractPipeContainer<?>> extends AbstractContainerScreen<T> {

    protected static final ResourceLocation TEXTURE = new ResourceLocation(Facto.MODID, "textures/gui/pipe.png");
    protected static final ResourceLocation BLANK_GUI = new ResourceLocation(Facto.MODID, "textures/gui/blank_gui.png");
    protected static final ResourceLocation ITEM_SLOT_GUI = new ResourceLocation(Facto.MODID, "textures/gui/item_slot.png");
    protected static final ResourceLocation MODULE_SLOT_GUI = new ResourceLocation(Facto.MODID, "textures/gui/modules.png");
    private final List<Tab> tabs = new ArrayList<>();
    private final ItemStack[] lastItems = new ItemStack[this.menu.tile.modules.getSlots()];

    public AbstractPipeGui(T screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.imageWidth = 176;
        this.imageHeight = 171 + 32;
    }

    @Override
    protected void init() {
        super.init();
        this.initTabs();
    }

    @Override
    public void containerTick() {
        super.containerTick();

        var changed = false;
        for (var i = 0; i < this.menu.tile.modules.getSlots(); i++) {
            var stack = this.menu.tile.modules.getStackInSlot(i);
            if (stack != this.lastItems[i]) {
                this.lastItems[i] = stack;
                changed = true;
            }
        }
        if (changed)
            this.initTabs();
    }

    @Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        
        this.renderBackground(graphics);
        
        super.render(graphics, mouseX, mouseY, partialTicks);
        for (var widget : this.renderables) {
            // TDOO render widget tooltips?
           /* if (widget instanceof AbstractWidget abstractWidget) {
                if (abstractWidget.isHoveredOrFocused())
                    abstractWidget.renderToolTip(matrix, mouseX, mouseY);
            }*/
        }
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@Nonnull GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, title, 8, 8, 50);
        // graphics.drawString(this.font, this.playerInventoryTitle.getString(), 8, this.imageHeight - 96 + 2, 4210752, false);
        // graphics.drawString(this.font, this.title.getString(), 8, 6 + 32, 4210752, false);
        // for (var tab : this.tabs)
        //     tab.drawForeground(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.blit(AbstractPipeGui.BLANK_GUI, leftPos, topPos, 0, 0, 176, 171);

        graphics.blit(AbstractPipeGui.MODULE_SLOT_GUI, leftPos + 176, topPos, 0, 0, 26, 27);
        graphics.blit(AbstractPipeGui.MODULE_SLOT_GUI, leftPos + 176, topPos + 27, 0, 4, 26, 19);
        graphics.blit(AbstractPipeGui.MODULE_SLOT_GUI, leftPos + 176, topPos + 46, 229, 234, 26, 21);

        for(var itemSlot : menu.slots) {
            graphics.blit(AbstractPipeGui.ITEM_SLOT_GUI, leftPos + itemSlot.x - 1, topPos + itemSlot.y - 1, 0, 0, 18, 18);
        }
        
        // for (var tab : this.tabs)
        //     tab.draw(graphics);

        // // draw the slots since we're using a blank ui
        // for (var slot : this.menu.slots) {
        //     if (slot instanceof SlotItemHandler)
        //         graphics.blit(AbstractPipeGui.TEXTURE, this.leftPos + slot.x - 1, this.topPos + slot.y - 1, 176, 62, 18, 18);
        // }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        for (var tab : this.tabs) {
            if (tab.onClicked(x, y, button))
                return true;
        }
        return super.mouseClicked(x, y, button);
    }

    @Override
    public void onClose() {
        var module = this.menu.tile.modules.getStackInSlot(0);
        var state = this.menu.tile.getBlockState();
        if(module != null) {
            if (module.getItem() instanceof ExtractModuleItem) {
                state = state.setValue(PipeBlock.MODULE_PROPERTY, ModuleState.EXTRACT);
            } else {
                state = state.setValue(PipeBlock.MODULE_PROPERTY, ModuleState.NONE);
            }
        } else {
            state = state.setValue(PipeBlock.MODULE_PROPERTY, ModuleState.NONE);
        }

        var level = menu.tile.getLevel();
        if(level != null) {
            level.setBlockAndUpdate(this.menu.tile.getBlockPos(), state);
        }

        super.onClose();
    }

    private void initTabs() {
        this.tabs.clear();
        this.tabs.add(new Tab(new ItemStack(Registry.pipeBlock), 0, -1));
        for (var i = 0; i < this.menu.tile.modules.getSlots(); i++) {
            var stack = this.menu.tile.modules.getStackInSlot(i);
            if (stack.isEmpty())
                continue;
            var module = (IModule) stack.getItem();
            if (module.hasContainer(stack, this.menu.tile))
                this.tabs.add(new Tab(stack, this.tabs.size(), i));
        }
    }

    private class Tab {

        private final ItemStack moduleStack;
        private final int index;
        private final int x;
        private final int y;

        public Tab(ItemStack moduleStack, int tabIndex, int index) {
            this.moduleStack = moduleStack;
            this.index = index;
            this.x = AbstractPipeGui.this.leftPos + 5 + tabIndex * 28;
            this.y = AbstractPipeGui.this.topPos;
        }

        private void draw(GuiGraphics graphics) {
            var y = 2;
            var v = 0;
            var height = 30;
            var itemOffset = 9;
            if (this.index == AbstractPipeGui.this.menu.moduleIndex) {
                y = 0;
                v = 30;
                height = 32;
                itemOffset = 7;
            }
            graphics.blit(AbstractPipeGui.TEXTURE, this.x, this.y + y, 176, v, 28, height);
            graphics.renderItem(this.moduleStack, this.x + 6, this.y + itemOffset);
        }

        private void drawForeground(GuiGraphics graphics, int mouseX, int mouseY) {
            if (mouseX < this.x || mouseY < this.y || mouseX >= this.x + 28 || mouseY >= this.y + 32)
                return;
            graphics.renderTooltip(AbstractPipeGui.this.font, this.moduleStack.getHoverName(), mouseX - AbstractPipeGui.this.leftPos, mouseY - AbstractPipeGui.this.topPos);
        }

        private boolean onClicked(double mouseX, double mouseY, int button) {
            if (this.index == AbstractPipeGui.this.menu.moduleIndex)
                return false;
            if (button != 0)
                return false;
            if (mouseX < this.x || mouseY < this.y || mouseX >= this.x + 28 || mouseY >= this.y + 32)
                return false;
            PacketHandler.sendToServer(new PacketButton(AbstractPipeGui.this.menu.tile.getBlockPos(), PacketButton.ButtonResult.PIPE_TAB, this.index));
            AbstractPipeGui.this.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
            return true;
        }
    }
}

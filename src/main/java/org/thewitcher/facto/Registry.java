package org.thewitcher.facto;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.thewitcher.facto.block.pipe.*;
import org.thewitcher.facto.container.AbstractPipeContainer;
import org.thewitcher.facto.item.modules.IModule;
import org.thewitcher.facto.item.modules.ModuleItem;
import org.thewitcher.facto.item.modules.ModuleTier;
import org.thewitcher.facto.item.modules.extraction.ExtractModuleContainer;
import org.thewitcher.facto.item.modules.extraction.ExtractModuleGui;
import org.thewitcher.facto.item.modules.extraction.ExtractModuleItem;
import org.thewitcher.facto.item.modules.modifier.FilterModifierModuleContainer;
import org.thewitcher.facto.item.modules.modifier.FilterModifierModuleGui;
import org.thewitcher.facto.item.modules.modifier.FilterModifierModuleItem;
import org.thewitcher.facto.misc.ItemEquality;
import org.thewitcher.facto.network.PipeNetwork;
import org.thewitcher.facto.packets.PacketHandler;

import java.util.Comparator;
import java.util.Locale;
import java.util.function.BiFunction;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Registry {
    public static Capability<PipeNetwork> pipeNetworkCapability = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static Capability<IPipeConnectable> pipeConnectableCapability = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static Block pipeBlock;
    public static BlockEntityType<PipeBlockEntity> pipeBlockEntity;
    public static MenuType<MainPipeContainer> pipeContainer;

    public static Item extractModuleItem;


    public static MenuType<ExtractModuleContainer> extractModuleContainer;
    public static MenuType<FilterModifierModuleContainer> filterModifierModuleContainer;

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.BLOCKS, h -> {
            h.register(new ResourceLocation(Facto.MODID, "pipe"), Registry.pipeBlock = new PipeBlock());
        });

        event.register(ForgeRegistries.Keys.ITEMS, h -> {
            Registry.registerTieredModule(h, "extract_module", ExtractModuleItem::new);

            for (var type : ItemEquality.Type.values()) {
                var name = type.name().toLowerCase(Locale.ROOT) + "_filter_modifier";
                h.register(new ResourceLocation(Facto.MODID, name), new FilterModifierModuleItem(name, type));
            }

            ForgeRegistries.BLOCKS.getEntries().stream()
                    .filter(b -> b.getKey().location().getNamespace().equals(Facto.MODID))
                    .forEach(b -> h.register(b.getKey().location(), new BlockItem(b.getValue(), new Item.Properties())));
        });

        event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, h -> {
            h.register(new ResourceLocation(Facto.MODID, "pipe"), Registry.pipeBlockEntity = BlockEntityType.Builder.of(PipeBlockEntity::new, Registry.pipeBlock).build(null));
        });

        event.register(ForgeRegistries.Keys.MENU_TYPES, h -> {
            h.register(new ResourceLocation(Facto.MODID, "pipe"), Registry.pipeContainer = IForgeMenuType.create((windowId, inv, data) -> new MainPipeContainer(Registry.pipeContainer, windowId, inv.player, data.readBlockPos())));
//            h.register(new ResourceLocation(Facto.MODID, "item_terminal"), Registry.itemTerminalContainer = IForgeMenuType.create((windowId, inv, data) -> new ItemTerminalContainer(Registry.itemTerminalContainer, windowId, inv.player, data.readBlockPos())));
//            h.register(new ResourceLocation(Facto.MODID, "crafting_terminal"), Registry.craftingTerminalContainer = IForgeMenuType.create((windowId, inv, data) -> new CraftingTerminalContainer(Registry.craftingTerminalContainer, windowId, inv.player, data.readBlockPos())));
//            h.register(new ResourceLocation(Facto.MODID, "pressurizer"), Registry.pressurizerContainer = IForgeMenuType.create((windowId, inv, data) -> new PressurizerContainer(Registry.pressurizerContainer, windowId, inv.player, data.readBlockPos())));

            Registry.extractModuleContainer = Registry.registerPipeContainer(h, "extract_module");
//            Registry.filterModuleContainer = Registry.registerPipeContainer(h, "filter_module");
//            Registry.retrievalModuleContainer = Registry.registerPipeContainer(h, "retrieval_module");
//            Registry.stackSizeModuleContainer = Registry.registerPipeContainer(h, "stack_size_module");
//            Registry.filterIncreaseModuleContainer = Registry.registerPipeContainer(h, "filter_increase_module");
//            Registry.craftingModuleContainer = Registry.registerPipeContainer(h, "crafting_module");
            Registry.filterModifierModuleContainer = Registry.registerPipeContainer(h, "filter_modifier_module");
        });

        event.register(BuiltInRegistries.CREATIVE_MODE_TAB.key(), h -> {
            h.register(new ResourceLocation(Facto.MODID, "tab"), CreativeModeTab.builder()
                    .title(Component.translatable("item_group." + Facto.MODID + ".tab"))
                    .icon(() -> new ItemStack(Registry.pipeBlock))
                    .displayItems((params, output) -> ForgeRegistries.ITEMS.getEntries().stream()
                            .filter(b -> b.getKey().location().getNamespace().equals(Facto.MODID))
                            .sorted(Comparator.comparing(b -> b.getValue().getClass().getSimpleName()))
                            .forEach(b -> output.accept(b.getValue()))).build()
            );
        });

    }

    public static void setup(FMLCommonSetupEvent event) {
        PacketHandler.setup();
    }

    public static final class Client {

        public static void setup(FMLClientSetupEvent event) {
            BlockEntityRenderers.register(Registry.pipeBlockEntity, PipeRenderer::new);
//            EntityRenderers.register(Registry.pipeFrameEntity, PipeFrameRenderer::new);
//
            MenuScreens.register(Registry.pipeContainer, MainPipeGui::new);
//            MenuScreens.register(Registry.itemTerminalContainer, ItemTerminalGui::new);
//            MenuScreens.register(Registry.pressurizerContainer, PressurizerGui::new);
//            MenuScreens.register(Registry.craftingTerminalContainer, CraftingTerminalGui::new);
            MenuScreens.register(Registry.extractModuleContainer, ExtractModuleGui::new);
//            MenuScreens.register(Registry.filterModuleContainer, FilterModuleGui::new);
//            MenuScreens.register(Registry.retrievalModuleContainer, RetrievalModuleGui::new);
//            MenuScreens.register(Registry.stackSizeModuleContainer, StackSizeModuleGui::new);
//            MenuScreens.register(Registry.filterIncreaseModuleContainer, FilterIncreaseModuleGui::new);
//            MenuScreens.register(Registry.craftingModuleContainer, CraftingModuleGui::new);
            MenuScreens.register(Registry.filterModifierModuleContainer, FilterModifierModuleGui::new);
        }

    }

    private static <T extends AbstractPipeContainer<?>> MenuType<T> registerPipeContainer(RegisterEvent.RegisterHelper<MenuType<?>> helper, String name) {
        var type = (MenuType<T>) IForgeMenuType.create((windowId, inv, data) -> {
            var tile = Utility.getBlockEntity(PipeBlockEntity.class, inv.player.level(), data.readBlockPos());
            var moduleIndex = data.readInt();
            var moduleStack = tile.modules.getStackInSlot(moduleIndex);
            return ((IModule) moduleStack.getItem()).getContainer(moduleStack, tile, windowId, inv, inv.player, moduleIndex);
        });
        helper.register(new ResourceLocation(Facto.MODID, name), type);
        return type;
    }

    private static void registerTieredModule(RegisterEvent.RegisterHelper<Item> helper, String name, BiFunction<String, ModuleTier, ModuleItem> item) {
        for (var tier : ModuleTier.values())
            helper.register(new ResourceLocation(Facto.MODID, tier.name().toLowerCase(Locale.ROOT) + "_" + name), item.apply(name, tier));
    }
}

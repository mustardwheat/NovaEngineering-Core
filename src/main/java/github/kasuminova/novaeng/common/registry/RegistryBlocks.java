package github.kasuminova.novaeng.common.registry;

import github.kasuminova.novaeng.NovaEngineeringCore;
import github.kasuminova.novaeng.common.block.BlockAngel;
import github.kasuminova.novaeng.common.block.BlockHyperNetTerminal;
import github.kasuminova.novaeng.common.block.BlockModularServerAssembler;
import github.kasuminova.novaeng.common.block.estorage.*;
import github.kasuminova.novaeng.common.item.ItemBlockAngel;
import github.kasuminova.novaeng.common.tile.TileHyperNetTerminal;
import github.kasuminova.novaeng.common.tile.TileModularServerAssembler;
import github.kasuminova.novaeng.common.tile.estorage.EStorageCellDrive;
import github.kasuminova.novaeng.common.tile.estorage.EStorageController;
import github.kasuminova.novaeng.common.tile.estorage.EStorageEnergyCell;
import github.kasuminova.novaeng.common.tile.estorage.EStorageMEChannel;
import hellfirepvp.modularmachinery.common.block.BlockCustomName;
import hellfirepvp.modularmachinery.common.block.BlockDynamicColor;
import hellfirepvp.modularmachinery.common.block.BlockMachineComponent;
import hellfirepvp.modularmachinery.common.item.ItemBlockCustomName;
import hellfirepvp.modularmachinery.common.item.ItemBlockMachineComponent;
import hellfirepvp.modularmachinery.common.item.ItemBlockMachineComponentCustomName;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static hellfirepvp.modularmachinery.common.registry.RegistryBlocks.pendingIBlockColorBlocks;

@SuppressWarnings({"MethodMayBeStatic", "UnusedReturnValue"})
public class RegistryBlocks {
    public static final List<Block> BLOCK_MODEL_TO_REGISTER = new ArrayList<>();

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        GenericRegistryPrimer.INSTANCE.wipe(event.getGenericType());

        registerBlocks();
        registerTileEntities();

        GenericRegistryPrimer.INSTANCE.fillRegistry(event.getRegistry().getRegistrySuperType(), event.getRegistry());
    }

    public static void registerBlocks() {
        prepareItemBlockRegister(registerBlock(BlockHyperNetTerminal.INSTANCE));
        prepareItemBlockRegister(registerBlock(BlockModularServerAssembler.INSTANCE));
        prepareItemBlockRegister(new ItemBlockAngel(registerBlock(BlockAngel.INSTANCE)));

        prepareItemBlockRegister(registerBlock(BlockEStorageController.L4));
        prepareItemBlockRegister(registerBlock(BlockEStorageController.L6));
        prepareItemBlockRegister(registerBlock(BlockEStorageController.L9));
        prepareItemBlockRegister(registerBlock(BlockEStorageEnergyCell.L4));
        prepareItemBlockRegister(registerBlock(BlockEStorageEnergyCell.L6));
        prepareItemBlockRegister(registerBlock(BlockEStorageEnergyCell.L9));
        prepareItemBlockRegister(registerBlock(BlockEStorageCellDrive.INSTANCE));
        prepareItemBlockRegister(registerBlock(BlockEStorageMEChannel.INSTANCE));
        prepareItemBlockRegister(registerBlock(BlockEStorageTail.L4));
        prepareItemBlockRegister(registerBlock(BlockEStorageTail.L6));
        prepareItemBlockRegister(registerBlock(BlockEStorageTail.L9));
        prepareItemBlockRegister(registerBlock(BlockEStorageVent.INSTANCE));
        prepareItemBlockRegister(registerBlock(BlockEStorageCasing.INSTANCE));
    }

    public static void registerTileEntities() {
        registerTileEntity(TileHyperNetTerminal.class, "hypernet_terminal");
        registerTileEntity(TileModularServerAssembler.class, "modular_server_assembler");

        registerTileEntity(EStorageController.class, "estorage_controller");
        registerTileEntity(EStorageEnergyCell.class, "estorage_energy_cell");
        registerTileEntity(EStorageCellDrive.class, "estorage_cell_drive");
        registerTileEntity(EStorageMEChannel.class, "estorage_me_channel");
    }

    public static void registerBlockModels() {
        BLOCK_MODEL_TO_REGISTER.forEach(RegistryBlocks::registerBlockModel);
        BLOCK_MODEL_TO_REGISTER.clear();
    }

    public static void registerTileEntity(Class<? extends TileEntity> tile, String name) {
        GameRegistry.registerTileEntity(tile, new ResourceLocation(NovaEngineeringCore.MOD_ID, name));
    }

    public static <T extends Block> T registerBlock(T block) {
        BLOCK_MODEL_TO_REGISTER.add(block);
        GenericRegistryPrimer.INSTANCE.register(block);
        if (block instanceof BlockDynamicColor) {
            pendingIBlockColorBlocks.add((BlockDynamicColor) block);
        }

        return block;
    }

    public static ItemBlock prepareItemBlockRegister(Block block) {
        if (block instanceof BlockMachineComponent) {
            if (block instanceof BlockCustomName) {
                return prepareItemBlockRegister(new ItemBlockMachineComponentCustomName(block));
            } else {
                return prepareItemBlockRegister(new ItemBlockMachineComponent(block));
            }
        } else {
            if (block instanceof BlockCustomName) {
                return prepareItemBlockRegister(new ItemBlockCustomName(block));
            } else {
                return prepareItemBlockRegister(new ItemBlock(block));
            }
        }
    }

    public static <T extends ItemBlock> T prepareItemBlockRegister(T item) {
        Block block = item.getBlock();
        ResourceLocation registryName = Objects.requireNonNull(block.getRegistryName());
        String translationKey = block.getTranslationKey();

        item.setRegistryName(registryName).setTranslationKey(translationKey);
        RegistryItems.ITEMS_TO_REGISTER.add(item);
        return item;
    }

    public static void registerBlockModel(final Block block) {
        Item item = Item.getItemFromBlock(block);
        ResourceLocation registryName = Objects.requireNonNull(item.getRegistryName());
        ModelBakery.registerItemVariants(item, registryName);
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(registryName, "inventory"));
    }
}

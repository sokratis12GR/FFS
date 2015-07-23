package com.lordmau5.ffs;

import com.lordmau5.ffs.blocks.BlockTankFrame;
import com.lordmau5.ffs.blocks.BlockValve;
import com.lordmau5.ffs.network.NetworkHandler;
import com.lordmau5.ffs.proxy.CommonProxy;
import com.lordmau5.ffs.proxy.GuiHandler;
import com.lordmau5.ffs.tile.TileEntityTankFrame;
import com.lordmau5.ffs.tile.TileEntityValve;
import com.lordmau5.ffs.util.GenericUtil;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

/**
 * Created by Dustin on 28.06.2015.
 */
@Mod(modid = FancyFluidStorage.modId, name = "Fancy Fluid Storage", dependencies="after:waila;after:chisel;after:OpenComputers;after:ComputerCraft;after:BuildCraftAPI|Transport;after:funkylocomotion")
public class FancyFluidStorage {

    public static final String modId = "FFS";

    public static BlockValve blockValve;
    public static BlockTankFrame blockTankFrame;

    @Mod.Instance(modId)
    public static FancyFluidStorage instance;

    @SidedProxy(clientSide = "com.lordmau5.ffs.proxy.ClientProxy", serverSide = "com.lordmau5.ffs.proxy.CommonProxy")
    public static CommonProxy proxy;

    public int MB_PER_TANK_BLOCK = 16000;
    public boolean INSIDE_CAPACITY = false;
    public int MAX_SIZE = 13;
    public boolean ALLOW_DIFFERENT_METADATA = false;
    public int MIN_BURNABLE_TEMPERATURE = 1300;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit();

        Configuration config = new Configuration(event.getSuggestedConfigurationFile());

        config.load();

        Property mbPerTankProp = config.get(Configuration.CATEGORY_GENERAL, "mbPerVirtualTank", 16000);
        mbPerTankProp.comment = "How many millibuckets can each block within the tank store?\nDefault: 16000";
        MB_PER_TANK_BLOCK = mbPerTankProp.getInt(16000);

        Property insideCapacityProp = config.get(Configuration.CATEGORY_GENERAL, "onlyCountInsideCapacity", true);
        insideCapacityProp.comment = "Should tank capacity only count the interior air blocks, rather than including the frame?\nDefault: true";
        INSIDE_CAPACITY = insideCapacityProp.getBoolean(true);

        Property maxSizeProp = config.get(Configuration.CATEGORY_GENERAL, "maxSize", 13);
        maxSizeProp.comment = "Define the maximum size a tank can have. This includes the whole tank, including the frame!\nMinimum: 3, Maximum: 32\nDefault: 13";
        MAX_SIZE = Math.max(3, Math.min(maxSizeProp.getInt(), 32));

        Property diffMetaProp = config.get(Configuration.CATEGORY_GENERAL, "allowDifferentMetadata", false);
        diffMetaProp.comment = "Allow different metadata of the same block for the tank frames. This can be useful for mods like Chisel.\nDefault: false";
        ALLOW_DIFFERENT_METADATA = diffMetaProp.getBoolean(false);

        Property minBurnProp = config.get(Configuration.CATEGORY_GENERAL, "minimumBurnableTemperature", 1300);
        minBurnProp.comment = "At which temperature should a tank start burning on a random occasion?\nThis only applies to blocks that are flammable, like Wood or Wool.\nDefault: 1300 (Temperature of Lava)";
        MIN_BURNABLE_TEMPERATURE = minBurnProp.getInt(1300);

        if (config.hasChanged()) {
            config.save();
        }

        GameRegistry.registerBlock(blockValve = new BlockValve(), "blockValve");
        GameRegistry.registerBlock(blockTankFrame = new BlockTankFrame(), "blockTankFrame");

        GameRegistry.registerTileEntity(TileEntityValve.class, "tileEntityValve");
        GameRegistry.registerTileEntity(TileEntityTankFrame.class, "tileEntityTankFrame");

        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());

        NetworkHandler.registerChannels(event.getSide());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

        GameRegistry.addRecipe(new ItemStack(blockValve), "IGI", "GBG", "IGI",
                'I', Items.iron_ingot,
                'G', Blocks.iron_bars,
                'B', Items.bucket);

        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        GenericUtil.init();
    }

}

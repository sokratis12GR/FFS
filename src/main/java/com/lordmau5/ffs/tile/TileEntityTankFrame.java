package com.lordmau5.ffs.tile;

import com.lordmau5.ffs.util.ExtendedBlock;
import cpw.mods.fml.common.Optional;
import framesapi.IMoveCheck;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Created by Dustin on 28.06.2015.
 */

@Optional.InterfaceList(value = {
        @Optional.Interface(iface = "framesapi.IMoveCheck", modid = "funkylocomotion")
})
public class TileEntityTankFrame extends TileEntity implements IMoveCheck {

    private ExtendedBlock block;
    public int valveX, valveY, valveZ;
    private TileEntityValve masterValve;
    private boolean hasValve = false;
    private int prevLightValue = 0;

    public boolean burning = false;

    public TileEntityTankFrame() {
        super();
    }

    public TileEntityTankFrame(TileEntityValve masterValve, ExtendedBlock block) {
        this.masterValve = masterValve;
        this.block = block;
    }

    @Override
    public void updateEntity() {
        if(worldObj.isRemote) {
            if(getValve() != null) {
                int brightness = getValve().getFluidLuminosity();
                if(prevLightValue != brightness) {
                    prevLightValue = brightness;
                    worldObj.updateLightByType(EnumSkyBlock.Block, xCoord, yCoord, zCoord);
                }
                return;
            }
            return;
        }

        if(masterValve == null && hasValve) {
            TileEntity tile = worldObj.getTileEntity(valveX, valveY, valveZ);
            if(tile != null && tile instanceof TileEntityValve)
                setValve((TileEntityValve) tile);
        }
    }

    public boolean isFrameInvalid() {
        TileEntity tile = worldObj.getTileEntity(xCoord, yCoord, zCoord);
        return tile == null || !(tile instanceof TileEntityTankFrame) || tile != this;
    }

    public void startBurning() {
        Block block = getBlock().getBlock();
        if(block == null || !block.isFlammable(worldObj, xCoord, yCoord, zCoord, ForgeDirection.UNKNOWN))
            return;

        onBreak();
        worldObj.setBlock(xCoord, yCoord, zCoord, Blocks.fire);
    }

    public void breakFrame() {
        if(isFrameInvalid())
            return;

        worldObj.removeTileEntity(xCoord, yCoord, zCoord);
        if(block != null && block.getBlock() != null)
            worldObj.setBlock(xCoord, yCoord, zCoord, block.getBlock(), block.getMetadata(), 2);
        else
            worldObj.setBlockToAir(xCoord, yCoord, zCoord);
    }

    public void onBreak() {
        if(masterValve != null && !worldObj.isRemote) {
            masterValve.breakTank(this);
        }
    }

    public void setValve(TileEntityValve valve) {
        this.masterValve = valve;
    }

    public TileEntityValve getValve() {
        if(this.masterValve == null && hasValve) {
            TileEntity tile = worldObj.getTileEntity(valveX, valveY, valveZ);
            if(tile != null && tile instanceof TileEntityValve)
                setValve((TileEntityValve) tile);
        }
        return this.masterValve;
    }

    public void setBlock(ExtendedBlock block) {
        this.block = block;
    }

    public ExtendedBlock getBlock() {
        return block;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        if(tag.hasKey("valveX")) {
            valveX = tag.getInteger("valveX");
            valveY = tag.getInteger("valveY");
            valveZ = tag.getInteger("valveZ");
            hasValve = true;
        }
        if(tag.hasKey("blockId")) {
            this.block = new ExtendedBlock(Block.getBlockById(tag.getInteger("blockId")), tag.getInteger("metadata"));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        if(getValve() != null) {
            tag.setInteger("valveX", getValve().xCoord);
            tag.setInteger("valveY", getValve().yCoord);
            tag.setInteger("valveZ", getValve().zCoord);
        }
        if(getBlock() != null) {
            tag.setInteger("blockId", Block.getIdFromBlock(getBlock().getBlock()));
            tag.setInteger("metadata", getBlock().getMetadata());
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.func_148857_g());
        this.burning = pkt.func_148857_g().getBoolean("burning");
        markForUpdate();
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        tag.setBoolean("burning", this.burning);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    public void markForUpdate() {
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Optional.Method(modid = "funkylocomotion")
    @Override
    public boolean canMove(World worldObj, int x, int y, int z) {
        return false;
    }
}

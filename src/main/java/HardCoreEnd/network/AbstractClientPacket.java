package HardCoreEnd.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class AbstractClientPacket implements IPacket{
    @Override
    public void handle(Side side, EntityPlayer player){
        //TODO EntityPlayerMultiplayer izmenjen za EntityPlayerMP
        if (side == Side.CLIENT){
            handle((EntityPlayerMP)player);
        }
        else{
            throw new UnsupportedOperationException("Tried to handle client packet on server side! Packet class: "+getClass().getSimpleName());
        }
    }

    @SideOnly(Side.CLIENT)
    protected abstract void handle(EntityPlayerMP player);
}


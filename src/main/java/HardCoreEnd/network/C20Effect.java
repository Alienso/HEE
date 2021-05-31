package HardCoreEnd.network;

import HardCoreEnd.random.FXHandler;
import HardCoreEnd.random.FXType;
import HardCoreEnd.random.Pos;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class C20Effect extends AbstractClientPacket{
    private FXType.Basic type;
    private double x, y, z;

    public C20Effect(){}

    public C20Effect(FXType.Basic type, double x, double y, double z){
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public C20Effect(FXType.Basic type, Pos pos){
        this(type, pos.getX()+0.5D, pos.getY()+0.5D, pos.getZ()+0.5D);
    }

    public C20Effect(FXType.Basic type, Entity entity){
        this(type, entity.posX, entity.posY, entity.posZ);
    }

    public C20Effect(FXType.Basic type, TileEntity tile){
        this(type, tile.getPos().getX()+0.5D, tile.getPos().getY()+0.5D, tile.getPos().getZ()+0.5D);
    }

    @Override
    public void write(ByteBuf buffer){
        buffer.writeByte(type.ordinal()).writeDouble(x).writeDouble(y).writeDouble(z);
    }

    @Override
    public void read(ByteBuf buffer){
        byte fxType = buffer.readByte();

        if (fxType >= 0 && fxType < FXType.Basic.values.length){
            type = FXType.Basic.values[fxType];
            x = buffer.readDouble();
            y = buffer.readDouble();
            z = buffer.readDouble();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handle(EntityPlayerMP player){
        if (type != null) FXHandler.handleBasic(player.world, player, type, x, y, z);
    }
}

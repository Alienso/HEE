package HardCoreEnd.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class C06SetPlayerVelocity extends AbstractClientPacket{
    private double velX, velY, velZ;

    public C06SetPlayerVelocity(){}

    public C06SetPlayerVelocity(double velocityX, double velocityY, double velocityZ){
        this.velX = velocityX;
        this.velY = velocityY;
        this.velZ = velocityZ;
    }

    @Override
    public void write(ByteBuf buffer){
        buffer.writeDouble(velX).writeDouble(velY).writeDouble(velZ);
    }

    @Override
    public void read(ByteBuf buffer){
        velX = buffer.readDouble();
        velY = buffer.readDouble();
        velZ = buffer.readDouble();
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handle(EntityPlayerMP player){
        player.motionX = velX;
        player.motionY = velY;
        player.motionZ = velZ;
    }
}

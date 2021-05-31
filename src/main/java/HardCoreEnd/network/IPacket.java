package HardCoreEnd.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

public interface IPacket{
    void write(ByteBuf buffer);
    void read(ByteBuf buffer);
    void handle(Side side, EntityPlayer player);
}

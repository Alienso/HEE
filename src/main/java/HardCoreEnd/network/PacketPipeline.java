package HardCoreEnd.network;

import HardCoreEnd.Main;
import HardCoreEnd.random.Pos;
import HardCoreEnd.random.Stopwatch;
import gnu.trove.map.hash.TByteObjectHashMap;
import gnu.trove.map.hash.TObjectByteHashMap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.io.FilenameUtils;

public class PacketPipeline{
    private static PacketPipeline instance;
    private static final String channelName = "hee";

    public static synchronized void initializePipeline(){
        if (instance != null){
            throw new RuntimeException("Packet pipeline has already been registered!");
        }

        instance = new PacketPipeline();
        instance.load();
    }

    private FMLEventChannel eventDrivenChannel;
    private EnumMap<Side, FMLEmbeddedChannel> channels;

    private final TByteObjectHashMap<Class<? extends IPacket>> idToPacket = new TByteObjectHashMap<>();
    private final TObjectByteHashMap<Class<? extends IPacket>> packetToId = new TObjectByteHashMap<>();

    private PacketPipeline(){}

    private void load(){
        Stopwatch.time("PacketPipeline");

        eventDrivenChannel = NetworkRegistry.INSTANCE.newEventDrivenChannel(channelName);
        eventDrivenChannel.register(this);

        try{
            Field channelField = FMLEventChannel.class.getDeclaredField("channels");
            channelField.setAccessible(true);
            channels = (EnumMap)channelField.get(eventDrivenChannel);

            int id = -1;

            File sourceFile = Main.sourceFile;
            List<String> classes = new ArrayList<>();

            if (sourceFile.isDirectory()){
                File root = Paths.get(sourceFile.getPath(), "HardCoreEnd").toFile();

                for(String name:new File(root,"network").list()){
                    if (name.startsWith("C"))classes.add("HardCoreEnd.network."+FilenameUtils.removeExtension(name));
                }
                //classes.add("HardCoreEnd.network.C06SetPlayerVelocity");
                //classes.add("HardCoreEnd.network.C20Effect");

                /*for(String name:new File(root, "server").list()){
                    if (name.startsWith("S"))classes.add("HardCoreEnd.network"+FilenameUtils.removeExtension(name));
                }*/
            }
            else{
                try(ZipFile zip = new ZipFile(sourceFile)){
                    for(ZipEntry entry:Collections.list(zip.entries())){
                        String name = entry.getName();

                        if (name.startsWith("HardCoreEnd/network")){
                            if (name.startsWith("HardCoreEnd/network/C") || name.startsWith("HardCoreEnd.network/S")){
                                classes.add(FilenameUtils.removeExtension(name.replace('/', '.')));
                            }
                        }
                    }
                }catch(IOException e){
                    throw e;
                }
            }

            Collections.sort(classes);
            for(String cls:classes)registerPacket(++id, (Class<? extends IPacket>)Class.forName(cls));
        }catch(NoSuchFieldException | IllegalArgumentException | IllegalAccessException | IOException | ClassNotFoundException e){
            throw new RuntimeException("Unable to load the Packet system!", e);
        }

        Stopwatch.finish("PacketPipeline");
    }

    private void registerPacket(int id, Class<? extends IPacket> cls){
        idToPacket.put((byte)id, cls);
        packetToId.put(cls, (byte)id);
    }

    private FMLProxyPacket writePacket(IPacket packet){
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeByte(packetToId.get(packet.getClass()));
        packet.write(buffer);
        return new FMLProxyPacket(buffer, channelName);
    }

    /*private void readPacket(FMLProxyPacket fmlPacket, Side side){
        ByteBuf buffer = fmlPacket.payload();

        try{
            IPacket packet = idToPacket.get(buffer.readByte()).newInstance();
            packet.read(buffer.slice());

            switch(side){
                case CLIENT:
                    packet.handle(Side.CLIENT, getClientPlayer());
                    break;

                case SERVER:
                    packet.handle(Side.SERVER, ((NetHandlerPlayServer)fmlPacket.handler()).playerEntity);
                    break;
            }
        }catch(InstantiationException | IllegalAccessException e){
            e.printStackTrace();
        }
    }

    @SideOnly(Side.CLIENT)
    private EntityPlayer getClientPlayer(){
        return Minecraft.getMinecraft().thePlayer;
    }

    // EVENTS AND DISPATCHING

    @SubscribeEvent
    public void onClientPacketReceived(ClientCustomPacketEvent e){
        readPacket(e.packet, Side.CLIENT);
    }

    @SubscribeEvent
    public void onServerPacketReceived(ServerCustomPacketEvent e){
        readPacket(e.packet, Side.SERVER);
    }

    public static void sendToAll(IPacket packet){
        FMLEmbeddedChannel channel = instance.channels.get(Side.SERVER);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.ALL);
        channel.writeAndFlush(instance.writePacket(packet)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }*/

    public static void sendToPlayer(EntityPlayer player, IPacket packet){
        FMLEmbeddedChannel channel = instance.channels.get(Side.SERVER);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
        channel.writeAndFlush(instance.writePacket(packet)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public static void sendToAllAround(int dimension, double x, double y, double z, double range, IPacket packet){
        FMLEmbeddedChannel channel = instance.channels.get(Side.SERVER);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(new NetworkRegistry.TargetPoint(dimension, x, y, z, range));
        channel.writeAndFlush(instance.writePacket(packet)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public static void sendToAllAround(int dimension, Pos pos, double range, IPacket packet){
        FMLEmbeddedChannel channel = instance.channels.get(Side.SERVER);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(new NetworkRegistry.TargetPoint(dimension, pos.getX()+0.5D, pos.getY()+0.5D, pos.getZ()+0.5D, range));
        channel.writeAndFlush(instance.writePacket(packet)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public static void sendToAllAround(Entity entity, double range, IPacket packet){
        FMLEmbeddedChannel channel = instance.channels.get(Side.SERVER);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(new NetworkRegistry.TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, range));
        channel.writeAndFlush(instance.writePacket(packet)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }/*

    public static void sendToAllAround(TileEntity tile, double range, IPacket packet){
        FMLEmbeddedChannel channel = instance.channels.get(Side.SERVER);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.ALLAROUNDPOINT);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(new TargetPoint(tile.getWorldObj().provider.dimensionId, tile.xCoord+0.5D, tile.yCoord+0.5D, tile.zCoord+0.5D, range));
        channel.writeAndFlush(instance.writePacket(packet)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public static void sendToDimension(int dimension, IPacket packet){
        FMLEmbeddedChannel channel = instance.channels.get(Side.SERVER);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.DIMENSION);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(dimension);
        channel.writeAndFlush(instance.writePacket(packet)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public static void sendToServer(IPacket packet){
        FMLEmbeddedChannel channel = instance.channels.get(Side.CLIENT);
        channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(OutboundTarget.TOSERVER);
        channel.writeAndFlush(instance.writePacket(packet)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }*/
}

package HardCoreEnd.entity.dragon.managers;


import HardCoreEnd.Main;
import HardCoreEnd.entity.EntityBossDragon;
import HardCoreEnd.util.EntitySelector;
import HardCoreEnd.util.GameRegistryUtil;
import HardCoreEnd.random.Stopwatch;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jline.utils.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DragonChunkManager implements LoadingCallback {
    private static final DragonChunkManager instance = new DragonChunkManager();
    private Ticket ticket;
    private int prevChunkX = Integer.MAX_VALUE, prevChunkZ = Integer.MAX_VALUE;
    private int timer;

    public static void register() {
        GameRegistryUtil.registerEventHandler(instance);
        ForgeChunkManager.setForcedChunkLoadingCallback(Main.instance, instance);
    }

    public static void ping(EntityBossDragon dragon) {
        Ticket ticket = instance.ticket;

        if (ticket == null) {
            ticket = instance.ticket = ForgeChunkManager.requestTicket(Main.instance, dragon.worldObj, Type.ENTITY);
            Log.debug("Requested chunkloading ticket on dragon load.");

            if (ticket != null) {
                ticket.bindEntity(dragon);
                ticket.setChunkListDepth(9);
            }
        }

        if (ticket == null || --instance.timer >= 0) return;

        instance.timer = 4;

        if (dragon.chunkCoordX == instance.prevChunkX && dragon.chunkCoordZ == instance.prevChunkZ) return;

        instance.prevChunkX = dragon.chunkCoordX;
        instance.prevChunkZ = dragon.chunkCoordZ;

        Stopwatch.timeAverage("DragonChunkManager - ping update", 10);

        Set<ChunkPos> oldChunks = ticket.getChunkList();
        Set<ChunkPos> updatedChunks = new HashSet<>();

        for (int xx = dragon.chunkCoordX - 1; xx <= dragon.chunkCoordX + 1; xx++) {
            for (int zz = dragon.chunkCoordZ - 1; zz <= dragon.chunkCoordZ + 1; zz++) {
                updatedChunks.add(new ChunkPos(xx, zz));
            }
        }

        Set<ChunkPos> toLoad = new HashSet<>();
        Set<ChunkPos> toUnload = new HashSet<>();

        for (ChunkPos pair : updatedChunks) {
            if (!oldChunks.contains(pair)) toLoad.add(pair);
        }

        for (ChunkPos pair : oldChunks) {
            if (!updatedChunks.contains(pair)) toUnload.add(pair);
        }

        for (ChunkPos unload : toUnload) ForgeChunkManager.unforceChunk(ticket, unload);
        for (ChunkPos load : toLoad) ForgeChunkManager.forceChunk(ticket, load);

        Stopwatch.finish("DragonChunkManager - ping update");
    }

    public static void release(EntityBossDragon dragon) {
        if (instance.ticket == null) return;

        //SaveData.<DragonFile>global(DragonFile.class).setLastDragonChunk(dragon.chunkCoordX, dragon.chunkCoordZ);
        ForgeChunkManager.releaseTicket(instance.ticket);
        instance.ticket = null;
        instance.clear();
        Log.debug("Dragon requested releasing the chunkloading ticket.");
    }

    @Override
    public void ticketsLoaded(List<Ticket> tickets, World world) {
        Log.debug("Loaded dragon chunkloading tickets ($0).", tickets.size());

        if (!tickets.isEmpty()) {
            ticket = tickets.get(0);

            if (ticket.getType() != Type.ENTITY || !(ticket.getEntity() instanceof EntityBossDragon)) {
                Log.debug("Canceled loaded ticket (invalid).");
                ticket = null;
            } else {
                clear();
                ping((EntityBossDragon) ticket.getEntity());
            }
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load e) {
        if (!e.getWorld().isRemote && e.getWorld().provider.getDimension() == 1) {
            //DragonFile file = SaveData.global(DragonFile.class);
            // TODO if (file.isDragonDead())return;

            //ChunkPos chunk = file.getLastDragonChunk();
            //e.getWorld().getChunkFromChunkCoords(chunk.chunkXPos, chunk.chunkZPos);

            //int xx = chunk.chunkXPos * 16, zz = chunk.chunkZPos * 16;
            int xx = 0;
            int zz = 0;
            List<EntityBossDragon> list = EntitySelector.type(e.getWorld(), EntityBossDragon.class, new AxisAlignedBB(xx, -32, zz, xx + 16, 512, zz + 16));

            if (!list.isEmpty()) {
                Log.debug("Loading dragon based on last stored chunk.");
                clear();
                ping(list.get(0));
            }
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload e) {
        if (ticket != null && !e.getWorld().isRemote && e.getWorld().provider.getDimension() == 1) {
            ticket = null;
            Log.debug("World unloaded, dereferencing dragon chunkloading ticket.");
        }
    }

    private void clear() {
        timer = 0;
        prevChunkX = prevChunkZ = Integer.MAX_VALUE;
    }
}

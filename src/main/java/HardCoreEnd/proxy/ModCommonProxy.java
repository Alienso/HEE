package HardCoreEnd.proxy;


import net.minecraft.entity.player.EntityPlayer;

public class ModCommonProxy{
    public static boolean opMobs, hardcoreEnderbacon = false;
    public static int renderIdObsidianSpecial, renderIdFlowerPot, renderIdSpookyLeaves, renderIdCrossedDecoration, renderIdRavishBell, renderIdLootChest, renderIdGloomtorch;

    public EntityPlayer getClientSidePlayer(){
        return null;
    }

    public void registerRenderers(){}
    public void registerSidedEvents(){}
    public void sendMessage(MessageType msgType, int...data){}

    public static enum MessageType{
        DEBUG_TITLE_SET, SPEED_UP_PLAYER, VIEW_MOD_CONTENT
    }
}
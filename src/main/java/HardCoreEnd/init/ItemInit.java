package HardCoreEnd.init;

import HardCoreEnd.objects.items.CustomMusicDisc;
import HardCoreEnd.objects.items.ItemBase;
import HardCoreEnd.objects.items.ItemPortalToken;
import HardCoreEnd.util.handlers.SoundsHandler;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;

public class ItemInit {
    public static final List<Item> ITEMS = new ArrayList<Item>();

    public static final Item PortalToken = new ItemPortalToken("item_portal_token");

    public static final Item DragonDisc = new CustomMusicDisc("music_dragon", SoundsHandler.MUSIC_DRAGON);
    public static final Item EndDisc = new CustomMusicDisc("music_end",SoundsHandler.MUSIC_END);
}

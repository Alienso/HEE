package HardCoreEnd.entity.dragon.managers;

import HardCoreEnd.entity.EntityBossDragon;
import HardCoreEnd.init.ItemInit;
import HardCoreEnd.util.EntitySelector;
import HardCoreEnd.util.MathUtil;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DragonRewardManager {
    private static byte[] difficultyHandicap = new byte[]{7, 20, 70, 100};

    private final EntityBossDragon dragon;
    private final List<UUID> deadPlayers = new ArrayList<>();
    private int[] difficultyTimer = new int[4];
    private float extraHandicap;
    private int finalDifficulty = -1;

    private int difficultyCooldown = 120;
    private boolean deadPlayerCheck;

    public DragonRewardManager(EntityBossDragon dragon) {
        this.dragon = dragon;
        extraHandicap = 0F;
    }

    public void addHandicap(float v, boolean playerAffect) {
        if (playerAffect) extraHandicap += v / Math.max(1F, dragon.worldObj.playerEntities.size() / 1.5F);
        else extraHandicap += v;
    }

    public float getExtraHandicap() {
        return extraHandicap;
    }

    private int getEssencePerTick() {
        return 1 + MathUtil.floor(getFinalDifficulty() / 6F);
    }

    public void updateManager() {
        byte diff = (byte) dragon.worldObj.getDifficulty().getDifficultyId();

        if ((deadPlayerCheck = deadPlayerCheck ^ true) == true) {
            for (EntityPlayer p : EntitySelector.players(dragon.worldObj)) {
                boolean wasDead = deadPlayers.contains(p.getPersistentID());

                if (p.isDead && !wasDead) {
                    float v;
                    switch (diff) {
                        case 3:
                            v = 2.9F;
                            break;
                        case 2:
                            v = 3.5F;
                            break;
                        case 1:
                            v = 3.8F;
                            break;
                        default:
                            v = 4F;
                    }

                    addHandicap(v, true);
                    deadPlayers.add(p.getPersistentID());
                } else if (!p.isDead && wasDead) deadPlayers.remove(p.getPersistentID());
            }
        }

        if (--difficultyCooldown > 0 || diff < 0 || diff > 3) return;
        difficultyCooldown = 80;
        difficultyTimer[diff] += dragon.angryStatus ? 2 : 1;
    }

    public int getFinalDifficultyRaw() {
        return getFinalDifficulty() + MathUtil.ceil(extraHandicap);
    }

    public int getFinalDifficulty() {
        if (finalDifficulty >= 0) return finalDifficulty;

        int total = 0, finalDiff = 0;
        for (int i : difficultyTimer) total += i;
        if (total == 0) return finalDifficulty = 0;

        for (byte a = 0; a < difficultyHandicap.length; a++) {
            finalDiff += MathUtil.floor(difficultyHandicap[a] * ((float) difficultyTimer[a] / (float) total));
        }

        return finalDifficulty = finalDiff -= Math.ceil(extraHandicap);
    }

    public void spawnEssence(World world, int x, int z) {
        float len = Math.min(44F, (float) (Math.abs(world.rand.nextGaussian()) * 24D));
        double ang = world.rand.nextDouble() * Math.PI * 2D;
        double fx = x + Math.cos(ang) * len,
                fz = z + Math.sin(ang) * len;
        EntityItem item = new EntityItem(world, fx + world.rand.nextDouble() - 0.5D, 128, fz + world.rand.nextDouble() - 0.5D, new ItemStack(Items.DIAMOND, getEssencePerTick(), 0));
        world.spawnEntity(item);
    }

    public void spawnPortalToken(World world, int x, int z) {
        float len = Math.min(44F, (float) (Math.abs(world.rand.nextGaussian()) * 24D));
        double ang = world.rand.nextDouble() * Math.PI * 2D;
        double fx = x + Math.cos(ang) * len,
                fz = z + Math.sin(ang) * len;
        EntityItem item = new EntityItem(world, fx + world.rand.nextDouble() - 0.5D, 128, fz + world.rand.nextDouble() - 0.5D, new ItemStack(ItemInit.PortalToken, 2, 0));
        world.spawnEntity(item);
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setFloat("ehc", extraHandicap);
        for (int a = 0; a < difficultyTimer.length; a++) tag.setInteger("dt" + a, difficultyTimer[a]);
        return tag;
    }

    public void readFromNBT(NBTTagCompound tag) {
        extraHandicap = tag.getFloat("ehc");
        for (byte a = 0; a < difficultyHandicap.length; a++) difficultyTimer[a] = tag.getInteger("dt" + a);
    }
}

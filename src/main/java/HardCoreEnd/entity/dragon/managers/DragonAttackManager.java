package HardCoreEnd.entity.dragon.managers;

import HardCoreEnd.collections.WeightedList;
import HardCoreEnd.collections.WeightedMap;
import HardCoreEnd.entity.EntityBossDragon;
import HardCoreEnd.entity.dragon.attacks.passive.DragonPassiveAttackBase;
import HardCoreEnd.entity.dragon.attacks.special.DragonSpecialAttackBase;
import HardCoreEnd.proxy.CommonProxy;
import HardCoreEnd.util.EntitySelector;
import HardCoreEnd.util.MathUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

public class DragonAttackManager {
    private final List<DragonPassiveAttackBase> passiveAttackList = new ArrayList<>();
    private final List<DragonSpecialAttackBase> specialAttackList = new ArrayList<>();
    private final WeightedList<DragonSpecialAttackBase> specialAttackWeights = new WeightedList<>();
    private final Queue<Byte> specialAttackQueue = new LinkedList<>();

    protected EntityBossDragon dragon;

    public DragonAttackManager(EntityBossDragon dragon) {
        this.dragon = dragon;
    }

    public void registerPassive(DragonPassiveAttackBase attack) {
        if (getPassiveAttackById(attack.id) != null)
            throw new IllegalArgumentException("Tried to register passive dragon attack with already registered attack ID " + attack.id);
        passiveAttackList.add(attack);
    }

    public void registerSpecial(DragonSpecialAttackBase attack) {
        if (getSpecialAttackById(attack.id) != null)
            throw new IllegalArgumentException("Tried to register special dragon attack with already registered attack ID " + attack.id);
        specialAttackList.add(attack);
        if (attack.getWeight() != -1) specialAttackWeights.add(attack);
    }

    public DragonPassiveAttackBase getPassiveAttackById(int id) {
        for (DragonPassiveAttackBase attack : passiveAttackList) {
            if (attack.id == id) return attack;
        }

        return null;
    }

    public DragonSpecialAttackBase getSpecialAttackById(int id) {
        for (DragonSpecialAttackBase attack : specialAttackList) {
            if (attack.id == id) return attack;
        }

        return null;
    }

    public List<DragonSpecialAttackBase> getSpecialAttackList() {
        return Collections.unmodifiableList(specialAttackList);
    }

    public boolean isPlayerViable(EntityPlayer player) {
        return MathUtil.distance(player.posX, player.posZ) <= 160D;
    }

    public List<EntityPlayer> getViablePlayers() {
        List<EntityPlayer> players = EntitySelector.players(dragon.worldObj, new AxisAlignedBB(-160D, -32D, -160D, 160D, 512D, 160D));

        if (players.size() > 1) {
            for (Iterator<EntityPlayer> iter = players.iterator(); iter.hasNext(); ) {
                EntityPlayer player = iter.next();
                if (player.isDead) iter.remove();
                //if (player.isCreative() || player.isDead) iter.remove();
            }
        }

        return players;
    }

    public EntityPlayer getRandomPlayer() {
        List<EntityPlayer> list = getViablePlayers();
        return list.isEmpty() ? null : list.get(dragon.worldObj.rand.nextInt(list.size()));
    }

    public EntityPlayer getWeakPlayer() {
        List<EntityPlayer> list = getViablePlayers();

        if (list.isEmpty()) return null;
        else if (list.size() == 1) return list.get(0);

        WeightedMap<EntityPlayer> players = new WeightedMap<>(list.size());
        for (EntityPlayer player : list)
            players.add(player, 5 + ((int) player.getHealth() >> 1) + (player.getTotalArmorValue() >> 2));
        return players.getRandomItem(dragon.worldObj.rand);
    }

    public void updatePassiveAttacks(DragonSpecialAttackBase currentSpecialAttack) {
        for (DragonPassiveAttackBase attack : passiveAttackList) {
            if (currentSpecialAttack != null && currentSpecialAttack.isPassiveAttackDisabled(attack.id)) continue;
            attack.update();
        }
    }

    public DragonSpecialAttackBase pickSpecialAttack(DragonSpecialAttackBase lastAttack) {
        int healthPercentage = getHealthPercentage();
        if (healthPercentage == 0) return null;

        if (specialAttackQueue.isEmpty()) {
            WeightedList<DragonSpecialAttackBase> list = new WeightedList<>(specialAttackWeights);

            for (int a = 0, amt = list.size() - 2; a < amt; a++) {
                DragonSpecialAttackBase attack = list.getRandomItem(dragon.worldObj.rand);
                list.remove(attack);
                specialAttackQueue.add(Byte.valueOf(attack.id));
            }
        }

        return getSpecialAttackById(specialAttackQueue.poll());
    }

    public boolean biteClosePlayers() {
        boolean res = false;

        for (EntityPlayer player : EntitySelector.players(dragon.worldObj, dragon.dragonPartHead.getEntityBoundingBox().expand(2.2D, 1.5D, 2.2D))) {
            int diff = dragon.worldObj.getDifficulty().getDifficultyId(), rm;
            player.attackEntityFrom(DamageSource.causeMobDamage(dragon), (9F) + diff);

            switch (diff) {
                case 3:
                    rm = 31;
                    break;
                case 2:
                    rm = 20;
                    break;
                case 1:
                    rm = 14;
                    break;
                default:
                    rm = 9;
            }

            if (dragon.worldObj.rand.nextInt(100) < rm) {
                player.addPotionEffect(new PotionEffect(MobEffects.POISON, 90 + 30 * diff, CommonProxy.opMobs ? 1 : 0));
                dragon.rewards.addHandicap(0.1F, false);

                if (dragon.worldObj.rand.nextInt(100) < 35 + diff * 12) {
                    player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 160 + 24 * diff, 0));
                    player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 80 + 24 * diff, 0));
                }
            }

            res = true;
        }

        return res;
    }

    public int getHealthPercentage() {
        return (int) ((100F / dragon.getMaxHealth()) * dragon.getHealth());
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByteArray("attq", ArrayUtils.toPrimitive(specialAttackQueue.toArray(new Byte[specialAttackQueue.size()])));
        return tag;
    }

    public void readFromNBT(NBTTagCompound tag) {
        for (byte b : tag.getByteArray("attq")) specialAttackQueue.add(Byte.valueOf(b));
    }
}
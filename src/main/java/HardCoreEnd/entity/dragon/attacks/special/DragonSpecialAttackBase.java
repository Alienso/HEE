package HardCoreEnd.entity.dragon.attacks.special;

import HardCoreEnd.collections.IWeightProvider;
import HardCoreEnd.entity.EntityBossDragon;
import HardCoreEnd.entity.dragon.attacks.special.event.*;
import HardCoreEnd.util.EntitySelector;
import gnu.trove.map.hash.TObjectFloatHashMap;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Random;
import java.util.UUID;

public abstract class DragonSpecialAttackBase implements IWeightProvider {
    public final byte id;
    private final short weight;
    protected EntityBossDragon dragon;
    protected Random rand;
    protected float damageTaken;
    protected float damageDealt;
    protected TObjectFloatHashMap<UUID> lastPlayerHealth = new TObjectFloatHashMap<>();
    protected int tick;
    protected byte phase;
    private byte[] disabledPassiveAttacks = ArrayUtils.EMPTY_BYTE_ARRAY;

    public DragonSpecialAttackBase(EntityBossDragon dragon, int attackId, int weight) {
        this.dragon = dragon;
        this.rand = dragon.worldObj.rand;
        this.id = (byte) attackId;
        this.weight = (short) weight;
    }

    public DragonSpecialAttackBase setDisabledPassiveAttacks(byte... attackIds) {
        disabledPassiveAttacks = attackIds;
        return this;
    }

    public boolean isPassiveAttackDisabled(byte attackId) {
        return ArrayUtils.contains(disabledPassiveAttacks, attackId);
    }

    public void init() {
        tick = 0;
        phase = 0;
        damageTaken = 0;
        damageDealt = 0;
        lastPlayerHealth.clear();
        updatePlayerHealth();
    }

    public void update() {
        tick++;
        updatePlayerHealth();
    }

    public void end() {
    }

    public boolean canStart() {
        return true;
    }

    protected void updatePlayerHealth() {
        for (EntityPlayer player : EntitySelector.players(dragon.worldObj)) {
            UUID id = player.getUniqueID();

            if (lastPlayerHealth.containsKey(id)) {
                float last = lastPlayerHealth.get(id);
                if (player.getHealth() < last) damageDealt += (last - player.getHealth());
            }

            lastPlayerHealth.put(id, player.getHealth());
        }
    }

    public final void onDamageTaken(float damage) {
        damageTaken += damage;
    }

    public abstract boolean hasEnded();

    public int getNextAttackTimer() {
        return Math.max(140, 220 + rand.nextInt(140) + ((4 - getDifficulty()) * 30) - Math.min(60, dragon.worldObj.playerEntities.size() * 10));
    }

    public float overrideMovementSpeed() {
        return 1F;
    }

    public float overrideWingSpeed() {
        return 1F;
    }

    public void onDamageTakenEvent(DamageTakenEvent event) {
    }

    public void onMotionUpdateEvent(MotionUpdateEvent event) {
    }

    public void onTargetSetEvent(TargetSetEvent event) {
    }

    public void onTargetPositionSetEvent(TargetPositionSetEvent event) {
    }

    public void onCollisionEvent(CollisionEvent event) {
    }

    protected final int getDifficulty() {
        return dragon.worldObj.getDifficulty().getDifficultyId();
    }

    @Override
    public final int getWeight() {
        return weight;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DragonSpecialAttackBase && ((DragonSpecialAttackBase) o).id == id;
    }

    public boolean equals(DragonSpecialAttackBase attack) {
        return attack != null && attack.id == this.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
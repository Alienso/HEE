package HardCoreEnd.entity.dragon.attacks.passive;

import HardCoreEnd.entity.EntityBossDragon;

public abstract class DragonPassiveAttackBase {
    public final byte id;
    protected EntityBossDragon dragon;

    public DragonPassiveAttackBase(EntityBossDragon dragon, int attackId) {
        this.dragon = dragon;
        this.id = (byte) attackId;
    }

    public abstract void update();
}

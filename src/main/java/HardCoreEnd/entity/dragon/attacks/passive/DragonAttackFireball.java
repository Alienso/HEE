package HardCoreEnd.entity.dragon.attacks.passive;


import HardCoreEnd.entity.EntityBossDragon;
import HardCoreEnd.entity.dragon.managers.DragonShotManager;
import HardCoreEnd.proxy.CommonProxy;

public class DragonAttackFireball extends DragonPassiveAttackBase {
    private int timer;

    public DragonAttackFireball(EntityBossDragon dragon, int attackId) {
        super(dragon, attackId);
    }

    @Override
    public void update() {
        if (dragon.target == null) timer = 8;
        else if (++timer >= (24 - dragon.world.getDifficulty().getDifficultyId() * 4) + (dragon.angryStatus ? 5 : 20) - (CommonProxy.opMobs ? 5 : 0) - ((100 - dragon.attacks.getHealthPercentage()) >> 4)) {
            if (dragon.target.getDistanceSq(dragon.dragonPartHead) > 400D) {
                dragon.shots.createNew(DragonShotManager.ShotType.FIREBALL).setTarget(dragon.target).shoot();
                timer = 0;
            } else --timer;
        }
    }
}

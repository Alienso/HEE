package HardCoreEnd.entity.dragon.attacks.special;

import HardCoreEnd.entity.EntityBossDragon;
import HardCoreEnd.entity.dragon.attacks.special.event.TargetPositionSetEvent;
import HardCoreEnd.entity.dragon.attacks.special.event.TargetSetEvent;
import HardCoreEnd.entity.dragon.managers.DragonShotManager;
import HardCoreEnd.proxy.CommonProxy;
import HardCoreEnd.util.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class DragonAttackFireburst extends DragonSpecialAttackBase {
    private Entity target;
    private byte shootTimer;
    private byte shotAmount;
    private byte runCounter;
    private byte waitTimer;
    private boolean ended;

    public DragonAttackFireburst(EntityBossDragon dragon, int attackId, int weight) {
        super(dragon, attackId, weight);
    }

    @Override
    public void init() {
        super.init();
        target = null;
        shootTimer = shotAmount = runCounter = waitTimer = 0;
        ended = false;
        dragon.target = null;
    }

    @Override
    public void update() {
        super.update();

        if (target == null || target.isDead) {
            if (waitTimer <= 0 || --waitTimer <= 0) {
                if ((target = dragon.attacks.getRandomPlayer()) == null) {
                    ended = true;
                } else {
                    if (MathUtil.distance(dragon.targetX - dragon.posX, dragon.targetZ - dragon.posZ) < 60D) {
                        target = null;
                        waitTimer = 60;

                        double dist = 10D;
                        Vec3d vec = new Vec3d(dragon.motionX, 0D, dragon.motionZ).normalize();

                        for (int attempt = 0; attempt < 10; attempt++) {
                            dragon.targetX = dragon.posX + vec.x * dist + (rand.nextDouble() - 0.5D) * 4D;
                            dragon.targetZ = dragon.posZ + vec.z * dist + (rand.nextDouble() - 0.5D) * 4D;

                            if (MathUtil.distance(dragon.targetX - dragon.posX, dragon.targetZ - dragon.posZ) > 65D)
                                break;
                            else dist += 5D;
                        }
                    } else waitTimer = 8;
                }
            }
        } else {
            dragon.targetX = target.posX;
            dragon.targetY = target.posY + 10D;
            dragon.targetZ = target.posZ;

            double dist = MathUtil.distance(dragon.targetX - dragon.posX, dragon.targetZ - dragon.posZ);
            boolean stopShooting = false;

            if (dist < 90D && (waitTimer <= 0 || --waitTimer <= 0)) {
                if (dist < 30D) stopShooting = true;
                else if (++shootTimer > 13 - getDifficulty() * 2 - (CommonProxy.opMobs ? 3 : 0)) {
                    dragon.shots.createNew(DragonShotManager.ShotType.FIREBALL).setTarget(target).setRandom().shoot();
                    shootTimer = 0;

                    if (++shotAmount > 7 + rand.nextInt(6) + getDifficulty()) stopShooting = true;
                }
            }

            if (stopShooting) {
                waitTimer = 110;
                shootTimer = shotAmount = 0;
                target = null;

                if (++runCounter > 3 + Math.min(4, dragon.attacks.getViablePlayers().size())) {
                    ended = true;
                }
            }
        }
    }

    @Override
    public boolean hasEnded() {
        return ended;
    }

    @Override
    public void onTargetSetEvent(TargetSetEvent event) {
        event.newTarget = null;
    }

    @Override
    public void onTargetPositionSetEvent(TargetPositionSetEvent event) {
        if (target != null) event.cancel();
    }
}
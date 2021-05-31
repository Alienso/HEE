package HardCoreEnd.entity.dragon.attacks.special;

import HardCoreEnd.entity.EntityBossDragon;
import HardCoreEnd.entity.EntityMobAngryEnderman;
import HardCoreEnd.entity.EntityWeatherLightningBoltSafe;
import HardCoreEnd.entity.dragon.DragonUtil;
import HardCoreEnd.entity.dragon.attacks.special.event.DamageTakenEvent;
import HardCoreEnd.entity.dragon.attacks.special.event.TargetSetEvent;
import HardCoreEnd.proxy.CommonProxy;
import HardCoreEnd.proxy.ModCommonProxy;
import HardCoreEnd.util.MathUtil;
import HardCoreEnd.random.Pos;
import gnu.trove.map.hash.TObjectByteHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.List;
import java.util.UUID;

public class DragonAttackSummoning extends DragonSpecialAttackBase {
    private final TObjectByteHashMap<UUID> lastStriked = new TObjectByteHashMap<>(6);
    private byte summonTimer;
    private byte summoned;
    private int totalTimer;
    private boolean ended;

    public DragonAttackSummoning(EntityBossDragon dragon, int attackId, int weight) {
        super(dragon, attackId, weight);
    }

    @Override
    public void init() {
        super.init();
        lastStriked.clear();
        summonTimer = summoned = 0;
        totalTimer = 1200;
        ended = false;
        dragon.target = null;
    }

    @Override
    public void update() {
        super.update();

        List<EntityPlayer> viablePlayers = dragon.attacks.getViablePlayers();

        if (++summonTimer > 35 - Math.min(viablePlayers.size() * 4 + (CommonProxy.opMobs ? 5 : 0), 20)) {
            summonTimer = 0;
            boolean didSummon = false;

            for (int amt = MathUtil.clamp(MathUtil.ceil(viablePlayers.size() * (0.2D + rand.nextDouble() * 0.25D)), 1, viablePlayers.size()), aggro = 0, total = 0; amt > 0; amt--) {
                EntityPlayer player = viablePlayers.remove(rand.nextInt(viablePlayers.size()));

				for(EntityMobAngryEnderman enderman:(List<EntityMobAngryEnderman>)dragon.worldObj.getEntitiesWithinAABB(EntityMobAngryEnderman.class, player.getEntityBoundingBox().expand(14D, 5D, 14D))){
					if (enderman.getTarget() == player)++aggro;
					++total;
				}

                if (aggro < getDifficulty() && total < 6 + getDifficulty()) {
                    Pos playerPos = Pos.at(player);
                    try {
                        boolean flying = !Pos.allBlocksMatch(playerPos, playerPos.offset(-5), pos -> pos.isAir(dragon.worldObj));

                    if (flying) {
                        if (lastStriked.adjustOrPutValue(player.getPersistentID(), (byte) -1, (byte) 0) <= 0) {
                            // TODO MultiDamage.from(dragon).addMagic(2F).addUnscaled(11F).attack(player);
                            player.setFire(5);

                            dragon.worldObj.addWeatherEffect(new EntityWeatherLightningBoltSafe(dragon.worldObj, player.posX, player.posY, player.posZ));
                            lastStriked.put(player.getPersistentID(), (byte) (4 + rand.nextInt(3)));
                        }

                        continue;
                    }
                    }catch (Exception e){e.printStackTrace();}

                    for (int a = 0; a < 3 + rand.nextInt(getDifficulty()); a++) {
						double x = player.posX+(rand.nextDouble()-0.5D)*13D, z = player.posZ+(rand.nextDouble()-0.5D)*13D;
						int y = 1+ DragonUtil.getTopBlockY(dragon.worldObj, Blocks.END_STONE, MathUtil.floor(x), MathUtil.floor(z), MathUtil.floor(player.posY+8));
						
						EntityMobAngryEnderman enderman = new EntityMobAngryEnderman(dragon.worldObj);
						enderman.setPosition(x, y, z);
						
						if ((getDifficulty() > 1 || ModCommonProxy.opMobs) && rand.nextInt(100) < 5+getDifficulty()*10+(ModCommonProxy.opMobs ? 25 : 0)){
							enderman.addPotionEffect(new PotionEffect(MobEffects.STRENGTH,500));
						}
						
						dragon.worldObj.addWeatherEffect(new EntityWeatherLightningBoltSafe(dragon.worldObj, x, y, z));
						dragon.worldObj.spawnEntity(enderman);
                    }

                    didSummon = true;
                }
            }

            if (didSummon && ++summoned > 2 + getDifficulty() + (CommonProxy.opMobs ? 1 : 0)) ended = true;
        }

        if (--totalTimer < 0) ended = true;

        if (dragon.ticksExisted % 10 == 0) {
            if (MathUtil.distance(dragon.posX, dragon.posZ) > 100D) {
                dragon.targetX = (rand.nextDouble() - 0.5D) * 60;
                dragon.targetZ = (rand.nextDouble() - 0.5D) * 60;
            }
        }
    }

    @Override
    public boolean canStart() {
        return getDifficulty() > 0;
    }

    @Override
    public boolean hasEnded() {
        return ended;
    }

    @Override
    public int getNextAttackTimer() {
        return super.getNextAttackTimer() + 100;
    }

    @Override
    public float overrideMovementSpeed() {
        return 0.7F;
    }

    @Override
    public void onDamageTakenEvent(DamageTakenEvent event) {
        totalTimer -= 40;
    }

    @Override
    public void onTargetSetEvent(TargetSetEvent event) {
        event.newTarget = null;
    }
}
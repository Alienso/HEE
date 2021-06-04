package HardCoreEnd.entity;

import HardCoreEnd.entity.dragon.DragonUtil;
import HardCoreEnd.entity.dragon.attacks.passive.DragonAttackBite;
import HardCoreEnd.entity.dragon.attacks.passive.DragonAttackFireball;
import HardCoreEnd.entity.dragon.attacks.special.*;
import HardCoreEnd.entity.dragon.attacks.special.event.*;
import HardCoreEnd.entity.dragon.managers.DragonAttackManager;
import HardCoreEnd.entity.dragon.managers.DragonChunkManager;
import HardCoreEnd.entity.dragon.managers.DragonRewardManager;
import HardCoreEnd.entity.dragon.managers.DragonShotManager;
import HardCoreEnd.init.BlockInit;
import HardCoreEnd.network.C06SetPlayerVelocity;
import HardCoreEnd.network.PacketPipeline;
import HardCoreEnd.proxy.CommonProxy;
import HardCoreEnd.sound.EndMusicType;
import HardCoreEnd.sound.MusicManager;
import HardCoreEnd.util.EntityAttributes;
import HardCoreEnd.util.EntityDataWatcher;
import HardCoreEnd.util.EntitySelector;
import HardCoreEnd.util.MathUtil;
import HardCoreEnd.util.handlers.SoundsHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.layers.LayerEnderDragonDeath;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.*;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.*;
import net.minecraft.world.end.DragonFightManager;

import java.util.List;

public class EntityBossDragon extends EntityDragon implements IEntityMultiPart, IMob {

    private final DragonFightManager fightManager;
    public World worldObj = getWorld();
    public BossInfoServer bossInfo = (BossInfoServer)(new BossInfoServer(this.getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS)).setDarkenSky(true);;

    public static final byte ATTACK_FIREBALL = 0, ATTACK_BITE = 1;
    public static long lastUpdate;


    private double[][] movementBuffer = new double[64][2];
    private int movementBufferIndex = -1;

    public MultiPartEntityPart[] dragonPartArray;
    public MultiPartEntityPart dragonPartHead;
    public MultiPartEntityPart dragonPartNeck;
    public MultiPartEntityPart dragonPartBody;
    public MultiPartEntityPart dragonPartTail1;
    public MultiPartEntityPart dragonPartTail2;
    public MultiPartEntityPart dragonPartTail3;
    public MultiPartEntityPart dragonPartWing1;
    public MultiPartEntityPart dragonPartWing2;

    public EntityEnderCrystal healingEnderCrystal;

    public SoundEvent soundWings = new SoundEvent(new ResourceLocation("mob.enderdragon.wings"));

    public float prevAnimTime;
    public float animTime;

    public boolean forceNewTarget;
    public boolean slowed;
    public int deathTicks;

    boolean change_lock = false;
    boolean should_change_music = false;

    public float WING_SPEED = 0;
    public boolean ANGRY = false;
    public EntityPlayer target;
    public double targetX, targetY, targetZ;
    public boolean angryStatus, forceAttackEnd, noViablePlayers, freezeAI, frozen;
    public int nextAttackTicks;
    public byte dragonHurtTime;

    public int spawnCooldown = 1200, lastAttackInterruption = -600;
    public byte loadTimer = 10;
    public double moveSpeedMp = 1D;

    public final DragonAttackManager attacks;
    public final DragonShotManager shots;
    public final DragonRewardManager rewards;

    private final DragonSpecialAttackBase defaultAttack;
    private DragonSpecialAttackBase lastAttack, currentAttack;

    public EntityBossDragon(World world){
        super(world);
        bossInfo.setName(new TextComponentString("Ender Dragon"));
        dragonPartHead = new MultiPartEntityPart(this, "head", 6.0F, 6.0F);
        dragonPartNeck = new MultiPartEntityPart(this, "neck", 6.0F, 6.0F);
        dragonPartBody = new MultiPartEntityPart(this, "body", 8.0F, 8.0F);
        dragonPartTail1 = new MultiPartEntityPart(this, "tail", 4.0F, 4.0F);
        dragonPartTail2 = new MultiPartEntityPart(this, "tail", 4.0F, 4.0F);
        dragonPartTail3 = new MultiPartEntityPart(this, "tail", 4.0F, 4.0F);
        dragonPartWing1 = new MultiPartEntityPart(this, "wing", 4.0F, 4.0F);
        dragonPartWing2 = new MultiPartEntityPart(this, "wing", 4.0F, 4.0F);
        dragonPartArray = new MultiPartEntityPart[]{
                dragonPartHead = new MultiPartEntityPart(this, "head", 6F, 6F), dragonPartBody = new MultiPartEntityPart(this, "body", 8F, 8F),
                dragonPartTail1 = new MultiPartEntityPart(this, "tail", 4F, 4F), dragonPartTail2 = new MultiPartEntityPart(this, "tail", 4F, 4F),
                dragonPartTail3 = new MultiPartEntityPart(this, "tail", 4F, 4F), dragonPartWing1 = new MultiPartEntityPart(this, "wing", 4F, 4F),
                dragonPartWing2 = new MultiPartEntityPart(this, "wing", 4F, 4F)
        };
        setHealth(getMaxHealth());
        setSize(16F, 8F);
        noClip = true;
        isImmuneToFire = true;
        targetY = 100D;
        ignoreFrustumCheck = true;
        switchMusic(SoundsHandler.MUSIC_END);

        if (!world.isRemote && world.provider instanceof WorldProviderEnd)
        {
            this.fightManager = ((WorldProviderEnd)world.provider).getDragonFightManager();
        }
        else
        {
            this.fightManager = null;
        }

        attacks = new DragonAttackManager(this);
        shots = new DragonShotManager(this);
        rewards = new DragonRewardManager(this);

        attacks.registerPassive(new DragonAttackFireball(this, ATTACK_FIREBALL));
        attacks.registerPassive(new DragonAttackBite(this, ATTACK_BITE));

        attacks.registerSpecial(defaultAttack = new DragonAttackDefault(this, 0));
        attacks.registerSpecial(new DragonAttackDivebomb(this, 1, 10).setDisabledPassiveAttacks(ATTACK_FIREBALL));
        attacks.registerSpecial(new DragonAttackFireburst(this, 2, 10).setDisabledPassiveAttacks(ATTACK_FIREBALL, ATTACK_BITE));
        attacks.registerSpecial(new DragonAttackPunch(this, 3, 10).setDisabledPassiveAttacks(ATTACK_FIREBALL));
        attacks.registerSpecial(new DragonAttackSummoning(this, 4, 7).setDisabledPassiveAttacks(ATTACK_FIREBALL, ATTACK_BITE));
        attacks.registerSpecial(new DragonAttackBloodlust(this, 5, 7).setDisabledPassiveAttacks(ATTACK_FIREBALL, ATTACK_BITE));
    }

    @Override
    public World getWorld() {
        return this.world;
    }


        @Override
        protected void applyEntityAttributes(){
            super.applyEntityAttributes();
            EntityAttributes.setValue(this, EntityAttributes.maxHealth, 250D + (CommonProxy.opMobs ? 80D : 0D));
        }

        @Override
        protected void entityInit(){
            super.entityInit();
        }

        @Override
        public void onLivingUpdate(){
            if (freezeAI){
                if (ticksExisted%10 == 0 && !attacks.getViablePlayers().isEmpty())freezeAI = noViablePlayers = false;
                else return;
            }
            else{
                if (noViablePlayers && ticksExisted%10 == 0 && !attacks.getViablePlayers().isEmpty())noViablePlayers = false;

                if (ticksExisted%40 == 0 && attacks.getViablePlayers().isEmpty()){
                    noViablePlayers = true;

                    if (world.getClosestPlayerToEntity(this, 180D) == null){
                        freezeAI = true;
                        if (!world.isRemote) DragonChunkManager.release(this);
                        return;
                    }
                    else freezeAI = false;
                }
            }

            if (currentAttack == null)currentAttack = defaultAttack;
            angryStatus = isAngry();

            if (!change_lock && !angryStatus && attacks.getHealthPercentage() <= 80 && world.isRemote) {
                should_change_music = true;
                change_lock = true;
            }
            if (should_change_music){
                should_change_music = false;
                this.switchMusic(SoundsHandler.MUSIC_DRAGON);
            }
            if (!world.isRemote){
                if (spawnCooldown > 0 && --spawnCooldown > 0 && ticksExisted%20 == 0){
                    for(EntityPlayer player:attacks.getViablePlayers()){
                        if (world.getBlockState(player.getPosition().down()).getBlock() == Blocks.END_STONE){
                            spawnCooldown = 0;
                            break;
                        }
                    }
                }

                if (loadTimer > 0 && --loadTimer == 1){
                    for(int chunkX = -6; chunkX <= 6; chunkX++){
                        for(int chunkZ = -6; chunkZ <= 6; chunkZ++)world.getChunkFromChunkCoords(chunkX, chunkZ);
                    }
                }

                if (loadTimer == 0 && !angryStatus && ticksExisted%10 == 0){
                    // TODO DragonFile save = SaveData.global(DragonFile.class);

                    if (attacks.getHealthPercentage() <= 80){
                        setAngry(true);
                        spawnCooldown = 0;
                    }
                    else if (this.fightManager.getNumAliveCrystals()<=2 && ticksExisted>200){
                        setAngry(true);
                        spawnCooldown = 0;
                    }
                }

                currentAttack.update();

                if (angryStatus){

                    if (currentAttack.equals(defaultAttack)){
                        if (nextAttackTicks-- <= 0 && target == null){
                            lastAttack = currentAttack;
                            if ((currentAttack = attacks.pickSpecialAttack(lastAttack)) == null)nextAttackTicks = (currentAttack = defaultAttack).getNextAttackTimer();
                            currentAttack.init();
                        }
                    }
                    else if (currentAttack.hasEnded() || forceAttackEnd){
                        forceAttackEnd = false;
                        currentAttack.end();
                        nextAttackTicks = MathUtil.ceil(currentAttack.getNextAttackTimer()*(0.5D+attacks.getHealthPercentage()/200D));
                        (currentAttack = defaultAttack).init();
                    }
                }

                if (getHealth() > 0) {
                    rewards.updateManager();
                    DragonChunkManager.ping(this);

                    if (dragonHurtTime > 0) --dragonHurtTime;

                    double spd = currentAttack.overrideMovementSpeed();
                    if (moveSpeedMp > spd)
                        moveSpeedMp = moveSpeedMp < 0.2D && spd == 0D ? 0D : Math.max(spd, moveSpeedMp - 0.0175D);
                    else if (moveSpeedMp < spd) moveSpeedMp = Math.min(spd, moveSpeedMp + 0.0175D);

                    float wng = currentAttack.overrideWingSpeed(), curWng = getWingSpeed();
                    if (curWng > wng) curWng = Math.max(wng, curWng - 0.015F);
                    else if (curWng < wng) curWng = Math.min(wng, curWng + 0.015F);

                    if (curWng != getWingSpeed()) setWingSpeed(curWng);

                    if (ticksExisted % 2 == 0) {
                        int perc = attacks.getHealthPercentage();

                        if (perc < 40 && rand.nextInt(500 - (50 - perc) * 8) == 0) {
                            int x = (int)posX+rand.nextInt(301)-150, z = (int)posZ+rand.nextInt(301)-150;
						    int y = 1+DragonUtil.getTopBlockY(worldObj, Blocks.END_STONE, x, z);

						    EntityMobAngryEnderman buddy = new EntityMobAngryEnderman(worldObj);
						    buddy.setPosition(x, y, z);

						    worldObj.addWeatherEffect(new EntityWeatherLightningBoltSafe(worldObj, x, y, z));
						    worldObj.spawnEntity(buddy);
                        }

                        lastUpdate = world.getTotalWorldTime();
                    }
                }
            }

            if (world.isRemote && MathHelper.cos(prevAnimTime*(float)Math.PI*2F) <= -0.3F && MathHelper.cos(animTime*(float)Math.PI*2F) >= -0.3F){
                world.playSound(posX,posY,posZ,soundWings, SoundCategory.HOSTILE,5F, 0.8F+rand.nextFloat()*0.3F, false);
            }

            prevAnimTime = animTime;

            if (getHealth() <= 0F)world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, posX+(rand.nextFloat()-0.5F)*8F, posY+2D+(rand.nextFloat()-0.5F)*4F, posZ+(rand.nextFloat()-0.5F)*8F, 0D, 0D, 0D);
            else{
                updateEnderCrystal();

                float animAdvance = 0.2F/(MathHelper.sqrt(motionX*motionX+motionZ*motionZ)*10F+1F);
                animAdvance *= (float)Math.pow(2D, motionY);
                animAdvance *= getWingSpeed();

                animTime += slowed ? animAdvance*0.5F : animAdvance;

                rotationYaw = MathHelper.wrapDegrees(rotationYaw);

                if (movementBufferIndex < 0){
                    for(int index = 0; index < movementBuffer.length; ++index){
                        movementBuffer[index][0] = rotationYaw;
                        movementBuffer[index][1] = posY;
                    }
                }
                if (++movementBufferIndex == movementBuffer.length)movementBufferIndex = 0;

                movementBuffer[movementBufferIndex][0] = rotationYaw;
                movementBuffer[movementBufferIndex][1] = posY;

                if (world.isRemote){
                    if (newPosRotationIncrements > 0){

                        double d5 = this.posX + (this.interpTargetX - this.posX) / (double)this.newPosRotationIncrements;
                        double d0 = this.posY + (this.interpTargetY - this.posY) / (double)this.newPosRotationIncrements;
                        double d1 = this.posZ + (this.interpTargetZ - this.posZ) / (double)this.newPosRotationIncrements;
                        double d2 = MathHelper.wrapDegrees(this.interpTargetYaw - (double)this.rotationYaw);
                        this.rotationYaw = (float)((double)this.rotationYaw + d2 / (double)this.newPosRotationIncrements);
                        this.rotationPitch = (float)((double)this.rotationPitch + (this.interpTargetPitch - (double)this.rotationPitch) / (double)this.newPosRotationIncrements);
                        --this.newPosRotationIncrements;
                        this.setPosition(d5, d0, d1);
                        this.setRotation(this.rotationYaw, this.rotationPitch);
                    }
                }
                else{
                    double xDiff = targetX-posX, yDiff = targetY-posY, zDiff = targetZ-posZ;
                    double distFromTargetSq = xDiff*xDiff+yDiff*yDiff+zDiff*zDiff;

                    if (noViablePlayers){
                        forceAttackEnd = true;
                        trySetTarget(null);
                        trySetTargetPosition(rand.nextDouble()*60D-30D, targetY, rand.nextDouble()*60D-30D);
                    }

                    if (target != null){
                        targetX = target.posX;
                        targetZ = target.posZ;
                        targetY = target.getEntityBoundingBox().minY+Math.min(0.4D+Math.sqrt(Math.pow(targetX-posX, 2)+Math.pow(targetZ-posZ, 2))/80D-1D, 10D);
                    }
                    else trySetTargetPosition(targetX+rand.nextGaussian()*2D, targetY, targetZ+rand.nextGaussian()*2D);

                    if ((target != null && target.isDead) || distFromTargetSq > 22500D)forceAttackEnd = forceNewTarget = true;

                    if (forceNewTarget || distFromTargetSq < 90D || distFromTargetSq > 22500D || collidedHorizontally || collidedVertically){
                        setNewTarget();
                    }

                    yDiff = MathUtil.clamp(yDiff/MathUtil.distance(xDiff, zDiff), -0.6F, 0.6F);

                    motionY += yDiff*0.1D;
                    rotationYaw = MathHelper.wrapDegrees(rotationYaw);
                    double d9 = MathUtil.clamp(MathHelper.wrapDegrees(180D-MathUtil.toDeg(Math.atan2(xDiff, zDiff))-rotationYaw), -50D, 50D);

                    Vec3d targetDiffVec3d = new Vec3d(targetX-posX, targetY-posY, targetZ-posZ).normalize();
                    Vec3d rotationVec3d = new Vec3d(MathHelper.sin(MathUtil.toRad(rotationYaw)), motionY, (-MathHelper.cos(MathUtil.toRad(rotationYaw)))).normalize();

                    float f4 = Math.max((float)(rotationVec3d.dotProduct(targetDiffVec3d)+0.5D)/1.5F, 0F);

                    randomYawVelocity *= 0.8F;
                    float speed = MathHelper.sqrt(motionX*motionX+motionZ*motionZ)+1F;
                    double speedLimited = Math.min(Math.sqrt(motionX*motionX+motionZ*motionZ)+1D, 40D);

                    randomYawVelocity = (float)(randomYawVelocity+d9*(0.7D/speedLimited/speed));
                    rotationYaw += randomYawVelocity*0.1F;
                    float f6 = (float)(2D/(speedLimited+1D));
                    this.moveRelative(0,0, -1F, 0.06F*(f4*f6+(1F-f6)));

                    if (frozen)motionX = motionY = motionZ = 0D;
                    MotionUpdateEvent event = new MotionUpdateEvent(motionX, motionY, motionZ);
                    currentAttack.onMotionUpdateEvent(event);
                    motionX = event.motionX;
                    motionY = event.motionY;
                    motionZ = event.motionZ;

                    if (slowed)move(MoverType.SELF,motionX*moveSpeedMp*0.8D, motionY*moveSpeedMp*0.8D, motionZ*moveSpeedMp*0.8D);
                    else move(MoverType.SELF,motionX*moveSpeedMp, motionY*moveSpeedMp, motionZ*moveSpeedMp);

                    double motionMultiplier = 0.8D+0.15D*((new Vec3d(motionX, motionY, motionZ).normalize().dotProduct(rotationVec3d)+1D)*0.5D);
                    motionX *= motionMultiplier;
                    motionZ *= motionMultiplier;
                    motionY *= 0.91D;
                }

                renderYawOffset = rotationYaw;
                dragonPartHead.width = dragonPartHead.height = 3F;
                dragonPartTail1.width = dragonPartTail1.height = 2F;
                dragonPartTail2.width = dragonPartTail2.height = 2F;
                dragonPartTail3.width = dragonPartTail3.height = 2F;
                dragonPartBody.width = 5F; dragonPartBody.height = 3F;
                dragonPartWing1.width = 4F; dragonPartWing1.height = 2F;
                dragonPartWing2.width = 4F; dragonPartWing2.height = 3F;

                float offsetAngle = MathUtil.toRad((float)(getMovementOffsets(5, 1F)[1]-getMovementOffsets(10, 1F)[1])*10F);
                float angleCos = MathHelper.cos(offsetAngle);
                float angleSin = -MathHelper.sin(offsetAngle);
                float yawRad = MathUtil.toRad(rotationYaw);
                float yawSin = MathHelper.sin(yawRad);
                float yawCos = MathHelper.cos(yawRad);
                dragonPartBody.onUpdate();
                dragonPartBody.setLocationAndAngles(posX+yawSin*0.5F, posY, posZ-yawCos*0.5F, 0F, 0F);
                dragonPartWing1.onUpdate();
                dragonPartWing1.setLocationAndAngles(posX+yawCos*4.5F, posY+2D, posZ+yawSin*4.5F, 0F, 0F);
                dragonPartWing2.onUpdate();
                dragonPartWing2.setLocationAndAngles(posX-yawCos*4.5F, posY+2D, posZ-yawSin*4.5F, 0F, 0F);

                collideWithEntities(worldObj.getEntitiesWithinAABBExcludingEntity(this, dragonPartWing1.getEntityBoundingBox().expand(1.5D, 2D, 1.5D).offset(0D, -2D, 0D)));
                collideWithEntities(worldObj.getEntitiesWithinAABBExcludingEntity(this, dragonPartWing2.getEntityBoundingBox().expand(1.5D, 2D, 1.5D).offset(0D, -2D, 0D)));
                collideWithEntities(worldObj.getEntitiesWithinAABBExcludingEntity(this, dragonPartTail3.getEntityBoundingBox().expand(0.8D, 1D, 0.8D)));
                collideWithEntities(worldObj.getEntitiesWithinAABBExcludingEntity(this, dragonPartHead.getEntityBoundingBox().expand(0.6D, 1D, 0.6D)));

                double[] oldOffsets = getMovementOffsets(5, 1F), currentOffsets = getMovementOffsets(0, 1F);
                float moveX = MathHelper.sin(MathUtil.toRad(rotationYaw)-randomYawVelocity*0.01F);
                float moveZ = MathHelper.cos(MathUtil.toRad(rotationYaw)-randomYawVelocity*0.01F);
                dragonPartHead.onUpdate();
                dragonPartHead.setLocationAndAngles(posX+moveX*5.5F*angleCos, posY+currentOffsets[1]-oldOffsets[1]+angleSin*5.5F, posZ-moveZ*5.5F*angleCos, 0F, 0F);

                for(int part = 0; part < 3; part++){
                    MultiPartEntityPart tailPart = part == 0 ? dragonPartTail1 : part == 1 ? dragonPartTail2 : dragonPartTail3;

                    double[] partOffsets = getMovementOffsets(12+part*2, 1F);
                    float partYaw = MathUtil.toRad(rotationYaw)+MathUtil.toRad((float)MathHelper.wrapDegrees(partOffsets[0]-oldOffsets[0]));
                    float partYawSin = MathHelper.sin(partYaw);
                    float partYawCos = MathHelper.cos(partYaw);
                    float partMp = (part+1)*2F;
                    tailPart.onUpdate();
                    tailPart.setLocationAndAngles(posX-((yawSin*1.5F+partYawSin*partMp)*angleCos), posY+(partOffsets[1]-oldOffsets[1])-((partMp+1.5F)*angleSin)+1.5D, posZ+((yawCos*1.5F+partYawCos*partMp)*angleCos), 0F, 0F);
                }

                if (!worldObj.isRemote){
                    slowed = destroyBlocksInAABB(dragonPartHead.getEntityBoundingBox())|destroyBlocksInAABB(dragonPartBody.getEntityBoundingBox());
                    if (currentAttack.id == 1)
                        slowed |= destroyBlocksInAABB(dragonPartWing1.getEntityBoundingBox().expand(0.5D, 0.5D, 0.5D))|destroyBlocksInAABB(dragonPartWing2.getEntityBoundingBox().expand(0.5D, 0.5D, 0.5D));

                    attacks.updatePassiveAttacks(currentAttack);
                }
            }
            bossInfo.setName((isAngry() ? new TextComponentString("Angry Ender Dragon") : new TextComponentString("Ender Dragon")));
            bossInfo.setPercent(getHealth()/getMaxHealth());
        }

        @Override
        public boolean attackEntityFromPart(MultiPartEntityPart dragonPart, DamageSource source, float amount){
            if ((source.isExplosion() && source.getTrueSource()== this) || dragonHurtTime > 0 || freezeAI)return false;
            if (noViablePlayers && source.getTrueSource() instanceof EntityPlayer && !attacks.isPlayerViable((EntityPlayer)source.getTrueSource()))amount *= 0.1F;
            spawnCooldown = 0;

            if (dragonPart != dragonPartHead)amount = amount/3+1;
            amount = Math.min(amount, CommonProxy.opMobs ? 14F : 18F);

            int players = attacks.getViablePlayers().size();
            if (players > 1)amount = amount*(1F-Math.max(0.5F, (players-1)*0.05F));

            DamageTakenEvent event = new DamageTakenEvent(source, amount);
            currentAttack.onDamageTakenEvent(event);
            currentAttack.onDamageTaken(event.damage);
            amount = event.damage;

            boolean shouldChangeTarget = (target != null && getDistanceSq(target) < 4600D && (!angryStatus || rand.nextInt(3) != 0));

            if (shouldChangeTarget && ticksExisted-lastAttackInterruption >= 500){
                trySetTarget(null);
                lastAttackInterruption = ticksExisted;

                float yawRad = MathUtil.toRad(rotationYaw);
                trySetTargetPosition(posX+(MathHelper.sin(yawRad)*5F)+((rand.nextFloat()-0.5F)*2F),
                        posY+(rand.nextFloat()*3F)+1D,
                        posZ-(MathHelper.cos(yawRad)*5F)+((rand.nextFloat()-0.5F)*2F));
            }

            //if ((source.getTrueSource() instanceof EntityPlayer || source.isExplosion()) && super.attackEntityFrom(source, amount))hurtResistantTime = (dragonHurtTime = (byte)(hurtTime = 15))+10;
            if ((source.getTrueSource() instanceof EntityPlayer || source.isExplosion()) && super.attackDragonFrom(source, amount))hurtResistantTime = (dragonHurtTime = (byte)(hurtTime = 15))+10;
            // TODO CausatumUtils.increase(source, CausatumMeters.DRAGON_DAMAGE, amount*16F);
            return true;
        }

        @Override
        public boolean attackEntityFrom(DamageSource source, float amount){
            return false;
        }

        @Override
        protected void onDeathUpdate(){
            ++deathTicks;

            if (!worldObj.isRemote){
                if (deathTicks == 1){
                    //achievements.onBattleFinished();
                    this.world.playBroadcastSound(1028, new BlockPos(this), 0);
                }
                else if (deathTicks == 20 || deathTicks == 140){ // double check
                    for(Entity entity: EntitySelector.any(worldObj)){
                        if (MathUtil.distance(entity.posX, entity.posZ) > 180D)continue;

                        if (entity instanceof EntityEnderman)((EntityEnderman)entity).setAttackTarget(null);
                    }
                }
                /*else if (deathTicks > 4 && deathTicks < 70 && deathTicks%4 == 0){
                    BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();

                    for(int a = 0; a < 250; a++){
                        boolean sidelr = rand.nextBoolean();
                        boolean sideud = rand.nextBoolean();
                        if (sidelr && sideud) {
                            mpos.setPos(this).move(EnumFacrand.nextInt(51) - 25);
                        }
                        if (sidelr && !sideud) {
                            mpos.setPos(this).move(rand.nextInt(51) - 25, 0, rand.nextInt(51) - 25);
                        }
                        if (!sidelr && sideud) {
                            mpos.setPos(this).move(rand.nextInt(51) - 25, 0, rand.nextInt(51) - 25);
                        }
                        if (!sidelr && !sideud) {
                            mpos.setPos(this).move(rand.nextInt(51) - 25, 0, rand.nextInt(51) - 25);
                        }
                        mpos.setPos(this).move(EnumFacing.EAST,rand.nextInt(51) - 25);
                        mpos.move(EnumFacing.SOUTH,rand.nextInt(51) - 25);
                        mpos.setY(1+DragonUtil.getTopBlockY(worldObj, Blocks.END_STONE, mpos.getX(), mpos.getZ(), 65));
                        if (mpos.getY() > 40 && world.getBlockState(mpos) == Blocks.FIRE)world.setBlockToAir(mpos);
                    }
                }*/

                else if (deathTicks > 150 && deathTicks%5 == 0)DragonUtil.spawnXP(this, 550+(250*(rewards.getFinalDifficulty()>>2)));
                /*else if (deathTicks == 191){
                    for(EntityPlayer player:EntitySelector.players(worldObj))player.addStat(AchievementManager.GO_INTO_THE_END, 1);
                }*/
                else if (deathTicks == 200) DragonUtil.spawnXP(this, 4000);

                if (deathTicks > 40 && deathTicks < 140)rewards.spawnEssence(worldObj, (int)posX, (int)posZ);

                this.move(MoverType.SELF, 0.0D, 0.10000000149011612D, 0.0D);
                this.rotationYaw += 20.0F;
                this.renderYawOffset = this.rotationYaw;
            }

            if (deathTicks >= 180 && deathTicks <= 200){
                worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, posX+(rand.nextFloat()-0.5F)*8F, posY+2D+(rand.nextFloat()-0.5F)*4F, posZ+(rand.nextFloat()-0.5F)*8F, 0D, 0D, 0D);
            }

            move(MoverType.SELF,0D, 0.1D, 0D);
            renderYawOffset = rotationYaw += 20F;

            if (deathTicks == 200 && !worldObj.isRemote){
                rewards.spawnPortalToken(world,(int)posX,(int)posZ);
                DragonUtil.spawnXP(this, 2000);
                DragonChunkManager.release(this);
                this.fightManager.processDragonDeath(this);
                setDead();
            }
        }

        public double[] getMovementOffsets(int offset, float partialTickTime){
            partialTickTime = getHealth() <= 0F ? 0F : 1F-partialTickTime;
            int index = movementBufferIndex-offset&63, prevIndex = movementBufferIndex-offset-1&63;

            return new double[]{
                    movementBuffer[index][0]+MathHelper.wrapDegrees(movementBuffer[prevIndex][0]-movementBuffer[index][0])*partialTickTime,
                    movementBuffer[index][1]+(movementBuffer[prevIndex][1]-movementBuffer[index][1])*partialTickTime
            };
        }

        private void updateEnderCrystal(){
            if (healingEnderCrystal != null){
                if (healingEnderCrystal.isDead){
                    if (!worldObj.isRemote){
                        attackEntityFromPart(dragonPartHead, DamageSource.causeExplosionDamage((Explosion) null), 10F);
                        if (target == null)trySetTarget(attacks.getRandomPlayer());
                    }

                    healingEnderCrystal = null;
                }
                else if (ticksExisted%10 == 0 && getHealth() < getMaxHealth())setHealth(getHealth()+(CommonProxy.opMobs ? 1F : 2F));
            }

            if (rand.nextInt(10) == 0){
                float dist = 30F+4F*worldObj.getDifficulty().getDifficultyId()+(CommonProxy.opMobs ? 8F : 0F);
                healingEnderCrystal = EntitySelector.closest(this, EntityEnderCrystal.class, getEntityBoundingBox().expand(dist, dist, dist));
            }
        }


        private void collideWithEntities(List<? extends Entity> list){
            double bodyCenterX = (dragonPartBody.getEntityBoundingBox().minX+dragonPartBody.getEntityBoundingBox().maxX)*0.5D;
            double bodyCenterZ = (dragonPartBody.getEntityBoundingBox().minZ+dragonPartBody.getEntityBoundingBox().maxZ)*0.5D;

            for(Entity entity:list){
                if (entity instanceof EntityLivingBase || entity instanceof EntityBlockFallingObsidian){
                    while(entity.getRidingEntity() != null)entity = entity.getRidingEntity();

                    Vec3d vec = new Vec3d(entity.posX-bodyCenterX,0, entity.posZ-bodyCenterZ).normalize();
                    CollisionEvent event = new CollisionEvent(entity, vec.x*2D, 0.2D, vec.z*2D);
                    currentAttack.onCollisionEvent(event);
                    event.collidedEntity.motionX = event.velocityX;
                    event.collidedEntity.motionY = event.velocityY;
                    event.collidedEntity.motionZ = event.velocityZ;

                    if (entity instanceof EntityPlayerMP) PacketPipeline.sendToPlayer((EntityPlayerMP)entity, new C06SetPlayerVelocity(event.velocityX, event.velocityY, event.velocityZ));
                }
            }
        }

        private boolean destroyBlocksInAABB(AxisAlignedBB aabb){
            //if (!WorldUtil.getRuleBool(worldObj, GameRule.MOB_GRIEFING))return false;

            boolean wasBlocked = false;
            boolean spawnParticles = false;
            int minX = MathUtil.floor(aabb.minX+0.5D-rand.nextDouble()*rand.nextDouble()*5D);
            int minY = MathUtil.floor(aabb.minY+0.5D-rand.nextDouble()*rand.nextDouble()*5D);
            int minZ = MathUtil.floor(aabb.minZ+0.5D-rand.nextDouble()*rand.nextDouble()*5D);
            int maxX = MathUtil.floor(aabb.maxX-0.5D+rand.nextDouble()*rand.nextDouble()*5D);
            int maxY = MathUtil.floor(aabb.maxY-0.5D+rand.nextDouble()*rand.nextDouble()*5D);
            int maxZ = MathUtil.floor(aabb.maxZ-0.5D+rand.nextDouble()*rand.nextDouble()*5D);
            double rad = 2.8D+Math.min((aabb.maxX-aabb.minX)*0.5D, (aabb.maxZ-aabb.minZ)*0.5D);
            int cx = (int)((aabb.maxX-aabb.minX)*0.5D+aabb.minX);
            int cy = (int)((aabb.maxY-aabb.minY)*0.5D+aabb.minY);
            int cz = (int)((aabb.maxZ-aabb.minZ)*0.5D+aabb.minZ);
            BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();

            for(int xx = minX; xx <= maxX; xx++){
                for(int yy = minY; yy <= maxY; yy++){
                    for(int zz = minZ; zz <= maxZ; zz++){
                        mpos.setPos(xx, yy, zz);
                        Block block = world.getBlockState(mpos).getBlock();

                        if (angryStatus && block == Blocks.OBSIDIAN){
                            world.setBlockToAir(mpos);
                            EntityBlockFallingObsidian obsidian = new EntityBlockFallingObsidian(worldObj, xx, yy, zz);
                            obsidian.motionY = -0.2;
                            worldObj.spawnEntity(obsidian);
                            spawnParticles = true;
                        }
                        else if (block == Blocks.BEDROCK || (!angryStatus && (block == Blocks.OBSIDIAN || block == BlockInit.obsidian_falling || (block == Blocks.IRON_BARS && world.getBlockState(mpos.down()) == BlockInit.obsidian_falling)))){
                            wasBlocked = true;
                        }
                        else if (this.canDestroyBlock(block) && MathUtil.distance(xx-cx, yy-cy, zz-cz) <= rad+(0.9D*rand.nextDouble()-0.4D)){
                            spawnParticles = world.setBlockToAir(mpos.setPos(xx, yy, zz)) || spawnParticles;
                        }
                    }
                }
            }

            if (spawnParticles)worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, aabb.minX+(aabb.maxX-aabb.minX)*rand.nextFloat(), aabb.minY+(aabb.maxY-aabb.minY)*rand.nextFloat(), aabb.minZ+(aabb.maxZ-aabb.minZ)*rand.nextFloat(), 0D, 0D, 0D);

            return wasBlocked;
        }

        private void setNewTarget(){
            forceNewTarget = false;
            if (rand.nextBoolean() && trySetTarget(attacks.getWeakPlayer()))return;

            for(double newTargetX, newTargetY, newTargetZ;;){
                newTargetX = (rand.nextFloat()*120F-60F);
                newTargetY = (70F+rand.nextFloat()*50F);
                newTargetZ = (rand.nextFloat()*120F-60F);

                if (MathUtil.square(posX-newTargetX)+MathUtil.square(posY-newTargetY)+MathUtil.square(posZ-newTargetZ) > 100D){
                    trySetTargetPosition(newTargetX, newTargetY, newTargetZ);
                    break;
                }
            }
        }

        public boolean trySetTarget(EntityPlayer entity){
            if (entity != null && (entity.isDead || !attacks.isPlayerViable(entity) || spawnCooldown > 0))return false;
            forceNewTarget = false;

            TargetSetEvent event = new TargetSetEvent(target, entity);
            currentAttack.onTargetSetEvent(event);
            target = event.newTarget;
            return target != null;
        }

        public void trySetTargetPosition(double newTargetX, double newTargetY, double newTargetZ){
            TargetPositionSetEvent event = new TargetPositionSetEvent(target, targetX, targetY, targetZ, newTargetX, newTargetY, newTargetZ);
            currentAttack.onTargetPositionSetEvent(event);

            if (event.isCancelled() && event.currentEntityTarget != null)target = event.currentEntityTarget;
            else{
                targetX = event.newTargetX;
                targetY = event.newTargetY;
                targetZ = event.newTargetZ;
                target = null;
            }
        }

        public void forceSpecialAttack(DragonSpecialAttackBase newAttack){
            lastAttack = currentAttack;

            if (currentAttack != null){
                currentAttack.end();
                nextAttackTicks = currentAttack.getNextAttackTimer();
            }

            currentAttack = newAttack;
            currentAttack.init();
        }

        @Override
        public void writeEntityToNBT(NBTTagCompound nbt){
            super.writeEntityToNBT(nbt);
            nbt.setBoolean("angry", isAngry());
            nbt.setShort("nat", (short)Math.max(120, nextAttackTicks));
            nbt.setShort("dth", (short)deathTicks); // suck it, zeek :P
            nbt.setShort("scd", (short)Math.max(200, spawnCooldown));
            nbt.setByte("load", loadTimer);

            nbt.setTag("atk", attacks.writeToNBT());
            nbt.setTag("rwr", rewards.writeToNBT());
            //nbt.setTag("acv", achievements.writeToNBT().getUnderlyingTag());
        }

        @Override
        public void readEntityFromNBT(NBTTagCompound nbt){
            super.readEntityFromNBT(nbt);

            setAngry(nbt.getBoolean("angry"));
            nextAttackTicks = nbt.getShort("nat");
            deathTicks = nbt.getShort("dth");
            spawnCooldown = nbt.getShort("scd");
            loadTimer = nbt.hasKey("load") ? nbt.getByte("load") : loadTimer;

            attacks.readFromNBT(nbt.getCompoundTag("atk"));
            rewards.readFromNBT(nbt.getCompoundTag("rwr"));
            //achievements.readFromNBT(NBT.wrap(nbt.getCompoundTag("acv")));
        }

        public boolean canDestroyBlock(Block block){
            return  block != net.minecraft.init.Blocks.BARRIER &&
                    block != net.minecraft.init.Blocks.BEDROCK &&
                    block != net.minecraft.init.Blocks.END_PORTAL &&
                    block != net.minecraft.init.Blocks.END_PORTAL_FRAME &&
                    block != net.minecraft.init.Blocks.COMMAND_BLOCK &&
                    block != net.minecraft.init.Blocks.REPEATING_COMMAND_BLOCK &&
                    block != net.minecraft.init.Blocks.CHAIN_COMMAND_BLOCK &&
                    block != net.minecraft.init.Blocks.IRON_BARS &&
                    block != net.minecraft.init.Blocks.END_GATEWAY;
        }

        public void setAngry(boolean angry){
            //entityData.setBoolean(Data.ANGRY, angry);
            this.ANGRY = angry;
        }

        public boolean isAngry(){
            //return entityData.getBoolean(Data.ANGRY);
            return this.ANGRY;
        }

        public void setWingSpeed(float wingSpeed){
            //entityData.setFloat(Data.WING_SPEED, wingSpeed);
            this.WING_SPEED = wingSpeed;
        }

        public float getWingSpeed(){
            //return entityData.getFloat(Data.WING_SPEED);
            return this.WING_SPEED;
        }

        @Override
        protected void despawnEntity(){}

        @Override
        public Entity[] getParts(){
            return dragonPartArray;
        }

        @Override
        public boolean canBeCollidedWith(){
            return false;
        }

        public void addTrackingPlayer(EntityPlayerMP player)
        {
            super.addTrackingPlayer(player);
            this.bossInfo.addPlayer(player);
        }

        public void removeTrackingPlayer(EntityPlayerMP player)
        {
            super.removeTrackingPlayer(player);
            this.bossInfo.removePlayer(player);
        }

        public void switchMusic(SoundEvent e){
        if (world.isRemote) {
            Minecraft mc = Minecraft.getMinecraft();
            mc.getSoundHandler().stopSounds();
            //mc.getMusicTicker().playMusic(MusicTicker.MusicType.MENU);
            mc.player.playSound(e, 10000, 1);
        }
    }
        /*@Override
        public World func_82194_d(){ // OBFUSCATED get world obj
            return worldObj;
        }

        @Override
        public String getCommandSenderName(){
            return hasCustomNameTag() ? getCustomNameTag() : StatCollector.translateToLocal(Baconizer.mobName("entity.dragon.name"));
        }

        @Override
        protected String getLivingSound(){
            return "mob.enderdragon.growl";
        }

        @Override
        protected String getHurtSound(){
            return Baconizer.soundNormal("mob.enderdragon.hit");
        }
*/
        @Override
        protected float getSoundVolume(){
            return angryStatus ? 6.5F : 5F;
        }
    }


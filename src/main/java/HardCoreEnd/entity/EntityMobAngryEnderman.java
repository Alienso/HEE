package HardCoreEnd.entity;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import HardCoreEnd.proxy.CommonProxy;
import HardCoreEnd.util.EntitySelector;
import HardCoreEnd.util.MathUtil;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

public class EntityMobAngryEnderman extends EntityEnderman {
    private static final UUID aggroSpeedBoostID = UUID.fromString("020E0DFB-87AE-4653-9556-831010E291A0");
    private static final AttributeModifier aggroSpeedBoost;
    private Entity lastEntityToAttack;
    public EntityPlayer target;
    private int teleportDelay;
    private Random rand;

    public EntityMobAngryEnderman(World world) {
        super(world);
        this.rand = new Random();
        this.teleportDelay = 0;

        this.setSize(0.6F, 2.9F);
        this.stepHeight = 1.0F;

        //-------------------
        this.initializeEntityAttributes();

    }
    @Override
    protected void initEntityAI()
    {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, true));
        this.tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D, 0.0F));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityMobAngryEnderman.AIFindPlayer(this));
        this.targetTasks.addTask(2, new EntityAIHurtByTarget(this, false, new Class[0]));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityEndermite.class, 10, true, false, new Predicate<EntityEndermite>()
        {
            public boolean apply(@Nullable EntityEndermite p_apply_1_)
            {
                return p_apply_1_.isSpawnedByPlayer();
            }
        }));
    }

    public EntityMobAngryEnderman(World world, double x, double y, double z) {
        this(world);
        this.setPosition(x,y,z);
    }

    protected void initializeEntityAttributes() {
        //super.func_110147_ax();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(CommonProxy.opMobs ? 40.0D : 32.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(CommonProxy.opMobs ? 7.4*0.3F : 0.3F);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(CommonProxy.opMobs ? 9.0D : 5.0D);
    }

    public Entity getTarget() {
        return this.target;
    }

    @Override
    public void onLivingUpdate() {
        if (this.inWater){
            this.damageEntity(DamageSource.DROWN, 1.0F);
            this.teleportRandomly();
        }

        this.lastEntityToAttack = this.target;
        /*if (this.target != null) {
            this.func_70625_a(this.target, 100.0F, 100.0F);
        }*/

        for(int i = 0; i < 2; ++i) {
            this.world.spawnParticle(EnumParticleTypes.PORTAL, this.getPosition().getX() + (this.rand.nextDouble() - 0.5D) * (double)this.width, this.getPosition().getY() + this.rand.nextDouble() * (double)this.height - 0.25D, this.getPosition().getZ() + (this.rand.nextDouble() - 0.5D) * (double)this.width, (this.rand.nextDouble() - 0.5D) * 2.0D, -this.rand.nextDouble(), (this.rand.nextDouble() - 0.5D) * 2.0D);
        }

        //if (!this.world.isRemote && this.func_70089_S()) {
        if (!this.world.isRemote) {
            if (this.target != null) {
                if (MathUtil.distance(this.posX-target.posX,this.posY-target.posY,this.posZ-target.posZ) > 256.0D && this.teleportDelay++ >= 30 && this.teleportToEntity(this.target)) {
                    this.teleportDelay = 0;
                }
            } else {
                this.teleportDelay = 0;
                if (this.rand.nextInt(30) == 0) {
                    //Iterator var5 = this.world.getPlayers(EntityPlayer.class, this.field_70121_D.func_72314_b(6.0D, 4.0D, 6.0D)).iterator();

                    /*while(var5.hasNext()) {
                        Object o = var5.next();
                        EntityPlayer player = (EntityPlayer)o;
                        if (!player.field_71075_bZ.field_75098_d) {
                            this.target = player;
                            break;
                        }
                    }*/
                    if (EntitySelector.players(world).isEmpty())
                        return;
                    this.target = EntitySelector.players(world).get(0);
                    this.setAttackTarget(target);
                    this.setRevengeTarget(target);
                    if (target instanceof EntityPlayer)
                        this.attackingPlayer = target;
                }
            }
        }
        //this.field_70703_bu = false;
        super.onLivingUpdate();
    }

    protected boolean teleportRandomly() {
        return this.teleportTo(this.getPosition().getX() + (this.rand.nextDouble() - 0.5D) * 64.0D, this.getPosition().getY() + (double)(this.rand.nextInt(64) - 32), this.getPosition().getZ() + (this.rand.nextDouble() - 0.5D) * 64.0D);
    }

    protected boolean teleportToEntity(Entity entity) {
        Vec3d vec3d = new Vec3d(this.posX - entity.posX, this.getEntityBoundingBox().minY + (double)(this.height / 2.0F) - entity.posY + (double)entity.getEyeHeight(), this.posZ - entity.posZ);
        vec3d = vec3d.normalize();
        double d0 = 16.0D;
        double d1 = this.posX + (this.rand.nextDouble() - 0.5D) * 8.0D - vec3d.x * 16.0D;
        double d2 = this.posY + (double)(this.rand.nextInt(16) - 8) - vec3d.y * 16.0D;
        double d3 = this.posZ + (this.rand.nextDouble() - 0.5D) * 8.0D - vec3d.z * 16.0D;
        return this.teleportTo(d1, d2, d3);
    }

    protected boolean teleportTo(double x, double y, double z) {
        net.minecraftforge.event.entity.living.EnderTeleportEvent event = new net.minecraftforge.event.entity.living.EnderTeleportEvent(this, x, y, z, 0);
        if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event)) return false;
        boolean flag = this.attemptTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ());

        if (flag)
        {
            this.world.playSound(null, this.prevPosX, this.prevPosY, this.prevPosZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, this.getSoundCategory(), 1.0F, 1.0F);
            this.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
        }

        return flag;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_ENDERMEN_SCREAM;
    }

    protected String func_70621_aR() {
        return "mob.endermen.hit";
    }

    protected String func_70673_aS() {
        return "mob.endermen.death";
    }

    /*protected int func_70633_aT() {
        return Item.field_77730_bn.field_77779_bT;
    }*/

    protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier)
    {
        super.dropEquipment(wasRecentlyHit, lootingModifier);
        IBlockState iblockstate = this.getHeldBlockState();

        if (iblockstate != null)
        {
            Item item = Item.getItemFromBlock(iblockstate.getBlock());
            int i = item.getHasSubtypes() ? iblockstate.getBlock().getMetaFromState(iblockstate) : 0;
            this.entityDropItem(new ItemStack(item, 1, i), 0.0F);
        }
    }

    public boolean attackEntityFrom(DamageSource source, float damage) {
            //this.dataManager.set(SCREAMING, Boolean.valueOf(true));
            if (source instanceof EntityDamageSourceIndirect && this.target == null) {
                for(int attempt = 0; attempt < 64; ++attempt) {
                    if (this.teleportRandomly()) {
                        return true;
                    }
                }

                return false;
            } else if (super.attackEntityFrom(source, damage)) {
                return true;
            } else {
                return false;
            }
    }

    private boolean shouldAttackPlayer(EntityPlayer player) {
        return true;
    }

    static class AIFindPlayer extends EntityAINearestAttackableTarget<EntityPlayer>
    {
        private final EntityMobAngryEnderman enderman;
        private EntityPlayer player;
        private int aggroTime;
        private int teleportTime;

        public AIFindPlayer(EntityMobAngryEnderman p_i45842_1_)
        {
            super(p_i45842_1_, EntityPlayer.class, false);
            this.enderman = p_i45842_1_;
        }

        public boolean shouldExecute()
        {
            double d0 = this.getTargetDistance();
            this.player = this.enderman.world.getNearestAttackablePlayer(this.enderman.posX, this.enderman.posY, this.enderman.posZ, d0, d0, (Function)null, new Predicate<EntityPlayer>()
            {
                public boolean apply(@Nullable EntityPlayer p_apply_1_)
                {
                    return p_apply_1_ != null && EntityMobAngryEnderman.AIFindPlayer.this.enderman.shouldAttackPlayer(p_apply_1_);
                }
            });
            return this.player != null;
        }

        public void startExecuting()
        {
            this.aggroTime = 999999;
            this.teleportTime = 0;
        }

        public void resetTask()
        {
            this.player = null;
            super.resetTask();
        }

        public boolean shouldContinueExecuting()
        {
            if (this.player != null)
            {
                if (!this.enderman.shouldAttackPlayer(this.player))
                {
                    return false;
                }
                else
                {
                    this.enderman.faceEntity(this.player, 10.0F, 10.0F);
                    return true;
                }
            }
            else
            {
                return this.targetEntity != null && ((EntityPlayer)this.targetEntity).isEntityAlive() ? true : super.shouldContinueExecuting();
            }
        }

        public void updateTask()
        {
            if (this.player != null)
            {
                if (--this.aggroTime <= 0)
                {
                    this.targetEntity = this.player;
                    this.player = null;
                    super.startExecuting();
                }
            }
            else
            {
                if (this.targetEntity != null)
                {
                    if ((this.targetEntity).getDistanceSq(this.enderman) > 16D && this.teleportTime++ >= 30 && this.enderman.teleportToEntity(this.targetEntity))
                    {
                        this.teleportTime = 0;
                    }
                }

                super.updateTask();
            }
        }
    }

    /*public boolean func_70652_k(Entity target) {
        if (super.func_70652_k(target)) {
            if (target instanceof EntityPlayer) {
                KnowledgeRegistrations.ANGRY_ENDERMAN.tryUnlockFragment((EntityPlayer)target, 0.06F);
            }

            return true;
        } else {
            return false;
        }
    }*/

    /*protected boolean func_70814_o() {
        return this.world.field_73011_w.field_76574_g == 1 ? true : super.func_70814_o();
    }

    public void setCanDespawn(boolean canDespawn) {
        this.field_70180_af.func_75692_b(19, (byte)(canDespawn ? 0 : 1));
    }

    public void func_70014_b(NBTTagCompound nbt) {
        super.func_70014_b(nbt);
        nbt.func_74757_a("canDespawn", this.field_70180_af.func_75683_a(19) == 0);
    }

    public void func_70037_a(NBTTagCompound nbt) {
        super.func_70037_a(nbt);
        this.setCanDespawn(nbt.func_74767_n("canDespawn"));
    }

    protected void func_70623_bb() {
        if (this.field_70180_af.func_75683_a(19) == 0) {
            super.func_70623_bb();
        }

    }

    public String func_70023_ak() {
        return StatCollector.func_74838_a("entity.angryEnderman.name");
    }*/

    static {
        aggroSpeedBoost = (new AttributeModifier(aggroSpeedBoostID, "Attacking speed boost", 7.4D, 0)).setSaved(false);
    }
}

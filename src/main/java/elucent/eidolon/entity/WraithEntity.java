package elucent.eidolon.entity;

import elucent.eidolon.Registry;
import elucent.eidolon.util.EntityUtil;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class WraithEntity extends MonsterEntity {
    public WraithEntity(EntityType<WraithEntity> type, World worldIn) {
        super(type, worldIn);
        registerGoals();
    }
    
    @Override
    public CreatureAttribute getCreatureAttribute() {
        return CreatureAttribute.UNDEAD;
    }

    @Override
    public boolean isEntityUndead() {
        return true;
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
    	if (target instanceof LivingEntity) {
    		this.castSpell((LivingEntity)target);
    		this.swing(Hand.MAIN_HAND, true);
    		this.swing(Hand.OFF_HAND, true);
    		return true;
    	}
    	
    	return false;
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.applyEntityAI();
    }

    public static AttributeModifierMap createAttributes() {
        return MonsterEntity.func_234295_eP_()
            .createMutableAttribute(Attributes.MAX_HEALTH, 20.0D)
            .createMutableAttribute(Attributes.MOVEMENT_SPEED, (double)0.2F)
            .createMutableAttribute(Attributes.ATTACK_DAMAGE, 0.0D)
            .createMutableAttribute(Attributes.ARMOR, 0.0D)
            .create();
    }

    protected void applyEntityAI() {
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public int getExperiencePoints(PlayerEntity player) {
        return 5;
    }
    
    /**
     * Creates a spell projectile that deals no real damage
     * The damage comes from the regular attack
     * @param target
     */
    public void castSpell(final LivingEntity target) {
    	final Vector3d pos = target.getBoundingBox().getCenter();
    	if (!world.isRemote) {
            world.addEntity(new BonechillProjectileEntity(Registry.BONECHILL_PROJECTILE.get(), world).shoot(
                pos.getX(), pos.getY() + 1D, pos.getZ(), 0, -1D, 0, this.getUniqueID()
            ));
            world.playSound(null, pos.getX(), pos.getY() + 1D, pos.getZ(), Registry.CAST_BONECHILL_EVENT.get(), SoundCategory.HOSTILE, 0.75f, 1.0f);
            final int chillTicks = (int) (400 * EntityUtil.getCurseMod(target));
            target.addPotionEffect(new EffectInstance(Registry.CHILLED_EFFECT.get(), chillTicks));
            target.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, this), 4F);
        }
    	this.swingArm(Hand.MAIN_HAND);
    }

    @Override
    public void livingTick() {
        if (this.world.isDaytime() && !this.world.isRemote) {
            float f = this.getBrightness();
            final BlockPos blockpos = this.getRidingEntity() instanceof BoatEntity ? (new BlockPos(this.getPosX(), (double) Math.round(this.getPosY()), this.getPosZ())).up() : new BlockPos(this.getPosX(), (double) Math.round(this.getPosY()), this.getPosZ());
            if (f > 0.5F && this.rand.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && this.world.canSeeSky(blockpos)) {
                this.setFire(8);
            }
        }

        // hover over water
        final FluidState below = world.getBlockState(getPositionUnderneath()).getFluidState();
        if (!below.isEmpty()) {
            final Vector3d motion = getMotion();
            this.setOnGround(true);
            if (getPosY() + motion.y < getPositionUnderneath().getY() + below.getHeight()) {
                setNoGravity(true);
                if (motion.y < 0) {
                	setMotion(motion.mul(1, 0, 1));
                }
                setPosition(getPosX(), getPositionUnderneath().getY() + below.getHeight(), getPosZ());
            }
        } else {
        	setNoGravity(false);
        }

        // slow fall
        this.fallDistance = 0;
        final Vector3d vector3d = this.getMotion();
        if (!this.onGround && vector3d.y < 0.0D) {
            this.setMotion(vector3d.mul(1.0D, 0.6D, 1.0D));
        }

        super.livingTick();
    }

    @Override
    public SoundEvent getDeathSound() {
    	return Registry.WRAITH_DEATH.get();
    }

    @Override
    public SoundEvent getAmbientSound() {
    	return Registry.WRAITH_LIVING.get();
    }

    @Override
    public SoundEvent getHurtSound(DamageSource source) {
    	return Registry.WRAITH_HURT.get();
    }
}

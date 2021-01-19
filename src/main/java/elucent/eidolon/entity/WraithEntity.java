package elucent.eidolon.entity;

import elucent.eidolon.Registry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class WraithEntity extends SkeletonEntity {
	public WraithEntity(EntityType<WraithEntity> type, World worldIn) {
        super(type, worldIn);
        registerGoals();
    }
    
    @Override
    public void attackEntityWithRangedAttack(LivingEntity target, float distanceFactor) {
    	final Vector3d pos = this.getPositionVec().add(this.getLookVec().scale(0.5)).add(0.5 * Math.sin(Math.toRadians(225 - this.rotationYawHead)), this.getHeight() * 2 / 3, 0.5 * Math.cos(Math.toRadians(225 - this.rotationYawHead)));
    	final Vector3d vel = this.getEyePosition(0).add(this.getLookVec().scale(5)).subtract(pos).scale(0.05D);
    	
    	final BonechillProjectileEntity spell = new BonechillProjectileEntity(Registry.BONECHILL_PROJECTILE.get(), world);
    	spell.setNoGravity();
        world.addEntity(spell.shoot(pos.x, pos.y, pos.z, vel.x, vel.y, vel.z, this.getUniqueID()));
        
        this.swingArm(Hand.MAIN_HAND);
    	this.playSound(Registry.WRAITH_SCREAM.get(), 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.2F + 0.55F));
     }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        super.registerGoals();
    }

    public static AttributeModifierMap createAttributes() {
        return MonsterEntity.func_234295_eP_()
            .createMutableAttribute(Attributes.MAX_HEALTH, 20.0D)
            .createMutableAttribute(Attributes.MOVEMENT_SPEED, (double)0.2F)
            .createMutableAttribute(Attributes.ATTACK_DAMAGE, 3.0D)
            .createMutableAttribute(Attributes.ARMOR, 1.0D)
            .create();
    }

    @Override
    public int getExperiencePoints(PlayerEntity player) {
        return 5;
    }

    @Override
    public void livingTick() {
        // Hover over water
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

        // Slow fall
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

	@Override
	protected SoundEvent getStepSound() {
		return null;
	}
	
	@Override
	protected void dropSpecialItems(DamageSource source, int looting, boolean recentlyHitIn) {
		// Nope
	}
}

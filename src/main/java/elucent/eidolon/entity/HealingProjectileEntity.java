package elucent.eidolon.entity;

import java.util.List;

import elucent.eidolon.Registry;
import elucent.eidolon.network.MagicBurstEffectPacket;
import elucent.eidolon.network.Networking;
import elucent.eidolon.particle.Particles;
import elucent.eidolon.util.ColorUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HealingProjectileEntity extends SpellProjectileEntity {
    public HealingProjectileEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    public void tick() {
        super.tick();

        Vector3d motion = getMotion();
        Vector3d pos = getPositionVec();
        Vector3d norm = motion.normalize().scale(0.025f);
        for (int i = 0; i < 8; i ++) {
            double lerpX = MathHelper.lerp(i / 8.0f, prevPosX, pos.x);
            double lerpY = MathHelper.lerp(i / 8.0f, prevPosY, pos.y);
            double lerpZ = MathHelper.lerp(i / 8.0f, prevPosZ, pos.z);
            Particles.create(Registry.SPARKLE_PARTICLE)
                .addVelocity(-norm.x, -norm.y, -norm.z)
                .setAlpha(0.0625f, 0).setScale(0.625f, 0)
                .setColor(0.9f, 0.2f, 0.2f, 0.9f, 0.2f, 0.2f)
                .setLifetime(5)
                .spawn(world, lerpX, lerpY, lerpZ);
            Particles.create(Registry.SPARKLE_PARTICLE)
                .addVelocity(-norm.x, -norm.y, -norm.z)
                .setAlpha(0.125f, 0).setScale(0.25f, 0.125f)
                .setColor(1.0f, 0.7f, 0.8f, 1.0f, 0.7f, 0.8f)
                .setLifetime(20)
                .spawn(world, lerpX, lerpY, lerpZ);
        }
    }
    
    protected void healTarget(final LivingEntity target) {
    	final float power = 3.5f + (potency * 0.5f);
    	target.heal(power);
    }
    
    @Override
    protected void onImpact(RayTraceResult ray, LivingEntity target) {
    	super.onImpact(ray, target);
    	this.healTarget(target);
    	if (occultism > 0) {
    		target.addPotionEffect(new EffectInstance(Effects.REGENERATION, occultism * 50));
    	}
    	this.onImpact(ray);
    }

    @Override
    protected void onImpact(RayTraceResult ray) {
        this.setDead();
        if (!world.isRemote) {
            final Vector3d pos = ray.getHitVec();
            world.playSound(null, pos.x, pos.y, pos.z, Registry.SPLASH_SOULFIRE_EVENT.get(), SoundCategory.NEUTRAL, 0.1f, rand.nextFloat() * 0.3f + 1.2f);
            Networking.sendToTracking(world, getPosition(), new MagicBurstEffectPacket(pos.x, pos.y, pos.z, ColorUtil.packColor(255, 255, 192, 203), ColorUtil.packColor(255, 255, 15, 15)));
            
            if (!this.hitEntity) {
                final AxisAlignedBB bb = this.getBoundingBox().grow(1.5D, 1.0D, 1.5D);
                final List<LivingEntity> list = this.world.getEntitiesWithinAABB(LivingEntity.class, bb);
                if (!list.isEmpty()) {
                   for(LivingEntity livingEntity : list) {
                	   this.healTarget(livingEntity);
                	   return;
                   }
                }
            }
        }
    }
}

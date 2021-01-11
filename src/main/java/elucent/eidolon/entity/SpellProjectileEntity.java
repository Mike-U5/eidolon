package elucent.eidolon.entity;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public abstract class SpellProjectileEntity extends Entity {
    protected UUID casterId = null;
    protected boolean wraithSpell = false;
    protected int potency = 0;
    protected int occultism = 0;

    public SpellProjectileEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }
    
    public Entity shoot(double x, double y, double z, double vx, double vy, double vz, UUID caster) {
        setPosition(x, y, z);
        setMotion(vx, vy, vz);
        this.casterId = caster;
        velocityChanged = true;
        return this;
    }

    public void setWraithSpell() {
    	this.wraithSpell = true;
    }

    @Override
    public void tick() {
    	final Vector3d motion = getMotion();

    	if (wraithSpell) {
    		setMotion(motion.x, (motion.y > 0 ? motion.y : motion.y) - 0.005f, motion.z);
    	} else {
    		setMotion(motion.x * 0.96, (motion.y > 0 ? motion.y * 0.96 : motion.y) - 0.03f, motion.z * 0.96);
    	}

        super.tick();

        if (!world.isRemote) {
            RayTraceResult ray = ProjectileHelper.func_234618_a_(this, (e) -> e instanceof LivingEntity && ((LivingEntity)e).isAlive() && !e.getUniqueID().equals(casterId));
            if (ray.getType() == RayTraceResult.Type.ENTITY) {
                onImpact(ray, (LivingEntity)((EntityRayTraceResult)ray).getEntity());
            } else if (ray.getType() == RayTraceResult.Type.BLOCK) {
                onImpact(ray);
            }
        }

        final Vector3d pos = getPositionVec();
        prevPosX = pos.x;
        prevPosY = pos.y;
        prevPosZ = pos.z;
        setPosition(pos.x + motion.x, pos.y + motion.y, pos.z + motion.z);
    }

    protected abstract void onImpact(RayTraceResult ray, LivingEntity target);
    protected abstract void onImpact(RayTraceResult ray);

    @Override
    protected void registerData() {
        //
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        this.casterId = compound.contains("caster") ? compound.getUniqueId("caster") : null;
        this.wraithSpell = compound.contains("wraithSpell") ? compound.getBoolean("wraithSpell") : false;
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        if (this.casterId != null) compound.putUniqueId("caster", this.casterId);
        compound.putBoolean("wraithSpell", this.wraithSpell);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}

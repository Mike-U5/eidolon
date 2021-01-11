package elucent.eidolon.item;

import elucent.eidolon.Registry;
import elucent.eidolon.entity.SoulfireProjectileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.server.ServerWorld;

public class SoulfireWandItem extends WandItem {
    public SoulfireWandItem(Properties builderIn) {
        super(builderIn);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity entity, Hand hand) {
    	final ItemStack stack = entity.getHeldItem(hand);
        if (stack.getMaxDamage() == stack.getDamage()) {
        	return ActionResult.resultFail(stack);
        }
        
        if (!entity.isSwingInProgress) {
            if (!world.isRemote) {
                Vector3d pos = entity.getPositionVec().add(entity.getLookVec().scale(0.5)).add(0.5 * Math.sin(Math.toRadians(225 - entity.rotationYawHead)), entity.getHeight() * 2 / 3, 0.5 * Math.cos(Math.toRadians(225 - entity.rotationYawHead)));
                Vector3d vel = entity.getEyePosition(0).add(entity.getLookVec().scale(40)).subtract(pos).scale(1.0 / 20);
                world.addEntity(new SoulfireProjectileEntity(Registry.SOULFIRE_PROJECTILE.get(), world).shoot(
                    pos.x, pos.y, pos.z, vel.x, vel.y, vel.z, entity.getUniqueID()
                ));
                world.playSound(null, pos.x, pos.y, pos.z, Registry.CAST_SOULFIRE_EVENT.get(), SoundCategory.NEUTRAL, 0.75f, random.nextFloat() * 0.2f + 0.9f);
                //stack.damageItem(1, entity, (player) -> {
                //    player.sendBreakAnimation(hand);
                //});
                stack.setDamage(this.getDamage(stack) + this.visPerCast());
                this.putWandsOnCooldown(entity);

                try {
                    Template t = ((ServerWorld)world).getStructureTemplateManager().getTemplate(new ResourceLocation("eidolon", "corridor"));
                    BlockPos d = t.getSize();
                    Rotation r = Rotation.values()[entity.getHorizontalFacing().getHorizontalIndex()];
                    BlockPos o = new BlockPos(-d.getX() / 2, -d.getY() / 2, -d.getZ() / 2);
                    BlockPos s = new BlockPos(Math.max(o.getX(), o.getZ()), o.getY(), Math.max(o.getX(), o.getZ()));
                    t.func_237152_b_((ServerWorld)world, entity.getPosition().down(8).add(o.rotate(r)).subtract(s), new PlacementSettings().setRotation(r), random);
                } catch (Exception e) {
                    //
                }
                
                entity.getCooldownTracker().setCooldown(this, this.getCooldown(entity));
            }
            entity.swingArm(hand);
            return ActionResult.resultSuccess(stack);
        }
        return ActionResult.resultPass(stack);
    }
}

package elucent.eidolon.spell;

import java.util.List;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public abstract class TransmuteSpell extends StaticSpell {

	public TransmuteSpell(ResourceLocation name, Sign[] signs) {
		super(name, signs);
	}
	
	public List<ItemEntity> getTargetedItems(World world, PlayerEntity player) {
    	final RayTraceResult ray = world.rayTraceBlocks(new RayTraceContext(player.getEyePosition(0), player.getEyePosition(0).add(player.getLookVec().scale(4)), RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player));
    	final Vector3d v = ray.getType() == RayTraceResult.Type.BLOCK ? ray.getHitVec() : player.getEyePosition(0).add(player.getLookVec().scale(4));
        return world.getEntitiesWithinAABB(ItemEntity.class, new AxisAlignedBB(v.x - 1.5, v.y - 1.5, v.z - 1.5, v.x + 1.5, v.y + 1.5, v.z + 1.5));
    }
	
	public ItemStack getTargetedStack(World world, PlayerEntity player) {
		final List<ItemEntity> list = this.getTargetedItems(world, player);
		if (list.size() != 1) {
			return null;
		}
		return list.get(0).getItem();
	}
	
	public boolean isHoldingItem(final PlayerEntity player, final Item item) {
    	if (player.getHeldItem(Hand.MAIN_HAND).getItem() == item) {
    		return true;
    	}
    	
    	if (player.getHeldItem(Hand.OFF_HAND).getItem() == item) {
    		return true;
    	}
		return false;
	}
}

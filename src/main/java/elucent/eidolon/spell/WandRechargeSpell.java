package elucent.eidolon.spell;

import java.util.List;

import elucent.eidolon.Registry;
import elucent.eidolon.network.MagicBurstEffectPacket;
import elucent.eidolon.network.Networking;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class WandRechargeSpell extends TransmuteSpell {
    public WandRechargeSpell(ResourceLocation name, Sign... signs) {
        super(name, signs);
    }

    @Override
    public boolean canCast(World world, BlockPos pos, PlayerEntity player) {
    	final ItemStack target = getTargetedStack(world, player);
    	
    	// Check if a target is targeted
    	if (target == null) {
    		return false;
    	}
    	
    	// Check if target has a repair item
    	final Item repairItem = getRepairItem(target.getItem());
    	if (repairItem == null) {
    		player.sendStatusMessage(new TranslationTextComponent("This item cannot be recharged."), true);
    		return false;
    	}
    	
    	// Check if target is damaged
    	if (target.getDamage() == 0) {
    		player.sendStatusMessage(new TranslationTextComponent("The wand is fully charged."), true);
    		return false;
    	}
    	
    	// Check if appropriate repair item is held
    	if (!isHoldingItem(player, repairItem)) {
    		player.sendStatusMessage(new TranslationTextComponent("You must sacrifice a wand core to recharge this wand."), true);
    		return false;
    	}
        
        return true;
    }

    @Override
    public void cast(World world, BlockPos pos, PlayerEntity player) {
    	final RayTraceResult ray = world.rayTraceBlocks(new RayTraceContext(player.getEyePosition(0), player.getEyePosition(0).add(player.getLookVec().scale(4)), RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player));
    	final Vector3d v = ray.getType() == RayTraceResult.Type.BLOCK ? ray.getHitVec() : player.getEyePosition(0).add(player.getLookVec().scale(4));
    	final List<ItemEntity> items = world.getEntitiesWithinAABB(ItemEntity.class, new AxisAlignedBB(v.x - 1.5, v.y - 1.5, v.z - 1.5, v.x + 1.5, v.y + 1.5, v.z + 1.5));

    	if (items.size() == 1 && items.get(0).getItem().getDamage() > 0) {
    		if (!world.isRemote) {
    			final ItemStack stack = items.get(0).getItem();
        		final Item repairItem = getRepairItem(stack.getItem());
                if (repairItem != null && consumeHeldItem(player, repairItem)) {
                	stack.setDamage(0);
                    items.get(0).setItem(stack);
                    final Vector3d p = items.get(0).getPositionVec();
                    items.get(0).setDefaultPickupDelay();
                    Networking.sendToTracking(world, items.get(0).getPosition(), new MagicBurstEffectPacket(p.x, p.y, p.z, Signs.SACRED_SIGN.getColor(), Signs.SACRED_SIGN.getColor()));
                }
            } else {
                world.playSound(player, player.getPosition(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.NEUTRAL, 1.0F, 0.6F + world.rand.nextFloat() * 0.2F);
            }
        }
    }
    
    /** Get repair items **/
    private Item getRepairItem(final Item wand) {
    	if (wand == Registry.BONECHILL_WAND.get()) {
    		return Registry.WRAITH_HEART.get();
    	}
    	if (wand == Registry.SOULFIRE_WAND.get()) {
    		return Registry.SHADOW_GEM.get();
    	}
    	if (wand == Registry.HEALING_WAND.get()) {
    		return Items.GOLDEN_APPLE;
    	}
    	return null;
    }
    
    /** Consume repair items **/
    public boolean consumeHeldItem(final PlayerEntity player, final Item item) {
    	final ItemStack mainStack = player.getHeldItem(Hand.MAIN_HAND);
    	if (mainStack.getItem() == item) {
    		mainStack.shrink(1);
    		if (mainStack.getCount() == 0) {
    			player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
    		} else {
    			player.setHeldItem(Hand.MAIN_HAND, mainStack);
    		}
    		
    		return true;
    	}
    	
    	final ItemStack offStack = player.getHeldItem(Hand.OFF_HAND);
    	if (offStack.getItem() == item) {
    		offStack.shrink(1);
    		if (mainStack.getCount() == 0) {
    			player.setHeldItem(Hand.OFF_HAND, ItemStack.EMPTY);
    		} else {
    			player.setHeldItem(Hand.OFF_HAND, offStack);
    		}
    		
    		return true;
    	}
    	
    	return false;
    }
}

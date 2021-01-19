package elucent.eidolon.item;

import elucent.eidolon.Registry;
import elucent.eidolon.entity.HealingProjectileEntity;
import elucent.eidolon.util.EntityUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class HealingWandItem extends WandItem {
    public HealingWandItem(Properties builder) {
        super(builder);
    }
    
    @Override
    public void castSpell(World world, PlayerEntity player, ItemStack stack) {
    	final Vector3d pos = player.getPositionVec().add(player.getLookVec().scale(0.1)).add(0.5 * Math.sin(Math.toRadians(225 - player.rotationYawHead)), player.getHeight() * 2 / 3, 0.5 * Math.cos(Math.toRadians(225 - player.rotationYawHead)));
        final Vector3d vel = player.getEyePosition(0).add(player.getLookVec().scale(40)).subtract(pos).scale(1.0 / 20);
        
        final HealingProjectileEntity spell = new HealingProjectileEntity(Registry.HEALING_PROJECTILE.get(), world);
        spell.setPotency(getPotency(stack));
        spell.setOccultism(getOccultism(stack));
        world.addEntity(spell.shoot(
            pos.x, pos.y, pos.z, vel.x, vel.y, vel.z, player.getUniqueID()
        ));
        
        world.playSound(null, pos.x, pos.y, pos.z, Registry.CAST_HEALING_EVENT.get(), SoundCategory.NEUTRAL, 0.75f, random.nextFloat() * 0.2f + 0.9f);
        stack.setDamage(this.getDamage(stack) + 1); 
        
        if (!EntityUtil.hasWizardHat(player)) {
			player.getCooldownTracker().setCooldown(this, 5);
		}
    }
    
    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
    	return !context.getWorld().isRemote ? ActionResultType.SUCCESS : ActionResultType.PASS;
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
    	player.setActiveHand(hand);
    	return new ActionResult<>(ActionResultType.SUCCESS, player.getHeldItem(hand));
    }
    
    @Override
    public UseAction getUseAction(ItemStack stack) {
    	return (stack.getDamage() >= stack.getMaxDamage()) ? UseAction.NONE : UseAction.BOW;
	}
    
    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, LivingEntity entityLiving) {
        if (entityLiving instanceof PlayerEntity) {
        	this.castSpell(entityLiving.world, (PlayerEntity)entityLiving, stack);
        }

        return stack;
    }
    
    @Override
	public int getMaxDamage(ItemStack stack) {
		return 75 + (getFrugal(stack) * 18);
	}
    
    @Override
    public int getUseDuration(ItemStack stack) {
    	return 32;
    }
}

package elucent.eidolon.item;

import java.util.List;

import elucent.eidolon.Registry;
import elucent.eidolon.util.EntityUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class WandItem extends ItemBase {
	public static final String PORTENCY = "WI_PORTENCY";
	public static final String FRUGAL = "WI_FRUGAL";
	public static final String OCCULTISM = "WI_OCCULTISM";
	
    public WandItem(Properties properties) {
        super(properties);
    }
    
    private String getNumeral(final int amp) {
    	switch(amp) {
	  	  case 1:
	  	    return "I";
	  	  case 2:
	  	    return "II";
    	  case 3:
    	    return "III";
    	  case 4:
    	    return "VI";
    	  default:
    	    return "V";
    	}
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (this.loreTag != null) {
        	if (getPotency(stack) > 0) {
        		tooltip.add(new StringTextComponent(TextFormatting.LIGHT_PURPLE + "Potency " + getNumeral(getPotency(stack))));
        	}
			if (getFrugal(stack) > 0) {
				tooltip.add(new StringTextComponent(TextFormatting.LIGHT_PURPLE + "Frugal " + getNumeral(getFrugal(stack))));
			}
			if (getOccultism(stack) > 0) {
				tooltip.add(new StringTextComponent(TextFormatting.LIGHT_PURPLE + "Occultism " + getNumeral(getOccultism(stack))));  		
			}
        }
    }
    
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity entity, Hand hand) {
    	final ItemStack stack = entity.getHeldItem(hand);
        if (stack.getDamage() >= stack.getMaxDamage()) {
        	return ActionResult.resultFail(stack);
        }
        
        if (!entity.isSwingInProgress) {
            if (!world.isRemote) {
                this.castSpell(world, entity, stack);
            }
            entity.swingArm(hand);
            return ActionResult.resultSuccess(stack);
        }
        return ActionResult.resultPass(stack);
    }

    @Override
    public int getItemEnchantability() {
        return 0;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchant) {
        return false;
    }
    
	@Override
	public int getMaxDamage(ItemStack stack) {
		return 200 + (getFrugal(stack) * 50);
	}
	
	public int countBlessings(final ItemStack stack) {
		return getPotency(stack) + getOccultism(stack) + getFrugal(stack);
	}
	
	protected int getPotency(final ItemStack stack) {
    	if (stack.hasTag() && stack.getTag().contains(WandItem.PORTENCY)) {
			return stack.getTag().getInt(WandItem.PORTENCY);
        }
    	return 0;
    }
    
    protected int getOccultism(final ItemStack stack) {
    	if (stack.hasTag() && stack.getTag().contains(WandItem.OCCULTISM)) {
			return stack.getTag().getInt(WandItem.OCCULTISM);
        }
    	return 0;
    }
    
    protected int getFrugal(final ItemStack stack) {
		if (stack.hasTag() && stack.getTag().contains(WandItem.FRUGAL)) {
			return stack.getTag().getInt(WandItem.FRUGAL);
        }
		return 0;
    }
    
    public void setGlobalWandsCooldown(PlayerEntity player) {
    	player.getCooldownTracker().setCooldown(Registry.BONECHILL_WAND.get(), this.getCooldown(player));
    	player.getCooldownTracker().setCooldown(Registry.SOULFIRE_WAND.get(), this.getCooldown(player));
    }
    
    public int getCooldown(LivingEntity entity) {
    	return EntityUtil.hasWizardHat(entity) ? 10 : 15;
    }
    
    public abstract void castSpell(World world, PlayerEntity player, ItemStack stack);
}

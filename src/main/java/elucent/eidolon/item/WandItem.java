package elucent.eidolon.item;

import elucent.eidolon.Registry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class WandItem extends ItemBase {
    public WandItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getItemEnchantability() {
        return 20;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchant) {
        return super.canApplyAtEnchantingTable(stack, enchant)
            || enchant == Enchantments.UNBREAKING
            || enchant == Enchantments.MENDING;
    }
    
    protected int visPerCast() {
    	return 40 - (getFrugal() * 7);
    }
    
    protected int getMysticality() {
    	return 0;
    }
    
    protected int getFrugal() {
    	return 0;
    }
    
    public void putWandsOnCooldown(PlayerEntity player) {
    	player.getCooldownTracker().setCooldown(Registry.BONECHILL_WAND.get(), this.getCooldown(player));
    	player.getCooldownTracker().setCooldown(Registry.SOULFIRE_WAND.get(), this.getCooldown(player));
    }
    
    public int getCooldown(LivingEntity entity) {
    	final Item hat = entity.getItemStackFromSlot(EquipmentSlotType.HEAD).getItem();
		if (hat == Registry.WARLOCK_HAT.get() || hat == Registry.TOP_HAT.get()) {
			return 10;
		}
    	return 15;
    }
}

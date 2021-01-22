package elucent.eidolon.util;

import elucent.eidolon.Eidolon;
import elucent.eidolon.ForeignItems;
import elucent.eidolon.Registry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;

public class EntityUtil {
    public static final String THRALL_KEY = Eidolon.MODID + ":thrall";

    public static void enthrall(LivingEntity caster, LivingEntity thrall) {
        thrall.getPersistentData().putUniqueId(THRALL_KEY, caster.getUniqueID());
    }

    public static boolean isEnthralled(LivingEntity entity) {
        return entity.getPersistentData().contains(THRALL_KEY);
    }

    public static boolean isEnthralledBy(LivingEntity entity, LivingEntity owner) {
        return entity != null && owner != null && isEnthralled(entity) && entity.getPersistentData().getUniqueId(THRALL_KEY).equals(owner.getUniqueID());
    }
    
    public static boolean hasWizardHat(final LivingEntity entity) {
    	final Item hat = entity.getItemStackFromSlot(EquipmentSlotType.HEAD).getItem();
    	return hat == Registry.WARLOCK_HAT.get() || hat == Registry.TOP_HAT.get();
    }
    
    public static float getItemWarding(Item item) {
    	// WARLOCK
    	if (item == Registry.WARLOCK_HAT.get() || item == Registry.TOP_HAT.get()) {
    		return 0.12F;
    	}
    	
    	if (item == Registry.WARLOCK_CLOAK.get()) {
    		return 0.32F;
    	}
    	
    	if (item == Registry.WARLOCK_BOOTS.get()) {
    		return 0.12F;
    	}
    	
    	//  Ironwood
    	if (item == ForeignItems.IRONWOOD_HELMET) {
    		return 0.04F;
    	}
    	if (item == ForeignItems.IRONWOOD_CHESTPLATE) {
    		return 0.16F;
    	}
    	if (item == ForeignItems.IRONWOOD_LEGGINGS) {
    		return 0.12F;
    	}
    	if (item == ForeignItems.IRONWOOD_BOOTS) {
    		return 0.04F;
    	}
		
    	// Naga 
    	if (item == ForeignItems.NAGA_CHESTPLATE) {
    		return 0.2F;
    	}
    	if (item == ForeignItems.NAGA_LEGGINGS) {
    		return 0.16F;
    	}
    	
    	// Yeti & Steeleaf
    	if (item == ForeignItems.STEELEAF_HELMET || item == ForeignItems.YETI_HELMET) {
    		return 0.08F;
    	}
    	if (item == ForeignItems.STEELEAF_CHESTPLATE || item == ForeignItems.YETI_CHESTPLATE) {
    		return 0.24F;
    	}
    	if (item == ForeignItems.STEELEAF_LEGGINGS || item == ForeignItems.YETI_LEGGINGS) {
    		return 0.2F;
    	}
    	if (item == ForeignItems.STEELEAF_BOOTS || item == ForeignItems.YETI_BOOTS) {
    		return 0.08F;
    	}
    	
    	return 0F;
    }
    
    public static float getWardingMod(LivingEntity entity) {
    	float modifier = 1F;
    	final Item head = entity.getItemStackFromSlot(EquipmentSlotType.HEAD).getItem();
    	final Item chest = entity.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem();
    	final Item legs = entity.getItemStackFromSlot(EquipmentSlotType.LEGS).getItem();
    	final Item feet = entity.getItemStackFromSlot(EquipmentSlotType.FEET).getItem();
    	
    	modifier -= EntityUtil.getItemWarding(head);
    	modifier -= EntityUtil.getItemWarding(chest);
    	modifier -= EntityUtil.getItemWarding(legs);
    	modifier -= EntityUtil.getItemWarding(feet);

    	return modifier;
    }
    
    public static float getCurseMod(LivingEntity entity) {
    	return EntityUtil.getWardingMod(entity);
    }
}

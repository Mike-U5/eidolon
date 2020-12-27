package elucent.eidolon.util;

import elucent.eidolon.Eidolon;
import elucent.eidolon.ForeignItems;
import elucent.eidolon.item.WarlockRobesItem;
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
    
    public static float getWardingMod(LivingEntity entity) {
    	float modifier = 1F;
    	final Item head = entity.getItemStackFromSlot(EquipmentSlotType.HEAD).getItem();
    	final Item chest = entity.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem();
    	final Item legs = entity.getItemStackFromSlot(EquipmentSlotType.LEGS).getItem();
    	final Item feet = entity.getItemStackFromSlot(EquipmentSlotType.FEET).getItem();
    	
    	if (head instanceof WarlockRobesItem || head == ForeignItems.IRONWOOD_HELMET) {
    		modifier -= 0.12F;
    	}
    	
    	if (chest instanceof WarlockRobesItem || chest == ForeignItems.IRONWOOD_CHESTPLATE) {
    		modifier -= 0.24F;
    	}
    	
    	if (legs == ForeignItems.IRONWOOD_LEGGINGS) {
    		modifier -= 0.16F;
    	}
    	
    	if (feet instanceof WarlockRobesItem || feet == ForeignItems.IRONWOOD_BOOTS) {
    		modifier -= 0.12F;
    	}
    	
    	
    	return modifier;
    }
}

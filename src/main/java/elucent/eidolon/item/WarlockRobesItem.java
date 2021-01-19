package elucent.eidolon.item;

import java.util.UUID;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;

import elucent.eidolon.Eidolon;
import elucent.eidolon.Registry;
import elucent.eidolon.item.model.WarlockArmorModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WarlockRobesItem extends ArmorItem {
	private static final AttributeModifier WARLOCK_MOVEMENT_MODIFIER = new AttributeModifier(UUID.fromString("725a1c21-a4d9-46cd-b65e-7a074f738137"), Eidolon.MODID + ":warlock_movement_speed", 0.1F, AttributeModifier.Operation.MULTIPLY_TOTAL);
	private static final int[] MAX_DAMAGE_ARRAY = new int[]{13, 15, 16, 11};

    public static class Material implements IArmorMaterial {
        @Override
        public int getDurability(EquipmentSlotType slot) {
            return MAX_DAMAGE_ARRAY[slot.getIndex()] * 21;
        }

        @Override
        public int getDamageReductionAmount(EquipmentSlotType slot) {
            switch (slot) {
                case CHEST:
                    return 6;
                case HEAD:
                    return 2;
                case FEET:
                    return 2;
                default:
                    return 0;
            }
        }

        @Override
        public int getEnchantability() {
            return 25;
        }

        @Override
        public SoundEvent getSoundEvent() {
            return ArmorMaterial.LEATHER.getSoundEvent();
        }

        @Override
        public Ingredient getRepairMaterial() {
            return Ingredient.fromStacks(new ItemStack(Registry.WICKED_WEAVE.get()));
        }

        @Override
        public String getName() {
            return Eidolon.MODID + ":warlock_robes";
        }

        @Override
        public float getToughness() {
            return 0;
        }

        @Override
        public float getKnockbackResistance() {
            return 0;
        }

        public static final Material INSTANCE = new Material();
    }

    public WarlockRobesItem(EquipmentSlotType slot, Properties builderIn) {
        super(Material.INSTANCE, slot, builderIn);
    }

    WarlockArmorModel model = null;

    @SuppressWarnings({ "rawtypes", "unchecked" })
	@OnlyIn(Dist.CLIENT)
    @Override
    public WarlockArmorModel getArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlotType slot, BipedModel defaultModel) {
        if (model == null) model = new WarlockArmorModel(slot);
        float pticks = Minecraft.getInstance().getRenderPartialTicks();
        float f = MathHelper.interpolateAngle(pticks, entity.prevRenderYawOffset, entity.renderYawOffset);
        float f1 = MathHelper.interpolateAngle(pticks, entity.prevRotationYawHead, entity.rotationYawHead);
        float netHeadYaw = f1 - f;
        float netHeadPitch = MathHelper.lerp(pticks, entity.prevRotationPitch, entity.rotationPitch);
        model.setRotationAngles(entity, entity.limbSwing, entity.limbSwingAmount, entity.ticksExisted + pticks, netHeadYaw, netHeadPitch);
        return model;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        return Eidolon.MODID + ":textures/entity/warlock_robes.png";
    }
    
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType slot) {
    	if (slot == EquipmentSlotType.FEET) {
        	final Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        	builder.put(Attributes.MOVEMENT_SPEED, WARLOCK_MOVEMENT_MODIFIER);
        	return builder.build();
        }
        
        return super.getAttributeModifiers(slot);
    }
}

package elucent.eidolon.item.curio;

import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import elucent.eidolon.Eidolon;
import elucent.eidolon.Registry;
import elucent.eidolon.item.ItemBase;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class WardedMailItem extends ItemBase {
	UUID ATTR_ID = new UUID(7207179127447911419l, 1628308750126455317l);
	
    public WardedMailItem(Properties properties) {
        super(properties);
        //MinecraftForge.EVENT_BUS.addListener(WardedMailItem::onDamage);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT unused) {
        return new EidolonCurio(stack) {
            @Override
            public Multimap<Attribute, AttributeModifier> getAttributeModifiers(String identifier) {
                Multimap<Attribute, AttributeModifier> map = HashMultimap.create();
                map.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(ATTR_ID, Eidolon.MODID + ":basic_ring", 2.0f, AttributeModifier.Operation.ADDITION));
                return map;
            }

            @Override
            public boolean canRightClickEquip() {
                return true;
            }
        };
    }

    /*@SubscribeEvent
    public static void onDamage(LivingAttackEvent event) {
        if (event.getSource().isMagicDamage()) {
            CuriosApi.getCuriosHelper().getEquippedCurios(event.getEntityLiving()).resolve().ifPresent((slots) -> {
                boolean hasMail = false;
                for (int i = 0; i < slots.getSlots(); i ++) {
                    if (slots.getStackInSlot(i).getItem() == Registry.WARDED_MAIL.get()) {
                        event.setCanceled(true);
                        event.getEntityLiving().attackEntityFrom(new DamageSource(event.getSource().getDamageType()), event.getAmount());
                        return;
                    }
                }
            });
        }
    }*/
}

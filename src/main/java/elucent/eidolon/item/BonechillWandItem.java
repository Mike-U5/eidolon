package elucent.eidolon.item;

import java.util.List;

import elucent.eidolon.Registry;
import elucent.eidolon.entity.BonechillProjectileEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BonechillWandItem extends WandItem {
    public BonechillWandItem(Properties builderIn) {
        super(builderIn);
    }

    @Override
    public void castSpell(World world, PlayerEntity player, ItemStack stack) {
    	final Vector3d pos = player.getPositionVec().add(player.getLookVec().scale(0.5)).add(0.5 * Math.sin(Math.toRadians(225 - player.rotationYawHead)), player.getHeight() * 2 / 3, 0.5 * Math.cos(Math.toRadians(225 - player.rotationYawHead)));
        final Vector3d vel = player.getEyePosition(0).add(player.getLookVec().scale(40)).subtract(pos).scale(0.05D);
        
        final BonechillProjectileEntity spell = new BonechillProjectileEntity(Registry.BONECHILL_PROJECTILE.get(), world);
        spell.setPotency(getPotency(stack));
        spell.setOccultism(getOccultism(stack));
        world.addEntity(spell.shoot(
            pos.x, pos.y, pos.z, vel.x, vel.y, vel.z, player.getUniqueID()
        ));
        
        world.playSound(null, pos.x, pos.y, pos.z, Registry.CAST_BONECHILL_EVENT.get(), SoundCategory.NEUTRAL, 0.75f, random.nextFloat() * 0.2f + 0.9f);
        stack.setDamage(this.getDamage(stack) + 1); 
        this.setGlobalWandsCooldown(player);
    }
}

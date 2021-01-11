package elucent.eidolon.potion;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import elucent.eidolon.Eidolon;
import elucent.eidolon.Registry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeEffect;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class WarpedEffect extends Effect implements IForgeEffect {
    static int packColor(int alpha, int red, int green, int blue) {
        return alpha << 0 | red << 0 | green << 0 | blue;
    }

    public WarpedEffect() {
        super(EffectType.NEUTRAL, packColor(0, 0, 0, 0));
        MinecraftForge.EVENT_BUS.addListener(this::awaken);
    }
    
	@SubscribeEvent
	public void awaken(PlayerWakeUpEvent event) {
		event.getPlayer().removePotionEffect(Registry.WARPED_EFFECT.get());
	}
    
    @Override
    public List<ItemStack> getCurativeItems() {
    	return new ArrayList<ItemStack>();
    }

    protected static final ResourceLocation EFFECT_TEXTURE = new ResourceLocation(Eidolon.MODID, "textures/mob_effect/warped.png");

    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, MatrixStack mStack, int x, int y, float z) {
        Minecraft mc = Minecraft.getInstance();
        mc.getTextureManager().bindTexture(EFFECT_TEXTURE);
        gui.blit(mStack, x, y, 0, 0, 18, 18);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderHUDEffect(EffectInstance effect, AbstractGui gui, MatrixStack mStack, int x, int y, float z, float alpha) {
        Minecraft mc = Minecraft.getInstance();
        mc.getTextureManager().bindTexture(EFFECT_TEXTURE);
        gui.blit(mStack, x, y, 0, 0, 18, 18);
    }
}

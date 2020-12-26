package elucent.eidolon.item.model;// Made with Blockbench 3.7.4
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports


import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EquipmentSlotType;

public class TopHatModel extends ArmorModel {
	private final ModelRenderer hat;

	public TopHatModel() {
		super(EquipmentSlotType.HEAD, 64, 32);
		textureWidth = 64;
		textureHeight = 32;

		hat = new ModelRenderer(this);
		hat.setRotationPoint(0.0F, -7.0F, 0.0F);
		bipedHead.addChild(hat);
		setRotationAngle(hat, -0.0873F, 0.0F, 0.0F);
		hat.setTextureOffset(0, 0).addBox(-5.0F, -2.0F, -5.0F, 10.0F, 2.0F, 10.0F, 0.0F, false);
		hat.setTextureOffset(0, 12).addBox(-4.0F, -12.0F, -4.0F, 8.0F, 10.0F, 8.0F, 0.0F, false);
	}

	public void setRotationAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){
		//
	}
}
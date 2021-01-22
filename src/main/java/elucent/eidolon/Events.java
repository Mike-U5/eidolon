package elucent.eidolon;

import java.util.Comparator;
import java.util.List;

import com.mojang.authlib.GameProfile;

import elucent.eidolon.capability.IKnowledge;
import elucent.eidolon.capability.KnowledgeProvider;
import elucent.eidolon.capability.ReputationProvider;
import elucent.eidolon.entity.ai.PriestBarterGoal;
import elucent.eidolon.entity.ai.WitchBarterGoal;
import elucent.eidolon.item.CleavingAxeItem;
import elucent.eidolon.item.CodexItem;
import elucent.eidolon.item.ReaperScytheItem;
import elucent.eidolon.item.WarlockRobesItem;
import elucent.eidolon.network.CrystallizeEffectPacket;
import elucent.eidolon.network.KnowledgeUpdatePacket;
import elucent.eidolon.network.Networking;
import elucent.eidolon.ritual.Ritual;
import elucent.eidolon.spell.Signs;
import elucent.eidolon.tile.GobletTileEntity;
import elucent.eidolon.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.monster.WitchEntity;
import net.minecraft.entity.monster.WitherSkeletonEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Events {
	@SubscribeEvent
	public void attachWorldCaps(AttachCapabilitiesEvent<World> event) {
		if (event.getObject() instanceof World)
			event.addCapability(new ResourceLocation(Eidolon.MODID, "reputation"), new ReputationProvider());
	}

	@SubscribeEvent
	public void attachEntityCaps(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Entity)
			event.addCapability(new ResourceLocation(Eidolon.MODID, "knowledge"), new KnowledgeProvider());
	}

	@SubscribeEvent
	public void onClone(PlayerEvent.Clone event) {
		if (event.isWasDeath()) {
			Capability<IKnowledge> KNOWLEDGE = KnowledgeProvider.CAPABILITY;
			KNOWLEDGE.getStorage().readNBT(KNOWLEDGE, event.getPlayer().getCapability(KNOWLEDGE, null).resolve().get(),
					null, KNOWLEDGE.getStorage().writeNBT(KNOWLEDGE,
							event.getOriginal().getCapability(KNOWLEDGE, null).resolve().get(), null));
			if (!event.getPlayer().world.isRemote) {
				Networking.sendTo(event.getPlayer(), new KnowledgeUpdatePacket(event.getPlayer(), false));
			}
		}
	}

	@SubscribeEvent
	public void onTarget(LivingSetAttackTargetEvent event) {
		if (EntityUtil.isEnthralledBy(event.getEntityLiving(), event.getTarget()))
			((MobEntity) event.getEntityLiving()).setAttackTarget(null);
	}

	@SubscribeEvent
	public void onDeath(LivingDropsEvent event) {
		LivingEntity entity = event.getEntityLiving();
		if (!(entity instanceof MonsterEntity)) {
			World world = entity.world;
			BlockPos pos = entity.getPosition();
			List<GobletTileEntity> goblets = Ritual.getTilesWithinAABB(GobletTileEntity.class, world,
					new AxisAlignedBB(pos.add(-2, -2, -2), pos.add(3, 3, 3)));
			if (goblets.size() > 0) {
				GobletTileEntity goblet = goblets.stream()
						.min(Comparator.comparingDouble((g) -> g.getPos().distanceSq(pos))).get();
				goblet.setEntityType(entity.getType());
			}
		}

		if (entity instanceof WitchEntity || entity instanceof VillagerEntity) {
			if (entity.getHeldItemMainhand().getItem() instanceof CodexItem) {
				event.getDrops().add(new ItemEntity(entity.world, entity.getPosX(), entity.getPosY(), entity.getPosZ(),
						entity.getHeldItemMainhand().copy()));
			}
		}

		if (EntityUtil.isEnthralled(entity)) {
			event.getDrops().clear();
			return;
		}

		if (event.getSource().getTrueSource() != null && event.getSource().getTrueSource() instanceof LivingEntity) {
			LivingEntity source = (LivingEntity) event.getSource().getTrueSource();
			ItemStack held = source.getHeldItemMainhand();
			final boolean warlockBonus = (source.getItemStackFromSlot(EquipmentSlotType.HEAD)
					.getItem() instanceof WarlockRobesItem);

			// Scythe Effect
			if (!entity.world.isRemote && held.getItem() instanceof ReaperScytheItem && entity.isEntityUndead()) {
				event.getDrops().clear();

				int rate = ForgeHooks.getLootingLevel(entity, source, event.getSource());
				if (warlockBonus) {
					rate += 3;
				}

				if (rate > source.world.rand.nextInt(6)) {
					final ItemEntity drop = new ItemEntity(source.world, entity.getPosX(), entity.getPosY(), entity.getPosZ(), new ItemStack(Registry.SOUL_SHARD.get(), 1));
					drop.setDefaultPickupDelay();
					event.getDrops().add(drop);
				}
				
				Networking.sendToTracking(entity.world, entity.getPosition(), new CrystallizeEffectPacket(entity.getPosition()));
			}

			// Cleaving Axe Event
			if (!entity.world.isRemote && held.getItem() instanceof CleavingAxeItem) {
				int looting = ForgeHooks.getLootingLevel(entity, source, event.getSource());
				ItemStack head = ItemStack.EMPTY;
				if (entity instanceof WitherSkeletonEntity)
					head = new ItemStack(Items.WITHER_SKELETON_SKULL);
				else if (entity instanceof SkeletonEntity)
					head = new ItemStack(Items.SKELETON_SKULL);
				else if (entity instanceof ZombieEntity)
					head = new ItemStack(Items.ZOMBIE_HEAD);
				else if (entity instanceof CreeperEntity)
					head = new ItemStack(Items.CREEPER_HEAD);
				else if (entity instanceof EnderDragonEntity)
					head = new ItemStack(Items.DRAGON_HEAD);
				else if (entity instanceof PlayerEntity) {
					head = new ItemStack(Items.PLAYER_HEAD);
					GameProfile gameprofile = ((PlayerEntity) entity).getGameProfile();
					head.getOrCreateTag().put("SkullOwner", NBTUtil.writeGameProfile(new CompoundNBT(), gameprofile));
				}

				if (!head.isEmpty()) {
					int chance = 40;
					chance -= (looting * 4);

					if (warlockBonus) {
						chance -= 8;
					}

					if (entity.world.rand.nextInt(chance) == 0) {
						ItemEntity drop = new ItemEntity(source.world, entity.getPosX(), entity.getPosY(),
								entity.getPosZ(), head);
						drop.setDefaultPickupDelay();
						event.getDrops().add(drop);
					}
				}
			}
		}

		// Entities killed by rituals do not have normal drops
		if (event.getSource().getDamageType() == Registry.RITUAL_DAMAGE.getDamageType()
				&& !(entity instanceof PlayerEntity)) {
			event.getDrops().clear();
		}
	}

	@SubscribeEvent
	public void registerSpawns(BiomeLoadingEvent ev) {
		if (ev.getCategory() == Biome.Category.NETHER) {
			ev.getSpawns().withSpawner(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(
					Registry.ZOMBIE_BRUTE.get(), Config.ZOMBIE_BRUTE_SPAWN_WEIGHT.get(), 1, 2));
		}

		if (ev.getCategory() != Biome.Category.MUSHROOM && ev.getCategory() != Biome.Category.OCEAN
				&& ev.getCategory() != Biome.Category.NETHER && ev.getCategory() != Biome.Category.THEEND) {
			ev.getSpawns().withSpawner(EntityClassification.MONSTER,
					new MobSpawnInfo.Spawners(Registry.WRAITH.get(), Config.WRAITH_SPAWN_WEIGHT.get(), 1, 2));
		}
	}

	@SubscribeEvent
	public void registerCustomAI(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof LivingEntity && !event.getWorld().isRemote) {
			if (event.getEntity() instanceof PlayerEntity) {
				Networking.sendTo((PlayerEntity) event.getEntity(),
						new KnowledgeUpdatePacket((PlayerEntity) event.getEntity(), false));
			}
			if (event.getEntity() instanceof WitchEntity) {
				((WitchEntity) event.getEntity()).goalSelector.addGoal(1,
						new WitchBarterGoal((WitchEntity) event.getEntity(),
								(stack) -> stack.getItem() == Registry.CODEX.get(),
								(stack) -> CodexItem.withSign(stack, Signs.WICKED_SIGN)));
			}
			if (event.getEntity() instanceof VillagerEntity) {
				((VillagerEntity) event.getEntity()).goalSelector.addGoal(1,
						new PriestBarterGoal((VillagerEntity) event.getEntity(),
								(stack) -> stack.getItem() == Registry.CODEX.get(),
								(stack) -> CodexItem.withSign(stack, Signs.SACRED_SIGN)));
			}
		}
	}

	@SubscribeEvent
	public void addTooltips(ItemTooltipEvent event) {
		final int val = (int) (EntityUtil.getItemWarding(event.getItemStack().getItem()) / 0.04F);
		if (val > 0) {
			final Style style = Style.EMPTY.setColor(Color.fromTextFormatting(TextFormatting.BLUE));
			final IFormattableTextComponent txt = new StringTextComponent("+" + val + " Warding").setStyle(style);
			event.getToolTip().add(event.getToolTip().size() - 2, txt);
		}
	}

	private boolean isMagical(DamageSource src) {
		if (src.isMagicDamage()) {
			return true;
		}
		
		if (src.getDamageType() == DamageSource.MAGIC.getDamageType()) {
			return true;
		}
		
		if (src.getDamageType() == "indirectMagic") {
			return true;
		}

		if (src.getDamageType() == DamageSource.WITHER.getDamageType()) {
			return true;
		}

		if (src.getDamageType() == Registry.FROST_DAMAGE.getDamageType()) {
			return true;
		}

		return false;
	}

	@SubscribeEvent
	public void onLivingHurt(LivingHurtEvent event) {
		if (event.getEntityLiving() instanceof PlayerEntity) {
			Minecraft.getInstance().player.sendChatMessage(event.getSource().getDamageType());
		}
		
		if (this.isMagical(event.getSource())) {
			// Warding Damage Reduction
			final float oldDmg = event.getAmount();
			final float mod = EntityUtil.getWardingMod(event.getEntityLiving());
			final float newDmg = oldDmg * mod;
			event.setAmount(newDmg);
			
			if (event.getSource().getTrueSource() instanceof LivingEntity) {
				final LivingEntity attacker = (LivingEntity) event.getSource().getTrueSource();
				
				// Warlock Hat Damage boosts the power of wither
				if (attacker.getItemStackFromSlot(EquipmentSlotType.HEAD).getItem() instanceof WarlockRobesItem && event.getSource() == DamageSource.WITHER) {
					event.setAmount(event.getAmount() * 1.5F);
				}	
				
				// Warlock Body Sapping Boost
				if (attacker.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() instanceof WarlockRobesItem && event.getSource().getDamageType() == DamageSource.WITHER.getDamageType()) {
					attacker.heal(event.getAmount() / 2);
				}
			}
		}
	}

	/**@SubscribeEvent
	public void onGetSpeedFactor(SpeedFactorEvent event) {
		// Warlock Boots speed effect
		if (event.getSpeedFactor() < 1.0f && event.getEntity() instanceof LivingEntity
				&& ((LivingEntity) event.getEntity()).getItemStackFromSlot(EquipmentSlotType.FEET)
						.getItem() instanceof WarlockRobesItem) {
			float diff = 1.0f - event.getSpeedFactor();
			event.setSpeedFactor(1.0f - diff / 2);
		}
	}**/
}

package elucent.eidolon.spell;

import java.util.Comparator;
import java.util.List;

import elucent.eidolon.Eidolon;
import elucent.eidolon.capability.ReputationProvider;
import elucent.eidolon.deity.Deities;
import elucent.eidolon.deity.Deity;
import elucent.eidolon.item.WandItem;
import elucent.eidolon.network.MagicBurstEffectPacket;
import elucent.eidolon.network.Networking;
import elucent.eidolon.ritual.Ritual;
import elucent.eidolon.tile.EffigyTileEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class WandImbueSpell extends StaticSpell {
	private final String imbueTag;
	private final Sign primarySign;
	private final Sign secondarySign;
	
    public WandImbueSpell(String imbueTag, Sign... signs) {
    	super(new ResourceLocation(Eidolon.MODID, imbueTag), signs);
    	this.imbueTag = imbueTag;
    	this.primarySign = signs[0];
    	this.secondarySign = signs[1];
    }

    @Override
    public boolean canCast(World world, BlockPos pos, PlayerEntity player) {
        return true;
    }

    @Override
    public void cast(World world, BlockPos pos, PlayerEntity player) {
    	final RayTraceResult ray = world.rayTraceBlocks(new RayTraceContext(player.getEyePosition(0), player.getEyePosition(0).add(player.getLookVec().scale(4)), RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, player));
    	final Vector3d v = ray.getType() == RayTraceResult.Type.BLOCK ? ray.getHitVec() : player.getEyePosition(0).add(player.getLookVec().scale(4));
    	final List<ItemEntity> items = world.getEntitiesWithinAABB(ItemEntity.class, new AxisAlignedBB(v.x - 1.5, v.y - 1.5, v.z - 1.5, v.x + 1.5, v.y + 1.5, v.z + 1.5));

    	if (items.size() == 1 && items.get(0).getItem().getItem() instanceof WandItem) {
    		if (!world.isRemote) {
    			final ItemStack stack = items.get(0).getItem();
    			final WandItem wand = (WandItem)stack.getItem();
    			
    			// Blessing Check
    			final int blessings = wand.countBlessings(stack);
    			if (blessings >= 5) {
    				player.sendStatusMessage(new TranslationTextComponent("This wand cannot receive any more blessings."), true);
    				return;
    			}
    			
    			// Check if Capacity is high enough
    			final AltarInfo altar = getAltarInfo(world, pos);
    			if (blessings > altar.getCapacity()) {
    				player.sendStatusMessage(new TranslationTextComponent("Your altar is lacking the capacity needed to perform this blessing."), true);
    			}
    			
    			// Attempt to drain favor
    			world.getCapability(ReputationProvider.CAPABILITY, null).ifPresent((provider) -> {
                    final Deity deity = Deities.DARK_DEITY;
                    final double oldRep = provider.getReputation(player, deity.getId());
                    
                    final double favorCost = Math.pow(2D, 1 + blessings);
                    
                    if (oldRep < favorCost) {
                    	player.sendStatusMessage(new TranslationTextComponent("The dark god demands more favor."), true);
                    }
                    
                    final double newRep = oldRep - favorCost;
                    provider.setReputation(player, deity.getId(), newRep);
                    deity.onReputationChange(player, provider, oldRep, newRep);
                    
        			// Apply Imbue
        			final int tier = stack.getOrCreateTag().getInt(imbueTag);
        			stack.getOrCreateTag().putInt(imbueTag, Math.min(tier + 1, 5));
        			
        			final Vector3d p = items.get(0).getPositionVec();
                    items.get(0).setDefaultPickupDelay();
                    Networking.sendToTracking(world, items.get(0).getPosition(), new MagicBurstEffectPacket(p.x, p.y, p.z, primarySign.getColor(), secondarySign.getColor()));
                });
            } else {
                world.playSound(player, player.getPosition(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.NEUTRAL, 1.0F, 0.6F + world.rand.nextFloat() * 0.2F);
            }
        }
    }
    
    private AltarInfo getAltarInfo(final World world, final BlockPos pos) {
    	final List<EffigyTileEntity> effigies = Ritual.getTilesWithinAABB(EffigyTileEntity.class, world, new AxisAlignedBB(pos.add(-4, -4, -4), pos.add(5, 5, 5)));
    	final EffigyTileEntity effigy = effigies.stream().min(Comparator.comparingDouble((e) -> e.getPos().distanceSq(pos))).get();
    	return AltarInfo.getAltarInfo(world, effigy.getPos());
    }
}

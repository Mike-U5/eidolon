package elucent.eidolon.entity.ai;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.monster.WitchEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class PriestBarterGoal extends GenericBarterGoal<VillagerEntity> {
    public PriestBarterGoal(VillagerEntity entity, Predicate<ItemStack> valid, Function<ItemStack, ItemStack> result) {
        super(entity, valid, result);
    }

    @Override
    public boolean shouldExecute() {
        if (entity.getVillagerData().getProfession() != VillagerProfession.CLERIC) return false;
        return super.shouldExecute();
    }

    @Override
    public void tick() {
        super.tick();
    }
}

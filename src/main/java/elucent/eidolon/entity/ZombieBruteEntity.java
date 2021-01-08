package elucent.eidolon.entity;

import javax.annotation.Nullable;

import elucent.eidolon.Registry;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

public class ZombieBruteEntity extends MonsterEntity {
    public ZombieBruteEntity(EntityType<ZombieBruteEntity> type, World worldIn) {
        super(type, worldIn);
        registerGoals();
    }

    @Override
    public CreatureAttribute getCreatureAttribute() {
        return CreatureAttribute.UNDEAD;
    }

    @Override
    public boolean isEntityUndead() {
        return true;
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.applyEntityAI();
    }

    public static AttributeModifierMap createAttributes() {
        return MonsterEntity.func_234295_eP_()
            .createMutableAttribute(Attributes.MAX_HEALTH, 40.0D)
            .createMutableAttribute(Attributes.MOVEMENT_SPEED, (double)0.23F)
            .createMutableAttribute(Attributes.ATTACK_DAMAGE, 5.0D)
            .createMutableAttribute(Attributes.KNOCKBACK_RESISTANCE, 0.4D)
            .createMutableAttribute(Attributes.ATTACK_KNOCKBACK, 2.0D)
            .createMutableAttribute(Attributes.ARMOR, 4.0D)
            .create();
    }
    
    public boolean isImmuneToFire() {
    	return true;
    }

    protected void applyEntityAI() {
        this.goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillagerEntity.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolemEntity.class, true));
    }

    @Override
    public int getExperiencePoints(PlayerEntity player) {
        return 8;
    }

    @Nullable
    public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
	   // Brute spawns with three random potion effects
       final int[] effects = {-1, -1, -1, -1};
	   for (int i = 0; i < 3; i++) {
		   final int rng = this.world.rand.nextInt(4);
		   effects[rng]++;
	   }
	   
	   // Strength
	   if (effects[0] >= 0) {
		   this.addPotionEffect(new EffectInstance(Effects.STRENGTH, Integer.MAX_VALUE, effects[0]));
	   }
	   // Regeneration
	   if (effects[1] >= 0) {
		   this.addPotionEffect(new EffectInstance(Effects.REGENERATION, Integer.MAX_VALUE, effects[1]));
	   }
	   // Resistance
	   if (effects[2] >= 0) {
		   this.addPotionEffect(new EffectInstance(Effects.RESISTANCE, Integer.MAX_VALUE, effects[2]));
	   }
	   // Speed
	   if (effects[3] >= 0) {
		   this.addPotionEffect(new EffectInstance(Effects.SPEED, Integer.MAX_VALUE, effects[3]));
	   }
	   
       return super.onInitialSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
    }

    @Override
    public SoundEvent getDeathSound() {
        return Registry.BRUTE_DEATH.get();
    }

    @Override
    public SoundEvent getAmbientSound() {
        return Registry.BRUTE_LIVING.get();
    }

    @Override
    public SoundEvent getHurtSound(DamageSource source) {
        return Registry.BRUTE_HURT.get();
    }
}

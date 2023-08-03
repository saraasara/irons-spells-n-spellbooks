package io.redspace.ironsspellbooks.entity.mobs.keeper;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.mobs.AnimatedAttacker;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.goals.AttackAnimationData;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

import javax.annotation.Nullable;

public class KeeperEntity extends AbstractSpellCastingMob implements Enemy, AnimatedAttacker {

    //private static final EntityDataAccessor<Integer> DATA_ATTACK_TYPE = SynchedEntityData.defineId(KeeperEntity.class, EntityDataSerializers.INT);

    public enum AttackType {
        //data measured from blockbench
        Double_Slash(43, "sword_double_slash", 13, 29),
        //Triple_Slash(41, "sword_triple_slash", 11, 21, 35),
        //Slash_Stab(38, "sword_slash_stab", 13, 33),
        Single_Upward(26, "sword_single_upward", 13),
        Single_Horizontal(28, "sword_single_horizontal", 12),
        Single_Horizontal_Fast(24, "sword_single_horizontal_fast", 12),
        Single_Stab(21, "sword_stab", 11),
        Lunge(96, "sword_lunge", 65, 66, 67, 68, 69, 70);

        AttackType(int lengthInTicks, String animationId, int... attackTimestamps) {
            this.data = new AttackAnimationData(lengthInTicks, animationId, attackTimestamps);
        }

        public final AttackAnimationData data;

    }

    public KeeperEntity(EntityType<? extends AbstractSpellCastingMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        xpReward = 25;
        maxUpStep = 1f;

    }

    public KeeperEntity(Level pLevel) {
        this(EntityRegistry.KEEPER.get(), pLevel);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new KeeperAnimatedWarlockAttackGoal(this, 1f, 10, 30, 3.5f));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, true, (entity) -> !(entity instanceof KeeperEntity)));

        //this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        //this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundRegistry.KEEPER_HURT.get();
    }

    protected SoundEvent getDeathSound() {
        return SoundRegistry.KEEPER_DEATH.get();
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        RandomSource randomsource = pLevel.getRandom();
        this.populateDefaultEquipmentSlots(randomsource, pDifficulty);
        return pSpawnData;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource pRandom, DifficultyInstance pDifficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemRegistry.TEST_CLAYMORE.get()));
//        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemRegistry.WANDERING_MAGICIAN_ROBE.get()));
//        this.setDropChance(EquipmentSlot.CHEST, 0.0F);
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.FOLLOW_RANGE, 25.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.ATTACK_KNOCKBACK, 2.0)
                .add(Attributes.MOVEMENT_SPEED, .185);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }


    private final AnimationController<KeeperEntity> meleeController = new AnimationController<>(this, "keeper_animations", 0, this::predicate);
    private final AnimationBuilder deflect_animation = new AnimationBuilder().addAnimation("sword_deflect_1", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
//
//    @Override
//    public boolean hurt(DamageSource pSource, float pAmount) {
//        if (pSource.getDirectEntity() instanceof Projectile projectile) {
//            if (random.nextFloat() < .8f) {
//                this.animationToPlay = deflect_animation;
//                if (pSource.getEntity() != null) {
//                    var entity = pSource.getEntity();
//                    double d0 = entity.getX() - this.getX();
//                    double d1 = entity.getZ() - this.getZ();
//                    float yRot = (float) (Mth.atan2(d1, d0) * (double) (180F / (float) Math.PI)) - 90.0F;
//                    this.setYBodyRot(yRot);
//                    this.setYHeadRot(yRot);
//                    this.setYRot(yRot);
//                    this.setOldPosAndRot();
//                }
//                return false;
//            }
//        }
//        return super.hurt(pSource, pAmount);
//    }

    @Override
    protected void playStepSound(BlockPos pPos, BlockState pState) {
        super.playStepSound(pPos, pState);
        if (!pState.getMaterial().isLiquid()) {
            IronsSpellbooks.LOGGER.debug("Stepsound move speed: {}", this.getDeltaMovement().horizontalDistanceSqr());
            IronsSpellbooks.LOGGER.debug("Stepsound walk anim state: {}", this.animationPosition - this.animationSpeed);
            this.playSound(SoundRegistry.KEEPER_STEP.get(), .25f, 1f);
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource pSource) {
        return super.isInvulnerableTo(pSource) || pSource.isFall();
    }

    AnimationBuilder animationToPlay = null;

    @Override
    public void playAnimation(int animationId) {
        if (animationId >= 0 && animationId < AttackType.values().length)
            animationToPlay = new AnimationBuilder().addAnimation(AttackType.values()[animationId].data.animationId, ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    }

    private PlayState predicate(AnimationEvent<KeeperEntity> animationEvent) {
        var controller = animationEvent.getController();

        if (this.animationToPlay != null) {
            controller.markNeedsReload();
            controller.setAnimation(animationToPlay);
            animationToPlay = null;
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(meleeController);
        super.registerControllers(data);
    }


    @Override
    public boolean isAnimating() {
        return meleeController.getAnimationState() != AnimationState.Stopped || super.isAnimating();
    }

    @Override
    public boolean shouldAlwaysAnimateLegs() {
        return false;
    }

    //    @Override
//    public boolean doHurtTarget(Entity pEntity) {
//        level.playSound(null, getX(), getY(), getZ(), SoundRegistry.DEAD_KING_HIT.get(), SoundSource.HOSTILE, 1, 1);
//        return super.doHurtTarget(pEntity);
//    }

}

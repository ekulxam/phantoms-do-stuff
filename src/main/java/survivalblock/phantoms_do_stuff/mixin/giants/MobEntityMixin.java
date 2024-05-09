package survivalblock.phantoms_do_stuff.mixin.giants;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import survivalblock.phantoms_do_stuff.access.BreakDoorsAccess;
import survivalblock.phantoms_do_stuff.common.PhantomsDoStuffConfig;
import survivalblock.phantoms_do_stuff.common.entity.GiantAttackGoal;

import java.time.LocalDate;
import java.time.temporal.ChronoField;

@SuppressWarnings("UnreachableCode")
@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {
    @Shadow @Final public GoalSelector goalSelector;

    @Shadow @Final public GoalSelector targetSelector;

    @Shadow @Final protected float[] armorDropChances;

    @Shadow protected abstract void initEquipment(Random random, LocalDifficulty localDifficulty);

    @Shadow protected abstract void updateEnchantments(Random random, LocalDifficulty localDifficulty);

    @Shadow public abstract void setCanPickUpLoot(boolean canPickUpLoot);

    @Shadow protected abstract boolean isAffectedByDaylight();

    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initGoals", at = @At("HEAD"))
    private void initGiantGoals(CallbackInfo ci){
        if (!((MobEntity) (Object) this instanceof GiantEntity giant)) {
            return;
        }
        if (!PhantomsDoStuffConfig.returnGiantAI) {
            return;
        }
        this.goalSelector.add(4, new ZombieEntity(giant.getWorld()).new DestroyEggGoal(giant, 1.0, 3));
        this.goalSelector.add(8, new LookAtEntityGoal(giant, PlayerEntity.class, 8.0f));
        this.goalSelector.add(8, new LookAroundGoal(giant));
        // initCustomGoals in ZombieEntity
        this.goalSelector.add(2, new GiantAttackGoal(giant, 1.0, false));
        this.goalSelector.add(6, new MoveThroughVillageGoal(giant, 1.0, true, 4, () -> ((BreakDoorsAccess) giant).phantoms_do_stuff$canBreakDoors()));
        this.goalSelector.add(7, new WanderAroundFarGoal(giant, 1.0));
        this.targetSelector.add(1, new RevengeGoal(giant).setGroupRevenge(ZombifiedPiglinEntity.class));
        this.targetSelector.add(2, new ActiveTargetGoal<>(giant, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(giant, MerchantEntity.class, false));
        this.targetSelector.add(3, new ActiveTargetGoal<>(giant, IronGolemEntity.class, true));
        this.targetSelector.add(5, new ActiveTargetGoal<>(giant, TurtleEntity.class, 10, true, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
    }


    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    private void writeBreakDoorsToNbt(NbtCompound nbt, CallbackInfo ci){
        if (!((MobEntity) (Object) this instanceof GiantEntity giant)) {
            return;
        }
        if (!PhantomsDoStuffConfig.returnGiantAI) {
            return;
        }
        nbt.putBoolean("CanBreakDoors", ((BreakDoorsAccess) giant).phantoms_do_stuff$canBreakDoors());
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void readBreakDoorsToNbt(NbtCompound nbt, CallbackInfo ci){
        if (!((MobEntity) (Object) this instanceof GiantEntity giant)) {
            return;
        }
        if (nbt.contains("CanBreakDoors")) {
            ((BreakDoorsAccess) giant).phantoms_do_stuff$setCanBreakDoors(nbt.getBoolean("CanBreakDoors"));
        }
    }

    @SuppressWarnings("RedundantExplicitChronoField")
    @ModifyReturnValue(method = "initialize", at = @At("RETURN"))
    private EntityData initializeGiant(EntityData original, ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt){
        if (!(((MobEntity) (Object) this instanceof GiantEntity giant))) {
            return original;
        }
        if (!PhantomsDoStuffConfig.returnGiantAI) {
            return original;
        }
        Random random = world.getRandom();
        float f = difficulty.getClampedLocalDifficulty();
        this.setCanPickUpLoot(random.nextFloat() < 0.55f * f);
        if (original != null) {
            ((BreakDoorsAccess) giant).phantoms_do_stuff$setCanBreakDoors(((BreakDoorsAccess) giant).phantoms_do_stuff$shouldBreakDoors() && random.nextFloat() < f * 0.1f);
            this.initEquipment(random, difficulty);
            this.updateEnchantments(random, difficulty);
        }
        if (this.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
            LocalDate localDate = LocalDate.now();
            int day = localDate.get(ChronoField.DAY_OF_MONTH);
            int month = localDate.get(ChronoField.MONTH_OF_YEAR);
            if (month == 10 && day == 31 && random.nextFloat() < 0.25f) { // Halloween
                this.equipStack(EquipmentSlot.HEAD, new ItemStack(random.nextFloat() < 0.1f ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
                this.armorDropChances[EquipmentSlot.HEAD.getEntitySlotId()] = 0.0f;
            }
        }
        return original;
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void daylightGiant(CallbackInfo ci){
        if (!((MobEntity) (Object) this instanceof GiantEntity giant)) {
            return; // so many guard clauses lol
        }
        if (!PhantomsDoStuffConfig.giantsBurnInDaylight) {
            return;
        }
        if (!this.isAlive()) {
            return;
        }
        boolean shouldBurn = ((BreakDoorsAccess) giant).phantoms_do_stuff$burnsInDaylight() && this.isAffectedByDaylight();
        if (!shouldBurn) {
            return;
        }
        ItemStack itemStack = this.getEquippedStack(EquipmentSlot.HEAD);
        if (!itemStack.isEmpty()) {
            if (itemStack.isDamageable()) {
                itemStack.setDamage(itemStack.getDamage() + this.random.nextInt(2));
                if (itemStack.getDamage() >= itemStack.getMaxDamage()) {
                    this.sendEquipmentBreakStatus(EquipmentSlot.HEAD);
                    this.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
                }
            }
            shouldBurn = false;
        }
        if (shouldBurn) {
            this.setOnFireFor(8);
        }
    }

    @ModifyReturnValue(method = "tryAttack", at = @At("RETURN"))
    private boolean setGiantTargetOnFire(boolean original, Entity target){
        if (!((MobEntity) (Object) this instanceof GiantEntity)) {
            return original; // so many guard clauses lol
        }
        if (original) {
            float f = this.getWorld().getLocalDifficulty(this.getBlockPos()).getLocalDifficulty();
            if (this.getMainHandStack().isEmpty() && this.isOnFire() && this.random.nextFloat() < f * 0.3f) {
                target.setOnFireFor(4 * (int) f); // 2x zombie
            }
        }
        return original;
    }
}

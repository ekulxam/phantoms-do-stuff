package survivalblock.phantoms_do_stuff.mixin.phantoms;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalEntityTypeTags;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import survivalblock.phantoms_do_stuff.common.PhantomsDoStuff;
import survivalblock.phantoms_do_stuff.common.PhantomsDoStuffConfig;
import survivalblock.phantoms_do_stuff.access.RidingTimeAccess;

import java.util.*;

@SuppressWarnings("UnreachableCode")
@Debug(export = true)
@Mixin(PhantomEntity.class)
public abstract class PhantomEntityMixin extends FlyingEntity {

	@Shadow public abstract int getPhantomSize();

	@Unique
	private static final UUID MAX_HEALTH_ID = UUID.fromString("0edc21a9-6d1f-4a6b-ad21-50215b61b6ba"); // jeremythephantom's UUID
	@Unique
	private static final int NEW_SMALLEST_SIZE = -4;

	public PhantomEntityMixin(EntityType<? extends PhantomEntity> entityType, World world) {
		super(entityType, world); // tell the compiler to stop being annoying
	}
	/**
	 * Drops passengers sometimes and randomly.
	 * Phantoms are at least 3x likely (by likely I mean run a check 3x more frequently)
	 * 	to drop their passengers while over the world height limit
	 * Bigger phantoms are less likely to drop their passengers
	 */
	@Inject(method = "tick", at = @At("HEAD"))
	private void dropPassenger(CallbackInfo ci){
        //noinspection ConstantValue
        if (PhantomsDoStuffConfig.phantomHealthScalesWithSize && PhantomsDoStuff.isABigPhantom((PhantomEntity) (Object) this) && !this.isPersistent()) {
			this.setPersistent();
		}
		if (PhantomsDoStuffConfig.phantomsPhaseThroughWalls) {
			this.noClip = true;
		}
		if(!PhantomsDoStuffConfig.phantomsDropPassengers){
			return;
		}
		List<Entity> passengerList = new ArrayList<>(this.getPassengerList());
		for (Iterator<Entity> iterator = passengerList.iterator(); iterator.hasNext(); ) {
			Entity passenger = iterator.next();
			if (!passenger.hasVehicle() || !Objects.equals(passenger.getVehicle(), this)) {
				iterator.remove();
				continue;
			}
			World world = passenger.getWorld();
			if(world.isClient()){
				continue;
			}
			int ridingTime = ((RidingTimeAccess) passenger).phantoms_do_stuff$getRidingTime();
			if (ridingTime != 0 && ((!world.isInBuildLimit(passenger.getBlockPos()) && ridingTime % 25 == 0) || ridingTime % (100 + this.getPhantomSize() * 4) == 0)) {
				int shouldDrop = MathHelper.nextBetween(world.getRandom(), 0, 1);
				if(shouldDrop == 0){
					passenger.stopRiding();
					iterator.remove();
                }
            }
		}
	}

	@Inject(method = "tick", at = @At("RETURN"))
	private void undoNoClip(CallbackInfo ci){
		if (PhantomsDoStuffConfig.phantomsPhaseThroughWalls) {
			this.noClip = false;
		}
	}

	@ModifyExpressionValue(method = "onSizeChanged", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/PhantomEntity;getPhantomSize()I"))
	private int noInverseDamage(int original){
		return Math.max(-5, original);
	}

	/**
	 * Increases phantom health with size
	 */
	@Inject(method = "onSizeChanged()V", at = @At("TAIL"))
	private void updateMaxHealth(CallbackInfo ci){
		if(!PhantomsDoStuffConfig.phantomHealthScalesWithSize){
			return;
		}
		Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;
		ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
		builder.put(EntityAttributes.GENERIC_MAX_HEALTH, new EntityAttributeModifier(MAX_HEALTH_ID, "phantom heal increase", this.getPhantomSize(), EntityAttributeModifier.Operation.ADDITION));
		float saveHealthDiff = (float) this.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) - this.getHealth();
		attributeModifiers = builder.build();
		this.getAttributes().addTemporaryModifiers(attributeModifiers);
		this.setHealth((float) (this.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH) - saveHealthDiff));
	}

	@ModifyExpressionValue(method = "setPhantomSize", at = @At(value = "CONSTANT", args = "intValue=0"))
	private int removeMinimumPhantomSize(int original){
		return NEW_SMALLEST_SIZE;
	}

	@Mixin(targets = "net.minecraft.entity.mob.PhantomEntity$PhantomMoveControl")
	abstract static class PhantomMoveControlMixin extends MoveControl {

		public PhantomMoveControlMixin(MobEntity owner) {
			super(owner);
		}

		/**
		 * Attempts to slightly increase the phantom's speed, smaller = faster
		 * @param instance the phantom
		 * @param vec3d the original velocity being applied to the phantom
		 * @param original the original operation (setVelocity)
		 */
		@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/PhantomEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"))
		private void iAmSpeed(PhantomEntity instance, Vec3d vec3d, Operation<Void> original){
			Entity entity = this.entity;
			if (entity instanceof PhantomEntity phantom && PhantomsDoStuffConfig.slightlyFasterPhantoms) {
				int invertedSizeScale = 64 - phantom.getPhantomSize();
				float multiplier = 1f + (float) ((float) invertedSizeScale / 2048);
				original.call(instance, vec3d.multiply(multiplier));
				return;
			}
			original.call(instance, vec3d);
			return;
		}
	}

	@Mixin(targets = "net.minecraft.entity.mob.PhantomEntity$SwoopMovementGoal")
	abstract static class SwoopMovementGoalMixin {

		/**
		 * Prevents the phantom from attacking its passengers
		 * @param instance the phantom
		 * @param entity the passenger that would be attacked
		 * @return false if the entity is a passenger
		 */
		@WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/PhantomEntity;tryAttack(Lnet/minecraft/entity/Entity;)Z"))
		private boolean attackCorrectly(PhantomEntity instance, Entity entity){
			List<Entity> passengerList = new ArrayList<>(instance.getPassengerList());
			for (Iterator<Entity> iterator = passengerList.iterator(); iterator.hasNext(); ) {
				Entity passenger = iterator.next();
				if(Objects.equals(passenger, entity)){
					iterator.remove();
					return false;
				}
			}
			return true;
		}

		/**
		 * Makes the phantom's target its passenger if the phantom's attack was successful
		 * @param attacker the phantom
		 * @param target the target to become passenger
		 * @param original the original method call (did the attack succeed?)
		 * @return original
		 */
		@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/PhantomEntity;tryAttack(Lnet/minecraft/entity/Entity;)Z"))
        private boolean youRaiseMeUp(PhantomEntity attacker, Entity target, Operation<Boolean> original) {
			boolean result = original.call(attacker, target);
			if(result && !Objects.equals(target.getVehicle(), attacker)){
				if (!(target.getType().isIn(ConventionalEntityTypeTags.BOSSES))) {
					((RidingTimeAccess) target).phantoms_do_stuff$resetRidingTime();
					target.startRiding(attacker, true); // become passenger
				}
				if (attacker.getPhantomSize() <= 0 || !PhantomsDoStuffConfig.duplicatingPhantoms) {
					return true;
				}
				if (attacker.getWorld() instanceof ServerWorld serverWorld && (target.isOnGround() || (target instanceof LivingEntity living && living.isFallFlying()))) {
					int count = serverWorld.getRandom().nextBetween(1, 10);
					int numberOfPhantoms = 3;
					if(count <= 5) {
						numberOfPhantoms = 1;
					} else if(count <= 7) {
						numberOfPhantoms = 0;
					} else if (count <= 9) {
						numberOfPhantoms = 9;
					}
					for(int i = 0; i < numberOfPhantoms; i++) {
						this.spawnBabyPhantom(attacker);
					}
				}
				return true;
			}
			return result;
		}

		/**
		 * Modifies the phantom swoop goal so that they no longer stop attacking when damaged
		 * @param original The original hurtTime
		 * @return The hurtTime modified
		 */
		@ModifyExpressionValue(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/mob/PhantomEntity;hurtTime:I", opcode = Opcodes.GETFIELD, ordinal = 0))
		private int noMoreHurtCancelAttack(int original){
			return PhantomsDoStuffConfig.noCancellingPhantomAttacks ? -1 : original;
		}

		@Unique
		private void spawnBabyPhantom(PhantomEntity attacker){
			try {
				if (!(attacker.getWorld() instanceof ServerWorld serverWorld)) {
					return;
				}
				PhantomEntity babyPhantom = EntityType.PHANTOM.create(serverWorld);
				if (babyPhantom == null) {
					return;
				}
				BlockPos blockPos = attacker.getBlockPos();
				babyPhantom.refreshPositionAndAngles(blockPos, 0.0f, 0.0f);
				NbtCompound nbt = new NbtCompound();
				attacker.writeNbt(nbt);
				nbt.remove(Entity.UUID_KEY);
				babyPhantom.readNbt(nbt);
				nbt = new NbtCompound();
				attacker.writeCustomDataToNbt(nbt);
				babyPhantom.readCustomDataFromNbt(nbt);
				babyPhantom.initialize(serverWorld, serverWorld.getLocalDifficulty(blockPos), SpawnReason.NATURAL, null, null);
				if (attacker.getServer() == null) {
					return;
				}
				ServerScoreboard scoreboard = attacker.getServer().getScoreboard();
				Team team = scoreboard.getPlayerTeam(attacker.getEntityName());
				if (team != null) {
					scoreboard.addPlayerToTeam(babyPhantom.getEntityName(), team);
				}
				babyPhantom.setPhantomSize(NEW_SMALLEST_SIZE);
				serverWorld.spawnEntity(babyPhantom);
			} catch (Exception e) {
				PhantomsDoStuff.LOGGER.error("An exception occurred while trying to duplicate phantoms, not spawning this phantom", e);
			}
		}
	}

	@Mixin(targets = "net.minecraft.entity.mob.PhantomEntity$FindTargetGoal")
	abstract static class FindTargetGoalMixin extends Goal {
		@Shadow @Final PhantomEntity field_7319; // SYNTHETIC FIELD THAT STORES THE PHANTOM OUTER CLASS

		FindTargetGoalMixin() {
        }

		/**
		 * Makes phantoms target all living entities by effectively replacing the PlayerList with this one
		 */
		@Inject(method = "canStart", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/PhantomEntity;getWorld()Lnet/minecraft/world/World;", shift = At.Shift.BEFORE), cancellable = true)
		private void targetLivingEntities(CallbackInfoReturnable<Boolean> cir) {
			if (!PhantomsDoStuffConfig.phantomsTargetEverything) {
				return;
			}
			List<LivingEntity> list = field_7319.getWorld().getEntitiesByClass(LivingEntity.class, field_7319.getBoundingBox().expand(16.0, 64.0, 16.0), e -> e.isAlive() && !(e instanceof PhantomEntity));
			if (!list.isEmpty()) {
				list.sort(Comparator.comparing(Entity::getY).reversed());
				for (LivingEntity living : list) {
					if (!field_7319.isTarget(living, TargetPredicate.DEFAULT)) continue;
					field_7319.setTarget(living);
					cir.setReturnValue(true);
					return;
				}
			}
			cir.setReturnValue(false);
			return;
		}
	}
}

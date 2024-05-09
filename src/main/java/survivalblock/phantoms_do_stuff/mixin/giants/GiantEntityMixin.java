package survivalblock.phantoms_do_stuff.mixin.giants;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import survivalblock.phantoms_do_stuff.access.BreakDoorsAccess;
import survivalblock.phantoms_do_stuff.common.PhantomsDoStuffConfig;

@Debug(export = true)
@Mixin(GiantEntity.class)
public abstract class GiantEntityMixin extends HostileEntity implements BreakDoorsAccess {

    @Unique
    private ZombieEntity delegate = new ZombieEntity(this.getWorld());

    @Unique
    private final BreakDoorGoal breakDoorsGoal = new BreakDoorGoal((GiantEntity) (Object) this, ZombieEntityAccessor.getDoorBreakDifficultyChecker());

    @Unique
    private boolean canBreakDoors;

    protected GiantEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initializeZombieDelegate(EntityType<? extends HostileEntity> entityType, World world, CallbackInfo ci){
        this.delegate = new ZombieEntity(world);
    }

    @Override
    public boolean phantoms_do_stuff$canBreakDoors() {
        return this.canBreakDoors;
    }

    @Override
    public void phantoms_do_stuff$setCanBreakDoors(boolean canBreakDoors) {
        if (this.phantoms_do_stuff$shouldBreakDoors() && NavigationConditions.hasMobNavigation(this)) {
            if (this.canBreakDoors != canBreakDoors) {
                this.canBreakDoors = canBreakDoors;
                ((MobNavigation)this.getNavigation()).setCanPathThroughDoors(canBreakDoors);
                if (canBreakDoors) {
                    this.goalSelector.add(1, this.breakDoorsGoal);
                } else {
                    this.goalSelector.remove(this.breakDoorsGoal);
                }
            }
        } else if (this.canBreakDoors) {
            this.goalSelector.remove(this.breakDoorsGoal);
            this.canBreakDoors = false;
        }
    }

    @Override
    public boolean phantoms_do_stuff$shouldBreakDoors() {
        return PhantomsDoStuffConfig.returnGiantAI;
    }

    /**
     * Adds more attributes to the giant
     * @param original The original giant attributes
     * @return The modified giant attributes
     */
    @ModifyReturnValue(method = "createGiantAttributes", at = @At("RETURN"))
    private static DefaultAttributeContainer.Builder addMoreGiantAttributes(DefaultAttributeContainer.Builder original){
        if (!PhantomsDoStuffConfig.buffGiantAttributes) {
            return original;
        }
        return original.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 42.5) // 7.5 more than zombie
                .add(EntityAttributes.GENERIC_ARMOR, 8.0) // 4x zombie
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.4) // zombie has none
                .add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS); // same as zombie, but doesn't do anything yet
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void buffGiantStepHeight(EntityType<? extends GiantEntity> entityType, World world, CallbackInfo ci){
        this.setStepHeight(2.0f); // 2x drowned
    }

    @Override
    public boolean phantoms_do_stuff$burnsInDaylight() {
        return PhantomsDoStuffConfig.giantsBurnInDaylight;
    }

    @Override
    public ZombieEntity phantoms_do_stuff$getZombieDelegate() {
        return this.delegate;
    }
}

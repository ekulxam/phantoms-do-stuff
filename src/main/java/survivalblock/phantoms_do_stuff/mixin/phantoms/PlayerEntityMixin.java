package survivalblock.phantoms_do_stuff.mixin.phantoms;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import survivalblock.phantoms_do_stuff.common.PhantomsDoStuff;
import survivalblock.phantoms_do_stuff.common.PhantomsDoStuffConfig;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    @Unique
    private int phantomDismountCooldown = PhantomsDoStuff.PHANTOM_DISMOUNT_DELAY;

    @Shadow public abstract boolean damage(DamageSource source, float amount);

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyReturnValue(method = "shouldDismount", at = @At("TAIL"))
    private boolean noCheesing(boolean original){
        if (original && PhantomsDoStuffConfig.phantomsTryStopDismount
                && this.getVehicle() instanceof PhantomEntity phantom
                && this.phantomDismountCooldown <= 0) {
            Random random = this.getWorld().getRandom();
            int randDismount = random.nextBetween(0, 2);
            if (randDismount == 0){
                this.phantomDismountCooldown = 0;
                return true;
            }
            this.phantomDismountCooldown = PhantomsDoStuff.PHANTOM_DISMOUNT_DELAY; // prevent spamming shift
            phantom.tryAttack(this); // bite if fail
            return false;
        }
        return original;
    }

    @Inject(method = "tickRiding", at = @At("HEAD"))
    private void decrementRidingCooldown(CallbackInfo ci){
        if (!this.getWorld().isClient() && this.phantomDismountCooldown > 0) {
            this.phantomDismountCooldown--;
        }
    }
}

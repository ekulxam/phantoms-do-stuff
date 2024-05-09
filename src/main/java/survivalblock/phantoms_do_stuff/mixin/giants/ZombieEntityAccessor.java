package survivalblock.phantoms_do_stuff.mixin.giants;

import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Predicate;

@Mixin(ZombieEntity.class)
public interface ZombieEntityAccessor {

    @Accessor("DOOR_BREAK_DIFFICULTY_CHECKER")
    static Predicate<Difficulty> getDoorBreakDifficultyChecker() {
        throw new UnsupportedOperationException();
    }
}

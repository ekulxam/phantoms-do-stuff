package survivalblock.phantoms_do_stuff.access;

import net.minecraft.entity.mob.ZombieEntity;

public interface BreakDoorsAccess {

    boolean phantoms_do_stuff$canBreakDoors();
    void phantoms_do_stuff$setCanBreakDoors(boolean canBreakDoors);
    boolean phantoms_do_stuff$shouldBreakDoors();
    boolean phantoms_do_stuff$burnsInDaylight();
    ZombieEntity phantoms_do_stuff$getZombieDelegate();
}

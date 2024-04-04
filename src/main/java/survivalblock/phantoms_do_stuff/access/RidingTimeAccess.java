package survivalblock.phantoms_do_stuff.access;

public interface RidingTimeAccess {
    /**
     * sets the riding time of the entity
     * @param i ridingTime in ticks
     */
    void phantoms_do_stuff$setRidingTime(int i);

    /**
     * resets the riding time of the entity
     */
    void phantoms_do_stuff$resetRidingTime();

    /**
     * gets the riding time of the entity
     * @return ridingTime in ticks
     */
    int phantoms_do_stuff$getRidingTime();
}

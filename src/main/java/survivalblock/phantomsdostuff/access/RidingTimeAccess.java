package survivalblock.phantomsdostuff.access;

public interface RidingTimeAccess {
    /**
     * sets the riding time of the entity
     * @param i ridingTime in ticks
     */
    void phantoms_do_stuff$setRidingTime(int i);

    /**
     * gets the riding time of the entity
     * @return ridingTime in ticks
     */
    int phantoms_do_stuff$getRidingTime();
}

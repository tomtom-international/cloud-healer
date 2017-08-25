package com.tomtom.cloud.recycling;

/**
 * Interface for handling shutdown events.
 * Action to be taken upon receiving recycleMe request depends on the implementation.
 */
public interface ActiveVMRecycler {

    /**
     * Sends notification with provided reason, doubles number of instances and recycles current instance.
     * Everying is performed in separate thread.
     * @param reason description what is wrong
     */
    void recycleMe(String reason);

    /**
     * Used only for demo purpose, not needed for prod.
     * @return cloud-specific id of the instance the code runs on.
     */
    String instanceId();

}

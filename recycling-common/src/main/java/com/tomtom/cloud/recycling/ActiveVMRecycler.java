package com.tomtom.cloud.recycling;

/**
 * Interface for handling shutdown events.
 * Action to be taken upon receiving recycleMe request depends on the implementation.
 */
public interface ActiveVMRecycler {

    void recycleMe(String reason);

}

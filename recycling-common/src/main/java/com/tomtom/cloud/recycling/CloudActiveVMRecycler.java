package com.tomtom.cloud.recycling;

public class CloudActiveVMRecycler implements ActiveVMRecycler {

    private final ShutdownNotifiсationPublisher notifier;
    private final WorkerRecycler recycler;

    public CloudActiveVMRecycler(ShutdownNotifiсationPublisher notifier, WorkerRecycler recycler) {
        this.notifier = notifier;
        this.recycler = recycler;
    }

    @Override
    public void recycleMe(String reason) {
        notifier.publishShutdownNotification(reason);
        recycler.recycle();
    }
}

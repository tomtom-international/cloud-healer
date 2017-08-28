package com.tomtom.cloud.recycling;

public class CloudActiveVMRecycler implements ActiveVMRecycler {

    private final ShutdownNotifiсationPublisher notifier;
    private final WorkerRecycler recycler;

    public CloudActiveVMRecycler(ShutdownNotifiсationPublisher notifier, WorkerRecycler recycler) {
        this.notifier = notifier;
        this.recycler = recycler;
    }

    @Override
    public boolean recycleMe(String reason) {
        notifier.publishShutdownNotification(reason);
        return recycler.recycle();
    }

    @Override
    public String instanceId() {
        return recycler.instanceId();
    }
}

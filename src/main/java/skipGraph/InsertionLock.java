package skipGraph;

import skipGraph.NodeInfo;

import java.util.concurrent.Semaphore;

public class InsertionLock {

    // Represents an acquired lock from a neighbor.
    public static class NeighborInstance {
        public final NodeInfo node;
        public final int minLevel;

        public NeighborInstance(NodeInfo node, int minLevel) {
            this.node = node;
            this.minLevel = minLevel;
        }
    }

    private final Semaphore locked = new Semaphore(1, true);
    public NodeInfo owner = null;

    public boolean startInsertion() {
        boolean acquired = locked.tryAcquire();
        if (acquired) owner = null;
        return acquired;
    }

    public void endInsertion() {
        if (owner == null) locked.release();
    }

    public boolean tryAcquire(NodeInfo receiver) {
        boolean acquired = (receiver.equals(owner)) || locked.tryAcquire();
        if (acquired) owner = receiver;
        return acquired;
    }

    public boolean isLocked() {
        return locked.availablePermits() == 0;
    }

    public boolean isLockedBy() {
        return isLocked() && owner != null;
    }

    public boolean unlockOwned(NodeInfo owner) {
        if (!this.owner.equals(owner)) return false;
        this.owner = null;
        locked.release();
        return true;
    }
}
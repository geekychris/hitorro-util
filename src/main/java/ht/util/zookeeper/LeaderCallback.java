package ht.util.zookeeper;

public interface LeaderCallback {
    void release();

    void takeLeadership(String name);
}

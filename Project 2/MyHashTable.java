package com;

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

@State(Scope.Benchmark)
public class MyHashTable {

    private static class Node {
        final int hash;
        private final String key;
        private Video val;
        Node next = null;
        Node(int h, String k, Video v) {
            hash = h;
            key = k;
            val = v;
        }
        public void add(int h, String k, Video v) {
            if(key.equals(k)) {
                val = v;
            } else {
                if (next == null) {
                    next = new Node(h, k, v);
                } else {
                    next.add(h, k, v);
                }
            }
        }
        public String getKey() { return key; }
        public Video getVal() { return val; }
    }

    private volatile ArrayList<String> keys = new ArrayList<String>();
    private static volatile Node[] nodes, newNodes;
    private final int MOVED = -1;
    private volatile static ReentrantLock[] locks;
    private volatile static ReentrantLock[] newLocks;
    private volatile static ReentrantLock keyLock = new ReentrantLock();
    private volatile boolean resizing;
    private static VarHandle RESIZING;
    static {
        try {
            RESIZING = MethodHandles.lookup().findVarHandle(MyHashTable.class, "resizing", boolean.class);
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }
    private volatile int total;
    private static VarHandle TOTAL;
    static {
        try {
            TOTAL = MethodHandles.lookup().findVarHandle(MyHashTable.class, "total", int.class);
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    MyHashTable(int s) {
        nodes = new Node[s];
        locks = new ReentrantLock[s];
    }

    MyHashTable() {
        nodes = new Node[2048];
        locks = new ReentrantLock[2048];
    }

    public Video get(String key) {
        int hash = hash(key, nodes.length);
        ReentrantLock lock = locks[hash];
        outer: for(;;) {
            if(lock == null) {
                return null;
            }
            if(!lock.isLocked()) {
                Node e = nodes[hash];
                if(e != null && e.hash != MOVED) {
                    for(; e != null; e = e.next) {
                        if (e.getKey().equals(key)) {
                            return e.getVal();
                        }
                    }
                } else {
                    if (newNodes != null && newNodes.length != nodes.length) {
                        hash = hash(key, newNodes.length);
                        for (; ; ) {
                            if(newLocks[hash] != null) {
                                if (!newLocks[hash].isLocked()) {
                                    for (; e != null; e = e.next) {
                                        String k = e.getKey();
                                        if (k != null && k.equals(key)) {
                                            return e.getVal();
                                        }
                                    }
                                    break outer;
                                }
                            }
                        }
                    }
                }
                break;
            }
        }
        return null;
    }

    public boolean put(String key, Video val) {
        System.out.print("");
        //Check to make sure you're not adding a duplicate
        if(!keys.contains(key)) {
            //Check if you need to resize
            if(total < (nodes.length * .6)) {
                int hash = hash(key, nodes.length);
                for(;;) {
                    Node e = null;
                    //Get the value at the hash (but make sure nobody is currently changing it)
                    ReentrantLock lock = (locks[hash] == null) ? new ReentrantLock() : locks[hash];
                    for(;;) {
                        if (!lock.isLocked()) {
                            e = nodes[hash];
                            break;
                        }
                    }
                    //Check the value of e. If null, give the value a new lock if necessary and then add the node to that slot
                    if (e == null) {
                        if (lock.tryLock()) {
                            try {
                                nodes[hash] = new Node(hash, key, val);
                            } finally {
                                lock.unlock();
                            }
                            addKey(key);
                            break;
                        }
                    //If e is a MOVED node, help out with the resize, then try to put the node again
                    } else if (e.hash == MOVED) {
                        help(hash);
                        put(key, val);
                    //If e isn't null or MOVED, add the node to the list at that hash
                    } else {
                        if (lock.tryLock()) {
                            try {
                                if(nodes[hash] == null) {
                                    nodes[hash] = new Node(hash, key, val);
                                } else {
                                    nodes[hash].add(hash, key, val);
                                }
                            } finally {
                                lock.unlock();
                            }
                            addKey(key);
                            break;
                        }
                    }
                }
                System.out.print("");
                return true;
            //If resizing is necessary
            } else {
                //Check if a thread is currently resizing. Then either resize or help out
                boolean r = (boolean)RESIZING.get(this);
                if (RESIZING.compareAndSet(this, false, true)) {
                    this.resize();
                } else {
                    help(0);
                }
                System.out.print("");
                return this.put(key, val);
            }
        }
        System.out.print("");
        return false;
    }

    public void remove(String key) {
        if(key == null) {
            return;
        }
        int hash = hash(key, nodes.length);
        Node predecessor = null;
        Node e;
        ReentrantLock lock = locks[hash];
        for(;;) {
            if(lock == null) {
                return;
            }
            if (!lock.isLocked()) {
                e = nodes[hash];
                break;
            }
        }
//        System.out.println("Got e");
        while (e != null) {
            if(e.hash == MOVED) {
                help(hash);
                remove(key);
            } else {
                for(;;) {
                    if(lock.tryLock()) {
                        try {
                            for(; e != null; predecessor = e, e = e.next) {
                                if (key.equals(e.getKey())) {
                                    if (predecessor == null) {
                                        nodes[hash] = e.next;
                                    } else {
                                        predecessor.next = e.next;
                                    }
//                                    System.out.println("Done removing");
                                    break;
                                }
                            }
                        } finally {
                            lock.unlock();
                        }
                        break;

                    }
                }
                removeKey(key);
                break;
            }
        }
    }

    private void help(int startIndex) {
        //Help out starting from where you were
        move(startIndex);
        //Wait until the resize method is completely done (arrays are reassigned)
        for(;;) {
            boolean b = (boolean)RESIZING.get(this);
            if(!b) {
                break;
            }
        }
    }

    private void resize() {
        //Resize the newNodes and newLocks array
        newNodes = new Node[nodes.length * 2];
        newLocks = new ReentrantLock[newNodes.length];
        //Start moving everything over
        move(0);
        //Once everything is moved, start acquiring every lock
        for (int i = 0; i < locks.length; ) {
            ReentrantLock lock = (locks[i] == null) ? new ReentrantLock() : locks[i];
            if (lock.isLocked()) {
            } else if (lock.tryLock()) {
                i++;
            }
        }
        //Once you have all the locks (i.e., nobody else can be working on the hashmap) assign the nodes
        //and locks arrays to the newNodes and newLocks arrays that were just filled
        nodes = newNodes;
        locks = newLocks;
        //Let everybody know that the RESIZING is done
        RESIZING.compareAndSet(this, true, false);
    }

    private void move(int startIndex) {
        //Starting from where you were, go through the list
        for(int i = startIndex; i < nodes.length; i++) {
            //If another thread has finished resizing, break
            if(!(boolean)RESIZING.get(this)) {
                break;
            }
            ReentrantLock lock = (locks[i] == null) ? new ReentrantLock() : locks[i];
            //lock the spot
            if(lock.tryLock()) {
                try {
                    //If that spot is null, add a MOVED marker
                    if(nodes[i] == null) {
                        nodes[i] = new Node(MOVED, null, null);
                    //If its not null and not moved, move all the nodes there
                    } else if (nodes[i].hash != MOVED){
                        Node n = nodes[i];
                        Node m = new Node(MOVED, null, null);
                        m.next = nodes[i];
                        nodes[i] = m;
                        //Make sure newNodes has been initialized/doubled
                        while (newNodes == null || newNodes.length == nodes.length) {}
                        //For each, put in newNodes array
                        for (; n != null; n = n.next) {
                            //Make sure it's not a mistake with multiple MOVED markers
                            if (n.key != null && n.hash != MOVED) {
                                int hash = hash(n.key, newNodes.length);
                                for (;;) {
                                    //Make sure there is a lock for the newNodes spot
                                    ReentrantLock newLock = (newLocks[hash] == null) ? new ReentrantLock() : newLocks[hash];
                                    if (newLock.tryLock()) {
                                        try {
                                            if (newNodes[hash] == null) {
                                                newNodes[hash] = new Node(hash, n.key, n.val);
                                            } else {
                                                newNodes[hash].add(hash, n.key, n.val);
                                            }
                                        } finally {
                                            newLock.unlock();
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } finally {
                    lock.unlock();
                }
            } else {
                i--;
            }
        }
    }

    private void addKey(String k) {
        for(;;) {
            if(keyLock.tryLock()) {
                try {
                    keys.add(k);
                } finally {
                    keyLock.unlock();
                }
                break;
            }
        }
        TOTAL.getAndAdd(this, 1);
    }

    private void removeKey(String k) {
        for(;;) {
            if(keyLock.tryLock()) {
                try {
                    if(keys.remove(k)) {
                        TOTAL.getAndAdd(this, -1);
                    }
                } finally {
                    keyLock.unlock();
                }
                break;
            }
        }
    }

    public ArrayList<String> getKeys() {
        ArrayList<String> k;
        for(;;) {
            if(keyLock.tryLock()) {
                try {
                    k = new ArrayList<String>(keys);
                } finally {
                    keyLock.unlock();
                }
                break;
            }
        }
        return k;
    }

    public int getSize() {
        return (int)TOTAL.getAcquire(this);
    }

    private int hash(String key, int tableLength) {
        int hash = 0;
        for(int i = 0; i < key.length(); i++) {
            hash ^= key.charAt(i);
            hash *= 31;
        }
        return hash & (tableLength - 1);
    }
}
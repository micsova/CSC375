package com;

import org.openjdk.jmh.annotations.*;
import java.util.ArrayList;
import java.util.Random;

@State(Scope.Thread)
public class Writer implements Runnable {

    MyHashTable hashTable;

    public Writer(MyHashTable ht) {
        hashTable = ht;
    }

    public Writer() {
        hashTable = new MyHashTable();
    }

    public void run() {
//        for(;;) {
//            write();
//        }
        write();
    }

    @Benchmark()
    @BenchmarkMode(Mode.Throughput)
    @Threads(5)
    @Warmup(iterations = 5)
    @Fork(value = 1)
    public void write() {
        Random r = new Random();
        int remove = r.nextInt(10);
        if(remove <= (r.nextInt(3) + 3)) {
            ArrayList<String> keys = hashTable.getKeys();
            if(keys.size() > 0) {
//                System.out.println("Remove");
                String k = keys.get(r.nextInt(keys.size()));
                hashTable.remove(k);
//                Main.writes.getAndAdd(1);
            }
        } else {
//            System.out.println("Write");
            Video v = new Video(r.nextInt(3) + 1);
            hashTable.put(v.getTitle(), v);
//            Main.writes.getAndAdd(1);
        }
    }
}

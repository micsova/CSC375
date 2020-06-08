package com;

import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Random;

@State(Scope.Thread)
public class Reader implements Runnable {

    MyHashTable hashTable;

    public Reader(MyHashTable ht) {
        hashTable = ht;
    }

    public Reader() {
        hashTable = new MyHashTable();
    }

    public void run() {
//        for(;;) {
//            read();
//        }
        read();
    }

    @Benchmark()
    @BenchmarkMode(Mode.Throughput)
    @Threads(5)
    @Warmup(iterations = 5)
    @Fork(value = 1)
    public void read() {
        Random r = new Random();
        ArrayList<String> keys = hashTable.getKeys();
        if(keys.size() > 0) {
            int index = r.nextInt(keys.size());
            String k = keys.get(index);
            hashTable.get(k);
//            Main.reads.getAndAdd(1);
        }
    }
}

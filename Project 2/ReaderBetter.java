package com;

import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@State(Scope.Thread)
public class ReaderBetter implements Runnable {

    ConcurrentHashMap<String, Video> hashTable;

    public ReaderBetter(ConcurrentHashMap ht) {
        hashTable = ht;
    }
    public ReaderBetter() {
        hashTable = new ConcurrentHashMap<String, Video>();
    }

    public void run() {
        read();
    }

    @Benchmark()
    @BenchmarkMode(Mode.Throughput)
    @Threads(5)
    @Warmup(iterations = 5)
    @Fork(value = 1)
    public void read() {
        Random r = new Random();
        ArrayList<Video> keys = new ArrayList<Video>(hashTable.values());
        if(keys.size() > 0) {
            String key = keys.get(r.nextInt(keys.size())).getTitle();
            Video v = hashTable.get(key);
            if(v != null) {
//                    System.out.println("Length: " + v.getLength());
            }
//            MainBetter.reads.getAndAdd(1);
        }
    }
}

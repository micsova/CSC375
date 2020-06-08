package com;

import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@State(Scope.Thread)
public class WriterBetter implements Runnable {
    ConcurrentHashMap<String, Video> hashTable;

    public WriterBetter(ConcurrentHashMap ht) {
        hashTable = ht;
    }
    public WriterBetter() {
        hashTable = new ConcurrentHashMap<String, Video>();
    }

    public void run() {
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
            ArrayList<Video> keys = new ArrayList<Video>(hashTable.values());
            if(keys.size() > 0) {
                String key = keys.get(r.nextInt(keys.size())).getTitle();
                hashTable.remove(key);
//                MainBetter.writes.getAndAdd(1);
            }
        } else {
            Video v = new Video(r.nextInt(3) + 1);
            hashTable.put(v.getTitle(), v);
//            MainBetter.writes.getAndAdd(1);
        }
    }
}
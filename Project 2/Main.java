package com;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static volatile AtomicInteger reads = new AtomicInteger(0);
    public static volatile AtomicInteger writes = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
        MyHashTable hashTable = new MyHashTable(64);
        for(int i = 0; i < 3; i++) {
            Writer w = new Writer(hashTable);
            Executor ex = Executors.newSingleThreadExecutor();
            ex.execute(w);
        }
        for(int i = 0; i < 3; i++) {
            Reader r = new Reader(hashTable);
            Executor ex = Executors.newSingleThreadExecutor();
            ex.execute(r);
        }
//        long startTime = System.nanoTime();
//        for(;;) {
//            long current = System.nanoTime();
//            long elapsedTime = (current - startTime) / 1000000000;
//            if(elapsedTime != 0) {
//                System.out.println("Reads/sec: " + (reads.get() / ((current - startTime) / 1000000000)) + "\tWrites/sec: " +
//                        (writes.get() / ((current - startTime) / 1000000000)));
//            }
//        }
    }
}
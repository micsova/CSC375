package com;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class MainBetter {
//    public static volatile AtomicInteger reads = new AtomicInteger(0);
//    public static volatile AtomicInteger writes = new AtomicInteger(0);
//
//    public static void main(String[] args) throws Exception {
//        org.openjdk.jmh.Main.main(args);
//        ConcurrentHashMap<String, Video> hashTable = new ConcurrentHashMap<String, Video>();
//        for(int i = 0; i < 2; i++) {
//            WriterBetter w = new WriterBetter(hashTable);
//            Executor ex = Executors.newSingleThreadExecutor();
//            ex.execute(w);
//        }
//        for(int i = 0; i < 3; i++) {
//            ReaderBetter r = new ReaderBetter(hashTable);
//            Executor ex = Executors.newSingleThreadExecutor();
//            ex.execute(r);
//        }
//        long startTime = System.nanoTime();
//        long lastElapsed = 0;
//        for(;;) {
//            long current = System.nanoTime();
//            long elapsedTime = (current - startTime) / 1000000000;
////            if(elapsedTime - lastElapsed > 2) {
//                lastElapsed = elapsedTime;
//                if (elapsedTime != 0) {
////                    System.out.println("Reads/sec: " + (reads.get() / elapsedTime) + "\tWrites/sec: " +
////                            (writes.get() / elapsedTime));
//                }
////            }
//        }
//    }
}

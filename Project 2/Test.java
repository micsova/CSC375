package com;

import java.util.ArrayList;
import java.util.Random;

public class Test implements Runnable {

    MyHashTable hashTable;

    Test(MyHashTable ht) {
        hashTable = ht;
    }

    @Override
    public void run() {
        for(int i = 0; i < 100; i++) {
            Video v = new Video(1);
            if(hashTable.put(v.getTitle(), v)) {
                if (i % 10 == 0) {
                    ArrayList<String> keys = hashTable.getKeys();
                    Random r = new Random();
                    String k = keys.get(r.nextInt(keys.size()));
                    if(k != null) {
                        hashTable.remove(k);
                    } else {
                        System.out.println("null");
                    }
                }
            } else {
                i--;
            }
        }
        System.out.println(hashTable.getSize());
    }
}

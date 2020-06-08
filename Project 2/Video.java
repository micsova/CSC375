package com;

import java.util.Random;

public class Video {

    private int length; //in seconds
    private String title;

    Video(int l) {
        length = l;
        title = newTitle();
    }
    Video(int l, String t) {
        length = l;
        title = t;
    }

    //Generate a title that consists of a 5 character string of random letters
    private String newTitle() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)(random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char)randomLimitedInt);
        }
        return buffer.toString();
    }

    public String getTitle() {
        return title;
    }

    public int getLength() {
        return length;
    }

    public void watch() {
        long start = System.nanoTime();
        //Spin until the time is up (currentTime - start) / 1000000000 = duration in seconds
        while(((System.nanoTime() - start) / 1000000000) < length) {
        }
    }
}
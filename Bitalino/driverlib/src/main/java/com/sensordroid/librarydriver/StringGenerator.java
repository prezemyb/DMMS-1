package com.sensordroid.librarydriver;

/**
 * Created by sveinpg on 24.02.16.
 */
public class StringGenerator {
    /*
        Generates a String of msgSize bytes
     */
    public static String createDataSize(int msgSize){
        StringBuilder sb = new StringBuilder(msgSize);
        for (int i = 0; i< msgSize; i++) {
            sb.append('a');
        }
        return sb.toString();
    }
}

package com.facebook.presto.udfs;

/**
 * Created by seven.wang on 2016/5/31.
 */
public class Test {
    public static void main(String[] args) {
        for(int i=0;i<16;i++) {
            System.out.println(Long.toBinaryString(i));
        }

        LongAndDoubleState state = null;
        state.setDouble(10);
    }
}

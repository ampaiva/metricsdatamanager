package com.ampaiva.metricsdatamanager.util;

import java.util.Arrays;

public class LCS {
    private static int[] subarray(int[] arr, int begin, int end) {
        return Arrays.copyOf(arr, end - begin);
    }

    private static int[] append(int[] arr, int value) {
        int[] newArr = Arrays.copyOf(arr, arr.length + 1);
        newArr[arr.length] = value;
        return newArr;
    }

    public static int[] lcs(int[] a, int[] b) {
        int aLen = a.length;
        int bLen = b.length;
        if (aLen == 0 || bLen == 0) {
            return new int[0];
        } else if (a[aLen - 1] == b[bLen - 1]) {
            return append(lcs(subarray(a, 0, aLen - 1), subarray(b, 0, bLen - 1)), a[aLen - 1]);
        } else {
            int[] x = lcs(a, subarray(b, 0, bLen - 1));
            int[] y = lcs(subarray(a, 0, aLen - 1), b);
            return (x.length > y.length) ? x : y;
        }
    }
}

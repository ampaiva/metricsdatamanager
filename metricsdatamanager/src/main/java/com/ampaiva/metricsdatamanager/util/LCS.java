package com.ampaiva.metricsdatamanager.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LCS {
    private static int[] subarray(int[] arr, int begin, int end) {
        return Arrays.copyOf(arr, end - begin);
    }

    private static int[] append(int[] arr, int value) {
        int[] newArr = Arrays.copyOf(arr, arr.length + 1);
        newArr[arr.length] = value;
        return newArr;
    }

    public static int[] lcs_(int[] a, int[] b) {
        int aLen = a.length;
        int bLen = b.length;
        if (aLen == 0 || bLen == 0) {
            return new int[0];
        } else if (a[aLen - 1] == b[bLen - 1]) {
            return append(lcs_(subarray(a, 0, aLen - 1), subarray(b, 0, bLen - 1)), a[aLen - 1]);
        } else {
            int[] x = lcs_(a, subarray(b, 0, bLen - 1));
            int[] y = lcs_(subarray(a, 0, aLen - 1), b);
            return (x.length > y.length) ? x : y;
        }
    }

    public static int[] lcs(int[] a, int[] b) {
        int[][] lengths = new int[a.length + 1][b.length + 1];

        // row 0 and column 0 are initialized to 0 already

        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b.length; j++) {
                if (a[i] == b[j]) {
                    lengths[i + 1][j + 1] = lengths[i][j] + 1;
                } else {
                    lengths[i + 1][j + 1] = Math.max(lengths[i + 1][j], lengths[i][j + 1]);
                }
            }
        }

        // read the substring out from the matrix
        List<Integer> sb = new ArrayList<Integer>();
        for (int x = a.length, y = b.length; x != 0 && y != 0;) {
            if (lengths[x][y] == lengths[x - 1][y]) {
                x--;
            } else if (lengths[x][y] == lengths[x][y - 1]) {
                y--;
            } else {
                assert a[x - 1] == b[y - 1];
                sb.add(a[x - 1]);
                x--;
                y--;
            }
        }

        int[] ret = new int[sb.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = sb.get(sb.size() - 1 - i);
        }
        return ret;
    }

    public static int[] convert(Integer[] integerArr) {
        int[] intArr = new int[integerArr.length];
        for (int i = 0; i < integerArr.length; i++) {
            intArr[i] = integerArr[i];
        }
        return intArr;
    }
}

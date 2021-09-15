package com.company;

import java.io.Console;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class Main {

    //int count = 500000000;
    private static Random random = new Random();
    private static int count = 150000000;
    private static int[] array = generateArray(count);
    public static int[] getArray() {
        return array;
    }
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        int amountParts = 12;
        long startTime = System.currentTimeMillis();
        int array[] = getArray();
        System.out.println(FindMaxSimple(array));
        long endTime = System.currentTimeMillis();
        System.out.println("Total execution time: " + (endTime - startTime) + "ms");

        /////////////////////////////

        startTime = System.currentTimeMillis();
        var a = splitArray(array, count / amountParts);
        //System.out.println(a.length);
        ThreadPoolExecutor tpe = new ThreadPoolExecutor(
                6,
                12,
                1,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
        ArrayList<Future<Integer>> Ints = new ArrayList<Future<Integer>>();
        for (var i : a) {
            FindInt fi = new FindInt(i);
            Ints.add(tpe.submit(fi));
        }
        int[] array1 = ConvertFuture(Ints);
        System.out.println(FindMaxSimple(array1));
        /*int max = Integer.MIN_VALUE;
        for(var val : Ints){
            if (val.get() > max)
                max = val.get();
        }*/
        endTime = System.currentTimeMillis();
        System.out.println("Total execution time: " + (endTime - startTime) + "ms");
        tpe.shutdown();

        ///////////////////////////////////////


        startTime = System.currentTimeMillis();
        int max = new ForkJoinPool().invoke(new FindMaxTask(0, count));
        System.out.println("max = " + max);
        endTime = System.currentTimeMillis();
        System.out.println("Total execution time: " + (endTime - startTime) + "ms");
    }

    public static int[] ConvertFuture(ArrayList<Future<Integer>> list) throws ExecutionException, InterruptedException {
        int[] a = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            a[i] = list.get(i).get();
        }
        return a;
    }

    public static int FindMaxSimple(int array[]) {
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

    public static int[] generateArray(int count) {
        Random rnd = new Random();
        int a[] = new int[count];
        for (int i = 0; i < count; i++) {
            a[i] = rnd.nextInt();
        }
        return a;
    }

    public static int[][] splitArray(int[] inputArray, int chunkSize) {
        return IntStream.iterate(0, i -> i + chunkSize)
                .limit((int) Math.ceil((double) inputArray.length / chunkSize))
                .mapToObj(j -> Arrays.copyOfRange(inputArray, j, Math.min(inputArray.length, j + chunkSize)))
                .toArray(int[][]::new);
    }



    public static class FindInt implements Callable<Integer> {
        int a[];
        int max;

        public FindInt(int[] a) {
            this.a = a;
        }

        @Override
        public Integer call() throws Exception {
            //System.out.println("Поток начался");
            max = Integer.MIN_VALUE;
            for (int i = 0; i < a.length; i++) {
                if (a[i] > max) {
                    max = a[i];
                }
            }
            //System.out.println("Поток завершился" + max);
            return max;
        }
    }

    private static int THRESHOLD = 100;
    static class FindMaxTask extends RecursiveTask<Integer> {

        private int start;
        private int end;

        public FindMaxTask(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        protected Integer compute() {
            int length = end - start;
            if (length < THRESHOLD) {
                return computeDirectly();
            }

            int offset = length / 2;

            FindMaxTask left = new FindMaxTask(start, start + offset);
            left.fork();
            FindMaxTask right = new FindMaxTask(start + offset, end);

            return Math.max(right.compute(), left.join());
        }

        private Integer computeDirectly() {
            int max = Integer.MIN_VALUE;
            for (int i = start; i < end; i++) {
                if (max < array[i]) {
                    max = array[i];
                }
            }
            return max;
        }
}}
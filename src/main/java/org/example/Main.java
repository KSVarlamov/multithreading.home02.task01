package org.example;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {

    private static final int TEXT_COUNT = 10_000;
    private static final int TEXT_LENGTH = 100_000;
    private static final String ROUTE_STR = "abc";

    private static final BlockingQueue<String> QUEUE_A = new ArrayBlockingQueue<>(100);
    private static final BlockingQueue<String> QUEUE_B = new ArrayBlockingQueue<>(100);
    private static final BlockingQueue<String> QUEUE_C = new ArrayBlockingQueue<>(100);
    private static final Random random = new Random();

    private static final Thread stringGenerator = new Thread(() -> {
        try {
            for (int i = 0; i < TEXT_COUNT; i++) {
                String s = generateRoute(ROUTE_STR, TEXT_LENGTH);
                if (i % 100 == 0) System.out.println("Положил " + i + " строку");
                QUEUE_A.put(s);
                QUEUE_B.put(s);
                QUEUE_C.put(s);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    });

    public static void main(String[] args) throws InterruptedException {
        stringGenerator.start();
        LetterCounter aCounter = new LetterCounter('a', QUEUE_A);
        LetterCounter bCounter = new LetterCounter('b', QUEUE_B);
        LetterCounter cCounter = new LetterCounter('c', QUEUE_C);
        aCounter.start();
        bCounter.start();
        cCounter.start();
        aCounter.join();
        bCounter.join();
        cCounter.join();
        System.out.println(aCounter);
        System.out.println(bCounter);
        System.out.println(cCounter);
    }

    public static String generateRoute(String letters, int length) {
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }

    private static class LetterCounter extends Thread {
        private final char letter;
        private final BlockingQueue<String> queue;
        private String strWithMax = "";
        private int maxLetters = 0;

        public LetterCounter(char letter, BlockingQueue<String> queue) {
            this.letter = letter;
            this.queue = queue;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < TEXT_COUNT; i++) {
                    if (i % 100 == 0) {
                        System.out.printf("(%s): Обработал %s строку%n", letter, i);
                    }
                    String s = queue.take();
                    strAnalysis(s);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void strAnalysis(String s) {
            int count = 0;
            for (char c : s.toCharArray()) {
                if (c == letter) {
                    count++;
                }
            }
            if (count > maxLetters) {
                maxLetters = count;
                strWithMax = s;
            }
        }

        public String getStrWithMax() {
            return strWithMax;
        }

        public int getMaxLetters() {
            return maxLetters;
        }

        @Override
        public String toString() {
            return "LetterCounter{" + "letter=" + letter +
                    //", max='" + strWithMax + '\'' +
                    ", maxLetters=" + maxLetters + '}';
        }
    }
}
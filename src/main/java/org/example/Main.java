package org.example;

import java.util.*;

public class Main {

    public static final Map<Integer, Integer> sizeToFreq = new TreeMap<>();
    private final static int THREAD_COUNT = 1000;
    private final static String ROUTE_STR = "RLRFR";

    public static void main(String[] args) throws InterruptedException {
        Runnable task = () -> {
            String route = generateRoute(ROUTE_STR, 100);
            int count = (int) route.chars().filter(c -> c == 'R').count();
            //System.out.printf("Количество поворотов направо: %s\n", count);
            updateFreq(count);
        };

        Thread leaderPrinter = new Thread(() -> {
            int i = 0;
            while (!Thread.interrupted()) {
                synchronized (sizeToFreq) {
                    try {
                        sizeToFreq.wait();
                        Map.Entry<Integer, Integer> maxEntry = sizeToFreq.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
                        System.out.printf("[%04d] Текущий лидер: повторений %s (встретилось %s раз)\n", ++i, maxEntry.getKey(), maxEntry.getValue());
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        leaderPrinter.start();

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            Thread thread = new Thread(task);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
        leaderPrinter.interrupt();
        printStatistic();
    }

    private static void printStatistic() {
        Map.Entry<Integer, Integer> maxEntry = sizeToFreq.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);

        System.out.printf("\nСамое частое количество повторений %s (встретилось %s раз)\n", maxEntry.getKey(), maxEntry.getValue());
        System.out.println("Другие размеры:");
        sizeToFreq.entrySet().stream().filter(entry -> !entry.equals(maxEntry)).forEach(m -> System.out.printf("- %s (%s раз)\n", m.getKey(), m.getValue()));
    }

    private static void updateFreq(int count) {
        synchronized (sizeToFreq) {
            if (sizeToFreq.containsKey(count)) {
                sizeToFreq.put(count, sizeToFreq.get(count) + 1);
            } else {
                sizeToFreq.put(count, 1);
            }
            sizeToFreq.notify();
        }
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }
}
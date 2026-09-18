package vtor_kolokvium_2026.stats;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class StatisticsService {
    private int count = 0;
    private long sum = 0;
    private Integer min = null;
    private Integer max = null;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void addNumber(int number) {
        lock.writeLock().lock();
        try {
            count++;
            sum += number;
            if (min == null || min > number) min = number;
            if (max == null || max < number) max = number;
        }finally {
            lock.writeLock().unlock();
        }
    }

    public int getCount() {
        lock.readLock().lock();
        try{
        return count;
        }finally {
            lock.readLock().unlock();
        }
    }

    public Double getAverage() {
        lock.readLock().lock();
        try {
            return count == 0 ? 0.0 : (double) sum / count;
        }finally {
            {
                lock.readLock().unlock();
            }
        }
    }

    public Double getMin() {
        lock.readLock().lock();
        try {
            return min == null ? 0.0 : min;
        }finally {
            lock.readLock().unlock();
        }
    }

    public Double getMax() {
        lock.readLock().lock();
        try {
            return max == null ? 0.0 : max;
        }finally {
            lock.readLock().unlock();
        }
    }
}

class SubmitNumberTask implements Callable<String> {
    private final StatisticsService service;
    private final int number;

    public SubmitNumberTask(StatisticsService service, int number) {
        this.service = service;
        this.number = number;
    }

    @Override
    public String call() {
        service.addNumber(number);
        return String.format("NUMBER %d ADDED. Total numbers: %d", number, service.getCount());
    }
}

class GetAverageTask implements Callable<String> {
    private final StatisticsService service;

    public GetAverageTask(StatisticsService service) {
        this.service = service;
    }

    @Override
    public String call() {
        return String.format("AVERAGE: %.2f", service.getAverage());
    }
}

class GetMinTask implements Callable<String> {
    private final StatisticsService service;

    public GetMinTask(StatisticsService service) {
        this.service = service;
    }

    @Override
    public String call(){
        return String.format("MIN: %.2f", service.getMin());
    }
}

class GetMaxTask implements Callable<String> {
    private final StatisticsService service;

    public GetMaxTask(StatisticsService service) {
        this.service = service;
    }

    @Override
    public String call() {
        return String.format("MAX: %.2f", service.getMax());
    }
}

class ConcurrentService {
    public static List<Future<String>> submitAll(int numThreads, List<Callable<String>> tasks) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        try {
            return executor.invokeAll(tasks);
        } finally {
            executor.shutdown();
        }
    }
}

public class StatisticsExam {

    public static void main(String[] args) throws Exception {

        StatisticsService service = new StatisticsService();

        int k;
        Scanner scanner = new Scanner(System.in);
        k = scanner.nextInt();


        List<Callable<String>> tasks = new ArrayList<>();

        /* ------------------------------------------------------------
           PHASE 1: Concurrent writers
           ------------------------------------------------------------ */

        int added = 0;
        int avg = 0;
        int min = 0;
        int max = 0;

        int expectedMin = 10;
        int expectedMax = 10;

        for (int i = 1; i < k * 100; i++) {
            int value = i * 10;
            tasks.add(new SubmitNumberTask(service, value));
            expectedMax = Math.max(expectedMax, value);
            added++;
        }

        /* ------------------------------------------------------------
           PHASE 2: Concurrent readers (should run in parallel)
           ------------------------------------------------------------ */

        for (int i = 0; i < k * 5; i++) {
            tasks.add(new GetAverageTask(service));
            avg++;
            tasks.add(new GetMinTask(service));
            min++;
            tasks.add(new GetMaxTask(service));
            max++;
        }

        /* ------------------------------------------------------------
           PHASE 3: Interleaved read/write (critical part)
           ------------------------------------------------------------ */

        for (int i = 100; i <= k * 200; i += 10) {
            tasks.add(new SubmitNumberTask(service, i));
            added++;
            expectedMax = Math.max(expectedMax, i);
            tasks.add(new GetAverageTask(service));
            avg++;
            tasks.add(new GetMinTask(service));
            min++;
            tasks.add(new GetMaxTask(service));
            max++;
        }

        /* ------------------------------------------------------------
           EXECUTION
           ------------------------------------------------------------ */


        List<Future<String>> results = ConcurrentService.submitAll(6, tasks);


        List<String> finalResults = new ArrayList<>();
        for (Future<String> f : results) {
            try {
                finalResults.add(f.get());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        int numberAddedMessage = 0, minInvoked = 0, maxInvoked = 0, averageInvoked = 0;

        for (String finalResult : finalResults) {
            if (finalResult.startsWith("AVERAGE")) {
                averageInvoked++;
            }
            if (finalResult.startsWith("MIN")) {
                minInvoked++;
            }
            if (finalResult.startsWith("MAX")) {
                maxInvoked++;
            }
            if (finalResult.contains("Total numbers: ")) {
                numberAddedMessage++;
            }
        }

        if (minInvoked != min) {
            System.out.println("GetMinTask was not invoked the correct number of times");
        }

        if (maxInvoked != max) {
            System.out.println("GetMaxTask was not invoked the correct number of times");
        }

        if (averageInvoked != avg) {
            System.out.println("GetAverageTask was not invoked the correct number of times");
        }

        if (numberAddedMessage != added) {
            System.out.println("Number of added tasks was not invoked the correct number of times");
        }

        /* ------------------------------------------------------------
           BASIC SANITY CHECKS (NO assert, exam-safe)
           ------------------------------------------------------------ */

        int finalCount = service.getCount();


        if (finalCount != added) {
            throw new RuntimeException(
                    String.format("ERROR: Expected %d numbers, but got %d", added, finalCount)
            );
        }
        if (service.getMin() != expectedMin) {
            throw new RuntimeException(
                    "ERROR: Expected MIN = " + expectedMin
            );
        }

        if (service.getMax() != expectedMax) {
            throw new RuntimeException(
                    "ERROR: Expected MAX = " + expectedMax
            );
        }

        System.out.println("✔ FINAL CHECKS PASSED");
    }
}

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;

public class TaskProcessor {

    public void processArray(boolean isWorkStealing) {
        int rows = 1000, cols = 1000;
        int[][] array = generateRandom2DArray(rows, cols);
        ForkJoinPool pool = isWorkStealing ? ForkJoinPool.commonPool() : new ForkJoinPool();

        long startTime = System.nanoTime();
        int minElement = pool.invoke(new ArrayMinTask(array, 0, rows));
        long endTime = System.nanoTime();

        System.out.println("Мінімальний елемент: " + minElement);
        System.out.println("Час виконання: " + (endTime - startTime) / 1e6 + " ms");
    }

    public void processFiles(boolean isWorkStealing) {
        File directory = chooseDirectory();
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".txt"));

        if (files == null || files.length == 0) {
            System.out.println("Немає текстових файлів у директорії.");
            return;
        }

        ExecutorService executor = isWorkStealing
                ? Executors.newWorkStealingPool()
                : Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        long startTime = System.nanoTime();
        int totalCharacters = 0;

        try {
            for (Future<Integer> future : executor.invokeAll(createFileTasks(files))) {
                totalCharacters += future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

        long endTime = System.nanoTime();

        System.out.println("Загальна кількість символів: " + totalCharacters);
        System.out.println("Час виконання: " + (endTime - startTime) / 1e6 + " ms");
    }

    private int[][] generateRandom2DArray(int rows, int cols) {
        Random random = new Random();
        int[][] array = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                array[i][j] = random.nextInt(1000);
            }
        }
        return array;
    }

    private File chooseDirectory() {
        System.out.println("Введіть шлях до директорії:");
        Scanner scanner = new Scanner(System.in);
        String dirPath = scanner.nextLine();
        return new File(dirPath);
    }

    private List<Callable<Integer>> createFileTasks(File[] files) {
        return List.of(files).stream().map(file -> (Callable<Integer>) () -> {
            try {
                return Files.readAllLines(file.toPath())
                        .stream()
                        .mapToInt(String::length)
                        .sum();
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
        }).toList();
    }
}

class ArrayMinTask extends RecursiveTask<Integer> {
    private final int[][] array;
    private final int startRow, endRow;

    public ArrayMinTask(int[][] array, int startRow, int endRow) {
        this.array = array;
        this.startRow = startRow;
        this.endRow = endRow;
    }

    @Override
    protected Integer compute() {
        if (endRow - startRow <= 10) {
            int min = Integer.MAX_VALUE;
            for (int i = startRow; i < endRow; i++) {
                for (int j = 0; j < array[i].length; j++) {
                    min = Math.min(min, array[i][j]);
                }
            }
            return min;
        } else {
            int mid = (startRow + endRow) / 2;
            ArrayMinTask leftTask = new ArrayMinTask(array, startRow, mid);
            ArrayMinTask rightTask = new ArrayMinTask(array, mid, endRow);

            leftTask.fork();
            return Math.min(rightTask.compute(), leftTask.join());
        }
    }
}

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        TaskProcessor processor = new TaskProcessor();

        System.out.println("Оберіть задачу:");
        System.out.println("1. Пошук мінімального елемента в 2D масиві.");
        System.out.println("2. Підрахунок символів у текстових файлах.");
        int choice = scanner.nextInt();

        System.out.println("Оберіть підхід:");
        System.out.println("1. Work Stealing.");
        System.out.println("2. Work Dealing.");
        int approach = scanner.nextInt();

        switch (choice) {
            case 1 -> processor.processArray(approach == 1);
            case 2 -> processor.processFiles(approach == 1);
            default -> System.out.println("Некоректний вибір!");
        }
    }
}

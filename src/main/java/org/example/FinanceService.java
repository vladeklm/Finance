package org.example;

import java.util.Scanner;

public class FinanceService {
    private Scanner scanner;

    public FinanceService() {
        scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("FinanceService started");
        while (true) {
            var line = scanner.nextLine();
            var data = line.split(" ");
            switch (data[0]) {
                case "доход":
                    break;
                case "расход":
                    break;
                case "бюджет":
                    break;
                case "расчёт":
                    break;
                case "вход":
                    break;
                case "выход":
                    break;
                default:
                    System.out.println("Unknown command");
            }
        }
    }
}

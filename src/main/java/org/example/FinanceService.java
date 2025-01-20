package org.example;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Scanner;

public class FinanceService {
    private Scanner scanner;
    private String currentUser;
    private UserStorage userStorage;
    private WalletStorage walletStorage;



    public FinanceService() {
        scanner = new Scanner(System.in);
        userStorage = new UserStorage();
        walletStorage = new WalletStorage();
    }

    public void start() {
        System.out.println("FinanceService started");
        while (true) {
            var line = scanner.nextLine();
            var data = line.split(" ");
            switch (data[0]) {
                case "доход":
                    income(data);
                    break;
                case "расход":
                    expense(data);
                    break;
                case "бюджет":
                    budget(data);
                    break;
                case "расчёт":
                    calculate(data);
                    break;
                case "вход":
                    login(data);
                    break;
                case "выход":
                    logout();
                    break;
                case "регистрация":
                    registerUser(data);
                    break;
                default:
                    System.out.println("Unknown command");
            }
        }
    }

    private void calculate(String[] data) {
        if (currentUser == null) {
            System.out.println("You must be logged in");
            return;
        }
        var wallet = walletStorage.getWallet(currentUser);
        if (data.length == 1) {
            System.out.println("Общий доход: " + wallet.getAllIncome());
            System.out.println("Доходы по категориям: ");
            var incomeByCategory = wallet.getIncomesByCategory();
            for (var category : incomeByCategory.keySet()) {
                System.out.println("   " + category + ": " + incomeByCategory.get(category));
            }
            System.out.println("Общий расход: " + wallet.getAllExpenses());
            System.out.println("Расходы по категориям: ");
            var expenseByCategory = wallet.getExpensesByCategory();
            for (var category : expenseByCategory.keySet()) {
                System.out.println("   " + category + ": " + expenseByCategory.get(category));
            }
            var budget = wallet.getBudgets(null);
            System.out.println("Бюджет: ");
            for (var item : budget) {
                System.out.println("   " + item.getCategory() + ": " + item.getAmount() + " Остаток: " + item.getLimit());
            }
        }
        else {
            var categories = new String[data.length - 1];
            System.arraycopy(data, 1, categories, 0, categories.length);
            System.out.println("Доходы по категориям: ");
            var incomeByCategory = wallet.getIncomesByWalletTypeAndCategory(Arrays.stream(categories).toList());
            for (var category : incomeByCategory.keySet()) {
                System.out.println("   " + category + ": " + incomeByCategory.get(category));
            }
            System.out.println("Расходы по категориям: ");
            var expenseByCategory = wallet.getExpensesByWalletTypeAndCategory(Arrays.stream(categories).toList());
            for (var category : expenseByCategory.keySet()) {
                System.out.println("   " + category + ": " + expenseByCategory.get(category));
            }
            var budget = wallet.getBudgets(Arrays.stream(categories).toList());
            System.out.println("Бюджет: ");
            for (var item : budget) {
                System.out.println("   " + item.getCategory() + ": " + item.getAmount() + " Остаток: " + item.getLimit());
            }
        }
    }

    private void registerUser(String[] data) {
        if (data.length != 3) {
            System.out.println("Использовать: регистрация <имя> <пароль>");
            return;
        }
        var userName = data[1];
        var password = data[2];
        userStorage.registerUser(userName, password);
    }

    private void budget(String[] data) {
        if (data.length != 3) {
            System.out.println("Использовать: бюджет <категория> <сумма>");
            return;
        }
        if (currentUser == null) {
            System.out.println("You must be logged in");
            return;
        }
        var category = data[1];
        var amount = BigDecimal.valueOf(Double.parseDouble(data[2]));
        var wallet = walletStorage.getWallet(currentUser);
        wallet.addBudget(amount, category);
    }

    private void income(String[] data) {
        incomeOrExpense(data, WalletItemType.INCOME);
    }

    private void expense(String[] data) {
        incomeOrExpense(data, WalletItemType.EXPENSE);
    }

    private void incomeOrExpense(String[] data, WalletItemType type) {
        if (data.length != 3) {
            System.out.println("Использовать: доход/расход <категория> <сумма>");
            return;
        }
        if (currentUser == null) {
            System.out.println("You must be logged in");
            return;
        }
        var category = data[1];
        var amount = BigDecimal.valueOf(Double.parseDouble(data[2]));
        var wallet = walletStorage.getWallet(currentUser);
        wallet.addItem(amount, category, type);
    }

    private void login(String[] data) {
        if (data.length != 3) {
            System.out.println("Использовать: вход <имя> <пароль>");
            return;
        }
        var userName = data[1];
        var password = data[2];
        if (userStorage.verifyUser(userName, password)) {
            currentUser = userName;
            loadUserData();
        }
    }

    private void logout() {
        currentUser = null;
        saveUserData();
    }

    private void saveUserData() {
        walletStorage.saveToFile(currentUser);
        userStorage.saveToFile(currentUser);
    }

    private void loadUserData() {
        walletStorage.loadFromFile(currentUser);
        userStorage.loadFromFile(currentUser);
    }
}

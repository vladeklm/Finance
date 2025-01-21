package org.example;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletStorage implements FileLoadable {
    private Map<String, Wallet> wallets;

    public WalletStorage() {
        wallets = new HashMap<>();
    }

    @Override
    public void loadFromFile(String userName) {
        var wallet = wallets.get(userName);
        if (wallet == null) {
            wallet = new Wallet();
            wallets.put(userName, wallet);
        }
        wallet.loadFromFile(userName);
    }

    @Override
    public void saveToFile(String userName) {
        var wallet = wallets.get(userName);
        wallet.saveToFile(userName);
    }

    public class Wallet implements FileLoadable {
        private List<WalletItem> walletItems;
        private List<WalletBudget> budget;

        public Wallet() {
            walletItems = new ArrayList<>();
            budget = new ArrayList<>();
        }

        public void addItem(BigDecimal amount, String category, WalletItemType type) {
            walletItems.add(new WalletItem(amount, category, type));
        }

        public void addBudget(BigDecimal amount, String category) {
            budget.add(new WalletBudget(amount, category));
        }

        public List<BudgetView> getBudgets(List<String> categories) {
            var result = new ArrayList<BudgetView>();
            for (var item : budget) {
                var category = new ArrayList<String>();
                if (categories!= null && !categories.contains(item.category)) {
                    continue;
                }
                category.add(item.category);
                var expense = getExpensesByWalletTypeAndCategory(category).get(item.category);
                if (expense == null) {
                    expense = BigDecimal.ZERO;
                }
                var limit = item.amount.subtract(expense);
                result.add(new BudgetView(item.amount, limit, item.category));
            }
            return result;
        }



        public BigDecimal getAllIncome() {
            return getAllDataByType(WalletItemType.INCOME);
        }

        public BigDecimal getAllExpenses() {
            return getAllDataByType(WalletItemType.EXPENSE);
        }

        private BigDecimal getAllDataByType(WalletItemType walletItemType) {
            var result = walletItems.stream()
                    .filter(item -> item.type == walletItemType)
                    .map(WalletItem::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return result;
        }

        public Map<String, BigDecimal> getIncomesByCategory() {
            return getDataByWalletType(WalletItemType.INCOME);
        }

        public Map<String, BigDecimal> getExpensesByCategory() {
            return getDataByWalletType(WalletItemType.EXPENSE);
        }

        public Map<String, BigDecimal> getIncomesByWalletTypeAndCategory(List<String> categories) {
            return getDataByWalletTypeAndCategory(WalletItemType.INCOME, categories);
        }

        public Map<String, BigDecimal> getExpensesByWalletTypeAndCategory(List<String> categories) {
            return getDataByWalletTypeAndCategory(WalletItemType.EXPENSE, categories);
        }

        private Map<String, BigDecimal> getDataByWalletTypeAndCategory(WalletItemType walletItemType, List<String> categories) {

            var tempResult = getDataByWalletType(walletItemType);
            var result = new HashMap<String, BigDecimal>();
            for (var category : categories) {
                if (tempResult.containsKey(category)) {
                    result.put(category, tempResult.get(category));
                }
            }
            return result;
        }

        private Map<String, BigDecimal> getDataByWalletType(WalletItemType walletItemType) {
            var result = new HashMap<String, BigDecimal>();
            for (var item : walletItems) {
                if (item.type == walletItemType) {
                    if (result.containsKey(item.category)) {
                        var amount = result.get(item.category).add(item.amount);
                        result.remove(item.category);
                        result.put(item.category, amount);
                    }
                    else {
                        result.put(item.category, item.amount);
                    }
                }
            }
            return result;
        }


        @Override
        public void loadFromFile(String userName) {
            var fileName = userName + "_wallet.txt";
            walletItems = new ArrayList<>();
            budget = new ArrayList<>();
            try (var reader = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    var parts = line.split(" ");
                    if (parts.length == 3) {
                        var amount = new BigDecimal(parts[1]);
                        var category = parts[2];
                        var type = WalletItemType.valueOf(parts[0]);
                        walletItems.add(new WalletItem(amount, category, type));
                    }
                    else if (parts.length == 2) {
                        var amount = new BigDecimal(parts[0]);
                        var category = parts[1];
                        budget.add(new WalletBudget(amount, category));
                    }
                }
            } catch (FileNotFoundException e) {
                //do nothing
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void saveToFile(String userName) {
            var fileName = userName + "_wallet.txt";
            try (var fos = new FileOutputStream(new File(fileName), false)) {
                var writer = new OutputStreamWriter(fos);
                for (var item : walletItems) {
                    writer.write(item.type.toString() + " " + item.amount + " " + item.category + "\n");
                }
                for (var item : budget) {
                    writer.write(item.amount + " " + item.category + "\n");
                }
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class WalletItem {
        private BigDecimal amount;
        private String category;
        private WalletItemType type;

        WalletItem(BigDecimal amount, String category, WalletItemType type) {
            this.amount = amount;
            this.category = category;
            this.type = type;
        }

        public BigDecimal getAmount() {
            return amount;
        }
    }

    public class WalletBudget {
        private BigDecimal amount;
        private String category;

        public WalletBudget(BigDecimal amount, String category) {
            this.amount = amount;
            this.category = category;
        }
    }

    public Wallet getWallet(String name) {
        var result = wallets.get(name);
        if (result == null) {
            wallets.put(name, new Wallet());
        }
        return wallets.get(name);
    }

    public class BudgetView {
        private String category;
        private BigDecimal amount;
        private BigDecimal limit;

        public BudgetView(BigDecimal amount, BigDecimal limit, String category) {
            this.amount = amount;
            this.limit = limit;
            this.category = category;
        }

        public String getCategory() {
            return category;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public BigDecimal getLimit() {
            return limit;
        }
    }


}

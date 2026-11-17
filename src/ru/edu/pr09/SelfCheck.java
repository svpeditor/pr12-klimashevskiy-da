package ru.edu.pr09;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class SelfCheck {
    private static final SalesAnalyticsService SERVICE = new SalesAnalyticsService();
    private static int passed = 0;
    private static int count = 0;

    public static void main(String[] args) {
        checkSale();
        checkTopAndSort();
        checkMaxAndGroups();
        checkNoSideEffects();
        System.out.println("Unit: " + passed + "/" + count + " passed");
        // свой файл: java -cp out ru.edu.pr09.SelfCheck data/my.csv
        runFile(args.length > 0 ? args[0] : "data/sample.csv");
    }

    private static void checkSale() {
        check("negative amount", !trySale("S1", "Ivanov", "Moscow", -1));
        check("empty manager", !trySale("S1", " ", "Moscow", 1000));
        check("zero amount", trySale("S1", "Ivanov", "Moscow", 0));
    }

    private static void checkTopAndSort() {
        List<Sale> sales = sample();
        List<Sale> top = SERVICE.topSales(sales, 100000, 2);
        check("top 2", top.size() == 2 && top.get(0).id().equals("S3") && top.get(1).id().equals("S1"));
        check("top limit 0", SERVICE.topSales(sales, 0, 0).isEmpty());
        check("top limit more", SERVICE.topSales(sales, 100000, 10).size() == 2);
        check("top min included", SERVICE.topSales(sales, 150000, 5).size() == 2);
        check("top nothing", SERVICE.topSales(sales, 1000000, 5).isEmpty());
        check("top negative limit", !tryTop(sales, -1));
        // при равной сумме порядок по id
        List<Sale> same = List.of(new Sale("S9", "Lee", "Kazan", 92000), new Sale("S2", "Petrova", "SPb", 92000));
        List<Sale> sorted = SERVICE.topSales(same, 0, 2);
        check("second field id", sorted.get(0).id().equals("S2") && sorted.get(1).id().equals("S9"));
    }

    private static void checkMaxAndGroups() {
        List<Sale> sales = sample();
        check("max sale", SERVICE.maxSale(sales).get().id().equals("S3"));
        check("max empty", SERVICE.maxSale(List.of()).isEmpty());
        Map<String, Double> totals = SERVICE.totalsByManager(sales);
        check("totals", totals.get("Ivanov") == 390000 && totals.get("Petrova") == 92000 && totals.get("Lee") == 45000);
        check("totals sorted", totals.keySet().iterator().next().equals("Ivanov"));
        totals.put("Ivanov", 0.0);
        check("totals new map", SERVICE.totalsByManager(sales).get("Ivanov") == 390000);
        check("average region", SERVICE.averageByRegion(sales, "Moscow") == 195000);
        check("average unknown region", SERVICE.averageByRegion(sales, "Omsk") == 0.0);
        check("average empty list", SERVICE.averageByRegion(List.of(), "Moscow") == 0.0);
    }

    private static void checkNoSideEffects() {
        List<Sale> sales = new ArrayList<>(sample());
        List<Sale> before = new ArrayList<>(sales);
        SERVICE.topSales(sales, 0, 3);
        SERVICE.totalsByManager(sales);
        SERVICE.largeSales(sales);
        check("source unchanged", sales.equals(before));
        // неизменяемый список на входе тоже работает
        check("immutable input", SERVICE.topSales(List.copyOf(sales), 0, 4).size() == 4);
    }

    // ---------- вспомогательные методы ----------

    private static List<Sale> sample() {
        return List.of(new Sale("S1", "Ivanov", "Moscow", 150000), new Sale("S2", "Petrova", "SPb", 92000),
                new Sale("S3", "Ivanov", "Moscow", 240000), new Sale("S4", "Lee", "Kazan", 45000));
    }

    private static boolean trySale(String id, String manager, String region, double amount) {
        try {
            new Sale(id, manager, region, amount);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean tryTop(List<Sale> sales, int limit) {
        try {
            SERVICE.topSales(sales, 0, limit);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static void check(String name, boolean ok) {
        count++;
        if (ok) {
            passed++;
            System.out.println("PASS " + name);
        } else {
            System.out.println("FAIL " + name);
        }
    }

    // ---------- продажи из файла id;manager;region;amount ----------

    private static void runFile(String fileName) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(fileName));
        } catch (IOException e) {
            System.out.println("Не удалось прочитать " + fileName);
            return;
        }
        System.out.println();
        System.out.println("Продажи из файла " + fileName + ":");
        List<Sale> sales = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (int i = 1; i < lines.size(); i++) {   // первая строка - заголовок
            if (lines.get(i).isBlank()) {
                continue;
            }
            try {
                Sale s = parseSale(lines.get(i));
                if (!ids.add(s.id())) {
                    throw new IllegalArgumentException("повтор id " + s.id());
                }
                sales.add(s);
            } catch (IllegalArgumentException e) {
                System.out.println("строка " + (i + 1) + " пропущена: " + e.getMessage());
            }
        }
        System.out.println("Загружено продаж: " + sales.size() + ", крупных (от "
                + (int) SalesAnalyticsService.LARGE_DEAL_MIN + "): " + SERVICE.largeSales(sales).size());
        System.out.println("Топ-3 крупных:");
        for (Sale s : SERVICE.topSales(sales, SalesAnalyticsService.LARGE_DEAL_MIN, 3)) {
            System.out.println("  " + show(s));
        }
        System.out.println("Максимальная: " + SERVICE.maxSale(sales).map(SelfCheck::show).orElse("нет продаж"));
        System.out.println("По менеджерам:");
        for (Map.Entry<String, Double> e : SERVICE.totalsByManager(sales).entrySet()) {
            System.out.println("  " + e.getKey() + " " + String.format("%.2f", e.getValue()));
        }
        Set<String> regions = new TreeSet<>();
        for (Sale s : sales) {
            regions.add(s.region());
        }
        for (String region : regions) {
            System.out.println("Среднее " + region + ": " + String.format("%.2f", SERVICE.averageByRegion(sales, region)));
        }
    }

    private static String show(Sale s) {
        return s.id() + " " + s.manager() + " " + s.region() + " " + String.format("%.2f", s.amount());
    }

    private static Sale parseSale(String line) {
        String[] p = line.split(";", -1);
        if (p.length != 4) {
            throw new IllegalArgumentException("нужно 4 значения через ;");
        }
        double amount;
        try {
            amount = Double.parseDouble(p[3].strip());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("сумма не число: " + p[3]);
        }
        return new Sale(p[0].strip(), p[1].strip(), p[2].strip(), amount);
    }
}

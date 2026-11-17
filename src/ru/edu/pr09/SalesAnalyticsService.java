package ru.edu.pr09;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

// исходный список нигде не меняется, каждый метод возвращает новый результат
public class SalesAnalyticsService {
    // крупная сделка - от 100 000 рублей
    public static final double LARGE_DEAL_MIN = 100000;

    // сначала по сумме по убыванию, при равной сумме по id
    public static final Comparator<Sale> BY_AMOUNT_DESC_THEN_ID =
            Comparator.comparingDouble(Sale::amount).reversed().thenComparing(Sale::id);

    public List<Sale> topSales(List<Sale> sales, double min, int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit не может быть отрицательным: " + limit);
        }
        return sales.stream()
                .filter(s -> s.amount() >= min)
                .sorted(BY_AMOUNT_DESC_THEN_ID)
                .limit(limit)
                .toList();
    }

    public Optional<Sale> maxSale(List<Sale> sales) {
        return sales.stream().max(Comparator.comparingDouble(Sale::amount));
    }

    // новый TreeMap: менеджеры по алфавиту
    public Map<String, Double> totalsByManager(List<Sale> sales) {
        return sales.stream()
                .collect(Collectors.groupingBy(Sale::manager, TreeMap::new, Collectors.summingDouble(Sale::amount)));
    }

    // нет продаж в регионе - 0
    public double averageByRegion(List<Sale> sales, String region) {
        return sales.stream()
                .filter(s -> s.region().equals(region))
                .mapToDouble(Sale::amount)
                .average()
                .orElse(0.0);
    }

    // без побочного эффекта: результат собирает toList, внешний список не нужен
    public List<Sale> largeSales(List<Sale> sales) {
        return sales.stream().filter(this::isLarge).toList();
    }

    public boolean isLarge(Sale s) {
        return s.amount() >= LARGE_DEAL_MIN;
    }
}

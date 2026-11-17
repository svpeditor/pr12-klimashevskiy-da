package ru.edu.pr09;

public record Sale(String id, String manager, String region, double amount) {
    public Sale {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Пустой id продажи");
        }
        if (manager == null || manager.isBlank() || region == null || region.isBlank()) {
            throw new IllegalArgumentException("Продажа " + id + ": не указан менеджер или регион");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Продажа " + id + ": отрицательная сумма " + amount);
        }
    }
}

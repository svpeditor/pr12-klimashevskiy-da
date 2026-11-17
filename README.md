# Практическая работа №12. Stream API, Comparator, Optional и функциональная обработка коллекций

Климашевский Даниил Александрович, группа ЭФБО-07-25, вариант 4.

Аналитика по списку продаж через Stream API: топ продаж с фильтром и сортировкой по двум полям, максимальная продажа в Optional, суммы по менеджерам через groupingBy, среднее по региону. Исходный список не меняется. По варианту 4 крупная сделка начинается от 25 000 рублей.

## Сборка и запуск

Нужна Java 21 или новее.

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out ru.edu.pr09.SelfCheck
```

Или скриптом: `./run_selfcheck.sh` (macOS/Linux), `run_selfcheck.bat` (Windows).

Сначала выполняются проверки, потом обрабатывается `data/sample.csv` (формат `id;manager;region;amount`, первая строка - заголовок).
Свой файл: `java -cp out ru.edu.pr09.SelfCheck data/boundaries.csv`. 

Пакет называется `ru.edu.pr09`, как в исходном проекте.

## Что где лежит

- `src/ru/edu/pr09/SalesAnalyticsService.java` - выборки, сортировка, агрегаты
- `src/ru/edu/pr09/Sale.java` - продажа с проверками
- `src/ru/edu/pr09/SelfCheck.java` - проверки и аналитика по файлу
- `data/sample.csv` - открытый набор из задания, `data/boundaries.csv` - мои граничные значения
- `diagrams/` - activity-диаграмма и диаграмма классов (dot и png)
- `DECISIONS.md` - вариант, два решения, граничные проверки, ответы на вопросы задания
- `REPORT.docx` - отчет

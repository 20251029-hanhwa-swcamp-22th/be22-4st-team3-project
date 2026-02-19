package com.mycompany._thstudy.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalMonthlyComparisonSeedRunner implements CommandLineRunner {

    private static final String SEED_PREFIX = "[SEED-MONTH-COMPARE]";
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        ensureSeedUserExists();
        ensureCategoriesForAllUsers();
        replaceSeedTransactionsForAllUsers();
        log.info("Local monthly comparison seed data applied.");
    }

    private void ensureSeedUserExists() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ?",
                Integer.class,
                "seed@local.test"
        );

        if (count != null && count > 0) {
            return;
        }

        jdbcTemplate.update(
                "INSERT INTO users(email, password, nickname, role, created_at, updated_at) VALUES (?, ?, ?, ?, NOW(6), NOW(6))",
                "seed@local.test",
                passwordEncoder.encode("seed1234!"),
                "seed-user",
                "USER"
        );
    }

    private void ensureCategoriesForAllUsers() {
        List<SeedCategory> categories = List.of(
                new SeedCategory("SALARY", "INCOME"),
                new SeedCategory("BONUS", "INCOME"),
                new SeedCategory("FOOD", "EXPENSE"),
                new SeedCategory("TRANSPORT", "EXPENSE"),
                new SeedCategory("CAFE", "EXPENSE"),
                new SeedCategory("SHOPPING", "EXPENSE")
        );

        for (SeedCategory category : categories) {
            jdbcTemplate.update(
                    """
                    INSERT INTO categories(user_id, name, type, created_at)
                    SELECT u.id, ?, ?, NOW(6)
                    FROM users u
                    LEFT JOIN categories c
                      ON c.user_id = u.id
                     AND c.name = ?
                     AND c.type = ?
                    WHERE c.id IS NULL
                    """,
                    category.name(),
                    category.type(),
                    category.name(),
                    category.type()
            );
        }
    }

    private void replaceSeedTransactionsForAllUsers() {
        jdbcTemplate.update("DELETE FROM transactions WHERE description LIKE ?", SEED_PREFIX + "%");

        YearMonth currentMonth = YearMonth.now();
        YearMonth previousMonth = currentMonth.minusMonths(1);

        List<SeedTransaction> templates = List.of(
                new SeedTransaction("SALARY", "INCOME", 3_500_000L, SEED_PREFIX + " prev salary", previousMonth.atDay(5)),
                new SeedTransaction("BONUS", "INCOME", 250_000L, SEED_PREFIX + " prev bonus", previousMonth.atDay(20)),
                new SeedTransaction("FOOD", "EXPENSE", 420_000L, SEED_PREFIX + " prev food", previousMonth.atDay(4)),
                new SeedTransaction("TRANSPORT", "EXPENSE", 110_000L, SEED_PREFIX + " prev transport", previousMonth.atDay(9)),
                new SeedTransaction("CAFE", "EXPENSE", 95_000L, SEED_PREFIX + " prev cafe", previousMonth.atDay(14)),
                new SeedTransaction("SHOPPING", "EXPENSE", 230_000L, SEED_PREFIX + " prev shopping", previousMonth.atDay(25)),
                new SeedTransaction("SALARY", "INCOME", 3_500_000L, SEED_PREFIX + " current salary", currentMonth.atDay(5)),
                new SeedTransaction("BONUS", "INCOME", 120_000L, SEED_PREFIX + " current bonus", currentMonth.atDay(18)),
                new SeedTransaction("FOOD", "EXPENSE", 360_000L, SEED_PREFIX + " current food", currentMonth.atDay(3)),
                new SeedTransaction("TRANSPORT", "EXPENSE", 100_000L, SEED_PREFIX + " current transport", currentMonth.atDay(8)),
                new SeedTransaction("CAFE", "EXPENSE", 80_000L, SEED_PREFIX + " current cafe", currentMonth.atDay(15)),
                new SeedTransaction("SHOPPING", "EXPENSE", 310_000L, SEED_PREFIX + " current shopping", currentMonth.atDay(22))
        );

        for (SeedTransaction tx : templates) {
            jdbcTemplate.update(
                    """
                    INSERT INTO transactions(user_id, category_id, type, amount, description, transaction_date, created_at, updated_at)
                    SELECT u.id, c.id, ?, ?, ?, ?, NOW(6), NOW(6)
                    FROM users u
                    JOIN categories c
                      ON c.user_id = u.id
                     AND c.name = ?
                     AND c.type = ?
                    """,
                    tx.type(),
                    tx.amount(),
                    tx.description(),
                    tx.transactionDate(),
                    tx.categoryName(),
                    tx.type()
            );
        }
    }

    private record SeedCategory(String name, String type) {
    }

    private record SeedTransaction(
            String categoryName,
            String type,
            Long amount,
            String description,
            LocalDate transactionDate
    ) {
    }
}

package com.ptda.tracker.repositories;

import com.ptda.tracker.models.tracker.Expense;
import com.ptda.tracker.models.tracker.ExpenseDivision;
import com.ptda.tracker.models.user.User;
import com.ptda.tracker.util.UserSession;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExpenseDivisionRepositoryTest {

    private final ExpenseDivisionRepository expenseDivisionRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    @Test
    void testSaveAndFindByExpenseId() {
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .password("password")
                .build();
        userRepository.save(user);
        assertThat(user.getId()).isNotNull();

        UserSession.getInstance().setUser(user);

        Expense expense = Expense.builder()
                .description("Test Expense")
                .amount(100.0)
                .build();
        expenseRepository.save(expense);
        assertThat(expense.getId()).isNotNull();

        ExpenseDivision expenseDivision = ExpenseDivision.builder()
                .amount(50.0)
                .equalDivision(true)
                .paidAmount(0.0)
                .paidAll(false)
                .expense(expense)
                .user(user)
                .build();
        expenseDivisionRepository.save(expenseDivision);
        assertThat(expenseDivision.getId()).isNotNull();

        List<ExpenseDivision> divisions = expenseDivisionRepository.findAllByExpenseId(expense.getId());
        assertThat(divisions).isNotEmpty();
        assertThat(divisions.get(0).getAmount()).isEqualTo(50.0);
    }

    @Test
    void testMultipleDivisionsByExpenseId() {
        User user1 = User.builder()
                .name("User 1")
                .email("user1@example.com")
                .password("password")
                .build();
        userRepository.save(user1);

        UserSession.getInstance().setUser(user1);

        User user2 = User.builder()
                .name("User 2")
                .email("user2@example.com")
                .password("password")
                .build();
        userRepository.save(user2);

        UserSession.getInstance().setUser(user2);

        Expense expense = Expense.builder()
                .description("Shared Expense")
                .amount(200.0)
                .createdBy(user1)
                .build();
        expenseRepository.save(expense);

        ExpenseDivision division1 = ExpenseDivision.builder()
                .amount(100.0)
                .equalDivision(true)
                .paidAmount(0.0)
                .paidAll(false)
                .expense(expense)
                .user(user1)
                .build();
        expenseDivisionRepository.save(division1);

        ExpenseDivision division2 = ExpenseDivision.builder()
                .amount(100.0)
                .equalDivision(true)
                .paidAmount(0.0)
                .paidAll(false)
                .expense(expense)
                .user(user2)
                .build();
        expenseDivisionRepository.save(division2);

        List<ExpenseDivision> divisions = expenseDivisionRepository.findAllByExpenseId(expense.getId());
        assertThat(divisions).hasSize(2);
        assertThat(divisions).extracting("user.name").containsExactlyInAnyOrder("User 1", "User 2");
    }

    @Test
    void testDeleteAllDivisionsByExpenseId() {
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .password("password")
                .build();
        userRepository.save(user);

        UserSession.getInstance().setUser(user);

        Expense expense = Expense.builder()
                .description("Test Expense")
                .amount(100.0)
                .createdBy(user)
                .build();
        expenseRepository.save(expense);

        ExpenseDivision division1 = ExpenseDivision.builder()
                .amount(50.0)
                .equalDivision(true)
                .paidAmount(0.0)
                .paidAll(false)
                .expense(expense)
                .user(user)
                .build();
        expenseDivisionRepository.save(division1);

        ExpenseDivision division2 = ExpenseDivision.builder()
                .amount(50.0)
                .equalDivision(true)
                .paidAmount(0.0)
                .paidAll(false)
                .expense(expense)
                .user(user)
                .build();
        expenseDivisionRepository.save(division2);

        expenseDivisionRepository.deleteByExpenseId(expense.getId());
        List<ExpenseDivision> divisions = expenseDivisionRepository.findAllByExpenseId(expense.getId());
        assertThat(divisions).isEmpty();
    }

    @Test
    void testExpenseDivisionExistsByID() {
        User user = User.builder()
                .name("Test User")
                .email("test@email.com")
                .password("password")
                .build();
        userRepository.save(user);

        UserSession.getInstance().setUser(user);

        Expense expense = Expense.builder()
                .description("Test Expense")
                .amount(100.0)
                .build();
        expenseRepository.save(expense);

        ExpenseDivision division = ExpenseDivision.builder()
                .amount(50.0)
                .equalDivision(true)
                .paidAmount(0.0)
                .paidAll(false)
                .expense(expense)
                .user(user)
                .build();
        expenseDivisionRepository.save(division);

        assertThat(expenseDivisionRepository.existsById(division.getId())).isTrue();
    }

    @Test
    void testFindById() {
        User user = User.builder()
                .name("Test User")
                .email("test@email.com")
                .password("password")
                .build();
        userRepository.save(user);

        UserSession.getInstance().setUser(user);

        Expense expense = Expense.builder()
                .description("Test Expense")
                .amount(100.0)
                .build();
        expenseRepository.save(expense);

        ExpenseDivision division = ExpenseDivision.builder()
                .amount(50.0)
                .equalDivision(true)
                .paidAmount(0.0)
                .paidAll(false)
                .expense(expense)
                .user(user)
                .build();
        expenseDivisionRepository.save(division);

        ExpenseDivision foundDivision = expenseDivisionRepository.findById(division.getId()).orElse(null);
        assertThat(foundDivision).isNotNull();
        assertThat(foundDivision.getAmount()).isEqualTo(50.0);

        assertThat(expenseDivisionRepository.findById(0L)).isEmpty();

    }

    @Test
    void testSaveAll() {
        User user = User.builder()
                .name("Test User")
                .email("test@email.com")
                .password("password")
                .build();
        userRepository.save(user);

        UserSession.getInstance().setUser(user);

        Expense expense = Expense.builder()
                .description("Test Expense")
                .amount(100.0)
                .build();

        expenseRepository.save(expense);

        ExpenseDivision division1 = ExpenseDivision.builder()
                .amount(50.0)
                .equalDivision(true)
                .paidAmount(0.0)
                .paidAll(false)
                .expense(expense)
                .user(user)
                .build();

        ExpenseDivision division2 = ExpenseDivision.builder()
                .amount(50.0)
                .equalDivision(true)
                .paidAmount(0.0)
                .paidAll(false)
                .expense(expense)
                .user(user)
                .build();

        List<ExpenseDivision> divisions = List.of(division1, division2);
        expenseDivisionRepository.saveAll(divisions);

        List<ExpenseDivision> foundDivisions = expenseDivisionRepository.findAllByExpenseId(expense.getId());
        assertThat(foundDivisions).hasSize(2);

    }

    @Test
    void testDeleteById() {
        User user = User.builder()
                .name("Test User")
                .email("test@user.com")
                .password("password")
                .build();
        userRepository.save(user);

        UserSession.getInstance().setUser(user);

        Expense expense = Expense.builder()
                .description("Test Expense")
                .amount(100.0)
                .build();
        expenseRepository.save(expense);

        ExpenseDivision division = ExpenseDivision.builder()
                .amount(50.0)
                .equalDivision(true)
                .paidAmount(0.0)
                .paidAll(false)
                .expense(expense)
                .user(user)
                .build();
        expenseDivisionRepository.save(division);

        assertThat(expenseDivisionRepository.existsById(division.getId())).isTrue();

        expenseDivisionRepository.deleteById(division.getId());

        assertThat(expenseDivisionRepository.existsById(division.getId())).isFalse();

    }

    @Test
    void deleteAllByExpenseId(){
        User user = User.builder()
                .name("Test User")
                .email("email@test.com")
                .password("password")
                .build();
        userRepository.save(user);

        UserSession.getInstance().setUser(user);

        Expense expense = Expense.builder()
                .description("Test Expense")
                .amount(100.0)
                .build();
        expenseRepository.save(expense);

        ExpenseDivision division1 = ExpenseDivision.builder()
                .amount(50.0)
                .equalDivision(true)
                .paidAmount(0.0)
                .paidAll(false)
                .expense(expense)
                .user(user)
                .build();

        ExpenseDivision division2 = ExpenseDivision.builder()
                .amount(50.0)
                .equalDivision(true)
                .paidAmount(0.0)
                .paidAll(false)
                .expense(expense)
                .user(user)
                .build();

        expenseDivisionRepository.save(division1);
        expenseDivisionRepository.save(division2);

        assertThat(expenseDivisionRepository.findAllByExpenseId(expense.getId())).hasSize(2);

        expenseDivisionRepository.deleteAllByExpenseId(expense.getId());

        assertThat(expenseDivisionRepository.findAllByExpenseId(expense.getId())).isEmpty();

    }


}
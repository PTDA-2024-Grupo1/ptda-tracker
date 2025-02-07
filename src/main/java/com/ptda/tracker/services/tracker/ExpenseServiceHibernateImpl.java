package com.ptda.tracker.services.tracker;

import com.ptda.tracker.models.tracker.Budget;
import com.ptda.tracker.models.tracker.Expense;
import com.ptda.tracker.models.tracker.ExpenseDivision;
import com.ptda.tracker.repositories.BudgetRepository;
import com.ptda.tracker.repositories.ExpenseRepository;
import com.ptda.tracker.repositories.ExpenseDivisionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseServiceHibernateImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseDivisionService expenseDivisionService;
    private final BudgetRepository budgetRepository;

    @Override
    @Transactional
    public Expense create(Expense expense) {
        return expenseRepository.save(expense);
    }

    @Override
    @Transactional
    public List<Expense> createAll(List<Expense> expenses) {
        return expenseRepository.saveAll(expenses);
    }

    @Override
    public Optional<Expense> getById(Long id) {
        return expenseRepository.findById(id);
    }

    @Override
    public List<Expense> getAll() {
        return expenseRepository.findAll();
    }

    @Override
    public int getCount() {
        return (int) expenseRepository.count();
    }

    @Override
    public List<Expense> getAllByBudgetId(Long budgetId) {
        return expenseRepository.findAllByBudgetId(budgetId);
    }

    @Override
    public List<Expense> getAllByUserId(Long userId) {
        return expenseRepository.findAllByCreatedById(userId);
    }

    @Override
    public List<Expense> getRecentExpensesByUserId(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return expenseRepository.findTopByCreatedByIdOrderByDateDesc(userId, pageable);
    }

    @Override
    public List<Expense> getPersonalExpensesByUserId(Long userId) {
        return expenseRepository.findAllByCreatedByIdAndBudgetNull(userId);
    }

    @Override
    public double getTotalExpenseAmountByBudgetId(Long budgetId) {
        return expenseRepository.findAllByBudgetId(budgetId).stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }

    @Override
    public Map<String, Double> getExpensesByCategory(Long userId) {
        List<Expense> expenses = expenseRepository.findAllByCreatedById(userId);
        return expenses.stream()
                .collect(Collectors.groupingBy(
                        expense -> expense.getCategory().toString(),
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }

    @Override
    public List<Expense> getExpensesByBudgetIdWithPagination(Long budgetId, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        return expenseRepository.findByBudgetIdOrderByDateDesc(budgetId, pageable);
    }

    @Override
    public List<Expense> getPersonalExpensesByUserIdWithPagination(Long userId, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        return expenseRepository.findByCreatedByIdAndBudgetNullOrderByDateDesc(userId, pageable);
    }

    @Override
    public int getCountByBudgetId(Long id) {
        return expenseRepository.countByBudgetId(id);
    }

    @Override
    public int getCountByUserId(Long userId) {
        return expenseRepository.countByCreatedById(userId);
    }

    @Override
    public int getCountByUserIdPersonal(Long userId) {
        return expenseRepository.countByCreatedByIdAndBudgetNull(userId);
    }

    @Override
    @Transactional
    public Expense update(Expense expense) {
        return expenseRepository.save(expense);
    }

    @Override
    @Transactional
    public Expense update(Expense expense, boolean updateDivisions) {
        if (expense.getBudget() == null) {
            throw new IllegalArgumentException("Budget must not be null.");
        }
        if (updateDivisions) {
            Optional<Expense> optionalExpense = expenseRepository.findById(expense.getId());
            if (optionalExpense.isPresent() && expense.getAmount() != optionalExpense.get().getAmount()) {
                List<ExpenseDivision> divisions = expenseDivisionService.getAllByExpenseId(expense.getId());
                double proportion = expense.getAmount() / optionalExpense.get().getAmount();
                for (ExpenseDivision division : divisions) {
                    division.setAmount(division.getAmount() * proportion);
                    division.setPaidAmount(division.getPaidAmount() * proportion);
                }
                expenseDivisionService.updateAll(divisions);
            }
        } else {
            expenseDivisionService.deleteAllByExpenseId(expense.getId());
        }
        return expenseRepository.save(expense);
    }

    @Override
    public List<Expense> updateAll(List<Expense> expenses) {
        return expenseRepository.saveAll(expenses);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        Optional<Expense> optionalExpense = expenseRepository.findById(id);
        if (optionalExpense.isPresent()) {
            // Delete related subdivisions first
            expenseDivisionService.deleteAllByExpenseId(id);
            // Then delete the expense
            expenseRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteAllPersonalExpensesByUserId(Long userId) {
        List<Expense> personalExpenses = expenseRepository.findAllByCreatedByIdAndBudgetNull(userId);
        expenseRepository.deleteAll(personalExpenses);
        return true;
    }
}
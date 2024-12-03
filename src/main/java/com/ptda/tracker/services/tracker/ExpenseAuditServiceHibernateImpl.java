package com.ptda.tracker.services.tracker;

import com.ptda.tracker.models.tracker.Expense;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseAuditServiceHibernateImpl implements ExpenseAuditService {

    @PersistenceContext
    private final EntityManager entityManager;

    public List<Object[]> getExpenseRevisionsWithDetails(Long expenseId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        AuditQuery query = auditReader.createQuery()
                .forRevisionsOfEntity(Expense.class, false, true)
                .add(AuditEntity.id().eq(expenseId));
        return query.getResultList();
    }

    public List<Number> getExpenseRevisions(Long expenseId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        return auditReader.getRevisions(Expense.class, expenseId);
    }

    public Expense getExpenseAtRevision(Long expenseId, Number revision) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        return auditReader.find(Expense.class, expenseId, revision);
    }
}

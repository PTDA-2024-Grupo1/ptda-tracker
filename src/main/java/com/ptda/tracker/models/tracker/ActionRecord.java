package com.ptda.tracker.models.tracker;

import com.ptda.tracker.models.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionRecord {

    @Id
    private Long id;

    private ActionType actionType;

    private String objectBefore;

    @ManyToOne
    private Budget budget;

    @ManyToOne
    private Expense expense;

    @ManyToOne
    private User createdBy;

    private long createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = System.currentTimeMillis();
    }

}

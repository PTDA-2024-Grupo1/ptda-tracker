package com.ptda.tracker.repositories;

import com.ptda.tracker.models.assistance.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findAllByCreatedById(Long createdById);

    List<Ticket> findAllByIsClosedIsFalseAndCreatedById(Long createdById);

    int countByCreatedByIdAndIsClosed(Long userId, boolean isClosed);
}
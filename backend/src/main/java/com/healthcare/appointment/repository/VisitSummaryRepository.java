package com.healthcare.appointment.repository;

import com.healthcare.appointment.entity.VisitSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitSummaryRepository extends JpaRepository<VisitSummary, Long> {
}

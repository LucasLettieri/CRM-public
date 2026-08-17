package com.crmVs.crm_vs.repository;

import com.crmVs.crm_vs.model.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    List<Interaction> findByLeadId(Long leadId);
}
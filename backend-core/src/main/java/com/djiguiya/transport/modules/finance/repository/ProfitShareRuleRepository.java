package com.djiguiya.transport.modules.finance.repository;

import com.djiguiya.transport.modules.finance.entities.ProfitShareRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProfitShareRuleRepository extends JpaRepository<ProfitShareRuleEntity, String> {

	@Query("""
			select r from ProfitShareRuleEntity r
			where r.enabled = true
			and r.effectiveFrom <= :onDate
			and (r.effectiveTo is null or r.effectiveTo >= :onDate)
			""")
	List<ProfitShareRuleEntity> findActiveOnDate(@Param("onDate") LocalDate onDate);
}

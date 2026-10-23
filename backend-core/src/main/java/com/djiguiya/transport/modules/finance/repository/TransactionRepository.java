package com.djiguiya.transport.modules.finance.repository;

import com.djiguiya.transport.modules.finance.entities.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * JPA access to ledger rows. All analytics queries exclude soft-deleted records
 * ({@code deletedAt} is null).
 */
public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {

	/** Active row by primary key. */
	Optional<TransactionEntity> findByIdAndDeletedAtIsNull(String id);

	/** Full ledger listing for admin views. */
	List<TransactionEntity> findByDeletedAtIsNullOrderByTxnDateDesc();

	/**
	 * Optional filters: null parameters are ignored. Used by stats summary and ad-hoc reporting.
	 */
	@Query("""
			select t from TransactionEntity t
			where t.deletedAt is null
			and (:monthKey is null or t.monthKey = :monthKey)
			and (:vehicleId is null or t.vehicleId = :vehicleId)
			and (:driverId is null or t.driverId = :driverId)
			and (:categoryCode is null or t.categoryCode = :categoryCode)
			and (:type is null or t.type = :type)
			""")
	List<TransactionEntity> findFiltered(
			@Param("monthKey") String monthKey,
			@Param("vehicleId") String vehicleId,
			@Param("driverId") String driverId,
			@Param("categoryCode") String categoryCode,
			@Param("type") String type);

	/** Matches {@code monthKey} values starting with {@code yearPrefix} (e.g. calendar year). */
	@Query("""
			select t from TransactionEntity t
			where t.deletedAt is null
			and t.monthKey like concat(:yearPrefix, '%')
			and (:vehicleId is null or t.vehicleId = :vehicleId)
			and (:driverId is null or t.driverId = :driverId)
			""")
	List<TransactionEntity> findByYearPrefix(
			@Param("yearPrefix") String yearPrefix,
			@Param("vehicleId") String vehicleId,
			@Param("driverId") String driverId);

	/**
	 * Inclusive {@code monthKey} range for fiscal-year loads ({@code yyyy-MM} lexicographic order).
	 */
	@Query("""
			select t from TransactionEntity t
			where t.deletedAt is null
			and t.monthKey >= :fromMonth
			and t.monthKey <= :toMonth
			and (:vehicleId is null or t.vehicleId = :vehicleId)
			and (:driverId is null or t.driverId = :driverId)
			""")
	List<TransactionEntity> findByMonthKeyRange(
			@Param("fromMonth") String fromMonth,
			@Param("toMonth") String toMonth,
			@Param("vehicleId") String vehicleId,
			@Param("driverId") String driverId);
}

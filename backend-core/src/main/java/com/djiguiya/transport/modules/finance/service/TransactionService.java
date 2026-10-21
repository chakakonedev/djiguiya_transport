package com.djiguiya.transport.modules.finance.service;

import com.djiguiya.transport.modules.finance.domain.TransactionTypes;
import com.djiguiya.transport.modules.finance.dto.TransactionRequest;
import com.djiguiya.transport.modules.finance.dto.TransactionResponse;
import com.djiguiya.transport.modules.finance.entities.TransactionEntity;
import com.djiguiya.transport.modules.finance.repository.TransactionRepository;
import com.djiguiya.transport.modules.trips.service.TripService;
import com.djiguiya.transport.shared.exception.BusinessException;
import com.djiguiya.transport.shared.security.SecurityUtils;
import com.djiguiya.transport.shared.util.EntityIds;
import com.djiguiya.transport.shared.util.MonthKeyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final TripService tripService;

	@Transactional(readOnly = true)
	public List<TransactionResponse> listAll() {
		return transactionRepository.findByDeletedAtIsNullOrderByTxnDateDesc().stream()
				.map(TransactionResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public TransactionResponse getById(String id) {
		return TransactionResponse.from(findEntity(id));
	}

	@Transactional
	public TransactionResponse create(TransactionRequest request) {
		validateBusinessRules(request);
		TransactionEntity entity = new TransactionEntity();
		entity.setId(EntityIds.newId());
		apply(entity, enrichFromTrip(request));
		SecurityUtils.currentUserId().ifPresent(entity::setCreatedBy);
		return TransactionResponse.from(transactionRepository.save(entity));
	}

	@Transactional
	public TransactionResponse update(String id, TransactionRequest request) {
		validateBusinessRules(request);
		TransactionEntity entity = findEntity(id);
		apply(entity, enrichFromTrip(request));
		return TransactionResponse.from(transactionRepository.save(entity));
	}

	@Transactional
	public void delete(String id) {
		TransactionEntity entity = findEntity(id);
		entity.setDeletedAt(Instant.now());
		transactionRepository.save(entity);
	}

	public TransactionEntity findEntity(String id) {
		return transactionRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new BusinessException("transaction.not_found", HttpStatus.NOT_FOUND));
	}

	private void validateBusinessRules(TransactionRequest request) {
		if (TransactionTypes.REVENUE.equalsIgnoreCase(request.type())) {
			if (request.tripId() == null || request.tripId().isBlank()) {
				throw new BusinessException("transaction.revenue_requires_trip", HttpStatus.BAD_REQUEST);
			}
			var trip = tripService.findEntity(request.tripId());
			if (request.txnDate().isBefore(trip.getDepartureDate())
					|| request.txnDate().isBefore(trip.getReturnDate())) {
				throw new BusinessException("transaction.payment_before_trip", HttpStatus.BAD_REQUEST);
			}
		}
		if (TransactionTypes.EXPENSE.equalsIgnoreCase(request.type())) {
			if (request.categoryCode() == null || request.categoryCode().isBlank()) {
				throw new BusinessException("common.validation_failed", HttpStatus.BAD_REQUEST);
			}
			if ("other".equalsIgnoreCase(request.subcategoryCode())) {
				if (blankToNull(request.label()) == null || blankToNull(request.notes()) == null) {
					throw new BusinessException("transaction.other_requires_details", HttpStatus.BAD_REQUEST);
				}
			}
		}
	}

	/** Copy vehicle/driver from the linked trip when omitted on a revenue entry. */
	private TransactionRequest enrichFromTrip(TransactionRequest request) {
		if (!TransactionTypes.REVENUE.equalsIgnoreCase(request.type())
				|| request.tripId() == null
				|| request.tripId().isBlank()) {
			return request;
		}
		var trip = tripService.findEntity(request.tripId());
		String vehicleId = blankToNull(request.vehicleId()) != null ? request.vehicleId() : trip.getVehicleId();
		String driverId = blankToNull(request.driverId()) != null ? request.driverId() : trip.getDriverId();
		return new TransactionRequest(
				request.type(),
				request.txnDate(),
				request.amount(),
				vehicleId,
				driverId,
				request.tripId(),
				request.categoryCode(),
				request.subcategoryCode(),
				request.label(),
				request.payrollMonth(),
				request.payrollKind(),
				request.notes());
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value;
	}

	private static String resolveLabel(TransactionRequest request) {
		if (request.label() != null && !request.label().isBlank()) {
			return request.label().trim();
		}
		return TransactionTypes.REVENUE.equalsIgnoreCase(request.type())
				? "Versement voyage"
				: "Dépense";
	}

	private void apply(TransactionEntity entity, TransactionRequest request) {
		entity.setType(request.type().toLowerCase());
		entity.setTxnDate(request.txnDate());
		entity.setMonthKey(MonthKeyUtil.fromDate(request.txnDate()));
		entity.setAmount(request.amount());
		entity.setVehicleId(request.vehicleId());
		entity.setDriverId(request.driverId());
		entity.setTripId(request.tripId());
		entity.setCategoryCode(request.categoryCode());
		entity.setSubcategoryCode(request.subcategoryCode());
		entity.setLabel(resolveLabel(request));
		entity.setPayrollMonth(request.payrollMonth());
		entity.setPayrollKind(request.payrollKind());
		entity.setNotes(request.notes());
	}
}

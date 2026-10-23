package com.djiguiya.transport.modules.finance.service;

import com.djiguiya.transport.modules.finance.domain.TransactionTypes;
import com.djiguiya.transport.modules.finance.dto.StatsCompareMonthResponse;
import com.djiguiya.transport.modules.finance.dto.StatsSummaryResponse;
import com.djiguiya.transport.modules.finance.dto.StatsYoyResponse;
import com.djiguiya.transport.modules.finance.entities.TransactionEntity;
import com.djiguiya.transport.modules.finance.repository.TransactionRepository;
import com.djiguiya.transport.shared.util.FiscalYearUtil;
import com.djiguiya.transport.shared.util.MonthKeyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

	private final TransactionRepository transactionRepository;

	@Transactional(readOnly = true)
	public StatsSummaryResponse summary(
			String vehicleId,
			String driverId,
			Integer month,
			Integer year,
			String category) {
		String monthKey = resolveMonthKey(month, year);
		List<TransactionEntity> rows = loadForSummary(monthKey, year, vehicleId, driverId, category);
		long revenue = LedgerAggregator.totalRevenue(rows);
		long expense = LedgerAggregator.totalExpense(rows);
		return new StatsSummaryResponse(
				monthKey,
				year,
				vehicleId,
				driverId,
				category,
				revenue,
				expense,
				revenue - expense,
				countTrips(rows),
				rows.size());
	}

	/** Distinct trips linked to revenue rows in the filtered ledger. */
	private static int countTrips(List<TransactionEntity> rows) {
		return (int) rows.stream()
				.filter(t -> TransactionTypes.REVENUE.equalsIgnoreCase(t.getType()))
				.map(TransactionEntity::getTripId)
				.filter(Objects::nonNull)
				.filter(id -> !id.isBlank())
				.distinct()
				.count();
	}

	@Transactional(readOnly = true)
	public StatsCompareMonthResponse compareMonth(int monthIndex, String vehicleId, String driverId) {
		int year = LocalDate.now().getYear();
		YearMonth current = YearMonth.of(year, monthIndex);
		YearMonth previous = current.minusMonths(1);
		String currentKey = MonthKeyUtil.forMonthYear(monthIndex, year);
		String previousKey = MonthKeyUtil.forMonthYear(previous.getMonthValue(), previous.getYear());
		List<TransactionEntity> currentRows = transactionRepository.findFiltered(currentKey, vehicleId, driverId, null, null);
		List<TransactionEntity> previousRows = transactionRepository.findFiltered(previousKey, vehicleId, driverId, null, null);
		long currentNet = LedgerAggregator.netProfit(currentRows);
		long previousNet = LedgerAggregator.netProfit(previousRows);
		return new StatsCompareMonthResponse(
				monthIndex,
				year,
				currentKey,
				previousKey,
				currentNet,
				previousNet,
				currentNet - previousNet);
	}

	@Transactional(readOnly = true)
	public StatsYoyResponse yearOverYear(int fiscalStartYear, String vehicleId, String driverId) {
		List<TransactionEntity> current = loadFiscalYear(fiscalStartYear, vehicleId, driverId);
		List<TransactionEntity> previous = loadFiscalYear(fiscalStartYear - 1, vehicleId, driverId);
		long currentNet = LedgerAggregator.netProfit(current);
		long previousNet = LedgerAggregator.netProfit(previous);
		Double pct = previousNet == 0 ? null : ((double) (currentNet - previousNet) / previousNet) * 100.0;
		return new StatsYoyResponse(
				fiscalStartYear,
				fiscalStartYear - 1,
				currentNet,
				previousNet,
				currentNet - previousNet,
				pct);
	}

	private List<TransactionEntity> loadForSummary(
			String monthKey,
			Integer year,
			String vehicleId,
			String driverId,
			String category) {
		if (monthKey != null) {
			return transactionRepository.findFiltered(monthKey, vehicleId, driverId, category, null);
		}
		if (year != null) {
			return loadFiscalYear(year, vehicleId, driverId).stream()
					.filter(t -> category == null || category.equals(t.getCategoryCode()))
					.collect(Collectors.toList());
		}
		return transactionRepository.findFiltered(null, vehicleId, driverId, category, null);
	}

	private List<TransactionEntity> loadFiscalYear(int fiscalStartYear, String vehicleId, String driverId) {
		return transactionRepository.findByMonthKeyRange(
				FiscalYearUtil.startMonthKey(fiscalStartYear),
				FiscalYearUtil.endMonthKey(fiscalStartYear),
				vehicleId,
				driverId);
	}

	private String resolveMonthKey(Integer month, Integer year) {
		if (month == null || year == null) {
			return null;
		}
		return MonthKeyUtil.forMonthYear(month, year);
	}
}

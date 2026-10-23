package com.djiguiya.transport.modules.finance.service;

import com.djiguiya.transport.modules.finance.domain.TransactionTypes;
import com.djiguiya.transport.modules.finance.dto.StatsGroupRowResponse;
import com.djiguiya.transport.modules.finance.entities.TransactionEntity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class LedgerAggregator {

	private LedgerAggregator() {
	}

	public static long totalRevenue(List<TransactionEntity> transactions) {
		return sumByType(transactions, TransactionTypes.REVENUE);
	}

	public static long totalExpense(List<TransactionEntity> transactions) {
		return sumByType(transactions, TransactionTypes.EXPENSE);
	}

	public static long netProfit(List<TransactionEntity> transactions) {
		return totalRevenue(transactions) - totalExpense(transactions);
	}

	/** Expense totals grouped by category (then subcategory). */
	public static List<StatsGroupRowResponse> expenseByGroup(List<TransactionEntity> transactions) {
		Map<String, Long> byCategory = new LinkedHashMap<>();
		Map<String, Long> bySubcategory = new LinkedHashMap<>();
		for (TransactionEntity t : transactions) {
			if (!TransactionTypes.EXPENSE.equalsIgnoreCase(t.getType())) {
				continue;
			}
			String category = blankToOther(t.getCategoryCode());
			byCategory.merge(category, t.getAmount(), Long::sum);
			String sub = t.getSubcategoryCode();
			if (sub != null && !sub.isBlank()) {
				bySubcategory.merge(category + "|" + sub, t.getAmount(), Long::sum);
			}
		}
		List<StatsGroupRowResponse> rows = byCategory.entrySet().stream()
				.sorted(Map.Entry.<String, Long>comparingByValue().reversed())
				.map(e -> new StatsGroupRowResponse(e.getKey(), null, e.getValue()))
				.collect(Collectors.toList());
		bySubcategory.entrySet().stream()
				.sorted(Map.Entry.<String, Long>comparingByValue().reversed())
				.forEach(e -> {
					String[] parts = e.getKey().split("\\|", 2);
					rows.add(new StatsGroupRowResponse(parts[0], parts[1], e.getValue()));
				});
		return rows;
	}

	private static String blankToOther(String code) {
		return code == null || code.isBlank() ? "other" : code;
	}

	private static long sumByType(List<TransactionEntity> transactions, String type) {
		return transactions.stream()
				.filter(t -> type.equalsIgnoreCase(t.getType()))
				.mapToLong(TransactionEntity::getAmount)
				.sum();
	}
}

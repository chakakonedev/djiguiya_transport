package com.djiguiya.transport.modules.finance.service;

import com.djiguiya.transport.modules.finance.domain.TransactionTypes;
import com.djiguiya.transport.modules.finance.entities.TransactionEntity;

import java.util.List;

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

	private static long sumByType(List<TransactionEntity> transactions, String type) {
		return transactions.stream()
				.filter(t -> type.equalsIgnoreCase(t.getType()))
				.mapToLong(TransactionEntity::getAmount)
				.sum();
	}
}

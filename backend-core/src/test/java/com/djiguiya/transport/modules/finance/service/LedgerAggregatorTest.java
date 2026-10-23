package com.djiguiya.transport.modules.finance.service;

import com.djiguiya.transport.modules.finance.domain.TransactionTypes;
import com.djiguiya.transport.modules.finance.entities.TransactionEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LedgerAggregatorTest {

	@Test
	void aggregatesRevenueExpenseAndGroupsBlankCategoryAsOther() {
		TransactionEntity revenue = txn(TransactionTypes.REVENUE, 1000L, null, null);
		TransactionEntity busFuel = txn(TransactionTypes.EXPENSE, 100L, "bus", "fuel");
		TransactionEntity blankCat = txn(TransactionTypes.EXPENSE, 50L, "  ", null);
		TransactionEntity nullCat = txn(TransactionTypes.EXPENSE, 25L, null, "misc");

		var rows = List.of(revenue, busFuel, blankCat, nullCat);
		assertEquals(1000L, LedgerAggregator.totalRevenue(rows));
		assertEquals(175L, LedgerAggregator.totalExpense(rows));
		assertEquals(825L, LedgerAggregator.netProfit(rows));

		var groups = LedgerAggregator.expenseByGroup(rows);
		assertEquals(true, groups.stream().anyMatch(g -> "bus".equals(g.categoryCode()) && g.subcategoryCode() == null));
		assertEquals(true, groups.stream().anyMatch(g -> "other".equals(g.categoryCode()) && g.subcategoryCode() == null));
		assertEquals(true, groups.stream().anyMatch(g -> "fuel".equals(g.subcategoryCode())));
		assertEquals(true, groups.stream().anyMatch(g -> "misc".equals(g.subcategoryCode())));
	}

	@Test
	void expenseByGroup_ignoresRevenue() {
		var groups = LedgerAggregator.expenseByGroup(List.of(txn(TransactionTypes.REVENUE, 10L, "bus", "fuel")));
		assertEquals(0, groups.size());
	}

	private static TransactionEntity txn(String type, long amount, String category, String sub) {
		TransactionEntity e = new TransactionEntity();
		e.setType(type);
		e.setAmount(amount);
		e.setCategoryCode(category);
		e.setSubcategoryCode(sub);
		return e;
	}
}

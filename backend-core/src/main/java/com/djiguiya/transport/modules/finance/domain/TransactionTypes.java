package com.djiguiya.transport.modules.finance.domain;

/**
 * Canonical ledger {@code type} strings persisted on {@code transactions} and used in aggregations.
 */
public final class TransactionTypes {

	public static final String REVENUE = "revenue";
	public static final String EXPENSE = "expense";

	private TransactionTypes() {
	}
}

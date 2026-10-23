package com.djiguiya.transport.shared.security;

/**
 * JWT roles use ROLE_{CODE} (see JwtAuthenticationFilter).
 * Use these constants in {@code @PreAuthorize("hasRole(...)")}.
 */
public final class AppRoles {

	public static final String OWNER = "OWNER";
	public static final String CASHIER = "CASHIER";
	public static final String ACCOUNTANT = "ACCOUNTANT";
	public static final String CASHIER_ACCOUNTANT = "CASHIER_ACCOUNTANT";
	public static final String DRIVER_MANAGER = "DRIVER_MANAGER";

	/** Roles allowed to create/update/delete ledger entries. */
	public static final String LEDGER_WRITE =
			"hasAnyRole('" + OWNER + "','" + CASHIER + "','" + ACCOUNTANT + "','" + CASHIER_ACCOUNTANT + "')";

	/** Owner / PDG only (staff sensitive fields & profit-share rules). */
	public static final String OWNER_ONLY = "hasRole('" + OWNER + "')";

	/** Roles allowed to create/update fleet vehicles, drivers, and documents. */
	public static final String FLEET_WRITE =
			"hasAnyRole('" + OWNER + "','" + CASHIER + "','" + ACCOUNTANT + "','"
					+ CASHIER_ACCOUNTANT + "','" + DRIVER_MANAGER + "')";

	/**
	 * Roles allowed to create/update trips (ledger revenue flow + driver manager).
	 */
	public static final String TRIP_WRITE =
			"hasAnyRole('" + OWNER + "','" + CASHIER + "','" + ACCOUNTANT + "','"
					+ CASHIER_ACCOUNTANT + "','" + DRIVER_MANAGER + "')";

	private AppRoles() {
	}
}

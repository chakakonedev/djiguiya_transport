package com.djiguiya.transport.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "djiguiya")
public class CoreStaffSeedProperties {

	private final Security security = new Security();
	private final Seed seed = new Seed();

	public Security getSecurity() {
		return security;
	}

	public Seed getSeed() {
		return seed;
	}

	public static class Security {
		/**
		 * Bootstrap password for seed accounts that have not changed theirs yet.
		 * Override with env {@code DJIGUIYA_DEFAULT_PASSWORD}.
		 */
		private String defaultPassword = "Djiguiya@2026";

		public String getDefaultPassword() {
			return defaultPassword;
		}

		public void setDefaultPassword(String defaultPassword) {
			this.defaultPassword = defaultPassword;
		}
	}

	public static class Seed {
		private boolean coreStaffEnabled = true;
		/** Import year-1 trips/expenses extracted from dashboard.html + CSVs. */
		private boolean year1Enabled = true;

		public boolean isCoreStaffEnabled() {
			return coreStaffEnabled;
		}

		public void setCoreStaffEnabled(boolean coreStaffEnabled) {
			this.coreStaffEnabled = coreStaffEnabled;
		}

		public boolean isYear1Enabled() {
			return year1Enabled;
		}

		public void setYear1Enabled(boolean year1Enabled) {
			this.year1Enabled = year1Enabled;
		}
	}
}

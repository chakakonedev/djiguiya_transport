/**
 * Shared DTO shapes matching the backend JSON envelope and domain entities.
 * Used by HTTP services and page components for typed requests/responses.
 */

export interface ApiResponse<T> {
  success: boolean;
  messageKey: string | null;
  data: T;
}

export type TransactionType = 'revenue' | 'expense';

export interface StaffMember {
  id: string;
  fullName: string;
  staffType: string;
  phone?: string | null;
  status: string;
  monthlySalary?: number | null;
  payDay?: number | null;
  notes?: string | null;
}

export interface StaffCreateRequest {
  fullName: string;
  staffType: string;
  monthlySalary?: number | null;
  payDay?: number | null;
  phone?: string | null;
  notes?: string | null;
}

export interface VehicleDocument {
  id?: string;
  vehicleId?: string;
  docType: string;
  expiresOn: string;
  lastRenewedOn?: string | null;
  renewalPeriodMonths?: number;
  lastAmount?: number | null;
  notes?: string | null;
}

export interface Vehicle {
  id: string;
  name: string;
  plate?: string | null;
  purchasePrice?: number | null;
  purchaseDate?: string | null;
  status: string;
  primaryDriverId?: string | null;
  primaryDriverName?: string | null;
  documents?: VehicleDocument[];
}

export interface VehicleCreateRequest {
  name: string;
  plate?: string | null;
  purchasePrice?: number | null;
  purchaseDate?: string | null;
  notes?: string | null;
}

export interface PrimaryDriverRequest {
  driverId: string;
  startDate: string;
  reason?: string | null;
}

export interface Trip {
  id: string;
  vehicleId: string;
  vehicleName?: string;
  departureDate: string;
  returnDate: string;
  monthKey: string;
  driverId: string;
  driverName?: string;
  isReplacement?: boolean;
  notes?: string | null;
}

export interface TripCreateRequest {
  vehicleId: string;
  departureDate: string;
  returnDate: string;
  driverId: string;
  notes?: string | null;
}

export interface LedgerTransaction {
  id: string;
  type: TransactionType;
  txnDate: string;
  monthKey: string;
  amount: number;
  label: string;
  vehicleId?: string | null;
  driverId?: string | null;
  tripId?: string | null;
  categoryCode?: string | null;
  subcategoryCode?: string | null;
  notes?: string | null;
}

export interface TransactionCreateRequest {
  type: TransactionType;
  txnDate: string;
  amount: number;
  label?: string | null;
  tripId?: string | null;
  vehicleId?: string | null;
  driverId?: string | null;
  categoryCode?: string | null;
  subcategoryCode?: string | null;
  notes?: string | null;
}

export interface StatsSummary {
  monthKey?: string | null;
  year?: number | null;
  totalRevenue: number;
  totalExpense: number;
  netProfit: number;
  tripCount: number;
  transactionCount?: number;
  groups?: StatsGroupRow[];
}

export interface StatsGroupRow {
  categoryCode?: string | null;
  subcategoryCode?: string | null;
  amount: number;
}

export interface StatsMonthPoint {
  monthKey: string;
  calendarMonth: number;
  calendarYear: number;
  totalRevenue: number;
  totalExpense: number;
  netProfit: number;
  tripCount: number;
}

export interface StatsMonthlySeries {
  fiscalStartYear: number;
  months: StatsMonthPoint[];
}

export interface StatsYoy {
  year: number;
  previousYear: number;
  currentNetProfit: number;
  previousNetProfit: number;
  deltaNetProfit: number;
  percentChange?: number | null;
}

export interface ProfitShareLine {
  beneficiaryStaffId?: string | null;
  beneficiaryName?: string | null;
  ruleKind: string;
  ratePercent?: number | null;
  amount: number;
  notes?: string | null;
}

export interface ProfitSharePreview {
  year: number;
  periodStart: string;
  periodEnd: string;
  totalRevenue?: number;
  totalExpense?: number;
  annualNetProfit: number;
  reserveAmount: number;
  distributableAmount: number;
  lines: ProfitShareLine[];
}

export interface ProfitShareRule {
  id: string;
  beneficiaryStaffId?: string | null;
  beneficiaryName?: string | null;
  ruleKind: string;
  ratePercent?: number | null;
  salaryMonthsEquivalent?: number | null;
  enabled: boolean;
  effectiveFrom: string;
  effectiveTo?: string | null;
  notes?: string | null;
}

export interface ProfitShareRuleUpdateRequest {
  ratePercent?: number | null;
  salaryMonthsEquivalent?: number | null;
  enabled?: boolean | null;
  notes?: string | null;
}

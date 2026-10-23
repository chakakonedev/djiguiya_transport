/** Formats amounts in West African CFA francs for display; returns em dash when missing. */
export function formatXof(amount: number | null | undefined): string {
  if (amount == null || Number.isNaN(amount)) {
    return '—';
  }
  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency: 'XOF',
    maximumFractionDigits: 0
  }).format(amount);
}

export function getCellClass(value: number) {
  if (value === 0) return 'bg-[#003366] text-transparent';
  if (value === 2) return 'bg-[#f8fafc] text-[#002244] shadow-sm';
  if (value === 4) return 'bg-[#e2e8f0] text-[#002244] shadow-sm';
  if (value === 8) return 'bg-[#cbd5e1] text-[#002244] shadow-sm';
  if (value === 16) return 'bg-[#94a3b8] text-white shadow-md';
  if (value === 32) return 'bg-[#64748b] text-white shadow-md';
  if (value === 64) return 'bg-[#475569] text-white shadow-md';
  if (value === 128) return 'bg-[#00509a] text-white shadow-lg text-2xl';
  if (value === 256) return 'bg-[#004080] text-white shadow-lg text-2xl';
  if (value === 512) return 'bg-[#003060] text-white shadow-lg text-2xl';
  if (value === 1024) return 'bg-[#eab308] text-[#002244] text-xl shadow-[0_0_15px_rgba(234,179,8,0.4)] ring-1 ring-yellow-400';
  if (value === 2048) return 'bg-[#ca8a04] text-white text-xl shadow-[0_0_20px_rgba(202,138,4,0.6)] ring-2 ring-yellow-500';
  return 'bg-[#1e293b] text-white text-xl shadow-xl';
}

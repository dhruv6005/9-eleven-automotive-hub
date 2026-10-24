export type VehicleSizeClass = 'SMALL' | 'MEDIUM' | 'LARGE' | 'SUV';
export type CustomerStatus = 'ACTIVE' | 'NO_LOGIN' | 'LOCKED' | 'DISABLED' | string;
export interface VehicleSizeOption { sizeClass: VehicleSizeClass; label: string; }
export interface Vehicle { id: string; customerId: string; make: string; model: string; variant: string | null; year: number | null; color: string | null; sizeClass: VehicleSizeClass; regNumber: string; vin: string | null; }
export interface VehicleRequest { make: string; model: string; variant: string | null; year: number | null; color: string | null; sizeClass: VehicleSizeClass; regNumber: string; vin: string | null; }
export interface CustomerSummary { id: string; name: string; phone: string; email: string | null; status: CustomerStatus; address: string | null; notes: string | null; }
export interface Customer extends CustomerSummary { vehicles: Vehicle[]; }
export interface CustomerRequest { name: string; phone: string; email: string | null; address: string | null; notes: string | null; }
export interface Page<T> { content: T[]; totalElements: number; totalPages: number; number: number; size: number; }

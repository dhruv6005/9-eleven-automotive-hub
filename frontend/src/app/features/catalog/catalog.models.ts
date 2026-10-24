export type VehicleSizeClass = 'SMALL' | 'MEDIUM' | 'LARGE' | 'SUV';
export interface VehicleSizeOption { sizeClass: VehicleSizeClass; label: string; }
export type DiscountType = 'PERCENT' | 'FLAT';

export interface Category { id: string; name: string; slug: string; description: string | null; sortOrder: number; imageUrl: string | null; active: boolean; }
export interface SizePrice { sizeClass: VehicleSizeClass; price: number; }
export interface Service { id: string; categoryId: string; categoryName: string; categorySlug: string; name: string; slug: string; description: string | null; shortBenefit: string | null; imageUrl: string | null; baseDurationHours: number | null; price: number | null; prices: SizePrice[]; fromPrice: number | null; active: boolean; }
export interface ServicePrice { id: string; serviceId: string; vehicleSizeClass: string; price: number; }
export interface OfferPrice { serviceId: string; serviceName: string; sizeClass: VehicleSizeClass; price: number; }
export interface Offer { id: string; title: string; description: string | null; discountType: DiscountType; discountValue: number; validFrom: string; validTo: string; appliesToAll: boolean; active: boolean; serviceIds: string[]; prices: OfferPrice[]; }
export interface CategoryRequest { name: string; slug: string; description: string | null; sortOrder: number; imageUrl: string | null; active: boolean; }
export interface ServiceRequest { categoryId: string; name: string; slug: string; description: string | null; shortBenefit: string | null; imageUrl: string | null; baseDurationHours: number | null; active: boolean; }
export interface OfferRequest { title: string; description: string | null; discountType: DiscountType; discountValue: number; validFrom: string; validTo: string; appliesToAll: boolean; active: boolean; serviceIds: string[]; }
export interface Page<T> { content: T[]; totalElements: number; totalPages: number; number: number; size: number; }

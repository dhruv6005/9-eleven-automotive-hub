import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { API_BASE_URL } from '../../core/api-paths';
import { Category, CategoryRequest, Offer, OfferRequest, Page, Service, ServicePrice, ServiceRequest, VehicleSizeOption } from './catalog.models';

@Injectable({ providedIn: 'root' })
export class CatalogApiService {
  private readonly root = `${API_BASE_URL}`;
  constructor(private readonly http: HttpClient) {}
  categories(): Promise<Category[]> { return firstValueFrom(this.http.get<Category[]>(`${this.root}/public/categories`)); }
  services(category?: string, page = 0): Promise<Page<Service>> {
    let params = new HttpParams().set('page', page).set('size', 100);
    if (category) params = params.set('category', category);
    return firstValueFrom(this.http.get<Page<Service>>(`${this.root}/public/services`, { params }));
  }
  service(slug: string): Promise<Service> { return firstValueFrom(this.http.get<Service>(`${this.root}/public/services/${encodeURIComponent(slug)}`)); }
  publicOffers(): Promise<Offer[]> { return firstValueFrom(this.http.get<Offer[]>(`${this.root}/public/offers`)); }
  vehicleSizeClasses(): Promise<VehicleSizeOption[]> { return firstValueFrom(this.http.get<VehicleSizeOption[]>(`${this.root}/public/vehicle-size-classes`)); }
  adminCategories(): Promise<Category[]> { return firstValueFrom(this.http.get<Category[]>(`${this.root}/admin/categories`)); }
  saveCategory(id: string | null, body: CategoryRequest): Promise<Category> { return firstValueFrom(id ? this.http.put<Category>(`${this.root}/admin/categories/${id}`, body) : this.http.post<Category>(`${this.root}/admin/categories`, body)); }
  hideCategory(id: string): Promise<void> { return firstValueFrom(this.http.delete<void>(`${this.root}/admin/categories/${id}`)); }
  adminServices(): Promise<Service[]> { return firstValueFrom(this.http.get<Service[]>(`${this.root}/admin/services`)); }
  saveService(id: string | null, body: ServiceRequest): Promise<Service> { return firstValueFrom(id ? this.http.put<Service>(`${this.root}/admin/services/${id}`, body) : this.http.post<Service>(`${this.root}/admin/services`, body)); }
  hideService(id: string): Promise<void> { return firstValueFrom(this.http.delete<void>(`${this.root}/admin/services/${id}`)); }
  prices(id: string): Promise<ServicePrice[]> { return firstValueFrom(this.http.get<ServicePrice[]>(`${this.root}/admin/services/${id}/prices`)); }
  setPrice(id: string, sizeClass: string, price: number): Promise<ServicePrice> { return firstValueFrom(this.http.put<ServicePrice>(`${this.root}/admin/services/${id}/prices/${encodeURIComponent(sizeClass)}`, { price })); }
  deletePrice(id: string, sizeClass: string): Promise<void> { return firstValueFrom(this.http.delete<void>(`${this.root}/admin/services/${id}/prices/${encodeURIComponent(sizeClass)}`)); }
  adminOffers(): Promise<Offer[]> { return firstValueFrom(this.http.get<Offer[]>(`${this.root}/admin/offers`)); }
  saveOffer(id: string | null, body: OfferRequest): Promise<Offer> { return firstValueFrom(id ? this.http.put<Offer>(`${this.root}/admin/offers/${id}`, body) : this.http.post<Offer>(`${this.root}/admin/offers`, body)); }
  hideOffer(id: string): Promise<void> { return firstValueFrom(this.http.delete<void>(`${this.root}/admin/offers/${id}`)); }
}

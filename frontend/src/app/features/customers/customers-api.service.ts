import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { API_BASE_URL } from '../../core/api-paths';
import { Customer, CustomerRequest, CustomerSummary, Page, Vehicle, VehicleRequest, VehicleSizeOption } from './customer.models';

@Injectable({ providedIn: 'root' })
export class CustomersApiService {
  private readonly root = API_BASE_URL;
  constructor(private readonly http: HttpClient) {}
  sizes(): Promise<VehicleSizeOption[]> { return firstValueFrom(this.http.get<VehicleSizeOption[]>(`${this.root}/public/vehicle-size-classes`)); }
  myVehicles(): Promise<Vehicle[]> { return firstValueFrom(this.http.get<Vehicle[]>(`${this.root}/me/vehicles`)); }
  saveMyVehicle(id: string | null, body: VehicleRequest): Promise<Vehicle> { return firstValueFrom(id ? this.http.put<Vehicle>(`${this.root}/me/vehicles/${id}`, body) : this.http.post<Vehicle>(`${this.root}/me/vehicles`, body)); }
  deleteMyVehicle(id: string): Promise<void> { return firstValueFrom(this.http.delete<void>(`${this.root}/me/vehicles/${id}`)); }
  customers(search = '', status = '', page = 0, size = 20): Promise<Page<CustomerSummary>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (search.trim()) params = params.set('search', search.trim());
    if (status) params = params.set('status', status);
    return firstValueFrom(this.http.get<Page<CustomerSummary>>(`${this.root}/admin/customers`, { params }));
  }
  customer(id: string): Promise<Customer> { return firstValueFrom(this.http.get<Customer>(`${this.root}/admin/customers/${id}`)); }
  saveCustomer(id: string | null, body: CustomerRequest): Promise<CustomerSummary> { return firstValueFrom(id ? this.http.put<CustomerSummary>(`${this.root}/admin/customers/${id}`, body) : this.http.post<CustomerSummary>(`${this.root}/admin/customers`, body)); }
  invite(id: string, email: string): Promise<void> { return firstValueFrom(this.http.post<void>(`${this.root}/admin/customers/${id}/invite`, { email })); }
  customerVehicles(id: string): Promise<Vehicle[]> { return firstValueFrom(this.http.get<Vehicle[]>(`${this.root}/admin/customers/${id}/vehicles`)); }
  saveCustomerVehicle(customerId: string, id: string | null, body: VehicleRequest): Promise<Vehicle> { const url = `${this.root}/admin/customers/${customerId}/vehicles`; return firstValueFrom(id ? this.http.put<Vehicle>(`${url}/${id}`, body) : this.http.post<Vehicle>(url, body)); }
  deleteCustomerVehicle(customerId: string, id: string): Promise<void> { return firstValueFrom(this.http.delete<void>(`${this.root}/admin/customers/${customerId}/vehicles/${id}`)); }
}

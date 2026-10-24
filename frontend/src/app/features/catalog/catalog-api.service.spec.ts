import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';
import { API_BASE_URL } from '../../core/api-paths';
import { CatalogApiService } from './catalog-api.service';

describe('CatalogApiService', () => {
  it('loads typed public services with category and pagination filters', async () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const api = TestBed.inject(CatalogApiService); const http = TestBed.inject(HttpTestingController);
    const result = api.services('paint-care', 2);
    const req = http.expectOne(r => r.url === `${API_BASE_URL}/public/services` && r.params.get('category') === 'paint-care' && r.params.get('page') === '2' && r.params.get('size') === '100');
    req.flush({ content: [{ id: 's1', price: null }], totalElements: 1, totalPages: 1, number: 2, size: 100 });
    await expect(result).resolves.toMatchObject({ content: [{ id: 's1', price: null }] });
    http.verify();
  });

  it('uses the admin price endpoint to set a vehicle-size price', async () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const api = TestBed.inject(CatalogApiService); const http = TestBed.inject(HttpTestingController);
    const result = api.setPrice('service-id', 'SUV', 12500);
    const req = http.expectOne(`${API_BASE_URL}/admin/services/service-id/prices/SUV`);
    expect(req.request.method).toBe('PUT'); expect(req.request.body).toEqual({ price: 12500 });
    req.flush({ id: 'price-id', serviceId: 'service-id', vehicleSizeClass: 'SUV', price: 12500 });
    await expect(result).resolves.toMatchObject({ price: 12500, vehicleSizeClass: 'SUV' });
    http.verify();
  });
});

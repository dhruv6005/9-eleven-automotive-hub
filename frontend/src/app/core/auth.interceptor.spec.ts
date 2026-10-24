import { provideHttpClient, withInterceptors, HttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { API_BASE_URL } from './api-paths';
import { AuthService } from './auth.service';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  it('attaches the in-memory access token to API requests', async () => {
    const auth = {
      accessToken: signal('test-access-token'),
      refreshSession: vi.fn().mockResolvedValue(false),
    };
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: auth },
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    const client = TestBed.inject(HttpClient);
    const httpTesting = TestBed.inject(HttpTestingController);

    const response = firstValueFrom(client.get(`${API_BASE_URL}/me`));
    const request = httpTesting.expectOne(`${API_BASE_URL}/me`);
    expect(request.request.headers.get('Authorization')).toBe('Bearer test-access-token');
    expect(request.request.withCredentials).toBe(true);
    request.flush({});
    await response;
    httpTesting.verify();
  });
});

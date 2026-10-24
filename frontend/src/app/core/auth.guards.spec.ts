import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot, provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { describe, expect, it, vi } from 'vitest';
import { AuthService } from './auth.service';
import { authenticatedGuard, roleGuard } from './auth.guards';

describe('authenticatedGuard', () => {
  it('allows an authenticated user after restoring the session', async () => {
    const auth = {
      initialize: vi.fn().mockResolvedValue(true),
      role: signal<'CUSTOMER' | 'ADMIN' | null>('CUSTOMER'),
      forcePasswordChange: signal(false),
    };
    TestBed.configureTestingModule({
      providers: [{ provide: AuthService, useValue: auth }, provideRouter([])],
    });

    const result = await TestBed.runInInjectionContext(() => authenticatedGuard(
      { routeConfig: { path: 'portal' } } as ActivatedRouteSnapshot,
      { url: '/portal' } as RouterStateSnapshot,
    ));

    expect(auth.initialize).toHaveBeenCalledOnce();
    expect(result).toBe(true);
  });
});

describe('roleGuard', () => {
  it('redirects a customer away from the admin route', () => {
    const auth = { role: signal<'CUSTOMER' | 'ADMIN' | null>('CUSTOMER'), homeForRole: vi.fn(() => '/portal') };
    TestBed.configureTestingModule({ providers: [{ provide: AuthService, useValue: auth }, provideRouter([])] });
    const result = TestBed.runInInjectionContext(() => roleGuard({ data: { role: 'ADMIN' } } as unknown as ActivatedRouteSnapshot, {} as RouterStateSnapshot));
    expect(result).not.toBe(true);
    expect(auth.homeForRole).toHaveBeenCalledOnce();
  });

  it('redirects an admin away from the customer portal', () => {
    const auth = { role: signal<'CUSTOMER' | 'ADMIN' | null>('ADMIN'), homeForRole: vi.fn(() => '/admin') };
    TestBed.configureTestingModule({ providers: [{ provide: AuthService, useValue: auth }, provideRouter([])] });
    const result = TestBed.runInInjectionContext(() => roleGuard({ data: { role: 'CUSTOMER' } } as unknown as ActivatedRouteSnapshot, {} as RouterStateSnapshot));
    expect(result).not.toBe(true);
    expect(auth.homeForRole).toHaveBeenCalledOnce();
  });
});

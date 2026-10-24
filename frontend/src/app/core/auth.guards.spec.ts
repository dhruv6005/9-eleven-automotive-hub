import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot, provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { describe, expect, it, vi } from 'vitest';
import { AuthService } from './auth.service';
import { authenticatedGuard } from './auth.guards';

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

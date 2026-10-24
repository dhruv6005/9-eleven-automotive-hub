import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthRole, AuthService } from './auth.service';

export const authenticatedGuard: CanActivateFn = async (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  await auth.initialize();
  if (!auth.role()) return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
  if (auth.forcePasswordChange() && route.routeConfig?.path !== 'change-password') {
    return router.createUrlTree(['/change-password']);
  }
  return true;
};

export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const allowedRole = route.data['role'] as AuthRole | undefined;
  if (!allowedRole || auth.role() === allowedRole) return true;
  return router.parseUrl(auth.homeForRole());
};

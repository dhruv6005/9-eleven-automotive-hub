import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, from, switchMap, throwError } from 'rxjs';
import { API_BASE_URL } from './api-paths';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const authUrl = request.url.startsWith(`${API_BASE_URL}/auth/`);
  const token = auth.accessToken();
  const outgoing = request.url.startsWith(API_BASE_URL)
    ? request.clone({
        withCredentials: true,
        ...(token ? { setHeaders: { Authorization: `Bearer ${token}` } } : {}),
      })
    : request;

  return next(outgoing).pipe(
    catchError((error: unknown) => {
      if (!(error instanceof HttpErrorResponse) || error.status !== 401 || authUrl) return throwError(() => error);
      return from(auth.refreshSession()).pipe(
        switchMap((refreshed) => {
          const newToken = auth.accessToken();
          if (!refreshed || !newToken) return throwError(() => error);
          return next(request.clone({ withCredentials: true, setHeaders: { Authorization: `Bearer ${newToken}` } }));
        }),
      );
    }),
  );
};

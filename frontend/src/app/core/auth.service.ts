import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { API_BASE_URL } from './api-paths';

export type AuthRole = 'ADMIN' | 'CUSTOMER';
export interface AuthResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresInSeconds: number;
  role: AuthRole;
  forcePasswordChange: boolean;
}
export interface MessageResponse { message: string; }

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly accessToken = signal<string | null>(null);
  readonly role = signal<AuthRole | null>(null);
  readonly forcePasswordChange = signal(false);
  private initialized = false;
  private authRevision = 0;
  private initializing?: Promise<boolean>;
  private refreshing?: Promise<boolean>;

  constructor(private readonly http: HttpClient) {}

  async initialize(): Promise<boolean> {
    if (this.initialized) return this.role() !== null;
    if (this.initializing) return this.initializing;
    this.initializing = this.refreshSession().finally(() => {
      this.initialized = true;
      this.initializing = undefined;
    });
    return this.initializing;
  }

  async refreshSession(): Promise<boolean> {
    if (this.refreshing) return this.refreshing;
    const revision = this.authRevision;
    this.refreshing = firstValueFrom(
      this.http.post<AuthResponse>(`${API_BASE_URL}/auth/refresh`, {}, { withCredentials: true }),
    )
      .then((response) => {
        if (revision !== this.authRevision) return this.role() !== null;
        this.accept(response);
        return true;
      })
      .catch(() => {
        if (revision === this.authRevision) this.clear();
        return false;
      })
      .finally(() => (this.refreshing = undefined));
    return this.refreshing;
  }

  async login(email: string, password: string): Promise<AuthResponse> {
    const response = await firstValueFrom(
      this.http.post<AuthResponse>(`${API_BASE_URL}/auth/login`, { email, password }, { withCredentials: true }),
    );
    this.accept(response);
    this.initialized = true;
    return response;
  }

  register(name: string, email: string, phone: string, password: string) {
    return firstValueFrom(this.http.post<MessageResponse>(`${API_BASE_URL}/auth/register`, { name, email, phone, password }));
  }
  forgotPassword(email: string) {
    return firstValueFrom(this.http.post<MessageResponse>(`${API_BASE_URL}/auth/forgot-password`, { email }));
  }
  resetPassword(token: string, password: string) {
    return firstValueFrom(this.http.post<MessageResponse>(`${API_BASE_URL}/auth/reset-password`, { token, password }));
  }
  verifyEmail(token: string) {
    return firstValueFrom(this.http.get<MessageResponse>(`${API_BASE_URL}/auth/verify-email`, { params: { token } }));
  }
  changePassword(currentPassword: string, newPassword: string) {
    return firstValueFrom(this.http.post<AuthResponse>(`${API_BASE_URL}/auth/change-password`, { currentPassword, newPassword }))
      .then((response) => { this.accept(response); return response; });
  }

  async logout(): Promise<void> {
    try {
      await firstValueFrom(this.http.post(`${API_BASE_URL}/auth/logout`, {}, { withCredentials: true }));
    } finally {
      this.clear();
    }
  }

  homeForRole(role: AuthRole | null = this.role()): string {
    return role === 'ADMIN' ? '/admin' : '/portal';
  }

  private accept(response: AuthResponse): void {
    this.authRevision++;
    this.accessToken.set(response.accessToken);
    this.role.set(response.role);
    this.forcePasswordChange.set(response.forcePasswordChange);
  }
  private clear(): void {
    this.authRevision++;
    this.accessToken.set(null);
    this.role.set(null);
    this.forcePasswordChange.set(false);
  }
}

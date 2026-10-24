import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';

type AuthMode = 'login' | 'register' | 'forgot-password' | 'reset-password' | 'verify-email' | 'change-password';

@Component({
  selector: 'app-auth-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <section class="auth-page">
      <div class="auth-panel">
        <a class="auth-brand" routerLink="/" aria-label="9-Eleven home"><span>9<span>/</span>11</span><small>AUTOMOTIVE HUB</small></a>
        <div class="eyebrow"><span class="eyebrow-line"></span>{{ eyebrow }}</div>
        <h1>{{ title }}</h1>
        <p class="auth-intro">{{ intro }}</p>

        @if (mode !== 'verify-email') {
          <form [formGroup]="form" (ngSubmit)="submit()" novalidate>
            @if (mode === 'register') {
              <label>Full name<input formControlName="name" autocomplete="name" /></label>
              <label>Phone <span class="optional">OPTIONAL</span><input formControlName="phone" autocomplete="tel" /></label>
            }
            @if (mode !== 'change-password' && mode !== 'reset-password') {
              <label>Email address<input formControlName="email" type="email" autocomplete="email" /></label>
            }
            @if (mode === 'login' || mode === 'register') {
              <label>Password<input formControlName="password" type="password" [autocomplete]="mode === 'login' ? 'current-password' : 'new-password'" /></label>
              @if (mode === 'register') { <p class="field-hint">Use 12 or more characters with uppercase, lowercase, a number, and a symbol.</p> }
            }
            @if (mode === 'reset-password') {
              <label>New password<input formControlName="password" type="password" autocomplete="new-password" /></label>
              <p class="field-hint">Use 12 or more characters with uppercase, lowercase, a number, and a symbol.</p>
            }
            @if (mode === 'change-password') {
              <label>Current password<input formControlName="currentPassword" type="password" autocomplete="current-password" /></label>
              <label>New password<input formControlName="newPassword" type="password" autocomplete="new-password" /></label>
              <p class="field-hint">Use 12 or more characters with uppercase, lowercase, a number, and a symbol.</p>
            }
            @if (errorMessage) { <p class="auth-message error" role="alert">{{ errorMessage }}</p> }
            @if (successMessage) { <p class="auth-message success" role="status">{{ successMessage }}</p> }
            @if (mode !== 'change-password') {
              <button class="button-primary auth-submit" type="submit" [disabled]="busy">
                {{ busy ? 'PLEASE WAIT…' : submitLabel }} <span aria-hidden="true">↗</span>
              </button>
            } @else {
              <button class="button-primary auth-submit" type="submit" [disabled]="busy">{{ busy ? 'PLEASE WAIT…' : 'CHANGE PASSWORD' }} <span aria-hidden="true">↗</span></button>
            }
          </form>
        } @else {
          @if (errorMessage) { <p class="auth-message error" role="alert">{{ errorMessage }}</p> }
          @if (successMessage) { <p class="auth-message success" role="status">{{ successMessage }}</p> }
          @if (busy) { <p class="field-hint">Verifying your email address…</p> }
        }

        <nav class="auth-links" aria-label="Account links">
          @if (mode === 'login') {
            <a routerLink="/forgot-password">Forgot password?</a><a routerLink="/register">Create an account</a>
          } @else if (mode === 'register') {
            <a routerLink="/login">Already have an account? Sign in</a>
          } @else if (mode === 'forgot-password' || mode === 'reset-password' || mode === 'verify-email') {
            <a routerLink="/login">Return to sign in</a>
          }
        </nav>
        <a class="auth-back" routerLink="/">← Back to 9-Eleven</a>
      </div>
      <aside class="auth-aside" aria-label="9-Eleven Automotive Hub">
        <div class="auth-aside-mark">9<span>/</span>11</div>
        <div class="auth-aside-copy"><span>PRECISION CARE. EVERY DRIVE.</span><strong>YOUR CAR’S<br />JOURNEY,<br /><em>IN THE LOOP.</em></strong></div>
        <div class="auth-aside-foot">9-ELEVEN AUTOMOTIVE HUB <span>SECURE CUSTOMER ACCESS</span></div>
      </aside>
    </section>
  `,
})
export class AuthPageComponent implements OnInit {
  private readonly fb = inject(FormBuilder).nonNullable;
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  readonly mode = this.route.snapshot.routeConfig?.path as AuthMode;
  readonly form = this.fb.group({
    email: [''], name: [''], phone: [''], password: [''], currentPassword: [''], newPassword: [''],
  });
  busy = false;
  errorMessage = '';
  successMessage = '';
  readonly title = this.getTitle();
  readonly intro = this.getIntro();
  readonly eyebrow = this.getEyebrow();
  readonly submitLabel = this.getSubmitLabel();

  constructor() { this.configureForm(); }

  ngOnInit(): void {
    if (this.mode === 'verify-email') void this.verify();
  }

  async submit(): Promise<void> {
    this.errorMessage = ''; this.successMessage = '';
    if (this.form.invalid) { this.form.markAllAsTouched(); this.errorMessage = 'Complete the required fields to continue.'; return; }
    this.busy = true;
    try {
      const value = this.form.getRawValue();
      if (this.mode === 'login') {
        const response = await this.auth.login(value.email, value.password);
        if (response.forcePasswordChange) await this.router.navigateByUrl('/change-password');
        else {
          const requested = this.route.snapshot.queryParamMap.get('returnUrl');
          const home = this.auth.homeForRole(response.role);
          await this.router.navigateByUrl(requested?.startsWith(home) ? requested : home);
        }
      } else if (this.mode === 'register') {
        const result = await this.auth.register(value.name, value.email, value.phone, value.password);
        this.successMessage = result.message;
      } else if (this.mode === 'forgot-password') {
        const result = await this.auth.forgotPassword(value.email); this.successMessage = result.message;
      } else if (this.mode === 'reset-password') {
        const result = await this.auth.resetPassword(this.route.snapshot.queryParamMap.get('token') ?? '', value.password);
        this.successMessage = result.message;
      } else if (this.mode === 'change-password') {
        await this.auth.changePassword(value.currentPassword, value.newPassword);
        this.successMessage = 'Password changed successfully.';
        await this.router.navigateByUrl(this.auth.homeForRole());
      }
    } catch (error) {
      this.errorMessage = this.describeError(error);
    } finally { this.busy = false; }
  }

  private async verify(): Promise<void> {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) { this.errorMessage = 'This verification link is incomplete.'; return; }
    this.busy = true;
    try { this.successMessage = (await this.auth.verifyEmail(token)).message; }
    catch (error) { this.errorMessage = this.describeError(error); }
    finally { this.busy = false; }
  }
  private describeError(error: unknown): string {
    if (error instanceof HttpErrorResponse) return error.error?.detail ?? error.error?.message ?? 'Something went wrong. Please try again.';
    return 'Something went wrong. Please try again.';
  }
  private configureForm(): void {
    const strongPassword = [Validators.required, Validators.minLength(12), Validators.maxLength(72),
      Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).+$/)];
    if (this.mode === 'login' || this.mode === 'forgot-password' || this.mode === 'register') {
      this.form.controls.email.setValidators([Validators.required, Validators.email]);
    }
    if (this.mode === 'register') {
      this.form.controls.name.setValidators([Validators.required, Validators.minLength(2), Validators.maxLength(160)]);
      this.form.controls.password.setValidators(strongPassword);
    } else if (this.mode === 'login') {
      this.form.controls.password.setValidators(Validators.required);
    } else if (this.mode === 'reset-password') {
      this.form.controls.password.setValidators(strongPassword);
    } else if (this.mode === 'change-password') {
      this.form.controls.currentPassword.setValidators(Validators.required);
      this.form.controls.newPassword.setValidators(strongPassword);
    }
    Object.values(this.form.controls).forEach((control) => control.updateValueAndValidity({ emitEvent: false }));
  }
  private getTitle(): string {
    return ({ login: 'SIGN IN.', register: 'JOIN THE HUB.', 'forgot-password': 'RESET ACCESS.', 'reset-password': 'SET A NEW PASSWORD.', 'verify-email': 'VERIFY YOUR EMAIL.', 'change-password': 'CHANGE YOUR PASSWORD.' } as Record<AuthMode, string>)[this.mode];
  }
  private getIntro(): string {
    return ({ login: 'Sign in to follow your car’s progress and stay close to every workshop update.', register: 'Create a customer account to follow your vehicle’s workshop journey.', 'forgot-password': 'Enter your account email and we’ll send a reset link if an account matches.', 'reset-password': 'Choose a new password for your 9-Eleven account.', 'verify-email': 'We’re confirming the email address linked to your account.', 'change-password': 'For account security, choose a new password before continuing.' } as Record<AuthMode, string>)[this.mode];
  }
  private getEyebrow(): string {
    return ({ login: 'CUSTOMER & WORKSHOP ACCESS', register: 'CUSTOMER ACCOUNT', 'forgot-password': 'ACCOUNT RECOVERY', 'reset-password': 'ACCOUNT RECOVERY', 'verify-email': 'ACCOUNT VERIFICATION', 'change-password': 'SECURITY CHECK' } as Record<AuthMode, string>)[this.mode];
  }
  private getSubmitLabel(): string {
    return ({ login: 'SIGN IN', register: 'CREATE ACCOUNT', 'forgot-password': 'SEND RESET LINK', 'reset-password': 'SAVE NEW PASSWORD', 'verify-email': '', 'change-password': 'CHANGE PASSWORD' } as Record<AuthMode, string>)[this.mode];
  }
}

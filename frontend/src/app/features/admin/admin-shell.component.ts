import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-admin-shell',
  standalone: true,
  template: `
    <section class="placeholder-page portal-shell">
      <div class="eyebrow"><span class="eyebrow-line"></span> WORKSHOP OPERATIONS · ADMIN</div>
      <h1>THE WORKSHOP<br /><span>COMMAND CENTRE.</span></h1>
      <p>Your authenticated workshop workspace is ready. Job management and operational tools arrive in their assigned phases.</p>
      <div class="phase-strip"><span>PHASE 2</span><strong>SECURE ACCESS ENABLED</strong><span>WORKSHOP TOOLS FOLLOW IN LATER PHASES</span></div>
      <button class="button-secondary sign-out" type="button" (click)="logout()">Sign out</button>
    </section>
  `,
})
export class AdminShellComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  async logout(): Promise<void> { await this.auth.logout(); await this.router.navigateByUrl('/login'); }
}

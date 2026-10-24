import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-customer-shell',
  standalone: true,
  template: `
    <section class="placeholder-page portal-shell">
      <div class="eyebrow"><span class="eyebrow-line"></span> YOUR CAR, IN THE LOOP · CUSTOMER</div>
      <h1>YOUR CAR’S<br /><span>JOURNEY. CLEAR.</span></h1>
      <p>You’re signed in. Vehicle records, job timelines and workshop updates will be added in their assigned phases.</p>
      <div class="phase-strip"><span>PHASE 2</span><strong>SECURE CUSTOMER ACCESS</strong><span>TRACKING ARRIVES IN A LATER PHASE</span></div>
      <button class="button-secondary sign-out" type="button" (click)="logout()">Sign out</button>
    </section>
  `,
})
export class CustomerShellComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  async logout(): Promise<void> { await this.auth.logout(); await this.router.navigateByUrl('/login'); }
}

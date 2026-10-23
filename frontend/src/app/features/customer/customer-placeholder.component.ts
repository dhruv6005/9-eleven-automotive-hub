import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-customer-placeholder',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="placeholder-page">
      <div class="eyebrow"><span class="eyebrow-line"></span> YOUR CAR, IN THE LOOP</div>
      <h1>YOUR CAR’S<br /><span>JOURNEY. CLEAR.</span></h1>
      <p>The customer portal shell is ready. Secure login, vehicle records, job timelines, workshop photos and invoices will be delivered in subsequent phases.</p>
      <a class="button-primary" routerLink="/">Back to the public site ↗</a>
      <div class="phase-strip"><span>PHASE 1</span><strong>FOUNDATION READY</strong><span>CUSTOMER TRACKING COMES LATER</span></div>
    </section>
  `,
})
export class CustomerPlaceholderComponent {}

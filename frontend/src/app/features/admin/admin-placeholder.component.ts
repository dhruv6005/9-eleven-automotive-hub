import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-admin-placeholder',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="placeholder-page">
      <div class="eyebrow"><span class="eyebrow-line"></span> WORKSHOP OPERATIONS</div>
      <h1>THE WORKSHOP<br /><span>COMMAND CENTRE.</span></h1>
      <p>The admin portal shell is ready. Login, job management, customer records and reporting will be built in upcoming phases. This preview is not authenticated.</p>
      <a class="button-primary" routerLink="/">Back to the public site ↗</a>
      <div class="phase-strip"><span>PHASE 1</span><strong>FOUNDATION READY</strong><span>AUTHENTICATION COMES IN PHASE 2</span></div>
    </section>
  `,
})
export class AdminPlaceholderComponent {}

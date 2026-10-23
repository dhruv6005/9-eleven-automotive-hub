import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <div class="min-h-screen bg-automotive-charcoal text-white">
      <header class="site-header">
        <a class="brand" routerLink="/" aria-label="9-Eleven Automotive Hub home">
          <span class="brand-mark">9<span>/</span>11</span>
          <span class="brand-name">AUTOMOTIVE <b>HUB</b></span>
        </a>
        <nav class="main-nav" aria-label="Main navigation">
          <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">Home</a>
          <a href="#services">Services</a>
          <a href="#why-us">Why 9/11</a>
          <a href="#contact">Contact</a>
        </nav>
        <div class="header-actions">
          <a class="portal-link" routerLink="/portal">My vehicle</a>
          <a class="header-cta" href="#contact">Book a visit <span aria-hidden="true">↗</span></a>
        </div>
      </header>
      <main><router-outlet /></main>
      <footer class="site-footer">
        <div class="footer-brand"><span class="brand-mark">9<span>/</span>11</span><span>Precision care. Every drive.</span></div>
        <span>© {{ currentYear }} 9-Eleven Automotive Hub</span>
        <a routerLink="/admin">Workshop admin preview</a>
      </footer>
    </div>
  `,
})
export class AppComponent {
  readonly currentYear = new Date().getFullYear();
}

import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CatalogApiService } from '../catalog/catalog-api.service';
import { Category } from '../catalog/catalog.models';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="hero-section">
      <div class="hero-grid"></div>
      <div class="hero-copy">
        <div class="eyebrow"><span class="eyebrow-line"></span> THE NEXT STANDARD IN CAR CARE</div>
        <h1>BUILT TO<br /><span>TURN HEADS.</span></h1>
        <p class="hero-description">Precision detailing. Advanced protection. Workshop expertise. Your car deserves more than ordinary.</p>
        <div class="hero-buttons">
          <a class="button-primary" href="#contact">Book your service <span aria-hidden="true">↗</span></a>
          <a class="button-secondary" href="#services">Explore services <span aria-hidden="true">↓</span></a>
        </div>
        <div class="hero-proof">
          <div><strong>01</strong><span>Detail obsessed</span></div>
          <div><strong>02</strong><span>Care without shortcuts</span></div>
          <div><strong>03</strong><span>Every finish matters</span></div>
        </div>
      </div>
      <div class="hero-art" role="img" aria-label="Abstract premium automotive illustration">
        <div class="art-ring art-ring-one"></div><div class="art-ring art-ring-two"></div>
        <div class="art-label">DETAIL / PROTECT / DRIVE</div>
        <svg viewBox="0 0 760 420" class="car-illustration" aria-hidden="true">
          <defs>
            <linearGradient id="body" x1="0" x2="1" y1="0" y2="1"><stop offset="0" stop-color="#f5f5f5"/><stop offset="0.45" stop-color="#767b80"/><stop offset="0.7" stop-color="#272a2d"/><stop offset="1" stop-color="#0c0d0e"/></linearGradient>
            <linearGradient id="glass" x1="0" x2="1"><stop offset="0" stop-color="#262e36"/><stop offset="1" stop-color="#59646d"/></linearGradient>
          </defs>
          <ellipse cx="388" cy="337" rx="290" ry="27" fill="#000" opacity=".65" />
          <path d="M82 278 L112 246 Q129 232 175 222 L275 169 Q304 152 356 151 L483 153 Q526 156 562 194 L615 222 Q644 233 666 256 L681 290 Q677 309 649 315 L133 318 Q94 314 82 296 Z" fill="url(#body)" stroke="#d6d9dc" stroke-opacity=".48" stroke-width="2" />
          <path d="M200 220 L292 174 Q316 164 358 164 L419 165 L457 218 Z" fill="url(#glass)" stroke="#bac1c6" stroke-opacity=".45" stroke-width="2" />
          <path d="M435 166 L477 167 Q516 171 544 207 L565 220 L464 218 Z" fill="url(#glass)" stroke="#bac1c6" stroke-opacity=".3" stroke-width="2" />
          <path d="M99 277 Q153 259 219 264 L250 305 L129 305 Q107 301 99 287 Z" fill="#111315" />
          <path d="M550 259 L651 258 Q666 266 671 283 L665 296 L573 301 Z" fill="#17191b" />
          <path d="M83 279 L132 269 L143 281 L95 292 Z" fill="#ffe46b" />
          <path d="M616 257 L659 261 L669 275 L624 275 Z" fill="#ff4747" />
          <path d="M293 229 L456 229" stroke="#e8eaec" stroke-opacity=".44" stroke-width="2" />
          <path d="M330 240 L333 283" stroke="#141618" stroke-opacity=".8" stroke-width="3" />
          <path d="M479 238 L492 283" stroke="#141618" stroke-opacity=".8" stroke-width="3" />
          <circle cx="211" cy="305" r="48" fill="#080909" stroke="#7e858a" stroke-width="6" />
          <circle cx="211" cy="305" r="29" fill="#272b2e" stroke="#b8bec2" stroke-width="2" />
          <circle cx="211" cy="305" r="10" fill="#d1d5d8" />
          <circle cx="573" cy="305" r="48" fill="#080909" stroke="#7e858a" stroke-width="6" />
          <circle cx="573" cy="305" r="29" fill="#272b2e" stroke="#b8bec2" stroke-width="2" />
          <circle cx="573" cy="305" r="10" fill="#d1d5d8" />
          <path d="M99 304 Q205 320 329 317 T671 302" fill="none" stroke="#f4d31d" stroke-width="3" stroke-opacity=".9" />
        </svg>
        <div class="hero-stamp"><span>9</span><i>/</i><span>11</span><small>EST. AUTOMOTIVE CARE</small></div>
        <div class="art-caption"><span class="caption-dot"></span> CARE, ENGINEERED.</div>
      </div>
      <div class="hero-bottom"><span>EXPERIENCE THE DIFFERENCE</span><span>SCROLL TO DISCOVER ↓</span></div>
    </section>

    <section id="services" class="section services-section">
      <div class="section-heading"><div><div class="eyebrow"><span class="eyebrow-line"></span> WHAT WE DO</div><h2>ONE HUB.<br /><span>EVERY DETAIL.</span></h2></div><p>From first wash to full transformation, our services bring together specialist care and workshop capability under one roof.</p></div>
      @if (categoriesLoading()) { <p class="catalog-state" role="status">Loading service categories…</p> }
      @else if (categoriesError()) { <p class="catalog-state" role="alert">{{categoriesError()}} <button type="button" (click)="loadCategories()">Try again</button></p> }
      @else if (!categories().length) { <p class="catalog-state">Service categories will appear here soon.</p> }
      @else { <div class="service-grid">@for (category of categories(); track category.id) { <article class="service-card"><span class="service-number">{{category.sortOrder + 1}} / 9/11 CARE</span><div class="service-icon">✳</div><h3>{{category.name}}</h3><p>{{category.description || 'Specialist care, delivered with attention to every detail.'}}</p><a [routerLink]="['/services']" [queryParams]="{category: category.slug}">Explore category <span>↗</span></a></article> }</div> }
      <p class="catalog-more"><a routerLink="/services">View all services ↗</a> <a routerLink="/offers">Current offers ↗</a></p>
    </section>

    <section id="why-us" class="statement-section"><div class="statement-mark">“</div><div class="eyebrow"><span class="eyebrow-line"></span> THE 9/11 APPROACH</div><h2>YOUR CAR ISN’T<br />JUST <span>ANOTHER JOB.</span></h2><p>Clear updates. Considered recommendations. Quality you can see. The workshop experience should feel as good as the result.</p><div class="statement-metrics"><div><strong>01</strong><span>Transparent estimates</span></div><div><strong>02</strong><span>Progress you can follow</span></div><div><strong>03</strong><span>Care at every stage</span></div></div></section>

    <section id="contact" class="contact-section"><div><div class="eyebrow"><span class="eyebrow-line"></span> READY WHEN YOU ARE</div><h2>LET’S GET YOUR<br /><span>CAR RIGHT.</span></h2><p>Tell us what your car needs. Our team will help you choose the right service.</p></div><div class="contact-panel"><div class="contact-panel-top"><span>START A CONVERSATION</span><span class="live-dot"></span></div><a href="mailto:hello@9elevenautomotive.example" class="contact-action"><span>Enquire about a service</span><b>↗</b></a><a href="tel:+910000000000" class="contact-action"><span>Talk to the workshop</span><b>↗</b></a><p class="contact-note">Contact details shown here are placeholders for Phase 1. Configure the workshop’s actual details before launch.</p></div></section>
  `,
})
export class HomeComponent implements OnInit {
  private readonly catalog = inject(CatalogApiService);
  readonly categories = signal<Category[]>([]);
  readonly categoriesLoading = signal(true);
  readonly categoriesError = signal('');
  ngOnInit(): void { void this.loadCategories(); }
  async loadCategories(): Promise<void> {
    this.categoriesLoading.set(true); this.categoriesError.set('');
    try { this.categories.set(await this.catalog.categories()); }
    catch { this.categoriesError.set('Service categories could not be loaded.'); }
    finally { this.categoriesLoading.set(false); }
  }
}

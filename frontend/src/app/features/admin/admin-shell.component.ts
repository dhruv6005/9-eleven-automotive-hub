import { Component, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { CatalogAdminComponent } from './catalog-admin.component';
import { AdminCustomersComponent } from './customers-admin.component';

@Component({
  selector: 'app-admin-shell',
  standalone: true,
  imports: [CatalogAdminComponent, AdminCustomersComponent],
  template: `<div class="admin-switch"><button type="button" [class.active]="section()==='customers'" (click)="section.set('customers')">Customers</button><button type="button" [class.active]="section()==='catalog'" (click)="section.set('catalog')">Catalog</button></div>@if(section()==='customers'){<app-admin-customers (signOut)="logout()"/>}@else{<app-catalog-admin (signOut)="logout()"/>}`,
  styles: [`:host{display:block}.admin-switch{display:flex;gap:8px;padding:16px 6%;background:#151719}.admin-switch button{padding:11px 16px;border:1px solid #45494c;background:transparent;color:#ddd;cursor:pointer}.admin-switch button.active{background:#ffd400;color:#151719;font-weight:800}`],
})
export class AdminShellComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly section = signal<'customers'|'catalog'>('customers');
  async logout(): Promise<void> { await this.auth.logout(); await this.router.navigateByUrl('/login'); }
}

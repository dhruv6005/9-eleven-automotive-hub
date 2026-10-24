import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { CatalogAdminComponent } from './catalog-admin.component';

@Component({
  selector: 'app-admin-shell',
  standalone: true,
  imports: [CatalogAdminComponent],
  template: `<app-catalog-admin (signOut)="logout()" />`,
})
export class AdminShellComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  async logout(): Promise<void> { await this.auth.logout(); await this.router.navigateByUrl('/login'); }
}

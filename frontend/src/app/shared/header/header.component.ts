import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { MenuModule } from 'primeng/menu';
import { MenuItem } from 'primeng/api';
import { AuthService } from '../../services/auth/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, ButtonModule, MenuModule],
  templateUrl: './header.component.html'
})
export class HeaderComponent {
  @Input() showMenuToggle = true;
  @Output() toggleMenu = new EventEmitter<void>();

  @ViewChild('userMenu') userMenu!: any;

  menuItems: MenuItem[] = [
    { label: 'Cerrar sesión', icon: 'pi pi-sign-out', command: () => this.cerrarSesion() }
  ];

  constructor(private authService: AuthService, private router: Router) {}

  get origen(): string {
    return this.authService.getOrigen() ?? '';
  }

  onToggleMenu(): void {
    this.toggleMenu.emit();
  }

  toggleUserMenu(event: Event): void {
    this.userMenu.toggle(event);
  }

  private cerrarSesion(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}

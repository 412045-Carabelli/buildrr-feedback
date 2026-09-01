import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { MenuModule } from 'primeng/menu';
import { SelectModule } from 'primeng/select';
import { MenuItem } from 'primeng/api';
import { AuthService } from '../../services/auth/auth.service';
import { AplicacionSeleccionadaService } from '../../services/aplicaciones/aplicacion-seleccionada.service';
import { Producto } from '../../core/models/models';
import { NOMBRE_PRODUCTO } from '../../core/constants/producto-labels';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonModule, MenuModule, SelectModule],
  templateUrl: './header.component.html'
})
export class HeaderComponent {
  @Input() showMenuToggle = true;
  @Output() toggleMenu = new EventEmitter<void>();

  @ViewChild('userMenu') userMenu!: any;

  menuItems: MenuItem[] = [
    { label: 'Cerrar sesión', icon: 'pi pi-sign-out', command: () => this.cerrarSesion() }
  ];

  constructor(
    private authService: AuthService,
    private router: Router,
    public aplicacionSeleccionadaService: AplicacionSeleccionadaService
  ) {}

  get opcionesAplicacion(): { label: string; value: Producto }[] {
    return this.aplicacionSeleccionadaService.aplicacionesActuales.map((a) => ({
      label: NOMBRE_PRODUCTO[a.producto],
      value: a.producto
    }));
  }

  cambiarAplicacion(producto: Producto): void {
    this.aplicacionSeleccionadaService.seleccionar(producto);
    this.router.navigate(['/tickets']);
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

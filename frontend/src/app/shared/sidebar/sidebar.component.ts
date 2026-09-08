import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter, map, startWith } from 'rxjs/operators';
import { AplicacionSeleccionadaService } from '../../services/aplicaciones/aplicacion-seleccionada.service';

interface ItemMenu {
  label: string;
  path: string;
  icon: string;
  /** Con qué prefijos de URL (sin query params) se marca activo — por defecto, solo `path` exacto. */
  activoEn?: string[];
}

const ITEMS_CLIENTE: ItemMenu[] = [
  // /tickets/nuevo tiene su propio ítem — activoEn evita que ambos queden
  // marcados a la vez, pero sí cubre el detalle (/tickets/123).
  { label: 'Tickets', path: '/tickets', icon: 'pi-list', activoEn: ['/tickets'] },
  { label: 'Nuevo ticket', path: '/tickets/nuevo', icon: 'pi-plus-circle', activoEn: ['/tickets/nuevo'] }
];

const ITEMS_ADMIN: ItemMenu[] = [
  { label: 'Dashboard', path: '/dashboard', icon: 'pi-chart-bar', activoEn: ['/dashboard'] },
  ...ITEMS_CLIENTE,
  { label: 'Usuarios', path: '/admin/usuarios', icon: 'pi-users', activoEn: ['/admin/usuarios'] }
];

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  @Input() visible = true;

  /** Path actual sin query params (`?estado=...` no debe romper el resaltado). */
  rutaActual$;

  constructor(
    public aplicacionSeleccionadaService: AplicacionSeleccionadaService,
    private router: Router
  ) {
    this.rutaActual$ = this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map((e) => e.urlAfterRedirects.split('?')[0]),
      startWith(this.router.url.split('?')[0])
    );
  }

  get menuItems(): ItemMenu[] {
    return this.aplicacionSeleccionadaService.esAdminDeAlguna() ? ITEMS_ADMIN : ITEMS_CLIENTE;
  }

  esActivo(item: ItemMenu, rutaActual: string): boolean {
    const prefijos = item.activoEn ?? [item.path];
    if (item.path === '/tickets') {
      // "Tickets" cubre la lista y el detalle, pero no /tickets/nuevo (item propio).
      return rutaActual === '/tickets' || (rutaActual.startsWith('/tickets/') && rutaActual !== '/tickets/nuevo');
    }
    return prefijos.some((p) => rutaActual === p || rutaActual.startsWith(p + '/'));
  }
}

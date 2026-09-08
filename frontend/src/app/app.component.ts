import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs/operators';
import { HeaderComponent } from './shared/header/header.component';
import { SidebarComponent } from './shared/sidebar/sidebar.component';
import { AuthService } from './services/auth/auth.service';
import { AplicacionSeleccionadaService } from './services/aplicaciones/aplicacion-seleccionada.service';

// TEMPORAL: /cuenta/cambiar-password es pública mientras no exige login (ver
// app.routes.ts) — sacarla de acá cuando se reponga el authGuard.
const RUTAS_PUBLICAS = ['/login', '/cuenta/cambiar-password'];

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, HeaderComponent, SidebarComponent],
  templateUrl: './app.component.html'
})
export class AppComponent {
  sidebarVisible = window.innerWidth >= 1024;
  esRutaPublica = RUTAS_PUBLICAS.some((r) => window.location.pathname.startsWith(r));

  constructor(
    router: Router,
    authService: AuthService,
    aplicacionSeleccionadaService: AplicacionSeleccionadaService
  ) {
    router.events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe((e) => {
      this.esRutaPublica = RUTAS_PUBLICAS.some((r) => (e as NavigationEnd).urlAfterRedirects.startsWith(r));
    });

    // Recarga de página con sesión ya iniciada — el login ya la carga sola
    // al loguearse, esto cubre F5.
    if (authService.estaLogueado()) {
      aplicacionSeleccionadaService.cargar().subscribe();
    }
  }

  toggleSidebar(): void {
    this.sidebarVisible = !this.sidebarVisible;
  }
}

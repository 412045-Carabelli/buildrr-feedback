import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs/operators';
import { HeaderComponent } from './shared/header/header.component';
import { SidebarComponent } from './shared/sidebar/sidebar.component';

const RUTAS_PUBLICAS = ['/login'];

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, HeaderComponent, SidebarComponent],
  templateUrl: './app.component.html'
})
export class AppComponent {
  sidebarVisible = window.innerWidth >= 1024;
  esRutaPublica = RUTAS_PUBLICAS.some((r) => window.location.pathname.startsWith(r));

  constructor(router: Router) {
    router.events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe((e) => {
      this.esRutaPublica = RUTAS_PUBLICAS.some((r) => (e as NavigationEnd).urlAfterRedirects.startsWith(r));
    });
  }

  toggleSidebar(): void {
    this.sidebarVisible = !this.sidebarVisible;
  }
}

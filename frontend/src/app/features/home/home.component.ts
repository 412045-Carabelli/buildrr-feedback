import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AplicacionesService } from '../../services/aplicaciones/aplicaciones.service';

/**
 * Ruta raíz ('/'): decide el destino según el rol real del usuario — no un
 * `redirectTo` fijo. Admin de algún producto → Dashboard; si no → Tickets.
 * No renderiza nada, solo redirige (misma idea que un guard, pero como
 * componente para no pelear con las limitaciones de `redirectTo` dinámico
 * del router).
 */
@Component({
  selector: 'app-home',
  standalone: true,
  template: ''
})
export class HomeComponent implements OnInit {
  constructor(
    private aplicacionesService: AplicacionesService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.aplicacionesService.misAplicaciones().subscribe({
      next: (aplicaciones) => {
        const esAdmin = aplicaciones.some((a) => a.rol === 'ADMIN');
        this.router.navigate([esAdmin ? '/dashboard' : '/tickets'], { replaceUrl: true });
      },
      error: () => this.router.navigate(['/tickets'], { replaceUrl: true })
    });
  }
}

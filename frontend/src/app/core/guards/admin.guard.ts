import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map, tap } from 'rxjs/operators';
import { AplicacionesService } from '../../services/aplicaciones/aplicaciones.service';

/**
 * Dashboard y panel de usuarios son solo para quien sea ADMIN de al menos un
 * producto. Consulta fresca al backend (no la caché del navbar) para no
 * depender de que AplicacionSeleccionadaService ya haya cargado en un F5
 * directo a la ruta.
 */
export const adminGuard: CanActivateFn = () => {
  const aplicacionesService = inject(AplicacionesService);
  const router = inject(Router);

  return aplicacionesService.misAplicaciones().pipe(
    map((aplicaciones) => aplicaciones.some((a) => a.rol === 'ADMIN')),
    tap((esAdmin) => {
      if (!esAdmin) router.navigate(['/tickets']);
    })
  );
};

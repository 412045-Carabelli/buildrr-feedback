import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    canActivate: [authGuard],
    loadComponent: () => import('./features/home/home.component').then((m) => m.HomeComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./features/login/login.component').then((m) => m.LoginComponent)
  },
  {
    path: 'dashboard',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent)
  },
  {
    path: 'admin/usuarios',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./features/admin/usuarios/usuarios.component').then((m) => m.UsuariosComponent)
  },
  {
    // TEMPORAL: sin authGuard — cambia por email, sin sesión. Reponer el guard
    // cuando vuelva a exigirse login (ver cambiar-password.component.ts).
    path: 'cuenta/cambiar-password',
    loadComponent: () =>
      import('./features/cuenta/cambiar-password/cambiar-password.component').then((m) => m.CambiarPasswordComponent)
  },
  {
    path: 'tickets',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/tickets/pages/tickets-list/tickets-list.component').then((m) => m.TicketsListComponent)
  },
  {
    path: 'tickets/nuevo',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/tickets/pages/tickets-create/tickets-create.component').then((m) => m.TicketsCreateComponent)
  },
  {
    path: 'tickets/:id/editar',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/tickets/pages/tickets-edit/tickets-edit.component').then((m) => m.TicketsEditComponent)
  },
  {
    path: 'tickets/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/tickets/pages/tickets-detail/tickets-detail.component').then((m) => m.TicketsDetailComponent)
  },
  { path: '**', redirectTo: 'tickets' }
];

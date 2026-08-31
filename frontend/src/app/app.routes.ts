import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'tickets', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/login/login.component').then((m) => m.LoginComponent)
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
    path: 'tickets/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/tickets/pages/tickets-detail/tickets-detail.component').then((m) => m.TicketsDetailComponent)
  },
  { path: '**', redirectTo: 'tickets' }
];

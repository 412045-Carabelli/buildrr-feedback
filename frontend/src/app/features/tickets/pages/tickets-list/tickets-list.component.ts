import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs/operators';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { TicketsService } from '../../../../services/tickets/tickets.service';
import { AplicacionSeleccionadaService } from '../../../../services/aplicaciones/aplicacion-seleccionada.service';
import { EstadoTicket, TicketResponse } from '../../../../core/models/models';

@Component({
  selector: 'app-tickets-list',
  standalone: true,
  imports: [CommonModule, RouterLink, TableModule, TagModule, ButtonModule, ToastModule],
  providers: [MessageService],
  templateUrl: './tickets-list.component.html'
})
export class TicketsListComponent implements OnInit {
  tickets: TicketResponse[] = [];
  cargando = false;

  constructor(
    private ticketsService: TicketsService,
    private aplicacionSeleccionadaService: AplicacionSeleccionadaService,
    private router: Router,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    // Se re-suscribe cada vez que cambia la app seleccionada en el navbar —
    // así el dashboard refleja el filtro sin depender de que la navegación
    // recree el componente.
    this.cargando = true;
    this.aplicacionSeleccionadaService.seleccionada$
      .pipe(switchMap((producto) => this.ticketsService.listar(producto)))
      .subscribe({
        next: (data) => {
          this.tickets = data;
          this.cargando = false;
        },
        error: () => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar los tickets' });
          this.cargando = false;
        }
      });
  }

  verTicket(id: number): void {
    this.router.navigate(['/tickets', id]);
  }

  severityEstado(estado: EstadoTicket): 'info' | 'warn' | 'secondary' | 'success' {
    switch (estado) {
      case 'NUEVO': return 'info';
      case 'EN_PROGRESO': return 'warn';
      case 'TESTING': return 'secondary';
      case 'COMPLETADO': return 'success';
    }
  }
}

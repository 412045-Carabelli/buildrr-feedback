import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { FileUploadModule, FileUploadHandlerEvent } from 'primeng/fileupload';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { TicketsService } from '../../../../services/tickets/tickets.service';
import { AdjuntosService } from '../../../../services/adjuntos/adjuntos.service';
import { AdjuntoResponse, EstadoTicket, TicketResponse } from '../../../../core/models/models';

// Mismas transiciones que ticket/estado/ en el backend — ver docs/03-ciclo-de-vida.md.
const TRANSICIONES: Record<EstadoTicket, EstadoTicket[]> = {
  NUEVO: ['EN_PROGRESO'],
  EN_PROGRESO: ['TESTING'],
  TESTING: ['COMPLETADO', 'EN_PROGRESO'],
  COMPLETADO: []
};

@Component({
  selector: 'app-tickets-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, TagModule, ButtonModule, SelectModule, FileUploadModule, ToastModule],
  providers: [MessageService],
  templateUrl: './tickets-detail.component.html'
})
export class TicketsDetailComponent implements OnInit {
  ticket: TicketResponse | null = null;
  adjuntos: AdjuntoResponse[] = [];
  cargando = false;
  cambiandoEstado = false;
  estadoDestino: EstadoTicket | null = null;

  constructor(
    private route: ActivatedRoute,
    private ticketsService: TicketsService,
    private adjuntosService: AdjuntosService,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.cargar(id);
  }

  private cargar(id: number): void {
    this.cargando = true;
    this.ticketsService.obtenerPorId(id).subscribe({
      next: (ticket) => {
        this.ticket = ticket;
        this.estadoDestino = null;
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo cargar el ticket' });
      }
    });
    this.adjuntosService.listarPorTicket(id).subscribe({
      next: (data) => (this.adjuntos = data)
    });
  }

  get opcionesEstado(): { label: string; value: EstadoTicket }[] {
    if (!this.ticket) return [];
    return TRANSICIONES[this.ticket.estado].map((e) => ({ label: e, value: e }));
  }

  cambiarEstado(): void {
    if (!this.ticket || !this.estadoDestino) return;

    this.cambiandoEstado = true;
    this.ticketsService.cambiarEstado(this.ticket.id, { estadoNuevo: this.estadoDestino }).subscribe({
      next: (ticket) => {
        this.ticket = ticket;
        this.estadoDestino = null;
        this.cambiandoEstado = false;
        this.messageService.add({ severity: 'success', summary: 'Listo', detail: 'Estado actualizado' });
      },
      error: (err) => {
        this.cambiandoEstado = false;
        const detalle = err?.error?.message ?? 'No se pudo cambiar el estado';
        this.messageService.add({ severity: 'error', summary: 'Error', detail: detalle });
      }
    });
  }

  subirAdjunto(event: FileUploadHandlerEvent): void {
    if (!this.ticket) return;
    const archivo = event.files[0];
    this.adjuntosService.subir(this.ticket.id, archivo).subscribe({
      next: (adjunto) => {
        this.adjuntos = [...this.adjuntos, adjunto];
        this.messageService.add({ severity: 'success', summary: 'Listo', detail: 'Adjunto subido' });
      },
      error: (err) => {
        const detalle = err?.error?.message ?? 'No se pudo subir el archivo';
        this.messageService.add({ severity: 'error', summary: 'Error', detail: detalle });
      }
    });
  }

  descargarAdjunto(adjunto: AdjuntoResponse): void {
    this.adjuntosService.descargar(adjunto.id).subscribe((blob) => {
      const url = window.URL.createObjectURL(blob);
      const enlace = document.createElement('a');
      enlace.href = url;
      enlace.download = adjunto.nombreOriginal;
      enlace.click();
      window.URL.revokeObjectURL(url);
    });
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

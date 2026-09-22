import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { TooltipModule } from 'primeng/tooltip';
import { TabsModule } from 'primeng/tabs';
import { TimelineModule } from 'primeng/timeline';
import { FileUploadModule, FileUploadHandlerEvent } from 'primeng/fileupload';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DialogModule } from 'primeng/dialog';
import { EditorModule } from 'primeng/editor';
import { FormsModule } from '@angular/forms';
import { MessageService, ConfirmationService } from 'primeng/api';
import { TicketsService } from '../../../../services/tickets/tickets.service';
import { AdjuntosService } from '../../../../services/adjuntos/adjuntos.service';
import { AplicacionSeleccionadaService } from '../../../../services/aplicaciones/aplicacion-seleccionada.service';
import { AuthService } from '../../../../services/auth/auth.service';
import { AdjuntoResponse, EstadoTicket, HistorialEstadoResponse, TicketResponse } from '../../../../core/models/models';
import { NOMBRE_ESTADO } from '../../../../core/constants/estado-labels';
import { NOMBRE_PRODUCTO } from '../../../../core/constants/producto-labels';
import { LayoutHeaderComponent } from '../../../../shared/layout-header/layout-header.component';
import { KpiCardComponent } from '../../../../shared/kpi-card/kpi-card.component';

interface AccionEstado {
  label: string;
  destino: EstadoTicket;
  icon: string;
  primaria: boolean;
  confirmar?: string;
}

// Mismas transiciones que ticket/estado/ en el backend — ver docs/03-ciclo-de-vida.md.
// Un botón por acción posible (no un dropdown genérico): la mayoría de los
// estados tienen un único paso siguiente. TESTING es la excepción real (puede
// andar o no) y COMPLETADO solo permite reabrir el ciclo, con confirmación.
// ANULADO no aparece acá — es su propio botón en el membrete, no un "siguiente paso".
const ACCIONES: Record<EstadoTicket, AccionEstado[]> = {
  NUEVO: [{ label: 'Arrancar a trabajar', destino: 'EN_PROGRESO', icon: 'pi pi-play', primaria: true }],
  EN_PROGRESO: [{ label: 'Pasar a testing', destino: 'TESTING', icon: 'pi pi-forward', primaria: true }],
  TESTING: [
    { label: 'Marcar completado', destino: 'COMPLETADO', icon: 'pi pi-check', primaria: true },
    { label: 'No funciona, volver a en progreso', destino: 'EN_PROGRESO', icon: 'pi pi-undo', primaria: false }
  ],
  COMPLETADO: [
    {
      label: 'Reabrir ciclo',
      destino: 'NUEVO',
      icon: 'pi pi-refresh',
      primaria: false,
      confirmar: '¿Seguro que querés reabrir el ciclo de vida de este ticket? Vuelve a NUEVO.'
    }
  ],
  ANULADO: []
};

// Desde qué estados se puede anular — coincide con transicionesPermitidas() de
// cada EstadoTicket en el backend (ver ticket/estado/).
const ESTADOS_ANULABLES: EstadoTicket[] = ['NUEVO', 'EN_PROGRESO', 'TESTING'];

@Component({
  selector: 'app-tickets-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    TagModule,
    ButtonModule,
    TooltipModule,
    TabsModule,
    TimelineModule,
    FileUploadModule,
    ToastModule,
    ConfirmDialogModule,
    DialogModule,
    EditorModule,
    FormsModule,
    LayoutHeaderComponent,
    KpiCardComponent
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './tickets-detail.component.html'
})
export class TicketsDetailComponent implements OnInit {
  ticket: TicketResponse | null = null;
  adjuntos: AdjuntoResponse[] = [];
  historial: HistorialEstadoResponse[] = [];
  cargando = false;
  cambiandoEstado = false;

  /** Modal de motivo — obligatorio cuando TESTING vuelve a EN_PROGRESO ("no funciona"). */
  mostrarDialogoMotivo = false;
  motivoNota = '';
  private destinoPendiente: EstadoTicket | null = null;

  constructor(
    private route: ActivatedRoute,
    private ticketsService: TicketsService,
    private adjuntosService: AdjuntosService,
    public aplicacionSeleccionadaService: AplicacionSeleccionadaService,
    private authService: AuthService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService
  ) {}

  get puedeGestionar(): boolean {
    return !!this.ticket && this.aplicacionSeleccionadaService.esAdminDe(this.ticket.producto);
  }

  /** El creador puede validar el resultado de un TESTING sin ser admin — mismo criterio que el backend. */
  get puedeValidarTesting(): boolean {
    return !!this.ticket && this.ticket.estado === 'TESTING' && this.ticket.creadoPor === this.authService.getUsername();
  }

  get puedeCambiarEstado(): boolean {
    return this.puedeGestionar || this.puedeValidarTesting;
  }

  puedeBorrarAdjunto(adjunto: AdjuntoResponse): boolean {
    return this.puedeGestionar || adjunto.subidoPor === this.authService.getUsername();
  }

  get puedeEditar(): boolean {
    if (!this.ticket || this.ticket.estado === 'ANULADO') return false;
    return this.ticket.creadoPor === this.authService.getUsername() || this.puedeGestionar;
  }

  get puedeAnular(): boolean {
    return this.puedeGestionar && !!this.ticket && ESTADOS_ANULABLES.includes(this.ticket.estado);
  }

  get acciones(): AccionEstado[] {
    return this.ticket ? ACCIONES[this.ticket.estado] : [];
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.cargar(id);
  }

  private cargar(id: number): void {
    this.cargando = true;
    this.ticketsService.obtenerPorId(id).subscribe({
      next: (ticket) => {
        this.ticket = ticket;
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
    this.ticketsService.historial(id).subscribe({
      next: (data) => (this.historial = data)
    });
  }

  labelEstado(estado: EstadoTicket): string {
    return NOMBRE_ESTADO[estado];
  }

  labelProducto(producto: TicketResponse['producto']): string {
    return NOMBRE_PRODUCTO[producto];
  }

  ejecutarAccion(accion: AccionEstado): void {
    // "No funciona, volver a en progreso" desde TESTING — pide motivo antes de mandar la transición.
    if (this.ticket?.estado === 'TESTING' && accion.destino === 'EN_PROGRESO') {
      this.destinoPendiente = accion.destino;
      this.motivoNota = '';
      this.mostrarDialogoMotivo = true;
      return;
    }

    if (accion.confirmar) {
      this.confirmationService.confirm({
        message: accion.confirmar,
        header: 'Confirmar',
        icon: 'pi pi-exclamation-triangle',
        accept: () => this.cambiarEstado(accion.destino)
      });
      return;
    }
    this.cambiarEstado(accion.destino);
  }

  get motivoVacio(): boolean {
    const texto = (this.motivoNota || '').replace(/<[^>]*>/g, '').trim();
    return texto.length === 0;
  }

  cancelarDialogoMotivo(): void {
    this.mostrarDialogoMotivo = false;
    this.destinoPendiente = null;
  }

  confirmarDialogoMotivo(): void {
    if (!this.destinoPendiente || this.motivoVacio) return;
    const destino = this.destinoPendiente;
    this.mostrarDialogoMotivo = false;
    this.cambiarEstado(destino, this.motivoNota);
  }

  confirmarAnular(): void {
    this.confirmationService.confirm({
      message: '¿Seguro que querés anular este ticket? No se puede deshacer.',
      header: 'Anular ticket',
      icon: 'pi pi-exclamation-triangle',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => this.cambiarEstado('ANULADO')
    });
  }

  private cambiarEstado(destino: EstadoTicket, nota?: string): void {
    if (!this.ticket) return;

    this.cambiandoEstado = true;
    this.ticketsService.cambiarEstado(this.ticket.id, { estadoNuevo: destino, nota }).subscribe({
      next: (ticket) => {
        this.ticket = ticket;
        this.cambiandoEstado = false;
        this.messageService.add({ severity: 'success', summary: 'Listo', detail: 'Estado actualizado' });
        this.ticketsService.historial(ticket.id).subscribe((data) => (this.historial = data));
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

  /** Imagen o PDF: se puede ver en el navegador sin descargar, mismo criterio que documentos-service de SGO. */
  esVisualizable(adjunto: AdjuntoResponse): boolean {
    return adjunto.tipo === 'FOTO' || adjunto.contentType === 'application/pdf';
  }

  abrirAdjunto(adjunto: AdjuntoResponse): void {
    this.adjuntosService.descargar(adjunto.id).subscribe((blob) => {
      const url = window.URL.createObjectURL(blob);
      if (this.esVisualizable(adjunto)) {
        // Sin revoke inmediato: la pestaña nueva necesita que el blob siga vivo.
        window.open(url, '_blank');
        return;
      }
      const enlace = document.createElement('a');
      enlace.href = url;
      enlace.download = adjunto.nombreOriginal;
      enlace.click();
      window.URL.revokeObjectURL(url);
    });
  }

  confirmarEliminarAdjunto(adjunto: AdjuntoResponse): void {
    this.confirmationService.confirm({
      message: `¿Borrar "${adjunto.nombreOriginal}"? No se puede deshacer.`,
      header: 'Borrar adjunto',
      icon: 'pi pi-exclamation-triangle',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        this.adjuntosService.eliminar(adjunto.id).subscribe({
          next: () => {
            this.adjuntos = this.adjuntos.filter((a) => a.id !== adjunto.id);
            this.messageService.add({ severity: 'success', summary: 'Listo', detail: 'Adjunto borrado' });
          },
          error: (err) => {
            const detalle = err?.error?.message ?? 'No se pudo borrar el adjunto';
            this.messageService.add({ severity: 'error', summary: 'Error', detail: detalle });
          }
        });
      }
    });
  }

  severityEstado(estado: EstadoTicket): 'info' | 'warn' | 'secondary' | 'success' | 'danger' {
    switch (estado) {
      case 'NUEVO': return 'info';
      case 'EN_PROGRESO': return 'warn';
      case 'TESTING': return 'secondary';
      case 'COMPLETADO': return 'success';
      case 'ANULADO': return 'danger';
    }
  }
}

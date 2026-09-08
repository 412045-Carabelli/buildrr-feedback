import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { SelectModule } from 'primeng/select';
import { InputTextModule } from 'primeng/inputtext';
import { DatePickerModule } from 'primeng/datepicker';
import { EditorModule } from 'primeng/editor';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { TicketsService } from '../../../../services/tickets/tickets.service';
import { AdjuntosService } from '../../../../services/adjuntos/adjuntos.service';
import { AplicacionSeleccionadaService } from '../../../../services/aplicaciones/aplicacion-seleccionada.service';
import { NOMBRE_PRODUCTO } from '../../../../core/constants/producto-labels';
import { LayoutHeaderComponent } from '../../../../shared/layout-header/layout-header.component';

@Component({
  selector: 'app-tickets-create',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    SelectModule,
    InputTextModule,
    DatePickerModule,
    EditorModule,
    ButtonModule,
    ToastModule,
    LayoutHeaderComponent
  ],
  providers: [MessageService],
  templateUrl: './tickets-create.component.html'
})
export class TicketsCreateComponent {
  form: FormGroup;
  guardando = false;
  archivosSeleccionados: File[] = [];

  tipos = [
    { label: 'Bug', value: 'BUG' },
    { label: 'Función nueva', value: 'FUNCION_NUEVA' }
  ];

  constructor(
    private fb: FormBuilder,
    private ticketsService: TicketsService,
    private adjuntosService: AdjuntosService,
    private aplicacionSeleccionadaService: AplicacionSeleccionadaService,
    private router: Router,
    private messageService: MessageService
  ) {
    this.form = this.fb.group({
      tipo: ['BUG', [Validators.required]],
      titulo: ['', [Validators.required, Validators.minLength(3)]],
      modulo: [''],
      fecha: [new Date(), [Validators.required]],
      descripcion: ['']
    });
  }

  get nombreAplicacionSeleccionada(): string {
    const producto = this.aplicacionSeleccionadaService.seleccionadaActual;
    return producto ? NOMBRE_PRODUCTO[producto] : '';
  }

  agregarArchivos(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.archivosSeleccionados = [...this.archivosSeleccionados, ...Array.from(input.files)];
    }
    input.value = '';
  }

  quitarArchivo(index: number): void {
    this.archivosSeleccionados = this.archivosSeleccionados.filter((_, i) => i !== index);
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const producto = this.aplicacionSeleccionadaService.seleccionadaActual;
    if (!producto) {
      this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No tenés ninguna aplicación asignada' });
      return;
    }

    this.guardando = true;
    const { fecha, ...resto } = this.form.getRawValue();
    const payload = { ...resto, producto, fecha: this.aIsoDate(fecha) };

    this.ticketsService.crear(payload).subscribe({
      next: (ticket) => this.subirArchivosYNavegar(ticket.id),
      error: (err) => {
        this.guardando = false;
        const detalle = err?.error?.message ?? 'No se pudo crear el ticket';
        this.messageService.add({ severity: 'error', summary: 'Error', detail: detalle });
      }
    });
  }

  private subirArchivosYNavegar(ticketId: number): void {
    if (this.archivosSeleccionados.length === 0) {
      this.guardando = false;
      this.router.navigate(['/tickets', ticketId]);
      return;
    }

    const subidas = this.archivosSeleccionados.map((archivo) =>
      this.adjuntosService.subir(ticketId, archivo)
    );

    forkJoin(subidas).subscribe({
      next: () => {
        this.guardando = false;
        this.router.navigate(['/tickets', ticketId]);
      },
      error: () => {
        this.guardando = false;
        this.messageService.add({
          severity: 'warn',
          summary: 'Ticket creado',
          detail: 'Algunos adjuntos no se pudieron subir — podés reintentarlos desde el detalle'
        });
        this.router.navigate(['/tickets', ticketId]);
      }
    });
  }

  private aIsoDate(fecha: Date): string {
    const y = fecha.getFullYear();
    const m = String(fecha.getMonth() + 1).padStart(2, '0');
    const d = String(fecha.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
  }
}

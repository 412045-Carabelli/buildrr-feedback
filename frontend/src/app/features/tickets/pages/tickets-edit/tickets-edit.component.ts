import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { DatePickerModule } from 'primeng/datepicker';
import { EditorModule } from 'primeng/editor';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { TicketsService } from '../../../../services/tickets/tickets.service';
import { NOMBRE_PRODUCTO } from '../../../../core/constants/producto-labels';
import { TicketResponse } from '../../../../core/models/models';
import { LayoutHeaderComponent } from '../../../../shared/layout-header/layout-header.component';

@Component({
  selector: 'app-tickets-edit',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    InputTextModule,
    DatePickerModule,
    EditorModule,
    ButtonModule,
    ToastModule,
    LayoutHeaderComponent
  ],
  providers: [MessageService],
  templateUrl: './tickets-edit.component.html'
})
export class TicketsEditComponent implements OnInit {
  ticket: TicketResponse | null = null;
  form: FormGroup;
  cargando = false;
  guardando = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private ticketsService: TicketsService,
    private messageService: MessageService
  ) {
    this.form = this.fb.group({
      titulo: ['', [Validators.required, Validators.minLength(3)]],
      modulo: [''],
      fecha: [new Date(), [Validators.required]],
      descripcion: ['']
    });
  }

  get nombreProducto(): string {
    return this.ticket ? NOMBRE_PRODUCTO[this.ticket.producto] : '';
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.cargando = true;
    this.ticketsService.obtenerPorId(id).subscribe({
      next: (ticket) => {
        this.ticket = ticket;
        this.form.patchValue({
          titulo: ticket.titulo,
          modulo: ticket.modulo,
          fecha: new Date(ticket.fecha + 'T00:00:00'),
          descripcion: ticket.descripcion
        });
        this.cargando = false;
      },
      error: () => {
        this.cargando = false;
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo cargar el ticket' });
      }
    });
  }

  guardar(): void {
    if (!this.ticket || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.guardando = true;
    const { fecha, ...resto } = this.form.getRawValue();
    const payload = { ...resto, fecha: this.aIsoDate(fecha) };

    this.ticketsService.editar(this.ticket.id, payload).subscribe({
      next: (ticket) => {
        this.guardando = false;
        this.router.navigate(['/tickets', ticket.id]);
      },
      error: (err) => {
        this.guardando = false;
        const detalle = err?.error?.message ?? 'No se pudo guardar el ticket';
        this.messageService.add({ severity: 'error', summary: 'Error', detail: detalle });
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

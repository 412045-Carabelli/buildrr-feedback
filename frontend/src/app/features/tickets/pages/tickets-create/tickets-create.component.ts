import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { SelectModule } from 'primeng/select';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { ButtonModule } from 'primeng/button';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { TicketsService } from '../../../../services/tickets/tickets.service';

@Component({
  selector: 'app-tickets-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, SelectModule, InputTextModule, TextareaModule, ButtonModule, ToastModule],
  providers: [MessageService],
  templateUrl: './tickets-create.component.html'
})
export class TicketsCreateComponent {
  form: FormGroup;
  guardando = false;

  tipos = [
    { label: 'Bug', value: 'BUG' },
    { label: 'Función nueva', value: 'FUNCION_NUEVA' }
  ];

  productos = [
    { label: 'Sistema de Gestión de Obras', value: 'SGO' },
    { label: 'FrezCo', value: 'FRESCO' }
  ];

  constructor(
    private fb: FormBuilder,
    private ticketsService: TicketsService,
    private router: Router,
    private messageService: MessageService
  ) {
    this.form = this.fb.group({
      tipo: ['BUG', [Validators.required]],
      producto: ['SGO', [Validators.required]],
      titulo: ['', [Validators.required, Validators.minLength(3)]],
      descripcion: ['']
    });
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.guardando = true;
    this.ticketsService.crear(this.form.getRawValue()).subscribe({
      next: (ticket) => {
        this.guardando = false;
        this.router.navigate(['/tickets', ticket.id]);
      },
      error: (err) => {
        this.guardando = false;
        const detalle = err?.error?.message ?? 'No se pudo crear el ticket';
        this.messageService.add({ severity: 'error', summary: 'Error', detail: detalle });
      }
    });
  }
}

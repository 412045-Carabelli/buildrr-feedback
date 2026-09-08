import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { InputTextModule } from 'primeng/inputtext';
import { ToastModule } from 'primeng/toast';
import { MessageService, ConfirmationService } from 'primeng/api';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { switchMap } from 'rxjs/operators';
import { UsuariosAplicacionService } from '../../../services/admin/usuarios-aplicacion.service';
import { AplicacionSeleccionadaService } from '../../../services/aplicaciones/aplicacion-seleccionada.service';
import { NOMBRE_PRODUCTO } from '../../../core/constants/producto-labels';
import { Producto, RolAplicacion, UsuarioAplicacionResponse } from '../../../core/models/models';
import { LayoutHeaderComponent } from '../../../shared/layout-header/layout-header.component';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    TagModule,
    ButtonModule,
    SelectModule,
    InputTextModule,
    ToastModule,
    ConfirmDialogModule,
    LayoutHeaderComponent
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './usuarios.component.html'
})
export class UsuariosComponent implements OnInit {
  usuarios: UsuarioAplicacionResponse[] = [];
  cargando = false;
  dandoDeAlta = false;

  nuevoUsername = '';
  nuevoRol: RolAplicacion = 'CLIENTE';

  opcionesRol: { label: string; value: RolAplicacion }[] = [
    { label: 'Cliente', value: 'CLIENTE' },
    { label: 'Admin', value: 'ADMIN' }
  ];

  constructor(
    private usuariosAplicacionService: UsuariosAplicacionService,
    public aplicacionSeleccionadaService: AplicacionSeleccionadaService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService
  ) {}

  get productoActual(): Producto | null {
    return this.aplicacionSeleccionadaService.seleccionadaActual;
  }

  get nombreProductoActual(): string {
    return this.productoActual ? NOMBRE_PRODUCTO[this.productoActual] : '';
  }

  ngOnInit(): void {
    // Igual que Tickets/Dashboard: re-consulta si cambia la app seleccionada en el navbar.
    this.cargando = true;
    this.aplicacionSeleccionadaService.seleccionada$
      .pipe(switchMap((producto) => this.usuariosAplicacionService.listar(producto as Producto)))
      .subscribe({
        next: (data) => {
          this.usuarios = data;
          this.cargando = false;
        },
        error: () => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudieron cargar los usuarios' });
          this.cargando = false;
        }
      });
  }

  darDeAlta(): void {
    if (!this.productoActual || !this.nuevoUsername.trim()) return;

    this.dandoDeAlta = true;
    this.usuariosAplicacionService
      .darDeAlta({ username: this.nuevoUsername.trim(), producto: this.productoActual, rol: this.nuevoRol })
      .subscribe({
        next: (usuario) => {
          this.usuarios = [...this.usuarios, usuario];
          this.nuevoUsername = '';
          this.nuevoRol = 'CLIENTE';
          this.dandoDeAlta = false;
          this.messageService.add({ severity: 'success', summary: 'Listo', detail: 'Usuario dado de alta' });
        },
        error: (err) => {
          this.dandoDeAlta = false;
          const detalle = err?.error?.message ?? 'No se pudo dar de alta el usuario';
          this.messageService.add({ severity: 'error', summary: 'Error', detail: detalle });
        }
      });
  }

  confirmarRevocar(usuario: UsuarioAplicacionResponse): void {
    this.confirmationService.confirm({
      message: `¿Revocar el acceso de ${usuario.username} a ${this.nombreProductoActual}?`,
      header: 'Confirmar',
      icon: 'pi pi-exclamation-triangle',
      accept: () => this.revocar(usuario)
    });
  }

  private revocar(usuario: UsuarioAplicacionResponse): void {
    this.usuariosAplicacionService.revocar(usuario.id).subscribe({
      next: () => {
        this.usuarios = this.usuarios.filter((u) => u.id !== usuario.id);
        this.messageService.add({ severity: 'success', summary: 'Listo', detail: 'Acceso revocado' });
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo revocar el acceso' });
      }
    });
  }
}

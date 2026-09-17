import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { AuthService } from '../../../services/auth/auth.service';
import { ForgotPasswordRequest, ResetPasswordRequest } from '../../../core/models/models';

@Component({
  selector: 'app-cambiar-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, ButtonModule, InputTextModule, MessageModule, ToastModule],
  providers: [MessageService],
  templateUrl: './cambiar-password.component.html'
})
export class CambiarPasswordComponent {
  paso: 'email' | 'codigo' = 'email';
  emailForm: FormGroup;
  codigoForm: FormGroup;
  cargando = false;
  emailEnviado = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private messageService: MessageService,
    private router: Router
  ) {
    this.emailForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });

    this.codigoForm = this.fb.group(
      {
        code: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
        newPassword: ['', [Validators.required, Validators.minLength(8)]],
        confirmPassword: ['', [Validators.required]]
      },
      { validators: this.passwordsCoinciden }
    );
  }

  solicitarCodigo(): void {
    if (this.emailForm.invalid) {
      this.emailForm.markAllAsTouched();
      return;
    }

    this.cargando = true;
    const request: ForgotPasswordRequest = this.emailForm.getRawValue();

    this.authService.forgotPassword(request).subscribe({
      next: () => {
        this.cargando = false;
        this.emailEnviado = request.email;
        this.paso = 'codigo';
        this.messageService.add({
          severity: 'success',
          summary: 'Código enviado',
          detail: 'Si el email existe, te enviamos un código. Revisá tu bandeja de entrada.'
        });
      },
      error: () => {
        this.cargando = false;
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'No se pudo procesar la solicitud. Probá de nuevo.' });
      }
    });
  }

  confirmarReset(): void {
    if (this.codigoForm.invalid) {
      this.codigoForm.markAllAsTouched();
      return;
    }

    this.cargando = true;
    const request: ResetPasswordRequest = {
      email: this.emailEnviado,
      ...this.codigoForm.getRawValue()
    };

    this.authService.resetPassword(request).subscribe({
      next: () => {
        this.cargando = false;
        this.messageService.add({ severity: 'success', summary: 'Listo', detail: 'Contraseña actualizada, ya podés loguearte' });
        setTimeout(() => this.router.navigate(['/login']), 1200);
      },
      error: (err) => {
        this.cargando = false;
        const detalle = err?.error?.message ?? 'Código inválido o expirado';
        this.messageService.add({ severity: 'error', summary: 'Error', detail: detalle });
      }
    });
  }

  volverAEmail(): void {
    this.paso = 'email';
  }

  private passwordsCoinciden(group: AbstractControl): ValidationErrors | null {
    const nueva = group.get('newPassword')?.value;
    const confirmacion = group.get('confirmPassword')?.value;

    if (nueva && confirmacion && nueva !== confirmacion) {
      group.get('confirmPassword')?.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    if (confirmacion && nueva === confirmacion) {
      group.get('confirmPassword')?.setErrors(null);
    }
    return null;
  }
}

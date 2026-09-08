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

/**
 * TEMPORAL — cambia contraseña por email, sin loguearse ni validar la
 * contraseña actual (pedido explícito, mientras no hay cuenta usable). Ver
 * ResetPasswordSinLoginRequest en auth-service. Cuando deje de hacer falta:
 * volver a currentPassword + AuthService.changePassword, y reponer authGuard
 * en la ruta (app.routes.ts).
 */
@Component({
  selector: 'app-cambiar-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, ButtonModule, InputTextModule, MessageModule, ToastModule],
  providers: [MessageService],
  templateUrl: './cambiar-password.component.html'
})
export class CambiarPasswordComponent {
  form: FormGroup;
  cambiando = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private messageService: MessageService,
    private router: Router
  ) {
    this.form = this.fb.group(
      {
        email: ['', [Validators.required, Validators.email]],
        newPassword: ['', [Validators.required, Validators.minLength(8)]],
        confirmPassword: ['', [Validators.required]]
      },
      { validators: this.passwordsCoinciden }
    );
  }

  cambiar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.cambiando = true;
    this.authService.resetPasswordSinLogin(this.form.getRawValue()).subscribe({
      next: () => {
        this.cambiando = false;
        this.messageService.add({ severity: 'success', summary: 'Listo', detail: 'Contraseña actualizada, ya podés loguearte' });
        this.form.reset();
        setTimeout(() => this.router.navigate(['/login']), 1200);
      },
      error: (err) => {
        this.cambiando = false;
        const detalle = err?.error?.message ?? 'No se pudo cambiar la contraseña';
        this.messageService.add({ severity: 'error', summary: 'Error', detail: detalle });
      }
    });
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

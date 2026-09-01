import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { AuthService } from '../../services/auth/auth.service';
import { AplicacionSeleccionadaService } from '../../services/aplicaciones/aplicacion-seleccionada.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, InputTextModule, PasswordModule, ButtonModule, MessageModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  form: FormGroup;
  cargando = false;
  error = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private aplicacionSeleccionadaService: AplicacionSeleccionadaService,
    private router: Router
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  ingresar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.cargando = true;
    this.error = '';
    this.authService.login(this.form.getRawValue()).subscribe({
      next: () => {
        this.aplicacionSeleccionadaService.cargar().subscribe(() => {
          this.cargando = false;
          this.router.navigate(['/tickets']);
        });
      },
      error: () => {
        this.cargando = false;
        this.error = 'Usuario o contraseña incorrectos';
      }
    });
  }
}

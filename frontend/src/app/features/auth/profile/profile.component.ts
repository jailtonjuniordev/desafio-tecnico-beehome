import {HttpErrorResponse} from '@angular/common/http';
import {Component, OnInit, inject, signal} from '@angular/core';
import {AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {translateApiErrorMessage} from '../../../core/constants/api-error-messages';
import {AuthService} from '../../../core/services/auth.service';
import {NotificationService} from '../../../core/services/notification.service';
import {UpdateProfileRequest} from '../models/auth.model';
import {HeaderComponent} from '../../../shared/components/header/header.component';

function optionalMinLength(min: number) {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as string | null | undefined;
    if (value == null || value === '') {
      return null;
    }
    return value.length >= min ? null : {minlength: {requiredLength: min, actualLength: value.length}};
  };
}

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, HeaderComponent, MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule, MatProgressBarModule,],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss',
})
export class ProfileComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly notification = inject(NotificationService);

  private initialUsername = '';
  private initialEmail = '';

  readonly loading = signal(false);
  readonly hasChanges = signal(false);
  readonly hidePassword = signal(true);

  readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [optionalMinLength(8)]],
  });

  ngOnInit(): void {
    this.form.valueChanges.subscribe(() => this.refreshHasChanges());
    this.loadProfile();
  }

  togglePasswordVisibility(): void {
    this.hidePassword.update((value) => !value);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const payload = this.buildChangedPayload();
    if (!payload) {
      this.notification.success('Nenhuma alteracao para salvar.');
      return;
    }

    this.loading.set(true);
    this.authService.updateMe(payload).subscribe({
      next: (profile) => {
        this.rememberInitial(profile.username, profile.email);
        this.form.patchValue({password: ''}, {emitEvent: false});
        this.refreshHasChanges();
        this.loading.set(false);
        this.notification.success('Perfil atualizado com sucesso.');
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        if (error.status === 401 || error.status === 403) {
          return;
        }
        this.notification.error(this.resolveErrorMessage(error));
      },
    });
  }

  private loadProfile(): void {
    this.loading.set(true);
    this.authService.getMe().subscribe({
      next: (profile) => {
        this.rememberInitial(profile.username, profile.email);
        this.form.patchValue(
          {
            username: profile.username,
            email: profile.email,
            password: '',
          },
          {emitEvent: false}
        );
        this.refreshHasChanges();
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        if (error.status === 401 || error.status === 403) {
          return;
        }
        this.notification.error(this.resolveLoadErrorMessage(error));
      },
    });
  }

  private buildChangedPayload(): UpdateProfileRequest | null {
    const {username, email, password} = this.form.getRawValue();
    const payload: UpdateProfileRequest = {};

    if (username.trim() !== this.initialUsername) {
      payload.username = username.trim();
    }
    if (email.trim() !== this.initialEmail) {
      payload.email = email.trim();
    }
    if (password.trim()) {
      payload.password = password;
    }

    return Object.keys(payload).length > 0 ? payload : null;
  }

  private rememberInitial(username: string, email: string): void {
    this.initialUsername = username;
    this.initialEmail = email;
  }

  private refreshHasChanges(): void {
    this.hasChanges.set(this.buildChangedPayload() !== null);
  }

  private resolveLoadErrorMessage(error: HttpErrorResponse): string {
    if (error.status === 0) {
      return 'Nao foi possivel conectar ao servidor.';
    }
    if (typeof error.error?.message === 'string') {
      return translateApiErrorMessage(error.error.message);
    }
    return 'Falha ao carregar o perfil.';
  }

  private resolveErrorMessage(error: HttpErrorResponse): string {
    if (error.status === 0) {
      return 'Nao foi possivel conectar ao servidor.';
    }
    if (typeof error.error?.message === 'string') {
      return translateApiErrorMessage(error.error.message);
    }
    return 'Falha ao atualizar o perfil. Tente novamente.';
  }
}

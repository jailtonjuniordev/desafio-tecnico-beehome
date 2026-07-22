import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import {
  BhSnackBarComponent,
  BhSnackBarVariant,
} from '../../shared/components/bh-snackbar/bh-snackbar.component';

const DEFAULT_DURATION_MS = 3000;

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly snackBar = inject(MatSnackBar);

  error(message: string): void {
    this.open(message, 'error');
  }

  success(message: string): void {
    this.open(message, 'success');
  }

  private open(message: string, variant: BhSnackBarVariant): void {
    this.snackBar.openFromComponent(BhSnackBarComponent, {
      data: { message, variant },
      duration: DEFAULT_DURATION_MS,
      panelClass: ['bh-snack-panel'],
      horizontalPosition: 'end',
      verticalPosition: 'top',
    });
  }
}

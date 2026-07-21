import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import {
  MAT_SNACK_BAR_DATA,
  MatSnackBarRef,
} from '@angular/material/snack-bar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';

export type BhSnackBarVariant = 'error' | 'success';

export interface BhSnackBarData {
  message: string;
  variant: BhSnackBarVariant;
}

@Component({
  selector: 'app-bh-snackbar',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatProgressBarModule],
  templateUrl: './bh-snackbar.component.html',
  styleUrl: './bh-snackbar.component.scss',
})
export class BhSnackBarComponent implements OnInit, OnDestroy {
  private readonly snackBarRef = inject(MatSnackBarRef<BhSnackBarComponent>);
  readonly data = inject<BhSnackBarData>(MAT_SNACK_BAR_DATA);

  readonly progress = signal(100);

  private frameId = 0;
  private startMs = 0;
  private durationMs = 3000;

  ngOnInit(): void {
    const configured = this.snackBarRef.containerInstance.snackBarConfig.duration;
    this.durationMs = configured && configured > 0 ? configured : 3000;
    this.startMs = performance.now();
    this.tick();
  }

  ngOnDestroy(): void {
    if (this.frameId) {
      cancelAnimationFrame(this.frameId);
    }
  }

  dismiss(): void {
    this.snackBarRef.dismiss();
  }

  private tick = (): void => {
    const elapsed = performance.now() - this.startMs;
    const remaining = Math.max(0, 100 - (elapsed / this.durationMs) * 100);
    this.progress.set(remaining);
    if (remaining > 0) {
      this.frameId = requestAnimationFrame(this.tick);
    }
  };
}

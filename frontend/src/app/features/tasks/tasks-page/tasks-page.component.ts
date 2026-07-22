import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { translateApiErrorMessage } from '../../../core/constants/api-error-messages';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import {
  TASK_STATUS_LABELS,
  Task,
  TaskRequest,
  TaskStatus,
} from '../models/task.model';
import { TaskService } from '../services/task.service';
import { TaskTableComponent } from '../task-table/task-table.component';
import {
  TaskFormDialogComponent,
  TaskFormDialogData,
} from '../task-form-dialog/task-form-dialog.component';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { HeaderComponent } from '../../../shared/components/header/header.component';

@Component({
  selector: 'app-tasks-page',
  standalone: true,
  imports: [
    FormsModule,
    HeaderComponent,
    TaskTableComponent,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatDatepickerModule,
    MatProgressBarModule,
    MatDialogModule,
    MatTooltipModule,
  ],
  templateUrl: './tasks-page.component.html',
  styleUrl: './tasks-page.component.scss',
})
export class TasksPageComponent implements OnInit {
  private readonly taskService = inject(TaskService);
  private readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly notification = inject(NotificationService);

  readonly statusLabels = TASK_STATUS_LABELS;
  readonly statusOptions: Array<TaskStatus | ''> = [
    '',
    'PENDING',
    'IN_PROGRESS',
    'COMPLETED',
  ];

  readonly loading = signal(false);
  readonly tasks = signal<Task[]>([]);
  readonly totalElements = signal(0);

  pageIndex = 0;
  pageSize = 10;
  statusFilter: TaskStatus | '' = '';
  sort: 'deadline,asc' | 'deadline,desc' = 'deadline,asc';
  deadlineStart: Date | null = null;
  deadlineEnd: Date | null = null;

  get username(): string {
    return this.authService.currentUser()?.username ?? 'Usuario';
  }

  ngOnInit(): void {
    this.authService.getMe().subscribe({
      error: () => {
        // Keep stored session user if profile refresh fails.
      },
    });
    this.loadTasks();
  }

  loadTasks(): void {
    this.loading.set(true);

    const hasDeadlineRange = this.deadlineStart != null && this.deadlineEnd != null;

    this.taskService
      .list({
        page: this.pageIndex,
        size: this.pageSize,
        status: this.statusFilter,
        sort: this.sort,
        deadlineStart: hasDeadlineRange
          ? this.toIsoStartOfDay(this.deadlineStart!)
          : undefined,
        deadlineEnd: hasDeadlineRange
          ? this.toIsoEndOfDay(this.deadlineEnd!)
          : undefined,
      })
      .subscribe({
        next: (page) => {
          this.tasks.set(page.content ?? []);
          this.totalElements.set(page.totalElements ?? 0);
          this.loading.set(false);
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

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadTasks();
  }

  onFilterChange(): void {
    this.pageIndex = 0;
    this.loadTasks();
  }

  onDeadlineStartChange(): void {
    if (!this.deadlineStart) {
      this.deadlineEnd = null;
      this.pageIndex = 0;
      this.loadTasks();
      return;
    }

    this.pageIndex = 0;
    this.loadTasks();
  }

  onDeadlineEndChange(): void {
    if (!this.deadlineStart || !this.deadlineEnd) {
      return;
    }

    this.pageIndex = 0;
    this.loadTasks();
  }

  clearDeadlineFilter(): void {
    this.deadlineStart = null;
    this.deadlineEnd = null;
    this.pageIndex = 0;
    this.loadTasks();
  }

  openCreateDialog(): void {
    const ref = this.dialog.open(TaskFormDialogComponent, {
      width: '480px',
      panelClass: 'bh-dialog-panel',
      autoFocus: 'dialog',
      data: {} as TaskFormDialogData,
    });

    ref.afterClosed().subscribe((payload?: TaskRequest) => {
      if (!payload) {
        return;
      }
      this.loading.set(true);
      this.taskService.create(payload).subscribe({
        next: () => {
          this.notification.success('Tarefa criada com sucesso.');
          this.loadTasks();
        },
        error: (error: HttpErrorResponse) => {
          this.loading.set(false);
          if (error.status === 401 || error.status === 403) {
            return;
          }
          this.notification.error(this.resolveErrorMessage(error));
        },
      });
    });
  }

  openEditDialog(task: Task): void {
    const ref = this.dialog.open(TaskFormDialogComponent, {
      width: '480px',
      panelClass: 'bh-dialog-panel',
      autoFocus: 'dialog',
      data: { task } as TaskFormDialogData,
    });

    ref.afterClosed().subscribe((payload?: TaskRequest) => {
      if (!payload) {
        return;
      }
      this.loading.set(true);
      this.taskService.update(task.id, payload).subscribe({
        next: () => {
          this.notification.success('Tarefa atualizada com sucesso.');
          this.loadTasks();
        },
        error: (error: HttpErrorResponse) => {
          this.loading.set(false);
          if (error.status === 401 || error.status === 403) {
            return;
          }
          this.notification.error(this.resolveErrorMessage(error));
        },
      });
    });
  }

  confirmDelete(task: Task): void {
    const ref = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      panelClass: 'bh-dialog-panel',
      autoFocus: 'dialog',
      data: {
        title: 'Excluir tarefa',
        message: `Deseja excluir a tarefa "${task.title}"?`,
        confirmLabel: 'Excluir',
      },
    });

    ref.afterClosed().subscribe((confirmed?: boolean) => {
      if (!confirmed) {
        return;
      }
      this.loading.set(true);
      this.taskService.delete(task.id).subscribe({
        next: () => {
          this.notification.success('Tarefa excluida com sucesso.');
          this.loadTasks();
        },
        error: (error: HttpErrorResponse) => {
          this.loading.set(false);
          if (error.status === 401 || error.status === 403) {
            return;
          }
          this.notification.error(this.resolveErrorMessage(error));
        },
      });
    });
  }

  private toIsoStartOfDay(date: Date): string {
    const start = new Date(date);
    start.setHours(0, 0, 0, 0);
    return start.toISOString();
  }

  private toIsoEndOfDay(date: Date): string {
    const end = new Date(date);
    end.setHours(23, 59, 59, 0);
    return end.toISOString();
  }

  private resolveErrorMessage(error: HttpErrorResponse): string {
    if (error.status === 0) {
      return 'Nao foi possivel conectar ao servidor.';
    }
    if (typeof error.error?.message === 'string') {
      return translateApiErrorMessage(error.error.message);
    }
    return 'Ocorreu um erro ao processar a solicitacao.';
  }
}

import { DatePipe } from '@angular/common';
import { Component, ViewEncapsulation, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageEvent } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { TASK_STATUS_LABELS, Task, TaskStatus } from '../models/task.model';

@Component({
  selector: 'app-task-table',
  standalone: true,
  imports: [
    DatePipe,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
  ],
  templateUrl: './task-table.component.html',
  styleUrl: './task-table.component.scss',
  encapsulation: ViewEncapsulation.None,
})
export class TaskTableComponent {
  readonly tasks = input.required<Task[]>();
  readonly totalElements = input<number>(0);
  readonly pageIndex = input<number>(0);
  readonly pageSize = input<number>(10);

  readonly editTask = output<Task>();
  readonly deleteTask = output<Task>();
  readonly pageChange = output<PageEvent>();

  readonly displayedColumns = [
    'title',
    'description',
    'status',
    'createdAt',
    'deadline',
    'actions',
  ];

  private readonly statusLabels = TASK_STATUS_LABELS;

  statusLabel(status: TaskStatus): string {
    return this.statusLabels[status];
  }

  statusClass(status: TaskStatus): string {
    switch (status) {
      case 'PENDING':
        return 'status-chip status-pending';
      case 'IN_PROGRESS':
        return 'status-chip status-in-progress';
      case 'COMPLETED':
        return 'status-chip status-completed';
      default:
        return 'status-chip';
    }
  }

  totalPages(): number {
    const size = this.pageSize();
    const total = this.totalElements();
    if (size <= 0) {
      return 1;
    }
    return Math.max(1, Math.ceil(total / size));
  }

  rangeStart(): number {
    if (this.totalElements() === 0) {
      return 0;
    }
    return this.pageIndex() * this.pageSize() + 1;
  }

  rangeEnd(): number {
    return Math.min(
      (this.pageIndex() + 1) * this.pageSize(),
      this.totalElements()
    );
  }

  canGoPrevious(): boolean {
    return this.pageIndex() > 0;
  }

  canGoNext(): boolean {
    return this.pageIndex() < this.totalPages() - 1;
  }

  goPrevious(): void {
    if (!this.canGoPrevious()) {
      return;
    }
    this.emitPageChange(this.pageIndex() - 1);
  }

  goNext(): void {
    if (!this.canGoNext()) {
      return;
    }
    this.emitPageChange(this.pageIndex() + 1);
  }

  private emitPageChange(nextIndex: number): void {
    this.pageChange.emit({
      pageIndex: nextIndex,
      pageSize: this.pageSize(),
      length: this.totalElements(),
      previousPageIndex: this.pageIndex(),
    });
  }
}

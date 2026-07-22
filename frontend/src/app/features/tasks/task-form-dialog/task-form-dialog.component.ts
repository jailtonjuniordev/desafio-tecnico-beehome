import { Component, inject, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { Task, TaskRequest, TASK_STATUS_LABELS, TaskStatus } from '../models/task.model';

export interface TaskFormDialogData {
  task?: Task;
}

function futureOrTodayDateValidator(
  control: AbstractControl
): ValidationErrors | null {
  const value = control.value;
  if (!value) {
    return null;
  }

  const date = value instanceof Date ? value : new Date(value);
  if (Number.isNaN(date.getTime())) {
    return { invalidDate: true };
  }

  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const candidate = new Date(date);
  candidate.setHours(0, 0, 0, 0);

  return candidate < today ? { pastDeadline: true } : null;
}

@Component({
  selector: 'app-task-form-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
  ],
  templateUrl: './task-form-dialog.component.html',
  styleUrl: './task-form-dialog.component.scss',
})
export class TaskFormDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<TaskFormDialogComponent>);
  readonly data = inject<TaskFormDialogData>(MAT_DIALOG_DATA);

  readonly statuses: TaskStatus[] = ['PENDING', 'IN_PROGRESS', 'COMPLETED'];
  readonly statusLabels = TASK_STATUS_LABELS;

  readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(255)]],
    description: [''],
    status: this.fb.nonNullable.control<TaskStatus>('PENDING', [
      Validators.required,
    ]),
    deadline: this.fb.control<Date | null>(null, [
      Validators.required,
      futureOrTodayDateValidator,
    ]),
  });

  get isEdit(): boolean {
    return !!this.data.task;
  }

  ngOnInit(): void {
    const task = this.data.task;
    if (!task) {
      return;
    }

    this.form.patchValue({
      title: task.title,
      description: task.description,
      status: task.status,
      deadline: new Date(task.deadline),
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const deadline = raw.deadline as Date;
    const payload: TaskRequest = {
      title: raw.title.trim(),
      description: raw.description.trim(),
      status: raw.status,
      deadline: this.toIsoDateTime(deadline),
    };

    this.dialogRef.close(payload);
  }

  private toIsoDateTime(date: Date): string {
    const endOfDay = new Date(date);
    endOfDay.setHours(23, 59, 59, 0);
    return endOfDay.toISOString();
  }
}

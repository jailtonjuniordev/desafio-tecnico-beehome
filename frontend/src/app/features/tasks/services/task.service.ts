import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Task, TaskPage, TaskQuery, TaskRequest } from '../models/task.model';

@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/tasks`;

  list(query: TaskQuery): Observable<TaskPage> {
    let params = new HttpParams().set('page', query.page).set('size', query.size).set('sort', query.sort ?? 'deadline,asc');

    if (query.status) {
      params = params.set('status', query.status);
    }

    if (query.deadlineStart) {
      params = params.set('deadlineStart', query.deadlineStart);
    }

    if (query.deadlineEnd) {
      params = params.set('deadlineEnd', query.deadlineEnd);
    }

    return this.http.get<TaskPage>(this.baseUrl, { params });
  }

  getById(id: string): Observable<Task> {
    return this.http.get<Task>(`${this.baseUrl}/${id}`);
  }

  create(payload: TaskRequest): Observable<Task> {
    return this.http.post<Task>(this.baseUrl, payload);
  }

  update(id: string, payload: TaskRequest): Observable<Task> {
    return this.http.put<Task>(`${this.baseUrl}/${id}`, payload);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}

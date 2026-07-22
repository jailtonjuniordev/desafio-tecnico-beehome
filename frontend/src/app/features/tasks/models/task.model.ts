export type TaskStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED';

export interface Task {
  id: string;
  title: string;
  description: string;
  status: TaskStatus;
  createdAt: string;
  deadline: string;
}

export interface TaskRequest {
  title: string;
  description: string;
  status: TaskStatus;
  deadline: string;
}

export interface TaskPage {
  content: Task[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface TaskQuery {
  page: number;
  size: number;
  status?: TaskStatus | '';
  sort?: 'deadline,asc' | 'deadline,desc';
  deadlineStart?: string;
  deadlineEnd?: string;
}

export const TASK_STATUS_LABELS: Record<TaskStatus, string> = {
  PENDING: 'Pendente',
  IN_PROGRESS: 'Em andamento',
  COMPLETED: 'Concluida',
};

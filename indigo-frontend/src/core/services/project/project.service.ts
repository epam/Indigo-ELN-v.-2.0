import { Injectable, signal } from '@angular/core';
import { ApiService } from '@/core/services/api.service';
import { catchError, finalize, tap, throwError } from 'rxjs';
import { Project } from '@core/types/entities/project.i';

@Injectable({
  providedIn: 'root',
})
export class ProjectService {
  constructor(private api: ApiService<unknown>) {}

  // Signals to hold the current project state
  readonly project = signal<Project | null>(null);
  readonly isLoading = signal<boolean>(false);
  readonly hasError = signal<boolean>(false);
  private readonly currentId = signal<string | null>(null);

  load(id: string) {
    this.currentId.set(id);
    this.project.set(null);
    this.isLoading.set(true);
    this.hasError.set(false);

    return this.api.request<Project>('get', `projects/${id}`).pipe(
      tap((project) => {
        this.project.set(project);
      }),
      catchError((error) => {
        this.hasError.set(true);
        return throwError(() => error);
      }),
      finalize(() => {
        this.isLoading.set(false);
      }),
    );
  }

  refresh() {
    const id = this.currentId();
    if (id) this.load(id);
  }

  reset() {
    this.currentId.set(null);
    this.project.set(null);
    this.isLoading.set(false);
    this.hasError.set(false);
  }
}

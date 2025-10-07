import { Injectable, signal } from '@angular/core';
import { NotebookDetail } from '@/core/types/entities/notebook-detail.i';
import { ApiService } from '@/core/services/api.service';

@Injectable()
export class NotebookService {
  constructor(private api: ApiService<unknown>) {}

  // Signals to hold the current notebook state
  readonly notebook = signal<NotebookDetail | null>(null);
  readonly isLoading = signal<boolean>(false);
  readonly hasError = signal<boolean>(false);
  private readonly currentId = signal<string | null>(null);

  // Public API
  setNotebook(notebookDetail: NotebookDetail | null) { this.notebook.set(notebookDetail); }
  setLoading(value: boolean) { this.isLoading.set(value); }
  setError(value: boolean) { this.hasError.set(value); }

  load(id: string) {
    this.currentId.set(id);
    this.isLoading.set(true);
    this.hasError.set(false);

    this.api.request<NotebookDetail>('get', `notebooks/${id}`).subscribe({
      next: (nb) => {
        this.notebook.set(nb);
        this.isLoading.set(false);
      },
      error: () => {
        this.hasError.set(true);
        this.isLoading.set(false);
      },
    });
  }

  refresh() {
    const id = this.currentId();
    if (id) this.load(id);
  }

  reset() {
    this.currentId.set(null);
    this.notebook.set(null);
    this.isLoading.set(false);
    this.hasError.set(false);
  }
}

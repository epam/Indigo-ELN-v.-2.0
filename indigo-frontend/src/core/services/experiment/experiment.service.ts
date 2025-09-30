import { Injectable, signal } from '@angular/core';
import { ExperimentDetail } from '@/core/types/entities/experiment-detail.i';
import { ApiService } from '@/core/services/api.service';

@Injectable()
export class ExperimentService {
  constructor(private api: ApiService<ExperimentDetail>) {}

  // Signals to hold the current experiment state
  readonly experiment = signal<ExperimentDetail | null>(null);
  readonly isLoading = signal<boolean>(false);
  readonly hasError = signal<boolean>(false);
  private readonly currentId = signal<string | null>(null);

  // Public API
  setExperiment(experimentDetail: ExperimentDetail | null) { this.experiment.set(experimentDetail); }
  setLoading(value: boolean) { this.isLoading.set(value); }
  setError(value: boolean) { this.hasError.set(value); }

  load(id: string) {
    this.currentId.set(id);
    this.isLoading.set(true);
    this.hasError.set(false);

    this.api.request<ExperimentDetail>('get', `experiments/${id}`).subscribe({
      next: (exp) => {
        this.experiment.set(exp);
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
    this.experiment.set(null);
    this.isLoading.set(false);
    this.hasError.set(false);
  }
}
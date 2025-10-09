import { inject, Injectable, signal } from '@angular/core';
import { ApiService } from '@/core/services/api.service';
import { ExperimentDetail } from '@core/types/entities/experiments/experiment-detail.i';

@Injectable()
export class ExperimentDetailService {
  private service = inject(ApiService);

  // Signals for experiment detail state
  readonly experimentDetail = signal<ExperimentDetail | null>(null);
  readonly isLoading = signal<boolean>(false);
  readonly hasError = signal<boolean>(false);
  private readonly currentId = signal<string | null>(null);

  // Query methods
  load(id: string) {
    this.currentId.set(id);
    this.isLoading.set(true);
    this.hasError.set(false);

    this.service
      .request<ExperimentDetail>('get', `experiments/${id}`)
      .subscribe({
        next: (exp) => {
          this.experimentDetail.set(exp);
          this.isLoading.set(false);
        },
        error: () => {
          this.hasError.set(true);
          this.isLoading.set(false);
        },
      });
  }

  // Setter methods
  setExperimentDetail(experimentDetail: ExperimentDetail | null) {
    this.experimentDetail.set(experimentDetail);
  }

  setLoading(value: boolean) {
    this.isLoading.set(value);
  }

  setError(value: boolean) {
    this.hasError.set(value);
  }

  // Utility methods
  refresh() {
    const id = this.currentId();
    if (id) this.load(id);
  }

  reset() {
    this.currentId.set(null);
    this.experimentDetail.set(null);
    this.isLoading.set(false);
    this.hasError.set(false);
  }
}
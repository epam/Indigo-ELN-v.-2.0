import { computed, inject, Injectable, signal } from '@angular/core';
import { ApiService } from '@/core/services/api.service';
import { ExperimentDetail } from '@core/types/entities/experiments/experiment-detail.i';
import { Mutation } from '@core/types/entities/experiments/mutation.i';
import { finalize, Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ExperimentDetailService {
  private service = inject(ApiService);

  // Signals for experiment detail state
  readonly experimentDetail = signal<ExperimentDetail | null>(null);
  readonly experimentModel = computed(() => this.experimentDetail()?.model);
  readonly lastLoadedDetail = signal<ExperimentDetail | null>(null);
  readonly isLoading = signal<boolean>(false);
  readonly hasError = signal<boolean>(false);
  readonly isUpdating = signal<boolean>(false);
  private readonly currentId = signal<string | null>(null);

  // Query methods
  load(id: string) {
    if (this.currentId() === id) {
      return;
    }
    this.currentId.set(id);
    this.isLoading.set(true);
    this.hasError.set(false);

    this.service
      .request<ExperimentDetail>('get', `experiments/${id}`)
      .pipe(finalize(() => this.isLoading.set(false)))
      .subscribe({
        next: (exp) => {
          this.experimentDetail.set(exp);
          this.lastLoadedDetail.set(structuredClone(exp));
        },
        error: () => this.hasError.set(true),
      });
  }

  // Update methods
  updateDataModel(mutation: Mutation): Observable<ExperimentDetail> {
    const id = this.currentId();

    if (!id) {
      console.warn('No experiment ID in context');
      return new Observable((observer) => {
        observer.error(new Error('No experiment ID available'));
      });
    }

    this.isUpdating.set(true);

    return this.service
      .request<ExperimentDetail>(
        'post',
        `experiments/${id}/mutate3?revision=${this.experimentDetail().revision}`,
        mutation,
      )
      .pipe(
        tap({
          next: (updated) => {
            this.experimentDetail.set(updated);
            this.lastLoadedDetail.set(updated);
            this.isUpdating.set(false);
          },
          error: () => {
            this.isUpdating.set(false);
            this.experimentDetail.set(this.lastLoadedDetail()); // revert to last known server state
          },
        }),
      );
  }

  // Utility methods
  refresh() {
    const id = this.currentId();
    if (id) this.load(id);
  }

  reset() {
    this.currentId.set(null);
    this.experimentDetail.set(null);
    this.lastLoadedDetail.set(null);
    this.isLoading.set(false);
    this.hasError.set(false);
  }
}

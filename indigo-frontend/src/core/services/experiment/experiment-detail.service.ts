import { inject, Injectable, signal } from '@angular/core';
import { ApiService } from '@/core/services/api.service';
import { ExperimentDetail } from '@core/types/entities/experiments/experiment-detail.i';
import { ExperimentModel } from '@core/types/entities/experiments/experiment.i';
import { Mutation } from '@core/types/entities/experiments/mutation.i';
import { finalize, Observable, tap } from 'rxjs';

@Injectable()
export class ExperimentDetailService {
  private service = inject(ApiService);

  // Signals for experiment detail state
  readonly experimentDetail = signal<ExperimentDetail | null>(null);
  readonly experimentModel = signal<ExperimentModel | null>(null);
  readonly isLoading = signal<boolean>(false);
  readonly hasError = signal<boolean>(false);
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
          this.experimentModel.set(exp.model);
        },
        error: () => this.hasError.set(true),
      });
  }

  // Update methods
  updateDataModel(mutation: Mutation): Observable<ExperimentModel> {
    const id = this.currentId();

    if (!id) {
      console.warn('No experiment ID in context');
      return new Observable((observer) => {
        observer.error(new Error('No experiment ID available'));
      });
    }

    const model = this.experimentModel();
    if (!model) {
      console.warn('No experiment model available');
      return new Observable((observer) => {
        observer.error(new Error('No experiment model available'));
      });
    }

    const payload = {
      model,
      mutation,
    };

    this.isLoading.set(true);
    this.hasError.set(false);

    return this.service
      .request<ExperimentModel>('post', `experiments/${id}/mutate`, payload)
      .pipe(
        tap({
          next: (updatedModel) => {
            this.experimentModel.set(updatedModel);
            this.isLoading.set(false);
          },
          error: () => {
            this.hasError.set(true);
            this.isLoading.set(false);
          },
        }),
      );
  }

  // Setter methods
  setExperimentDetail(experimentDetail: ExperimentDetail | null) {
    this.experimentDetail.set(experimentDetail);
  }

  setExperimentModel(experimentModel: ExperimentModel | null) {
    this.experimentModel.set(experimentModel);
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

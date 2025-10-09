import { inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ApiService } from '@/core/services/api.service';
import { ExperimentModel } from '@/core/types/entities/experiments/experiment.i';
import { MutateModelForm } from '@/core/types/entities/experiments/experiment-mutate-form.i';

@Injectable()
export class ExperimentModelService {
  private service = inject(ApiService);

  // Signals for experiment model state
  readonly experimentModel = signal<ExperimentModel | null>(null);
  readonly isLoading = signal<boolean>(false);
  readonly hasError = signal<boolean>(false);
  private readonly currentId = signal<string | null>(null);

  // Query methods
  load(experimentId: string) {
    this.currentId.set(experimentId);
    this.isLoading.set(true);
    this.hasError.set(false);

    this.service
      .request<ExperimentModel>('get', `experiments/${experimentId}/datamodel`)
      .subscribe({
        next: (model) => {
          this.experimentModel.set(model);
          this.isLoading.set(false);
        },
        error: (error) => {
          console.warn('No existing experiment model found or error loading:', error);
          this.experimentModel.set(null);
          this.hasError.set(true);
          this.isLoading.set(false);
        },
      });
  }

  updateDataModel(experimentId: string, payload: MutateModelForm): Observable<ExperimentModel> {
    this.isLoading.set(true);
    this.hasError.set(false);
    
    return this.service
      .request<ExperimentModel>('post', `experiments/${experimentId}/datamodel`, payload)
      .pipe(
        tap({
          next: (updatedModel) => {
            this.experimentModel.set(updatedModel);
            this.isLoading.set(false);
          },
          error: (error) => {
            console.error('Error updating experiment model:', error);
            this.hasError.set(true);
            this.isLoading.set(false);
          }
        })
      );
  }

  // Setter methods
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
    this.experimentModel.set(null);
    this.isLoading.set(false);
    this.hasError.set(false);
  }
}
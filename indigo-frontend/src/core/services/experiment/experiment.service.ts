import { inject, Injectable, signal } from '@angular/core';
import { ApiService } from '@/core/services/api.service';
import { BehaviorSubject, filter, map, of, switchMap } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { LoadingState } from '@core/types/entities/loading-state.i';
import { Template } from '@core/types/entities/template.i';
import { ExperimentModel } from '@core/types/entities/experiments/experiment.i';
import { Mutation } from '@core/types/entities/experiments/mutation.i';
import { ExperimentDetail } from '@core/types/entities/experiments/experiment-detail.i';

@Injectable()
export class ExperimentService {
  private service = inject(ApiService);

  // Signals to hold the current experiment state
  readonly experiment = signal<ExperimentDetail | null>(null);
  readonly isLoading = signal<boolean>(false);
  readonly hasError = signal<boolean>(false);
  private readonly currentId = signal<string | null>(null);

  // TODO using some hand-made LoadingState instead of separate data/loading/error to avoid inconsistent states;
  // if it's more readable to use separate flags or there is a better alternative, i'll rewrite it
  private experimentSubject = new BehaviorSubject<LoadingState<ExperimentDetail>>({
    state: 'empty',
  });
  public experimentLoad$ = this.experimentSubject.asObservable();
  public experiment$ = this.experimentLoad$.pipe(
    filter((x) => x.state === 'ready'),
    map((x) => x.value),
  );
  private template = new BehaviorSubject<LoadingState<Template>>({
    state: 'empty',
  });
  public templateLoad$ = this.template.asObservable();
  public template$ = this.templateLoad$.pipe(
    filter((x) => x.state === 'ready'),
    map((x) => x.value),
  );
  private model = new BehaviorSubject<LoadingState<ExperimentModel>>({
    state: 'empty',
  });
  public modelLoad$ = this.model.asObservable();
  public model$ = this.modelLoad$.pipe(
    filter((x) => x.state === 'ready'),
    map((x) => x.value),
  );
  private mutating = new BehaviorSubject(false);
  public mutating$ = this.mutating.asObservable();
  private picture = new BehaviorSubject<LoadingState<Blob>>({ state: 'empty' });
  public picture$ = this.picture.asObservable();

  // Public API
  setExperiment(experimentDetail: ExperimentDetail | null) {
    this.experiment.set(experimentDetail);
  }
  setLoading(value: boolean) {
    this.isLoading.set(value);
  }
  setError(value: boolean) {
    this.hasError.set(value);
  }

  load(id: string) {
    this.currentId.set(id);
    this.isLoading.set(true);
    this.hasError.set(false);

    this.service
      .request<ExperimentDetail>('get', `experiments/${id}`)
      .subscribe({
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

  loadExperiment(id: string) {
    this.experimentSubject.next({ state: 'loading' });
    this.template.next({ state: 'loading' });
    this.model.next({ state: 'loading' });
    this.picture.next({ state: 'empty' });
    this.service
      .request<ExperimentDetail>('get', `experiments/${id}`)
      .pipe(
        switchMap((experiment) =>
          this.service
            .request<Template>('get', `templates/${experiment.templateId}`)
            .pipe(map((template) => ({ experiment, template }))),
        ),
        switchMap(({ experiment, template }) =>
          this.service
            .request<ExperimentModel>('get', `experiments/${id}/datamodel`)
            .pipe(map((model) => ({ experiment, template, model }))),
        ),
        catchError((err) => {
          console.error('Failed to load experiment:', err);
          this.experimentSubject.next({ state: 'error' });
          this.template.next({ state: 'error' });
          this.model.next({ state: 'error' });
          return of(null);
        }),
      )
      .subscribe((data) => {
        if (data != null) {
          const { experiment, template, model } = data;
          this.experimentSubject.next({ state: 'ready', value: experiment });
          this.template.next({ state: 'ready', value: template });
          this.model.next({ state: 'ready', value: model });
        }
      });
  }

  loadPicture(experimentId: string) {
    this.picture.next({ state: 'loading' });
    this.service
      .request<Blob>('get', `experiments/${experimentId}/picture`, {
        responseType: 'blob',
      })
      .pipe(
        catchError((err) => {
          console.error('Failed to load experiment picture:', err);
          this.picture.next({ state: 'error' });
          return of(null);
        }),
      )
      .subscribe((data) => {
        if (data != null) {
          this.picture.next({ state: 'ready', value: data });
        }
      });
  }

  mutateModel(mutation: Mutation) {
    if (
      this.experimentSubject.value.state !== 'ready' ||
      this.model.value.state !== 'ready'
    ) {
      console.warn('Experiment not loaded');
      return;
    }
    console.log('Mutating model', mutation);
    this.mutating.next(true);
    this.service
      .request<ExperimentModel>(
        'post',
        `experiments/${this.experimentSubject.value.value.id}/datamodel`,
        { model: this.model.value.value, mutation },
      )
      .pipe(
        catchError((err) => {
          console.error('Failed to mutate experiment model:', err);
          alert('Failed to mutate experiment model: ' + err);
          return of(null);
        }),
      )
      .subscribe((newModel) => {
        this.mutating.next(false);
        if (newModel != null) {
          this.model.next({ state: 'ready', value: newModel });
        }
      });
  }
}

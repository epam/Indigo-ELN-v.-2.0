import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, filter, map, of, switchMap } from 'rxjs';
import { Experiment } from '@core/types/entities/experiment.i';
import { ApiService } from '@core/services/api.service';
import { catchError } from 'rxjs/operators';
import { Template } from '@core/types/entities/template.i';
import { LoadingState } from '@core/types/entities/loading-state.i';
import { ExperimentModel } from '@core/types/entities/experiment-model.i';
import { Mutation } from '@core/types/entities/mutation.i';

@Injectable({
  providedIn: 'root',
})
export class ExperimentService {
  private service = inject(ApiService);

  // TODO using some hand-made LoadingState instead of separate data/loading/error to avoid inconsistent states;
  // if it's more readable to use separate flags or there is a better alternative, i'll rewrite it
  private experiment = new BehaviorSubject<LoadingState<Experiment>>({
    state: 'empty',
  });
  public experimentLoad$ = this.experiment.asObservable();
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

  loadExperiment(id: string) {
    this.experiment.next({ state: 'loading' });
    this.template.next({ state: 'loading' });
    this.model.next({ state: 'loading' });
    this.picture.next({ state: 'empty' });
    this.service
      .request<Experiment>('get', `experiments/${id}`)
      .pipe(
        switchMap((experiment) =>
          this.service
            .request<Template>(
              'get',
              `templates/e1a11ab0-43c2-4729-9438-f16dcf742621`,
            )
            .pipe(map((template) => ({ experiment, template }))),
        ),
        switchMap(({ experiment, template }) =>
          this.service
            .request<ExperimentModel>('get', `experiments/${id}/datamodel`)
            .pipe(map((model) => ({ experiment, template, model }))),
        ),
        catchError((err) => {
          console.error('Failed to load experiment:', err);
          this.experiment.next({ state: 'error' });
          this.template.next({ state: 'error' });
          this.model.next({ state: 'error' });
          return of(null);
        }),
      )
      .subscribe((data) => {
        if (data != null) {
          const { experiment, template, model } = data;
          this.experiment.next({ state: 'ready', value: experiment });
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
    if (this.experiment.value.state !== 'ready' || this.model.value.state !== 'ready') {
      console.warn('Experiment not loaded');
      return;
    }
    console.log('Mutating model', mutation);
    this.mutating.next(true);
    this.service
      .request<ExperimentModel>(
        'post',
        `experiments/${this.experiment.value.value.id}/datamodel`,
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

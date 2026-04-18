import { inject, Injectable } from '@angular/core';
import { ApiService } from '@/core/services/api.service';
import { BehaviorSubject, filter, map } from 'rxjs';
import { LoadingState } from '@core/types/entities/loading-state.i';
import { Template } from '@core/types/entities/template.i';
import { ExperimentModel } from '@core/types/entities/experiments/experiment.i';
import { ExperimentDetail } from '@core/types/entities/experiments/experiment-detail.i';

@Injectable({
  providedIn: 'root',
})
export class ExperimentService {
  private service = inject(ApiService);

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
}

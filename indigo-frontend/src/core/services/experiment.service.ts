import { inject, Injectable } from '@angular/core';
import { BehaviorSubject, map, of, switchMap } from 'rxjs';
import { Experiment, ExperimentData } from '@core/types/entities/experiment.i';
import { ApiService } from '@core/services/api.service';
import { catchError } from 'rxjs/operators';
import { Template } from '@core/types/entities/template.i';
import {LoadingState} from '@core/types/entities/loading-state.i';

@Injectable({
  providedIn: 'root',
})
export class ExperimentService {
  private service = inject(ApiService);

  // TODO using some hand-made LoadingState instead of separate data/loading/error to avoid inconsistent states;
  // if it's more readable to use separate flags or there is a better alternative, i'll change it back
  private data = new BehaviorSubject<LoadingState<ExperimentData>>({state: 'empty'});
  public data$ = this.data.asObservable();

  private picture = new BehaviorSubject<LoadingState<Blob>>({state: 'empty'});
  public picture$ = this.picture.asObservable();

  loadExperiment(id: string) {
    this.data.next({ state: 'loading' });
    this.service
      .request<Experiment>('get', `experiments/${id}`)
      .pipe(
        switchMap((experiment) =>
          this.service
            .request<Template>(
              'get',
              `templates/e1a11ab0-43c2-4729-9438-f16dcf742621`,
            )
            .pipe(
              map((template) => ({ experiment, template }) as ExperimentData),
            ),
        ),
        catchError((err) => {
          console.error('Failed to load experiment:', err);
          this.data.next({ state: 'error' });
          return of(null);
        }),
      )
      .subscribe((data) => {
        if (data != null) {
          this.data.next({ state: 'ready', value: data } );
        }
      });
  }

  loadPicture(experimentId: string) {
    this.picture.next({ state: 'loading' });
    this.service
      .request<Blob>('get', `experiments/${experimentId}/picture`, { responseType: 'blob' })
      .pipe(
        catchError((err) => {
          console.error('Failed to load experiment picture:', err);
          this.picture.next({ state: 'error' });
          return of(null);
        }),
      )
      .subscribe((data) => {
        if (data != null) {
          this.picture.next({ state: 'ready', value: data } );
        }
      });
  }
}

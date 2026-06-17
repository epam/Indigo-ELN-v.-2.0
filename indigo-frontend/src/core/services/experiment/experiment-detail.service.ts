import { computed, inject, Injectable, signal } from '@angular/core';
import { ApiService } from '@/core/services/api.service';
import { ExperimentDetail, ExperimentEditRequest } from '@core/types/entities/experiments/experiment-detail.i';
import { Mutation, MutationResponse, ReactionAnchor } from '@core/types/entities/experiments/mutation.i';
import { finalize, Observable, Subject, tap } from 'rxjs';
import { NotificationService } from '@core/services/notification/notification.service';
import { NotificationType } from '@core/types/notification.i';
import { Reaction } from '@core/types/entities/experiments/experiment.i';
import { JSON_PATCHER } from '@core/utils/json-patcher';
import { Template } from '@core/types/entities/template.i';
import { switchMap } from 'rxjs/operators';
import { EnteredValue } from '@core/types/entities/values.i';

@Injectable({
  providedIn: 'root',
})
export class ExperimentDetailService {
  private service = inject(ApiService);
  private notificationService = inject(NotificationService);

  // Signals for experiment detail state
  readonly experimentDetail = signal<ExperimentDetail | null>(null);
  readonly experimentModel = computed(() => this.experimentDetail()?.model);
  readonly experimentTemplate = signal<Template | null>(null);
  readonly lastLoadedDetail = signal<ExperimentDetail | null>(null);
  readonly isLoading = signal<boolean>(false);
  readonly hasError = signal<boolean>(false);
  readonly isUpdating = signal<boolean>(false);
  readonly currentId = signal<string | null>(null);
  readonly updatedNodes = signal<Map<unknown, unknown>>(new Map());
  readonly updatedReactionImages = signal<Map<ReactionAnchor, string>>(new Map());

  readonly markedChanged$ = new Subject<void>();

  // Query methods
  load(id: string) {
    if (this.currentId() === id) {
      return;
    }
    this.currentId.set(id);
    this.isLoading.set(true);
    this.hasError.set(false);
    this.experimentDetail.set(null);
    this.experimentTemplate.set(null);

    this.service
      .request<ExperimentDetail>('get', `experiments/${id}`)
      .pipe(
        finalize(() => this.isLoading.set(false)),
        switchMap((exp) => {
          this.experimentDetail.set(exp);
          this.lastLoadedDetail.set(structuredClone(exp));
          return this.service.request<Template>('get', `/api/eln/templates/${exp.templateId}`);
        }),
      )
      .subscribe({
        next: (template) => {
          this.experimentTemplate.set(template);
        },
        error: () => this.hasError.set(true),
      });
  }

  // Update methods
  updateDataModel(mutation: Mutation): Observable<MutationResponse> {
    return this.updateDataModel2(
      this.service.request<MutationResponse>(
        'post',
        `experiments/${this.currentId()}/mutate?revision=${this.experimentDetail().revision}`,
        mutation,
      ),
    );
  }

  updateDataModel2(operation: Observable<MutationResponse>): Observable<MutationResponse> {
    this.isUpdating.set(true);

    return operation.pipe(
      tap({
        next: (response) => {
          const previous = this.experimentDetail();
          const [updated, updatedNodes] = JSON_PATCHER.apply(previous, response.patch);
          this.experimentDetail.set(updated as ExperimentDetail);
          this.lastLoadedDetail.set(structuredClone(updated) as ExperimentDetail);
          this.updatedNodes.set(updatedNodes);
          if (response.reactionImages) {
            this.updatedReactionImages.update((map) => {
              const map1 = new Map(map.entries());
              for (const [anchor, image] of Object.entries(response.reactionImages)) {
                map1.set(anchor, image);
              }
              return map1;
            });
          }
          this.isUpdating.set(false);
          if (response.messages) {
            for (const message of response.messages) {
              this.notificationService.notify({
                type: NotificationType.Info,
                isInline: true,
                message: message,
              });
            }
          }
        },
        error: () => {
          this.isUpdating.set(false);
          this.experimentDetail.set(this.lastLoadedDetail()); // revert to last known server state
        },
      }),
    );
  }

  dataModelUpdated(updater: (model: ExperimentDetail) => ExperimentDetail) {
    this.experimentDetail.update(updater);
    this.lastLoadedDetail.update(updater);
  }

  editExperiment(patch: ExperimentEditRequest): Observable<ExperimentDetail> {
    const id = this.currentId();
    return this.service.request<ExperimentDetail>('patch', `experiments/${id}`, patch).pipe(
      tap((updated) => {
        this.experimentDetail.set(updated);
        this.lastLoadedDetail.set(structuredClone(updated));
      }),
    );
  }

  executeWorkflow(operation: string, params?: Record<string, string>): Observable<ExperimentDetail> {
    this.isLoading.set(true);
    this.hasError.set(false);
    const query = params ? '?' + new URLSearchParams(params).toString() : '';
    return this.service.request('post', `experiments/${this.currentId()}/workflow/${operation}${query}`).pipe(
      finalize(() => this.isLoading.set(false)),
      tap({
        next: (response: ExperimentDetail) => {
          this.experimentDetail.set(response);
          this.lastLoadedDetail.set(structuredClone(response));
        },
        error: () => this.hasError.set(true),
      }),
    );
  }

  // Utility methods
  mark(id: string): Observable<boolean> {
    return this.service.request<boolean>('post', `experiments/${id}/mark`).pipe(tap(() => this.markedChanged$.next()));
  }

  unmark(id: string): Observable<boolean> {
    return this.service
      .request<boolean>('post', `experiments/${id}/unmark`)
      .pipe(tap(() => this.markedChanged$.next()));
  }

  refresh() {
    const id = this.currentId();
    if (id) this.load(id);
  }

  reset() {
    this.currentId.set(null);
    this.experimentDetail.set(null);
    this.lastLoadedDetail.set(null);
    this.experimentTemplate.set(null);
    this.isLoading.set(false);
    this.isUpdating.set(false);
    this.hasError.set(false);
  }

  getReaction(anchor: ReactionAnchor): Reaction | null {
    for (const reaction of this.experimentModel().reactions) {
      if (reaction.anchor === anchor) {
        return reaction;
      }
    }
    return null;
  }

  determineCellClasses(value: EnteredValue<unknown> | null): string[] {
    if (value != null) {
      const hasAnyUpdates = this.updatedNodes().size !== 0; // to prevent animation on initial load
      const previous = this.updatedNodes().get(value) as EnteredValue<unknown> | null;
      if (isUserEntered(value)) {
        return ['value-state-set-manually'];
      }
      const classes = [];
      if (value.source === 'default') {
        classes.push('value-state-default');
      } else if (value.source === 'fixed') {
        classes.push('value-state-fixed');
      }
      if (hasAnyUpdates && previous != null && isUserEntered(previous) && !isUserEntered(value)) {
        // overwritten
        classes.push('animate-[flash-red_500ms_ease-in-out]');
      } else if (
        hasAnyUpdates &&
        value.source === 'calculated' &&
        (value.source !== previous?.source || value.value !== previous?.value)
      ) {
        // recalculated
        classes.push('animate-[flash-green_500ms_ease-in-out]');
      }
      return classes;
    }
    return [];
  }
}

function isUserEntered(value: EnteredValue<unknown>): boolean {
  return typeof value.source === 'number' && value.source > 0;
}

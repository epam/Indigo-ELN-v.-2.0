import { computed, inject, Injectable, signal } from '@angular/core';
import { ApiService } from '@/core/services/api.service';
import { ExperimentDetail, ExperimentEditRequest } from '@core/types/entities/experiments/experiment-detail.i';
import { Mutation, MutationResponse, ReactionAnchor } from '@core/types/entities/experiments/mutation.i';
import { finalize, Observable, tap } from 'rxjs';
import { NotificationService } from '@core/services/notification/notification.service';
import { NotificationType } from '@core/types/notification.i';
import { Reaction } from '@core/types/entities/experiments/experiment.i';
import { JSON_PATCHER } from '@core/utils/json-patcher';

@Injectable({
  providedIn: 'root',
})
export class ExperimentDetailService {
  private service = inject(ApiService);
  private notificationService = inject(NotificationService);

  // Signals for experiment detail state
  readonly experimentDetail = signal<ExperimentDetail | null>(null);
  readonly experimentModel = computed(() => this.experimentDetail()?.model);
  readonly lastLoadedDetail = signal<ExperimentDetail | null>(null);
  readonly isLoading = signal<boolean>(false);
  readonly hasError = signal<boolean>(false);
  readonly isUpdating = signal<boolean>(false);
  readonly currentId = signal<string | null>(null);
  readonly updatedNodes = signal<Map<unknown, unknown>>(new Map());
  readonly updatedReactionImages = signal<Map<ReactionAnchor, string>>(new Map());

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
  updateDataModel(mutation: Mutation): Observable<MutationResponse> {
    return this.updateDataModel2(
      this.service.request<MutationResponse>(
        'post',
        `experiments/${this.currentId()}/mutate4?revision=${this.experimentDetail().revision}`,
        mutation,
      ),
    );
  }

  updateDataModel2(operation: Observable<MutationResponse>): Observable<MutationResponse> {
    const id = this.currentId();

    if (!id) {
      console.warn('No experiment ID in context');
      return new Observable((observer) => {
        observer.error(new Error('No experiment ID available'));
      });
    }

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
  refresh() {
    const id = this.currentId();
    if (id) this.load(id);
  }

  reset() {
    this.currentId.set(null);
    this.experimentDetail.set(null);
    this.lastLoadedDetail.set(null);
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
}

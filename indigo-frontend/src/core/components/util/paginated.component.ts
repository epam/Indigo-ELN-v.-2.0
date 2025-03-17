import { ApiService } from '@/core/services/api.service';
import { PagedRequest } from '@/core/types/request/paged-request.i';
import { PaginatedResponse } from '@/core/types/response/paginated-response.i';
import { Component, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  BehaviorSubject,
  defer,
  finalize,
  Observable,
  of,
  switchMap,
  take,
  tap,
} from 'rxjs';

interface PaginatedConfig {
  controller: string;
  enableQueryParams?: boolean;
}
@Component({
  template: '',
})
export abstract class PaginatedComponent<T> {
  protected config: PaginatedConfig = {
    controller: 'unknown',
    enableQueryParams: true,
  };
  protected activatedRoute: ActivatedRoute;
  protected service: ApiService<T>;
  protected router: Router;
  protected total = 0;
  private filters: Record<string, unknown> = {};
  protected isLoading = false;
  private pager: PagedRequest = {
    pageSize: 10,
    pageNo: 1,
  };

  protected dataList$: Observable<PaginatedResponse<T>>;

  private dataSubject$ = new BehaviorSubject<PaginatedResponse<T>>(null);
  constructor() {
    this.activatedRoute = inject(ActivatedRoute);
    this.service = inject(ApiService);
    this.router = inject(Router);
  }

  protected setup(config: PaginatedConfig) {
    this.config = { ...this.config, ...config };
    this.initialize();
  }

  private initialize() {
    this.service.setup(this.config.controller);

    // Initiate rxjs logic
    const dataLogic$ = this.dataSubject$.pipe(
      switchMap((res) => {
        return res
          ? of(res)
          : defer(() => {
              this.isLoading = true;
              return this.service.getPaged(this.pager, this.filters).pipe(
                tap({
                  next: (res) => {
                    this.total = res.totalItems;
                    if (res && res.items.length == 0 && this.pager.pageNo > 0) {
                      this.pager.pageNo = res.lastIndex;
                      this.fetchDataAndUpdateQueryParams(false);

                      this.dataSubject$.next(null);
                    } else {
                      this.dataSubject$.next(res);
                    }
                  },
                }),

                finalize(() => {
                  this.isLoading = false;
                }),
              );
            });
      }),
    );

    this.dataList$ = this.config.enableQueryParams
      ? this.activatedRoute.queryParams.pipe(
          take(1),
          switchMap((params) => {
            if (this.config.enableQueryParams) {
              const queryFilters = Object.keys(
                params as Record<string, unknown>,
              ).reduce((acc: Record<string, unknown>, curr) => {
                acc[curr] = params[curr];
                return acc;
              }, {});

              Object.assign(this.filters, queryFilters);
              this.fetchDataAndUpdateQueryParams(false);
            }

            return dataLogic$;
          }),
        )
      : dataLogic$;
  }

  protected fetchDataAndUpdateQueryParams(fetch = true) {
    if (!fetch && !this.config.enableQueryParams) {
      return;
    }

    const params = {
      ...this.pager,
      ...this.filters,
    };

    // Get the current URL tree
    const currentUrlTree = this.router.createUrlTree(
      [],
      this.router.parseUrl(this.router.url),
    );

    Object.keys(params).forEach((key) => {
      if (params[key]) {
        currentUrlTree.queryParams[key] = params[key];
      }
    });

    // Navigate to the updated URL
    if (this.config.enableQueryParams) {
      this.router
        .navigate([], {
          queryParams: params,
          replaceUrl: true,
        })
        .then(() => {
          if (fetch) {
            this.dataSubject$.next(null);
          }
        });
      return;
    }

    this.dataSubject$.next(null);
  }
}

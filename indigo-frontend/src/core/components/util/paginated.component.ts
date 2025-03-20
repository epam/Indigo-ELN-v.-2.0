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
import { PaginatedConfig } from './paginated.i';

@Component({
  template: '',
})
export abstract class PaginatedComponent<T> {
  protected firstLoad = true;
  protected config: PaginatedConfig = {
    controller: 'unknown',
    enableQueryParams: true,
    enableScrollRestoration: false,
  };
  protected activatedRoute: ActivatedRoute;
  protected service: ApiService<T>;
  protected router: Router;
  protected total = 0;
  protected filters: Record<string, unknown> = {};
  protected isLoading = false;
  protected pager: PagedRequest = {
    pageSize: 10,
    pageNo: 0,
  };

  protected dataList$: Observable<PaginatedResponse<T>>;
  protected dataSubject$ = new BehaviorSubject<PaginatedResponse<T>>(null);
  constructor() {
    this.activatedRoute = inject(ActivatedRoute);
    this.service = inject(ApiService);
    this.router = inject(Router);
  }

  protected setup(config: PaginatedConfig) {
    this.config = { ...this.config, ...config };
    this.initialize();
  }

  protected initialize() {
    this.service.setup(this.config.controller);

    // Initiate rxjs logic
    const dataLogic$ = this.dataSubject$.pipe(
      switchMap((res) => {
        return res
          ? of(res)
          : defer(() => {
              this.isLoading = true;

              const computedPager =
                this.config.enableScrollRestoration && this.firstLoad
                  ? // On first load with restoration enabled, fetch all data up to current page
                    {
                      pageNo: 0,
                      // + 1 since pageNo 0 = Page 1
                      pageSize: (this.pager.pageNo + 1) * this.pager.pageSize,
                    }
                  : // For subsequent loads or restoration disabled, use standard pager
                    this.pager;

              return this.service.getPaged(computedPager, this.filters).pipe(
                tap({
                  next: (res) => {
                    this.firstLoad = false;
                    this.total = res.totalItems;
                    if (res && res.items.length == 0 && this.pager.pageNo > 0) {
                      this.pager.pageNo = res.totalPages;
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
            const queryFilters = Object.keys(
              params as Record<string, unknown>,
            ).reduce((acc: Record<string, unknown>, curr) => {
              acc[curr] = params[curr];
              return acc;
            }, {});

            Object.keys(queryFilters).forEach((key) => {
              if (key in this.pager) {
                this.pager[key] = Number(queryFilters[key]);
                delete queryFilters[key];
              }
            });

            Object.assign(this.filters, queryFilters);

            this.fetchDataAndUpdateQueryParams(false);

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

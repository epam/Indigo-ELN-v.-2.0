import { ApiService } from '@/core/services/api.service';
import { PagedRequest, SortOption } from '@/core/types/request/paged-request.i';
import { PaginatedResponse } from '@/core/types/response/paginated-response.i';
import { inject } from '@angular/core';
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

export abstract class PaginatedBase<T> {
  protected firstLoad = true;
  protected config: PaginatedConfig = {
    loadUrl: 'unknown',
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

  protected currentSort: {
    sortBy: string;
    sort: 'EARLIEST' | 'LATEST';
  } | null = null;
  protected sortOptions: SortOption[] = [];

  protected dataList$: Observable<PaginatedResponse<T>>;
  protected dataSubject$ = new BehaviorSubject<PaginatedResponse<T>>(null);

  constructor() {
    this.activatedRoute = inject(ActivatedRoute);
    this.service = inject(ApiService);
    this.router = inject(Router);
  }

  protected setup(config: PaginatedConfig) {
    this.config = { ...this.config, ...config };

    if (config.sortOptions) {
      this.sortOptions = config.sortOptions;
    }

    if (config.defaultSort) {
      this.currentSort = config.defaultSort;
      this.pager.sortBy = config.defaultSort.sortBy;
      this.pager.sort = config.defaultSort.sort;
    }

    this.initialize();
  }

  protected initialize() {
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
                      sortBy: this.pager.sortBy,
                      sort: this.pager.sort,
                    }
                  : // For subsequent loads or restoration disabled, use standard pager
                    this.pager;

              return this.service
                .getPaged(this.config.loadUrl, computedPager, this.filters)
                .pipe(
                  tap({
                    next: (res) => {
                      this.firstLoad = false;
                      this.total = res.totalItems;
                      if (
                        res &&
                        res.items.length == 0 &&
                        this.pager.pageNo > 0
                      ) {
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
                // Handle special cases for pager properties
                if (key === 'sortBy') {
                  this.pager[key] = queryFilters[key] as string;
                } else if (key === 'sort') {
                  this.pager[key] = queryFilters[key] as 'EARLIEST' | 'LATEST';
                } else {
                  this.pager[key] = Number(queryFilters[key]);
                }
                delete queryFilters[key];
              }
            });

            // Handle sorting from query params
            if (params['sortBy']) {
              this.currentSort = {
                sortBy: params['sortBy'] as string,
                sort: (params['sort'] as 'EARLIEST' | 'LATEST') || 'EARLIEST',
              };
            }

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

  protected search(value: string) {
    this.filters['search'] = value;
    this.fetchDataAndUpdateQueryParams();
  }

  public sort(sortBy: string, sort?: 'EARLIEST' | 'LATEST') {
    // If no sort provided, determine it based on current sort
    if (!sort) {
      if (this.currentSort?.sortBy === sortBy) {
        // Toggle sort order if same field
        sort = this.currentSort.sort === 'EARLIEST' ? 'LATEST' : 'EARLIEST';
      } else {
        // Use default order for new field or 'EARLIEST' as fallback
        const option = this.sortOptions.find((opt) => opt.value === sortBy);
        sort = option?.defaultOrder || 'EARLIEST';
      }
    }

    this.currentSort = { sortBy, sort: sort };
    this.pager.sortBy = sortBy;
    this.pager.sort = sort;
    this.pager.pageNo = 0; // Reset to first page when sorting

    this.fetchDataAndUpdateQueryParams();
  }

  public clearSort() {
    this.currentSort = null;
    delete this.pager.sortBy;
    delete this.pager.sort;
    this.pager.pageNo = 0;

    this.fetchDataAndUpdateQueryParams();
  }

  public getCurrentSort() {
    return this.currentSort;
  }

  public getSortOptions() {
    return this.sortOptions;
  }

  public isSortedBy(sortBy: string): boolean {
    return this.currentSort?.sortBy === sortBy;
  }

  public getSortOrder(sortBy: string): 'EARLIEST' | 'LATEST' | null {
    return this.isSortedBy(sortBy) ? this.currentSort!.sort : null;
  }
}

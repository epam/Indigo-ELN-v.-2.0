import { ApiService } from '@/core/services/api.service';
import { FilterOption, PagedRequest, SortOption } from '@/core/types/request/paged-request.i';
import { PaginatedResponse } from '@/core/types/response/paginated-response.i';
import { inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, defer, finalize, Observable, of, switchMap, tap } from 'rxjs';
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
    sort: 'EARLIEST' | 'LATEST';
  } | null = null;
  protected sortOptions: SortOption[] = [];
  protected filterOptions: FilterOption[] = [];

  protected dataList$: Observable<PaginatedResponse<T>>;
  protected dataSubject$ = new BehaviorSubject<PaginatedResponse<T>>(null);
  private previousParams: any = null;

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

    if (config.filterOptions) {
      this.filterOptions = config.filterOptions;
    }

    if (config.defaultSort) {
      this.currentSort = config.defaultSort;
      this.pager.sort = config.defaultSort.sort;
    }

    this.reinitialize();
  }

  protected reinitialize() {
    this.previousParams = null;

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
                      sort: this.pager.sort,
                    }
                  : // For subsequent loads or restoration disabled, use standard pager
                    this.pager;

              return this.service.getPaged(this.config.loadUrl, computedPager, this.filters).pipe(
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
          switchMap((params) => {
            const isExternalParamsChange =
              this.previousParams !== null && JSON.stringify(params) !== JSON.stringify(this.previousParams);
            const previousParams = this.previousParams;
            this.previousParams = { ...params };

            this.pager = {
              pageNo: Number(params['pageNo'] ?? 0),
              pageSize: Number(params['pageSize'] ?? 10),
            };
            if (params['sort'] === 'EARLIEST' || params['sort'] === 'LATEST') {
              this.pager.sort = params['sort'];
              this.currentSort = { sort: params['sort'] };
            } else {
              this.pager.sort = this.config.defaultSort?.sort;
              this.currentSort = this.config.defaultSort ? { ...this.config.defaultSort } : null;
            }

            this.filters = Object.keys(params).reduce((acc: Record<string, unknown>, key) => {
              if (!['pageNo', 'pageSize', 'sort'].includes(key)) {
                acc[key] = params[key];
              }
              return acc;
            }, {});

            const isPageOnlyChange =
              isExternalParamsChange &&
              previousParams &&
              Object.keys(params).every(
                (key) => ['pageNo', 'pageSize'].includes(key) || params[key] === previousParams[key],
              ) &&
              Object.keys(previousParams).every(
                (key) => ['pageNo', 'pageSize'].includes(key) || params[key] === previousParams[key],
              );

            if (isExternalParamsChange) {
              if (!isPageOnlyChange) {
                this.onQueryParamsChange();
              }
              this.dataSubject$.next(null);
            }

            return dataLogic$;
          }),
        )
      : dataLogic$;
  }

  protected onQueryParamsChange(): void {}

  protected fetchDataAndUpdateQueryParams(fetch = true) {
    if (!fetch && !this.config.enableQueryParams) {
      return;
    }

    const params = {
      ...this.pager,
      ...this.filters,
    };

    // Get the current URL tree
    const currentUrlTree = this.router.createUrlTree([], this.router.parseUrl(this.router.url));

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

  protected setBooleanFilter(key: string, value: boolean): void {
    if (value) {
      this.filters[key] = true;
    } else {
      delete this.filters[key];
    }
  }

  public sort(sort: 'EARLIEST' | 'LATEST') {
    this.currentSort = { sort };
    this.pager.sort = sort;
    this.pager.pageNo = 0; // Reset to first page when sorting

    this.fetchDataAndUpdateQueryParams();
  }

  public clearSort() {
    this.currentSort = null;
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

  public getFilterOptions() {
    const selectedValues = Array.isArray(this.filters['status'])
      ? (this.filters['status'] as unknown[])
      : this.filters['status']
        ? [this.filters['status']]
        : [];

    return this.filterOptions.map((option) => ({
      ...option,
      checked: selectedValues.includes(option.value),
    }));
  }

  public getSearchValue(): string {
    return (this.filters['search'] as string) || '';
  }

  public getBooleanFilterValue(key: string): boolean {
    return this.filters[key] === true || this.filters[key] === 'true';
  }
}

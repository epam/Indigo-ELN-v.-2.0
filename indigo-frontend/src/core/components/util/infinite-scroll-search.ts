// TODO try to unify with InfiniteScrollBase, but the latter seems to be tightly bound to router navigation
import { ApiService } from '@core/services/api.service';
import {
  FindSamplesRequest,
  GlobalSearchRequest,
  GlobalSearchResult,
  Sample,
  SampleSearchResult,
} from '@core/types/entities/experiments/search.i';
import { PaginatedResponse, PaginatedResponseBase } from '@core/types/response/paginated-response.i';
import { BehaviorSubject, Observable } from 'rxjs';

export abstract class InfiniteSearchLoader<R, T, P extends PaginatedResponseBase<T>> {
  private searchParams: R | null = null;
  private currentPage: P | null = null;
  private dataSubject$ = new BehaviorSubject<T[] | null>(null);
  data$ = this.dataSubject$ as Observable<T[] | null>;
  loading = false;
  started = false;
  completed = false;
  error = false;
  totalItems: number | null = null;
  totalItemsStr: string | null = null;
  private isInfiniteLoaderVisible = false;

  protected abstract doFetch(searchParams: R, currentPage: P | null): Observable<P>;

  protected abstract hasNext(currentPage: P): boolean;

  search(searchParams: R): void {
    this.searchParams = searchParams;
    this.currentPage = null;
    this.started = true;
    this.completed = false;
    this.error = false;
    this.totalItems = null;
    this.totalItemsStr = null;
    this.dataSubject$.next(null);
    this.fetchNext();
  }

  fetchNext(): void {
    if (this.searchParams == null || this.completed || this.loading) {
      return;
    }
    this.loading = true;
    this.doFetch(this.searchParams, this.currentPage).subscribe({
      next: (response) => {
        this.currentPage = response;
        if (!this.hasNext(response)) {
          this.completed = true;
        }
        const current = this.dataSubject$.value || [];
        const allItems = [...current, ...response.items];
        this.dataSubject$.next(allItems);
        this.loading = false;
        this.totalItems = response.totalItems;
        this.totalItemsStr =
          response.totalItems != null
            ? response.totalItems.toString()
            : this.completed
              ? allItems.length.toString()
              : `${allItems.length}+`;
        if (this.isInfiniteLoaderVisible) {
          this.fetchNext();
        }
      },
      error: () => {
        this.error = true;
        this.completed = true;
        this.loading = false;
      },
    });
  }

  replace(predicate: (item: T) => boolean, newItem: T): boolean {
    let list = this.dataSubject$.value;
    if (list) {
      const index = list.findIndex(predicate);
      if (index !== -1) {
        list = [...list];
        list[index] = newItem;
        this.dataSubject$.next(list);
        return true;
      }
    }
    return false;
  }

  onInfiniteLoaderLeft(): void {
    this.isInfiniteLoaderVisible = false;
  }

  onInfiniteLoaderEntered(): void {
    this.isInfiniteLoaderVisible = true;
  }
}

export class SamplesSearchLoader extends InfiniteSearchLoader<FindSamplesRequest, Sample, SampleSearchResult> {
  private readonly service: ApiService<unknown>;

  constructor(service: ApiService<unknown>) {
    super();
    this.service = service;
  }

  protected doFetch(
    searchParams: FindSamplesRequest,
    currentPage: SampleSearchResult | null,
  ): Observable<SampleSearchResult> {
    let url = 'samples/search?limit=100';
    if (currentPage?.nextCatalog) {
      url += `&nextCatalog=${currentPage.nextCatalog}`;
    }
    if (currentPage?.nextAfter) {
      url += `&nextAfter=${currentPage.nextAfter}`;
    }
    return this.service.request('post', url, searchParams);
  }

  protected hasNext(currentPage: SampleSearchResult): boolean {
    return currentPage.hasNext;
  }
}

export class GlobalSearchLoader extends InfiniteSearchLoader<
  GlobalSearchRequest,
  GlobalSearchResult,
  PaginatedResponse<GlobalSearchResult>
> {
  private readonly service: ApiService<unknown>;

  constructor(service: ApiService<unknown>) {
    super();
    this.service = service;
  }

  protected override doFetch(
    searchParams: GlobalSearchRequest,
    currentPage: PaginatedResponse<GlobalSearchResult> | null,
  ): Observable<PaginatedResponse<GlobalSearchResult>> {
    const pageNo = currentPage ? currentPage.pageNo + 1 : 0;
    return this.service.request('post', `search?pageNo=${pageNo}&pageSize=20`, searchParams);
  }

  protected hasNext(currentPage: PaginatedResponse<GlobalSearchResult>): boolean {
    return currentPage.items.length !== 0 && currentPage.pageNo + 1 < currentPage.totalPages;
  }
}

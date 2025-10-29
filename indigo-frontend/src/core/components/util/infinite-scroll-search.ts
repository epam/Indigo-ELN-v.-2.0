// TODO try to unify with InfiniteScrollBase, but the latter seems to be tightly bound to router navigation
import { BehaviorSubject, Observable } from 'rxjs';
import { PaginatedResponse } from '@core/types/response/paginated-response.i';

export class InfiniteSearchLoader<R, T> {
  private fetcher: (
    searchParams: R,
    pageNo: number,
  ) => Observable<PaginatedResponse<T>>;
  private searchParams: R | null = null;
  private pageNo = -1;
  private dataSubject$ = new BehaviorSubject(null);
  data$ = this.dataSubject$ as Observable<T[] | null>;
  loading = false;
  started = false;
  completed = false;
  error = false;
  totalItems: number | null = null;

  constructor(
    fetcher: (
      searchParams: R,
      pageNo: number,
    ) => Observable<PaginatedResponse<T>>,
  ) {
    this.fetcher = fetcher;
  }

  search(searchParams: R): void {
    this.searchParams = searchParams;
    this.pageNo = -1;
    this.started = true;
    this.completed = false;
    this.error = false;
    this.totalItems = null;
    this.dataSubject$.next(null);
    this.fetchNextPage();
  }

  fetchNextPage(): void {
    if (this.searchParams == null || this.completed || this.loading) {
      return;
    }
    this.pageNo++;
    this.loading = true;
    this.fetcher(this.searchParams, this.pageNo).subscribe({
      next: (response) => {
        if (
          response.items.length === 0 ||
          this.pageNo + 1 >= response.totalPages
        ) {
          this.completed = true;
        }
        const current = this.dataSubject$.value || [];
        this.dataSubject$.next([...current, ...response.items]);
        this.loading = false;
        this.totalItems = response.totalItems;
      },
      error: (error) => {
        console.error('Failed to fetch search results: ', error);
        this.error = true;
        this.completed = true;
        this.loading = false;
      },
    });
  }
}

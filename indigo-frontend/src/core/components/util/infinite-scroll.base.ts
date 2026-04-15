import { ensureDistinct } from '@/core/utils/array.util';
import { BehaviorSubject, Observable } from 'rxjs';
import { PaginatedBase } from './paginated.base';

export abstract class InfiniteScrollBase<T> extends PaginatedBase<T> {
  dataBh = new BehaviorSubject<T[]>([]);
  data$: Observable<T[]>;
  appendToTop = false;
  protected override isLoading = false;

  private isInfiniteLoaderVisible = false;

  protected override initialize(): void {
    super.initialize();
    this.config.enableScrollRestoration = true;

    // 2. Clearer data management: avoid side effects inside switchMap
    this.dataList$.subscribe((data) => {
      const currValue = this.dataBh.value;
      const result = this.appendToTop
        ? [...data.items, ...currValue]
        : [...currValue, ...data.items];

      this.dataBh.next(ensureDistinct(result, 'id'));
      this.isLoading = false;

       if (this.isInfiniteLoaderVisible) {
        this.infiniteLoad();
      }
    });

    this.data$ = this.dataBh.asObservable();
  }

   onInfiniteLoaderEntered(): void {
    this.isInfiniteLoaderVisible = true;
  }

  onInfiniteLoaderLeft(): void {
    this.isInfiniteLoaderVisible = false;
  }

  infiniteLoad() {
    if (this.isLoading) return;
    

    const nextBackendPage = this.pager.pageNo + 1;
    const totalPages = Math.ceil(this.total / this.pager.pageSize);

    if (nextBackendPage < totalPages) {
      this.isLoading = true;
      this.pager.pageNo = nextBackendPage;
      this.fetchDataAndUpdateQueryParams(true);
    }
  }

  private resetListState() {
    this.isLoading = true;
    this.appendToTop = false;
    this.pager.pageNo = 0;
    this.dataBh.next([]);
  }

  override search(value: string) {
    this.resetListState();
    super.search(value);
  }

  override sort(sortBy: string, sort?: 'EARLIEST' | 'LATEST') {
    this.resetListState();
    super.sort(sortBy, sort);
  }

  override clearSort() {
    this.pager.pageNo = 0;
    this.dataBh.next([]);
    super.clearSort();
  }

  reload() {
    this.resetListState();
    this.firstLoad = true;
    this.dataSubject$.next(null);
  }
}

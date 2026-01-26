import { ensureDistinct } from '@/core/utils/array.util';
import { BehaviorSubject, Observable } from 'rxjs';
import { PaginatedBase } from './paginated.base';

export abstract class InfiniteScrollBase<T> extends PaginatedBase<T> {
  dataBh = new BehaviorSubject<T[]>([]);
  data$: Observable<T[]>;
  appendToTop = false;
  protected override isLoading = false;

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
    });

    this.data$ = this.dataBh.asObservable();
  }

  infiniteLoad() {
    if (this.isLoading) return;
    // Since backend uses zero-based indexing (pageNo=0 equals page 1),
    const currentBackendPage = this.pager.pageNo;
    const nextBackendPage = currentBackendPage + 1;

    const totalPages = Math.ceil(this.total / this.pager.pageSize);

    if (nextBackendPage < totalPages) {
      this.isLoading = true;
      this.pager.pageNo = nextBackendPage;
      this.fetchDataAndUpdateQueryParams(true);
    }
  }
  private resetListState() {
    this.isLoading = true;
    this.appendToTop = false; // Reset the reverse order bug
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

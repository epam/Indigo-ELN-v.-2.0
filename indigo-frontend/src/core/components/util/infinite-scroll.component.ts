import { ensureDistinct } from '@/core/utils/array.util';
import { BehaviorSubject, Observable, switchMap } from 'rxjs';
import { PaginatedComponent } from './paginated.component';

export abstract class InfiniteScrollComponent<T> extends PaginatedComponent<T> {
  dataBh = new BehaviorSubject<T[]>([]);
  data$: Observable<T[]>;
  search = '';

  protected override initialize(): void {
    super.initialize();
    this.config.enableScrollRestoration = true;
    this.data$ = this.dataList$.pipe(
      switchMap((data) => {
        const currValue = this.dataBh.value;
        const result = [...currValue, ...data.items];

        this.dataBh.next(ensureDistinct(result, 'id'));

        return this.dataBh.asObservable();
      }),
    );
  }

  infiniteLoad() {
    // Since backend uses zero-based indexing (pageNo=0 equals page 1),
    const currentBackendPage = this.pager.pageNo;
    const nextBackendPage = currentBackendPage + 1;

    const totalPages = Math.ceil(this.total / this.pager.pageSize);

    if (nextBackendPage < totalPages) {
      this.pager.pageNo = nextBackendPage;
      this.fetchDataAndUpdateQueryParams(true);
    }
  }
}

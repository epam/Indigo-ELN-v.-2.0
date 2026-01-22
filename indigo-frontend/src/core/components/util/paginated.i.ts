import { SortOption } from '@/core/types/request/paged-request.i';

export interface PaginatedConfig {
  loadUrl: string;
  enableQueryParams?: boolean;
  enableScrollRestoration?: boolean;
  sortOptions?: SortOption[];
  defaultSort?: {
    sortBy: string;
    sort: 'EARLIEST' | 'LATEST';
  };
}

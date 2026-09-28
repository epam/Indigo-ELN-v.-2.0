export interface PagedRequest {
  pageNo: number;
  pageSize: number;
  sort?: 'EARLIEST' | 'LATEST';
}

export interface SortOption {
  label: string;
  value: string;
  defaultOrder?: 'EARLIEST' | 'LATEST';
}

export interface FilterOption {
  label: string;
  value: string;
  checked: boolean;
}

export interface PaginatedResponseBase<T> {
  totalItems?: number;
  items: T[];
}

export interface PaginatedResponse<T> extends PaginatedResponseBase<T> {
  pageNo: number;
  pageSize: number;
  totalItems: number;
  items: T[];
  firstIndex: number;
  lastIndex: number;
  totalPages: number;
}

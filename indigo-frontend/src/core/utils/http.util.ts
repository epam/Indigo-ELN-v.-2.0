import { HttpParams } from '@angular/common/http';
import { PagedRequest } from '../types/request/paged-request.i';

export function getRequestParams(
  filter: Record<string, unknown>,
  paging?: PagedRequest,
) {
  let params = new HttpParams();

  const filters = { ...filter, ...paging };

  Object.keys(filters).forEach((key) => {
    if (typeof filters[key] == 'number' || !!filters[key]) {
      let value = filters[key];

      if (key === 'sortOrder') {
        key = 'sort';
        value = filters['sortOrder'] === 'asc' ? 'EARLIEST' : 'LATEST';
      }

      params = params.append(key, value);
    }
  });

  return params;
}

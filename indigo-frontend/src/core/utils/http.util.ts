import { HttpParams } from '@angular/common/http';
import { PagedRequest } from '../types/request/paged-request.i';

export function getRequestParams(filter: Record<string, unknown>, paging?: PagedRequest) {
  let params = new HttpParams();

  const filters = { ...filter, ...paging };

  Object.keys(filters).forEach((key) => {
    const value = filters[key];

    if (value === null || value === undefined || value === '') {
      return;
    }

    if (Array.isArray(value)) {
      value.forEach((item) => {
        if (item !== null && item !== undefined && item !== '') {
          params = params.append(key, item);
        }
      });
      return;
    }

    if (typeof value === 'number' || typeof value === 'string' || typeof value === 'boolean') {
      params = params.append(key, value);
    }
  });

  return params;
}

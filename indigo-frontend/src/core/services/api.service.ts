import { HttpClient, HttpRequest, HttpResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { filter, map, Observable } from 'rxjs';
import { PagedRequest } from '../types/request/paged-request.i';
import { PaginatedResponse } from '../types/response/paginated-response.i';
import { getRequestParams } from '../utils/http.util';

@Injectable({
  providedIn: 'root',
})
export class ApiService<T> {
  protected httpClient = inject(HttpClient);

  request<T>(
    method: 'get' | 'post' | 'put' | 'delete' | 'patch',
    url: string,
    body?: unknown,
    options?: unknown,
  ): Observable<T> {
    const httpReq = new HttpRequest(method, this.buildUrl(url), body, options);

    return this.httpClient.request<T>(httpReq).pipe(
      filter((event) => event instanceof HttpResponse),
      map((event) => event.body),
    );
  }

  public getPaged(url: string, pager: PagedRequest, filter?): Observable<PaginatedResponse<T>> {
    return this.httpClient.get<PaginatedResponse<T>>(this.buildUrl(url), {
      params: getRequestParams(filter, pager),
    });
  }

  public create(url: string, body: unknown): Observable<T> {
    return this.httpClient.post<T>(this.buildUrl(url), body);
  }

  public update(url: string, body: unknown): Observable<T> {
    return this.httpClient.patch<T>(this.buildUrl(url), body);
  }

  public getDictionary<T = { id: string; name: string }[]>(dictionary: string): Observable<T> {
    return this.httpClient.get<T>(this.buildUrl(dictionary));
  }

  public delete(url: string, id: string): Observable<T> {
    return this.httpClient.delete<T>(this.buildUrl(`${url}/${id}`));
  }

  private buildUrl = (str?: string) => `/api/eln/${str || ''}`.replace(/\/\//g, '/').replace(/\/+$/, '');
}

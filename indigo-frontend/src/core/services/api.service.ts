import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly headers = new HttpHeaders({
    'X-Requested-With': 'XMLHttpRequest',
    // Consider adding 'Content-Type': 'application/json' as a default,
    // but it can be overridden in options.
  });

  constructor(private readonly httpClient: HttpClient) {}

  // POST
  post<ResponseType>(url: string, body: any, options: { headers?: HttpHeaders | { [header: string]: string | string[] } } = {}): Observable<ResponseType> {
    const mergedHeaders = this.mergeHeaders(options.headers);
    return this.httpClient.post<ResponseType>(url, body, { headers: mergedHeaders });
  }

  // GET
  get<ResponseType>(url: string, params?: any, options: { headers?: HttpHeaders | { [header: string]: string | string[] } } = {}): Observable<ResponseType> {
    const mergedHeaders = this.mergeHeaders(options.headers);
    const httpParams = this.createHttpParams(params);
    return this.httpClient.get<ResponseType>(url, { headers: mergedHeaders, params: httpParams });
  }

  // PUT
  put<ResponseType>(url: string, body: any, options: { headers?: HttpHeaders | { [header: string]: string | string[] } } = {}): Observable<ResponseType> {
    const mergedHeaders = this.mergeHeaders(options.headers);
    return this.httpClient.put<ResponseType>(url, body, { headers: mergedHeaders });
  }

  // DELETE
  delete<ResponseType>(url: string, options: { headers?: HttpHeaders | { [header: string]: string | string[] }; params?: any; } = {}): Observable<ResponseType> {
    const mergedHeaders = this.mergeHeaders(options.headers);
    const httpParams = this.createHttpParams(options.params);
    return this.httpClient.delete<ResponseType>(url, { headers: mergedHeaders, params: httpParams });
  }


  private mergeHeaders(customHeaders?: HttpHeaders | { [header: string]: string | string[] }): HttpHeaders {
    let mergedHeaders = this.headers;
    if (customHeaders) {
      if (customHeaders instanceof HttpHeaders) {
        customHeaders.keys().forEach(key => {
          mergedHeaders = mergedHeaders.set(key, customHeaders.getAll(key) || []);
        });
      } else { // Handle plain object
        for (const key in customHeaders) {
          if (customHeaders.hasOwnProperty(key)) {
            mergedHeaders = mergedHeaders.set(key, customHeaders[key]);
          }
        }
      }
    }
    return mergedHeaders;
  }

  private createHttpParams(params?: any): HttpParams {
      let httpParams = new HttpParams();
      if (params) {
          for (const key in params) {
              if (params.hasOwnProperty(key)) {
                  const value = params[key];
                  if (value !== null && value !== undefined) {
                      httpParams = httpParams.set(key, value);
                  }
              }
          }
      }
      return httpParams;
  }
}
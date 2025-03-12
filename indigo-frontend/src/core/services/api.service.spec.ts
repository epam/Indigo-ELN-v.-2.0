import { HttpHeaders } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ApiService } from './api.service';

describe('HttpRequestFormatService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApiService]
    });
    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // Ensure that there are no outstanding requests
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('postData', () => {
    it('should send a POST request with default headers', () => {
      const testData = { message: 'Test POST' };
      const url = '/api/test';

      service.post(url, testData).subscribe(response => {
        expect(response).toEqual(testData);
      });

      const req = httpMock.expectOne(url);
      expect(req.request.method).toBe('POST');
      expect(req.request.headers.get('X-Requested-With')).toBe('XMLHttpRequest');
      expect(req.request.body).toEqual(testData);
      req.flush(testData);
    });

    it('should send a POST request with custom headers (HttpHeaders object)', () => {
      const testData = { message: 'Test POST' };
      const url = '/api/test';
      const customHeaders = new HttpHeaders({ 'Authorization': 'Bearer token' });

      service.post(url, testData, { headers: customHeaders }).subscribe();

      const req = httpMock.expectOne(url);
      expect(req.request.headers.get('Authorization')).toBe('Bearer token');
      expect(req.request.headers.get('X-Requested-With')).toBe('XMLHttpRequest'); // Default header should still be present
      req.flush(testData);
    });

    it('should send a POST request with custom headers (plain object)', () => {
      const testData = { message: 'Test POST' };
      const url = '/api/test';
      const customHeaders = { 'Authorization': 'Bearer token' };

      service.post(url, testData, { headers: customHeaders }).subscribe();

      const req = httpMock.expectOne(url);
      expect(req.request.headers.get('Authorization')).toBe('Bearer token');
      expect(req.request.headers.get('X-Requested-With')).toBe('XMLHttpRequest');
      req.flush(testData);
    });
  });

  describe('getData', () => {
    it('should send a GET request with default headers', () => {
      const testData = [{ id: 1, name: 'Test' }];
      const url = '/api/test';

      service.get(url).subscribe(response => {
        expect(response).toEqual(testData);
      });

      const req = httpMock.expectOne(url);
      expect(req.request.method).toBe('GET');
      expect(req.request.headers.get('X-Requested-With')).toBe('XMLHttpRequest');
      expect(req.request.params.toString()).toBe(''); // No params
      req.flush(testData);
    });

    it('should send a GET request with query parameters', () => {
      const testData = [{ id: 1, name: 'Test' }];
      const url = '/api/test';
      const params = { page: 1, limit: 10 };

      service.get(url, params).subscribe();

      const req = httpMock.expectOne(r => r.url === url && r.params.toString() === 'page=1&limit=10');
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('page')).toBe('1');
      expect(req.request.params.get('limit')).toBe('10');
      req.flush(testData);
    });

    it('should send a GET request with custom headers and params', () => {
      const testData = [{ id: 1, name: 'Test' }];
      const url = '/api/test';
      const params = { page: 1, limit: 10 };
      const customHeaders = { 'X-Custom-Header': 'value' };

      service.get(url, params, { headers: customHeaders }).subscribe();

      const req = httpMock.expectOne(r => r.url === url && r.params.toString() === 'page=1&limit=10');
      expect(req.request.headers.get('X-Custom-Header')).toBe('value');
      expect(req.request.headers.get('X-Requested-With')).toBe('XMLHttpRequest');
      expect(req.request.params.get('page')).toBe('1');
      req.flush(testData);
    });

    it('should exclude null and undefined parameters', () => {
      const url = '/api/test';
      const params = { page: 1, limit: null, search: undefined, valid: 'yes' };

      service.get(url, params).subscribe();
      const req = httpMock.expectOne(r => r.url === url);
      expect(req.request.params.get('page')).toBe('1');
      expect(req.request.params.get('valid')).toBe('yes');
      expect(req.request.params.has('limit')).toBe(false);
      expect(req.request.params.has('search')).toBe(false);
      req.flush([]);
    });
  });

  describe('putData', () => {
    it('should send a PUT request with default headers', () => {
      const testData = { id: 1, message: 'Updated' };
      const url = '/api/test/1';

      service.put(url, testData).subscribe(response => {
        expect(response).toEqual(testData);
      });

      const req = httpMock.expectOne(url);
      expect(req.request.method).toBe('PUT');
      expect(req.request.headers.get('X-Requested-With')).toBe('XMLHttpRequest');
      expect(req.request.body).toEqual(testData);
      req.flush(testData);
    });

    it('should send a PUT request with custom headers', () => {
      const testData = { id: 1, message: 'Updated' };
      const url = '/api/test/1';
      const customHeaders = { 'Authorization': 'Bearer token' };

      service.put(url, testData, { headers: customHeaders }).subscribe();

      const req = httpMock.expectOne(url);
      expect(req.request.headers.get('Authorization')).toBe('Bearer token');
      expect(req.request.headers.get('X-Requested-With')).toBe('XMLHttpRequest');
      req.flush(testData);
    });
  });

  describe('deleteData', () => {
    it('should send a DELETE request with default headers', () => {
      const url = '/api/test/1';

      service.delete(url).subscribe();

      const req = httpMock.expectOne(url);
      expect(req.request.method).toBe('DELETE');
      expect(req.request.headers.get('X-Requested-With')).toBe('XMLHttpRequest');
      expect(req.request.body).toBeNull();
      req.flush({}); // Usually, DELETE responses are empty
    });

    it('should send a DELETE request with custom headers', () => {
      const url = '/api/test/1';
      const customHeaders = { 'Authorization': 'Bearer token' };

      service.delete(url, { headers: customHeaders }).subscribe();

      const req = httpMock.expectOne(url);
      expect(req.request.headers.get('Authorization')).toBe('Bearer token');
      expect(req.request.headers.get('X-Requested-With')).toBe('XMLHttpRequest');
      req.flush({});
    });
    it('should send a DELETE request with query parameters', () => {
          const url = '/api/test/1';
          const params = { force: true };
          service.delete(url, {params}).subscribe();
          const req = httpMock.expectOne(r => r.url === url && r.params.toString() === 'force=true');
          expect(req.request.method).toBe('DELETE');
          expect(req.request.params.get('force')).toBe('true');
          req.flush({});
    });
      it('should exclude null and undefined parameters from delete', () => {
        const url = '/api/test';
        const params = { page: 1, limit: null, search: undefined, valid: 'yes' };

        service.delete(url, {params}).subscribe();
        const req = httpMock.expectOne(r => r.url === url);
        expect(req.request.params.get('page')).toBe('1');
        expect(req.request.params.get('valid')).toBe('yes');
        expect(req.request.params.has('limit')).toBe(false);
        expect(req.request.params.has('search')).toBe(false);
        req.flush({});
      });
  });

  describe('mergeHeaders', () => {
    it('should merge headers correctly with HttpHeaders object', () => {
        const customHeaders = new HttpHeaders({ 'Authorization': 'Bearer token', 'X-Another-Header': 'value' });
        const mergedHeaders = service['mergeHeaders'](customHeaders); // Access private method
        expect(mergedHeaders.get('X-Requested-With')).toBe('XMLHttpRequest');
        expect(mergedHeaders.get('Authorization')).toBe('Bearer token');
        expect(mergedHeaders.get('X-Another-Header')).toBe('value');
    });

    it('should merge headers correctly with plain object', () => {
        const customHeaders = { 'Authorization': 'Bearer token', 'X-Another-Header': 'value' };
        const mergedHeaders = service['mergeHeaders'](customHeaders);
        expect(mergedHeaders.get('X-Requested-With')).toBe('XMLHttpRequest');
        expect(mergedHeaders.get('Authorization')).toBe('Bearer token');
        expect(mergedHeaders.get('X-Another-Header')).toBe('value');
    });

    it('should return default headers if no custom headers are provided', () => {
        const mergedHeaders = service['mergeHeaders']();
        expect(mergedHeaders.get('X-Requested-With')).toBe('XMLHttpRequest');
        expect(mergedHeaders.keys().length).toBe(1);
    });

    it('should handle multi-value headers correctly', () => {
        const customHeaders = new HttpHeaders({
            'X-Multi-Header': ['value1', 'value2']
        });
        const merged = service['mergeHeaders'](customHeaders);
        expect(merged.getAll('X-Multi-Header')).toEqual(['value1', 'value2']);
    });
  });

  describe('createHttpParams', () => {
    it('should create HttpParams from a plain object', () => {
        const params = { page: 1, limit: 10, search: 'test' };
        const httpParams = service['createHttpParams'](params);
        expect(httpParams.get('page')).toBe('1');
        expect(httpParams.get('limit')).toBe('10');
        expect(httpParams.get('search')).toBe('test');
    });
    it('should exclude null and undefined values', () => {
        const params = { page: 1, limit: null, search: undefined, valid: 'yes' };
        const httpParams = service['createHttpParams'](params);
        expect(httpParams.get('page')).toBe('1');
        expect(httpParams.has('limit')).toBe(false);
        expect(httpParams.has('search')).toBe(false);
        expect(httpParams.get('valid')).toBe('yes');
    });
    it('should return an empty HttpParams object if no params are provided', () => {
        const httpParams = service['createHttpParams']();
        expect(httpParams.keys().length).toBe(0);
    });
  });
});
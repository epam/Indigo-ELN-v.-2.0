import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { PagedRequest } from '../types/request/paged-request.i';
import { PaginatedResponse } from '../types/response/paginated-response.i';
import { ApiService } from './api.service';

interface TestItem {
  id: number;
  name?: string;
}

interface ApiServicePrivate {
  buildUrl: (str?: string) => string;
}

describe('ApiService', () => {
  let service: ApiService<TestItem>;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApiService],
    });
    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('setup', () => {
    it('should set the controller name', () => {
      service.setup('test-controller');
      expect(service.controller).toBe('test-controller');
    });
  });

  describe('getPaged', () => {
    it('should send a GET request with paging parameters', () => {
      service.setup('projects');
      const pager: PagedRequest = { pageNo: 1, pageSize: 10 };
      const mockResponse: PaginatedResponse<TestItem> = {
        pageNo: 1,
        pageSize: 10,
        totalItems: 30,
        items: [{ id: 1 }, { id: 2 }, { id: 3 }],
        firstIndex: 0,
        lastIndex: 2,
        totalPages: 3,
      };

      service.getPaged(pager).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne((r) => r.url === '/api/eln/projects');
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('pageNo')).toBe('1');
      expect(req.request.params.get('pageSize')).toBe('10');
      req.flush(mockResponse);
    });

    it('should send a GET request with filter and paging parameters', () => {
      service.setup('projects');
      const pager: PagedRequest = { pageNo: 2, pageSize: 20 };
      const filter = { search: 'test', status: 'active' };
      const mockResponse: PaginatedResponse<TestItem> = {
        pageNo: 2,
        pageSize: 20,
        totalItems: 50,
        items: [{ id: 21 }, { id: 22 }],
        firstIndex: 20,
        lastIndex: 21,
        totalPages: 3,
      };

      service.getPaged(pager, filter).subscribe((response) => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(
        (r) =>
          r.url === '/api/eln/projects' &&
          r.params.get('search') === 'test' &&
          r.params.get('status') === 'active',
      );
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('pageNo')).toBe('2');
      expect(req.request.params.get('pageSize')).toBe('20');
      expect(req.request.params.get('search')).toBe('test');
      expect(req.request.params.get('status')).toBe('active');
      req.flush(mockResponse);
    });

    it('should exclude null or undefined filter values', () => {
      service.setup('projects');
      const pager: PagedRequest = { pageNo: 1, pageSize: 10 };
      const filter = {
        search: 'test',
        status: null,
        category: undefined,
        active: true,
      };

      service.getPaged(pager, filter).subscribe();

      const req = httpMock.expectOne((r) => r.url === '/api/eln/projects');
      expect(req.request.params.get('search')).toBe('test');
      expect(req.request.params.get('active')).toBe('true');
      expect(req.request.params.has('status')).toBeFalse();
      expect(req.request.params.has('category')).toBeFalse();
      req.flush({ items: [] });
    });
  });

  describe('buildUrl', () => {
    it('should build a URL with the controller name', () => {
      service.setup('projects');

      const url = (service as unknown as ApiServicePrivate).buildUrl();
      expect(url).toBe('/api/eln/projects');
    });

    it('should build a URL with the controller name and additional path', () => {
      service.setup('projects');

      const url = (service as unknown as ApiServicePrivate).buildUrl('123');
      expect(url).toBe('/api/eln/projects/123');
    });
  });
});

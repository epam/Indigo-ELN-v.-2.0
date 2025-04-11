import { TestBed } from '@angular/core/testing';

import { ProjectRefreshService } from './project-refresh.service';

describe('ProjectRefreshService', () => {
  let service: ProjectRefreshService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProjectRefreshService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});

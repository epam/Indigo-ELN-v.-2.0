import { describe, expect, it, vi } from 'vitest';

import type { Page } from '@/lib/types/common.ts';
import type { Project } from '@/lib/types/projects.ts';

vi.mock('aws-amplify/auth', () => ({
  fetchAuthSession: vi.fn().mockResolvedValue({ tokens: undefined }),
}));

const { fetchProjects, getNextPageParam, projectsQueryString } = await import('@/lib/api/projects');

function page(pageNo: number, totalPages: number): Page<Project> {
  return { pageNo, pageSize: 10, totalItems: totalPages * 10, totalPages, items: [] };
}

describe('getNextPageParam', () => {
  it('returns the next page number while pages remain', () => {
    expect(getNextPageParam(page(0, 3))).toBe(1);
  });

  it('returns undefined on the last page', () => {
    expect(getNextPageParam(page(2, 3))).toBeUndefined();
  });

  it('returns undefined when there are no results at all', () => {
    expect(getNextPageParam(page(0, 0))).toBeUndefined();
  });
});

describe('projectsQueryString', () => {
  it('omits search and createdByMe when unset', () => {
    expect(projectsQueryString({ search: '', sort: 'EARLIEST', createdByMe: false }, 0)).toBe(
      'sort=EARLIEST&pageNo=0&pageSize=10',
    );
  });

  it('includes both once set', () => {
    expect(projectsQueryString({ search: 'acid test', sort: 'LATEST', createdByMe: true }, 2)).toBe(
      'sort=LATEST&pageNo=2&pageSize=10&search=acid+test&createdByMe=true',
    );
  });
});

describe('fetchProjects', () => {
  it('requests the ELN projects endpoint with the built query', async () => {
    const fetchMock = vi.fn().mockResolvedValue(new Response(JSON.stringify(page(0, 1)), { status: 200 }));
    vi.stubGlobal('fetch', fetchMock);

    await fetchProjects({ search: 'x', sort: 'EARLIEST', createdByMe: false }, 1);

    expect(fetchMock.mock.calls[0][0]).toBe('/api/eln/projects?sort=EARLIEST&pageNo=1&pageSize=10&search=x');
  });
});

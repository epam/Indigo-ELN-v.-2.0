import { useInfiniteQuery, useQuery } from '@tanstack/react-query';

import { apiFetch } from '@/lib/api';
import type { Page } from '@/lib/types/common.ts';
import type { Project, ProjectFilters, TotalCounts } from '@/lib/types/projects.ts';

export const PROJECTS_PAGE_SIZE = 10;

/** Optional params are omitted when unset, matching indigo-frontend's client. */
export function projectsQueryString(filters: ProjectFilters, pageNo: number, pageSize = PROJECTS_PAGE_SIZE): string {
  const params = new URLSearchParams({
    sort: filters.sort,
    pageNo: String(pageNo),
    pageSize: String(pageSize),
  });
  if (filters.search) params.set('search', filters.search);
  if (filters.createdByMe) params.set('createdByMe', 'true');
  return params.toString();
}

export function fetchProjects(filters: ProjectFilters, pageNo: number): Promise<Page<Project>> {
  return apiFetch<Page<Project>>(`projects?${projectsQueryString(filters, pageNo)}`);
}

export function fetchTotalCounts(): Promise<TotalCounts> {
  return apiFetch<TotalCounts>('total-counts');
}

/** Filters belong in the key; the page number comes from pageParam. */
export const projectKeys = {
  list: (filters: ProjectFilters) => ['projects', filters] as const,
  totalCounts: () => ['totalCounts'] as const,
};

export function getNextPageParam(lastPage: Page<Project>): number | undefined {
  return lastPage.pageNo + 1 < lastPage.totalPages ? lastPage.pageNo + 1 : undefined;
}

export function useProjects(filters: ProjectFilters) {
  return useInfiniteQuery({
    queryKey: projectKeys.list(filters),
    queryFn: ({ pageParam }) => fetchProjects(filters, pageParam),
    initialPageParam: 0,
    getNextPageParam,
  });
}

export function useTotalCounts() {
  return useQuery({
    queryKey: projectKeys.totalCounts(),
    queryFn: fetchTotalCounts,
  });
}

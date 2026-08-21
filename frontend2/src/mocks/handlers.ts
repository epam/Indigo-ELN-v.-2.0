import { delay, http, HttpResponse } from 'msw';

import { makeTotalCounts, MARKED_EXPERIMENTS, PROJECTS } from '@/mocks/fixtures';

import type { Page } from '@/lib/types/common.ts';
import type { Project } from '@/lib/types/projects.ts';

// buildUrl() in src/lib/api.ts prefixes bare paths with /api/eln/, so handlers
// must carry that prefix.
const ELN = '/api/eln';

function page(items: Project[]): Page<Project> {
  return { pageNo: 0, pageSize: 20, totalItems: items.length, totalPages: 1, items };
}

export const handlers = [
  http.get(`${ELN}/projects`, () => HttpResponse.json(page(PROJECTS))),
  http.get(`${ELN}/total-counts`, () => HttpResponse.json(makeTotalCounts())),
  http.get(`${ELN}/experiments/marked`, () => HttpResponse.json(MARKED_EXPERIMENTS)),
  http.get(`${ELN}/currentUser`, () =>
    HttpResponse.json({
      id: '99999999-9999-9999-9999-999999999999',
      username: 'anna.petrova',
      displayName: 'Administrator',
      permissions: ['VIEW_PROJECTS', 'CREATE_PROJECTS', 'EDIT_PROJECTS'],
    }),
  ),
];

/** Every list endpoint answers with nothing. */
export const emptyHandlers = [
  http.get(`${ELN}/projects`, () => HttpResponse.json(page([]))),
  http.get(`${ELN}/experiments/marked`, () => HttpResponse.json([])),
];

/** Every endpoint fails, so the error branch renders. */
export const errorHandlers = [http.get(`${ELN}/*`, () => new HttpResponse(null, { status: 500 }))];

/** Nothing ever resolves, pinning the story on its pending state. */
export const loadingHandlers = [
  http.get(`${ELN}/*`, async () => {
    await delay('infinite');
    return HttpResponse.json(null);
  }),
];

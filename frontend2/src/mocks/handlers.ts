import { delay, http, HttpResponse } from 'msw';

import {
  KEYWORDS,
  makeCurrentUser,
  makeProjectDetails,
  makeTotalCounts,
  MARKED_EXPERIMENTS,
  PROJECTS,
} from '@/mocks/fixtures';

import type { Page } from '@/lib/types/common.ts';
import type { Project } from '@/lib/types/projects.ts';

// buildUrl() in src/lib/api.ts prefixes bare paths with /api/eln/, so handlers
// must carry that prefix.
const ELN = '/api/eln';

function page(items: Project[]): Page<Project> {
  return { pageNo: 0, pageSize: 20, totalItems: items.length, totalPages: 1, items };
}

/** The one name the mock backend claims is taken, so the duplicate path is reachable. */
export const TAKEN_PROJECT_NAME = 'Kinase Inhibitor Screening';

export const handlers = [
  http.get(`${ELN}/projects`, () => HttpResponse.json(page(PROJECTS))),
  http.post(`${ELN}/projects`, async ({ request }) => {
    const body = (await request.json()) as { name: string };
    return HttpResponse.json(makeProjectDetails({ name: body.name }), { status: 201 });
  }),
  http.get(`${ELN}/projects/existence`, ({ request }) => {
    const name = new URL(request.url).searchParams.get('name') ?? '';
    return HttpResponse.json({ exists: name === TAKEN_PROJECT_NAME });
  }),
  http.get(`${ELN}/projects/keywords/suggest`, ({ request }) => {
    const search = (new URL(request.url).searchParams.get('search') ?? '').toLowerCase();
    // Prefix match, sorted and capped at 20 — the same contract as ProjectRepository.
    const matches = KEYWORDS.filter((keyword) => keyword.toLowerCase().startsWith(search))
      .sort()
      .slice(0, 20);
    return HttpResponse.json(matches);
  }),
  http.get(`${ELN}/total-counts`, () => HttpResponse.json(makeTotalCounts())),
  http.get(`${ELN}/experiments/marked`, () => HttpResponse.json(MARKED_EXPERIMENTS)),
  http.get(`${ELN}/currentUser`, () => HttpResponse.json(makeCurrentUser())),
];

/**
 * A user with no system permissions, so every gated nav item drops out. Story-level
 * handlers replace the default list wholesale, and MSW takes the first match, so the
 * override leads and the rest of the defaults follow.
 */
export const restrictedUserHandlers = [
  http.get(`${ELN}/currentUser`, () => HttpResponse.json(makeCurrentUser({ permissions: ['VIEW_PROJECTS'] }))),
  ...handlers,
];

/** Every list endpoint answers with nothing. */
export const emptyHandlers = [
  http.get(`${ELN}/projects`, () => HttpResponse.json(page([]))),
  http.get(`${ELN}/experiments/marked`, () => HttpResponse.json([])),
];

/** Creating fails while everything else works, so the dialog's error path has a story. */
export const createProjectErrorHandlers = [
  http.post(`${ELN}/projects`, () =>
    HttpResponse.json([{ path: 'name', message: 'Project could not be created' }], { status: 500 }),
  ),
  ...handlers,
];

/** POST never resolves, pinning the dialog on its submitting state. */
export const submittingProjectHandlers = [
  http.post(`${ELN}/projects`, async () => {
    await delay('infinite');
    return HttpResponse.json(null);
  }),
  ...handlers,
];

/** Keyword suggestions arrive slowly, so the loading state is observable. */
export const slowKeywordHandlers = [
  http.get(`${ELN}/projects/keywords/suggest`, async ({ request }) => {
    await delay(400);
    const search = (new URL(request.url).searchParams.get('search') ?? '').toLowerCase();
    return HttpResponse.json(KEYWORDS.filter((keyword) => keyword.toLowerCase().startsWith(search)).sort());
  }),
  ...handlers,
];

/** Only the keyword lookup fails, so the combobox's error branch renders in isolation. */
export const keywordErrorHandlers = [
  http.get(`${ELN}/projects/keywords/suggest`, () => new HttpResponse(null, { status: 500 })),
  ...handlers,
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

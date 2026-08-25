import { delay, http, HttpResponse } from 'msw';

import {
  DICTIONARIES,
  KEYWORDS,
  makeCurrentUser,
  makeProjectDetails,
  makeTotalCounts,
  MARKED_EXPERIMENTS,
  PROJECTS,
  REACTION_SCHEME_SVG,
  SEARCH_RESULTS,
  USERS,
} from '@/mocks/fixtures';

import type { Page, UserRef } from '@/lib/types/common.ts';
import type { GlobalSearchResult } from '@/lib/types/search.ts';
import type { BuiltInDictionary } from '@/lib/types/dictionaries.ts';
import type { Project } from '@/lib/types/projects.ts';

// buildUrl() in src/lib/api.ts prefixes bare paths with /api/eln/, so handlers
// must carry that prefix.
const ELN = '/api/eln';

function page(items: Project[]): Page<Project> {
  return { pageNo: 0, pageSize: 20, totalItems: items.length, totalPages: 1, items };
}

/**
 * A real slice of a real total, unlike `page()` above — infinite scroll only has something
 * to fetch when totalPages is honest.
 */
function searchPage(request: Request, items: GlobalSearchResult[]): Page<GlobalSearchResult> {
  const params = new URL(request.url).searchParams;
  const pageNo = Number(params.get('pageNo') ?? 0);
  const pageSize = Number(params.get('pageSize') ?? 20);
  return {
    pageNo,
    pageSize,
    totalItems: items.length,
    totalPages: Math.ceil(items.length / pageSize),
    items: items.slice(pageNo * pageSize, (pageNo + 1) * pageSize),
  };
}

/** Prefix match across the ref's fields, capped at 10 — the contract of UserRepository.suggest. */
function suggestedUsers(search: string): UserRef[] {
  const term = search.toLowerCase();
  return USERS.filter(
    (user) => user.displayName.toLowerCase().startsWith(term) || user.username.toLowerCase().startsWith(term),
  ).slice(0, 10);
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
  http.get(`${ELN}/dictionaries/:dictionary`, ({ params }) =>
    HttpResponse.json(DICTIONARIES[params.dictionary as BuiltInDictionary] ?? []),
  ),
  http.get(`${ELN}/users/suggest`, ({ request }) =>
    HttpResponse.json(suggestedUsers(new URL(request.url).searchParams.get('search') ?? '')),
  ),
  http.post(`${ELN}/search`, ({ request }) => HttpResponse.json(searchPage(request, SEARCH_RESULTS))),
  // image/svg+xml, which apiFetch hands back as a string because it is not valid JSON.
  http.get(`${ELN}/experiments/:id/picture`, () =>
    HttpResponse.text(REACTION_SCHEME_SVG, { headers: { 'Content-Type': 'image/svg+xml' } }),
  ),
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

/** The two advanced-search lookups arrive slowly, so their loading states are observable. */
export const slowLookupHandlers = [
  http.get(`${ELN}/dictionaries/:dictionary`, async ({ params }) => {
    await delay(400);
    return HttpResponse.json(DICTIONARIES[params.dictionary as BuiltInDictionary] ?? []);
  }),
  http.get(`${ELN}/users/suggest`, async ({ request }) => {
    await delay(400);
    return HttpResponse.json(suggestedUsers(new URL(request.url).searchParams.get('search') ?? ''));
  }),
  ...handlers,
];

/** Only the advanced-search lookups fail, so those comboboxes' error branches render in isolation. */
export const lookupErrorHandlers = [
  http.get(`${ELN}/dictionaries/:dictionary`, () => new HttpResponse(null, { status: 500 })),
  http.get(`${ELN}/users/suggest`, () => new HttpResponse(null, { status: 500 })),
  ...handlers,
];

/** The search finds nothing, so the results' empty branch renders. */
export const emptySearchHandlers = [
  http.post(`${ELN}/search`, ({ request }) => HttpResponse.json(searchPage(request, []))),
  ...handlers,
];

/** Only the search fails, so its error branch renders while the form still works. */
export const searchErrorHandlers = [
  http.post(`${ELN}/search`, () => new HttpResponse(null, { status: 500 })),
  ...handlers,
];

/** The search never resolves, pinning the results on their first-load skeletons. */
export const loadingSearchHandlers = [
  http.post(`${ELN}/search`, async () => {
    await delay('infinite');
    return HttpResponse.json(null);
  }),
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

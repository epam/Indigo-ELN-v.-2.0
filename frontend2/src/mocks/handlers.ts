import { delay, http, HttpResponse } from 'msw';

import {
  ATTACHMENTS,
  COMPOUND_STRUCTURE_SVG,
  DICTIONARIES,
  EXPERIMENT_ACL,
  EXPERIMENT_REFS,
  EXPERIMENTS,
  KEYWORDS,
  makeAttachment,
  makeCurrentUser,
  makeExperimentDetails,
  makeNotebookDetails,
  makeProjectDetails,
  makeTemplateDetails,
  makeTotalCounts,
  MARKED_EXPERIMENTS,
  NOTEBOOK_ACL,
  NOTEBOOKS,
  PROJECT_ACL,
  PROJECTS,
  REACTION_INPUTS,
  REACTION_RXNFILE,
  REACTION_SCHEME_SVG,
  REVISION_DIFF_HTML,
  REVISIONS,
  SAMPLE_RESULTS,
  SEARCH_RESULTS,
  SIGNATURE_TEMPLATES,
  TEMPLATES,
  USERS,
} from '@/mocks/fixtures';

import type { AccessForm, ACLEntry, Page, UserRef } from '@/lib/types/common.ts';
import type { GlobalSearchResult } from '@/lib/types/search.ts';
import type { BuiltInDictionary } from '@/lib/types/dictionaries.ts';
import type { ExperimentEditRequest, ExperimentRequest, ExperimentStatus } from '@/lib/types/experiments.ts';
import type { ModelMutation, MutationResponse } from '@/lib/types/mutations.ts';
import type { NotebookEditRequest, NotebookRequest } from '@/lib/types/notebooks.ts';
import type { ProjectEditRequest } from '@/lib/types/projects.ts';
import type { FindSamplesRequest, SampleDTO, SampleSearchResult, SearchCatalog } from '@/lib/types/samples.ts';

// apiFetch sends the path verbatim, so handlers match the same full paths the
// callers in src/lib/api/ pass.
const ELN = '/api/eln';

function page<T>(items: T[]): Page<T> {
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

/**
 * What `POST /access` answers with: the recomputed ACL for the whole entity — existing
 * entries re-levelled, new ones appended.
 */
function recomputedAcl(existing: ACLEntry[], updates: AccessForm[]): ACLEntry[] {
  const changed = new Map(updates.map((update) => [update.username, update.level]));
  const relevelled = existing.map((entry) =>
    changed.has(entry.username) ? { ...entry, level: changed.get(entry.username)! } : entry,
  );
  const known = new Set(existing.map((entry) => entry.username));
  const added = updates
    .filter((update) => !known.has(update.username))
    .map((update) => ({
      username: update.username,
      displayName: USERS.find((user) => user.username === update.username)?.displayName ?? update.username,
      level: update.level,
      inherited: false,
    }));
  return [...relevelled, ...added];
}

/** The one name the mock backend claims is taken, so the duplicate path is reachable. */
export const TAKEN_PROJECT_NAME = 'Kinase Inhibitor Screening';

/** Likewise for notebooks, which are numbered rather than named. */
export const TAKEN_NOTEBOOK_NAME = '00000002';

/** What `/notebooks/next-number` offers — one past the last of `NOTEBOOKS`, as the backend does. */
export const NEXT_NOTEBOOK_NAME = '00000004';

/**
 * What `/mutate` answers for a `SetScheme`: a diff, not a document. The real backend re-reads
 * the drawing and patches the input and output rows too — this only moves `rxnfile`, which is
 * enough to prove the round trip, since that is what the scheme redraws from.
 *
 * The reaction is addressed as list key `"0"` — same index in, same index out.
 */
function setSchemeResponse(mutation: ModelMutation): MutationResponse {
  if (mutation.type === 'ResolveInputs') return resolveInputsResponse(mutation);
  if (mutation.type !== 'SetScheme') return { patch: {} };
  return {
    patch: { model: { reactions: { '0': { rxnfile: { $old: REACTION_RXNFILE, $new: mutation.rxnFile } } } } },
    messages: ['Reaction scheme updated'],
  };
}

/**
 * What `/mutate` answers for a `ResolveInputs`: the bound sample id written onto the input row's
 * first sample. The real handler also fills in the compound, the batch number and everything the
 * calculator derives from them — this moves the one field the dialog reads back, which is what
 * makes an added row's Add button go disabled.
 *
 * Anchors are addressed by list index, so the row has to be looked up in the same fixture the
 * experiment detail was built from.
 */
function resolveInputsResponse(mutation: Extract<ModelMutation, { type: 'ResolveInputs' }>): MutationResponse {
  const samples: Record<string, unknown> = {};
  for (const [inputAnchor, sampleId] of Object.entries(mutation.inputSamples)) {
    const index = REACTION_INPUTS.findIndex((input) => input.anchor === inputAnchor);
    if (index === -1) continue;
    samples[String(index)] = { samples: { '0': { sampleId: { $new: sampleId } } } };
  }
  return { patch: { model: { reactions: { '0': { inputs: samples } } } } };
}

/** The sample the mark endpoints answer with: the fixture, with the flag flipped. */
function markedSample(sampleID: string | readonly string[] | undefined, marked: boolean): SampleDTO {
  const sample = SAMPLE_RESULTS.find((each) => each.id === sampleID) ?? SAMPLE_RESULTS[0];
  return { ...sample, marked };
}

/**
 * Which fixtures a set of catalogs answers with. `MY_MATERIALS` filters on the mark rather than
 * on the source, as `MyMaterialsCatalogSearchProvider` does — its hits are ELN samples.
 */
function samplesFor(catalogs: SearchCatalog[]): SampleDTO[] {
  return SAMPLE_RESULTS.filter((sample) =>
    catalogs.some((catalog) => (catalog === 'MY_MATERIALS' ? sample.marked === true : sample.source === catalog)),
  );
}

/**
 * A page of catalog hits, paged by the cursor rather than by a page number.
 *
 * `totalItems` is null when only PubChem can answer: it reports no count, and the real service
 * propagates that by adding null to whatever the countable catalogs contributed.
 *
 * `pageSize` overrides what the caller asked for, which is how `pagedSampleHandlers` makes a
 * second page reachable without a hundred fixtures.
 */
async function sampleSearchResponse(request: Request, pageSize?: number): Promise<SampleSearchResult> {
  const body = (await request.json()) as FindSamplesRequest;
  const items = samplesFor(body.catalogs);
  const size = pageSize ?? Number(new URL(request.url).searchParams.get('pageSize') ?? 100);
  const pageNo = body.state?.pageNo ?? 0;
  const countable = items.filter((sample) => sample.source !== 'PUBCHEM');
  const page = items.slice(pageNo * size, (pageNo + 1) * size);
  const hasNext = items.length > (pageNo + 1) * size;

  return {
    items: page,
    totalItems: countable.length === 0 ? null : countable.length,
    next: hasNext ? { catalogs: body.catalogs, pageNo: pageNo + 1, pageSize: size, oldCatalogsTotalItems: null } : null,
  };
}

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
  // After /projects/existence, which `:id` would otherwise swallow — MSW takes the first match.
  http.get(`${ELN}/projects/:id`, ({ params }) => HttpResponse.json(makeProjectDetails({ id: String(params.id) }))),
  http.patch(`${ELN}/projects/:id`, async ({ params, request }) => {
    const { literature, description, ...body } = (await request.json()) as ProjectEditRequest;
    // Absent stays absent, matching JsonNullable; an explicit null clears the field, which on
    // the response DTO is the same as it simply not being there.
    return HttpResponse.json(
      makeProjectDetails({
        id: String(params.id),
        ...body,
        ...(literature === undefined ? {} : { literature: literature ?? undefined }),
        ...(description === undefined ? {} : { description: description ?? undefined }),
      }),
    );
  }),
  http.get(`${ELN}/projects/:id/notebooks`, () => HttpResponse.json(page(NOTEBOOKS))),
  http.post(`${ELN}/projects/:id/notebooks`, async ({ params, request }) => {
    const body = (await request.json()) as NotebookRequest;
    return HttpResponse.json(makeNotebookDetails({ ...body, projectId: String(params.id) }));
  }),
  http.post(`${ELN}/projects/:id/attachments`, async ({ request }) => {
    const form = await request.formData();
    const file = form.get('file');
    const name = file instanceof File ? file.name : 'upload.bin';
    // The endpoint answers with the project's whole attachment list, not just the new file.
    return HttpResponse.json([...ATTACHMENTS, makeAttachment(name, { id: `a77a-${name}`, size: 1_024 })]);
  }),
  http.get(`${ELN}/projects/:id/attachments/:attachmentId`, () =>
    HttpResponse.arrayBuffer(new TextEncoder().encode('mock attachment').buffer as ArrayBuffer, {
      headers: { 'Content-Type': 'application/octet-stream' },
    }),
  ),
  http.delete(`${ELN}/projects/:id/attachments/:attachmentId`, () => new HttpResponse(null, { status: 204 })),
  http.post(`${ELN}/projects/:id/access`, async ({ request }) =>
    HttpResponse.json(recomputedAcl(PROJECT_ACL, (await request.json()) as AccessForm[])),
  ),

  // Both before /notebooks/:id, which would otherwise swallow them — MSW takes the first match.
  http.get(`${ELN}/notebooks/existence`, ({ request }) => {
    const name = new URL(request.url).searchParams.get('name') ?? '';
    return HttpResponse.json({ exists: name === TAKEN_NOTEBOOK_NAME });
  }),
  // Text, not JSON: the endpoint returns a bare String, and `00000004` is not legal JSON.
  http.get(`${ELN}/notebooks/next-number`, () => HttpResponse.text(NEXT_NOTEBOOK_NAME)),
  http.get(`${ELN}/notebooks/:id`, ({ params }) => HttpResponse.json(makeNotebookDetails({ id: String(params.id) }))),
  http.patch(`${ELN}/notebooks/:id`, async ({ params, request }) => {
    const { description, ...body } = (await request.json()) as NotebookEditRequest;
    // Absent stays absent, matching JsonNullable; an explicit null clears the field, which on
    // the response DTO is the same as it simply not being there.
    return HttpResponse.json(
      makeNotebookDetails({
        id: String(params.id),
        ...body,
        ...(description === undefined ? {} : { description: description ?? undefined }),
      }),
    );
  }),
  http.post(`${ELN}/notebooks/:id/experiments`, async ({ params, request }) => {
    const body = (await request.json()) as ExperimentRequest;
    return HttpResponse.json(makeExperimentDetails({ notebookId: String(params.id), templateId: body.templateID }));
  }),
  http.get(`${ELN}/notebooks/:id/experiments`, ({ request }) => {
    const query = new URL(request.url).searchParams;
    const search = (query.get('search') ?? '').toLowerCase();
    const statuses = query.getAll('status') as ExperimentStatus[];
    return HttpResponse.json(
      page(
        EXPERIMENTS.filter(
          (experiment) =>
            experiment.name.toLowerCase().includes(search) &&
            (statuses.length === 0 || statuses.includes(experiment.status)),
        ),
      ),
    );
  }),
  http.post(`${ELN}/notebooks/:id/attachments`, async ({ request }) => {
    const form = await request.formData();
    const file = form.get('file');
    const name = file instanceof File ? file.name : 'upload.bin';
    return HttpResponse.json([...ATTACHMENTS, makeAttachment(name, { id: `b88b-${name}`, size: 2_048 })]);
  }),
  http.get(`${ELN}/notebooks/:id/attachments/:attachmentId`, () =>
    HttpResponse.arrayBuffer(new TextEncoder().encode('mock attachment').buffer as ArrayBuffer, {
      headers: { 'Content-Type': 'application/octet-stream' },
    }),
  ),
  http.delete(`${ELN}/notebooks/:id/attachments/:attachmentId`, () => new HttpResponse(null, { status: 204 })),
  http.post(`${ELN}/notebooks/:id/access`, async ({ request }) =>
    HttpResponse.json(recomputedAcl(NOTEBOOK_ACL, (await request.json()) as AccessForm[])),
  ),
  http.get(`${ELN}/total-counts`, () => HttpResponse.json(makeTotalCounts())),
  http.get(`${ELN}/experiments/marked`, () => HttpResponse.json(MARKED_EXPERIMENTS)),
  http.post(`${ELN}/experiments/:id/mark`, () => HttpResponse.json(true)),
  http.post(`${ELN}/experiments/:id/unmark`, () => HttpResponse.json(false)),
  // Before /experiments/:id, which would otherwise swallow it — MSW takes the first match, the
  // same hazard as /projects/existence and /notebooks/existence above.
  http.get(`${ELN}/experiments/suggest`, ({ request }) => {
    const search = (new URL(request.url).searchParams.get('search') ?? '').toLowerCase();
    // Prefix match on name, ordered by name, capped at 10 — the contract of
    // ExperimentRepository.suggest. Note it does *not* exclude the experiment being edited.
    const matches = EXPERIMENT_REFS.filter((ref) => ref.name.toLowerCase().startsWith(search))
      .sort((a, b) => a.name.localeCompare(b.name))
      .slice(0, 10);
    return HttpResponse.json(matches);
  }),
  http.get(`${ELN}/experiments/:id/revisions`, () => HttpResponse.json(REVISIONS)),
  // text/html, not JSON — the endpoint declares `@Produces(TEXT_HTML)` and `apiFetch` reads it
  // with `responseType: 'text'`.
  http.get(`${ELN}/experiments/:id/revisions/:revisionNo/diff`, () =>
    HttpResponse.text(REVISION_DIFF_HTML, { headers: { 'Content-Type': 'text/html' } }),
  ),
  // After /experiments/marked, which `:id` would otherwise swallow — MSW takes the first match,
  // and the sidebar's starred list would start answering with a single experiment.
  http.get(`${ELN}/experiments/:id`, ({ params }) =>
    HttpResponse.json(makeExperimentDetails({ id: String(params.id) })),
  ),
  // Before /templates/:id, which would otherwise swallow it — MSW takes the first match.
  http.get(`${ELN}/templates`, () => HttpResponse.json(page(TEMPLATES))),
  http.get(`${ELN}/templates/:id`, ({ params }) => HttpResponse.json(makeTemplateDetails({ id: String(params.id) }))),
  http.patch(`${ELN}/experiments/:id`, async ({ params, request }) => {
    const { title, therapeuticArea, projectCode, description, literature, ...lists } =
      (await request.json()) as ExperimentEditRequest;
    // Absent stays absent, matching JsonNullable; an explicit null clears the field, which on
    // the response DTO is the same as it simply not being there. The three list fields have no
    // null state, so they pass straight through.
    return HttpResponse.json(
      makeExperimentDetails({
        id: String(params.id),
        ...(title === undefined ? {} : { title: title ?? undefined }),
        ...(therapeuticArea === undefined ? {} : { therapeuticArea: therapeuticArea ?? undefined }),
        ...(projectCode === undefined ? {} : { projectCode: projectCode ?? undefined }),
        ...(description === undefined ? {} : { description: description ?? undefined }),
        ...(literature === undefined ? {} : { literature: literature ?? undefined }),
        ...lists,
      }),
    );
  }),
  http.post(`${ELN}/experiments/:id/attachments`, async ({ request }) => {
    const form = await request.formData();
    const file = form.get('file');
    const name = file instanceof File ? file.name : 'upload.bin';
    // The endpoint answers with the experiment's whole attachment list, not just the new file.
    return HttpResponse.json([...ATTACHMENTS, makeAttachment(name, { id: `c99c-${name}`, size: 4_096 })]);
  }),
  http.get(`${ELN}/experiments/:id/attachments/:attachmentId`, () =>
    HttpResponse.arrayBuffer(new TextEncoder().encode('mock attachment').buffer as ArrayBuffer, {
      headers: { 'Content-Type': 'application/octet-stream' },
    }),
  ),
  http.delete(`${ELN}/experiments/:id/attachments/:attachmentId`, () => new HttpResponse(null, { status: 204 })),
  http.post(`${ELN}/experiments/:id/mutate`, async ({ request }) =>
    HttpResponse.json(setSchemeResponse((await request.json()) as ModelMutation)),
  ),
  // Multipart, and not a `/mutate` call: `ImportSDF` is the one model mutation the endpoint does
  // not accept. The response is the same `MutationResponse` shape.
  http.post(`${ELN}/experiments/:id/datamodel/reactions/:anchor/importSDF`, () =>
    HttpResponse.json({ patch: {}, messages: ['Imported 1 compound'] } satisfies MutationResponse),
  ),
  /*
   * The workflow transitions. Each answers the whole `ExperimentDetailsDTO` with the status the
   * matching handler would have set, which is what lets a story press Complete and see the header
   * swap to the COMPLETED row. `submit` and `completeAndSubmit` also take a `signatureTemplateId`
   * query param, which the real backend uses to upload the printed report for signature.
   */
  http.post(`${ELN}/experiments/:id/workflow/complete`, () =>
    HttpResponse.json(makeExperimentDetails({ status: 'COMPLETED' })),
  ),
  http.post(`${ELN}/experiments/:id/workflow/reopen`, () =>
    HttpResponse.json(makeExperimentDetails({ status: 'REOPEN' })),
  ),
  http.post(`${ELN}/experiments/:id/workflow/cancel`, () =>
    HttpResponse.json(makeExperimentDetails({ status: 'CANCELLED' })),
  ),
  http.post(`${ELN}/experiments/:id/workflow/submit`, () =>
    HttpResponse.json(makeExperimentDetails({ status: 'SUBMITTED' })),
  ),
  http.post(`${ELN}/experiments/:id/workflow/completeAndSubmit`, () =>
    HttpResponse.json(makeExperimentDetails({ status: 'SUBMITTED' })),
  ),
  http.get(`${ELN}/signatureTemplates`, () => HttpResponse.json(SIGNATURE_TEMPLATES)),
  // A POST, unlike every other download here: the reports service generates the PDF on demand.
  http.post(`${ELN}/experiments/:id/print`, () =>
    HttpResponse.text('%PDF-1.4\n', {
      headers: {
        'Content-Type': 'application/pdf',
        'Content-Disposition': 'attachment; filename="report.pdf"',
      },
    }),
  ),
  // `@Produces("chemical/x-mdl-sdfile")`, with the filename the download names the file after.
  http.get(`${ELN}/experiments/:id/exportSdf`, () =>
    HttpResponse.text('$$$$\n', {
      headers: {
        'Content-Type': 'chemical/x-mdl-sdfile',
        'Content-Disposition': 'attachment; filename="experiment.sdf"',
      },
    }),
  ),
  http.post(`${ELN}/experiments/:id/access`, async ({ request }) =>
    HttpResponse.json(recomputedAcl(EXPERIMENT_ACL, (await request.json()) as AccessForm[])),
  ),
  http.get(`${ELN}/currentUser`, () => HttpResponse.json(makeCurrentUser())),
  http.get(`${ELN}/dictionaries/:dictionary`, ({ params }) =>
    HttpResponse.json(DICTIONARIES[params.dictionary as BuiltInDictionary] ?? []),
  ),
  http.get(`${ELN}/users/suggest`, ({ request }) =>
    HttpResponse.json(suggestedUsers(new URL(request.url).searchParams.get('search') ?? '')),
  ),
  http.post(`${ELN}/search`, ({ request }) => HttpResponse.json(searchPage(request, SEARCH_RESULTS))),
  // image/svg+xml, which fetchApiImage reads as text (`responseType: 'text'`).
  http.get(`${ELN}/experiments/:id/picture`, () =>
    HttpResponse.text(REACTION_SCHEME_SVG, { headers: { 'Content-Type': 'image/svg+xml' } }),
  ),
  // `CompoundAPI` — the Analyze RXN dialog. The three literal paths lead: MSW takes the first
  // match, and `/samples/:sampleID/mark` would otherwise swallow `/samples/external/picture`.
  http.post(`${ELN}/samples/search`, async ({ request }) => HttpResponse.json(await sampleSearchResponse(request))),
  http.post(`${ELN}/samples/importFromSearch`, async ({ request }) => {
    const sample = (await request.json()) as SampleDTO;
    // Registering gives it an ELN identity: an id, a batch number, and a compound to draw.
    return HttpResponse.json({
      ...sample,
      id: '55555555-5555-4555-8555-00000000000f',
      nbkBatchNumber: '20260101-0002-001',
      compoundID: 'c0000000-0000-4000-8000-00000000000f',
    } satisfies SampleDTO);
  }),
  http.get(`${ELN}/samples/external/picture`, () =>
    HttpResponse.text(COMPOUND_STRUCTURE_SVG, { headers: { 'Content-Type': 'image/svg+xml' } }),
  ),
  http.post(`${ELN}/samples/:sampleID/mark`, ({ params }) => HttpResponse.json(markedSample(params.sampleID, true))),
  http.post(`${ELN}/samples/:sampleID/unmark`, ({ params }) => HttpResponse.json(markedSample(params.sampleID, false))),
  // `CompoundAPI.getCompoundPicture` — the batch detail panel's structure pane.
  http.get(`${ELN}/compounds/:id/picture`, () =>
    HttpResponse.text(COMPOUND_STRUCTURE_SVG, { headers: { 'Content-Type': 'image/svg+xml' } }),
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
  http.get(`${ELN}/projects/:id/notebooks`, () => HttpResponse.json(page([]))),
  http.get(`${ELN}/notebooks/:id/experiments`, () => HttpResponse.json(page([]))),
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

/** Creating fails while everything else works, so the dialog's error path has a story. */
export const createExperimentErrorHandlers = [
  http.post(`${ELN}/notebooks/:id/experiments`, () =>
    HttpResponse.json([{ path: 'templateID', message: 'Experiment could not be created' }], { status: 500 }),
  ),
  ...handlers,
];

/** The template list never arrives, pinning the dialog on its initializing state. */
export const initializingTemplateHandlers = [
  http.get(`${ELN}/templates`, async () => {
    await delay('infinite');
    return HttpResponse.json(null);
  }),
  ...handlers,
];

/** The template list fails, so the picker opens empty and says why. */
export const templatesErrorHandlers = [
  http.get(`${ELN}/templates`, () => new HttpResponse(null, { status: 500 })),
  ...handlers,
];

/** Creating fails while everything else works, so the dialog's error path has a story. */
export const createNotebookErrorHandlers = [
  http.post(`${ELN}/projects/:id/notebooks`, () =>
    HttpResponse.json([{ path: 'name', message: 'Notebook could not be created' }], { status: 500 }),
  ),
  ...handlers,
];

/** The next number never arrives, pinning the dialog on its initializing state. */
export const initializingNotebookHandlers = [
  http.get(`${ELN}/notebooks/next-number`, async () => {
    await delay('infinite');
    return HttpResponse.text('');
  }),
  ...handlers,
];

/** The next number fails, so the dialog opens on an empty name the user has to type. */
export const nextNumberErrorHandlers = [
  http.get(`${ELN}/notebooks/next-number`, () => new HttpResponse(null, { status: 500 })),
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

/**
 * The experiment PATCH resolves slowly, so the saving state is reachable at all — the default
 * handler answers instantly, and `SavingOverlay` deliberately shows nothing for a fast save.
 */
export const slowExperimentWriteHandlers = [
  http.patch(`${ELN}/experiments/:id`, async ({ params }) => {
    await delay(1_000);
    return HttpResponse.json(makeExperimentDetails({ id: String(params.id) }));
  }),
  ...handlers,
];

/**
 * A model mutation slow enough to observe the sketcher holding itself open, and no slower:
 * these run in a real browser, in parallel, and a story that parks a worker for a second
 * pushes the timing-sensitive ones around it closer to their own deadlines.
 */
export const slowMutateHandlers = [
  http.post(`${ELN}/experiments/:id/mutate`, async ({ request }) => {
    const mutation = (await request.json()) as ModelMutation;
    await delay(300);
    return HttpResponse.json(setSchemeResponse(mutation));
  }),
  ...handlers,
];

/**
 * The model-mutation endpoint answering with a **recalculation**: whatever was asked for, the
 * reply also rewrites a value the user did not touch. That is the normal case on the real
 * backend — changing one weight moves every mol, EQ and yield derived from it — and it is what
 * the stoichiometry table's green flash exists to point out.
 *
 * The path spells out how the diff dialect addresses a nested row. `/model/reactions` and the
 * `inputs`/`samples` arrays under it are **list** paths in `JSON_PATCHER`, whose keys are
 * indices (`"0"`, or `"2>5"` for something that moved), while a leaf carrying `$old`/`$new`
 * replaces the node outright. Replacing the `mol` node is what puts it in `updatedNodes`,
 * which is what `determineCellClasses` compares against.
 */
export const recalculatingMutateHandlers = [
  http.post(`${ELN}/experiments/:id/mutate`, () =>
    HttpResponse.json({
      patch: {
        model: {
          reactions: {
            '0': {
              inputs: {
                '1': {
                  samples: {
                    '0': {
                      mol: {
                        $old: { value: '0.0049', unit: 'MMOL', source: 'calculated' },
                        $new: { value: '0.0075', unit: 'MMOL', source: 'calculated' },
                      },
                    },
                  },
                },
              },
            },
          },
        },
      },
    } satisfies MutationResponse),
  ),
  ...handlers,
];

/**
 * The model-mutation endpoint fails. Separate from `errorHandlers`, which only intercepts GETs
 * — a POST would fall straight through it and hit the happy path.
 */
export const failingMutateHandlers = [
  http.post(`${ELN}/experiments/:id/mutate`, () => new HttpResponse(null, { status: 500 })),
  ...handlers,
];

/**
 * What an undo with an empty stack answers: 400 carrying the backend's `ErrorDTO[]` body. That
 * is the only way to learn there is nothing to undo — no `canUndo` is exposed anywhere — and
 * `describeError` renders the array's own wording, so the toast says what the server said.
 */
export const nothingToUndoHandlers = [
  http.post(`${ELN}/experiments/:id/mutate`, () =>
    HttpResponse.json([{ message: 'Nothing to undo' }], { status: 400 }),
  ),
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

/**
 * A scheme edit that comes back with two unmatched reactants, which is what opens Analyze RXN.
 * The anchors are the first two rows of `REACTION_INPUTS`, so the dialog's tabs are named after
 * their formulas; the values are molfiles only in shape — nothing here parses them.
 */
export const unresolvedSchemeHandlers = [
  http.post(`${ELN}/experiments/:id/mutate`, async ({ request }) => {
    const mutation = (await request.json()) as ModelMutation;
    const response = setSchemeResponse(mutation);
    if (mutation.type !== 'SetScheme') return HttpResponse.json(response);
    return HttpResponse.json({
      ...response,
      unresolvedInputs: {
        [REACTION_INPUTS[0].anchor]: 'unresolved-reactant-molfile',
        [REACTION_INPUTS[1].anchor]: 'unresolved-solvent-molfile',
      },
    } satisfies MutationResponse);
  }),
  ...handlers,
];

/** Two hits at a time, so the results table's scroll actually reaches a second page. */
export const pagedSampleHandlers = [
  http.post(`${ELN}/samples/search`, async ({ request }) => HttpResponse.json(await sampleSearchResponse(request, 2))),
  ...handlers,
];

/** The catalog search fails; the table says so under its own header. */
export const failingSampleSearchHandlers = [
  http.post(`${ELN}/samples/search`, () => new HttpResponse(null, { status: 500 })),
  ...handlers,
];

/** The catalogs have nothing for this structure. */
export const emptySampleSearchHandlers = [
  http.post(`${ELN}/samples/search`, () =>
    HttpResponse.json({ items: [], totalItems: 0, next: null } satisfies SampleSearchResult),
  ),
  ...handlers,
];

/**
 * Slow enough to observe a row's Add spinner, and no slower. `SavingOverlay` holds its spinner
 * back for 300 ms, so a delay much under this leaves nothing to see.
 */
export const slowSampleWriteHandlers = [
  http.post(`${ELN}/experiments/:id/mutate`, async ({ request }) => {
    const mutation = (await request.json()) as ModelMutation;
    await delay(900);
    return HttpResponse.json(setSchemeResponse(mutation));
  }),
  ...handlers,
];

/**
 * The catalog search never answers, pinning the table on its skeletons. Not `loadingHandlers`:
 * that intercepts GETs only, and the search is a POST, so it would fall through to no handler
 * at all and fail instead of hanging.
 */
export const loadingSampleSearchHandlers = [
  http.post(`${ELN}/samples/search`, async () => {
    await delay('infinite');
    return HttpResponse.json(null);
  }),
  ...handlers,
];

/**
 * A catalog that answers without a count and still has more to give — `totalItems: null` plus a
 * cursor. Page two never arrives, so the state stays put and the tab's count stays a lower bound
 * (`2+`) instead of flickering through it on the way to a total.
 */
export const uncountedSampleHandlers = [
  http.post(`${ELN}/samples/search`, async ({ request }) => {
    const body = (await request.json()) as FindSamplesRequest;
    if (body.state != null) {
      await delay('infinite');
      return HttpResponse.json(null);
    }
    return HttpResponse.json({
      items: SAMPLE_RESULTS.slice(0, 2),
      totalItems: null,
      next: { catalogs: body.catalogs, pageNo: 1, pageSize: 2, oldCatalogsTotalItems: null },
    } satisfies SampleSearchResult);
  }),
  ...handlers,
];

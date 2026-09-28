import { describe, expect, it } from 'vitest';

import type { AddMaterialFormValues } from '@/components/experiments/samples/add-material-form';
import {
  EMPTY_ADD_MATERIAL_FORM,
  isEmpty,
  isFilterDisabled,
  pubchemIncluded,
  summarizeAddMaterialSearch,
  toFindSamplesRequest,
} from '@/components/experiments/samples/add-material-form';

function form(overrides: Partial<AddMaterialFormValues> = {}): AddMaterialFormValues {
  return { ...EMPTY_ADD_MATERIAL_FORM, ...overrides };
}

describe('pubchemIncluded', () => {
  it('is true for All Catalogs as well as PubChem, since All is ELN + PubChem', () => {
    expect(pubchemIncluded('ALL')).toBe(true);
    expect(pubchemIncluded('PUBCHEM')).toBe(true);
  });

  it('is false for the two catalogs that are entirely ELN-side', () => {
    expect(pubchemIncluded('ELN')).toBe(false);
    expect(pubchemIncluded('MY_MATERIALS')).toBe(false);
  });
});

describe('isFilterDisabled', () => {
  /** PubChem's API does take a formula, which is why it is the one filter left enabled. */
  it('leaves Molecular Formula available even under PubChem', () => {
    expect(isFilterDisabled('ALL', 'molecularFormula')).toBe(false);
    expect(isFilterDisabled('PUBCHEM', 'molecularFormula')).toBe(false);
  });

  it('disables the fine-grained filters under any catalog that reaches PubChem', () => {
    expect(isFilterDisabled('ALL', 'compoundKey')).toBe(true);
    expect(isFilterDisabled('PUBCHEM', 'healthHazards')).toBe(true);
  });

  it('leaves everything available for the ELN catalogs', () => {
    expect(isFilterDisabled('ELN', 'compoundKey')).toBe(false);
    expect(isFilterDisabled('MY_MATERIALS', 'casNumber')).toBe(false);
  });
});

describe('toFindSamplesRequest', () => {
  it('maps All Catalogs to ELN + PubChem, and sends nothing else for an untouched form', () => {
    expect(toFindSamplesRequest(form())).toEqual({
      catalogs: ['ELN', 'PUBCHEM'],
      quickSearch: undefined,
      structure: undefined,
      compoundKey: undefined,
      nbkBatchNumber: undefined,
      molecularFormula: undefined,
      molWeight: undefined,
      chemicalName: undefined,
      externalNumber: undefined,
      compoundState: undefined,
      batchComment: undefined,
      healthHazards: undefined,
      casNumber: undefined,
    });
  });

  /** `@Size(min = 1)` server-side: an empty string is a 400, not "no filter". */
  it('omits a blank quick search rather than sending an empty string', () => {
    expect(toFindSamplesRequest(form({ quickSearch: '   ' })).quickSearch).toBeUndefined();
    expect(toFindSamplesRequest(form({ quickSearch: ' aspirin ' })).quickSearch).toBe('aspirin');
  });

  it('sends the drawn structure under the chosen search type', () => {
    const request = toFindSamplesRequest(form({ structure: 'molfile', structureType: 'EXACT' }));
    expect(request.structure).toEqual({ type: 'EXACT', query: 'molfile' });
  });

  it('carries every filter through for an ELN-only catalog', () => {
    const request = toFindSamplesRequest(
      form({
        catalog: 'ELN',
        compoundKey: { type: 'contains', value: 'ASA' },
        molWeight: { type: 'ge', value: 100 },
        externalNumber: { type: 'exact', value: 'EXT-1' },
        compoundState: { id: 'state-1', name: 'Solid' },
      }),
    );

    expect(request).toMatchObject({
      catalogs: ['ELN'],
      compoundKey: { type: 'contains', value: 'ASA' },
      molWeight: { type: 'ge', value: 100 },
      externalNumber: { type: 'exact', value: 'EXT-1' },
      compoundState: { id: 'state-1', name: 'Solid' },
    });
  });

  /**
   * The Angular original deletes these from the body before sending. Keeping them would have the
   * results answer a different question from the one the form appears to be asking.
   */
  it('drops the PubChem-disabled filters, keeping the formula, the term and the structure', () => {
    const request = toFindSamplesRequest(
      form({
        catalog: 'ALL',
        quickSearch: 'aspirin',
        structure: 'molfile',
        compoundKey: { type: 'exact', value: 'ASA' },
        molWeight: { type: 'eq', value: 180 },
        healthHazards: { id: 'hazard-1', name: 'Irritant' },
        molecularFormula: { type: 'exact', value: 'C9H8O4' },
      }),
    );

    expect(request.compoundKey).toBeUndefined();
    expect(request.molWeight).toBeUndefined();
    expect(request.healthHazards).toBeUndefined();
    expect(request.molecularFormula).toEqual({ type: 'exact', value: 'C9H8O4' });
    expect(request.quickSearch).toBe('aspirin');
    expect(request.structure).toEqual({ type: 'SUBSTRUCTURE', query: 'molfile' });
  });
});

describe('isEmpty', () => {
  it('is true for an untouched form — a catalog on its own asks nothing', () => {
    expect(isEmpty(form())).toBe(true);
  });

  it('is false once there is a term, a structure or a filter', () => {
    expect(isEmpty(form({ quickSearch: 'aspirin' }))).toBe(false);
    expect(isEmpty(form({ structure: 'molfile' }))).toBe(false);
    expect(isEmpty(form({ catalog: 'ELN', compoundKey: { type: 'exact', value: 'ASA' } }))).toBe(false);
  });

  it('ignores whitespace in the quick search, as the request does', () => {
    expect(isEmpty(form({ quickSearch: '   ' }))).toBe(true);
  });

  /**
   * The form looks filled in, but the catalog drops that filter on the way out — enabling Search
   * would run a whole-catalog browse under the appearance of a filtered one.
   */
  it('is true when the only filter set is one the catalog has disabled', () => {
    const values = form({ compoundKey: { type: 'exact', value: 'ASA' } });

    expect(isEmpty({ ...values, catalog: 'ELN' })).toBe(false);
    expect(isEmpty({ ...values, catalog: 'ALL' })).toBe(true);
  });

  /** Molecular Formula survives the PubChem gate, so it is a criterion there too. */
  it('counts the formula even under PubChem', () => {
    expect(isEmpty(form({ catalog: 'PUBCHEM', molecularFormula: { type: 'exact', value: 'C9H8O4' } }))).toBe(false);
  });
});

describe('summarizeAddMaterialSearch', () => {
  it('says nothing about an untouched form', () => {
    expect(summarizeAddMaterialSearch(form())).toEqual([]);
  });

  it('words a text, a numeric and a dictionary filter each by its own rule', () => {
    const summary = summarizeAddMaterialSearch(
      form({
        catalog: 'ELN',
        compoundKey: { type: 'contains', value: 'ASA' },
        molWeight: { type: 'ge', value: 100 },
        compoundState: { id: 'state-1', name: 'Solid' },
      }),
    );

    expect(summary).toEqual([
      { label: 'Compound ID', operator: 'contains', value: 'ASA' },
      { label: 'Molecular Weight', operator: '≥', value: '100' },
      { label: 'Component State', operator: 'is', value: 'Solid' },
    ]);
  });

  it('reads a range as its two bounds', () => {
    const summary = summarizeAddMaterialSearch(
      form({ catalog: 'ELN', nbkBatchNumber: { type: 'between', from: '1', to: '9' } }),
    );

    expect(summary).toEqual([{ label: 'Nbk Batch #', operator: 'between', value: '1 and 9' }]);
  });

  /** The summary must not claim a filter the request will not carry. */
  it('leaves out a filter the catalog has disabled', () => {
    const values = form({ compoundKey: { type: 'exact', value: 'ASA' } });

    expect(summarizeAddMaterialSearch({ ...values, catalog: 'ELN' })).toHaveLength(1);
    expect(summarizeAddMaterialSearch({ ...values, catalog: 'ALL' })).toEqual([]);
  });
});

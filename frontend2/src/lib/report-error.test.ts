import { describe, expect, it } from 'vitest';

import { buildIncidentForm, buildReportErrorContext } from '@/lib/report-error';

describe('buildReportErrorContext', () => {
  it('serialises JSON bodies and passes a text response through', () => {
    expect(buildReportErrorContext('/api/eln/projects', 'POST', { name: 'x' }, 'Bad Gateway')).toEqual({
      requestURL: '/api/eln/projects',
      requestMethod: 'POST',
      requestBody: '{"name":"x"}',
      responseBody: 'Bad Gateway',
    });
  });

  it('leaves absent bodies undefined', () => {
    const context = buildReportErrorContext('/api/eln/projects', 'GET', undefined, null);
    expect(context.requestBody).toBeUndefined();
    expect(context.responseBody).toBeUndefined();
  });
});

describe('buildIncidentForm', () => {
  const values = { title: ' Broken ', problemDescription: ' It broke ' };

  it('sends only the url and the joined message for a user-opened report', () => {
    const form = buildIncidentForm(values, 'http://localhost/projects');
    expect(Object.fromEntries(form)).toEqual({ url: 'http://localhost/projects', message: 'Broken\n\nIt broke' });
  });

  it('adds the experiment and the failed request, skipping the parts that are missing', () => {
    const context = buildReportErrorContext('/api/eln/experiments/1', 'GET', undefined, [{ message: 'nope' }]);
    const form = buildIncidentForm(values, 'http://localhost/experiments/1', '{"id":"1"}', context);
    expect(Object.fromEntries(form)).toEqual({
      url: 'http://localhost/experiments/1',
      message: 'Broken\n\nIt broke',
      experiment: '{"id":"1"}',
      requestURL: '/api/eln/experiments/1',
      requestMethod: 'GET',
      responseBody: '[{"message":"nope"}]',
    });
  });
});

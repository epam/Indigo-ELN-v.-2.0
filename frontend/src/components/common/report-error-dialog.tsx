import { useForm } from '@tanstack/react-form';
import { useQueryClient } from '@tanstack/react-query';
import { useMatch } from '@tanstack/react-router';

import { FormDialog } from '@/components/common/form-dialog';
import { Field } from '@/components/ui/field';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { experimentKeys } from '@/lib/api/experiments';
import { useSubmitIncident } from '@/lib/api/incidents';
import { buildIncidentForm, closeReportError, useReportErrorState } from '@/lib/report-error';
import { notifyInfo } from '@/lib/toast';

import type { ReportErrorDraft } from '@/lib/report-error';
import type { ExperimentDetails } from '@/lib/types/experiments.ts';

/**
 * The one Report Error dialog, mounted once in `AppHeader` and opened through
 * `openReportError` — by the header button with an empty draft, or by an error toast's action
 * with the failed request attached.
 */
function ReportErrorDialog() {
  const { open, draft, seq } = useReportErrorState();
  return <ReportErrorForm key={seq} open={open} draft={draft} />;
}

function ReportErrorForm({ open, draft }: { open: boolean; draft: ReportErrorDraft }) {
  const submitIncident = useSubmitIncident();
  const queryClient = useQueryClient();
  // The experiment on screen travels with every report, as the backend pairs it with its own
  // snapshot of the same id.
  const experimentMatch = useMatch({ from: '/_auth/experiments/$id', shouldThrow: false });
  const experiment = experimentMatch
    ? queryClient.getQueryData<ExperimentDetails>(experimentKeys.detail(experimentMatch.params.id))
    : undefined;
  const experimentJson = experiment ? JSON.stringify(experiment, null, 2) : undefined;
  const currentUrl = window.location.href;
  const { context } = draft;

  const form = useForm({
    defaultValues: { title: draft.title ?? '', problemDescription: draft.problemDescription ?? '' },
    onSubmit: async ({ value }) => {
      await submitIncident.mutateAsync(buildIncidentForm(value, currentUrl, experimentJson, context));
      closeReportError();
      notifyInfo('Error report submitted successfully.');
    },
  });

  // Shown read-only so the user sees what is sent along — and only for a report raised from a
  // failed request.
  const technicalDetails: [id: string, label: string, value: string | undefined, rows: number][] = context
    ? [
        ['currentUrl', 'Current URL', currentUrl, 2],
        ['requestURL', 'Request URL', context.requestURL, 2],
        ['requestMethod', 'Request Method', context.requestMethod, 1],
        ['requestBody', 'Request Body', context.requestBody, 4],
        ['responseBody', 'Response Body', context.responseBody, 6],
        ['experimentJson', 'Experiment JSON', experimentJson, 8],
      ]
    : [];

  return (
    <form.Subscribe selector={(state) => !state.canSubmit}>
      {(submitDisabled) => (
        <FormDialog
          open={open}
          onOpenChange={(nextOpen) => {
            if (!nextOpen) closeReportError();
          }}
          title="Report Error"
          submitLabel="Submit"
          submitDisabled={submitDisabled}
          onSubmit={() => form.handleSubmit()}
          className={context ? 'w-[720px]' : undefined}
        >
          <form.Field
            name="title"
            validators={{ onChange: ({ value }) => (value.trim() ? undefined : 'Title is required') }}
          >
            {(field) => (
              <Field
                id={field.name}
                label="Title"
                required
                error={field.state.meta.isTouched ? field.state.meta.errors[0] : undefined}
              >
                <Input
                  id={field.name}
                  name={field.name}
                  value={field.state.value}
                  placeholder="Brief summary of the issue"
                  onBlur={field.handleBlur}
                  onChange={(event) => field.handleChange(event.target.value)}
                />
              </Field>
            )}
          </form.Field>

          <form.Field
            name="problemDescription"
            validators={{ onChange: ({ value }) => (value.trim() ? undefined : 'Problem Description is required') }}
          >
            {(field) => (
              <Field
                id={field.name}
                label="Problem Description"
                required
                error={field.state.meta.isTouched ? field.state.meta.errors[0] : undefined}
              >
                <Textarea
                  id={field.name}
                  name={field.name}
                  value={field.state.value}
                  placeholder="Describe what happened and what you expected instead"
                  rows={6}
                  onBlur={field.handleBlur}
                  onChange={(event) => field.handleChange(event.target.value)}
                />
              </Field>
            )}
          </form.Field>

          {technicalDetails.map(
            ([id, label, value, rows]) =>
              value && (
                <Field key={id} id={id} label={label}>
                  <Textarea id={id} value={value} rows={rows} readOnly className="font-mono text-[12px]/5" />
                </Field>
              ),
          )}
        </FormDialog>
      )}
    </form.Subscribe>
  );
}

export { ReportErrorDialog };

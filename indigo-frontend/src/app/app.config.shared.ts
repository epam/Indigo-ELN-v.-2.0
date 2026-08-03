import { ChipGridFieldComponent } from '@/core/components/formly/fields/chip-grid-field.component';
import { InputFieldComponent } from '@/core/components/formly/fields/input-field.component';
import { TextareaFieldComponent } from '@/core/components/formly/fields/textarea-field.component';
import { ElnWrapperFormField } from '@/core/components/formly/wrappers/field-wrapper.component';
import { HTTP_INTERCEPTORS, HttpFeature, HttpFeatureKind, withXsrfConfiguration } from '@angular/common/http';
import { EnvironmentProviders, importProvidersFrom, Provider, provideZoneChangeDetection } from '@angular/core';

import { EditorFormlyFieldComponent } from '@/core/components/formly/fields/editor/editor-field.component';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter, withComponentInputBinding, withRouterConfig } from '@angular/router';
import { FormlyModule } from '@ngx-formly/core';
import { FormlyPresetModule } from '@ngx-formly/core/preset';
import { FormlyMaterialModule } from '@ngx-formly/material';
import { FormlyMatDatepickerModule } from '@ngx-formly/material/datepicker';
import { routes } from './app.routes';
import { SelectFieldComponent } from '@/core/components/formly/fields/select-field.component';
import { SelectChipsComponent } from '@/core/components/formly/fields/select-chips.component';
import { DropdownFieldComponent } from '@/core/components/formly/fields/dropdown-field.component';
import { ExperimentSelectFieldComponent } from '@/core/components/formly/fields/experiment-select-field.component';
import { ErrorInterceptor } from '@core/interceptors/error.interceptor';

/**
 * Providers shared by every auth provider.
 *
 * The app ships two bootstrap configurations, swapped by the `local` build configuration in
 * angular.json (`fileReplacements`): app.config.ts (Cognito) and app.config.local.ts (Keycloak).
 * Anything that is not auth-specific belongs HERE so the two cannot drift apart.
 */
export const commonProviders: Array<Provider | EnvironmentProviders> = [
  FormlyPresetModule,
  importProvidersFrom(
    FormlyModule.forRoot({
      types: [
        {
          name: 'input',
          component: InputFieldComponent,
          wrappers: ['raw'],
        },
        {
          name: 'textarea',
          component: TextareaFieldComponent,
          wrappers: ['raw'],
        },
        {
          name: 'chip-grid',
          component: ChipGridFieldComponent,
          wrappers: ['raw'],
        },
        {
          name: 'editor',
          component: EditorFormlyFieldComponent,
          wrappers: ['raw'],
        },
        {
          name: 'select',
          component: SelectFieldComponent,
          wrappers: ['raw'],
        },
        {
          name: 'dropdown',
          component: DropdownFieldComponent,
          wrappers: ['raw'],
        },
        {
          name: 'select-chips',
          component: SelectChipsComponent,
          wrappers: ['raw'],
        },
        {
          name: 'experiment-select',
          component: ExperimentSelectFieldComponent,
          wrappers: ['raw'],
        },
      ],
      validationMessages: [
        {
          name: 'required',
          message: (_, field) => {
            return `${field.props.label} is required.`;
          },
        },
      ],
      wrappers: [
        {
          name: 'raw',
          component: ElnWrapperFormField,
        },
      ],
      presets: [],
    }),
    FormlyMaterialModule,
    FormlyMatDatepickerModule,
  ),
  provideZoneChangeDetection({ eventCoalescing: true }),
  provideRouter(routes, withComponentInputBinding(), withRouterConfig({ paramsInheritanceStrategy: 'always' })),
  provideAnimationsAsync(),
  { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true },
];

/**
 * HttpClient features shared by both configurations. Auth interceptors are added per config.
 * A function rather than a const so the two configurations never share a feature instance.
 */
export const sharedHttpFeatures = (): HttpFeature<HttpFeatureKind>[] => [
  withXsrfConfiguration({
    cookieName: 'CSRF-TOKEN',
    headerName: 'X-CSRF-TOKEN',
  }),
];

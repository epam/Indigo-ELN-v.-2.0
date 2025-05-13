import { ChipGridFieldComponent } from '@/core/components/formly/chip-grid-field.component';
import { ElnWrapperFormField } from '@/core/components/formly/field-wrapper.component';
import { InputFieldComponent } from '@/core/components/formly/input-field.component';
import { JwtInterceptor } from '@/core/interceptors/jwt.interceptor';
import {
  HTTP_INTERCEPTORS,
  provideHttpClient,
  withInterceptorsFromDi,
  withXsrfConfiguration,
} from '@angular/common/http';
import {
  ApplicationConfig,
  importProvidersFrom,
  provideZoneChangeDetection,
} from '@angular/core';
// import { provideQuillConfig } from 'ngx-quill';

import { EditorFormlyFieldComponent } from '@/core/components/formly/editor/editor-field.component';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';
import { FormlyModule } from '@ngx-formly/core';
import { FormlyPresetModule } from '@ngx-formly/core/preset';
import { FormlyMaterialModule } from '@ngx-formly/material';
import { FormlyMatDatepickerModule } from '@ngx-formly/material/datepicker';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
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
            name: 'chip-grid',
            component: ChipGridFieldComponent,
            wrappers: ['raw'],
          },
          {
            name: 'editor',
            component: EditorFormlyFieldComponent,
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
    provideRouter(routes),
    provideAnimationsAsync(),
    provideHttpClient(
      withXsrfConfiguration({
        cookieName: 'CSRF-TOKEN',
        headerName: 'X-CSRF-TOKEN',
      }),
      withInterceptorsFromDi(),
    ),
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
    // provideQuillConfig({
    //   modules: {
    //     toolbar: [
    //       ['bold', 'italic', 'underline', 'strike'],
    //       ['blockquote', 'code-block'],
    //       [{ list: 'ordered' }, { list: 'bullet' }],
    //       [{ header: [1, 2, 3, 4, 5, 6, false] }],
    //       ['clean'],
    //       ['link', 'image'],
    //     ],
    //   },
    // }),
  ],
};

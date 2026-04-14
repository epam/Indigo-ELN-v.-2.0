import { ChipGridFieldComponent } from '@/core/components/formly/fields/chip-grid-field.component';
import { InputFieldComponent } from '@/core/components/formly/fields/input-field.component';
import { ElnWrapperFormField } from '@/core/components/formly/wrappers/field-wrapper.component';
import { DropdownFieldComponent } from '@/core/components/formly/fields/dropdown-field.component';

import { provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  ApplicationConfig,
  importProvidersFrom,
  provideZoneChangeDetection,
} from '@angular/core';

import { EditorFormlyFieldComponent } from '@/core/components/formly/fields/editor/editor-field.component';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';
import { FormlyModule } from '@ngx-formly/core';
import { FormlyPresetModule } from '@ngx-formly/core/preset';
import { FormlyMaterialModule } from '@ngx-formly/material';
import { FormlyMatDatepickerModule } from '@ngx-formly/material/datepicker';
import { routes } from './app.routes';
import {
  AutoRefreshTokenService,
  createInterceptorCondition,
  INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
  IncludeBearerTokenCondition,
  includeBearerTokenInterceptor,
  provideKeycloak,
  UserActivityService,
  withAutoRefreshToken,
} from 'keycloak-angular';

const urlCondition = createInterceptorCondition<IncludeBearerTokenCondition>({
  urlPattern: /^(http:\/\/localhost:8080)(\/.*)?$/i,
  bearerPrefix: 'Bearer',
});

export const appConfig: ApplicationConfig = {
  providers: [
    FormlyPresetModule,
    provideKeycloak({
      config: {
        url: 'http://localhost:8088',
        realm: 'indigo-eln',
        clientId: 'frontend-client',
      },
      initOptions: {
        onLoad: 'login-required',
        silentCheckSsoRedirectUri:
          window.location.origin + '/assets/silent-check-sso.html',
        redirectUri: window.location.origin + '/',
      },
      features: [
        withAutoRefreshToken({
          onInactivityTimeout: 'logout',
          sessionTimeout: 1000,
        }),
      ],
      providers: [
        AutoRefreshTokenService,
        UserActivityService,
        {
          provide: INCLUDE_BEARER_TOKEN_INTERCEPTOR_CONFIG,
          useValue: [urlCondition],
        },
      ],
    }),
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
          {
            name: 'dropdown',
            component: DropdownFieldComponent,
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
    provideHttpClient(withInterceptors([includeBearerTokenInterceptor])),
  ],
};

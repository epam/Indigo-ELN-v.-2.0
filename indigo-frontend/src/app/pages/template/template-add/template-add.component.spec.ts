import { InputFieldComponent } from '@/core/components/formly/fields/input-field.component';
import { ElnWrapperFormField } from '@/core/components/formly/wrappers/field-wrapper.component';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { importProvidersFrom } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormlyModule } from '@ngx-formly/core';

import { TemplateAddComponent } from './template-add.component';

describe('TemplateAddComponent', () => {
  let component: TemplateAddComponent;
  let fixture: ComponentFixture<TemplateAddComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TemplateAddComponent],
      providers: [
        { provide: MatDialogRef, useValue: jasmine.createSpyObj('MatDialogRef', ['close']) },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        provideHttpClient(),
        provideHttpClientTesting(),
        importProvidersFrom(
          FormlyModule.forRoot({
            types: [{ name: 'input', component: InputFieldComponent, wrappers: ['raw'] }],
            wrappers: [{ name: 'raw', component: ElnWrapperFormField }],
          }),
        ),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TemplateAddComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

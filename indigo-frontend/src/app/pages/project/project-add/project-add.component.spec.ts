import { ApiService } from '@/core/services/api.service';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ProjectAddComponent } from './project-add.component';

describe('ProjectAddComponent', () => {
  let component: ProjectAddComponent;
  let fixture: ComponentFixture<ProjectAddComponent>;
  let mockApiService: jasmine.SpyObj<ApiService<any>>;

  beforeEach(async () => {
    mockApiService = jasmine.createSpyObj<ApiService<any>>('ApiService', ['create']);

    await TestBed.configureTestingModule({
      imports: [ProjectAddComponent],
      providers: [
        FormBuilder,
        { provide: ApiService, useValue: mockApiService },
        { provide: MatDialogRef, useValue: jasmine.createSpyObj('MatDialogRef', ['close']) },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(ProjectAddComponent);
    component = fixture.componentInstance;

    /*
    component.modalComponent = jasmine.createSpyObj<ModalComponent>(
      'ModalComponent',
      ['open', 'close'],
    );
    component.fileUpload = jasmine.createSpyObj<FileUploadComponent>(
      'FileUploadComponent',
      ['clearFiles'],
    );
     */
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  /*
  it('should initialize the form and setup API', () => {
    component.ngOnInit();
    expect(mockApiService.setup).toHaveBeenCalledWith('projects');
    expect(component.formGroup).toBeDefined();
    expect(component.formGroup.controls['name']).toBeDefined();
  });

  it('should add a chip when Enter key is pressed', () => {
    component.ngOnInit();
    component.formGroup.patchValue({ keywords: 'testKeyword' });

    const event = new KeyboardEvent('keydown', { key: 'Enter' });
    component.addChip(event);

    expect(component.chips).toContain('testKeyword');
    expect(component.formGroup.value.keywords).toBe('');
  });

  it('should not add an empty chip', () => {
    component.ngOnInit();
    component.formGroup.patchValue({ keywords: '' });

    const event = new KeyboardEvent('keydown', { key: 'Enter' });
    component.addChip(event);

    expect(component.chips.length).toBe(0);
  });

  it('should remove a chip by index', () => {
    component.chips = ['chip1', 'chip2'];
    component.removeChip(0);
    expect(component.chips).toEqual(['chip2']);
  });

  it('should not remove a chip if index is out of bounds', () => {
    component.chips = ['chip1', 'chip2'];
    component.removeChip(5);
    expect(component.chips).toEqual(['chip1', 'chip2']);
  });

  it('should open the modal', async () => {
    await component.open();
    expect(component.modalComponent.open).toHaveBeenCalled();
  });

  it('should close the modal with a reason', () => {
    component.ngOnInit();
    // @ts-expect-error - Stub to prevent error
    component.fileUpload = { clearFiles: () => {} };
    spyOn(component.fileUpload, 'clearFiles');

    component.close('closedByUser');
    expect(component.modalComponent.close).toHaveBeenCalledWith('closedByUser');
  });

  it('should add uploaded file to the files array', () => {
    const file = new File(['content'], 'test.doc', {
      type: 'application/msword',
    });
    component.onFilesUploaded(file);
    expect(component.files).toContain(file);
  });

  it('should set isSubmitted to true when creating a project', () => {
    component.ngOnInit();
    component.createProject();
    expect(component.isSubmitted).toBeTrue();
  });

  it('should not call API if form is invalid', () => {
    component.ngOnInit();
    component.createProject();
    expect(mockApiService.create).not.toHaveBeenCalled();
  });

  it('should call API if form is valid', () => {
    component.ngOnInit();
    component.formGroup.patchValue({ name: 'Test Project' });

    mockApiService.create.and.returnValue(of({ id: 1 }));
    component.createProject();

    expect(mockApiService.create).toHaveBeenCalled();
  });

  it('should handle API error during project creation', fakeAsync(() => {
    spyOn(window, 'alert');
    component.ngOnInit();
    component.formGroup.patchValue({ name: 'Test Project' });

    mockApiService.create.and.returnValue(
      throwError(() => new Error('API Error')),
    );
    component.createProject();
    tick();

    expect(window.alert).toHaveBeenCalledWith('API Error');
    expect(component.modalComponent.close).toHaveBeenCalledWith(
      'projectAddError',
    );
  }));

  it('should upload files after project creation', () => {
    component.ngOnInit();
    component.formGroup.patchValue({ name: 'Test Project' });
    component.files = [new File([''], 'test.doc')];

    mockApiService.create.and.returnValue(of({ id: 1 }));
    mockApiService.uploadAttachment.and.returnValue(of({ success: true }));

    component.createProject();

    expect(mockApiService.uploadAttachment).toHaveBeenCalled();
  });

  it('should handle file upload errors', fakeAsync(() => {
    spyOn(window, 'alert');
    component.ngOnInit();
    component.formGroup.patchValue({ name: 'Test Project' });
    component.files = [new File([''], 'test.doc')];

    mockApiService.create.and.returnValue(of({ id: 1 }));
    mockApiService.uploadAttachment.and.returnValue(
      throwError(() => new Error('Upload Error')),
    );

    component.createProject();
    tick(); // <-- allow the uploadAttachment subscription to complete

    expect(mockApiService.create).toHaveBeenCalled();
    expect(mockApiService.uploadAttachment).toHaveBeenCalled();
    expect(window.alert).toHaveBeenCalledWith('Upload Error');
    expect(component.modalComponent.close).toHaveBeenCalledWith('projectAdded');
  }));

  it('should proceed without file upload if no files are selected', fakeAsync(() => {
    component.ngOnInit();
    component.formGroup.patchValue({ name: 'Project Name' });

    mockApiService.create.and.returnValue(of({ id: 1 }));

    component.createProject();
    tick();

    expect(mockApiService.uploadAttachment).not.toHaveBeenCalled();
    expect(component.modalComponent.close).toHaveBeenCalledWith('projectAdded');
  }));

  it('should trigger file upload if project is created and files exist', fakeAsync(() => {
    component.ngOnInit();
    component.formGroup.patchValue({ name: 'Project Name' });
    const file = new File(['test'], 'doc.doc');
    component.files = [file];

    mockApiService.create.and.returnValue(of({ id: 1 }));
    mockApiService.uploadAttachment.and.returnValue(of({ success: true }));

    component.createProject();
    tick();

    expect(mockApiService.uploadAttachment).toHaveBeenCalled();
    expect(component.modalComponent.close).toHaveBeenCalledWith('projectAdded');
  }));
   */
});

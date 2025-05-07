import { ApiService } from '@/core/services/api.service';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { ExperimentAddComponent } from './experiment-add.component';

describe('ExperimentAddComponent', () => {
  let component: ExperimentAddComponent<any>;
  let fixture: ComponentFixture<ExperimentAddComponent<any>>;
  let apiServiceSpy: jasmine.SpyObj<ApiService<any>>;

  beforeEach(async () => {
    const apiSpy = jasmine.createSpyObj('ApiService', ['setup', 'getDictionary', 'create']);

    await TestBed.configureTestingModule({
      imports: [ExperimentAddComponent],
      providers: [
        FormBuilder,
        { provide: ApiService, useValue: apiSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ExperimentAddComponent);
    component = fixture.componentInstance;
    apiServiceSpy = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService<any>>;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form and call getDictionaries on init', () => {
    const therapeuticMock = [{ id: '1', name: 'Therapy A' }];
    const projectCodeMock = [{ id: '2', name: 'Project X' }];
    apiServiceSpy.getDictionary.and.callFake((key: string) => {
      return of(key === 'THERAPEUTIC_AREA' ? therapeuticMock : projectCodeMock);
    });

    component.ngOnInit();

    expect(apiServiceSpy.setup).toHaveBeenCalledWith('projects');
    expect(component.formGroup).toBeDefined();
    expect(component.theraputicItem).toEqual([{ label: 'Therapy A', value: '1' }]);
    expect(component.projectCodeItem).toEqual([{ label: 'Project X', value: '2' }]);
  });
  
  it('should add chip to linkedExperiments on Enter key', () => {
    component.formGroup = new FormBuilder().group({ linkedExperiments: ['Experiment 1'] });
    const event = { key: 'Enter' };
    component.addChip(event, 'linkedExperiments');
    expect(component.linkedExperiments).toContain('Experiment 1');
    expect(component.formGroup.controls['linkedExperiments'].value).toBe('');
  });

  it('should not add chip if key is not Enter', () => {
    component.formGroup = new FormBuilder().group({ batchCreators: ['User1'] });
    const event = { key: 'Space' };
    component.addChip(event, 'batchCreators');
    expect(component.batchCreators).toEqual([]);
  });

  it('should remove chip from batchCreators', () => {
    component.batchCreators = ['User1', 'User2'];
    component.removeChip(0, 'batchCreators');
    expect(component.batchCreators).toEqual(['User2']);
  });

  it('should reset form and close modal on close()', () => {
    const mockModal = jasmine.createSpyObj('ModalComponent', ['close']);
    spyOn(component, 'modalComponent').and.returnValue(mockModal);
    component.formGroup = new FormBuilder().group({ name: ['test'] });
    component.linkedExperiments = ['A'];
    component.batchCreators = ['B'];

    component.close('testReason');

    expect(component.formGroup.value.name).toBeNull();
    expect(component.linkedExperiments).toEqual([]);
    expect(component.batchCreators).toEqual([]);
    expect(mockModal.close).toHaveBeenCalledWith('testReason');
  });

  it('should call service.create when form is valid', () => {
    component.formGroup = new FormBuilder().group({
      name: ['Test', []],
      template: [[]],
      therapeuticArea: [''],
      contFromRxn: [''],
      projectCode: [''],
      contToRxn: [''],
      coAuthors: [''],
      literature: [''],
      projectAlias: [''],
      linkedExperiments: [''],
      batchCreators: ['']
    });

    const response = { id: '123' };
    apiServiceSpy.create.and.returnValue(of(response));
    spyOn(component, 'close');
    
    component.createExperiment();

    expect(apiServiceSpy.setup).toHaveBeenCalledWith('notebooks', { createUrl: '{notebookId}/experiments' });
    expect(apiServiceSpy.create).toHaveBeenCalled();
    expect(component.close).toHaveBeenCalledWith('experimentAdded');
  });

  it('should close and alert error on failed create', () => {
    spyOn(window, 'alert');
    spyOn(component, 'close');
    component.formGroup = new FormBuilder().group({ name: ['Valid Name'] });
    component.formGroup.addControl('template', new FormBuilder().control([]));
    ['therapeuticArea', 'contFromRxn', 'projectCode', 'contToRxn', 'coAuthors', 'literature', 'projectAlias', 'linkedExperiments', 'batchCreators']
      .forEach(key => component.formGroup.addControl(key, new FormBuilder().control('')));
    
    apiServiceSpy.create.and.returnValue(throwError(() => ({ message: 'Creation failed' })));

    component.createExperiment();

    expect(component.close).toHaveBeenCalledWith('experimentAddError');
    expect(window.alert).toHaveBeenCalledWith('Creation failed');
  });
})

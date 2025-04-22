import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExperimentAddComponent } from './experiment-add.component';

describe('ExperimentAddComponent', () => {
  let component: ExperimentAddComponent;
  let fixture: ComponentFixture<ExperimentAddComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExperimentAddComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ExperimentAddComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

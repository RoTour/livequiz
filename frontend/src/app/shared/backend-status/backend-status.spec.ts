import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BackendStatus } from './backend-status';

describe('BackendStatus', () => {
  let component: BackendStatus;
  let fixture: ComponentFixture<BackendStatus>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BackendStatus]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BackendStatus);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

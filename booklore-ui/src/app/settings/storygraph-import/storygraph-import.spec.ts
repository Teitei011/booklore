import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StorygraphImport } from './storygraph-import';

describe('StorygraphImport', () => {
  let component: StorygraphImport;
  let fixture: ComponentFixture<StorygraphImport>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StorygraphImport]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StorygraphImport);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

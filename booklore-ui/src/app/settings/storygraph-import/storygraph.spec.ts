import { TestBed } from '@angular/core/testing';

import { Storygraph } from './storygraph';

describe('Storygraph', () => {
  let service: Storygraph;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Storygraph);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});

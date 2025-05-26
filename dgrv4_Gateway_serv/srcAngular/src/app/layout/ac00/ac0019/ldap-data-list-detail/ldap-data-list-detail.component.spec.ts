import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LdapDataListDetailComponent } from './ldap-data-list-detail.component';

describe('LdapDataListDetailComponent', () => {
  let component: LdapDataListDetailComponent;
  let fixture: ComponentFixture<LdapDataListDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LdapDataListDetailComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LdapDataListDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

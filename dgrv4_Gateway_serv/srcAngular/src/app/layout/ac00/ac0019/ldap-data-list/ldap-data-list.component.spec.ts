import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LdapDataListComponent } from './ldap-data-list.component';

describe('LdapDataListComponent', () => {
  let component: LdapDataListComponent;
  let fixture: ComponentFixture<LdapDataListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LdapDataListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LdapDataListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

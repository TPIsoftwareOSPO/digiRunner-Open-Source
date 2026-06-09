import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SmartFhirClientService, ConsentInfo, PatientDisplay } from '../smart-shared/smart-fhir-client.service';

type ViewState = 'loading' | 'ready' | 'no-state' | 'error';

@Component({
  selector: 'app-smart-select-patient',
  templateUrl: './smart-select-patient.component.html',
  styleUrls: ['./smart-select-patient.component.scss'],
  standalone: false,
})
export class SmartSelectPatientComponent implements OnInit {

  state: string = '';
  viewState: ViewState = 'loading';

  clientName: string = '';
  websiteName: string = '';
  fhirBasePath: string = '';

  searchMode: 'name' | 'id' = 'name';
  keyword: string = '';
  patients: PatientDisplay[] = [];
  selected: PatientDisplay | null = null;
  loading: boolean = false;
  page: number = 1;
  nextUrl: string | null = null;
  prevUrl: string | null = null;
  readonly pageSize: number = 20;

  /** 候選清單（多選時帶來的限定範圍）；空陣列代表全域搜尋 */
  candidates: string[] = [];
  /** 候選 id → display 預載對應表（受限模式時） */
  candidateDisplayMap: Record<string, PatientDisplay> = {};

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fhir: SmartFhirClientService,
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const state = params['state'];
      if (!state) {
        this.viewState = 'no-state';
        return;
      }
      this.state = state;

      // 取得 consent-info 拿 website / fhirBasePath / clientName
      this.fhir.getConsentInfo(state).subscribe({
        next: (info: ConsentInfo) => {
          // dispatcher 防呆：若其實不需要 patient 選取，回 dispatcher 重新派發
          if (!info.needsPatientSelection) {
            this.router.navigate(['/smart/consent'], { queryParams: { state } });
            return;
          }
          this.clientName = info.clientName;
          this.websiteName = info.websiteName ?? '';
          this.fhirBasePath = info.fhirBasePath ?? '';
          this.candidates = info.patientCandidates ?? [];
          this.viewState = 'ready';
          // 預載：把 sessionStorage 中已選的 patientId 帶入 selected（讓 Back 回來不空白）
          const sel = this.fhir.loadSelection(state);
          if (sel.patientId) {
            this.selected = { id: sel.patientId, name: sel.patientId, gender: '', birthDate: '', age: null };
          }
          if (this.candidates.length > 0) {
            // 受限模式：預載候選人顯示資訊，不做全域搜尋
            this.preloadCandidates();
          } else {
            // 全域搜尋
            this.search();
          }
        },
        error: () => this.viewState = 'error',
      });
    });
  }

  search(): void {
    this.page = 1;
    this.nextUrl = null;
    this.prevUrl = null;
    this.loading = true;
    this.fhir.searchPatients(this.websiteName, this.fhirBasePath, this.keyword, this.searchMode, 0, this.pageSize)
      .subscribe({
        next: (bundle) => this.applyBundle(bundle),
        error: () => this.loading = false,
      });
  }

  prevPage(): void {
    if (!this.prevUrl) return;
    this.loading = true;
    this.fhir.fetchByLink(this.prevUrl).subscribe({
      next: (bundle) => {
        this.page = Math.max(1, this.page - 1);
        this.applyBundle(bundle);
      },
      error: () => this.loading = false,
    });
  }

  nextPage(): void {
    if (!this.nextUrl) return;
    this.loading = true;
    this.fhir.fetchByLink(this.nextUrl).subscribe({
      next: (bundle) => {
        this.page += 1;
        this.applyBundle(bundle);
      },
      error: () => this.loading = false,
    });
  }

  private applyBundle(bundle: any): void {
    const entries = bundle.entry || [];
    this.patients = entries.map((e: any) => this.fhir.extractPatientDisplay(e.resource));
    this.nextUrl = SmartFhirClientService.nextLink(bundle);
    this.prevUrl = SmartFhirClientService.prevLink(bundle);
    this.loading = false;
  }

  get hasNextPage(): boolean { return !!this.nextUrl; }
  get hasPrevPage(): boolean { return !!this.prevUrl; }

  pick(p: PatientDisplay): void {
    this.selected = p;
  }

  pickCandidate(id: string): void {
    this.selected = this.candidateDisplayMap[id] || { id, name: id, gender: '', birthDate: '', age: null };
  }

  confirm(): void {
    if (!this.selected) return;
    this.fhir.saveSelection(this.state, { patientId: this.selected.id });
    this.router.navigate(['/smart/consent'], { queryParams: { state: this.state } });
  }

  /** 受限模式：把每個候選 ID 對應的 Patient resource 抓回來 */
  private preloadCandidates(): void {
    this.candidates.forEach(id => {
      this.fhir.getPatientById(this.websiteName, this.fhirBasePath, id).subscribe({
        next: (resource) => {
          if (resource?.resourceType === 'Patient') {
            this.candidateDisplayMap[id] = this.fhir.extractPatientDisplay(resource);
          }
        },
        error: () => {
          this.candidateDisplayMap[id] = { id, name: id, gender: '', birthDate: '', age: null };
        },
      });
    });
  }

  get isRestricted(): boolean {
    return this.candidates.length > 0;
  }
}

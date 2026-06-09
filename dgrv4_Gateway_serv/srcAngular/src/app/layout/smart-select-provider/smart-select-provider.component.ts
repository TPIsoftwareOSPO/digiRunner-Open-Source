import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SmartFhirClientService, ConsentInfo, ProviderDisplay } from '../smart-shared/smart-fhir-client.service';

type ViewState = 'loading' | 'ready' | 'no-state' | 'error';

@Component({
  selector: 'app-smart-select-provider',
  templateUrl: './smart-select-provider.component.html',
  styleUrls: ['./smart-select-provider.component.scss'],
  standalone: false,
})
export class SmartSelectProviderComponent implements OnInit {

  state: string = '';
  viewState: ViewState = 'loading';

  clientName: string = '';
  websiteName: string = '';
  fhirBasePath: string = '';

  /** 候選清單，FHIR Reference 格式（Practitioner/{id}） */
  candidates: string[] = [];
  /** reference → display 對應表（受限模式預載；全域搜尋逐步填入） */
  displayMap: Record<string, ProviderDisplay> = {};

  /** 受限模式：使用者勾選的 reference */
  selectedRef: string = '';

  // 全域搜尋模式（候選 = 0）
  searchMode: 'name' | 'id' = 'name';
  keyword: string = '';
  results: ProviderDisplay[] = [];
  loading: boolean = false;
  page: number = 1;
  nextUrl: string | null = null;
  prevUrl: string | null = null;
  readonly pageSize: number = 20;

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

      this.fhir.getConsentInfo(state).subscribe({
        next: (info: ConsentInfo) => {
          if (!info.needsProviderSelection) {
            this.router.navigate(['/smart/consent'], { queryParams: { state } });
            return;
          }
          this.clientName = info.clientName;
          this.websiteName = info.websiteName ?? '';
          this.fhirBasePath = info.fhirBasePath ?? '';
          this.candidates = info.providerCandidates ?? [];
          this.viewState = 'ready';
          // 預載：把 sessionStorage 中已選的 fhirUser 帶入 selectedRef
          const sel = this.fhir.loadSelection(state);
          if (sel.fhirUser) {
            this.selectedRef = sel.fhirUser;
          }

          if (this.candidates.length > 0) {
            this.preloadCandidates();
          } else {
            // 全域搜尋模式：自動觸發第一次搜尋
            this.search();
          }
        },
        error: () => this.viewState = 'error',
      });
    });
  }

  /** 受限模式（候選 2+）：預載候選顯示名稱 */
  private preloadCandidates(): void {
    this.candidates.forEach(reference => {
      const id = reference.startsWith('Practitioner/')
        ? reference.substring('Practitioner/'.length)
        : reference;
      this.fhir.getPractitionerById(this.websiteName, this.fhirBasePath, id).subscribe({
        next: (resource) => {
          if (resource?.resourceType === 'Practitioner') {
            this.displayMap[reference] = this.fhir.extractProviderDisplay(resource);
          } else {
            this.displayMap[reference] = { id, name: id, reference };
          }
        },
        error: () => {
          this.displayMap[reference] = { id, name: id, reference };
        },
      });
    });
  }

  /** 全域搜尋模式（候選 0） */
  search(): void {
    this.page = 1;
    this.nextUrl = null;
    this.prevUrl = null;
    this.loading = true;
    this.fhir.searchPractitioners(this.websiteName, this.fhirBasePath, this.keyword, this.searchMode, this.pageSize, 0)
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
    this.results = entries
      .filter((e: any) => e.resource?.resourceType === 'Practitioner')
      .map((e: any) => this.fhir.extractProviderDisplay(e.resource));
    this.nextUrl = SmartFhirClientService.nextLink(bundle);
    this.prevUrl = SmartFhirClientService.prevLink(bundle);
    this.loading = false;
  }

  get hasNextPage(): boolean { return !!this.nextUrl; }
  get hasPrevPage(): boolean { return !!this.prevUrl; }

  pickCandidate(reference: string): void {
    this.selectedRef = reference;
  }

  pickResult(p: ProviderDisplay): void {
    this.selectedRef = p.reference;
    this.displayMap[p.reference] = p;
  }

  getLabel(reference: string): string {
    const d = this.displayMap[reference];
    return d ? `${d.name} (${d.id})` : reference;
  }

  get isRestricted(): boolean {
    return this.candidates.length > 0;
  }

  confirm(): void {
    if (!this.selectedRef) return;
    this.fhir.saveSelection(this.state, { fhirUser: this.selectedRef });
    this.router.navigate(['/smart/consent'], { queryParams: { state: this.state } });
  }
}

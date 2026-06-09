import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { SmartFhirService, PatientDisplay, PractitionerDisplay, EncounterDisplay } from '../smart-fhir.service';
import { Patient, Practitioner, Encounter } from '../smart-fhir.types';

@Component({
  selector: 'app-ehr-sim',
  templateUrl: './ehr-sim.component.html',
  styleUrls: ['./ehr-sim.component.scss'],
  standalone: false,
})
export class EhrSimComponent implements OnInit, OnDestroy {

  appUrl: SafeResourceUrl | null = null;
  rawAppUrl: string = '';

  patientId: string = '';
  providerId: string = '';
  encounterId: string = '';
  proxyName: string = '';
  launchToken: string = '';

  patient: PatientDisplay | null = null;
  provider: PractitionerDisplay | null = null;
  encounter: EncounterDisplay | null = null;

  launchContextJson: string = '';
  appMessages: string[] = [];

  private messageHandler: ((event: MessageEvent) => void) | null = null;

  constructor(
    private route: ActivatedRoute,
    private sanitizer: DomSanitizer,
    private fhirService: SmartFhirService,
  ) {}

  ngOnInit(): void {
    const p = this.route.snapshot.queryParams;
    this.proxyName = p['proxy'] || '';
    this.patientId = p['patient'] || '';
    this.providerId = p['provider'] || '';
    this.encounterId = p['encounter'] || '';
    this.launchToken = p['launch'] || '';

    const appLaunchUrl = p['app'] || '';
    const iss = p['iss'] || '';
    const simError = p['sim_error'] || '';

    if (appLaunchUrl && iss && this.launchToken) {
      const sep = appLaunchUrl.includes('?') ? '&' : '?';
      let url = `${appLaunchUrl}${sep}iss=${encodeURIComponent(iss)}&launch=${encodeURIComponent(this.launchToken)}`;
      if (simError) url += `&sim_error=${encodeURIComponent(simError)}`;
      this.rawAppUrl = url;
      this.appUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.rawAppUrl);
    }

    this.launchContextJson = JSON.stringify({
      patient: this.patientId || undefined,
      encounter: this.encounterId || undefined,
      provider: this.providerId || undefined,
      launch: this.launchToken || undefined,
    }, null, 2);

    this.loadContextDetails();
    this.listenForMessages();
  }

  ngOnDestroy(): void {
    if (this.messageHandler) {
      window.removeEventListener('message', this.messageHandler);
    }
  }

  private loadContextDetails(): void {
    if (!this.proxyName) return;

    if (this.patientId) {
      this.fhirService.fetchByUrl(`/smart-on-fhir/${this.proxyName}/fhir/Patient/${this.patientId}`).subscribe({
        next: (res) => {
          if (res.resourceType === 'Patient') {
            this.patient = SmartFhirService.extractPatientDisplay(res as Patient);
          }
        },
        error: () => {},
      });
    }

    if (this.providerId) {
      this.fhirService.fetchByUrl(`/smart-on-fhir/${this.proxyName}/fhir/Practitioner/${this.providerId}`).subscribe({
        next: (res) => {
          if (res.resourceType === 'Practitioner') {
            this.provider = SmartFhirService.extractPractitionerDisplay(res as Practitioner);
          }
        },
        error: () => {},
      });
    }

    if (this.encounterId) {
      this.fhirService.fetchByUrl(`/smart-on-fhir/${this.proxyName}/fhir/Encounter/${this.encounterId}`).subscribe({
        next: (res) => {
          if (res.resourceType === 'Encounter') {
            this.encounter = SmartFhirService.extractEncounterDisplay(res as Encounter);
          }
        },
        error: () => {},
      });
    }
  }

  private listenForMessages(): void {
    this.messageHandler = (event: MessageEvent) => {
      if (event.data && typeof event.data === 'object') {
        this.appMessages.push(JSON.stringify(event.data, null, 2));
      }
    };
    window.addEventListener('message', this.messageHandler);
  }
}

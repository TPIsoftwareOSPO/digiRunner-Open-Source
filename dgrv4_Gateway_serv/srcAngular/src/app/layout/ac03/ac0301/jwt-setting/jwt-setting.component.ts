import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Subscription } from 'rxjs';
import { ToolService } from 'src/app/shared/services/tool.service';

@Component({
  selector: 'app-jwt-setting',
  templateUrl: './jwt-setting.component.html',
  styleUrls: ['./jwt-setting.component.css'],
})
export class JwtSettingComponent implements OnInit {
  form: FormGroup;
  updateJwtSettingFlags: { label: string; value: string }[] = [];

  jweFlagRef?: Subscription;
  jweFlagRespRef?: Subscription;

  constructor(
    private toolService: ToolService,
    // private translateService: TranslateService,
    private ref: DynamicDialogRef,
    private config: DynamicDialogConfig,
    private fb: FormBuilder
  ) {
    this.form = this.fb.group({
      jwtSetting: new FormControl('N'),
      jweFlag: new FormControl({ value: '0', disabled: true }),
      jweFlagResp: new FormControl({ value: '0', disabled: true }),
    });

    this.form.get('jwtSetting')?.valueChanges.subscribe((value) => {
      this.toggleJweFlagControls(value === 'Y');
    });
  }

  private toggleJweFlagControls(enable: boolean): void {
    if (enable) {
      this.form.get('jweFlag')?.enable();
      this.form.get('jweFlag')?.setValue('0', { emitEvent: false });
      this.form.get('jweFlagResp')?.enable();
      this.form.get('jweFlagResp')?.setValue('1', { emitEvent: false });
      this.jweFlagRef = this.form
        .get('jweFlag')
        ?.valueChanges.subscribe((value) => {
          this.checkAndSetJwtSettingN();
        });

      this.jweFlagRespRef = this.form
        .get('jweFlagResp')
        ?.valueChanges.subscribe((value) => {
          this.checkAndSetJwtSettingN();
        });
    } else {
      this.form.get('jweFlag')?.disable({ emitEvent: false });
      this.form.get('jweFlagResp')?.disable({ emitEvent: false });
      this.form.patchValue(
        { jweFlag: '0', jweFlagResp: '0' },
        { emitEvent: false }
      );
    }
  }

  private checkAndSetJwtSettingN(): void {
    if (
      this.form.get('jweFlag')?.value === '0' &&
      this.form.get('jweFlagResp')?.value === '0'
    ) {
      this.jweFlagRef?.unsubscribe();
      this.jweFlagRef = undefined;
      this.jweFlagRespRef?.unsubscribe();
      this.jweFlagRespRef = undefined;
      this.form.get('jwtSetting')?.setValue('N');
    }
  }

  ngOnInit(): void {
    if (this.config.data.updateJwtSettingFlags)
      this.updateJwtSettingFlags = this.config.data.updateJwtSettingFlags;
  }

  confirm() {
    this.ref.close({
      jweFlag: this.form.get('jweFlag')?.value,
      jweFlagResp: this.form.get('jweFlagResp')?.value,
    });
  }

  cancel() {
    this.ref.close(null);
  }
}

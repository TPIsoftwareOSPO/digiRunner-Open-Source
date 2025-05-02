import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnInit,
} from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { ListService } from 'src/app/shared/services/api-list.service';
import { ToolService } from 'src/app/shared/services/tool.service';

@Component({
  selector: 'app-api-status-modify',
  templateUrl: './api-status-modify.component.html',
  styleUrls: ['./api-status-modify.component.css'],
})
export class ApiStatusModifyComponent implements OnInit {
  @Input() type: string = '';
  @Input() rowData: any;

  apiStateLabel: string = '';

  enableFlagList: { label: string; value: string }[] = [];
  form: FormGroup;
  minDate: Date = new Date();

  constructor(
    private toolService: ToolService,
    private translateService: TranslateService,
    private ref: DynamicDialogRef,
    private config: DynamicDialogConfig,
    private list: ListService,
    private fb: FormBuilder
  ) {
    this.form = this.fb.group({
      modifyDate: new FormControl(''),
      apiStatus: new FormControl(''),
      revokeFlag: new FormControl(''),
    });
  }

  async ngOnInit() {
    this.apiStatus.setValue(this.config.data.status);
    if (this.config.data.revokeFlag)
      this.revokeFlag.setValue(this.config.data.revokeFlag);
    this.minDate.setDate(this.minDate.getDate() + 1);
    if (this.apiStatus.value != 'c') {
      this.list
        .querySubItemsByItemNo({
          encodeItemNo:
            this.toolService.Base64Encoder(
              this.toolService.BcryptEncoder('ENABLE_FLAG')
            ) +
            ',' +
            9,
          isDefault: 'N',
        })
        .subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.translateService
              .get(['button.enable', 'button.disable'])
              .subscribe((dict) => {
                this.enableFlagList = res.RespBody.subItems!.filter(
                  (item) => (item.subitemNo == '0' || item.subitemNo == '1') //啟用 停用
                ).map((item) => {
                  return {
                    label: item.subitemNo == '1'? dict['button.enable']: dict['button.disable'],
                    // label: item.subitemNo,
                    value: item.param1!,
                  };
                });
              });
          }
        });
    } else {
      this.list
        .querySubItemsByItemNo({
          encodeItemNo:
            this.toolService.Base64Encoder(
              this.toolService.BcryptEncoder('DGR_API_REVOKE_TYPE')
            ) +
            ',' +
            66,
          isDefault: 'N',
        })
        .subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.enableFlagList = res.RespBody.subItems!.filter(
              (item) => item.subitemNo != '0' && item.subitemNo != '-1' //移除停用及全部
            ).map((item) => {
              return {
                label: item.subitemName,
                value: item.subitemNo,
              };
            });
          }
        });
    }
  }

  confirm() {
    if (this.apiStatus.value != 'c') {
      this.ref.close({
        modifyDate: this.modifyDate.value,
        apiStatus: this.apiStatus.value,
      });
    } else {
      this.ref.close({
        revokeFlag:
          this.toolService.Base64Encoder(
            this.toolService.BcryptEncoder(this.revokeFlag!.value)
          ) +
          ',' +
          this.enableFlagList.findIndex(
            (item) => item.value == this.revokeFlag!.value
          ),
      });
    }
  }
  cancel() {
    this.ref.close();
  }

  public get modifyDate() {
    return this.form.get('modifyDate')!;
  }

  public get apiStatus() {
    return this.form.get('apiStatus')!;
  }

  public get revokeFlag() {
    return this.form.get('revokeFlag')!;
  }
}

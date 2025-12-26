import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ConfirmationService } from 'primeng/api';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { AA0423RespItem } from 'src/app/models/api/ApiService/aa0423.interface';
import { AA0432Req } from 'src/app/models/api/ApiService/aa0432.interfcae';
import { ToolService } from 'src/app/shared/services/tool.service';

@Component({
  selector: 'app-fail-handle-policy',
  templateUrl: './fail-handle-policy.component.html',
  styleUrls: ['./fail-handle-policy.component.css'],
  providers: [ConfirmationService]
})
export class FailHandlePolicyComponent implements OnInit {
  selected: Array<AA0423RespItem> = [];
  form!: FormGroup;

  constructor(
    private ref: DynamicDialogRef,
    private config: DynamicDialogConfig,
    private fb: FormBuilder,
    private confirmationService: ConfirmationService,
    private toolService: ToolService,
  ) {}

  ngOnInit(): void {
    this.selected = this.config?.data?.data ? this.config?.data?.data : [];
    this.form = this.fb.group({
      failDiscoveryPolicy: new FormControl("0"),
      failHandlePolicy: new FormControl("0"),
    })
  }

  async chooseRole() {
    const codes = ['fail_handle_policy.reset', 'fail_handle_policy.cfm_reset'];
    const dict = await this.toolService.getDict(codes);
    this.confirmationService.confirm({
      // key: 'cd',
      header: dict['fail_handle_policy.reset'],
      message: dict['fail_handle_policy.cfm_reset'],
      accept: () => {

        let req = {
          apiList:this.selected.map(item=>{
            return {
              apiKey: item.apiKey,
              moduleName: item.moduleName
            }
          }),
          failDiscoveryPolicy: this.form.get("failDiscoveryPolicy")!.value,
          failHandlePolicy: this.form.get("failHandlePolicy")!.value,

        } as AA0432Req;
        this.ref.close(req);
      },
    });



  }

  cancelRole() {
    this.ref.close(null);
  }
}

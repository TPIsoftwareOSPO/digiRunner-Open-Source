import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { AA0202List } from 'src/app/models/api/ClientService/aa0202.interface';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { SmartOnFhirProxyStickyDto } from 'src/app/models/api/ServerService/dpb0317.interface';
import { ClientService } from 'src/app/shared/services/api-client.service';
import { ListService } from 'src/app/shared/services/api-list.service';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import * as ValidatorFns from '../../../../shared/validator-functions';
import { BaseComponent } from 'src/app/layout/base-component';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute } from '@angular/router';
import { SmartOnFhirProxyDto } from 'src/app/models/api/ServerService/dpb0310.interface';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-sticky-list',
  templateUrl: './sticky-list.component.html',
  styleUrls: ['./sticky-list.component.css'],
  standalone: false,
})
export class StickyListComponent extends BaseComponent implements OnInit {
  form!: FormGroup;
  formE!: FormGroup;

  stickyData: SmartOnFhirProxyStickyDto[] = [];
  selectedStickyData: SmartOnFhirProxyStickyDto[] = [];
  pageNum: number = 1;
  diversionData: { label: string; value: string }[] = [];
  currentAction: string = '';
  sofDto?: SmartOnFhirProxyDto;
  currentDto?: SmartOnFhirProxyStickyDto;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private clientService: ClientService,
    private ref: DynamicDialogRef,
    private config: DynamicDialogConfig,
    private toolService: ToolService,
    private listService: ListService,
    private fb: FormBuilder,
    private translate: TranslateService,
    private serverService: ServerService,
    private messageService: MessageService,
  ) {
    super(route, tr);
  }

  ngOnInit() {
    this.form = this.fb.group({
      sofProxyDiversionId: new FormControl(''),
      sofProxyStickyType: new FormControl(''),
    });
    this.formE = this.fb.group({
      sofProxyDiversionId: new FormControl(
        '',
        ValidatorFns.requiredValidator(),
      ),
      sofProxyStickyType: new FormControl('', ValidatorFns.requiredValidator()),
    });

    this.serverService
      .queryStickyList({
        sofProxyId: this.config.data.data.sofProxyId,
      })
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.stickyData = res.RespBody.content;
        }
      });

    if (this.config.data) {
      this.sofDto = this.config.data.data;
      this.diversionData = this.config.data.data.diversionList.map((row) => {
        return {
          label: row.sofProxyDiversionUrl,
          value: row.sofProxyDiversionId,
        };
      });
    }
  }

  queryStickyList() {
    this.serverService
      .queryStickyList({
        sofProxyId: this.config.data.data.sofProxyId,
        sofProxyDiversionId: this.sofProxyDiversionId!.value,
        sofProxyStickyType: this.sofProxyStickyType!.value,
      })
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.stickyData = res.RespBody.content;
        } else this.stickyData = [];
      });
  }

  changePage(action: string, rowData?: SmartOnFhirProxyStickyDto) {
    this.resetFormValidator(this.formE);
    this.currentAction = action;
    this.selectedStickyData = [];
    this.currentDto = undefined;
    switch (action) {
      case 'query':
        this.pageNum = 1;
        break;
      case 'create':
        this.sofProxyDiversionIdE?.setValidators(
          ValidatorFns.requiredValidator(),
        );
        this.sofProxyStickyTypeE?.setValidators(
          ValidatorFns.requiredValidator(),
        );
        this.formE.reset('');
        this.formE?.updateValueAndValidity();
        this.pageNum = 2;
        break;
      case 'update':
        this.sofProxyDiversionIdE?.setValidators(
          ValidatorFns.requiredValidator(),
        );
        this.sofProxyStickyTypeE?.setValidators(
          ValidatorFns.requiredValidator(),
        );
        this.sofProxyDiversionIdE?.setValue(rowData?.sofProxyDiversionId);
        this.sofProxyStickyTypeE?.setValue(rowData?.sofProxyStickyType);
        this.formE?.updateValueAndValidity();
        this.currentDto = rowData;
        this.pageNum = 2;
        break;
    }
  }

  addSticky() {
    this.serverService
      .addSticky({
        sofProxyId: this.config.data.data.sofProxyId,
        sofProxyDiversionId: this.sofProxyDiversionIdE?.value,
        sofProxyStickyType: this.sofProxyStickyTypeE?.value,
      })
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.config.data.showMessage(
            this.translate.instant('message.create'),
            this.translate.instant('message.success'),
          );
          this.queryStickyList();
          this.changePage('query');
        }
      });
  }

  batchDeleteSticky() {
    this.serverService
      .batchDeleteSticky({
        stickyList: this.selectedStickyData.map((row) => {
          return {
            sofProxyStickyId: row.sofProxyStickyId,
            version: row.version,
          };
        }),
      })
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.config.data.showMessage(
            this.translate.instant('message.delete'),
            this.translate.instant('message.success'),
          );
          this.queryStickyList();
          this.changePage('query');
        }
      });
  }

  batchUpdateSticky() {
    this.serverService
      .batchUpdateSticky({
        stickyList: [
          {
            sofProxyStickyId: this.currentDto!.sofProxyStickyId,
            version: this.currentDto!.version,
            sofProxyDiversionId: this.sofProxyDiversionIdE?.value,
            sofProxyStickyType: this.sofProxyStickyTypeE?.value,
          },
        ],
      })
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.config.data.showMessage(
            this.translate.instant('message.update'),
            this.translate.instant('message.success'),
          );
          this.queryStickyList();
          this.changePage('query');
        }
      });
  }

  chooseClient() {
    this.ref.close();
  }
  cancelClient() {
    this.ref.close(null);
  }

  public get sofProxyDiversionId() {
    return this.form.get('sofProxyDiversionId');
  }

  public get sofProxyStickyType() {
    return this.form.get('sofProxyStickyType');
  }

  public get sofProxyDiversionIdE() {
    return this.formE.get('sofProxyDiversionId');
  }

  public get sofProxyStickyTypeE() {
    return this.formE.get('sofProxyStickyType');
  }
}

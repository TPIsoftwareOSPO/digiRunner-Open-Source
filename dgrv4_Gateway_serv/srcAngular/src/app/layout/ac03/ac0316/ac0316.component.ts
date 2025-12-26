import { DynamicDialogRef, DynamicDialogConfig } from 'primeng/dynamicdialog';
import { BaseComponent } from 'src/app/layout/base-component';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { CommonAPI } from '../../../shared/register-api/common-api.class';
import { KeyValueComponent } from '../../../shared/key-value/key-value.module';
import { TokenService } from 'src/app/shared/services/api-token.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { AlertService } from 'src/app/shared/services/alert.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import * as shajs from 'sha.js'
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { GrantType } from 'src/app/models/common.enum';
import { ResToken } from '../../../models/api/TokenService/token.interface';
import { ApiService } from 'src/app/shared/services/api-api.service';
import { SignBlockService } from 'src/app/shared/services/sign-block.service';
// import { ModuleService } from 'src/app/shared/services/api-module.service';
// import { DCService } from 'src/app/shared/services/api-dc.service';
import { AA0510Resp } from 'src/app/models/api/UtilService/aa0510.interface';
import { AA0302Req } from 'src/app/models/api/ApiService/aa0302_v3.interface';
import { AA0422Req } from 'src/app/models/api/DCService/aa0422.interface';
import { FormParam, HttpHeader } from 'src/app/models/api/ApiService/aa0312_v3.interface';
import { ModuleService } from 'src/app/shared/services/api-module.service';
import { DCService } from 'src/app/shared/services/api-dc.service';

@Component({
  selector: 'app-ac0316',
  templateUrl: './ac0316.component.html',
  styleUrls: ['./ac0316.component.css'],
  providers: [ApiService, ModuleService, DCService]
})
export class Ac0316Component extends BaseComponent implements OnInit {

  @ViewChild('keyValueForm') kv_form!: KeyValueComponent;
  @ViewChild('keyValueRequest') kv_request!: KeyValueComponent;

  apiKey: string = '';
  moduleName: string = '';
  apiSrc: string = '';
  form: FormGroup;
  methods: { label: string; value: string; }[] = [];
  dcs: { label: string; value: string; }[] = [];
  sites: { label: string; value: string; }[] = [];
  resTime: string = "";
  funCode: string = '';
  protocol: string = '';
  acConf: AA0510Resp;

  initData:{[key:string]:string} = {};

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private tokenService: TokenService,
    private toolService: ToolService,
    private api: ApiService,
    private httpClient: HttpClient,
    private ngxService: NgxUiLoaderService,
    private signBlockService: SignBlockService,
    private moduleService: ModuleService,
    private alert: AlertService,
    private tool: ToolService,
    private dc: DCService,

  ) {
    super(route, tr);
    this.acConf = this.tool.getAcConf();

    this.form = this.fb.group({
      enableClientAuth: new FormControl(true),
      enableUserAuth: new FormControl(false),
      userName: new FormControl({ value: '', disabled: true }),
      passwd: new FormControl({ value: '', disabled: true }),
      clientId: new FormControl(""),
      clientPasswd: new FormControl(""),
      tsmpUrl: new FormControl(""),
      method: new FormControl('POST'),
      requestHeader: new FormControl,
      ContentType: new FormControl("application/json"),
      requestBody: new FormControl('none'),
      keyValueRequest: new FormControl(""),
      keyValueForm: new FormControl(""),
      bodyText: new FormControl(""),
      resBody: new FormControl({ value: '', disabled: false }),
      resStatus: new FormControl({ value: '', disabled: false }),
      headerList: new FormControl({ value: '', disabled: false }),
      dc: new FormControl(''),
      siteCode: new FormControl(''),
      baseUrl: new FormControl(location.hostname + (location.port ? ':' + location.port : ''))
    });
  }

  ngOnInit() {
    this.route.params.subscribe((value) => {
      this.apiKey = value["apiKey"] == undefined ? "" : value["apiKey"];
      this.moduleName = value["moduleName"] == undefined ? "" : value["moduleName"];
      this.apiSrc = value["apiSrc"] == undefined ? "" : value["apiSrc"];
      this.initData = {
        'apiKey': this.apiKey,
        'moduleName' : this.moduleName,
        'apiSrc': this.apiSrc
      }
    });

  }
}

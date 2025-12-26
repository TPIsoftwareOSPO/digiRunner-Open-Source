import { DialogService, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Component, OnInit, ViewChild, Output, EventEmitter, Input, ChangeDetectionStrategy } from '@angular/core';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import * as ValidatorFns from 'src/app/shared/validator-functions';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { ThemeLovComponent } from '../theme-lov/theme-lov.component';
import { FormOperate } from 'src/app/models/common.enum';
import { DPB0055Themes } from 'src/app/models/api/ThemeService/dpb0055.interface';
import { DPB0075RespItem, DPB0075Req } from 'src/app/models/api/LovService/dpb0075.interface';
import { ApiLovComponent } from '../api-lov/api-lov.component';
import { newApiOnOff, newApiOnOffAddNo } from 'src/app/models/api/RequisitionService/dpb0065.interface';
import { LovService } from 'src/app/shared/services/api-lov.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { AlertService } from 'src/app/shared/services/alert.service';
import { FileService } from 'src/app/shared/services/api-file.service';
import { MessageService } from 'primeng/api';
import { DPB0076Req } from 'src/app/models/api/LovService/dpb0076.interface';

@Component({
  selector: 'app-api-shelves',
  templateUrl: './api-shelves.component.html',
  styleUrls: ['./api-shelves.component.css'],
  providers: [MessageService, DynamicDialogRef],
  // changeDetection: ChangeDetectionStrategy.OnPush
})
export class ApiShelvesComponent implements OnInit {

  @ViewChild('dialog') _dialog!: DialogComponent;
  @Input() _ref: any;
  @Input() no?: number;
  @Input() data?: newApiOnOff;
  @Input() subType?: string;
  @Output() change: EventEmitter<newApiOnOffAddNo> = new EventEmitter;
  @Output() remove: EventEmitter<number> = new EventEmitter;
  @Output() isInvalid: EventEmitter<boolean> = new EventEmitter;

  form!: FormGroup;
  dialogTitle: string = '';
  selectedApis?: DPB0075RespItem;
  selectedThemes: Array<DPB0055Themes> = new Array();

  constructor(
    private fb: FormBuilder,
    private lov: LovService,
    private tool: ToolService,
    private alert: AlertService,
    private file: FileService,
    private message: MessageService,
    private dialogService: DialogService,

  ) {

  }

  ngOnInit() {

    this.form = this.fb.group({
      apiUid: new FormControl(this.data ? this.data.apiUid : ''),
      apiName: new FormControl(this.data ? this.data.apiName : '', ValidatorFns.requiredValidator()),
      themeId: new FormControl(this.data ? this.data.refThemeId : []),
      themeName: new FormControl(this.data ? this.data.refThemeName : [], ValidatorFns.requiredValidator()),
      fileName: new FormControl({ value: this.data ? this.data.fileName : null, disabled: true }),
      tempFileName: new FormControl({ value: this.data ? this.data.tempFileName : null, disabled: true })
    });

    this.form.valueChanges.subscribe((res: newApiOnOff) => {
      this.isInvalid.emit(this.form.invalid)
      this.change.emit({ no: this.no, apiUid: this.apiUid!.value, refThemeId: this.themeId!.value, fileName: this.fileName!.value, tempFileName: this.tempFileName!.value } as newApiOnOffAddNo);
    });
  }

  searchAPI() {
    let ReqBody = {
      dpStatus: '0'
    } as DPB0075Req;
    if (this.data && window.location.hash == '#/np04/np0401') {
      ReqBody.keyword = this.data.apiName;
      let _themeDatas = {};
      for (let i = 0; i < this.data.refThemeId.length; i++) {
        _themeDatas[this.data.refThemeId[i]] = this.data.refThemeName[i];
      }
      this.selectedApis = {
        apiUid: this.data.apiUid,
        apiName: this.data.apiName,
        fileName: this.data.fileName!,
        themeDatas: _themeDatas
      }
    }
    this.lov.queryApiLov(ReqBody).subscribe(async res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        const code = ['un_shelves_api_search'];
        const dict = await this.tool.getDict(code);
        this.dialogTitle = dict['un_shelves_api_search'];
        // let data: FormParams = {
        //   displayInDialog: true,
        //   data: { dpStatus: '0', selectedApis: this.selectedApis, apiList: res.RespBody.dataList, selectionMode: 'single', keyword: ReqBody.keyword },
        //   afterCloseCallback: (_chooseAPI) => {
        //     if (this.data && window.location.hash == '#/np04/np0401') {
        //       this.data.apiUid = _chooseAPI.apiUid;
        //       this.data.apiName = _chooseAPI.apiName;
        //     }
        //     this.selectedApis = _chooseAPI;
        //     this.apiUid!.setValue(this.selectedApis?.apiUid);
        //     this.apiName!.setValue(this.selectedApis?.apiName);
        //   }
        // }
        // this._dialog.open(ApiLovComponent, data);

        const ref = this.dialogService.open(ApiLovComponent, {
          data: {
            displayInDialog: true,
            data: { dpStatus: '0', selectedApis: this.selectedApis, apiList: res.RespBody.dataList, selectionMode: 'single', keyword: ReqBody.keyword },
          },
          width: '80vw',
          height: '100vh',
          header: dict['un_shelves_api_search'],
        })

        ref.onClose.subscribe(_chooseAPI => {
          if (_chooseAPI) {
            if (this.data && window.location.hash == '#/np04/np0401') {
              this.data.apiUid = _chooseAPI.apiUid;
              this.data.apiName = _chooseAPI.apiName;
            }
            this.selectedApis = _chooseAPI;
            this.apiUid!.setValue(this.selectedApis?.apiUid);
            this.apiName!.setValue(this.selectedApis?.apiName);
          }
        });

      }
    });
  }

  searchTheme() {
    let ReqBody = {} as DPB0076Req;
    if (this.data && window.location.hash == '#/np04/np0401') {
      this.selectedThemes = [];
      for (let i = 0; i < this.data.refThemeId.length; i++) {
        this.selectedThemes.push({ themeId: this.data.refThemeId[i], themeName: this.data.refThemeName[i] });
      }
      let _themeName = '';
      for (let theme of this.data.refThemeName) {
        _themeName += `${theme} `;
      }
      ReqBody.keyword = _themeName;
    }
    this.lov.queryThemeLov(ReqBody).subscribe(async res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        const code = ['theme_list'];
        const dict = await this.tool.getDict(code);
        this.dialogTitle = dict['theme_list'];
        // let data: FormParams = {
        //   operate: this.data ? FormOperate.update : FormOperate.create,
        //   displayInDialog: true,
        //   data: { selectedThemes: this.selectedThemes, dataList: res.RespBody.dataList, keyword: ReqBody.keyword },
        // afterCloseCallback: (_chooseThemes) => {

        //     this.selectedThemes = _chooseThemes;
        //     let _refThemeId:any[] = [];
        //     let _themeNames:any[] = [];
        //     for (let theme of this.selectedThemes) {
        //         _refThemeId.push(theme.themeId);
        //         _themeNames.push(theme.themeName);
        //     }
        //     if (this.data && window.location.hash == '#/np04/np0401') {
        //         this.data.refThemeId = _refThemeId;
        //         this.data.refThemeName = _themeNames;
        //     }
        //     this.themeId!.setValue(_refThemeId);
        //     this.themeName!.setValue(_themeNames);
        // }
        // }
        // this._dialog.open(ThemeLovComponent, data);

        const ref = this.dialogService.open(ThemeLovComponent, {
          data: {
            operate: this.data ? FormOperate.update : FormOperate.create,
            displayInDialog: true,
            data: { selectedThemes: this.selectedThemes, dataList: res.RespBody.dataList, keyword: ReqBody.keyword },
          },
          width: '50vw',
          height: '100vh',
          header: dict['theme_list'],
        })

        ref.onClose.subscribe(_chooseThemes => {
          if (_chooseThemes) {
            this.selectedThemes = _chooseThemes;
            let _refThemeId: any[] = [];
            let _themeNames: any[] = [];

            for (let theme of this.selectedThemes) {
              _refThemeId.push(theme.themeId);
              _themeNames.push(theme.themeName);
            }
            if (this.data && window.location.hash == '#/np04/np0401') {
              this.data.refThemeId = _refThemeId;
              this.data.refThemeName = _themeNames;
            }
            this.themeId!.setValue(_refThemeId);
            this.themeName!.setValue(_themeNames);
          }
        });
      }
    });
  }

  openFileBrowser() {
    $('#choose-file_' + this.no).click();
  }

  // async upload(event) {
  //   let file = event.target.files[0];
  //   if (file == undefined) {
  //     this.fileName!.setValue(null);
  //     this.tempFileName!.setValue(null);
  //     return;
  //   }
  //   const code = ['uploading', 'waiting', 'cfm_size', 'upload_result', 'message.success'];
  //   const dict = await this.tool.getDict(code);
  //   this.message.add({ severity: 'success', summary: dict['uploading'], detail: `${dict['waiting']}!` });
  //   let _fileName = file.name;
  //   if (file.size / 1024 / 1024 > 5) {
  //     // alert 超過 5MB
  //     this.message.clear();
  //     this.alert.ok('Return message : ', dict['cfm_size'], undefined);
  //     return;
  //   }
  //   let fileReader = new FileReader();
  //   fileReader.onloadend = () => {
  //     this.file.uploadFile2(file).subscribe(res => {
  //       if (this.tool.checkDpSuccess(res.ResHeader)) {
  //         this.message.add({ severity: 'success', summary: dict['upload_result'], detail: `${dict['message.success']}!` });
  //         this.fileName!.setValue(_fileName);
  //         this.tempFileName!.setValue(res.RespBody.tempFileName);
  //       }
  //     });
  //   }
  //   fileReader.readAsBinaryString(file);
  // }

  delete() {
    this._ref.destroy();
    this.remove.emit(this.no);
  }

  public get apiUid() { return this.form.get('apiUid'); };
  public get apiName() { return this.form.get('apiName'); };
  public get themeId() { return this.form.get('themeId'); };
  public get themeName() { return this.form.get('themeName'); };
  public get fileName() { return this.form.get('fileName'); };
  public get tempFileName() { return this.form.get('tempFileName'); };
}

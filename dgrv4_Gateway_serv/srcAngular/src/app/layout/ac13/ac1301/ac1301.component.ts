import { TranslateService } from '@ngx-translate/core';
import { ReportService } from 'src/app/shared/services/api-report.service';
import { ActivatedRoute } from '@angular/router';
import { BaseComponent } from 'src/app/layout/base-component';
import { Component, OnInit } from '@angular/core';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
// import * as chartjs from 'chart.js';
import { Chart } from 'chart.js/auto'
import { ListService } from 'src/app/shared/services/api-list.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { AA1201Req } from 'src/app/models/api/ReportService/aa1201.interface';
import * as dayjs from 'dayjs';

import { ApiService } from 'src/app/shared/services/api-api.service';
import { MessageService } from 'primeng/api';
import { AA0510Resp } from 'src/app/models/api/UtilService/aa0510.interface';
import { AlertService } from 'src/app/shared/services/alert.service';
import { AlertType } from "src/app/models/common.enum";
import { AA0321Req, AA0321RespItem } from 'src/app/models/api/ApiService/aa0321.interface';

@Component({
    selector: 'app-ac1301',
    templateUrl: './ac1301.component.html',
    styleUrls: ['./ac1301.component.css'],
    providers:[ApiService]

})
export class Ac1301Component extends BaseComponent implements OnInit {
    canvas: any;
    ctx: any;
    timeTypes: { label: string; value: string }[] = [];
    form!: FormGroup;
    pageNum: number = 1;
    currentTitle: string = this.title;
    apiListCols: { field: string; header: string }[] = [];
    apiListData: Array<AA0321RespItem> = [];
    apiListRowcount: number = 0;
    selectedApi: Array<AA0321RespItem> = new Array<AA0321RespItem>();
    today: Date = new Date();
    minDate: Date = new Date();
    maxDate: Date = new Date();
    // displayStartDate: string = '';
    // displayEndDate: string = '';
    acConf?: AA0510Resp;

    hourData: { label: string, value: string }[] = [];

    // chart:any;



    constructor(
        router: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private list: ListService,
        private toolService: ToolService,
        private fb: FormBuilder,
        private reportService: ReportService,
        private apiService: ApiService,
        private message: MessageService,
        private alertService: AlertService,
        private translate:TranslateService
    ) {
        super(router, tr);
    }

    ngOnInit() {
        this.acConf = this.toolService.getAcConf();
        this.canvas = document.getElementById('reportChart');
        this.ctx = this.canvas.getContext('2d');
        this.form = this.fb.group({
            timeType: new FormControl('DAY'),
            apiUidList: new FormControl([]),
            apiNameList: new FormControl([]),
            startDate: new FormControl(),
            endDate: new FormControl(''),
            keyword: new FormControl(''),
            startHour: new FormControl('00'),
            endHour: new FormControl('23')
        });
        // 欄位檢查
        this.reportService.queryApiUsageStatistics_before().subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.addFormValidator(this.form, res.RespBody.constraints);
            }
        });
        // 時間單位
        let ReqBody = {
            encodeItemNo: this.toolService.Base64Encoder(this.toolService.BcryptEncoder('REPORT_TIME_TYPE')) + ',' + 37,
            isDefault: 'N'
        } as DPB0047Req;
        this.list.querySubItemsByItemNo(ReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                let _timeTypes:{label:string, value:string}[] = [];
                if(res.RespBody.subItems){
                  for (let item of res.RespBody.subItems) {
                      _timeTypes.push({ label: item.subitemName, value: item.subitemNo });
                  }
                }
                this.timeTypes = _timeTypes;
            }
        });
        // this.minDate.setDate(this.today.getDate() - 90);
        this.timeType.valueChanges.subscribe(type => {
            this.minDate = new Date();
            this.startDate.setValue('');
            this.endDate.setValue('');
            if (type == 'DAY') {
                this.minDate.setDate(this.today.getDate() - 90);
            }
            if (type == 'MONTH') {
                this.minDate.setMonth(this.today.getMonth() + 1 - 13);
            }

            if (type == 'MINUTE') {
              this.minDate.setDate(this.today.getDate() - 2);
              this.startHour.setValue('00');
              this.endHour.setValue('23');
            }

            this.startDate.setValue(new Date);
            this.endDate.setValue(new Date);
        });
        // this.startDate.valueChanges.subscribe(date => {

        //     if (date != '') {
        //         let _date = new Date(date);
        //         if (this.timeType.value == 'DAY') {
        //             this.displayStartDate = dayjs(_date).format('YYYY/MM/DD');
        //         }
        //         if (this.timeType.value == 'MONTH') {
        //             _date.setDate(1);
        //             this.displayStartDate = dayjs(_date).format('YYYY/MM/DD');
        //         }
        //     }
        //     else {
        //         this.displayStartDate = '';
        //     }
        // });
        // this.endDate.valueChanges.subscribe(date => {
        //     if (date != '') {
        //         let _date = new Date(date);
        //         if (this.timeType.value == 'DAY') {
        //             this.displayEndDate = dayjs(date).format('YYYY/MM/DD');
        //         }
        //         if (this.timeType.value == 'MONTH') {
        //             if (_date.getMonth() == this.today.getMonth()) {
        //                 _date = new Date();
        //                 this.displayEndDate = dayjs(_date).format('YYYY/MM/DD');
        //             }
        //             else {
        //                 _date.setDate(1);
        //                 _date.setMonth(_date.getMonth() + 1);
        //                 _date.setDate(_date.getDate() - 1);
        //                 this.displayEndDate = dayjs(_date).format('YYYY/MM/DD');
        //             }
        //         }
        //     }
        //     else {
        //         this.displayEndDate = '';
        //     }
        // });
        this.init();

    }

    async init() {
        const code = ['api_key', 'module_name', 'api_name', 'api_desc', 'public_flag'];
        const dict = await this.toolService.getDict(code);
        this.apiListCols = [
            { field: 'apiName', header: `(${dict['api_key']},${dict['module_name']}) - ${dict['api_name']}` },
            { field: 'apiDesc', header: dict['api_desc'] },
            { field: 'publicFlagName', header: dict['public_flag'] }
        ];

        for (let i = 0; i < 24; i++) {
          let tmpStr = ('0' + i).slice(-2)
          this.hourData.push({ label: tmpStr, value: tmpStr });
        }

    }

    queryApiList() {
        this.apiListData = [];
        this.selectedApi = [];
        this.apiListRowcount = this.apiListData.length;
        let ReqBody = {
            keyword: this.keyword.value,
            apiUidList: this.apiUidList.value
        } as AA0321Req;
        this.apiService.queryAPIListByOrg(ReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.apiListData = res.RespBody.dataList;
                this.apiListRowcount = this.apiListData.length;
            }
        });
    }

    moreApiList() {
        let ReqBody = {
            keyword: this.keyword.value,
            apiKey: this.apiListData[this.apiListData.length - 1].apiKey,
            moduleName: this.apiListData[this.apiListData.length - 1].moduleName,
            apiUidList: this.apiUidList.value
        } as AA0321Req;
        this.apiService.queryAPIListByOrg(ReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.apiListData = this.apiListData.concat(res.RespBody.dataList);
                this.apiListRowcount = this.apiListData.length;
            }
        });
    }

    chooseApi() {
        this.currentTitle = this.title;
        this.pageNum = 1;
        let set = new Set();
        let _apiName:any[] = [];
        let _apiUid:any[] = [];
        this.apiUidList.value.map(apiUid => {
            set.add(apiUid);
            _apiUid.push(apiUid);
        });
        this.apiNameList.value.map(apiName => {
            _apiName.push(apiName);
        });
        this.selectedApi.map(item => {
            if (!set.has(item.apiUid)) {
                _apiUid.push(item.apiUid);
                _apiName.push(item.apiName);
            }
        });
        this.apiUidList.setValue(_apiUid);
        this.apiNameList.setValue(_apiName);
    }

    deleteApi(idx: number) {
        this.apiUidList.value.splice(idx, 1);
        this.apiNameList.value.splice(idx, 1);
    }

    submitForm() {
        // 清除canvas
        $('#reportChart').remove();
        $('#div_canvas').append('<canvas id="reportChart"</canvas>');
        this.canvas = document.getElementById('reportChart');
        this.canvas.height = '100%';
        this.ctx = this.canvas.getContext('2d');
        // this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        let ReqBody = {
            timeType: this.toolService.Base64Encoder(this.toolService.BcryptEncoder(this.timeType.value)) + ',' + this.timeTypes.findIndex(item => item.value == this.timeType.value),
            apiUidList: this.apiUidList.value,
        } as AA1201Req;

        if (this.timeType.value == 'DAY') {
            ReqBody.startDate = dayjs(this.startDate.value).format('YYYY/MM/DD');
            ReqBody.endDate = dayjs(this.endDate.value).format('YYYY/MM/DD')
        }
        else if (this.timeType.value == 'MONTH') {
            let _startDate = new Date(this.startDate.value);
            _startDate.setDate(1);
            // let _endDate = new Date(this.endDate.value);
            // if (_endDate.getMonth() == this.today.getMonth()) {
            //     _endDate = new Date();
            // }
            // else {
            //     _endDate.setDate(1);
            //     _endDate.setMonth(_endDate.getMonth() + 1);
            //     _endDate.setDate(_endDate.getDate() - 1);
            // }
            let _endDate = new Date(this.endDate.value);
            _endDate.setMonth(_endDate.getMonth()+1,1);
            _endDate.setDate(_endDate.getDate() - 1);

            if(_endDate>this.today)
            {
              _endDate = this.today;
            }

            ReqBody.startDate = dayjs(_startDate).format('YYYY/MM/DD');
            ReqBody.endDate = dayjs(_endDate).format('YYYY/MM/DD');
        }
        else{
          ReqBody.startDate = dayjs(this.startDate.value).format('YYYY/MM/DD');
          ReqBody.endDate = dayjs(this.startDate.value).format('YYYY/MM/DD');
          ReqBody.startHour = this.startHour.value;
          ReqBody.endHour = this.endHour.value;
        }

        this.reportService.queryApiUsageStatistics(ReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                let _datasets:any[] = [];
                res.RespBody.datasets.map(item => {
                    let _obj = {};
                    _obj['label'] = item.label;
                    _obj['data'] = item.data;
                    if (item.label == '成功' || item.label == 'success') {
                        _obj['backgroundColor'] = 'rgb(0, 255, 0)';
                        _obj['borderColor'] = 'rgb(0, 255, 0)';
                    }
                    else {
                        _obj['backgroundColor'] = 'rgb(255, 0, 0)';
                        _obj['borderColor'] = 'rgb(255, 0, 0)';
                    }
                    _obj['borderWidth'] = 1;
                    _datasets.push(_obj);
                });
               new Chart(this.ctx, {
                    // type: 'horizontalBar',
                    type:'bar',
                    data: {
                        labels: res.RespBody.labels,
                        datasets: _datasets
                    },
                    options: {
                        maintainAspectRatio: false,

                        indexAxis: 'y',
                        // Elements options apply to all of the options unless overridden in a dataset
                        // In this case, we are setting the border of each horizontal bar to be 2px wide
                        // elements: {
                        //     rectangle: {
                        //         borderWidth: 2,
                        //     }
                        // },
                        responsive: true,
                        plugins:{
                            legend: {
                                position: 'right',
                            },
                            title: {
                              display: true,
                              text: res.RespBody.reportName
                            }
                        },
                        scales: {
                          x: {
                              title:{
                                display:true,
                                text:res.RespBody.xLable
                              },
                              beginAtZero:true
                            }

                            // xAxes: [{
                            //     display: true,
                            //     scaleLabel: {
                            //         display: true,
                            //         labelString: res.RespBody.xLable
                            //     },
                            //     ticks: {
                            //         beginAtZero: true,
                            //         //	steps: 10, // 幾格
                            //         //	stepValue: 5, // 間距
                            //         // max: 25 // 最大
                            //     }
                            // }]
                        },

                    }
                });
                Chart.defaults.elements.bar.borderWidth = 2;
            }
        });
    }

    async copyData(data: string) {
        const code = ['copy', 'data', 'message.success'];
        const dict = await this.toolService.getDict(code);
        let selBox = document.createElement('textarea');
        selBox.style.position = 'fixed';
        selBox.style.left = '0';
        selBox.style.top = '0';
        selBox.style.opacity = '0';
        selBox.value = data;
        document.body.appendChild(selBox);
        selBox.focus();
        selBox.select();
        document.execCommand('copy');
        document.body.removeChild(selBox);
        this.message.add({ severity: 'success', summary: `${dict['copy']} ${dict['data']}`, detail: `${dict['copy']} ${dict['message.success']}` });
    }

    async changePage(action: string) {
        const code = ['api_list'];
        const dict = await this.toolService.getDict(code);
        switch (action) {
            case 'query':
                this.currentTitle = this.title;
                this.pageNum = 1;
                break;
            case 'api_list':
                this.currentTitle = `${this.title} > ${dict['api_list']}`;
                this.pageNum = 2;
                this.queryApiList();
                break;
        }
    }

    // draw() {
    //     const myChart = new chartjs(this.ctx, {
    //         type: 'bar',
    //         data: {
    //             labels: ['USA', 'Spain', 'Italy', 'France', 'Germany', 'UK', 'Turkey', 'Iran', 'China', 'Russia', 'Brazil', 'Belgium', 'Canada', 'Netherlands', 'Switzerland', 'India', 'Portugal', 'Peru', 'Ireland', 'Sweden'],
    //             datasets: [{
    //                 label: 'Total cases.',
    //                 data: [886789, 213024, 189973, 158183, 153129, 138078, 101790, 87026, 82804, 62773, 50036, 42797, 42110, 35729, 28496, 23502, 22353, 20914, 17607, 16755],
    //                 backgroundColor: 'blue',
    //                 borderWidth: 1
    //             }]
    //         },
    //         options: {
    //             legend: {
    //                 display: false
    //             },
    //             responsive: false
    //         }
    //     });
    // }

    exportReport() {
        // if (this.acConf.edition == 'Express') {
            this.alertService.ok('', 'Only Available in Enterprise version', AlertType.warning);
            return;
        // }
    }

    headerReturn() {
      this.changePage('query');
    }

    // @HostListener('window:resize', ['$event'])
    //     renderChart(){
    //     // console.log(window.innerHeight);
    //     // console.log( this.chart);
    //     // console.log( this.chart);
    //     if(this.chart) this.chart.resize();
    // }

    public get timeType() { return this.form.get('timeType')!; }
    public get apiUidList() { return this.form.get('apiUidList')!; }
    public get apiNameList() { return this.form.get('apiNameList')!; }
    public get startDate() { return this.form.get('startDate')!; }
    public get endDate() { return this.form.get('endDate')!; }
    public get keyword() { return this.form.get('keyword')!; }

    public get startHour() { return this.form.get('startHour')!; }
    public get endHour() { return this.form.get('endHour')!; }

}

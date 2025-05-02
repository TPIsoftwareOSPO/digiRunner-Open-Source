import { ApiService } from 'src/app/shared/services/api-api.service';
import { ActivatedRoute } from '@angular/router';
import { BaseComponent } from 'src/app/layout/base-component';
import { Component, OnInit } from '@angular/core';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ToolService } from 'src/app/shared/services/tool.service';
import { ListService } from 'src/app/shared/services/api-list.service';
import { ReportService } from 'src/app/shared/services/api-report.service';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import * as dayjs from 'dayjs';
import { AA1204Req } from 'src/app/models/api/ReportService/aa1204.interface';
// import * as chartjs from 'chart.js';
import { Chart } from 'chart.js/auto'
import { AA0510Resp } from 'src/app/models/api/UtilService/aa0510.interface';
import { AlertService } from 'src/app/shared/services/alert.service';
import { AlertType } from "src/app/models/common.enum";

@Component({
    selector: 'app-ac1304',
    templateUrl: './ac1304.component.html',
    styleUrls: ['./ac1304.component.css'],
    providers: [ApiService]
})
export class Ac1304Component extends BaseComponent implements OnInit {

    form!: FormGroup;
    canvas: any;
    ctx: any;
    timeTypes: { label: string; value: string }[] = [];
    today: Date = new Date();
    minDate: Date = new Date();
    maxDate: Date = new Date();
    displayStartDate: string = '';
    displayEndDate: string = '';
    acConf?: AA0510Resp;

    hourData: { label: string, value: string }[] = [];

    // chart:any;

    constructor(
        router: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private toolService: ToolService,
        private list: ListService,
        private reportService: ReportService,
        private alertService: AlertService
    ) {
        super(router, tr);
    }

    ngOnInit() {
        this.acConf = this.toolService.getAcConf();
        this.canvas = document.getElementById('reportChart');
        this.ctx = this.canvas.getContext('2d');
        this.form = this.fb.group({
            timeType: new FormControl('DAY'),
            startDate: new FormControl(''),
            endDate: new FormControl(''),
            startHour: new FormControl('00'),
            endHour: new FormControl('23')
        });
        // 欄位檢查
        this.reportService.queryApiTraffic_before().subscribe(res => {
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
                let _timeTypes:{label: string, value: string}[] = [];
                if(res.RespBody.subItems) {
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
        // this.timeType.valueChanges.subscribe(type => {
        //     this.minDate = new Date();
        //     this.startDate.setValue('');
        //     this.endDate.setValue('');
        //     if (type == 'DAY') {
        //         this.minDate.setDate(this.today.getDate() - 90);
        //     }
        //     if (type == 'MONTH') {
        //         this.minDate.setMonth(this.today.getMonth() + 1 - 12);
        //     }
        // });
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
        // this.startDate.setValue(new Date);
        // this.endDate.setValue(new Date);

        for (let i = 0; i < 24; i++) {
          let tmpStr = ('0' + i).slice(-2)
          this.hourData.push({ label: tmpStr, value: tmpStr });
        }
    }

    submitForm() {
        // 清除canvas
        $('#reportChart').remove();
        $('#div_canvas').append('<canvas id="reportChart"></canvas>');
        this.canvas = document.getElementById('reportChart');
        this.canvas.height = '100%';
        this.ctx = this.canvas.getContext('2d');
        let ReqBody = {
            timeType: this.toolService.Base64Encoder(this.toolService.BcryptEncoder(this.timeType.value)) + ',' + this.timeTypes.findIndex(item => item.value == this.timeType.value)
        } as AA1204Req;
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
        this.reportService.queryApiTraffic(ReqBody).subscribe(res => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                let _datasets:any[] = [];
                res.RespBody.datasets.map(item => {
                    let _obj = {};
                    _obj['label'] = item.label;
                    _obj['data'] = item.data;
                    if (item.label == '成功' || item.label == 'success' ) {
                        _obj['backgroundColor'] = 'rgb(0, 255, 0)';
                        _obj['borderColor'] = 'rgb(0, 255, 0)';
                    }
                    _obj['borderWidth'] = 1;
                    _obj['fill'] = false;
                    _datasets.push(_obj);
                });
             new Chart(this.ctx, {
                    type: 'line',
                    data: {
                        labels: res.RespBody.labels,
                        datasets: _datasets
                    },
                    options: {
                      maintainAspectRatio: false,
                        responsive: true,
                        events: ['mousemove'],
                        plugins:{
                            title: {
                              display: true,
                                  text: res.RespBody.reportName
                              },
                              tooltip: {
                                mode: 'index',
                                intersect: false,
                              },

                          // hover: {
                          //     mode: 'nearest',
                          //     intersect: true
                          // },
                          legend: {
                              position: 'right',
                          },
                        },

                        scales: {
                            x: {
                              title:{
                                display:true,
                                text:res.RespBody.xLable
                              },
                            },
                            y: {
                              title:{
                                display:true,
                                text:res.RespBody.yLable
                              },
                            }
                            // xAxes: [{
                            //     display: true,
                            //     scaleLabel: {
                            //         display: true,
                            //         labelString: res.RespBody.xLable
                            //     }
                            // }],
                            // yAxes: [{
                            //     display: true,
                            //     scaleLabel: {
                            //         display: true,
                            //         labelString: res.RespBody.yLable
                            //     },
                                // ticks: {
                                //     beginAtZero: true,
                                //     //	steps: 10, // 幾格
                                //     //	stepValue: 5, // 間距
                                //     max: 20 // 最大
                                // }
                            // }]
                        }
                    }
                });
            }
        });
    }

    exportReport() {
        // if (this.acConf.edition == 'Express') {
            this.alertService.ok('', 'Only Available in Enterprise version', AlertType.warning);
            return;
        // }
    }

// @HostListener('window:resize', ['$event'])
// renderChart(){
//     // console.log(window.innerHeight);
//     // console.log( this.chart);
//     // console.log( this.chart);
//     if(this.chart) this.chart.resize();
// }

    public get timeType() { return this.form.get('timeType')!; }
    public get startDate() { return this.form.get('startDate')!; }
    public get endDate() { return this.form.get('endDate')!; }
    public get startHour() { return this.form.get('startHour')!; }
    public get endHour() { return this.form.get('endHour')!; }
}

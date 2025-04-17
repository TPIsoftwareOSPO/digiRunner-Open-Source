import { Component, OnInit, AfterViewInit, HostListener, ViewChildren, QueryList, ElementRef } from '@angular/core';
import { AlertService } from 'src/app/shared/services/alert.service';
import { AlertType } from 'src/app/models/common.enum';
import { AboutService } from 'src/app/shared/services/api-about.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DPB0118Resp } from 'src/app/models/api/AboutService/dpb0118.interface';
import { TranslateService } from '@ngx-translate/core';
import * as dayjs from 'dayjs';
import * as echarts from 'echarts';
import { ServerService } from 'src/app/shared/services/api-server.service';
import {
  AA1211ApiTrafficDistributionResp,
  AA1211BadAttemptResp,
  AA1211ClientUsagePercentageResp,
  AA1211FailResp,
  AA1211LastLoginLog,
  AA1211MedianResp,
  AA1211PopularResp,
  AA1211Req,
  AA1211SuccessResp,
  AA1211UnpopularResp,
} from 'src/app/models/api/ReportService/aa1211.interface';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import {
  AA1212BadAttemptItemResp,
  AA1212BadAttemptResp,
  AA1212LastLoginLog,
  AA1212RespItem,
} from 'src/app/models/api/ReportService/aa1212.interface';
import {
  catchError,
  delay,
  expand,
  of,
  Subject,
  Subscription,
  takeUntil,
} from 'rxjs';
import { BadAttemptListComponent } from './bad-attempt-list/bad-attempt-list.component';
import { DialogService } from 'primeng/dynamicdialog';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard2.component.html',
  styleUrls: ['./dashboard2.component.scss'],
})
export class DashboardComponent implements OnInit {
  versionInfo?: DPB0118Resp;
  title: string = '';
  edition: string = '';
  editionDate: string = '';
  nearWarnDays: number = 0;
  overBufferDays: number = 0;

  today: string = '';
  diffDates: number = 0;
  dataTime: string = ' - '; // 資料取回時間
  request?: string;
  success?: AA1211SuccessResp;
  fail?: AA1211FailResp;
  badAttempt?: AA1211BadAttemptResp;
  avg?: string;
  median?: AA1211MedianResp;
  popular: Array<AA1211PopularResp> = [];
  unpopular: Array<AA1211UnpopularResp> = [];
  apiTrafficDistribution: Array<AA1211ApiTrafficDistributionResp> = [];
  clientUsagePercentage: Array<AA1211ClientUsagePercentageResp> = [];

  timeTypeUnit: { label: string; key: number }[] = [];
  timeType: any = 2;

  isAsc: boolean = true;
  includeFail: boolean = false;


  // badattempChart: any;
  // medianChart: any;
  // hotChart: any;
  // apiCountChart: any;
  // clientChart: any;

  zoom: number = 1;

  reloadDataRef: any;

  lastLoginLog: Array<AA1211LastLoginLog> = [];

  private apiSubscription!: Subscription;
  destroy$ = new Subject<void>();

  // cardHeight: string = '700px'; // defalut
  // @ViewChildren('allNodeCard') allNodeCards!: QueryList<ElementRef>;

  constructor(
    private alert: AlertService,
    private toolService: ToolService,
    private aboutService: AboutService,
    private translate: TranslateService,
    private serverService: ServerService,
    private ngxService: NgxUiLoaderService,
    private dialogService: DialogService,
    private messageService: MessageService

  ) {}

  // resizeReport() {
  // setTimeout(() => {
  // if (this.badattempChart) this.badattempChart.resize();
  // if (this.medianChart) this.medianChart.resize();
  // if (this.hotChart) this.hotChart.resize();
  // if (this.apiCountChart) this.apiCountChart.resize();
  // if (this.clientChart) this.clientChart.resize();
  // }, 0);
  // }

  ngOnDestroy() {
    // if (this.reloadDataRef) clearInterval(this.reloadDataRef);
    this.destroy$.next();
    this.destroy$.complete();
  }

  // @HostListener('window:resize')
  // onResize(event) {
  //   // if (this.badattempChart) this.badattempChart.resize();
  //   if (this.medianChart) this.medianChart.resize();
  //   if (this.hotChart) this.hotChart.resize();
  //   if (this.apiCountChart) this.apiCountChart.resize();
  //   if (this.clientChart) this.clientChart.resize();
  // }

  // getDataReload() {
  //   if (this.reloadDataRef) clearInterval(this.reloadDataRef);
  //   this.getDashboardData();
  //   this.reloadDataRef = setInterval(() => {
  //     this.getDashboardData();
  //   }, 1000 * 60 * 10);
  // }

  async ngOnInit() {
    const code = ['minute', 'calendar.day', 'calendar.month', 'calendar.year'];
    const dict = await this.toolService.getDict(code);
    this.timeTypeUnit = [
      { label: `10 ${dict['minute']}`, key: 1 },
      { label: dict['calendar.day'], key: 2 },
      { label: dict['calendar.month'], key: 3 },
      { label: dict['calendar.year'], key: 4 },
    ];

    this.aboutService.getModuleVersionData().subscribe((res) => {
      this.versionInfo = res;
      this.edition = this.toolService.getAcConfEdition();
      this.editionDate = this.toolService.getAcConfExpiryDate();
      this.nearWarnDays = this.versionInfo.nearWarnDays;
      this.overBufferDays = this.versionInfo.overBufferDays;

      this.today = dayjs(new Date()).format('YYYY-MM-DD');
      const endDate = dayjs(this.editionDate).format('YYYY-MM-DD');

      this.diffDates = this.getDiffDays(this.today, endDate);

      // 還剩n天即將到期
      if (
        this.diffDates == this.nearWarnDays ||
        (this.diffDates > 0 && this.diffDates < this.nearWarnDays)
      ) {
        this.translate
          .get(['dashBoard.title1', 'dashBoard.desc1'], {
            edition: this.edition,
            expiryDate: this.versionInfo.expiryDate,
          })
          .subscribe((res) => {
            this.alert.ok(
              res['dashBoard.title1'],
              '',
              AlertType.info,
              res['dashBoard.desc1']
            );
          });
      } else if (
        this.diffDates < 0 &&
        Math.abs(this.diffDates) >= this.overBufferDays
      ) {
        this.translate
          .get(['dashBoard.title2', 'dashBoard.desc2'], {
            edition: this.edition,
          })
          .subscribe((res) => {
            this.alert.ok(
              res['dashBoard.title2'],
              '',
              AlertType.info,
              res['dashBoard.desc2']
            );
          });
      } else if (
        this.diffDates <= 0 ||
        (this.diffDates <= -1 * this.nearWarnDays &&
          this.diffDates <= -1 * this.overBufferDays)
      ) {
        this.translate
          .get(['dashBoard.title2', 'dashBoard.desc2'], {
            edition: this.edition,
          })
          .subscribe((res) => {
            this.alert.ok(
              res['dashBoard.title2'],
              '',
              AlertType.info,
              res['dashBoard.desc2']
            );
          });
      }
    });
    // this.getDataReload();
    this.queryRealtimeDashboardData();
  }

  // clearDashboardData() {
  //   this.dataTime = ' - ';
  //   this.request = undefined;
  //   this.success = undefined;
  //   this.fail = undefined;
  //   this.badAttempt = undefined;
  //   this.avg = undefined;
  //   this.median = undefined;
  //   this.clientUsagePercentage = [];
  //   this.apiTrafficDistribution = [];
  //   this.popular = [];
  //   this.unpopular = [];

  // if (this.badattempChart) this.badattempChart.dispose();
  // if (this.medianChart) this.medianChart.dispose();
  // if (this.hotChart) this.hotChart.dispose();
  // if (this.apiCountChart) this.apiCountChart.dispose();
  // if (this.clientChart) this.clientChart.dispose();
  // }

  // switchOption(evt) {
  //   this.clearDashboardData();
  //   this.getDataReload();
  // }

  getDiffDays(sDate: string, eDate: string) {
    let startDate = new Date(sDate);
    let endDate = new Date(eDate);

    let time = endDate.getTime() - startDate.getTime();
    return Math.ceil(time / (1000 * 3600 * 24));
  }

  formateDate(date: Date) {
    if (!date) return '';
    const procDate = Number(date);
    return dayjs(procDate).format('YYYY-MM-DD HH:mm:ss') != 'Invalid Date'
      ? dayjs(procDate).format('YYYY-MM-DD HH:mm:ss')
      : '';
  }

  badattempChart: { [key: string]: echarts.ECharts } = {};
  // 產生BadAttempt圖表
  generateBadAttemptReport(
    nodeName: string = '',
    badAttempt: AA1212BadAttemptResp
  ) {
    // console.log(nodeName)
    // this.badAttempt = badAttempt;
    let total: number =
      Number(badAttempt.code401) +
      Number(badAttempt.code403) +
      Number(badAttempt.others);

    setTimeout(() => {
      //避免chart init 因container剛長出來還未有clientwidth,clientheight的警告
      const elementId = 'badAttemptReport_' + nodeName;
      const badAttemptEle = document.getElementById(elementId);
      if (badAttemptEle) {
        if (
          !this.badattempChart[nodeName] ||
          this.badattempChart[nodeName]?.getDom().id !== badAttemptEle.id
        ) {
          console.log('init');
          this.badattempChart[nodeName]?.dispose();
          this.badattempChart[nodeName] = echarts.init(badAttemptEle);
        }
        // console.log(this.badattempChart)
        console.log(this.badattempChart[nodeName]);
        this.badattempChart[nodeName].setOption({
          // animation:false,
          color: ['#F6D8CB', '#DA7A53', '#6E79ED'],
          tooltip: {
            trigger: 'item',
          },
          title: {
            text: this.numberComma(total.toString()),
            left: 'center',
            top: 'center',
          },
          // legend: {
          //   top: '0%',
          //   left: 'center'
          // },
          series: [
            {
              type: 'pie',
              radius: ['40%', '70%'],
              avoidLabelOverlap: false,
              itemStyle: {
                borderRadius: 10,
                borderColor: '#fff',
                borderWidth: 1,
              },
              label: {
                show: false,
                position: 'center',
              },
              emphasis: {
                label: {
                  show: false,
                  fontSize: 24,
                  // fontWeight: 'bold'
                },
              },
              labelLine: {
                show: false,
              },
              data: [
                { value: badAttempt.code401, name: '401' },
                { value: badAttempt.code403, name: '403' },
                { value: badAttempt.others, name: 'Others' },
              ],
            },
          ],
        });
        this.badattempChart[nodeName].resize();
        // this.badattempChart.setOption(option);
      }
    }, 100);
  }
  // 中位數圖表
  // async generateMadianReport(median: AA1211MedianResp) {
  //   this.median = median;
  //   if (median.max == median.min) {
  //     return;
  //   }
  //   const codes = ['normal', 'good', 'aberrant'];
  //   const dict = await this.toolService.getDict(codes);
  //   setTimeout(() => {
  //     const medianEle = document.getElementById('medianReport');
  //     if (medianEle) {
  //       this.medianChart = echarts.init(medianEle);
  //       let option = {
  //         series: [
  //           {
  //             type: 'gauge',
  //             startAngle: 180,
  //             endAngle: 0,
  //             center: ['50%', '50%'],
  //             radius: '60%',
  //             min: median.min,
  //             max: median.max,
  //             splitNumber: median.gap,
  //             axisLine: {
  //               lineStyle: {
  //                 width: 15,
  //                 color: [
  //                   [0.1, '#6DEA38'],
  //                   [0.2, '#ACE236'],
  //                   [0.3, '#D8ED38'],
  //                   [0.4, '#EDE438'],
  //                   [0.5, '#edd538'],
  //                   [0.6, '#F2B13A'],
  //                   [0.7, '#EFA526'],
  //                   [0.8, '#F28232'],
  //                   [0.9, '#f7630e'],
  //                   [1, '#f73d0e'],
  //                 ],
  //               },
  //             },
  //             pointer: {
  //               icon: 'path://M12.8,0.7l12,40.1H0.7L12.8,0.7z',
  //               length: '55%',
  //               width: 3,
  //               offsetCenter: [0, '-10%'],
  //               itemStyle: {
  //                 color: 'auto',
  //               },
  //             },
  //             axisTick: {
  //               show: false,
  //               // length: 0,
  //               // lineStyle: {
  //               //   color: 'auto',
  //               //   width: 2
  //               // }
  //             },
  //             splitLine: {
  //               show: false,
  //               // length: 0,
  //               // lineStyle: {
  //               //   color: 'auto',
  //               //   width: 5
  //               // }
  //             },
  //             axisLabel: {
  //               padding: [-5, -15, 0, -15],
  //               color: '#464646',
  //               fontSize: 10,
  //               distance: -30,
  //               // rotate: 'tangential',
  //               formatter: function (value) {
  //                 if (value === median.max) {
  //                   return dict['aberrant'];
  //                 } else if (value === (median.min + median.max) / 2) {
  //                   return dict['normal'];
  //                 } else if (value === median.min) {
  //                   return dict['good'];
  //                 }

  //                 return '';
  //               },
  //             },
  //             // title: {
  //             //   offsetCenter: [0, '-10%'],
  //             //   fontSize: 20
  //             // },
  //             detail: {
  //               fontSize: 0,
  //               offsetCenter: [0, '-35%'],
  //               valueAnimation: true,
  //               // formatter: function (value) {
  //               //   return Math.round(value * 100) + '';
  //               // },
  //               color: 'inherit',
  //             },
  //             data: [
  //               {
  //                 value: median.median,
  //               },
  //             ],
  //           },
  //         ],
  //       };
  //       this.medianChart.setOption(option);
  //     }
  //   }, 0);
  // }

  //top5熱門排行
  // async generatePopularReport(popular: Array<AA1211PopularResp>) {
  //   this.popular = popular;

  //   const code = ['resp_avg_time'];
  //   const dict = await this.toolService.getDict(code);

  //   const popularRefactor = popular
  //     .sort((a: AA1211PopularResp, b: AA1211PopularResp) => {
  //       return b.rank - a.rank;
  //     })
  //     .map((item) => {
  //       return {
  //         apiName: item.apiName,
  //         success: item.success.replace(',', ''),
  //         fail: item.fail.replace(',', ''),
  //         showFlag: 0,
  //         avg: item.avg,
  //       };
  //     });
  //   setTimeout(() => {
  //     const popularEle = document.getElementById('popularReport');
  //     if (popularEle) {
  //       const _this = this;
  //       this.hotChart = echarts.init(popularEle);

  //       let option = {
  //         dataset: {
  //           source: popularRefactor,
  //         },
  //         tooltip: {
  //           trigger: 'axis',
  //           axisPointer: {
  //             // Use axis to trigger tooltip
  //             type: 'shadow', // 'shadow' as default; can also be 'line' or 'shadow'
  //           },
  //           formatter: (params) => {
  //             // console.log(params)
  //             let failTarIdx =
  //               params.length > 1 ? params.length - 2 : params.length - 1;
  //             return `
  //                     ${params[0].name}<br />
  //                     ${params[0].marker} ${
  //               params[0].seriesName
  //             }: ${this.toolService.numberComma(params[0].value.success)}<br />
  //                     ${params[failTarIdx].marker} ${
  //               params[failTarIdx].seriesName
  //             }: ${this.toolService.numberComma(
  //               params[failTarIdx].value.fail
  //             )}<br />
  //                     ${dict['resp_avg_time']}: ${this.toolService.numberComma(
  //               params[0].value.avg
  //             )}ms
  //                     `;
  //           },
  //         },
  //         // barWidth: '40%',
  //         barCategoryGap: '20%',
  //         legend: {},
  //         grid: {
  //           left: '3%',
  //           right: '4%',
  //           bottom: '3%',
  //           containLabel: true,
  //         },

  //         xAxis: {
  //           type: 'value',
  //         },
  //         yAxis: {
  //           type: 'category',
  //           // data: popular.map(item=>{ return item.apiName })
  //         },
  //         label: {
  //           fontWeight: 'bold',
  //           fontSize: 14,
  //           color: '#000',
  //         },
  //         itemStyle: {
  //           // borderRadius:[0,15,15,0]
  //         },
  //         series: [
  //           {
  //             name: 'Success',
  //             type: 'bar',
  //             stack: 'total',
  //             label: {
  //               show: false,
  //             },
  //             emphasis: {
  //               focus: 'series',
  //             },
  //             // data: popular.map(item=>{return item.success}),
  //             // color: '#F3B142',
  //             color: {
  //               type: 'linear',
  //               x: 0,
  //               y: 0,
  //               x2: 1,
  //               y2: 0,
  //               colorStops: [
  //                 {
  //                   offset: 0,
  //                   color: '#FCEDCC', // 0% 的颜色
  //                 },
  //                 {
  //                   offset: 1,
  //                   color: '#F3B041', // 100% 的颜色
  //                 },
  //               ],
  //               global: false,
  //             },
  //           },
  //           {
  //             name: 'Fail',
  //             type: 'bar',
  //             stack: 'total',
  //             label: {
  //               show: false,
  //             },

  //             emphasis: {
  //               focus: 'series',
  //             },
  //             color: '#DFDFDF',
  //             // data: popular.map(item=>{return item.fail}),
  //           },
  //           {
  //             name: 'Fail',
  //             type: 'bar',
  //             stack: 'total',
  //             label: {
  //               show: true,

  //               formatter: function (params) {
  //                 const total =
  //                   Number(params.value.success.replace(',')) +
  //                   Number(params.value.fail.replace(','));
  //                 return _this.toolService.numberComma(total);
  //               },
  //               position: 'inside',
  //             },
  //           },
  //         ],
  //       };
  //       this.hotChart.setOption(option);
  //     }
  //   }, 0);
  // }

  //api流量分佈
  // generateApiTrafficDistributionReport(
  //   apiTrafficDistribution: Array<AA1211ApiTrafficDistributionResp>
  // ) {
  //   if (apiTrafficDistribution.length == 0) return;
  //   this.apiTrafficDistribution = apiTrafficDistribution;
  //   // console.log(apiTrafficDistribution)
  //   // console.log(Array.from({ length: 25}, (vlue, index) => (`0`+index).slice(-2)));
  //   // let hhmmPool = Array.from({ length: 25 }, (vlue, index) => {
  //   //   // console.log(index)
  //   //   return (Array.from({ length: 6 }, (vlue, mIndex) => (`0` + index).slice(-2) + ":" + mIndex + "0"))
  //   //   // return (`0`+index).slice(-2)+":00"
  //   // }).reduce((prev, curr) => { //把陣列攤平
  //   //   return prev.concat(curr);
  //   // });

  //   let hhmmPool = apiTrafficDistribution.map((item) => item.xLable);
  //   let successPool = apiTrafficDistribution.map((item) => item.success);
  //   let failPool = apiTrafficDistribution.map((item) => item.fail);
  //   // for (let index = 0; index < 150; index++) {
  //   //   successPool.push(Math.floor(Math.random() * 100) + 1);
  //   //   failPool.push(Math.floor(Math.random() * 10));
  //   // }
  //   // console.log(hhmmPool)
  //   // console.log(successPool)
  //   // console.log(failPool)

  //   let dataset = [
  //     ['time', ...hhmmPool],
  //     ['success', ...successPool],
  //     ['fail', ...failPool],
  //   ];
  //   // console.log('dataset',dataset)
  //   setTimeout(() => {
  //     const targetEle = document.getElementById('apiTrafficDistributionReport');
  //     if (targetEle) {
  //       this.apiCountChart = echarts.init(targetEle);
  //       // title: {
  //       //   // text: 'api流量分佈'
  //       // },
  //       let option = {
  //         zoom: 1,
  //         tooltip: {
  //           trigger: 'axis',
  //           formatter: (params) => {
  //             return `
  //                     ${params[0].name} <br />
  //                     ${params[0].marker} ${
  //               params[0].seriesName
  //             }: ${this.toolService.numberComma(params[0].value[1])}<br />
  //                     ${params[1].marker} ${
  //               params[1].seriesName
  //             }: ${this.toolService.numberComma(params[1].value[2])}
  //                     `;
  //           },
  //         },
  //         legend: {
  //           data: ['Success', 'Fail'],
  //         },
  //         grid: {
  //           left: '3%',
  //           right: '4%',
  //           bottom: '3%',
  //           containLabel: true,
  //         },
  //         dataset: {
  //           source: dataset,
  //         },
  //         xAxis: {
  //           type: 'category',
  //           boundaryGap: false,
  //           // data: hhmmPool
  //         },
  //         yAxis: {
  //           type: 'value',
  //         },

  //         series: [
  //           {
  //             color: '#8dd6b7',
  //             seriesLayoutBy: 'row',
  //             name: 'Success',
  //             type: 'line',
  //             lineStyle: {
  //               width: 3,
  //             },
  //             areaStyle: {
  //               color: {
  //                 type: 'linear',
  //                 x: 0,
  //                 y: 1,
  //                 x2: 0,
  //                 y2: 0,
  //                 colorStops: [
  //                   {
  //                     offset: 1,
  //                     color: 'rgba(135, 204, 14, 0.67)', // 0% 的颜色
  //                   },
  //                   {
  //                     offset: 0,
  //                     color: 'rgba(135, 204, 14, 0.03)', // 100% 的颜色
  //                   },
  //                 ],
  //                 global: false,
  //               },
  //             },
  //             // stack: 'Total',
  //             showSymbol: false,
  //             // data: successPool,
  //           },
  //           {
  //             color: '#F6D8cb',
  //             seriesLayoutBy: 'row',
  //             name: 'Fail',
  //             type: 'line',
  //             lineStyle: {
  //               width: 3,
  //             },
  //             // stack: 'Total',
  //             showSymbol: false,
  //             // data: failPool
  //           },
  //         ],
  //       };
  //       this.apiCountChart.setOption(option);
  //     }
  //   }, 0);
  // }

  // getRandomColor() {
  //   var letters = '0123456789ABCDEF'.split('');
  //   var color = '#';
  //   for (var i = 0; i < 6; i++) {
  //     color += letters[Math.floor(Math.random() * 16)];
  //   }
  //   return color;
  // }

  // generateClientReport(
  //   clientUsagePercentage: Array<AA1211ClientUsagePercentageResp>
  // ) {
  //   if (clientUsagePercentage.length == 0) return;
  //   this.clientUsagePercentage = clientUsagePercentage;
  //   // this.clientData.forEach(item=>{
  //   // item['color'] = this.getRandomColor();
  //   // })
  //   // console.log(this.clientData.map(item=>item['color']))

  //   let clientData = clientUsagePercentage.map((item) => {
  //     return {
  //       value: item.request,
  //       name: item.client,
  //       percentage: item.percentage,
  //     };
  //   });
  //   // console.log('clientUsagePercentage', clientUsagePercentage)
  //   setTimeout(() => {
  //     const targetEle = document.getElementById('clientReport');
  //     if (targetEle) {
  //       this.clientChart = echarts.init(targetEle);
  //       let option = {
  //         title: {
  //           text: this.toolService.numberComma(clientUsagePercentage[0].total),
  //           left: 'center',
  //           top: 'center',
  //         },
  //         // color: this.clientData.map(item=>item['color']),
  //         tooltip: {
  //           trigger: 'item',
  //           position: 'right',
  //           formatter: (params) => {
  //             return `
  //                   <span style="font-weight:bold">${params.marker} ${
  //               params.name
  //             }</span> <br />
  //                   Percentage:  ${params.data.percentage}%<br />
  //                   Request: ${this.toolService.numberComma(params.value)}
  //                     `;
  //           },
  //         },
  //         // legend: {
  //         //   top: '0%',
  //         //   left: 'center'
  //         // },
  //         series: [
  //           {
  //             // name: 'Access From',
  //             type: 'pie',
  //             radius: ['30%', '50%'],
  //             avoidLabelOverlap: false,
  //             itemStyle: {
  //               borderRadius: 10,
  //               borderColor: '#fff',
  //               borderWidth: 1,
  //             },

  //             label: {
  //               show: false,
  //               position: 'center',
  //             },
  //             // emphasis: {
  //             //   label: {
  //             //     show: false,
  //             //     fontSize: 20,
  //             //     // fontWeight: 'bold'
  //             //   }
  //             // },
  //             labelLine: {
  //               show: false,
  //             },
  //             data: clientData,
  //             // data: [
  //             //   { value: 341, name: 'clientA' },
  //             //   { value: 1760, name: 'clientB' },
  //             //   { value: 1363, name: 'clientC' },
  //             //   { value: 1022, name: 'clientD' },
  //             //   { value: 681, name: 'clientE' },
  //             //   { value: 511, name: 'Others' },
  //             // ]
  //           },
  //         ],
  //       };
  //       this.clientChart.setOption(option);
  //       // console.log(this.clientChart.getOption().color)
  //       // console.log(
  //       //  this.clientChart.getModel().getSeries().map(s => {
  //       //   console.log(s)
  //       //   return {
  //       //     seriesIndex: s.seriesIndex,
  //       //     seriesColor: this.clientChart.getVisual({
  //       //       seriesIndex: s.seriesIndex
  //       //     }, 'color')
  //       //   }
  //       // })
  //       // )

  //       // )
  //     }
  //   }, 0);
  // }
  // getColor(idx) {
  //   return this.clientChart?.getOption()?.color[idx];
  // }

  // const badattempEle = document.getElementById('charts-container');
  //   if(badattempEle){
  //     let myChart = echarts.init(badattempEle);
  //     var option = {
  //               xAxis: {
  //                   type: 'category',
  //                   data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
  //               },
  //               yAxis: {
  //                   type: 'value'
  //               },
  //               series: [{
  //                   data: [120, 200, 150, 80, 70, 110, 130],
  //                   type: 'bar',
  //                   showBackground: true,
  //                   backgroundStyle: {
  //                       color: 'rgba(180, 180, 180, 0.2)'
  //                   }
  //               }]
  //           };
  //           myChart.setOption(option);
  //   }
  lastLoginLogList: Array<AA1212LastLoginLog> = [];
  dataList: Array<AA1212RespItem> = [];
  queryRealtimeDashboardData() {
    // this.serverService.queryRealtimeDashboardData({}).subscribe((res) => {
    //   if (this.toolService.checkDpSuccess(res.ResHeader)) {
    //     this.dataList = res.RespBody?.dataList ?? [];
    //     this.lastLoginLogList = res.RespBody.lastLoginLogList;
    //   }
    // });
    // return;
    this.apiSubscription = of(null)
      .pipe(
        expand(() =>
          this.serverService.queryRealtimeDashboardData({}).pipe(
            delay(2000), // API 回傳後延遲2秒再繼續
            catchError((err) => {
              //   console.error('API 發生錯誤:', err);
              return of(null); // 錯誤處理，避免停止循環
            })
          )
        ),
        takeUntil(this.destroy$)
      )
      .subscribe((res) => {
        if (res && this.toolService.checkDpSuccess(res.ResHeader)) {
          // console.log(res.RespBody)
          this.dataList = res.RespBody?.dataList ?? [];
          this.lastLoginLogList = res.RespBody.lastLoginLogList;
          // if(this.dataList)
          //   {
          //     this.dataList.forEach(item=>{
          //       if(item.badAttempt)
          //         {
          //           this.generateBadAttemptReport(item.nodeName,item.badAttempt)
          //         }
          //     })
          //   }
        }
      });
  }

  // getDashboardData() {
  //   let reqBody = {
  //     timeType: this.timeType,
  //   } as AA1211Req;
  //   // console.log(reqBody)
  //   // this.ngxService.start();
  //   this.serverService.getDashboardData(reqBody).subscribe((res) => {
  //     // console.log(res)
  //     if (this.toolService.checkDpSuccess(res.ResHeader)) {
  //       this.dataTime = res.RespBody.data.dataTime; //資料取回時間
  //       this.request = res.RespBody.data.request;
  //       this.success = res.RespBody.data.success;
  //       this.fail = res.RespBody.data.fail;
  //       if (res.RespBody.data.badAttempt)
  //         this.generateBadAttemptReport(res.RespBody.data.badAttempt);
  //       this.avg = this.numberComma(res.RespBody.data.avg.toString());
  //       if (res.RespBody.data.median)
  //         this.generateMadianReport(res.RespBody.data.median);
  //       if (res.RespBody.data.popular)
  //         this.generatePopularReport(res.RespBody.data.popular);

  //       this.unpopular = res.RespBody.data.unpopular
  //         ? res.RespBody.data.unpopular.sort(
  //             (a: AA1211UnpopularResp, b: AA1211UnpopularResp) => {
  //               return a.rank - b.rank;
  //             }
  //           )
  //         : [];
  //       if (res.RespBody.data.apiTrafficDistribution)
  //         this.generateApiTrafficDistributionReport(
  //           res.RespBody.data.apiTrafficDistribution
  //         );

  //       if (res.RespBody.data.clientUsagePercentage)
  //         this.generateClientReport(res.RespBody.data.clientUsagePercentage);
  //       if (res.RespBody.data.lastLoginLog)
  //         this.lastLoginLog = res.RespBody.data.lastLoginLog;
  //     }
  //     // else { //測試用

  //     // let aa1211RespItem = this.testData;
  //     // this.dataTime = aa1211RespItem.dataTime;
  //     // this.request = aa1211RespItem.request;
  //     // this.success = aa1211RespItem.success;
  //     // this.fail = aa1211RespItem.fail;
  //     // if (aa1211RespItem.badAttempt) this.generateBadAttemptReport(aa1211RespItem.badAttempt);
  //     // this.avg = aa1211RespItem.avg;
  //     // if (aa1211RespItem.median) this.generateMadianReport(aa1211RespItem.median);
  //     // if (aa1211RespItem.popular) this.generatePopularReport(aa1211RespItem.popular);

  //     // this.unpopular = aa1211RespItem.unpopular.sort((a: AA1211UnpopularResp, b: AA1211UnpopularResp) => { return b.rank - a.rank });
  //     // if (aa1211RespItem.apiTrafficDistribution) this.generateApiTrafficDistributionReport(aa1211RespItem.apiTrafficDistribution)

  //     // if (aa1211RespItem.clientUsagePercentage) this.generateClientReport(aa1211RespItem.clientUsagePercentage);
  //     // }
  //     this.ngxService.stopAll();
  //     // this.resizeReport();
  //   });
  // }

  numberComma(tar?: string) {
    // console.log(tar)

    return tar ? this.toolService.numberComma(tar) : tar;
  }

  changeSort(field, tarData) {
    // console.log(field)
    let tmpData = tarData;
    tarData = tmpData.sort((a, b) => {
      if (field != 'apiName') {
        return this.compare(Number(a[field]), Number(b[field]), this.isAsc);
      }
      // console.log( typeof a[field])
      return this.compare(a[field], b[field], this.isAsc);
    });
    this.isAsc = !this.isAsc;
  }

  public compare(a: number | string, b: number | string, isAsc: boolean) {
    return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
  }

  showBadAttemptList(badAttemptList: AA1212BadAttemptItemResp) {
    const ref = this.dialogService.open(BadAttemptListComponent, {
      data: { badAttemptList: badAttemptList },
      header: `400 Above API List`,
      width: '700px',
      styleClass: 'cHeader cContent cIcon',
    });
  }

  // ngAfterViewChecked() {
  //   setTimeout(() => {
  //     const firstAllNodeCard = this.allNodeCards.find(el =>
  //       el.nativeElement.classList.contains('all-node')
  //     );
  //     if (firstAllNodeCard) {
  //       this.cardHeight = `${firstAllNodeCard.nativeElement.offsetHeight}px`;
  //     }
  //   });
  // }

  // setFlag(item,flagName:string){

  //   if(item[flagName]== undefined)
  //   {
  //     item[flagName] = true;
  //   }
  //   else item[flagName] = !item[flagName];;

  // }

  copyToClipboard(text: string): void {
    if (navigator.clipboard) {
      navigator.clipboard.writeText(text).then(async () => {
        console.log('Copied to clipboard!');
        const code = ['copy', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success', summary: `${dict['copy']} `,
          detail: `${dict['copy']} ${dict['message.success']}!`
        });
        // 可改成顯示提示訊息或 toast
      }).catch(err => {
        console.error('Failed to copy:', err);
      });
    } else {
      // 備援處理
      console.warn('This browser does not support the Clipboard API');
    }
  }
}

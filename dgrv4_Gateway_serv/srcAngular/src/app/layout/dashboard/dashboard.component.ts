import {
  Component,
  OnInit,
  AfterViewInit,
  HostListener,
  ViewChildren,
  QueryList,
  ElementRef,
  OnDestroy,
} from '@angular/core';
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
  BehaviorSubject,
  catchError,
  delay,
  expand,
  of,
  Subject,
  Subscription,
  switchMap,
  take,
  takeUntil,
  timer,
} from 'rxjs';
import { BadAttemptListComponent } from './bad-attempt-list/bad-attempt-list.component';
import { DialogService } from 'primeng/dynamicdialog';
import { MessageService } from 'primeng/api';
import { NavigationExtras, Router } from '@angular/router';

// tps
interface tpsPoint {
  time: string;
  req: string;
  resp: string;
}
// Total Request 圖表資料
interface reqPoint {
  time: string;
  success: string;
  fail: string;
  total: string;
}
// db
interface dbPoint {
  time: string;
  active: number;
  waiting: number;
  idle: number;
  total: number;
}

// es
interface esPoint {
  time?: string;
  current: string;
  discarded: string;
}

//cpuUsage
interface cpuUsagePoint {
  time: string;
  cpuUsage: string;
}

//mem
interface memPoint {
  time: string;
  memFree: string;
  memMax: string;
  memTotal: string;
}

// thread
interface threadPoint {
  time: string;
  countryRoadActvieCount: string;
  highwayActvieCount: string;
}

// cache
interface cachePoint {
  time: string;
  dao: string;
  fixed: string;
  rcd: string;
}

interface DataNode {
  nodeName: string;
  tpsSeries: tpsPoint[];
  // reqSeries: reqPoint[];
  dbSeries?: dbPoint[];
  esSeries?: esPoint[];
  cpuSeries?: cpuUsagePoint[];
  memSeries?: memPoint[];
  threadSeries?: threadPoint[];
  cacheSeries?: cachePoint[];
}

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard2.component.html',
  styleUrls: ['./dashboard2.component.scss'],
})
export class DashboardComponent implements OnInit, OnDestroy {
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

  zoom: number = 1;
  reloadDataRef: any;
  lastLoginLog: Array<AA1211LastLoginLog> = [];

  private apiSubscription!: Subscription;
  destroy$ = new Subject<void>();

  dataPool: DataNode[] = [];
  reqChart: any[] = [];
  tpsChart: any[] = [];
  dbChart: any[] = [];
  esChart: any[] = [];
  cpuChart: any[] = [];
  memChart: any[] = [];
  threadChart: any[] = [];
  cacheChart: any[] = [];

  keeperIP?: string;
  keeperPort?: string;

  constructor(
    private alert: AlertService,
    private toolService: ToolService,
    private aboutService: AboutService,
    private translate: TranslateService,
    private serverService: ServerService,
    private ngxService: NgxUiLoaderService,
    private dialogService: DialogService,
    private messageService: MessageService,
    private router: Router
  ) {}

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  @HostListener('window:resize')
  onResize() {
    // this.reqChart.forEach((elm) => {
    //   elm.nodeEle.resize();
    // });
    this.cacheChart.forEach((elm) => {
      elm.nodeEle.resize();
    });
    this.tpsChart.forEach((elm) => {
      elm.nodeEle.resize();
    });
    this.dbChart.forEach((elm) => {
      elm.nodeEle.resize();
    });
    this.esChart.forEach((elm) => {
      elm.nodeEle.resize();
    });
    this.cpuChart.forEach((elm) => {
      elm.nodeEle.resize();
    });
    this.memChart.forEach((elm) => {
      elm.nodeEle.resize();
    });
    this.threadChart.forEach((elm) => {
      elm.nodeEle.resize();
    });
  }

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
    this.queryRealtimeDashboardData();

    this.getKeeperInfo();
  }

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

  lastLoginLogList: Array<AA1212LastLoginLog> = [];
  dataList: Array<AA1212RespItem> = [];
  queryRealtimeDashboardData() {
    this.apiSubscription = timer(0, 2000)
      .pipe(
        // take(10), //給予上限
        switchMap(() =>
          this.serverService.queryRealtimeDashboardData({}).pipe(
            catchError((err) => {
              //   console.error('API 發生錯誤:', err);
              return of(null); // 錯誤處理，避免停止循環
            })
          )
        ),
        // take(2),
        takeUntil(this.destroy$)
      )
      .subscribe((res) => {
        if (res && this.toolService.checkDpSuccess(res.ResHeader)) {
          // console.log(res.RespBody)
          this.dataList = res.RespBody?.dataList ?? [];
          if (this.dataList.length > 0) {
            this.dataList = this.dataList.map((item) => {
              const cleanedItem = { ...item };
              const urlContainingLists = [
                'badAttemptList',
                'inclundeFailFastList',
                'inclundeFailSlowList',
                'exclundeFailFastList',
                'exclundeFailSlowList',
              ];

              urlContainingLists.forEach((listName) => {
                if (
                  cleanedItem[listName] &&
                  Array.isArray(cleanedItem[listName])
                ) {
                  cleanedItem[listName] = cleanedItem[listName].map(
                    (entry) => ({
                      ...entry,
                      uri: entry.uri ? entry.uri.replace(/\n/g, '') : entry.uri,
                    })
                  );
                }
              });

              return cleanedItem;
            });

            this.dataList.forEach((node) => {
              this.addData(this.dataPool, node);
            });
            this.checkChartStatus();
          }

          this.lastLoginLogList = res.RespBody.lastLoginLogList;
        }
      });
  }

  checkChartStatus() {
    this.generateChart();
  }

  addData(pool: DataNode[], nodeData: AA1212RespItem) {
    const existingNode = pool.find(
      (node) => node.nodeName === nodeData.nodeName
    );
    // 產生資料的時間
    const dataGenerateTime = this.toolService.setformate(
      new Date(),
      'HH:mm:ss'
    );

    // Total Request 圖表資料
    // let newReqPoint = {
    //   time: dataGenerateTime,
    //   success: nodeData.success?.success,
    //   fail: nodeData.fail?.fail,
    //   total: nodeData.success?.total
    //     ? nodeData.success?.total
    //     : nodeData.fail?.total,
    // } as reqPoint;

    // Cache
    let newCachePoint = {
      time: dataGenerateTime,
      dao: nodeData.cache?.dao,
      fixed: nodeData.cache?.fixed,
      rcd: nodeData.cache?.rcd,
    } as cachePoint;

    // TPS
    let newTpsPoint = {
      time: dataGenerateTime,
      req: nodeData.reqTps?.replace('/s', ''),
      resp: nodeData.respTps?.replace('/s', ''),
    } as tpsPoint;

    // DB connection
    let newDbPoint = {
      time: dataGenerateTime,
      active: nodeData.db?.active,
      waiting: nodeData.db?.waiting,
      idle: nodeData.db?.idle,
      total: nodeData.db?.total,
    } as dbPoint;

    //quene ES
    const esQueneValue = this.parseQueneEsString(nodeData.queue?.es);
    let newEsPoint = {
      time: dataGenerateTime,
      current: esQueneValue.current,
      discarded: esQueneValue.discarded,
    } as esPoint;

    //cpuUsage
    let newCpuUsagePoint = {
      time: dataGenerateTime,
      cpuUsage: nodeData.nodeInfo?.cpuUsage.replace('%', ''),
    } as cpuUsagePoint;

    //mem
    let newMemPoint = {
      time: dataGenerateTime,
      memFree: nodeData.nodeInfo?.memFree?.replace(/[, MB]/g, ''),
      memTotal: nodeData.nodeInfo?.memTotal?.replace(/[, MB]/g, ''),
      memMax: nodeData.nodeInfo?.memMax?.replace(/[, MB]/g, ''),
    } as memPoint;

    //thread
    let newThreadPoint = {
      time: dataGenerateTime,
      highwayActvieCount: nodeData.apiThreadStatus?.highwayActvieCount,
      countryRoadActvieCount: nodeData.apiThreadStatus?.countryRoadActvieCount,
    } as threadPoint;

    if (existingNode) {
      // 3.有新資料，就推進既有的dataSeries陣列
      // existingNode.reqSeries.push(newReqPoint);
      existingNode.tpsSeries.push(newTpsPoint);
      existingNode.dbSeries?.push(newDbPoint);
      existingNode.esSeries?.push(newEsPoint);
      existingNode.cpuSeries?.push(newCpuUsagePoint);
      existingNode.memSeries?.push(newMemPoint);
      existingNode.threadSeries?.push(newThreadPoint);
      existingNode.cacheSeries?.push(newCachePoint);

      //控制每張圖節點資料資料上限
      // if (existingNode.reqSeries.length > 30) existingNode.reqSeries.shift();
      if (existingNode.cacheSeries && existingNode.cacheSeries.length > 30)
        existingNode.cacheSeries.shift();
      if (existingNode.tpsSeries.length > 30) existingNode.tpsSeries.shift();
      if (existingNode.dbSeries && existingNode.dbSeries.length > 30)
        existingNode.dbSeries.shift();
      if (existingNode.esSeries && existingNode.esSeries.length > 30)
        existingNode.esSeries.shift();
      if (existingNode.cpuSeries && existingNode.cpuSeries.length > 30)
        existingNode.cpuSeries.shift();
      if (existingNode.memSeries && existingNode.memSeries.length > 30)
        existingNode.memSeries.shift();
      // console.log(`已更新節點 '${nodeData.nodeName}' 的資料。`);
    } else {
      // 4. 如果沒找到，就建立一個新物件並推進 pool 陣列
      const newNode: DataNode = {
        nodeName: nodeData.nodeName!,
        // reqSeries: [newReqPoint],
        tpsSeries: [newTpsPoint],
        dbSeries: [newDbPoint],
        esSeries: [newEsPoint],
        cpuSeries: [newCpuUsagePoint],
        memSeries: [newMemPoint],
        threadSeries: [newThreadPoint],
        cacheSeries: [newCachePoint],
      };
      pool.push(newNode);
      // console.log(`已建立新節點 '${nodeData.nodeName}' 並加入資料。`);
    }
  }

  trackByNodeName(index: number, item: any): string {
    return item.nodeName;
  }

  generateChart() {
    setTimeout(() => {});
    this.dataPool.forEach((node) => {
      this.procTpsChart(node);
      // this.procReqChart(node);
      this.procDbChart(node);
      this.procEsChart(node);
      this.procCPUChart(node);
      this.procMemChart(node);
      this.procThreadChart(node);
      this.procCacheChart(node);
    });
  }

  procCacheChart(node) {
    // threadChart
    const cacheChartItem = this.cacheChart.find(
      (c) => c.nodeName === 'cacheChart_' + node.nodeName
    );
    if (cacheChartItem && cacheChartItem.nodeEle) {
      //更新
      cacheChartItem.nodeEle.setOption(
        {
          dataset: {
            source: node.cacheSeries, // ⚠️ 確保這裡資料格式正確
          },
        },
        {
          notMerge: false,
          replaceMerge: ['dataset'], // 只替換 dataset，不重建整個 option
        }
      );
    }
    //建立
    else {
      setTimeout(() => {
        // threadChart
        const targetEle = document.getElementById(
          'cacheChart_' + node.nodeName
        );
        if (targetEle) {
          let tarChart = echarts.init(targetEle);
          this.cacheChart.push({
            nodeName: 'cacheChart_' + node.nodeName,
            nodeEle: tarChart,
          });

          let option = {
            zoom: 1,
            tooltip: {
              trigger: 'axis',
            },
            legend: {
              data: ['dao', 'fixed', 'rcd'],
            },
            grid: {
              left: '3%',
              right: '3%',
              bottom: '3%',
              containLabel: true,
            },
            dataset: {
              source: node.cacheSeries,
            },
            xAxis: {
              type: 'category',
              boundaryGap: false,
              axisLabel: {
                fontSize: 8,
              },
            },
            yAxis: {
              type: 'value',
            },
            series: [
              {
                color: '#ff7f0e',
                seriesLayoutBy: 'row',
                name: 'dao',
                type: 'line',
                lineStyle: {
                  width: 3,
                },
                showSymbol: false,
              },
              {
                color: '#2ca02c',
                seriesLayoutBy: 'row',
                name: 'fixed',
                type: 'line',
                lineStyle: {
                  width: 3,
                },
                showSymbol: false,
              },
              {
                color: '#1f77b4',
                seriesLayoutBy: 'row',
                name: 'rcd',
                type: 'line',
                lineStyle: {
                  width: 3,
                },
                showSymbol: false,
              },
            ],
          };
          tarChart.setOption(option);
        }
      }, 0);
    }
  }

  procThreadChart(node) {
    // threadChart
    const threadChartItem = this.threadChart.find(
      (c) => c.nodeName === 'threadChart_' + node.nodeName
    );
    if (threadChartItem && threadChartItem.nodeEle) {
      //更新
      threadChartItem.nodeEle.setOption(
        {
          dataset: {
            source: node.threadSeries, // ⚠️ 確保這裡資料格式正確
          },
        },
        {
          notMerge: false,
          replaceMerge: ['dataset'], // 只替換 dataset，不重建整個 option
        }
      );
    }
    //建立
    else {
      setTimeout(() => {
        // threadChart
        const targetEle = document.getElementById(
          'threadChart_' + node.nodeName
        );
        if (targetEle) {
          let tarChart = echarts.init(targetEle);
          this.threadChart.push({
            nodeName: 'threadChart_' + node.nodeName,
            nodeEle: tarChart,
          });

          let option = {
            zoom: 1,
            tooltip: {
              trigger: 'axis',
            },
            legend: {
              data: ['Country road', 'Highway'],
            },
            grid: {
              left: '3%',
              right: '3%',
              bottom: '3%',
              containLabel: true,
            },
            dataset: {
              source: node.threadSeries,
            },
            xAxis: {
              type: 'category',
              boundaryGap: false,
              axisLabel: {
                fontSize: 8,
              },
            },
            yAxis: {
              // type: 'value',
            },

            series: [
              {
                color: '#14c431',
                seriesLayoutBy: 'row',
                name: 'Country road',
                data: node.threadSeries.countryRoadActvieCount,
                type: 'line',
                lineStyle: {
                  width: 3,
                },
                showSymbol: false,
              },
              {
                color: '#c93295',
                seriesLayoutBy: 'row',
                name: 'Highway',
                data: node.threadSeries.highwayActvieCount,
                type: 'line',
                lineStyle: {
                  width: 3,
                },
                showSymbol: false,
              },
            ],
          };
          tarChart.setOption(option);
        }
      }, 0);
    }
  }

  procMemChart(node) {
    // memChart
    const memChartItem = this.memChart.find(
      (c) => c.nodeName === 'memChart_' + node.nodeName
    );
    if (memChartItem && memChartItem.nodeEle) {
      //更新
      memChartItem.nodeEle.setOption(
        {
          dataset: {
            source: node.memSeries, // ⚠️ 確保這裡資料格式正確
          },
        },
        {
          notMerge: false,
          replaceMerge: ['dataset'], // 只替換 dataset，不重建整個 option
        }
      );
    }
    //建立
    else {
      setTimeout(() => {
        // memChart
        const targetEle = document.getElementById('memChart_' + node.nodeName);
        if (targetEle) {
          let tarChart = echarts.init(targetEle);
          this.memChart.push({
            nodeName: 'memChart_' + node.nodeName,
            nodeEle: tarChart,
          });

          let option = {
            zoom: 1,
            tooltip: {
              trigger: 'axis',
              formatter: (params) => {
                let html = `<span>${params[0].name}</span><br/>`;
                html += `<div>
                      ${params[0].marker}
                      <span style="margin-right: 16px;">${
                        params[0].seriesName
                      }</span>
                      <span style="float: right; font-weight: bold;">${
                        (this,
                        this.toolService.numberComma(params[0].value.memFree))
                      } MB</span>
                    </div>
                    <div>
                      ${params[1].marker}
                      <span style="margin-right: 16px;">${
                        params[1].seriesName
                      }</span>
                      <span style="float: right; font-weight: bold;">${
                        (this,
                        this.toolService.numberComma(params[1].value.memTotal))
                      } MB</span>
                    </div>
                    <div>
                      ${params[2].marker}
                      <span style="margin-right: 16px;">${
                        params[2].seriesName
                      }</span>
                      <span style="float: right; font-weight: bold;">${
                        (this,
                        this.toolService.numberComma(params[2].value.memMax))
                      } MB</span>
                    </div>
                 `;
                return html;
              },
            },
            legend: {
              data: ['Free', 'Total', 'Max'],
              // data: ['memFree', 'memTotal', 'memMax'],
            },
            grid: {
              left: '3%',
              right: '8%',
              bottom: '3%',
              containLabel: true,
            },
            dataset: {
              source: node.memSeries,
            },
            xAxis: {
              type: 'category',
              boundaryGap: false,
              axisLabel: {
                fontSize: 8,
              },
            },
            yAxis: {
              // type: 'value',
            },

            series: [
              {
                color: '#28a745',
                seriesLayoutBy: 'row',
                name: 'Free',
                // name: 'memFree',
                data: node.memSeries.memFree,
                type: 'line',
                lineStyle: {
                  width: 3,
                },
                areaStyle: {
                  color: {
                    type: 'linear',
                    x: 0,
                    y: 1,
                    x2: 0,
                    y2: 0,
                    colorStops: [
                      {
                        offset: 1,
                        color: 'rgba(40, 167, 69, 1)',
                      },
                      {
                        offset: 0.7,
                        color: 'rgba(40, 167, 69, 0.7)',
                      },
                      {
                        offset: 0.4,
                        color: 'rgba(40, 167, 69, 0.4)',
                      },
                      {
                        offset: 0,
                        color: 'rgba(40, 167, 69, 0.05)',
                      },
                    ],
                    global: false,
                  },
                },
                showSymbol: false,
              },
              {
                color: '#007bff',
                seriesLayoutBy: 'row',
                // name: 'memTotal',
                name: 'Total',
                data: node.memSeries.memTotal,
                type: 'line',
                lineStyle: {
                  width: 3,
                },
                showSymbol: false,
              },
              {
                color: '#dc3545',
                seriesLayoutBy: 'row',
                // name: 'memMax',
                name: 'Max',
                data: node.memSeries.memMax,
                type: 'line',
                lineStyle: {
                  width: 3,
                },
                showSymbol: false,
              },
            ],
          };
          tarChart.setOption(option);
        }
      }, 0);
    }
  }

  //procCPUChart
  procCPUChart(node) {
    // cpuChart
    const cpuChartItem = this.cpuChart.find(
      (c) => c.nodeName === 'cpuChart_' + node.nodeName
    );
    if (cpuChartItem && cpuChartItem.nodeEle) {
      //更新
      cpuChartItem.nodeEle.setOption(
        {
          dataset: {
            source: node.cpuSeries, // ⚠️ 確保這裡資料格式正確
          },
        },
        {
          notMerge: false,
          replaceMerge: ['dataset'], // 只替換 dataset，不重建整個 option
        }
      );
    }
    //建立
    else {
      setTimeout(() => {
        // cpuChart
        const targetEle = document.getElementById('cpuChart_' + node.nodeName);
        if (targetEle) {
          let tarChart = echarts.init(targetEle);
          this.cpuChart.push({
            nodeName: 'cpuChart_' + node.nodeName,
            nodeEle: tarChart,
          });

          let option = {
            zoom: 1,
            tooltip: {
              trigger: 'axis',
              formatter: (params) => {
                let html = `<span>${params[0].name}</span><br/>`;
                params.forEach((item) => {
                  html += `
                    <div>
                      ${item.marker}
                      <span style="margin-right: 16px;">${item.seriesName}</span>
                      <span style="float: right; font-weight: bold;">${item.value.cpuUsage}%</span>
                    </div>
                  `;
                });

                return html;
              },
            },
            legend: {
              data: ['CPU Usage'],
            },
            grid: {
              left: '3%',
              right: '8%',
              bottom: '3%',
              containLabel: true,
            },
            dataset: {
              source: node.cpuSeries,
            },
            xAxis: {
              type: 'category',
              boundaryGap: false,
              axisLabel: {
                fontSize: 8,
              },
              data: node.cpuSeries.time,
            },
            yAxis: {
              // type: 'value',
            },

            series: [
              {
                color: '#14b8a6',
                seriesLayoutBy: 'row',
                name: 'CPU Usage',
                data: node.cpuSeries.cpuUsage,
                type: 'line',
                lineStyle: {
                  width: 3,
                },
                areaStyle: {
                  color: {
                    type: 'linear',
                    x: 0,
                    y: 1,
                    x2: 0,
                    y2: 0,
                    colorStops: [
                      {
                        offset: 1,
                        color: 'rgba(20, 184, 166, 1)',
                      },
                      {
                        offset: 0.7,
                        color: 'rgba(20, 184, 166, 0.7)',
                      },
                      {
                        offset: 0.4,
                        color: 'rgba(20, 184, 166, 0.4)',
                      },
                      {
                        offset: 0,
                        color: 'rgba(20, 184, 166, 0.05)',
                      },
                    ],
                    global: false,
                  },
                },
                showSymbol: false,
                // markLine: {
                //   symbol: 'none', // 標記線兩端不顯示符號
                //   data: [
                //     // 第一條標記線：數值 100 的實線
                //     {
                //       yAxis: 0.05,
                //       name: '100%',
                //       lineStyle: {
                //         type: 'solid', // 類型：實線
                //         color: '#d9534f', // 線條顏色
                //       },
                //       label: {
                //         position: 'start',
                //       },
                //     },
                //     // 第二條標記線：數值 80 的虛線
                //     {
                //       yAxis: 0.03,
                //       name: '80%',
                //       lineStyle: {
                //         type: 'dashed', // 類型：虛線
                //         color: '#f0ad4e', // 線條顏色
                //       },
                //       label: {
                //         position: 'start',
                //       },
                //     },
                //   ],
                // },
              },
            ],
          };
          tarChart.setOption(option);
        }
      }, 0);
    }
  }
  //reqChart
  procReqChart(node) {
    // reqChart
    const reqChartItem = this.reqChart.find(
      (c) => c.nodeName === 'reqChart_' + node.nodeName
    );
    if (reqChartItem && reqChartItem.nodeEle) {
      //更新
      reqChartItem.nodeEle.setOption(
        {
          dataset: {
            source: node.reqSeries, // ⚠️ 確保這裡資料格式正確
          },
        },
        {
          notMerge: false,
          replaceMerge: ['dataset'], // 只替換 dataset，不重建整個 option
        }
      );
    }
    //建立
    else {
      setTimeout(() => {
        // ReqChart
        const targetEle = document.getElementById('reqChart_' + node.nodeName);
        if (targetEle) {
          let tarChart = echarts.init(targetEle);
          this.reqChart.push({
            nodeName: 'reqChart_' + node.nodeName,
            nodeEle: tarChart,
          });

          let option = {
            zoom: 1,
            tooltip: {
              trigger: 'axis',
              // formatter: (params) => {
              //   return `
              //         ${params[0].name} <br />
              //         ${params[0].marker} ${params[0].seriesName}:   ${params[0].value.success} <br />
              //         ${params[1].marker} ${params[1].seriesName}:   ${params[0].value.fail} <br />
              //         ${params[2].marker} ${params[2].seriesName}:   ${params[1].value.total}
              //         `;
              // },
            },
            legend: {
              data: ['Success', 'Fail', 'Total'],
            },
            grid: {
              left: '3%',
              right: '3%',
              bottom: '3%',
              containLabel: true,
            },
            dataset: {
              source: node.reqSeries,
            },
            xAxis: {
              type: 'category',
              boundaryGap: false,
              axisLabel: {
                fontSize: 8,
              },
            },
            yAxis: {
              // type: 'value',
            },

            series: [
              {
                color: '#14b8a6',
                seriesLayoutBy: 'row',
                name: 'Success',
                data: node.reqSeries.success,
                type: 'line',
                lineStyle: {
                  width: 3,
                },
                areaStyle: {
                  color: {
                    type: 'linear',
                    x: 0,
                    y: 1,
                    x2: 0,
                    y2: 0,
                    colorStops: [
                      {
                        offset: 1,
                        color: 'rgba(20, 184, 166, 1)',
                      },
                      {
                        offset: 0.7,
                        color: 'rgba(20, 184, 166, 0.7)',
                      },
                      {
                        offset: 0.4,
                        color: 'rgba(20, 184, 166, 0.4)',
                      },
                      {
                        offset: 0,
                        color: 'rgba(20, 184, 166, 0.05)',
                      },
                    ],
                    global: false,
                  },
                },
                showSymbol: false,
              },
              {
                color: '#ff8780',
                seriesLayoutBy: 'row',
                name: 'Fail',
                data: node.reqSeries.fail,
                type: 'line',
                lineStyle: {
                  width: 3,
                },
                showSymbol: false,
              },
              {
                color: '#7c9194',
                seriesLayoutBy: 'row',
                name: 'Total',
                data: node.reqSeries.total,
                type: 'line',
                lineStyle: {
                  width: 3,
                },
                showSymbol: false,
              },
            ],
          };
          tarChart.setOption(option);
        }
      }, 0);
    }
  }

  // tpsChart
  procTpsChart(node) {
    const tpsChartItem = this.tpsChart.find(
      (c) => c.nodeName === 'tpsChart_' + node.nodeName
    );
    if (tpsChartItem && tpsChartItem.nodeEle) {
      //更新
      tpsChartItem.nodeEle.setOption(
        {
          dataset: {
            source: node['tpsSeries'], // ⚠️ 確保這裡資料格式正確
          },
        },
        {
          notMerge: false,
          replaceMerge: ['dataset'], // 只替換 dataset，不重建整個 option
        }
      );
    }
    //建立
    else {
      const targetEle = document.getElementById('tpsChart_' + node.nodeName);
      if (targetEle) {
        let tarChart = echarts.init(targetEle);
        this.tpsChart.push({
          nodeName: 'tpsChart_' + node.nodeName,
          nodeEle: tarChart,
        });

        let option = {
          zoom: 1,
          tooltip: {
            trigger: 'axis',
            // formatter: (params) => {
            //   return `
            //           ${params[0].name} <br />
            //           ${params[0].marker} ${params[0].seriesName}:   ${params[0].value.req}/s <br />
            //           ${params[1].marker} ${params[1].seriesName}:   ${params[1].value.resp}/s
            //           `;
            // },
          },
          legend: {
            data: ['req', 'resp'],
          },
          grid: {
            left: '3%',
            right: '8%',
            bottom: '3%',
            containLabel: true,
          },
          dataset: {
            source: node.tpsSeries,
          },
          xAxis: {
            type: 'category',
            boundaryGap: false,
            axisLabel: {
              fontSize: 8,
            },
          },
          yAxis: {
            type: 'value',
          },

          series: [
            {
              color: '#3B82F6',
              seriesLayoutBy: 'row',
              name: 'req',
              type: 'line',
              lineStyle: {
                width: 3,
              },
              areaStyle: {
                color: {
                  type: 'linear',
                  x: 0,
                  y: 1,
                  x2: 0,
                  y2: 0,
                  colorStops: [
                    {
                      offset: 1,
                      color: 'rgba(59, 130, 246, 0.7)',
                    },
                    {
                      offset: 0.5,
                      color: 'rgba(59, 130, 246, 0.3)',
                    },
                    {
                      offset: 0,
                      color: 'rgba(59, 130, 246, 0.05)',
                    },
                  ],
                  global: false,
                },
              },

              showSymbol: false,
            },
            {
              color: '#10B981',
              seriesLayoutBy: 'row',
              name: 'resp',
              type: 'line',
              lineStyle: {
                width: 3,
              },
              showSymbol: false,
            },
          ],
        };
        tarChart.setOption(option);
      }
    }
  }
  // DbChart
  procDbChart(node) {
    const dbChartItem = this.dbChart.find(
      (c) => c.nodeName === 'dbChart_' + node.nodeName
    );
    if (dbChartItem && dbChartItem.nodeEle) {
      //更新
      dbChartItem.nodeEle.setOption(
        {
          dataset: {
            source: node['dbSeries'], // ⚠️ 確保這裡資料格式正確
          },
        },
        {
          notMerge: false,
          replaceMerge: ['dataset'], // 只替換 dataset，不重建整個 option
        }
      );
    }
    //建立
    else {
      const targetEle = document.getElementById('dbChart_' + node.nodeName);
      if (targetEle) {
        let tarChart = echarts.init(targetEle);
        this.dbChart.push({
          nodeName: 'dbChart_' + node.nodeName,
          nodeEle: tarChart,
        });

        let option = {
          zoom: 1,
          tooltip: {
            trigger: 'axis',
            // formatter: (params) => {
            //   return `
            //           ${params[0]?.name} <br />
            //           ${params[0]?.marker} ${params[0]?.seriesName}:   ${params[0]?.value.active} <br />
            //           ${params[1]?.marker} ${params[1]?.seriesName}:   ${params[1]?.value.waiting} <br />
            //           ${params[2]?.marker} ${params[2]?.seriesName}:   ${params[2]?.value.idle} <br />
            //           ${params[3]?.marker} ${params[3]?.seriesName}:   ${params[3]?.value.total}
            //           `;
            // },
          },
          legend: {
            data: ['active', 'waiting', 'idle', 'total'],
          },
          grid: {
            left: '3%',
            right: '3%',
            bottom: '3%',
            containLabel: true,
          },
          dataset: {
            source: node.dbSeries,
          },
          xAxis: {
            type: 'category',
            boundaryGap: false,
            axisLabel: {
              fontSize: 8,
            },
          },
          yAxis: {
            type: 'value',
          },

          series: [
            {
              color: '#2ECC71',
              seriesLayoutBy: 'row',
              name: 'active',
              type: 'line',
              lineStyle: {
                width: 3,
              },
              areaStyle: {
                color: {
                  type: 'linear',
                  x: 0,
                  y: 1,
                  x2: 0,
                  y2: 0,
                  colorStops: [
                    {
                      offset: 1,
                      color: 'rgba(46, 204, 113, 0.8)', // 100% 的顏色（濃）
                    },
                    {
                      offset: 0.5,
                      color: 'rgba(46, 204, 113, 0.3)', // 中間過渡
                    },
                    {
                      offset: 0,
                      color: 'rgba(46, 204, 113, 0.05)', // 0% 的顏色（淡）
                    },
                  ],
                  global: false,
                },
              },
              showSymbol: false,
            },
            {
              color: '#F1C40F',
              seriesLayoutBy: 'row',
              name: 'waiting',
              type: 'line',
              lineStyle: {
                width: 3,
              },
              showSymbol: false,
            },
            {
              color: '#727687',
              seriesLayoutBy: 'row',
              name: 'idle',
              type: 'line',
              lineStyle: {
                width: 3,
              },
              showSymbol: false,
            },
            {
              color: '#a3a6b5',
              seriesLayoutBy: 'row',
              name: 'total',
              type: 'line',
              lineStyle: {
                width: 3,
              },
              showSymbol: false,
            },
          ],
        };
        tarChart.setOption(option);
      }
    }
  }

  //esChart
  procEsChart(node) {
    // esChart
    const esChartItem = this.esChart.find(
      (c) => c.nodeName === 'esChart_' + node.nodeName
    );
    if (esChartItem && esChartItem.nodeEle) {
      //更新
      esChartItem.nodeEle.setOption(
        {
          dataset: {
            source: node.esSeries, // ⚠️ 確保這裡資料格式正確
          },
        },
        {
          notMerge: false,
          replaceMerge: ['dataset'], // 只替換 dataset，不重建整個 option
        }
      );
    }
    //建立
    else {
      setTimeout(() => {
        // esChart
        const targetEle = document.getElementById('esChart_' + node.nodeName);
        if (targetEle) {
          let tarChart = echarts.init(targetEle);
          this.esChart.push({
            nodeName: 'esChart_' + node.nodeName,
            nodeEle: tarChart,
          });

          let option = {
            zoom: 1,
            tooltip: {
              trigger: 'axis',
              // formatter: (params) => {
              //   return `
              //         ${params[0].name} <br />
              //         ${params[0].marker} ${params[0].seriesName}:   ${params[0].value.value1} <br />
              //         ${params[1].marker} ${params[1].seriesName}:   ${params[0].value.value2} <br />
              //         ${params[2].marker} ${params[2].seriesName}:   ${params[1].value.value3}
              //         `;
              // },
            },
            legend: {
              data: ['Current', 'Discarded'],
            },
            grid: {
              left: '3%',
              right: '3%',
              bottom: '3%',
              containLabel: true,
            },
            dataset: {
              source: node.esSeries,
            },
            xAxis: {
              type: 'category',
              boundaryGap: false,
              axisLabel: {
                fontSize: 8,
              },
            },
            yAxis: {
              // type: 'value',
            },

            series: [
              {
                color: '#4CC9F0',
                seriesLayoutBy: 'row',
                name: 'Current',
                type: 'line',
                data: node.esSeries.current,
                lineStyle: {
                  width: 3,
                },
                showSymbol: false,
              },
              {
                color: '#ff5714',
                seriesLayoutBy: 'row',
                name: 'Discarded',
                data: node.esSeries.discarded,
                type: 'line',
                lineStyle: {
                  width: 3,
                },
                showSymbol: false,
              },
            ],
          };
          tarChart.setOption(option);
        }
      }, 0);
    }
  }

  /**
   * 解析特定格式的字串 "v1(v2)(v3)"
   * @param inputString - 要解析的輸入字串，例如 "0(-1)(1)"
   * @returns - 回傳一個包含三個值的物件，如果格式不符則回傳 null
   */
  parseQueneEsString(inputString?: string): esPoint {
    if (!inputString) {
      return {
        current: '',
        discarded: '',
      };
    }

    // 正規表示式：
    // ^                  - 字串的開頭
    // ([-\d.]+)          - 捕獲群組 1 (v1/current): 匹配一個或多個數字、負號或小數點
    // \s*                - 匹配零個或多個空格 (space, tab, etc.)
    // \(                 - 匹配一個字面上的左括號 "("
    // ([-\d.]+)          - 捕獲群組 2 (v2/discarded): 匹配括號內的內容
    // \)                 - 匹配一個字面上的右括號 ")"
    // $                  - 字串的結尾

    // 修正：正規表達式只匹配 v1(v2)
    const regex = /^([-\d.]+)\s*\(([-\d.]+)\)$/;
    const match = inputString.match(regex);

    // 如果沒有匹配成功，match 會是 null
    if (!match) {
      return {
        current: '',
        discarded: '',
      };
    }

    // match 陣列的結構如下：
    // match[0] 是整個匹配到的字串，例如 "0 (-1)(1)"
    // match[1] 是第一個捕獲群組的內容，例如 "0"
    // match[2] 是第二個捕獲群組的內容，例如 "-1"
    const result: esPoint = {
      current: match[1],
      discarded: match[2],
    };

    return result;
  }

  numberComma(tar?: string) {
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

  copyToClipboard(text: string): void {
    if (navigator.clipboard) {
      navigator.clipboard
        .writeText(text)
        .then(async () => {
          console.log('Copied to clipboard!');
          const code = ['copy', 'message.success'];
          const dict = await this.toolService.getDict(code);
          this.messageService.add({
            severity: 'success',
            summary: `${dict['copy']} `,
            detail: `${dict['copy']} ${dict['message.success']}!`,
          });
          // 可改成顯示提示訊息或 toast
        })
        .catch((err) => {
          console.error('Failed to copy:', err);
        });
    } else {
      // 備援處理
      console.warn('This browser does not support the Clipboard API');
    }
  }

  // api異常報表
  navigateReport() {
    this.router.navigate(['/ac05/ac0503']);
  }

  navigateSetting() {
    const options: NavigationExtras = {
      queryParams: {
        keyword: 'DGRKEEPER',
      },
    };
    this.router.navigate(['/lb00/lb0001'], options);
  }

  getKeeperIP() {
    this.serverService.queryTsmpSettingDetail({id:'DGRKEEPER_IP'}).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.keeperIP = res.RespBody.value;
      }
    });
  }

  getKeeperPort() {
    this.serverService.queryTsmpSettingDetail({id:'DGRKEEPER_PORT'}).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.keeperPort = res.RespBody.value;
      }
    });
  }

  getKeeperInfo(){
    this.getKeeperIP();
    this.getKeeperPort();
  }
}

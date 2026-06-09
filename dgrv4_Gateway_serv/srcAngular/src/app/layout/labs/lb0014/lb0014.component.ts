import { Component, OnDestroy, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  UntypedFormControl,
} from '@angular/forms';
import { ToolService } from 'src/app/shared/services/tool.service';
import { ListService } from 'src/app/shared/services/api-list.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { AlertService } from 'src/app/shared/services/alert.service';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { ConfirmationService, MessageService } from 'primeng/api';
import { CommonModule } from '@angular/common';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { SharedPipesModule } from 'src/app/shared/pipes/shared-pipes.module';
import { TokenExpiredGuard } from 'src/app/shared';
import { DPB0302RespCurrentSyncInfo } from 'src/app/models/api/ServerService/dpb0302.interface';
import {
  catchError,
  of,
  Subject,
  Subscription,
  switchMap,
  takeUntil,
  timer,
} from 'rxjs';

@Component({
  selector: 'app-lb0014',
  templateUrl: './lb0014.component.html',
  styleUrls: ['./lb0014.component.css'],
  providers: [ConfirmationService, TokenExpiredGuard],
  imports: [
    CommonModule,
    PrimengModule,
    SharedModule,
    SharedPipesModule,
    ReactiveFormsModule,
    FormsModule,
  ],
  standalone: true,
})
export class Lb0014Component
  extends BaseComponent
  implements OnInit, OnDestroy
{
  currentTitle = this.title;
  pageNum: number = 1;
  form!: FormGroup;
  srcNodeList: { label: string; value: string }[] = [];
  tarNodeList: { label: string; value: string }[] = [];
  btnStatus: boolean = true;
  currentSync?: DPB0302RespCurrentSyncInfo;
  syncSubscription?: Subscription;

  apiSubscription!: Subscription;
  destroy$ = new Subject<void>();
  latestStatus?: string;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private listService: ListService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private serverService: ServerService,
    private alertService: AlertService,
    private ngxSrvice: NgxUiLoaderService,
  ) {
    super(route, tr);
  }

  ngOnInit() {
    this.form = this.fb.group({
      sourceId: new UntypedFormControl(''),
      targetIds: new UntypedFormControl({ value: [], disabled: true }),
    });

    this.form.get('sourceId')?.valueChanges.subscribe((value) => {
      this.tarNodeList = this.srcNodeList.filter(
        (node) => node.value !== value,
      );
      this.form.get('targetIds')?.setValue([]);
      if (this.tarNodeList.length === 0) {
        this.form.get('targetIds')?.disable();
      } else {
        this.form.get('targetIds')?.enable();
      }
    });

    this.serverService.dbSyncNodeInfo_ignore1298({}).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.srcNodeList = res.RespBody.nodeInfoList.map((node) => ({
          label: node.nodeName,
          value: node.nodeId,
        }));
        this.btnStatus = res.RespBody.btnStatus;
      }
    });

    this.syncSubscription = timer(0, 1000)
      .pipe(
        switchMap(() =>
          this.serverService.dbSyncNodeInfo_ignore1298({}).pipe(
            catchError((err) => {
              //   console.error('API 發生錯誤:', err);
              return of(null); // 錯誤處理，避免停止循環
            }),
          ),
        ),
        takeUntil(this.destroy$),
      )
      .subscribe((res) => {
        if (res && this.toolService.checkDpSuccess(res.ResHeader)) {
          this.btnStatus = res.RespBody.btnStatus;
          this.currentSync = res.RespBody.currentSync;
          if (
            this.latestStatus !== res.RespBody?.currentSync?.status &&
            res.RespBody?.currentSync?.status == 'DONE'
          ) {
            this.messageService.add({
              severity: 'success',
              summary: `H2 Config Sync`,
              detail: `Progress's done!`
            });
          }
          // console.log(this.latestStatus !== res.RespBody?.currentSync?.status )
          // console.log(res.RespBody?.currentSync?.status == 'DONE')
          // console.log(this.latestStatus)
          this.latestStatus = res.RespBody?.currentSync?.status;
        }
      });
  }

  ngOnDestroy(): void {
    if (this.syncSubscription) {
      this.syncSubscription.unsubscribe();
      this.syncSubscription = undefined;
    }
    this.destroy$.next();
    this.destroy$.complete();
  }

  headerReturn() {
    // this.changePage('query');
  }

  dbSyncTrigger() {
    this.serverService
      .dbSyncTrigger({
        sourceId: this.form.get('sourceId')?.value,
        targetIds: this.form.get('targetIds')?.value,
      })
      .subscribe(() => {});
  }
}

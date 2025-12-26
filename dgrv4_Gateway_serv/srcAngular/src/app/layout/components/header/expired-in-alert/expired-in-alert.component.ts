import { Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Subscription } from 'rxjs';
import { ToolService } from 'src/app/shared/services/tool.service';

@Component({
  selector: 'app-expired-in-alert',
  templateUrl: './expired-in-alert.component.html',
  styleUrls: ['./expired-in-alert.component.css'],
})
export class ExpiredInAlertComponent implements OnInit {
  aliveSec: number = 0;
  aliveStr: string = '';
  subscription?: Subscription;

  constructor(
    private toolService: ToolService,
    private config: DynamicDialogConfig,
    public ref: DynamicDialogRef
  ) {}

  ngOnInit() {
    if (this.config.data?.timerStream) {
      this.subscription = this.config.data.timerStream.subscribe((val) => {
        this.aliveSec = val; // 更新畫面上的變數
        this.aliveStr = this.formatTime(this.aliveSec)
      });
    }
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  formatTime(seconds) {
    const hrs = Math.floor(seconds / 3600);
    const mins = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;

    // 補零函式
    const pad = (num) => String(num).padStart(2, '0');

    if (hrs > 0) {
      return `${pad(hrs)} : ${pad(mins)} : ${pad(secs)}`;
    } else {
      return `${pad(mins)} : ${pad(secs)}`;
    }
  }

  onAccept() {
    this.ref.close();
  }
}

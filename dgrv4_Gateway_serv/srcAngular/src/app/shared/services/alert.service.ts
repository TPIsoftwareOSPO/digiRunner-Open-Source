import { Injectable } from '@angular/core';
import { AlertType } from 'src/app/models/common.enum';
import { Observable, from, zip } from 'rxjs';
import { Router } from '@angular/router';
import { DialogService, DynamicDialogRef } from 'primeng/dynamicdialog';
import { CustomAlertComponent } from '../custom-alert/custom-alert.component';

@Injectable({
  providedIn: 'root',
})
export class AlertService {
  private ref: DynamicDialogRef | null = null;
  constructor(private router: Router, private dialogService: DialogService) {}

  openDialog(data: any) {
    // 若對話框已存在，先銷毀它，避免重複開啟
    if (this.ref) {
      this.ref.close();
      this.ref = null;
    }

    // 開啟新的對話框
    this.ref = this.dialogService.open(CustomAlertComponent, {
      data,
      showHeader: false,
    });

    // 監聽對話框關閉時，清除 this.ref，避免 memory leak
    this.ref.onClose.subscribe(() => {
      this.ref = null;
    });
  }

  ok(title: string, text: string, type: AlertType = AlertType.warning, html?: string) {
    this.openDialog({ title, text, type, html });
  }

  error(title: string, text: string, type: AlertType = AlertType.error) {
    this.openDialog({ title, text, type });
  }

  logout(title: string, text: string, type: AlertType = AlertType.success) {
    this.openDialog({ title, text, type });

    // 監聽對話框關閉後，執行登出導頁
    if (this.ref) {
      this.ref.onClose.subscribe(() => {
        this.router.navigateByUrl('/login');
      });
    }
  }
}

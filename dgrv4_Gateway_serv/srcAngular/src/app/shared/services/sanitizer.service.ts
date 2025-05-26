import { Injectable } from '@angular/core';

@Injectable()
export class SanitizerService {
  constructor() {}
  navigateUrl(url: string) {
    window.location.href = url;
      // // 動態建立 <a> 標籤
      // const anchor = document.createElement('a');
      // anchor.href = url; // 設定目標 URL
      // anchor.target = '_self'; // 設定在當前視窗開啟（預設值，可改成 _blank 開啟新視窗）
      // anchor.rel = 'noopener noreferrer'; // 增加安全性（可選）
      // // 將 <a> 元素加入 DOM
      // document.body.appendChild(anchor);
      // // 觸發點擊事件
      // anchor.click();
      // // 移除 <a> 元素
      // document.body.removeChild(anchor);
  }
}

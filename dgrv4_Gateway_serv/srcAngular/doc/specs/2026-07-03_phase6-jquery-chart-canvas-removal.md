# Angular 13→20 技術債整理 Phase 6 子任務：移除 jQuery — Chart Canvas 重建模式

延續 `doc/specs/2026-07-03_phase1-tech-debt-cleanup.md` 六階段技術債整理計畫，接續 file-input、欄位 enable/disable 兩個子任務，本次處理 Phase 6 的第三種 jQuery 模式：Chart.js 圖表的 canvas 重建。

## 修改內容

### 範圍

`src/app/layout/ac13/ac1301` ～ `ac1305`，共 5 個檔案，皆為 API 用量統計報表頁面。

### 根因分析

5 個檔案在 `submitForm()`（重新查詢報表）時都有這段邏輯：

```ts
// 修改前
submitForm() {
    // 清除canvas
    $('#reportChart').remove();
    $('#div_canvas').append('<canvas id="reportChart"></canvas>');
    this.canvas = document.getElementById('reportChart');
    this.canvas.height = '100%';
    this.ctx = this.canvas.getContext('2d');
    ...
    new Chart(this.ctx, { ... });
}
```

**根因**：Chart.js 不允許在同一個已經附掛 Chart 實例的 `<canvas>` 上直接 `new Chart()`（會拋出「Canvas is already in use」錯誤），必須先呼叫舊實例的 `.destroy()`。這份程式碼原本沒有保留 Chart 實例的參考（`new Chart(...)` 是丟棄回傳值的敘述式呼叫），因此改用 jQuery 把整個 `<canvas>` DOM 元素砍掉重建、繞過「同一個 canvas 不能重複建立」的限制，而不是正規地呼叫 `chart.destroy()`。

### 修正方案

改為保留 Chart 實例參考，重繪前呼叫 `.destroy()`，沿用同一個 `<canvas>` 元素（改用 `@ViewChild` 取得，取代 `document.getElementById`）：

```html
<!-- 修改前 -->
<canvas id="reportChart"></canvas>
<!-- 修改後 -->
<canvas id="reportChart" #reportChartRef></canvas>
```

```ts
// 修改後
export class Ac1301Component extends BaseComponent implements OnInit {
  @ViewChild('reportChartRef', { static: true }) reportChartRef!: ElementRef<HTMLCanvasElement>;
  canvas: any;
  ctx: any;
  chart?: Chart;

  ngOnInit() {
    ...
    this.canvas = this.reportChartRef.nativeElement; // 取代 document.getElementById('reportChart')
    this.ctx = this.canvas.getContext('2d');
    ...
  }

  submitForm() {
    // 清除 canvas：改用 chart.destroy() 銷毀舊的 Chart 實例，沿用同一個 canvas 元素
    this.chart?.destroy();
    this.canvas.height = '100%';
    this.ctx = this.canvas.getContext('2d');
    ...
    this.chart = new Chart(this.ctx, { ... }); // 保留實例參考供下次 destroy()
  }
}
```

`<canvas id="reportChart">` 在 5 個檔案的範本中都是無條件渲染（未包在 `*ngIf` 內），因此 `@ViewChild` 可以安全使用 `{ static: true }`，確保在 `ngOnInit()` 就能取得，不需要移到 `ngAfterViewInit()`。

`this.canvas.height = '100%';` 這行既有的可疑寫法（對 canvas 的 `height` IDL 屬性指派一個非數字字串）維持原樣沿用，本次只處理 jQuery 移除，不變更這行既有邏輯的行為，避免範圍外的修正。

### 差異處理

- `ac1301.component.ts` 原本的 `$('#div_canvas').append('<canvas id="reportChart"</canvas>')` 字串本身有 HTML 語法錯誤（漏了 `>`），但由於整段邏輯已經改為不重建 DOM，這個既有錯字連同整段字串一併移除，不影響修正後的行為。
- `ac1301`/`ac1302`/`ac1303` 圖表類型為 `'bar'`，`ac1304`/`ac1305` 為 `'line'`，皆已對應保留原本的 `new Chart(...)` 設定內容，只改「賦值給 `this.chart`」這一處。

## 驗證方式

### 靜態驗證

- `npm run build`（production）：**成功**，無新增錯誤。
- grep 確認 `src/app/layout/ac13/` 目錄下已無殘留 `$(` 呼叫。

### 尚待人工驗證

本環境無可用瀏覽器自動化工具，建議人工測試 `ac1301`～`ac1305` 任一頁面：
1. 首次載入頁面查詢報表，確認圖表正常顯示。
2. **重點**：變更查詢條件（時間區間、API 清單等）後重新送出查詢，確認圖表能正確重繪（新資料顯示正確），且瀏覽器 console 沒有跳出 Chart.js 的「Canvas is already in use」錯誤或其他例外。
3. 連續重新查詢多次，確認沒有記憶體洩漏疑慮（每次重繪前都正確銷毀了上一個 Chart 實例，而非疊加多個實例）。

## 實作紀錄

### 時間軸

1. 使用者選擇 Phase 6 剩餘模式清單中的「Chart canvas 重建」繼續處理。
2. 讀取 `ac1301.component.ts` 完整的 `submitForm()` 與 `ngOnInit()`，確認根因是 Chart.js「同一 canvas 不可重複建立」的限制，且原程式碼選擇用 jQuery 砍掉重建 DOM 元素來繞過限制，而非正規地銷毀舊 Chart 實例。
3. 確認 `ac1302`～`ac1305` 皆為完全相同的模式（僅圖表類型 `bar`/`line`、部分變數命名有些微差異），且範本中 `<canvas id="reportChart">` 皆為無條件渲染，可安全使用 `@ViewChild({ static: true })`。
4. 逐檔統一套用：新增 `@ViewChild` 取得 canvas、新增 `chart?: Chart` 欄位保留實例參考、`ngOnInit` 改用 ViewChild 取代 `document.getElementById`、`submitForm` 改用 `chart.destroy()` 取代 DOM 重建、`new Chart(...)` 呼叫改為賦值給 `this.chart`。
5. 意外發現 `ac1301.component.ts` 的 jQuery `append` 字串本身有 HTML 語法錯字（`<canvas id="reportChart"</canvas>`，漏了 `>`），因為整段邏輯已改為不重建 DOM，這個錯字隨著程式碼移除自然消失，不需要額外處理。
6. `npm run build` 驗證通過，比照先前慣例清理驗證動作弄髒的已追蹤建置產物目錄。

### 經驗紀錄（給未來類似任務參考）

- **「砍掉重建 DOM」的 jQuery 用法背後，通常是在繞過某個 library 的狀態管理限制**：這裡的根因不是「舊寫法比較習慣用 jQuery操作 DOM」，而是 Chart.js 本身不允許在同一個 canvas 上重複 `new Chart()`。找到真正的根因（保留實例參考、呼叫 `.destroy()`）後，替換方案不只是「把 jQuery 換成原生 DOM API」，而是換成該 library 官方支援、更正確的生命週期管理方式，程式碼也因此變得更簡潔（不需要整個 DOM 元素重建）。
- **`{ static: true }` 的判斷依據是模板結構，不是猜測**：只有在確認 `<canvas>` 元素不在任何 `*ngIf`/`*ngFor` 等結構型指令內、一定會在初始渲染時就存在時，才能安全使用 `@ViewChild(..., { static: true })` 讓它在 `ngOnInit()` 就可用；否則必須改用預設的非 static 查詢，並把存取時機挪到 `ngAfterViewInit()`。逐檔確認範本結構後才決定使用 `static: true`，不是套用固定公式。

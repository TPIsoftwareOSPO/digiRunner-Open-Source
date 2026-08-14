# AC0301 API 詳細資料群組清單補上查看更多流程

## 修改內容

### 問題描述

使用者反映 AC0301 API 列表的詳細資料中，群組清單目前只顯示 AA0320 首次查詢回傳的 20 筆資料，要求參考系統既有做法增加「查看更多」按鈕及續查流程。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `src/app/layout/ac03/ac0301/ac0301.component.html` | 將群組清單筆數傳入 `api-detail` 子元件，並接收查看更多事件後呼叫父元件既有續查方法 |
| `src/app/layout/ac03/ac0301/api-detail/api-detail.component.ts` | 新增群組清單筆數 input、查看更多 output 與事件轉發方法 |

### 詳細程式碼異動

#### 1. 子元件接收筆數並轉發查看更多事件

修改前，`api-detail.component.html` 已引用 `apiGroupListRowcount` 與 `moreApiGroupList()`，但 `ApiDetailComponent` 沒有定義這兩個對應成員：

```ts
@Input() apiGroupList: Array<AA0320Item> = [];
@Input() detailCols: { field: string; header: string }[] = [];

@Output() getApiGroupListEvt: EventEmitter<string> = new EventEmitter();
```

修改後補上筆數 input、查看更多 output 與點擊轉發方法：

```ts
@Input() apiGroupList: Array<AA0320Item> = [];
@Input() apiGroupListRowcount: number = 0;
@Input() detailCols: { field: string; header: string }[] = [];

@Output() getApiGroupListEvt: EventEmitter<string> = new EventEmitter();
@Output() moreApiGroupListEvt: EventEmitter<void> = new EventEmitter();

moreApiGroupList() {
  this.moreApiGroupListEvt.emit();
}
```

#### 2. 父元件串接群組清單查看更多事件

修改前，父元件只傳入群組清單與欄位設定，也只接收關鍵字查詢事件：

```html
<app-api-detail
  [apiGroupList]="apiGroupList"
  [detailCols]="detailCols"
  (getApiGroupListEvt)="getApiGroupListEvt($event)">
</app-api-detail>
```

修改後傳入 `apiGroupListRowcount`，並在收到 `moreApiGroupListEvt` 時呼叫父元件既有的 `moreApiGroupList()`：

```html
<app-api-detail
  [apiGroupList]="apiGroupList"
  [apiGroupListRowcount]="apiGroupListRowcount"
  [detailCols]="detailCols"
  (getApiGroupListEvt)="getApiGroupListEvt($event)"
  (moreApiGroupListEvt)="moreApiGroupList()">
</app-api-detail>
```

父元件原有 `moreApiGroupList()` 會取目前清單最後一筆的 `gId` 呼叫 AA0320，成功後以 `concat()` 將下一批資料追加到 `apiGroupList`，因此本次未重複新增 API 或續查邏輯。

## 預計修改方式

### 需求：群組清單增加查看更多按鈕及流程

**Context** — 使用者指出 AC0301 API 列表詳細資料的群組清單只顯示 20 筆，希望參考系統既有模式加入「查看更多」。

**分析** — 讀取 `ac0301.component.ts`、父元件 template、`api-detail` 子元件及 AA0320 interface 後確認：

- `api-detail.component.html` 原本已有群組清單 footer、筆數顯示與使用既有 `button.more` 語系的按鈕。
- 父元件原本已有 `moreApiGroupList()`，會帶入關鍵字、API module/name 與最後一筆 `gId` 呼叫 AA0320，並追加回傳資料。
- 實際缺口是子元件未宣告 `apiGroupListRowcount`，造成 footer 顯示條件無法取得筆數；同時子元件也沒有將按鈕點擊事件傳回父元件。

**修正方案** — 沿用現有父元件的 AA0320 續查邏輯與既有按鈕樣式，只補齊父子元件的 input/output wiring。此方案不新增 API、不修改 request model，也不改動群組清單搜尋流程。

## 驗證方式

### 靜態驗證

- 執行 `git diff --check -- src/app/layout/ac03/ac0301/api-detail/api-detail.component.ts src/app/layout/ac03/ac0301/ac0301.component.html`，exit code 0。
- 執行 `npm run build`（`ng build --optimization=true`），exit code 0；AC0301 lazy chunk 正常產出，無編譯錯誤。
- build 僅作為驗證；依專案規範，已還原 tracked static 產物並刪除本次新產生的 hash bundles。

### 尚待人工驗證（建議）

1. 啟動前端並進入 AC0301 API 列表。
2. 選擇一筆隸屬超過 20 個群組的 API，開啟詳細資料。
3. 確認群組清單 footer 顯示目前筆數及「查看更多」按鈕。
4. 點擊「查看更多」，確認 AA0320 request 帶入目前最後一筆 `gId`，下一批資料追加於既有 20 筆之後。
5. 輸入群組關鍵字重新查詢後，再點擊「查看更多」，確認續查沿用該關鍵字且不混入前一次查詢結果。

## 實作紀錄

### 時間軸

1. 使用者提出 AC0301 API 詳細資料群組清單只能顯示 20 筆，希望增加「查看更多」按鈕與流程。
2. 讀取專案 Angular、API 與 dialog 規範，並搜尋系統內既有 `button.more`／`moreData` 實作。
3. 初步搜尋發現 `api-detail.component.html` 已有 `moreApiGroupList()` 按鈕，因而改為調查既有按鈕沒有作用或沒有顯示的原因，而不是再新增一顆重複按鈕。
4. 讀取父子元件後確認：父元件已有 AA0320 `gId` 續查與 `concat()` 流程；子元件則缺少 `apiGroupListRowcount` 成員與查看更多事件轉發。
5. 在 `ApiDetailComponent` 新增 row count input、`EventEmitter<void>` output 與 `moreApiGroupList()`。
6. 在父元件 template 傳入 row count，並將子元件查看更多事件綁定至父元件既有方法。
7. 執行 `git diff --check` 與 `npm run build`，結果均成功；隨後清理 build 驗證產物。
8. 使用者要求建立工作日誌並上傳；建立本文件當下尚未 commit/push，後續依同一指示將本次兩個程式檔與本文件一併提交。

### 經驗紀錄（給未來類似任務參考）

- 畫面已有按鈕 markup 不代表互動流程已完成；拆成父子元件後，要同時確認 template 所引用的 state、method，以及 input/output wiring 是否都存在。
- 遇到分頁或「查看更多」需求時，應先搜尋父元件與 service 是否已有續查方法。本次 AA0320 的 `gId` cursor 與資料追加流程已完整存在，缺口只在子元件事件串接，不應再建立另一套 API 呼叫。
- 清單 footer 的顯示條件依賴父元件維護的 row count；若未傳入子元件，按鈕即使已寫在 template 中也不會呈現。

# lb0008 RDB 連線設定新增 Minimum Idle

## 修改內容

### 問題描述

使用者在本次對話中依序提出以下需求：

1. 在 `DPB0190RespItem`、`DPB0191Resp`、`DPB0192Req`、`DPB0193Req` 新增 `minimumIdle` 欄位，型別為 `number`。
2. 在 `lb0008` 的建立與更新畫面，於 `Max Pool Size` 下方新增 `Minimum Idle` 欄位及檢核。
3. 在呼叫 DPB0190 的共用選擇表格中，於 `Max Pool Size` 右側新增 `Minimum Idle` 欄位並顯示 `minimumIdle`。
4. 使用者檢查畫面後反映 `lb0008` 自身列表未顯示新欄位，因此補上同樣的列表欄位。
5. 執行 `npm run build`，成功後建立本工作日誌，並將原始碼、工作日誌及 build 產物一併提交與上傳。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `src/app/models/api/ServerService/dpb0190.interface.ts` | `DPB0190RespItem` 新增 `minimumIdle: number` |
| `src/app/models/api/ServerService/dpb0191.interface.ts` | `DPB0191Resp` 新增 `minimumIdle: number` |
| `src/app/models/api/ServerService/dpb0192.interface.ts` | `DPB0192Req` 新增 `minimumIdle: number` |
| `src/app/models/api/ServerService/dpb0193.interface.ts` | `DPB0193Req` 新增 `minimumIdle: number` |
| `src/app/layout/labs/rdb-connection/rdb-connection.component.ts` | 新增表單控制項、預設值、詳細／更新回填、建立／更新 API 傳值及 getter |
| `src/app/layout/labs/rdb-connection/rdb-connection.component.html` | 建立／更新表單與 `lb0008` 列表新增 `Minimum Idle`，並調整空資料 `colspan` |
| `src/app/shared/connection-info-list/connection-info-list.component.html` | DPB0190 共用選擇表格新增 `Minimum Idle`，並調整空資料 `colspan` |
| `../src/main/resources/static/dgrv4/ac4/` | `npm run build` 產生的 `index.html`、新 hash bundles 與被取代的舊 bundles |

### 詳細程式碼異動

#### 1. API model 新增 `minimumIdle`

修改前：

```ts
maxPoolSize: string;
connectionTimeout: string;
```

修改後：

```ts
maxPoolSize: string;
minimumIdle: number;
connectionTimeout: string;
```

DPB0190 列表項目與 DPB0191 詳細回應也在 `maxPoolSize` 後加入相同欄位。

#### 2. lb0008 表單、資料回填與 API request

修改前：

```ts
maxPoolSize: new UntypedFormControl(''),
connectionTimeout: new UntypedFormControl(''),
```

修改後：

```ts
maxPoolSize: new UntypedFormControl(''),
minimumIdle: new UntypedFormControl(''),
connectionTimeout: new UntypedFormControl(''),
```

建立畫面將 `minimumIdle` 預設為 `10`。詳細與更新畫面以 DPB0191 的 `res.RespBody.minimumIdle` 回填；建立 DPB0192 與更新 DPB0193 request 均加入：

```ts
minimumIdle: this.minimumIdle.value,
```

#### 3. lb0008 表單欄位與驗證訊息

在 `Max Pool Size` 下方新增 `type="number"` 的 `minimumIdle` control，錯誤訊息沿用既有數值欄位模式：

```html
<input type="number" class="form-control" id="minimumIdle" formControlName="minimumIdle">
<div *ngIf="minimumIdle?.invalid && (minimumIdle?.dirty || minimumIdle?.touched)" class="text-danger">
  <small class="form-text">{{minimumIdle?.errors.isRequired | translate}}</small>
  <small class="form-text">{{minimumIdle?.errors.min | translate}}</small>
  <small class="form-text">{{minimumIdle?.errors.max | translate}}</small>
</div>
```

前端透過既有 `addFormValidator(this.formEdit, res.RespBody.constraints)` 套用 Before API 回傳的 `minimumIdle` 規則。

#### 4. DPB0190 表格顯示

共用選擇表格與 `lb0008` 自身列表皆在 `Max Pool Size` 右側新增：

```html
<th>Minimum Idle</th>
...
<td>{{rowData.minimumIdle}}</td>
```

新增欄位後，空資料列的 `colspan` 由 `6` 調整為 `7`。

## 預計修改方式

### 需求 1：API model 新增欄位

**Context** — 使用者明確指定四個 TypeScript interface 新增 `minimumIdle`，型別為 `number`。

**分析** — 透過 `rg` 定位四個 interface，確認皆位於 `src/app/models/api/ServerService/`，且連線池欄位以 `maxPoolSize` 為相鄰欄位。另查閱後端 Java VO 與 service，確認 DPB0190、DPB0191、DPB0192、DPB0193 已有 `Integer minimumIdle` 並實際讀寫。

**修正方案** — 在四個 TypeScript interface 的 `maxPoolSize` 後加入必填 `minimumIdle: number`，不調整其他既有欄位型別。

### 需求 2：lb0008 建立／更新欄位與檢核

**Context** — 使用者要求在 `Max Pool Size` 下方新增 `Minimum Idle`，且建立與更新都必須支援檢核。

**分析** — `lb0008` 實際路由載入 `layout/labs/rdb-connection`。元件使用 `UntypedFormGroup`，並分別呼叫 DPB0192 Before 與 DPB0193 Before，再透過 `BaseComponent.addFormValidator()` 動態加入後端 constraints。查閱後端 `DPB0192Req` 與 `DPB0193Req`，確認 `minimumIdle` 規則為必填、最小值 `0`、最大值 `2147483647`。

**修正方案** — 新增同名 FormControl、getter 與 template 錯誤訊息，讓既有動態驗證機制自動套用後端規則；建立預設值採用 `10`，更新與詳細資料由 DPB0191 回填，建立／更新 request 均傳送數值。

### 需求 3：DPB0190 共用選擇表格顯示

**Context** — 使用者要求在呼叫 DPB0190 的表格中，於 `Max Pool Size` 右側顯示 `minimumIdle`。

**分析** — 搜尋 DPB0190 呼叫後，定位到 `shared/connection-info-list` 會呼叫 `queryRdbConnectionInfoList()` 並顯示 Connection Info 選擇表格；該元件同時可被其他呼叫端共用。

**修正方案** — 在共用表格新增表頭與 `rowData.minimumIdle` 儲存格，並同步調整 empty message 的欄位跨度。

### 需求 4：補上 lb0008 自身列表欄位

**Context** — 第一輪完成後，使用者實際查看 `lb0008` 並反映列表中沒有新欄位。

**分析** — 重新檢查 `rdb-connection.component.html`，確認第一輪只修改了 `lb0008` 的表單，以及 DPB0190 的共用選擇表格；`lb0008` 頁面上方的獨立列表仍維持原本 6 欄。

**修正方案** — 在 `lb0008` 自身列表同樣加入 `Minimum Idle` 表頭及資料欄，並將 empty message 的 `colspan` 改為 `7`。此修正不改動查詢或分頁邏輯。

### 需求 5：build、工作日誌與上傳

**Context** — 使用者明確要求執行 build，成功後建立工作日誌並一併上傳。

**分析** — 專案 `angular.json` 的 output path 指向 tracked 的後端 static 目錄。依專案規範，「build 且上傳」需保留 `index.html`、新 hash bundles 與被取代的舊 bundles，並與原始碼、工作日誌放在同一提交。檢查工作樹亦發現既有的 `application.properties`、`.claude/settings.local.json`、`srcVite/` 變更，均不屬於本次需求。

**修正方案** — build 成功後，只暫存本次 7 個前端檔案、本工作日誌及 static build 差異；排除所有無關變更，再依 staged diff 建立需 QA 驗證的 commit 並推送目前分支。

## 驗證方式

### 靜態驗證

- `git diff --check`：本次原始碼差異通過，exit code 0。
- `npm run build`：最終交付 build 成功，exit code 0。
  - Angular build hash：`dbcb12cdb1a83c20`
  - Build time：`30180ms`
  - `lb0008` 對應 hash bundle、共用元件 bundle、runtime 與 `index.html` 正常產出。
- 先前驗證過程曾因工具逾時上限設為 1 秒與 60 秒而被終止；提高上限後 build 均成功，未發現 TypeScript 或 Angular template 編譯錯誤。

### 尚待人工驗證（建議）

- 開啟 `lb0008`：
  - 確認列表的 `Max Pool Size` 右側顯示 `Minimum Idle` 與正確數值。
  - 建立 RDB connection 時確認 `Minimum Idle` 預設為 `10`。
  - 將欄位留空、輸入負數及超過 `2147483647` 的數值，確認必填、最小值及最大值訊息正常顯示。
  - 建立後重新查詢，確認 DPB0192 已保存 `minimumIdle`。
  - 更新既有資料，確認 DPB0191 正確回填，且 DPB0193 更新後數值正確。
- 開啟使用 Connection Info 共用選擇表格的功能，確認 `Minimum Idle` 位於 `Max Pool Size` 右側並顯示 DPB0190 回傳內容。

## 實作紀錄

### 時間軸

1. 使用者要求四個 API interface 新增 `minimumIdle: number`。
2. 讀取 `dgr-angular-conventions` 與 API 規範，使用 `rg` 定位 interface、service 及元件使用處；確認目標檔案當時沒有未提交變更。
3. 在四個 interface 新增欄位。編輯後發現局部行尾格式造成差異顯示擴大，經檢查原檔格式後重新正規化，最終差異收斂為四行新增。
4. 使用者追加 `lb0008` 建立／更新欄位與檢核，以及 DPB0190 表格顯示需求。
5. 查閱 Angular 元件規範、`rdb-connection` 與 `connection-info-list`，並查證後端 Java VO、service 與 constraints，確認 `minimumIdle` 的資料流和驗證上下限。
6. 在 `lb0008` 表單加入控制項、預設值 `10`、DPB0191 回填、DPB0192／DPB0193 request 傳值、getter 與 template 檢核訊息；在共用 DPB0190 選擇表格加入顯示欄。
7. 執行 Angular build。最初兩次因工具逾時門檻過短被中止，調高上限後 build 成功；驗證用 build 產物依規範清理。
8. 使用者反映 `lb0008` 列表沒有顯示新欄位。重新檢查後確認第一輪只修改表單與共用選擇表格，遺漏 `lb0008` 自身列表。
9. 在 `lb0008` 自身列表的 `Max Pool Size` 右側補上 `Minimum Idle` 及 `rowData.minimumIdle`，並調整 empty message `colspan`。重新 build 成功後清理驗證產物。
10. 使用者要求正式 build、建立工作日誌並上傳。確認目前分支為 `feature/tom/rdb_connection_min`、remote 為 `eapdd2025`，且暫存區為空；辨識並排除與本次無關的既有工作樹內容。
11. 執行交付用 `npm run build` 成功，hash 為 `dbcb12cdb1a83c20`，依規範保留 static build 產物供同次提交。
12. 建立本工作日誌時，commit 與 push 尚未執行；後續將在同一工作流程中完成。

### 經驗紀錄（給未來類似任務參考）

- 同一個 API response 可能同時出現在功能頁列表與共用選擇 Dialog；需求提到「DPB0190 表格」時，應列出所有實際呈現該 response 的 template，逐一確認使用者期望的畫面，避免只修改其中一處。
- 動態 Before API constraints 要生效，前端 FormGroup 必須存在與後端 `field` 完全同名的 control；本次加入 `minimumIdle` control 後即可重用 `addFormValidator()`，不必重複硬編碼 validators。
- Build 逾時不等於編譯失敗；應區分工具 timeout 與 Angular compiler error，並以足夠的 timeout 重新取得完整結果。
- 專案 build 會寫入 tracked static 目錄；純驗證需清理產物，但使用者明確要求 build 且上傳時，必須連同新舊 hash bundles 與 `index.html` 一起交付。
- 暫存前必須比對工作樹，避免把本次任務開始前已存在的 `application.properties` 或本地工具目錄誤納入提交。

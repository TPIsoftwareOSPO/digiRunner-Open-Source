# lb0018 Patient Dashboard：$everything 改用 Paginator 分頁，並優化病人搜尋支援 name / identifier / id

延續同日的 [`2026-07-16_lb0018-patient-dashboard-auth-flow.md`](./2026-07-16_lb0018-patient-dashboard-auth-flow.md)（Patient Dashboard 分頁與 Auth Method 測試流程）。本次聚焦於 Tab 2「Patient Dashboard」右側 `$everything` 資源資料的分頁呈現，以及左側病人搜尋框的搜尋條件優化。

## 修改內容

### 問題描述

本次對話依序提出以下需求：

1. **$everything 改分頁**：Patient Dashboard 中 patient 的 resource 資料改用分頁方式呈現，API 參數固定為 `$everything?_count=20&_offset=0&_sort=_id&_format=json`，每次 20 筆。
2. **改用 Paginator 呈現**：資料回來後依「總筆數 / 每頁筆數」呈現分頁，改用 PrimeNG `<p-paginator>` 元件（而非「載入更多」）。
3. **提供測試資料**：找出一位 resource 超過 20 筆的病人，以便實測分頁畫面。
4. **病人清單「載入更多」也改 Paginator**（後被使用者撤回）：一度要求把左側病人搜尋清單的「載入更多」按鈕改成同款 paginator；實作到一半使用者表示不喜歡，要求還原。
5. **病人搜尋支援 name / identifier / id**：單一自由輸入框輸入任一種即可搜尋，請優化。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `src/app/layout/labs/lb0018/lb0018.component.ts` | `$everything` 由「沿 next link 抓齊全部」改為「offset 分頁、Paginator 換頁取代目前頁」；新增 `onEverythingPageChange`、`everythingFirst`/`everythingTotal`；移除 `EVERYTHING_MAX_PAGES`/`everythingTruncated`/`rebaseLink`。病人搜尋新增 `patientSearchParam()` helper（UUID→`_id`、身分證→`identifier`、純數字→`identifier`、其餘→`name:contains`）。 |
| `src/app/layout/labs/lb0018/lb0018.component.html` | 移除截斷警告；`$everything` 區塊底部改放 `<p-paginator>`。 |
| `src/app/shared/primeng.module.ts` | 新增 export `PaginatorModule`。 |
| `src/assets/i18n/{zh-tw,zh-cn,en-us,ja}.json` | `fhir_viewer.search_placeholder` 補上「資源ID / Resource ID」。 |

> 註：`lb0018.component.ts` 的 diff 另含兩行 `ToolService.Base64Encoder(...)`（`client_secret` 表身列與 `basicAuthorization`）—— 此為 session 開始前工作區既有的未提交改動（屬前案 Auth flow），**非本次對話所編寫**，依「尊重他人成果」原則未動、亦未於本紀錄計為本次成果。

### 詳細程式碼異動

#### 1. `$everything` 改 offset 分頁 + Paginator（取代式換頁）

修改前（遞迴沿 next link 抓齊全部，最多 30 頁）：

```ts
private loadEverything(patientId: string): void {
  ...
  this.fetchEverythingPage(`${base}/Patient/${patientId}/$everything?_format=json`, base, patientId, [], 0);
}
private fetchEverythingPage(url, base, patientId, acc, page): void {
  ...
  const next = SmartFhirService.nextLink(bundle);
  if (next && page + 1 < Lb0018Component.EVERYTHING_MAX_PAGES) {
    this.fetchEverythingPage(this.rebaseLink(next, base), base, patientId, acc, page + 1);
  } else { this.everythingTruncated = !!next; this.bindEverything(patientId, acc); ... }
}
```

修改後（每次一頁、Paginator 依 total/pageSize 換頁）：

```ts
private loadEverything(patientId: string): void {
  const base = this.currentBaseUrl();
  if (!base || !patientId) return;
  this.everythingFirst = 0;
  this.fetchEverythingPage(base, patientId, 0);
}

onEverythingPageChange(event: PaginatorState): void {
  const offset = event.first ?? 0;
  if (!this.selectedPatient || offset === this.everythingFirst) return;
  const base = this.currentBaseUrl();
  if (!base) return;
  this.everythingFirst = offset;
  this.fetchEverythingPage(base, this.selectedPatient.id, offset);
}

private fetchEverythingPage(base: string, patientId: string, offset: number): void {
  const url = `${base}/Patient/${patientId}/$everything` +
    `?_count=${this.everythingPageSize}&_offset=${offset}&_sort=_id&_format=json`;
  this.ngxService.start();
  this.httpClient.get<Bundle>(url, { headers: this.bypassHeaders() })
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: (bundle) => {
        if (this.selectedPatient?.id !== patientId) return;
        const entries = (bundle.entry || []).map((e) => e.resource).filter((r): r is FhirResource => !!r);
        this.everythingTotal = bundle.total ?? this.everythingTotal;   // 部分伺服器僅首頁回 total
        this.everythingFailed = false;
        this.bindEverything(patientId, entries);
        this.ngxService.stop();
      },
      error: () => { /* 清空目前頁 + everythingFailed */ },
    });
}
```

Template：

```html
@if (everythingLoaded && everythingTotal !== null && everythingTotal > everythingPageSize) {
  <div class="card card-body mt-3 p-2 align-items-center">
    <p-paginator [first]="everythingFirst" [rows]="everythingPageSize" [totalRecords]="everythingTotal"
      (onPageChange)="onEverythingPageChange($event)"></p-paginator>
  </div>
}
```

#### 2. 共用 `PrimengModule` 新增 `PaginatorModule`

`PrimengModule` 原本未 export paginator；依專案慣例（功能元件透過共用 `PrimengModule` 取用 PrimeNG，而非各自 import 子模組）新增：

```ts
import { PaginatorModule } from 'primeng/paginator';
const sharedModules: any[] = [ ..., ProgressBarModule, PaginatorModule ];
```

#### 3. 病人搜尋智慧判斷 name / identifier / id

修改前（僅 identifier / name）：

```ts
const param = /^[A-Za-z]\d{9}$/.test(kw) || /^\d{3,}$/.test(kw) ? 'identifier' : 'name:contains';
url += `&${param}=${encodeURIComponent(kw)}`;
```

修改後（抽 helper，新增 UUID→`_id`）：

```ts
private patientSearchParam(kw: string): string {
  const UUID = /^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/;
  if (UUID.test(kw)) return '_id';
  if (/^[A-Za-z]\d{9}$/.test(kw) || /^\d{3,}$/.test(kw)) return 'identifier';
  return 'name:contains';
}
```

i18n placeholder：`姓名 / 病歷號 / 身分證` → `姓名 / 病歷號 / 身分證 / 資源ID`（四語系同步）。

## 預計修改方式

### 需求 1／2：$everything 分頁 → Paginator

**Context**：使用者在 lb0018 Patient Dashboard 反映 resource 資料一次全載，希望改為每次 20 筆的分頁；後續進一步指定改用 Paginator 元件、依總筆數/每頁筆數呈現頁數。

**分析**：
- 讀 `lb0018.component.ts` 確認原本 `loadEverything`→`fetchEverythingPage` 會沿 `bundle.link` 的 `next` 遞迴抓齊所有分頁（上限 `EVERYTHING_MAX_PAGES=30`），再一次 `bindEverything`。
- `bindEverything` 產出的三種檢視（就診時間軸 / 資源參照樹 / 完整 JSON）都依賴 `everythingEntries` 全量資料（`buildResourceTree` 需跨資源解析 reference）。
- grep 確認全專案原本無 `p-paginator` 用法，`PrimengModule`（`src/app/shared/primeng.module.ts`）未含 `PaginatorModule`；`primeng/paginator` 有 export `PaginatorModule` 與型別 `PaginatorState`（`node_modules/primeng/paginator/index.d.ts` 第 2 行 `export * from 'primeng/types/paginator'`）。

**修正方案**：
- 先實作「載入更多（累積）」版本（見時間軸 T2）；使用者改要求 Paginator 後，改為**取代式換頁**：每次只抓 `_offset` 起 20 筆、`bindEverything` 取代目前頁。
- `PaginatorModule` 加進共用 `PrimengModule`（符合既有慣例，避免元件各自 import）。
- 明確告知取捨：取代式換頁下，就診時間軸 / 參照樹 / JSON 只反映當前 20 筆，跨頁 reference 無法解析（此為伺服器端 offset 分頁固有限制，使用者接受）。

### 需求 3：測試資料

**Context**：使用者需要一位 resource >20 筆的病人來驗測 paginator 多頁。

**分析**：無法連線使用者 gateway（無 base URL / token / 內網）。repo 內 `smart-fhir.service.ts` 註解與 i18n `base_tip` 引用兩個公開測試站：`https://hapi.fhir.tw/fhir`、`https://r4.smarthealthit.org`。實測 `hapi.fhir.tw` 回 503；`r4.smarthealthit.org` 正常。

**修正方案**：以元件實際送出的網址格式對 `r4.smarthealthit.org` 逐一實測 `bundle.total`，提供多位病人（見「實作紀錄」）。首選 **Deidre Daniel**（`97e57d8e-d075-48db-b933-2f73bda460ef`，total=212，11 頁）。

### 需求 4：病人清單改 Paginator（已撤回）

**Context**：使用者要求把左側病人搜尋清單的「載入更多」也改成同款 paginator。

**分析**：讀 `loadPatientPage(offset, append)` 現況為 offset + append 累積、以 `hasMorePatients` 控制載入更多。

**修正方案**：一度動手改欄位（`patientFirst`/移除 `hasMorePatients` 等）與方法（`onPatientPageChange`），但使用者在方法改寫的編輯被拒絕後回覆「這個修改我不喜歡請修正回去」→ **完整還原**，病人清單維持原本「載入更多」。

### 需求 5：搜尋支援 name / identifier / id

**Context**：使用者希望單一自由輸入框可輸入 name / identifier / id 任一種來搜尋。

**分析**：
- grep 既有病人搜尋模式：`lb0016` / `smart-shared` 用 `'name' | 'id'` 模式下拉（`params[searchBy === 'id' ? '_id' : 'name:contains']`）；`lb0018` 則用單框啟發式（identifier / name）。使用者要「隨意輸入」，排除下拉模式。
- FHIR 無法在單一查詢乾淨地 OR 不同搜尋參數（`_filter` 各伺服器支援不一，不採用）；純數字在 logical id 與 identifier 間有歧義。
- 以 `AskUserQuestion` 讓使用者在「同時查三者合併」與「智慧判斷單一查詢」間抉擇 → 使用者選**智慧判斷（單一查詢）**。

**修正方案**：抽出 `patientSearchParam()`，新增 UUID→`_id`；純數字沿用歸 `identifier`（並於註解標明此歧義取捨）。四語系 placeholder 補「資源ID」。

## 驗證方式

### 靜態驗證（本次實際執行）

- VS Code 診斷（`mcp__ide__getDiagnostics`）：`lb0018.component.ts`、`lb0018.component.html`、`primeng.module.ts` 均 **0 diagnostics**。
- i18n JSON：四份檔案 `JSON.parse` 皆通過（`node -e` 驗證，輸出 `zh-tw OK / zh-cn OK / en-us OK / ja OK`）。

### 動態驗證（對公開測試站 `https://r4.smarthealthit.org` 實測）

- `GET /metadata?_format=json` → 200，`CapabilityStatement` FHIR 4.0.0（FHIR Base URL 驗證會通過）。
- `$everything?_count=20&_offset=0&_sort=_id&_format=json` 五位病人 total：212 / 149 / 109 / 74 / 73（皆 >20）。
- 搜尋路由：`Patient?...&_id=97e57d8e-...` → total=1（UUID 正確走 `_id`）；`Patient?...&name:contains=Daniel` → total=2（走 `name:contains`）。

### 尚待人工驗證（建議）

1. 於實際 gateway（走 SOF Proxy）開 lb0018 → Patient Dashboard，選一位 >20 筆病人，確認右側出現 `<p-paginator>`、頁數 = ⌈total/20⌉、換頁會以 `_offset` 重新請求並取代三種檢視內容、切換病人時 paginator 歸位第 1 頁。
2. 病人搜尋分別輸入姓名、identifier、資源 id（UUID），確認各自命中；純數字會走 identifier（若某病人 logical id 為純數字則查不到，屬已知取捨）。
3. 確認 `r4.smarthealthit.org` 無 `.well-known/smart-configuration`（回 404）只影響 Authorization 分頁，不影響 Patient Dashboard。

## 實作紀錄

### 時間軸

1. **需求 1**：使用者要求 $everything 改分頁（`_count=20&_offset=0&_sort=_id&_format=json`，每次 20 筆）。
2. **實作 A（載入更多）**：先實作「載入更多（累積）」—— 保留原三種檢視需全量資料的特性，改為 on-demand 累積、`bindEverything` 加 `append` 保留展開狀態。回報時已標註「若想要換頁式再告知」。
3. **需求 2（改 Paginator）**：使用者要求改用 `<p-paginator>`、依 total/pageSize 呈現。→ 將 A 版的載入更多改為**取代式換頁**：移除 `append`/state 保留、`hasMoreEverything`/`everythingLoadingMore`，改 `everythingFirst`/`everythingTotal`+`onEverythingPageChange`；`PaginatorModule` 加進 `PrimengModule`；import `PaginatorState`。中途診斷曾誤報 stale 行號，逐一以 `getDiagnostics` 重取確認為 0。
4. **需求 3（測試資料）**：`hapi.fhir.tw` 503 → 改 `r4.smarthealthit.org`，實測病人 total，提供 Deidre Daniel（212）等。過程踩到：Git Bash 的 `python` 異常（`-c` 也 exit 49 無輸出）→ 改用 PowerShell `Invoke-RestMethod`；PowerShell 迴圈內 `$everything` 曾因字串轉義瞬間 404，改用字串串接後正常。
5. **需求 4（清單改 Paginator）→ 撤回**：開始改病人清單欄位/方法；第二筆（方法）編輯被使用者拒絕，並回「這個修改我不喜歡請修正回去」→ 還原已套用的欄位改動，病人清單回到原「載入更多」。
6. **需求 5（搜尋 name/identifier/id）**：以 `AskUserQuestion` 確認策略 → 選「智慧判斷（單一查詢）」→ 抽 `patientSearchParam()` 新增 UUID→`_id`；更新四語系 placeholder；對 `r4.smarthealthit.org` 驗 `_id` 與 `name:contains` 路由。
7. **本次（產出工作日誌 + 上傳）**：撰寫本 spec；git commit/push 狀態見文末。

### 經驗紀錄（給未來類似任務參考）

- **先確認 UX 再動工**：需求 1 直接做了「載入更多」，需求 2 又改「Paginator」，兩者的分頁語意（累積 vs 取代）差異大。日後遇「分頁」字眼，先釐清是累積式還是換頁式，可省一次重工。
- **取代式換頁對「需全量資料的檢視」有固有限制**：$everything 的參照樹 / 時間軸依賴跨資源 reference，offset 換頁後只剩單頁資料、跨頁 reference 解析不到，需事先向使用者講清楚取捨。
- **新增 PrimeNG 元件先查共用 `PrimengModule`**：本專案慣例是功能元件統一透過 `PrimengModule` 取用，缺的模組加在該檔而非元件各自 import。
- **本機驗 FHIR 用 PowerShell 而非 Git Bash python**：此環境 Git Bash 的 `python` 會 exit 49 無輸出；`Invoke-RestMethod` 穩定。字串含 `$everything` 時用字串串接，避免 `$` 轉義誤觸。
- **搜尋「隨意輸入多欄位」的先天限制**：FHIR 單一查詢無法乾淨 OR 不同搜尋參數，純數字在 `_id` 與 `identifier` 間有歧義；若要真正「任一符合」需多查詢合併（但與分頁相衝）。本次採智慧判斷單一查詢是取捨後結果，已在註解與回報中標明歧義。
- **工作區既有他人未提交改動要辨識**：session 起始 `lb0018.component.ts` 已是 `M`，diff 內含兩行非本次編寫的 `Base64Encoder` 改動，須辨識、不覆蓋、不計為己功。

## Git 狀態

截至本 spec 產出時，需求 1–6 的改動仍在工作區（尚未 commit）；本輪任務即為「建立工作日誌並上傳」，commit/push 於本 spec 寫入後進行。

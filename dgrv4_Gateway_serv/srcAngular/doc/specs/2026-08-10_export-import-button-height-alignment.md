# 匯出／匯入按鈕高度對齊修正（7 個頁面）與 lb0011/lb0012 路由註解更正

## 1. 修改內容

### 問題描述

依序記錄使用者在本次對話中提出的需求：

1. 「功能維護、回傳碼維護、嵌入頁面維護、Items、WebSocket Proxy 維護、靜態網頁反向代理、AI Proxy」七個頁面的**匯出鈕高度比匯入鈕高**，請參考 `np0201` 頁面的做法修正。
2. 順便把 `lb0011` 的路由註解改成 `AI Proxy`（原本寫 `//webhook`）。
3. （使用者反映）lb0011 的匯出鈕高度也要改 — 實際上該頁在需求 1 已一併處理，本輪無新增改動。
4. `lb0012` 的路由註解也一併修正。
5. 執行 `npm run build`，成功後建立本工作日誌。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `src/app/layout/ac01/ac0101/ac0101.component.html` | 匯出／匯入列容器改用 `d-flex align-items-center justify-content-between` |
| `src/app/layout/ac01/ac0103/ac0103.component.html` | 同上 |
| `src/app/layout/ac01/ac0105/ac0105.component.html` | 同上 |
| `src/app/layout/labs/tsmpdpitems/tsmpdpitems.component.html` | 同上 |
| `src/app/layout/labs/websocket-proxy/websocket-proxy.component.html` | 同上 |
| `src/app/layout/labs/website-proxy/website-proxy.component.html` | 同上 |
| `src/app/layout/labs/lb0011/lb0011.component.html` | 同上 |
| `src/app/layout/layout.routing.ts` | `lb0011` 註解 `//webhook` → `//AI Proxy`；`lb0012` 註解 `//webhook logs` → `//AI Proxy logs` |
| `../src/main/resources/static/dgrv4/ac4/` | 完成 `npm run build` 後更新的封裝產物 |

### 詳細程式碼異動

#### (1) 七個頁面的匯出／匯入列容器

七個檔案的異動內容完全相同，每個檔案各一處：

修改前（以 `ac0101.component.html:17` 為例）：

```html
<div class="col-12 px-0" style="display: flex;justify-content: space-between;">

  <button type="button" class="btn tpi-btn tpi-primary me-2"
    (click)="exportTsmpFunc()">{{'button.export'|translate}}</button>

  <div style="display: flex;align-items: center;">
    <div class="ui-inputgroup me-2">
      <input type="file" ... >
      <input type="text" class="form-control" ... >
      <button pButton type="button" icon="pi pi-file" ... ></button>
    </div>
    <button type="button" class="btn tpi-btn tpi-primary me-2" [disabled]="!file?.name"
      (click)="importTsmpFunc()">{{'button.import'|translate}}</button>
  </div>
</div>
```

修改後：

```html
<div class="col-12 px-0 d-flex align-items-center justify-content-between">
```

只改外層容器一行，內部按鈕與 `ui-inputgroup` 結構完全未動。

#### (2) 路由註解

修改前（`layout.routing.ts:146-147`）：

```ts
{ path: 'lb00/lb0011', loadChildren: () => import('./labs/lb0011/lb0011.module').then(m => m.Lb0011Module), data: { id: 'lb0011' } }, //webhook
{ path: 'lb00/lb0012', loadChildren: () => import('./labs/lb0012/lb0012.module').then(m => m.Lb0012Module), data: { id: 'lb0012' } }, //webhook logs
```

修改後：

```ts
{ path: 'lb00/lb0011', loadChildren: () => import('./labs/lb0011/lb0011.module').then(m => m.Lb0011Module), data: { id: 'lb0011' } }, //AI Proxy
{ path: 'lb00/lb0012', loadChildren: () => import('./labs/lb0012/lb0012.module').then(m => m.Lb0012Module), data: { id: 'lb0012' } }, //AI Proxy logs
```

## 2. 預計修改方式

### 需求 1：匯出鈕高度對齊

**Context**
使用者以中文頁面名稱列出七個頁面，指出匯出鈕比匯入鈕高，並指定參考 `np0201` 的做法。使用者當時在 IDE 開啟的是 `ac0301.component.ts`（與本次需求無關）。

**分析**

- 先讀 `np0201.component.html:30`，確認其容器寫法為：
  `<div class="form-group col-12 px-0 d-flex align-items-center justify-content-between">`。
- 以 `grep -rln "button.export" src/app/layout --include=*.html` 盤點，共 20 個頁面有匯出鈕。
- 逐一比對七個目標頁面，發現它們的容器**全部**是：
  `<div class="col-12 px-0" style="display: flex;justify-content: space-between;">` — 有 `display:flex` 但**沒有 `align-items`**。
- 成因確認：flex container 的 `align-items` 預設為 `stretch`，左側匯出鈕會被拉伸到與右側「檔案輸入框 + 上傳 icon 鈕 + 匯入鈕」群組同高（右側因 `ui-inputgroup` 內含 `form-control`，高度較高），因此看起來匯出鈕偏高。`np0201` 因為多了 `align-items-center` 而沒有這個現象。
- 以 `grep -c` 確認七個檔案中該字串各只出現 1 次，可安全做字串取代。

**修正方案**

比照 `np0201`，將 inline style 改為 Bootstrap utility class `d-flex align-items-center justify-content-between`。

- 選擇「改成 Bootstrap class」而非「在 inline style 補 `align-items: center`」，是因為 `.claude/rules/style.md` 明訂版面排版優先使用 Bootstrap utility class、避免 inline style 手刻排版；既然這行本來就要改，順勢對齊規範。
- **未**加上 `np0201` 有的 `form-group` class：該 class 會帶入額外 margin，而 `np0201` 的按鈕列位於 `<hr>` 之後，七個目標頁則多在 `<hr>` 之前，加上去會改變既有間距。此處只取解決問題所需的 flex 對齊，維持最小改動。

### 需求 2 / 4：lb0011、lb0012 路由註解

**Context**
在需求 1 尋找「AI Proxy」頁面時，發現 i18n 與路由註解都查不到 AI Proxy 字樣，需另行推定該頁對應哪個元件。

**分析**

- `grep -rn -i "ai proxy|aiproxy|ai-proxy" src/` 命中四個元件：`ac0301`、`ac0311`、`lb0011`、`lb0012`。
- `ac0301`/`ac0311` 只是表單內有 `AI Proxy Notify Name` 欄位標籤，不是 AI Proxy 主畫面。
- `lb0011.component.ts:499` 匯出檔名為 `` saveAs(data, `AiProxy_${date}.xlsx`) `` → 判定 `lb0011` 即 AI Proxy 維護頁，但 `layout.routing.ts:146` 註解寫的是 `//webhook`，與實際功能不符。
- `lb0012` 為日期區間 + 關鍵字的紀錄查詢頁，表格欄位顯示 `AI Proxy Name`（`lb0012.component.html:48`、`:104`），但註解寫 `//webhook logs`。

**修正方案**

- `lb0011` → `//AI Proxy`。
- `lb0012` → `//AI Proxy logs`（保留 logs 語意，只把 webhook 換成 AI Proxy）。
- **只改註解，不動任何識別字**：元件內部仍大量沿用 webhook 命名（`queryWebhookLogs()`、`DPB0285WebhookLogItem`、`DPB0285Req`），後端交易代碼也是 `DPB0285`。要整體對齊 AI Proxy 命名會牽動 `models/api/ServerService/` 與後端介面，屬於另一件事，不在本次範圍。

### 需求 3：lb0011 匯出鈕高度

**Context**
使用者在路由註解改完後，追加要求「lb0011 也要改匯出鈕高度」。

**分析**
讀 `lb0011.component.html:32`，確認該行已是 `col-12 px-0 d-flex align-items-center justify-content-between` — 需求 1 的七頁清單中「AI Proxy」指的就是 lb0011，已一併處理完成。

**修正方案**
無需新增改動，向使用者回報現況即可。

## 3. 驗證方式

### 靜態驗證

- `grep -c` 確認七個檔案的目標字串各只有 1 處，取代不會誤傷其他區塊。
- `npm run build`：**成功**。Build hash `77e385f9cc670f9f`，耗時 66598 ms（約 66.6 秒），輸出時間 `2026-08-10T10:04:50.254Z`，無 error。
- `git diff --stat`：確認變更僅涵蓋 7 個 template + `layout.routing.ts`，每檔各 1 行（另有 `environment.ts`、`application.properties` 兩個**本次未動、原本就存在的本機變更**）。
- Build 產物：`src/main/resources/static/dgrv4/ac4/` 下 `index.html` 及數個 hashed chunk 已更新（含 `labs-lb0012-lb0012-module`、`ac03-ac0301-ac0301-module` 等）。

### 尚待人工驗證（建議）

逐一開啟以下七個頁面的查詢列表畫面，確認匯出鈕與匯入鈕**上下置中、高度一致**，且版面間距與修改前相比沒有位移：

| 頁面 | 路由 |
|------|------|
| 功能維護 | `ac01/ac0101` |
| 回傳碼維護 | `ac01/ac0103` |
| 嵌入頁面維護 | `ac01/ac0105` |
| Items | `labs/tsmpdpitems`（別名 `lb00/lb0002`） |
| WebSocket Proxy 維護 | `lb00/lb0006` |
| 靜態網頁反向代理 | `lb00/lb0007` |
| AI Proxy | `lb00/lb0011` |

另需確認：

- `tsmpdpitems` 與 `website-proxy` 的按鈕列上方有其他區塊（`website-proxy` 上方緊接一段被註解掉的 HTML），改動後上下間距是否仍正常。
- 各頁匯入鈕旁的說明 icon（`far fa-question-circle`）是否仍與按鈕垂直置中。

## 4. 實作紀錄

### 時間軸

1. 使用者提出需求：七個頁面匯出鈕高度異常，參考 `np0201` 修正。
2. 讀 `np0201.component.html`，取得基準寫法 `d-flex align-items-center justify-content-between`。
3. `grep` 盤點所有含 `button.export` 的頁面（20 個），逐一比對中文頁名對應的元件。
4. 前六個頁面容易對應；**「AI Proxy」一度找不到** — i18n 四份語系檔、`layout.routing.ts` 都沒有 AI Proxy 字樣。
5. 中途誤判：先看到 `lb0012.component.html` 有 `AI Proxy Name` 欄位，一度以為 lb0012 是目標頁；但檢查後發現 lb0012 沒有匯出／匯入鈕，排除。
6. 也曾檢查 `lb0013`（實為 SMART on FHIR Proxy）、`ai00/*`（無匯出鈕），皆排除。
7. 最終依 `lb0011.component.ts:499` 的匯出檔名 `AiProxy_${date}.xlsx` 判定 lb0011 即 AI Proxy 頁。
8. 嘗試用 Bash + `perl -0pi -e` 批次取代，**失敗**：Bash tool 回報 fnm 環境變數錯誤（`We can't find the necessary environment variables to replace the Node version`）。改用 Read/Edit 工具逐檔處理，七個檔案全部完成。
9. 回報時附帶提及 `lb0011` 路由註解 `//webhook` 與實際功能不符。
10. 使用者要求修正該註解 → 改為 `//AI Proxy`。
11. 使用者追問「lb0011 也要改匯出鈕高度」→ 查證後確認需求 1 已一併改完，回報現況，本輪無改動。
12. 使用者要求一併修正 `lb0012` 註解 → 先確認該頁實際內容（日期區間查詢 + `AI Proxy Name` 欄位）後，改為 `//AI Proxy logs`。
13. 執行 `npm run build` 成功（hash `77e385f9cc670f9f`，66.6 秒）。
14. 產出本工作日誌。

**本次工作尚未 commit，也尚未 push。** 工作區目前除了本次的 8 個原始碼檔案與 `ac4` 封裝產物外，另有兩個原本就存在、非本次造成的本機變更：`dgrv4_Common_lib/build/resources/main/application.properties` 與 `srcAngular/src/environments/environment.ts`，提交前請留意不要誤帶進去。

### 經驗紀錄（給未來類似任務參考）

- **「按鈕比隔壁高」的典型成因是 flex container 缺 `align-items`**：`display:flex` 預設 `align-items: stretch`，同列中只要有一個較高的元素（此處是含 `form-control` 的 `ui-inputgroup`），其他 flex item 就會被拉伸。看到同列元件高度不一致時，先檢查容器有沒有 `align-items-center`，通常比調整按鈕本身的 `height`／`padding` 正確。
- **本專案有一組重複出現的「匯出／匯入列」樣板**，散落在至少 20 個頁面，其中部分（`np0201`）已含 `align-items-center`、部分未含。未來若再遇到同類問題，可直接用
  `grep -rn 'class="col-12 px-0" style="display: flex;justify-content: space-between;"'`
  找出所有尚未修正的頁面，而不必靠中文頁名逐一比對。
- **用中文頁名找元件不可靠時，改用「功能特徵」反查**：本次「AI Proxy」在 i18n 與路由註解中完全查不到，最後是靠匯出檔名 `AiProxy_*.xlsx` 定位。類似情境可優先 grep 匯出檔名、API 交易代碼、table 欄位標題等實際會出現在程式碼中的字串。
- **路由註解可能過時，不要當成事實依據**：`layout.routing.ts` 中 `lb0011`（實為 AI Proxy）與 `lb0012`（實為 AI Proxy logs）的註解都還停留在舊的 webhook 命名，元件內部識別字亦然。判斷頁面用途時應以 template 顯示內容與實際 API 呼叫為準。
- **Bash tool 在本環境無法執行 `node`／`perl` 相關批次指令**（fnm 未初始化）。需要跨檔案批次取代時，改用 Read/Edit 工具逐檔處理，或用 PowerShell tool。
- 依 `.claude/rules/style.md`，既有 inline style 排版屬歷史包袱、不必主動清理；但當該行本來就要修改時，順勢改成 Bootstrap utility class 是合理的做法，不算無關重構。

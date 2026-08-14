# lb0017 多語系匯出匯入頁面優化（全選 / 顯示文字 / Toast 提示）

## 修改內容

### 問題描述

使用者針對 `lb0017`（多語系匯出/匯入）頁面，在同一次工作階段中依序提出以下 5 個需求：

1. 移除自行實作的「全選」選項，改用 `p-multiSelect` 元件內建的全選功能。
2. `exportItemOptions` 的顯示文字（label）不需要透過 i18n 字典查詢，直接使用與 `value` 相同的內容。
3. 匯出／匯入成功後應該要有 toast 提示，但畫面上沒有看到。
4. 補上 toast 呼叫後，畫面仍然沒有正常呈現 toast。
5. 希望把「使用 `MessageService` 就要在 html 加 `<p-toast>`」這件事建立成規則，避免未來再犯。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `src/app/layout/labs/lb0017/lb0017.component.ts` | 移除手動全選邏輯與 i18n label 查詢；匯出成功後新增 toast |
| `src/app/layout/labs/lb0017/lb0017.component.html` | 移除 `(onChange)` 全選處理綁定；新增 `<p-toast>` |
| `.claude/RULES/angular-component.md` | 新增「Toast（`MessageService`）通知」章節，記錄「使用 `MessageService` 必須搭配 `<p-toast>`」的規則 |

### 詳細程式碼異動

#### 1. 移除手動全選邏輯，改用元件內建全選

`lb0017.component.ts` 修改前：

```ts
const EXPORT_ALL = 'ALL';
const EXPORT_ITEM_CODES = ['setting', 'items', 'mailTplt', 'func', 'embeddedFunc', 'rtnCode'];
...
const code = ['select_all', 'ml_export_setting', ...];
const dict = await this.toolService.getDict(code);
this.exportItemOptions = [
  { label: dict['select_all'], value: EXPORT_ALL },
  { label: dict['ml_export_setting'], value: 'setting' },
  ...
];
...
onExportItemListChange(event: any) {
  const value: string[] = event.value || [];
  if (value.includes(EXPORT_ALL)) {
    this.exportItemList.setValue([...EXPORT_ITEM_CODES], { emitEvent: false });
  }
}
```

修改後：拿掉 `EXPORT_ALL`／`EXPORT_ITEM_CODES` 常數、拿掉 `select_all` 選項與對應的 dict 查詢、刪除 `onExportItemListChange()`。`lb0017.component.html` 對應拿掉 `(onChange)="onExportItemListChange($event)"` 綁定。

#### 2. `exportItemOptions` label 直接使用 value 內容，不查 i18n 字典

修改前：

```ts
const code = ['ml_export_setting', 'ml_export_items', 'ml_export_mail_tplt', 'ml_export_func', 'ml_export_embedded_func', 'ml_export_rtn_code'];
const dict = await this.toolService.getDict(code);
this.exportItemOptions = [
  { label: dict['ml_export_setting'], value: 'setting' },
  ...
];
```

修改後：

```ts
this.exportItemOptions = [
  { label: 'setting', value: 'setting' },
  { label: 'items', value: 'items' },
  { label: 'mailTplt', value: 'mailTplt' },
  { label: 'func', value: 'func' },
  { label: 'embeddedFunc', value: 'embeddedFunc' },
  { label: 'rtnCode', value: 'rtnCode' },
];
```

#### 3. 匯出成功後補上 toast 提示

`export()` 原本下載 xlsx 成功（`res.type !== 'application/json'`）後只做檔案下載，沒有任何提示。比照專案既有慣例（`summary`/`detail` 組字典 key + `message.success`），在下載成功後新增：

```ts
const code = ['export', 'message.success'];
const dict = await this.toolService.getDict(code);
this.messageService.add({
  severity: 'success',
  summary: dict['export'],
  detail: `${dict['export']} ${dict['message.success']}!`,
});
```

`importMultilingual()` 原本就已經有成功 toast（`upload_result` + `message.success`），未變動。

#### 4. Template 補上 `<p-toast>`

`lb0017.component.html` 在 `</app-container>` 後補上：

```html
<p-toast [style]="{marginTop: '60px'}" position="top-left"></p-toast>
```

元件 `imports` 陣列已經有 `PrimengModule`（內含 `ToastModule`），不需要額外 import。

#### 5. 新增 `.claude/RULES/angular-component.md` 專案規則

在 `.claude/RULES/angular-component.md` 新增「Toast（`MessageService`）通知」章節，記錄：只要元件注入 `MessageService` 並呼叫 `this.messageService.add(...)`，template 就必須加入 `<p-toast>`，否則 toast 不會顯示；並附上標準寫法與參考範例（`lb0014`、`lb0017`）。

> **修正說明**：此規則最初誤放進 `srcAngular/CLAUDE.md`。當時用 Glob 搜尋 `CLAUDE.md` 誤回報「找不到檔案」，於是誤判專案沒有 `CLAUDE.md`，直接用 `Write` 新增一份只含 Toast 規則的極簡版本，**整個覆蓋掉了原本內容豐富、且已有完整 git 提交歷史的既有 `CLAUDE.md`**。後續 `CLAUDE.md` 已還原回原始內容，但這也代表最初寫入的 Toast 規則跟著消失、未被保留在任何規則檔案中。經使用者要求覆核後發現此問題，改將規則正確地新增到 `CLAUDE.md` 本身列出的延伸規則文件 `.claude/RULES/angular-component.md`（元件撰寫規範，性質上更貼切），而不是再次整份改寫 `CLAUDE.md`。

## 預計修改方式

以下依 5 個需求，分別記錄提出當下的 context、實際調查／分析過程，以及最後定案的修正方案。

### 需求 1：移除手動全選，改用元件內建全選

**Context**

使用者選取 `lb0017.component.ts` 第 86 行（`select_all` 所在的 `code` 陣列），要求「移除 code 的 select_all 項目，透過元件內建功能全選即可」。

**分析（已對照原始碼確認）**

- 讀取 `lb0017.component.ts` 全文，發現 `ngOnInit()` 內組了一個字典查詢陣列 `code`，其中第一個 key 是 `'select_all'`，用來組出 `exportItemOptions` 陣列裡的一個特殊選項 `{ label: dict['select_all'], value: EXPORT_ALL }`（`EXPORT_ALL = 'ALL'`）。
- `onExportItemListChange()` 監聽 `p-multiSelect` 的 `(onChange)`，一旦選到 `EXPORT_ALL` 這個 sentinel value，就用 `setValue([...EXPORT_ITEM_CODES])` 把表單值換成「全部真實選項」，模擬全選效果。
- 用 grep 確認 `EXPORT_ALL`、`EXPORT_ITEM_CODES`、`select_all`、`onExportItemListChange` 這幾個識別字只在 `lb0017.component.ts`/`.html` 內出現、且只被這組全選邏輯使用，沒有被其他地方依賴，可以安全整組移除。
- 確認 PrimeNG `p-multiSelect` 本身已內建「全選」checkbox（`showToggleAll`，預設值為 `true`，會顯示在下拉選單的表頭），不需要自己實作 sentinel value 的 workaround。

**修正方案**

刪除 `EXPORT_ALL`／`EXPORT_ITEM_CODES` 常數、`select_all` 的 dict 查詢與對應選項、`onExportItemListChange()` 方法本身，並拿掉 template 上的 `(onChange)="onExportItemListChange($event)"` 綁定，改為完全依賴 `p-multiSelect` 元件內建的全選功能。

### 需求 2：`exportItemOptions` label 不查 i18n，直接用 value

**Context**

使用者選取 `exportItemOptions` 這個屬性，要求「內容 label 我不需要轉換多國語系，直接使用 value 同樣的內容即可」。

**分析**

- 需求 1 完成後，`exportItemOptions` 建構邏輯仍保留一組 `code`（`ml_export_setting`、`ml_export_items` 等 6 個 i18n key）搭配 `this.toolService.getDict(code)` 查字典來產生 label。
- 這 6 個 key 對應到 `zh-tw.json` 等語系檔中「設定值」「項目」「郵件範本」等中文翻譯（第 1016~1022 行），使用者希望不要顯示翻譯後文字，而是直接顯示跟 `value` 相同的英文代碼（`setting`、`items`、`mailTplt`…）。

**修正方案**

整段移除 `code`/`dict` 查詢邏輯，`exportItemOptions` 改為 `label` 與 `value` 內容相同的靜態陣列（例如 `{ label: 'setting', value: 'setting' }`）。因為不再需要非同步字典查詢，`ngOnInit()` 這一段也順勢從「先 await 字典再組陣列」簡化成同步賦值。

### 需求 3：匯出／匯入成功後要有 toast 提示

**Context**

使用者反應「匯出匯入成功後沒有建立相關的 toast 提示，請幫我補上」。

**分析**

- 重新讀整份 `lb0017.component.ts`，發現 `importMultilingual()` 在 `checkDpSuccess` 通過後，其實已經有呼叫 `this.messageService.add({...})` 顯示成功訊息（`summary: dict['upload_result']`、`detail: dict['message.success']!`），這段邏輯在使用者反應問題前就已存在。
- 反觀 `export()`：分成兩個分支，`res.type === 'application/json'` 是後端回傳錯誤（走 `alertService.ok(...)` 顯示警告訊息），另一分支（正常拿到 xlsx 檔）只呼叫 `FileSaver.saveAs(...)` 下載檔案，完全沒有任何成功提示。
- 用 grep 搜尋整個 `src/app/layout` 底下 `messageService.add` 的既有用法，確認專案慣例是：`summary` 用某個動作/情境的 i18n key（如 `message.create`、`message.delete`），`detail` 用同一個 key 再接 `message.success`（`成功`）組成，例如 `` `${dict['message.create']} ${dict['message.success']}!` ``。
- 確認 i18n 字典裡已存在可用的頂層 key `export`（`"匯出"`）與 `message.success`（`"成功"`），不需要新增翻譯 key。

**修正方案**

只針對 `export()` 缺少提示的成功分支補上 toast，寫法比照專案既有慣例：`const code = ['export', 'message.success']` → `await this.toolService.getDict(code)` → `messageService.add({ severity: 'success', summary: dict['export'], detail: ... })`。因為需要 `await getDict(...)`，`subscribe` 的 callback 由同步改為 `async`。`importMultilingual()` 原本的 toast 邏輯不變。

### 需求 4：toast 補上後仍然沒有顯示

**Context**

使用者選取 lb0014 頁面裡的 `<p-toast [style]="{marginTop: '60px'}" position="top-left"></p-toast>` 這一行，反應「html 頁面沒加入這段內容導致無法正常呈現 toast」。

**分析**

- 這點出了需求 3 遺漏的根因：PrimeNG 的 `MessageService.add(...)` 只是把訊息塞進一個共用的訊息佇列，實際畫面上必須有一個 `<p-toast>` 元件訂閱並渲染這個佇列，否則呼叫 `add()` 不會有任何視覺效果。
- 檢查 `lb0017.component.html`，確認裡面完全沒有 `<p-toast>` 標籤，這正是問題根因（而不是 `messageService.add()` 呼叫本身有誤）。
- 對照同資料夾內 `lb0014.component.html`，確認該頁面已經在 `</app-container>` 之後、以同一層級的 sibling 元素方式放置 `<p-toast [style]="{marginTop: '60px'}" position="top-left"></p-toast>`，是專案內既有可依循的正確寫法。
- 確認元件已經在 `imports` 陣列中引入 `PrimengModule`，而 `PrimengModule` 內已經 export 了 `ToastModule`，因此不需要額外新增 import，只需要在 template 補上標籤即可。

**修正方案**

在 `lb0017.component.html` 的 `</app-container>` 之後，比照 `lb0014.component.html` 加入同一段 `<p-toast [style]="{marginTop: '60px'}" position="top-left"></p-toast>`。

### 需求 5：把這個慣例寫成規則，避免未來再犯

**Context**

使用者要求「能否幫我把 `<p-toast>` 這個建立一個 rules，以便於日後使用 messageService 時都要在 html 中加入」。

**分析**

- 這類「程式撰寫慣例／規則」屬於可以直接從專案現狀（程式碼）推導出來的資訊，不適合放進僅限本次對話使用的暫存記憶，而應該落地成專案內、會被版本控制、任何人（包含未來的 AI 協作者）都看得到的規則文件。
- **第一次判斷（有誤）**：用 Glob 搜尋 `CLAUDE.md`／`**/CLAUDE.md`，誤回報「找不到檔案」，因此誤判專案沒有任何 `CLAUDE.md`，決定直接用 `Write` 新增一份僅含 Toast 規則的極簡版本。這個判斷實際上是錯的：`srcAngular/CLAUDE.md` 早已存在、內容涵蓋整個專案架構說明，且有完整 git 提交歷史，`Write` 因而整份覆蓋掉了原始內容。
- **覆核發現問題**：使用者事後要求檢查「這條規則說明如果從 spec 文件刪掉，在現在的規則中是否會遺漏」。重新讀取 `CLAUDE.md` 才發現內容已是（被還原回的）原始豐富版本，且用 grep 搜尋 `p-toast`／`Toast`／`MessageService` 於 `CLAUDE.md`、`AGENTS.md`、`.claude/RULES/*` 全部沒有結果——代表最初寫入的 Toast 規則確實已遺失，且 `git status` 顯示 `CLAUDE.md` 當下與 git HEAD 完全一致（無待commit差異），可推斷檔案已被還原、我當初的覆蓋沒有留下痕跡。
- **正確的落地位置**：`CLAUDE.md` 本身在「延伸規則文件（`.claude/`）」章節列出一張表，依主題把細部規範拆到 `.claude/RULES/*.md`，其中 `.claude/RULES/angular-component.md` 專門處理「新增或修改元件」相關規範，性質上最貼近「元件要正確顯示 Toast」這條規則，比直接改動 `CLAUDE.md` 本體更符合專案既有的文件分工方式。

**修正方案**

在 `.claude/RULES/angular-component.md` 新增「Toast（`MessageService`）通知」章節，內容包含：規則本身（注入 `MessageService` 就必須在 template 加 `<p-toast>`）、標準放置位置與寫法、`PrimengModule` 已內含 `ToastModule` 不需額外 import 的補充說明、`summary`/`detail` 组字典 key 的既有慣例範例，並附上 `lb0014`／`lb0017` 作為參考實作。`CLAUDE.md` 本體維持原始內容不再改動。

## 驗證方式

### 靜態驗證

- `npm run build`（`ng build --optimization=true`）執行成功，exit code 0，`lb0017` 對應的 chunk（`labs-lb0017-lb0017-component`）正常產出，`stdout` 中無 error（僅有既有、與本次改動無關的 `file-saver` 型別宣告、jQuery `.click()` deprecated 兩則 IDE 診斷訊息）。

### 尚待人工驗證（建議）

- 實際啟動 `npm start`，開啟 lb0017 頁面：
  - 確認匯出項目下拉選單有內建的全選 checkbox，且行為與先前手動邏輯一致（勾選全選 = 6 個項目全選，取消全選 = 6 個項目全清空）。
  - 確認選單顯示文字為 `setting`／`items`／`mailTplt`／`func`／`embeddedFunc`／`rtnCode` 等英文代碼，而非中文翻譯。
  - 執行一次匯出，確認下載完成後畫面出現成功 toast，且位置／樣式與 `lb0014` 一致。
  - 執行一次匯入，確認成功後 toast 仍正常顯示。

## 實作紀錄

### 時間軸

1. **需求 1 提出**：使用者選取 `lb0017.component.ts:86` 的 `select_all`，要求移除 code 內的 `select_all` 項目，改用元件內建全選功能。
2. **調查與實作**：讀取整份 `lb0017.component.ts`／`.html`，確認 `select_all` 是透過 `EXPORT_ALL` sentinel value + `onExportItemListChange()` 手動模擬全選；用 grep 確認這組識別字沒有被其他地方使用；確認 PrimeNG `p-multiSelect` 已內建全選 checkbox；移除相關常數、dict 查詢、選項與事件處理方法，template 拿掉對應的 `(onChange)` 綁定。
3. **需求 2 提出**：使用者選取 `exportItemOptions`，要求 label 不用轉多國語系，直接用跟 value 一樣的內容。
4. **實作**：移除該段 `getDict()` 查詢，`exportItemOptions` 改為 `label`/`value` 相同的靜態陣列。
5. **需求 3 提出**：使用者反應匯出匯入成功後沒有 toast 提示，要求補上。
6. **調查**：重讀 `lb0017.component.ts`，確認 `importMultilingual()` 早已有成功 toast，但 `export()` 下載成功分支完全沒有提示；grep 專案內既有 `messageService.add` 用法，歸納出「動作 key + `message.success`」的組字慣例；確認 i18n 已有可用的 `export`／`message.success` key，不需新增翻譯。
7. **實作**：在 `export()` 的成功分支補上 `messageService.add(...)`，`subscribe` callback 改為 `async` 以支援 `await getDict(...)`。
8. **需求 4 提出**：使用者選取 `lb0014.component.html` 裡的 `<p-toast>` 那一行，反應 `lb0017.component.html` 沒有加入這段內容，導致 toast 無法正常呈現。
9. **調查**：確認 PrimeNG `MessageService` 需要頁面上存在 `<p-toast>` 才會渲染訊息佇列；檢查 `lb0017.component.html` 內確實沒有 `<p-toast>`；對照 `lb0014.component.html` 既有寫法（放在 `</app-container>` 之後的 sibling 位置）；確認 `PrimengModule` 已內含 `ToastModule`，component 的 `imports` 已引入 `PrimengModule`，不需額外 import。
10. **實作**：在 `lb0017.component.html` 補上與 `lb0014` 相同的 `<p-toast [style]="{marginTop: '60px'}" position="top-left"></p-toast>`。
11. **需求 5 提出**：使用者要求把「使用 `MessageService` 要在 html 加 `<p-toast>`」建立成規則，供日後開發參考。
12. **判斷落地位置（第一次，有誤）**：確認此類「程式撰寫慣例」屬於可從專案現狀推導、應版本控制的資訊，不適合存放在僅限單次對話的暫存記憶；用 Glob 搜尋 `CLAUDE.md`／`**/CLAUDE.md` 皆回報「找不到檔案」，因而誤判專案根目錄下沒有 `CLAUDE.md`。
13. **實作（第一次，後續被判定有誤）**：用 `Write` 新增 `srcAngular/CLAUDE.md`，寫入只含 Toast 規則的極簡內容——但因為誤判「檔案不存在」，這個 `Write` 實際上整份覆蓋掉了本來就存在、內容豐富且有完整 git 提交歷史的原始 `CLAUDE.md`。
14. **使用者要求先 build 再上傳分支**：執行 `npm run build`，確認 exit code 0、無編譯錯誤後，準備進行 git 提交。
15. **中斷 git 流程**：使用者中斷了查看 git status／準備 commit 的操作，改為要求先建立本次工作的紀錄檔案（即本文件）；本文件第一版內容不夠詳盡，經使用者指出後，補上「預計修改方式」章節。
16. **使用者要求覆核**：使用者選取 spec 文件中「希望把使用 `MessageService` 就要在 html 加 `<p-toast>` 建立成規則」這段說明，要求檢查「若把這段說明從 spec 刪掉，在現在的規則中是否會遺漏」，若不會遺漏才刪除。
17. **覆核與發現問題**：重新讀取 `CLAUDE.md`，看到的是完整的原始專案文件（非稍早寫入的極簡版本）；用 grep 搜尋 `p-toast`／`Toast`／`MessageService` 於 `CLAUDE.md`、`AGENTS.md`、`.claude/RULES/*`，皆無結果；確認 `git status` 顯示 `CLAUDE.md` 當下與 git HEAD 完全一致、無任何未提交差異。三者合併判斷：`CLAUDE.md` 已被還原回原始內容，稍早寫入的 Toast 規則已遺失、目前不存在於任何規則檔案中——因此依使用者的判斷條件，**不刪除** spec 文件裡的這段說明。
18. **詢問使用者**：說明上述發現後，詢問使用者是否要把規則補回去、要補在哪個檔案，使用者選擇補進 `.claude/RULES/angular-component.md`（`CLAUDE.md` 本身列出的元件撰寫細部規範文件）。
19. **實作（修正）**：在 `.claude/RULES/angular-component.md` 新增「Toast（`MessageService`）通知」章節，記錄規則、標準寫法、`PrimengModule` 已內含 `ToastModule` 的補充說明、`summary`/`detail` 组字典慣例，並附上 `lb0014`／`lb0017` 參考實作；`CLAUDE.md` 本體維持原始內容，不再改動。
20. **回頭修正本文件**：更新「修改檔案清單」「詳細程式碼異動 5」「需求 5」章節與時間軸，把規則落地位置由 `CLAUDE.md` 改為 `.claude/RULES/angular-component.md`，並如實記錄這次誤判/覆蓋/修正的完整過程。
21. **本次工作尚未提交／推送到分支**，仍待使用者後續指示執行 git commit/push。

### 經驗紀錄（給未來類似任務參考）

- PrimeNG 元件多半已內建常見的互動功能（如 `p-multiSelect` 的全選 checkbox），手動實作 sentinel value 之類的 workaround 前，應先確認元件本身是否已支援，避免重複造輪子、也避免像本例一樣留下容易誤用的特殊值（`EXPORT_ALL = 'ALL'`）。
- `MessageService.add()` 呼叫成功不代表 toast 會顯示，PrimeNG 需要頁面上存在對應的 `<p-toast>` 才會渲染；這是一個容易被忽略、且不會在編譯期被抓到的錯誤（TypeScript/Angular 編譯都不會報錯），值得寫成專案規則提醒未來開發，而不是只在這次事後補救。
- 屬於「程式撰寫慣例／規則」性質的資訊，應該落地成專案內版本控制的文件，而不是放進僅限單次對話的暫存記憶，這樣才能真正被未來任何開發者（人或 AI）看到並遵循；且要優先放進專案既有的規則分工位置（本例是 `.claude/RULES/angular-component.md`），而不是不管三七二十一都改 `CLAUDE.md` 本體。
- **`Write` 新增檔案前，Glob／搜尋工具「找不到檔案」不代表檔案真的不存在**，尤其是專案根目錄下這種關鍵設定/文件檔名（`CLAUDE.md`、`.env`、`package.json` 等）。這次就是因為 Glob 誤回報導致誤判、進而用 `Write` 整份覆蓋掉既有的重要文件。之後遇到類似情況，應該多一道確認（例如用 `ls`/`git ls-tree` 或直接嘗試 `Read`）再決定要新增還是修改，尤其是「新增」一個聽起來像是專案常見慣例檔名的檔案時要特別小心。
- 紀錄文件除了寫「最後改了什麼」，也應該完整寫出「當下 context 是什麼、怎麼分析、為什麼選這個修法」的思考過程，**包含中間走過的錯誤判斷與修正經過**，方便日後回顧時理解決策脈絡，而不只是複製最終 diff。

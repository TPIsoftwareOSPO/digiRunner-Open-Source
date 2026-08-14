# gtwidp errMsg 頁面卡在 Loading 畫面

派工需求單：https://docs.google.com/document/d/1mlX7M8qO38Tbua-7xXkW_zXJQVdR5HWOcrjo1HT0wgQ/edit?tab=t.0

## 修改內容

### 問題描述

透過類似以下網址進入時：

```
https://{hostname}/dgrv4/ac4/gtwidp/errMsg?redirect_uri=https:%2F%2Foauth.pstmn.io%2Fv1%2Fcallback&rtn_code=error&msg=5a-G
```

若 `msg` query param 內容無法有效解碼／轉譯，畫面的訊息文字會一直停在初始值「Loading...」，看起來像卡住，而非顯示實際的錯誤說明。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `src/app/layout/login/gtwidp/gtwidp.component.ts` | `type=errMsg` 分支改用 `js-base64` 解碼 `msg`，並加上 try/catch fallback |
| `src/assets/i18n/zh-tw.json` | 新增 `gtwidp.msg_decode_error` key |
| `src/assets/i18n/zh-cn.json` | 新增 `gtwidp.msg_decode_error` key |
| `src/assets/i18n/en-us.json` | 新增 `gtwidp.msg_decode_error` key |
| `src/assets/i18n/ja.json` | 新增 `gtwidp.msg_decode_error` key |

### 詳細程式碼異動

#### `gtwidp.component.ts`

新增 import：

```ts
import * as base64 from 'js-base64';
```

`ngOnInit()` 內 `type === 'errMsg'` 分支，修改前：

```ts
else {
  this.show = true;
  this.msg = this.toolService.Base64Decoder(this.paramsObj['msg'])
}
```

修改後：

```ts
else {
  this.show = true;
  try {
    this.msg = base64.Base64.decode(this.paramsObj['msg']);
  } catch {
    const dict = await this.toolService.getDict(['gtwidp.msg_decode_error']);
    this.msg = dict['gtwidp.msg_decode_error'];
  }
}
```

#### 新增 i18n key：`gtwidp.msg_decode_error`

| 語系 | 內容 |
|------|------|
| zh-tw | 無法顯示錯誤訊息內容，請點選「返回登入」重新嘗試。 |
| zh-cn | 无法显示错误消息内容，请点击「返回登录」重新尝试。 |
| en-us | Unable to display the error message. Please click "Return to Login" to try again. |
| ja | エラーメッセージを表示できませんでした。「ログインに戻る」をクリックして再度お試しください。 |

## 預計修改方式

### Context

使用者回報：透過類似
`https://{hostname}/dgrv4/ac4/gtwidp/errMsg?redirect_uri=...&rtn_code=error&msg=5a-G`
的網址進入時，若 `msg` query param 內容無法有效解碼，畫面的訊息文字會一直停在初始值「Loading...」，看起來像卡住，而不是顯示實際的錯誤說明。

### 根因分析（已對照原始碼並實際驗證）

- 路由 `gtwidp/:type`（`app.routing.ts`）在 `type=errMsg` 時載入 `GtwidpModule` → `GtwidpComponent`（`src/app/layout/login/gtwidp/gtwidp.component.ts`）。
- `msg: string = 'Loading...'` 是欄位初始值，畫面直接以 `{{ msg }}` 顯示（`gtwidp.component.html`）。
- `ngOnInit()` 原本呼叫 `this.toolService.Base64Decoder(this.paramsObj['msg'])`。
- `ToolService.Base64Decoder()`（`src/app/shared/services/tool.service.ts`）直接呼叫 `window.atob(base64str)`，**沒有 try/catch**，且 `atob()` 只認得標準 base64 字元集（`+`、`/`），不認得 base64url 字元集（`-`、`_`）。
- **真正原因**：`msg=5a-G` 其實是合法的 **base64url** 編碼（用 `-` 取代標準 base64 的 `+`），不是損毀資料。已用 Node 實際驗證：
  ```
  atob('5a-G')                          → 拋出例外 "Invalid character"
  js-base64 的 Base64.decode('5a-G')     → 正確解出 "密"
  ```
- 由於 `ngOnInit()` 是 `async` function，`atob()` 的同步例外會讓該次執行中斷在這一行，`this.msg` 永遠不會被賦值，畫面因此一直顯示初始的「Loading...」文字。`show`／`path['type']==='errMsg'` 這兩個控制面板顯示與否的旗標在例外拋出**之前**就已設定完成，所以面板與「返回登入」按鈕其實都有正確顯示，問題單純出在 `msg` 文字沒有被正確賦值。
- **關鍵佐證**：同樣處理 SSO 錯誤訊息的姊妹元件 `idpsso.component.ts`（`src/app/layout/idpsso/`）已經在用 `js-base64` 套件正確解碼：
  ```ts
  import * as base64 from 'js-base64';
  this.msg = base64.Base64.decode(value['msg'])
  ```
  `js-base64` 的 `Base64.decode()` 會自動處理 base64url 變體，是標準 base64 解碼的超集合。`gtwidp` 只是沒有比照這個既有模式，改用了較嚴格、且沒有防護的 `ToolService.Base64Decoder()`。

### 為何不修改共用的 `Base64Decoder()`

`Base64Decoder()` 目前被 9 個以上不同功能呼叫（`client-ca.component.ts`、`tsmpdpfile.component.ts`、`param-item.component.ts` 等，多半用於解內部產生的 token / blob 資料）。即使 `js-base64` 是超集合、理論上風險低，仍選擇不去動這個共用方法，改為讓 `gtwidp.component.ts` 直接比照 `idpsso.component.ts` 既有寫法引入 `js-base64`，範圍更小、有直接的既有先例可循，符合專案「拒絕無關重構」「遵循既有模式」原則。

### 修正方案

1. `src/app/layout/login/gtwidp/gtwidp.component.ts`：比照 `idpsso.component.ts` 既有寫法，改用 `js-base64` 解碼（能正確處理 base64url），並保留 try/catch 作為「真的無法解碼」時的最後防護，fallback 顯示新增的 i18n 錯誤訊息（此檔案已用 `this.toolService.getDict([...])` 模式，維持風格一致）。
2. 新增 i18n key `gtwidp.msg_decode_error`：依 `.claude/RULES/i18n.md` 規則，同步新增到四份語系檔（`src/assets/i18n/{zh-tw,zh-cn,en-us,ja}.json`）。

## 驗證方式

### 靜態驗證

- 確認 TypeScript 變更語法正確、`ToolService`／`js-base64` 依賴皆已存在於專案（`package.json` 已有 `"js-base64": "^3.7.2"`），無需新增套件。
- 確認四份語系檔（`zh-tw`、`zh-cn`、`en-us`、`ja`）皆為合法 JSON，且新增的 `gtwidp.msg_decode_error` key 在四份檔案中皆存在、皆非空值。

### 邏輯模擬驗證（Node 直接呼叫 `js-base64`）

用 Node 模擬新的 decode 邏輯（`try Base64.decode(msg)`，失敗則 fallback），針對以下輸入驗證：

| 輸入 | 預期結果 | 實測結果 |
|------|----------|----------|
| `5a-G`（使用者原始回報案例，base64url 編碼） | 正確解碼，不再卡在 Loading | ✅ 解出「密」 |
| 標準 base64（例：`btoa('hello world')`） | 正確解碼，行為與修改前相同 | ✅ 解出 `hello world` |
| `msg` 缺失（`undefined`） | 觸發 try/catch，顯示 fallback 錯誤訊息 | ✅ 觸發 fallback |
| 夾雜非法字元的字串（例：`!!!not-base64!!!`） | 原預期會觸發 fallback | ⚠️ 實測未拋例外，`js-base64` 會忽略非法字元並盡量解出（可能得到亂碼），故不會進入 catch |

### 驗證結論與已知限制

- 本次要修正的核心案例（`msg=5a-G` 這類 base64url 編碼）已確認修正生效，畫面不會再卡在「Loading...」。
- `js-base64` 的 `Base64.decode()` 相較原生 `atob()` **非常寬容**，對夾雜非法字元的輸入不會拋例外，而是盡量解碼（可能得到亂碼文字顯示給使用者，而非顯示「無法解碼」的錯誤訊息）。try/catch fallback 實際上主要防護的是 `msg` 完全缺失或其他極端執行期例外，而非「格式怪異但仍含部分合法字元」的輸入。
- 這個特性不影響本次修復目標（避免卡在 Loading 畫面），因為無論如何都不會再發生「例外中斷、`msg` 永遠不被賦值」的情況。若未來需要更嚴謹地偵測「內容雖可解碼但顯然不是預期訊息」的情況，需另外評估（例如檢查解碼結果是否為可印字元），本次不在範圍內。

### 尚待人工驗證（建議）

- 實際啟動 `npm start`，於瀏覽器組出上述測試網址進行手動驗證，確認畫面呈現與瀏覽器 console 皆無異常。
- 確認「返回登入」按鈕在 fallback 情境下仍可正常運作（呼叫 `returnToLogin()`，把原始未解碼的 `msg` 帶回 `redirect_uri`，此行為未變更）。

## 實作紀錄

### 時間軸

1. **需求提出**：使用者回報 `gtwidp/errMsg` 頁面在 `msg` query param 無法有效轉譯時，畫面會一直停在 loading 呈現，要求協助修改及優化。

2. **初步調查**：透過 Explore agent 調查 `src/app/layout/login/gtwidp/`，找到 `GtwidpComponent.ngOnInit()` 中 `type === 'errMsg'` 分支呼叫 `ToolService.Base64Decoder()`（底層為 `window.atob()`），且該方法沒有 try/catch。初步診斷為「`msg=5a-G` 屬於非法/損毀的 base64 資料，`atob()` 拋例外導致 `this.msg` 永遠不會被賦值」。

3. **提出初版計畫**：計畫在 `gtwidp.component.ts` 加上 try/catch，解碼失敗時 fallback 顯示新的 i18n 錯誤訊息。此版計畫尚未考慮到「`msg` 是否其實可以被正確解碼」。

4. **使用者質疑並提供關鍵線索**：使用者指出用 https://www.base64decode.org/ 實測 `5a-G` 可以正常解碼，要求先確認原因，並詢問是否能優化解碼流程本身，而非直接顯示「無法解碼」的錯誤訊息。

5. **重新調查與驗證**：
   - 用 Node 實際執行 `atob('5a-G')` 確認會拋出 `Invalid character` 例外。
   - 同時測試專案既有依賴 `js-base64` 的 `Base64.decode('5a-G')`，確認可正確解出「密」。
   - 發現根本原因：`5a-G` 是合法的 **base64url** 編碼（用 `-` 取代標準 base64 的 `+`），`atob()` 只支援標準 base64 字元集，遇到 `-`/`_` 會直接拋例外；`js-base64` 的解碼器則會自動處理 base64url 變體。
   - 查找專案內是否有既有先例：確認姊妹元件 `idpsso.component.ts`（處理同類型 SSO 錯誤訊息流程）已經在用 `js-base64` 的 `Base64.decode()`，證實 `gtwidp` 只是沒有比照這個既有正確作法。

6. **修訂計畫**：改為讓 `gtwidp.component.ts` 比照 `idpsso.component.ts` 既有寫法，直接引入 `js-base64` 解碼（可正確處理使用者實際遇到的 base64url 案例），並保留 try/catch 作為最後防護（fallback 顯示新增的 i18n 錯誤訊息），而非一律顯示錯誤訊息。同時說明為何不修改共用的 `ToolService.Base64Decoder()`（該方法被 9 個以上不相關功能呼叫，範圍過大，且已有更貼近問題現場的既有先例可循）。

7. **實作**：
   - 於 `gtwidp.component.ts` 新增 `import * as base64 from 'js-base64';`。
   - 將 `type === 'errMsg'` 分支的解碼邏輯改為 `try { this.msg = base64.Base64.decode(...) } catch { ... fallback ... }`。
   - 新增 i18n key `gtwidp.msg_decode_error`，同步補齊 `zh-tw`、`zh-cn`、`en-us`、`ja` 四份語系檔。

8. **驗證**：
   - 確認四份語系檔皆為合法 JSON、新 key 完整無缺漏。
   - 用 Node 模擬新的 decode 邏輯，驗證使用者回報案例（`5a-G`）、標準 base64、`msg` 缺失等情境的實際行為。
   - 過程中發現 `js-base64` 的解碼器對「夾雜非法字元」的輸入相當寬容，不會拋例外（與原本計畫預期的「非法字元會觸發 fallback」不完全相符），已如實記錄在驗證文件中，並確認不影響本次修復目標。

### 經驗紀錄（給未來類似任務參考）

- **不要只看表面症狀就下結論**：一開始以為 `msg=5a-G` 是「損毀/非法資料」，但實際上是合法的 base64url 編碼，只是解碼函式選錯了。使用者的質疑（用外部工具驗證可以解碼）是扭轉診斷方向的關鍵。
- **先找專案內既有先例，再決定修法**：發現 `idpsso.component.ts` 已經用 `js-base64` 正確處理同類問題後，修法從「補一個 fallback 錯誤訊息」升級為「用對的方式解碼，讓大部分情況根本不會走到錯誤分支」，品質更好且改動範圍沒有變大。
- **驗證時要如實記錄跟預期不符的地方**：`js-base64` 的寬容特性跟原計畫預期的行為不完全一致，已誠實記錄於「驗證方式」章節，而非美化成完全符合預期。

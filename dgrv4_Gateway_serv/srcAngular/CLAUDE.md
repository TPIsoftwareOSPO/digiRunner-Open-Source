# CLAUDE.md

本文件提供 Claude Code（claude.ai/code）在此儲存庫中工作時所需的指引。

## 專案概覽

本專案是 digiRunner / DGR v4 Gateway 服務的 Angular 前端管理介面，提供登入、後台版面、API Gateway 管理、系統維運、Labs 工具與 AI Gateway 相關畫面。建置後的產物會輸出並複製到後端 Java 服務的靜態資源目錄，本身並非獨立部署的 SPA。

- 框架：Angular 20（混合使用 NgModule 與部分 standalone components）
- UI：Bootstrap 5、PrimeNG 20（Aura 主題，`app.module.ts` 內有客製化橘色 preset）、Angular Material、FontAwesome
- 圖表：Chart.js、ECharts
- 多語系：`@ngx-translate/core`，語系檔位於 `src/assets/i18n/`（`zh-tw`、`zh-cn`、`en-us`、`ja`）
- 測試框架：Karma / Jasmine
- Dev server：`http://localhost:4701/`
- Runtime base path / `baseHref`：`/dgrv4/ac4/`

## 延伸規則文件（`.claude/`）

除本檔與 `AGENTS.md` 之外，`.claude/RULES/` 下還有依主題拆分的細部規範，處理對應類型的任務時請一併查閱：

| 檔案 | 適用時機 |
|------|----------|
| `.claude/RULES/api-pattern.md` | 新增或修改 API 呼叫、`ApiBaseService`／`api-*.service.ts` 相關工作時 |
| `.claude/RULES/angular-component.md` | 新增或修改元件（standalone/NgModule 選擇、類別內部順序、表單、loading、RxJS 清理慣例）時 |
| `.claude/RULES/dialog-modal.md` | 需要開啟彈窗、確認對話框時 |
| `.claude/RULES/style.md` | 新增或調整 CSS/SCSS 樣式、版面排版、按鈕/PrimeNG 元件外觀時 |
| `.claude/RULES/primeng-mcp.md` | 需要查詢 PrimeNG 元件 API（props/events/templates）、或要調整 `.mcp.json`／`.codex/config.toml` 的 MCP 設定時 |
| `.claude/RULES/i18n.md` | 新增或調整畫面文字、語系 key 時 |
| `.claude/RULES/unit-test.md` | 使用者要求新增或修改單元測試時 |
| `.claude/RULES/commit-pr.md` | 撰寫 commit 訊息、討論分支策略時 |

另外 `.claude/commands/git-commit-standardizer.md` 是可直接呼叫的 slash command（`/git-commit-standardizer`），依暫存區變更自動產出符合本專案慣例的 commit 訊息；同樣內容也做成 `.claude/skills/git-commit-standardizer/SKILL.md` 這個 skill，當使用者用自然語言要求「幫我寫 commit message」等情境時會自動觸發，不需要輸入 slash command。

`.claude/commands/spec-recorder.md`（`/spec-recorder`）則用於在一輪工作完成後，回顧該次對話的需求與實作過程，依既有慣例在 `doc/specs/` 產出 `YYYY-MM-DD_<slug>.md` 工作紀錄檔。

## 常用指令

```bash
npm ci                # 依 package-lock.json 安裝依賴
npm start              # ng serve --port 4701 --open
npm run build           # ng build --optimization=true（production build）
npm run watch           # ng build --watch --configuration development
npm test                # ng test（Karma/Jasmine）

cd .tools/primeng-mcp && npm ci   # 安裝 PrimeNG MCP server（clone 後需執行一次，與根目錄安裝獨立）
```

執行單一測試檔：可用 Karma 標準過濾方式，例如 `ng test --include='**/ac0101.component.spec.ts'`。

Production build 產物會輸出到 `../src/main/resources/static/dgrv4/ac4/`（見 `angular.json` 的 `outputPath`），而非 Angular 預設的 `dist/`，因為本專案是直接給後端服務使用的靜態資源。

開發環境 API 端點設定於 `src/environments/environment.ts`（`apiUrl`、`dpPath`、`netApiUrl`，依檔案開頭的 `DEV_HOST`／`DEV_PORT` 常數組成）。此檔案內含大量歷史環境註解，切換環境時請避免提交不必要的本機設定變更。

## 架構說明

### 路由 / 模組結構

- `app.routing.ts` — 最外層路由：`login`、`login2`、`idpsso/*`、`ldap`、`gtwidp/*`、`smart/*`（SMART on FHIR 的 consent / patient / provider 選擇），以及 `''` lazy-load `layout.module.ts`。
- `layout/layout.routing.ts` 是登入後主畫面（`LayoutComponent`）的核心路由表。幾乎每個功能都是以功能代碼命名、lazy-loaded 的 NgModule，route 上的 `data.id` 對應該代碼（用於權限 /選單查詢）：
  - `ac00`–`ac13` — 核心 Gateway / 後台管理畫面（API、Client、User、Role、Group Auth、Token、Security、Certificate Authority、Server、Job、Alert、Event、Mail、Report、Theme、SiteMap 等）
  - `np01`–`np05` — 其他編號功能模組
  - `ai00/ai0001`–`ai0005` — AI Gateway（AI Provider、AI API Key、API Key Usage、Prompt Template、User Prompt Template Setting）
  - `labs/*` 與 `lb00/lb0001`–`lb0016` — Labs 工具（Online Console、WebSocket/Website Proxy、RDB Connection、Mail Template IO、Bot Detection、Webhook、DB Config、SMART Launcher）。注意有些 `lb00/*` 路由是別名，指向與某個 `labs/*` 相同的模組，修改前請先確認 `layout.routing.ts`，不要假設路由與資料夾是一對一。
  - `ac09/:cusfunc` 與 `:cus/:cusfunc` — 給客戶端註冊的自訂報表 / 客製功能使用的通用容器路由（`ac0900`、`za0000` 模組）
  - 每個功能資料夾遵循傳統 Angular 模式：`xxNNNN.module.ts`、`xxNNNN-routing.module.ts`、`xxNNNN.component.ts/.html/.css`，並可能有子元件（例如 `ac0101/role-list/`）。
  - 少數較新畫面（如 `labs/lb0014`、`labs/lb0015`）改用 `loadComponent` 而非 `loadChildren` 註冊，這些是 standalone components，也是 `AGENTS.md` 建議新開發優先採用的模式。
- `layout.routing.ts` 中有不少路由是被註解掉的，請視為已停用 / 淘汰的功能，不要在不了解原因的情況下逕自清除。

### HTTP / API 層

- `src/app/shared/services/api-base.service.ts`（`ApiBaseService`）是所有 `api-*.service.ts` 共用的底層 HTTP client，集中處理：
  - 請求簽章（`SignCode` header，以 SHA-256 對 sign block + JSON body 運算，參見 `cryptSignCode` / `signBlockService`）
  - 從 `TokenService` 取得並附加 Bearer token
  - DigiRunner 特有的封包格式慣例：request 帶 `ReqHeader`/`ReqBody`，response 帶 `ResHeader`/`ResBody`（呼叫 .NET 後端時為小寫的 `resHeader`），並以 `rtnCode`/`rtnMsg` 表示結果。`rtnCode` 不為 `0000`（`1100`、`9914`、`9929` 等為例外）時會跳出全域提示視窗，並呼叫稽核事件記錄 API（`AA0206`）。
  - 針對不同後端 / 行為提供多種 `excute*` method：`excutePost`（預設，錯誤會跳提示）、`excutePost_bg`（靜默不提示）、`excuteDotNetPost`（.NET 後端，小寫 `resHeader`）、`excuteNpPost*`（另一組 "NP" 後端 base URL，有數個忽略特定 rtnCode 的變體）、`excuteDpGet`/`excuteDpPut`/`excuteDpDelete`/`excuteDpUpload`，以及檔案上傳／下載變體（`excuteFileUpload`、`excuteDpGetFile`、`excutePostGetFile`、`excuteDpGetPEMFile`）。
  - 新增 API 呼叫時，請優先重複使用既有的 `excute*` method 與對應領域的 `api-*.service.ts`，避免自行發明新的 HTTP 呼叫模式。
- `TxID`（定義於 `src/app/models/common.enum.ts`）列舉了後端交易代碼（如 `AA0001`–`AA02xx`…），用於組成 `ReqHeader.txID`。Request/response 的 payload 介面放在 `src/app/models/api/<ServiceName>/aaNNNN.interface.ts`，依交易代碼一檔一個，並依後端 service 分資料夾（`ClientService`、`UserService`、`RoleService` 等），各資料夾內有 `index.ts` barrel。
- `TokenInterceptor`（`src/app/shared/Interceptors/token-interceptor.ts`）是全域的 `HttpInterceptor`：處理 401 → refresh token → 重送、依 `9914`/`9929` 回傳碼重新取得 sign block、並在無法復原的認證錯誤時強制登出並導向 `/login`。Request header 中的 `digiRunner` 是一個逃生門，用來跳過此攔截器的回應處理（用於 API 測試流程）。

### 認證 / Session

- Session 狀態存放於 `sessionStorage`（`isLoggedin`、`decode_token` 等），並非使用 cookie 或狀態管理庫。
- `AuthGuard`、`AutoLoginGuard`、`TokenExpiredGuard`，以及特定路由用的 `ac0505.guard.ts` 皆位於 `src/app/shared/guard/`。
- `LogoutService` 集中處理登出 / 導回登入頁的行為，攔截器與各 guard 在認證失敗時都會呼叫它。

### 共用層（Shared）

- `src/app/shared/services/` — 每個後端領域各有一個 `api-*.service.ts`（client、user、role、job、event、mail、security、certificate-authority、ai 等），另有跨領域共用服務：`alert.service.ts`（全域彈窗提示）、`tool.service.ts`（token / 日期 / 字典等工具方法）、`sign-block.service.ts`、`sanitizer.service.ts`、`export.service.ts`。
- `src/app/shared/shared.module.ts` 與 `src/app/shared/primeng.module.ts` 分別彙整共用 directive/pipe/component 與 PrimeNG 模組；各功能 NgModule 應 import 這兩者，而非各自單獨 import PrimeNG 子模組。
- 可重複使用的 UI 元件（清單挑選器、key-value 編輯器、對話框、組織 / 角色選擇器等）直接放在 `src/app/shared/` 下各自的資料夾（例如 `key-value-grid`、`role-list-lov`、`role-mapping-list-lov`、`manager-group-list`、`organization`、`target-site-list`）。

### 多語系

語系 JSON 檔位於 `src/assets/i18n/`（`zh-tw.json`、`zh-cn.json`、`en-us.json`、`ja.json`）。新增或調整畫面文字時，請同步維護四份語系檔的對應 key。

## 專案規範

本儲存庫另有一份 `AGENTS.md`，以下規則是其內容的完整移植。若兩份文件未來有出入，以本檔（`CLAUDE.md`）為準，並回頭更新 `AGENTS.md` 使其一致。

### 通用規則

- **最小化改動：** 保持改動範圍盡可能精簡，專注於解決使用者當下的需求。
- **嚴格限制修改：** 預設嚴禁修改或變動現有程式碼。除非使用者在對話中「主動且明確」要求修改特定代碼，如果只是請你寫個 demo 不等於同意變動任何程式碼，除非說「幫我修改/重構某段 code」否則不得進行任何自動化改動。
- **優先使用既有資源：** 專案中若已有可用的套件、元件、Service 或業務邏輯，必須優先採用。嚴禁重複輪子（Reinvent the wheel）。僅在既有資源完全無法滿足需求時，方可討論開發新元件。
- **拒絕幻想：** 所有的開發與決策必須基於事實與現有代碼庫，禁止憑空幻想不存在的 API、外部函式庫、元件或方法。
- **拒絕無關重構：** 嚴禁重構與目前需求無關的代碼。
- **尊重他人成果：** 嚴禁還原、覆蓋或隨意刪除非由你本人（AI）所編寫的改動與歷史程式碼。
- **遵循既有模式：** 優先使用專案現有的設計模式，避免引入未經討論的新架構或模式。
- **註解規範：** 針對複雜業務邏輯、演算法或新增功能，必須包含清晰、簡潔的程式碼註解。

### 溝通規範

- **語言：** 回覆使用者時請使用繁體中文。
- **技術術語：** 代碼、標識符（Identifiers）、檔名及指令請保持使用英文。
- **依據事實：** 當決策取決於程式碼庫時，請先檢查儲存庫內容，嚴禁憑空猜測。
- **回報格式：** 完成修改後，請使用以下結構化格式簡述成果：

  ```text
  ### 🛠️ 完成的改動
  * [簡述改動內容 1]
  * [簡述改動內容 2]

  ### 📦 受影響的組件 / API
  * `src/app/...`
  ```

### Angular 開發準則

- **組件架構：** 優先使用 Standalone Components。新建立的組件、指令（Directives）與管道（Pipes）應預設為 `standalone: true`。
- **狀態管理：** 優先採用 Angular Signals 進行響應式狀態管理與資料流處理（特別是 `signal()`、`computed()`、`effect()`），除非既有代碼明確使用 RxJS 模式 — 請與所編輯的檔案 / 模組保持一致。
- **依賴注入：** 優先使用 `inject()` 函數進行依賴注入，而非傳統的建構子（Constructor）注入。
- **類型安全：** 嚴格遵守 TypeScript 類型定義，嚴禁濫用 `any` 類型。所有 API 回傳值與元件屬性應有明確的 Interface 或 Type 定義（依循本專案 `models/api/<Service>/aaNNNN.interface.ts` 的既有慣例）。

### 變更與編輯方針

1. **編輯前準備：** 在編輯任何檔案前，先檢查相關文件，並參考鄰近程式碼的在地化慣例與風格。
2. **方案評估：** 優先採取能徹底解決問題、且改動行數最精簡的方案。
3. **工具與編輯方式：** 進行檔案編輯時，優先使用精準的區塊替換工具，避免重寫整份大檔案。
4. **安全性檢查：** 嚴禁將任何 API Key、密碼、Token 或敏感憑證寫入代碼中。環境變數或機密資訊必須使用 `.env` 或 Angular 環境設定檔。
5. **前端驗證：** 改動後必須確保符合 TypeScript 規範，且應用程式能正常編譯（Build Success），不產生 Linter 錯誤。
6. **變更後工作：** 清晰列出被修改的檔案與具體變更要點；特別標註任何潛在的副作用（Side Effects）、破壞性變更（Breaking Changes）、後續需要人工串接的工作，或缺失的單元測試（Unit Tests）。

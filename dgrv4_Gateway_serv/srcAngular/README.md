# digiRunner DGR v4 Angular Console

本目錄是 digiRunner / DGR v4 Gateway 服務的 Angular 前端管理介面。專案負責提供登入、後台版面、API Gateway 管理、系統設定、Labs 工具與 AI Gateway 相關功能，建置後會輸出到後端服務的靜態資源目錄。

## 專案概覽

- Framework: Angular 20
- Language: TypeScript
- UI: Bootstrap、PrimeNG、Angular Material、FontAwesome
- Charts: Chart.js、ECharts
- i18n: `@ngx-translate/core`
- Test runner: Karma / Jasmine
- Default dev server: `http://localhost:4701/`
- Runtime base path: `/dgrv4/ac4/`

## 主要功能區

- 登入與驗證：一般登入、LDAP、IDP SSO、Gateway IDP callback
- 管理後台：Dashboard、Profile、About
- Gateway 管理：API、Client、User、Role、Group Auth、Token、Security、Certificate Authority
- 系統維運：Server、Job、Alert、Event、Mail、Report、Theme、SiteMap
- Labs / 工具：Online Console、WebSocket Proxy、Website Proxy、RDB Connection、Mail Template、Bot Detection、Webhook、DB Config
- AI Gateway：AI Provider、AI API Key、API Key Usage、Prompt Template、User Prompt Template Setting
- 客製化容器：自定義報表與客製功能路由

## 目錄結構

```text
src/
  app/
    layout/              # 主要頁面、功能模組與後台版面
    models/              # 共用 model 與 API request/response interface
    shared/              # 共用元件、guard、interceptor、service、pipe
  assets/
    i18n/                # 多語系 JSON
    images/              # 圖片與功能選單素材
  environments/          # Angular environment 設定
  styles/                # 全域 SCSS、layout 與 UI 客製樣式
```

## 環境需求

請使用與 `package-lock.json` 相容的 Node.js / npm 版本安裝依賴。若本機已有 `node_modules`，仍建議在切換分支或更新套件後重新安裝。

```bash
npm ci
```

## 本機開發

```bash
npm start
```

此指令會執行：

```bash
ng serve --port 4701 --open
```

開發環境 API 端點設定於 `src/environments/environment.ts`。目前預設使用：

```ts
const DEV_HOST = 'https://localhost';
const DEV_PORT = '';
```

如需連接不同後端，請依本機或測試環境調整 `apiUrl`、`dpPath`、`netApiUrl` 等設定。

## 建置

```bash
npm run build
```

此指令會執行 production build：

```bash
ng build --optimization=true
```

根據 `angular.json`，建置輸出位置為：

```text
../src/main/resources/static/dgrv4/ac4/
```

因此本專案不是輸出到 Angular 預設的 `dist/`，而是直接產生給後端服務使用的靜態檔案。`baseHref` 與 `deployUrl` 皆設定為：

```text
/dgrv4/ac4/
```

## 開發模式建置

```bash
npm run watch
```

此指令會使用 development configuration 持續監看並建置：

```bash
ng build --watch --configuration development
```

## 測試

```bash
npm test
```

此指令會執行 Karma / Jasmine 單元測試：

```bash
ng test
```

## 多語系

語系檔位於：

```text
src/assets/i18n/
```

目前包含：

- `zh-tw.json`
- `zh-cn.json`
- `en-us.json`
- `ja.json`

新增或調整畫面文字時，請同步維護對應語系 key。

## 常用指令

| 指令 | 說明 |
| :--- | :--- |
| `npm ci` | 依 `package-lock.json` 安裝依賴 |
| `npm start` | 啟動本機 Angular dev server，port `4701` |
| `npm run build` | 執行 production build 並輸出到後端靜態資源目錄 |
| `npm run watch` | 以 development configuration 監看建置 |
| `npm test` | 執行單元測試 |

## 注意事項

- 此專案與後端部署路徑耦合，調整 `baseHref`、`deployUrl` 或 `outputPath` 前，請同步確認後端靜態資源與 routing 設定。
- `environment.ts` 內含多組歷史環境註解，切換環境時請避免提交不必要的本機設定。
- 專案中仍有大量 NgModule lazy-loading 模組；新增功能時請遵循既有模組結構，若新增 standalone component，需確認路由與 shared dependency 的使用方式。

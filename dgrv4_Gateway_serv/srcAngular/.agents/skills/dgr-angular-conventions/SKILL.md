---
name: dgr-angular-conventions
description: 提供 DGR Gateway Angular 專案的既有元件、API、Dialog、i18n、CSS、測試、build、commit 與上傳慣例。當 Codex 要分析、修改、重構、審查、測試、建置、提交或上傳此 repository 的 Angular/TypeScript/HTML/SCSS 程式碼時使用。
---

# DGR Angular Conventions

處理此專案的程式碼前，先讀取與任務相關的 reference；不得憑空假設未讀取的規範。

- Angular 元件、表單、DI、Signals、PrimeNG 與 RxJS：讀取 [references/angular-component.md](references/angular-component.md)。
- PrimeNG 元件 API（props、events、templates）或 `.mcp.json`／`.codex/config.toml` MCP 設定：讀取 [references/primeng-mcp.md](references/primeng-mcp.md)。
- HTTP API、request/response model 與 service：讀取 [references/api-pattern.md](references/api-pattern.md)。
- Dialog、confirm 或 modal：讀取 [references/dialog-modal.md](references/dialog-modal.md)。
- i18n key 或語系檔：讀取 [references/i18n.md](references/i18n.md)。
- CSS、SCSS、Bootstrap 或 PrimeNG 樣式：讀取 [references/style.md](references/style.md)。
- 單元測試：讀取 [references/unit-test.md](references/unit-test.md)。
- Build、build 產物、commit、push、branch、PR/MR 或 CI：讀取 [references/commit-pr.md](references/commit-pr.md)。

若任務跨越多個領域，讀取所有相關 reference。實作前仍須檢查目標檔案及鄰近程式碼，以 repository 現況為準；規範與現況衝突時，先向使用者說明查證結果。

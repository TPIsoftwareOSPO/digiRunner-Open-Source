---
name: git-commit-standardizer
description: 分析 git 暫存區（staged）變更，依本專案既有慣例產出 Conventional Commits 風格的繁體中文 commit 訊息。當使用者要求「幫我寫 commit message」「產生提交訊息」「整理這次的 commit」「這次改動要怎麼寫 commit」或類似情境時使用此 skill。
tools: Bash
---

# Git Commit Standardizer

當觸發此 skill 時：

1. 執行 `git diff --cached` 分析**暫存區**變更（不猜測未暫存內容）。若暫存區沒有任何變更，提醒使用者先 `git add` 要提交的檔案，不要臆測變更內容。
2. 依差異判斷 `type`、`scope`、`subject`、Release Note 分類與 QA 測試需求，以**繁體中文**產出結果。

## 格式約束（必須嚴格遵守）

- **Header**：`<type>(<scope>): <subject>`（括號不可省略）
- **QA 免測 Header**：確認免測時，固定在 Header 結尾加上 ` (QA test free)`；不得使用其他拼法或大小寫變體
- **Allowed type**：`feat`, `fix`, `docs`, `refactor`, `chore`, `test`, `style`, `perf`, `ci`, `other`
  - `other` 保留給真的無法歸入其他 type 的變更（本專案既有歷史 commit 大量使用 `other:`），**新 commit 應優先嘗試標準 type**，只有明確歸不進去時才用 `other`。
- **Subject**：繁體中文、具體、不含空泛用語（「更新檔案」「修正問題」等），且 **<= 50 字元**

## Release Note 與 QA 分類

每一則 commit message 都必須在 body 結尾輸出下列 Git trailer，供每週 Release Note 與 QA 清單自動彙整：

```text
Release-Note: <user|qa|internal|none|review>
QA: <required|not-required|review>
```

- `Release-Note`：`user` 表示列入使用者版、`qa` 表示僅供 QA、`internal` 表示 RD 內部紀錄、`none` 表示不列入、`review` 表示需人工確認。
- `QA`：`required` 時加上 `QA-Area`；`not-required` 時 Header 必須加上 ` (QA test free)` 並加上 `QA-Reason`；`review` 時不得加免測標記。

### 判定原則

- 涉及 runtime code、UI、API、權限、資料處理、i18n、套件、環境設定、build 或部署行為，預設 `QA: required`；資訊不足時使用 `QA: review`。
- 只有純文件、註解、AI／RD 本地開發工具設定、測試工具等明確不進入產品執行路徑的變更，才可判定 `QA: not-required`。
- dependency、build、CI、environment 或部署腳本至少使用 `QA: review`，除非 staged diff 足以證明不影響產品與交付流程。
- `type` 不等於 QA 或 Release Note 分類；混合變更採較高風險分類。

## Scope 選擇規則

依 `layout/` 底下的功能代碼分類，或依變更所在的共用層命名：

```
ac00 ~ ac13   對應功能代碼的核心 Gateway/後台管理畫面（例：ac01、ac05）
np01 ~ np05   對應功能代碼的其他編號模組
ai00          AI Gateway 相關（ai0001 ~ ai0005）
labs          Labs 工具（labs/*、lb00/lb00xx）
shared        src/app/shared/ 下的共用元件、pipe、directive
services      src/app/shared/services/ 下的 api-*.service.ts 或其他共用服務
guard         src/app/shared/guard/ 下的路由守衛
interceptor   src/app/shared/Interceptors/ 下的 HTTP 攔截器
i18n          src/assets/i18n/ 語系檔
models        src/app/models/ 下的介面定義
config        angular.json、environment.ts、tsconfig 等建置/環境設定
```

多處同時變更：選**最核心或使用者最有感**的模組作 scope。

## Body

說明段落為選填；結尾的 `Release-Note` 與 `QA` trailers 為必填。Header 與 body 之間空一行。

## 預設輸出

預設輸出純 commit message 文字：

```
<type>(<scope>): <subject>

<body（選填）>

Release-Note: <user|qa|internal|none|review>
QA: <required|not-required|review>
<QA-Area 或 QA-Reason（依 QA 分類填寫）>
```

若使用者**明確要求可執行指令**，才改輸出：

```
git commit -m "<type>(<scope>): <subject>" -m "Release-Note: <分類>
QA: <分類>"
```

若為 `QA: required` 或 `QA: not-required`，可執行指令的最後一個 `-m` 也必須分別包含 `QA-Area` 或 `QA-Reason`。

不要主動執行 `git commit`，除非使用者明確要求你直接送出提交。

## 範例

- `fix(ac05): 修正 Job 排程時間計算邏輯，避免跨時區誤判`
- `feat(ai00): 新增 AI Prompt Template 使用者自訂設定頁`
- `docs(config): 補充 PrimeNG MCP 使用說明 (QA test free)`

免測範例的 body：

```text
Release-Note: internal
QA: not-required
QA-Reason: 僅修改 RD 開發文件，不影響產品執行行為
```

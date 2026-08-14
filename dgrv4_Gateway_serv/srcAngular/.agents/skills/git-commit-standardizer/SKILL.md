---
name: git-commit-standardizer
description: 分析 git 暫存區（staged）變更，依本專案既有慣例產出 Conventional Commits 風格的繁體中文 commit 訊息。當使用者要求撰寫 commit message、產生提交訊息、整理本次 commit 或詢問改動應如何提交時使用。
---

# Git Commit Standardizer

當觸發此 skill 時：

1. 使用 shell 執行 `git diff --cached` 分析**暫存區**變更（不猜測未暫存內容）。若暫存區沒有任何變更，提醒使用者先 `git add` 要提交的檔案，不要臆測變更內容。
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

### `Release-Note` 允許值

- `user`：使用者可感知的功能、修正或效能改善，列入使用者版 Release Note。
- `qa`：不一定對外公告，但 QA 需要知道的產品或測試影響。
- `internal`：RD、建置、CI、文件或開發工具調整，僅列入內部紀錄。
- `none`：純機械性且無須列入 Release Note 的變更。
- `review`：無法從 staged diff 安全判斷，需人工確認。

### `QA` 允許值

- `required`：需要 QA 測試；應加上 `QA-Area: <受影響功能與建議驗證範圍>`。
- `not-required`：QA 免測；Header 必須加上 ` (QA test free)`，且必須加上 `QA-Reason: <免測理由>`。
- `review`：無法安全判斷，需由 QA 或 RD 人工確認；Header 不得加上 `(QA test free)`。

### 判定原則

- 只要涉及 runtime code、UI、API、權限、資料處理、i18n、套件、環境設定、build 或部署行為，預設 `QA: required`；資訊不足時使用 `QA: review`。
- `refactor`、`chore`、`docs` 等 type 不等於 QA 免測，必須依 staged diff 的實際影響判斷。
- 只有純文件、註解、AI／RD 本地開發工具設定、測試工具等明確不進入產品執行路徑的變更，才可判定 `QA: not-required`。
- dependency、build、CI、environment 或部署腳本即使屬內部調整，至少使用 `QA: review`，除非 staged diff 足以證明不影響產品與交付流程。
- 混合變更採較高風險分類；不得因部分內容屬 RD 調整，就將整筆 commit 標成免測。
- `Release-Note` 與 `QA` 是兩個獨立判斷，不得只依 Conventional Commit type 相互推定。

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

說明段落仍為選填，只有非直覺或較複雜的變更才加入，簡短說明「為何」與「採用的方法」。Header 與 body 之間空一行；結尾的 `Release-Note` 與 `QA` trailers 為必填。

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
- `refactor(services): 統一 api-base.service.ts 的 rtnCode 錯誤處理`
- `docs(shared): 補充 dialog.component.ts 的動態掛載說明`
- `chore(config): 調整 angular.json 的 production budget 上限`

QA 免測範例：

```text
docs(config): 補充 PrimeNG MCP 使用說明 (QA test free)

Release-Note: internal
QA: not-required
QA-Reason: 僅修改 RD 開發文件，不影響產品執行行為
```

需要 QA 測試範例：

```text
fix(ac05): 修正 Job 排程跨時區時間誤判

Release-Note: user
QA: required
QA-Area: Job 排程時間顯示與跨時區執行結果
```

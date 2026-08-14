# Commit 與分支規範

> 本文件內容依實際 `git log` / 分支 / CI 設定調查得出，如實反映現況，**不是**理想化的團隊規範。若之後團隊決議收緊規則，請同步更新本文件。

## Commit 訊息格式

以**繁體中文**撰寫主旨（type 與 scope 保持英文）：

```
<type>(<scope>): <繁體中文主旨>

[選填] 詳細說明（繁體中文）

Release-Note: <user|qa|internal|none|review>
QA: <required|not-required|review>
```

確認 QA 免測時，Header 結尾必須加上固定標記 ` (QA test free)`，並在 body 加上 `QA-Reason`：

```text
docs(config): 補充 RD 開發工具說明 (QA test free)

Release-Note: internal
QA: not-required
QA-Reason: 僅修改 RD 開發文件，不影響產品執行行為
```

`Release-Note` 與 `QA` 的允許值、判定原則及每週彙整用途，詳見 `.agents/skills/git-commit-standardizer/SKILL.md`。不得只依 `type` 判定免測；涉及產品執行行為或無法確認影響時，使用 `QA: required` 或 `QA: review`。

### Type 說明

實際 `git log` 中觀察到的 type：`feat:`、`fix:`、`docs:`，以及大量使用、**非標準 Conventional Commits** 的 `other:`（幾乎是萬用分類，例：「other: 修改腳本」「other:升級spring boot」）。

建議往下列標準 type 收斂，`other` 僅在真的無法歸類時使用：

| Type | 用途 |
|------|------|
| `feat` | 新增功能 |
| `fix` | 修正 Bug |
| `refactor` | 重構（不影響功能） |
| `chore` | 建置、設定、套件更新 |
| `docs` | 文件更新 |
| `style` | 樣式調整（不影響邏輯） |
| `test` | 新增或修改測試 |
| `perf` | 效能改善 |
| `other` | 上述都不適用時的最後選項（既有歷史慣例，非推薦首選） |

### Scope 參照

依 `layout/` 功能代碼與共用層分類（詳見 `.agents/skills/git-commit-standardizer/SKILL.md`），例如 `ac01`、`ac05`、`ai00`、`labs`、`shared`、`services`、`guard`、`interceptor`、`i18n`、`models`、`config`。

### 範例

```
feat(ai00): 新增 AI Prompt Template 使用者自訂設定頁

fix(ac05): 修正 Job 排程時間計算邏輯，避免跨時區誤判

docs: 補充 CLAUDE.md 架構說明

other: 調整部署腳本
```

## 分支策略

實際觀察到的分支：

- `develop`、`main` 為主要長期分支
- 大量 `feature/<個人或功能名稱>/*` 分支（例：`feature/chad`、`feature/tom/*`、`feature/fe_chad/upgrade@20`），**命名沒有嚴格統一格式**，多依開發者個人習慣命名，不強制規範
- 遠端有多個 remote（`origin`、`gitlab`、`eapdd2025`），推送前請確認目標 remote 正確

**Feature Branch 命名建議**（非強制，供新分支參考）：
```
feature/<負責人>/<簡短說明>
fix/<簡短說明>
```

## CI / 部署

- `.gitlab-ci.yml`（位於 monorepo 根目錄）：手動觸發的 GitLab CI pipeline，透過 `GOAL` 參數控制行為（如 `DEPLOY_TO_88`、`MAKE_PACKAGE`、`MAKE_RELEASE`），stages 為 `tooling`、`test`、`build`、`deploy`、`package`、`clean`。沒有觀察到自動的 branch-based 觸發條件。
- `dgrv4_Root/dgrv4_Gateway_serv/Jenkinsfile`：另一套 Jenkins CI，與 GitLab CI 並行存在。

## Angular Build 與上傳規範

`srcAngular/angular.json` 的 `outputPath` 指向後端 tracked 部署目錄：

```text
../src/main/resources/static/dgrv4/ac4/
```

依使用者指示判斷是否提交 build 產物：

- 使用者只要求執行 `npm run build`、確認 build、驗證編譯或回報 build 結果：將 build 視為驗證；完成後清理由本次 build 造成的 static 目錄差異，不提交 build 產物。
- 使用者明確要求「`npm run build` 且上傳」、build 後上傳、建置並推送或同等語意：將 build 輸出視為交付內容；build 成功後，必須把 `src/main/resources/static/dgrv4/ac4/` 中更新的 `index.html`、新 hash bundles 與被取代的舊 bundles 一併加入同次提交／上傳流程。
- 不得因 build 產物位於 static 或含 hash 檔名，就在第二種情境自行清理或排除。
- Build 失敗時不得提交或上傳失敗／不完整的 build 產物；先回報錯誤並處理失敗原因。
- 暫存前檢查變更範圍，避免把其他使用者或先前工作留下的 static 差異誤納入本次提交。

## 目前沒有的規範（避免虛構）

- **沒有** `.gitlab/merge_request_templates/` 或任何 PR/MR 樣板檔案。
- **沒有** `CONTRIBUTING.md`。
- 若使用者要求產出 PR 描述，可參考下列最小結構，但這不是專案既有強制格式：

  ```markdown
  ## 變更原因
  ## 主要修改
  - `src/app/...` — 說明
  ## 測試方式
  ```

## 禁止事項（一般性最佳實踐，非本專案特有文件記載，但屬合理預設）

- 禁止 force push 到 `develop`、`main`
- 禁止在 commit 中包含 `.env`、憑證、私鑰等敏感資訊
- 禁止提交 `node_modules/`、`dist/` 等一般大型或建置產物目錄；但使用者明確要求「build 且上傳」時，依「Angular Build 與上傳規範」提交專案既定的 `src/main/resources/static/dgrv4/ac4/` 交付產物

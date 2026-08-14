# 建立 Codex Build 與上傳產物規範

本次工作延續 [Email 輸入驗證工作](./2026-07-16_email-input-validation.md)，將該次交付過程確認的 build 產物規則正式寫入 `.agents`。

## 修改內容

### 問題描述

使用者在 Email 驗證工作完成並上傳後，指出先前「Build 產物已清理，未納入提交」的處理不符合需求，並明確定義：

1. 使用者若說明要執行 `npm run build` 且上傳，build 出來的內容必須併入該次上傳。
2. 將這項規範建立到 `.agents`，供未來 Codex 執行 build／push 工作時遵循。
3. 建立本次規範新增工作的日誌並上傳。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `.agents/skills/dgr-angular-conventions/SKILL.md` | 擴充 skill 觸發描述與 reference 導覽，使 build、commit、push 任務會載入相關規範 |
| `.agents/skills/dgr-angular-conventions/references/commit-pr.md` | 新增 Angular Build 與上傳規範，並修正禁止提交建置產物的一般預設 |
| `doc/specs/2026-07-16_agent-build-upload-artifact-rule.md` | 新增本次規範建立工作日誌 |

### 詳細程式碼異動

#### 1. 擴充 skill 觸發範圍

修改前：

```yaml
description: 提供 DGR Gateway Angular 專案的既有元件、API、Dialog、i18n、CSS、commit 與單元測試慣例。當 Codex 要分析、修改、重構、審查或測試此 repository 的 Angular/TypeScript/HTML/SCSS 程式碼時使用。
```

修改後把 build、提交及上傳情境納入觸發描述：

```yaml
description: 提供 DGR Gateway Angular 專案的既有元件、API、Dialog、i18n、CSS、測試、build、commit 與上傳慣例。當 Codex 要分析、修改、重構、審查、測試、建置、提交或上傳此 repository 的 Angular/TypeScript/HTML/SCSS 程式碼時使用。
```

SKILL 導覽同步將 `commit-pr.md` 的讀取情境改為：

```markdown
- Build、build 產物、commit、push、branch、PR/MR 或 CI：讀取 references/commit-pr.md。
```

#### 2. 明確區分純驗證與交付型 build

在 `commit-pr.md` 新增「Angular Build 與上傳規範」：

- 只要求 build／驗證編譯：build 視為驗證，完成後清理由本次 build 造成的 static 差異。
- 明確要求「build 且上傳」或同等語意：build 輸出視為交付內容，必須提交 `src/main/resources/static/dgrv4/ac4/` 的 `index.html`、新 hash bundles 與被取代的舊 bundles。
- Build 失敗時禁止提交或上傳失敗／不完整產物。
- 暫存前確認 static 差異確實由本次工作產生。

#### 3. 修正既有禁止事項的衝突

修改前：

```markdown
- 禁止提交 `node_modules/`、`dist/` 等大型或建置產物目錄
```

修改後保留一般預設，但加入本專案交付目錄的明確例外：

```markdown
- 禁止提交 `node_modules/`、`dist/` 等一般大型或建置產物目錄；但使用者明確要求「build 且上傳」時，依「Angular Build 與上傳規範」提交專案既定的 `src/main/resources/static/dgrv4/ac4/` 交付產物
```

## 預計修改方式

### 需求 1：建立 build 且上傳時包含產物的規範

**Context** — Email 驗證工作中，使用者要求建立工作日誌、在 `npm run build` 成功後上傳。當時將 build 理解為純驗證並清除產物；使用者後續明確指出，只要同時要求 build 與上傳，就應把 build 內容併入該次交付。依更正重新 build、補交產物後，使用者要求把規則寫入 `.agents`。

**分析** — 讀取 `dgr-angular-conventions` skill 與 `commit-pr.md`，確認 build／push 規範應放在 commit/CI reference。`commit-pr.md` 原本另有「禁止提交建置產物」的一般性條款，若只新增規則而不調整原條款，兩者會衝突。另依 `skill-creator` 指引，觸發情境必須寫入 frontmatter description；若不把 build／上傳放入描述，未來任務可能不會載入這項規範。

**修正方案** — 在 `commit-pr.md` 新增具體判斷規則及專案 outputPath，並在禁止事項加入例外；同步擴充 skill description 與導覽，確保建置、提交、上傳任務會讀取此 reference。

### 需求 2：建立日誌並上傳

**Context** — 規範完成後，使用者要求建立工作日誌並上傳。

**分析** — 使用 `Get-Date -Format yyyy-MM-dd` 確認日期為 `2026-07-16`，用 `rg --files doc/specs` 確認沒有同主題檔案。Git 顯示 `develop` 與 `eapdd2025/develop` 同步；工作樹另有 `.claude/settings.local.json` 未追蹤檔案，屬使用者本機設定，不能納入。

**修正方案** — 建立本文件，只 stage `.agents` 兩份規範與本文件，通過 `diff --check` 後提交並推送至 `eapdd2025/develop`。

## 驗證方式

### 靜態驗證

- 驗證 `SKILL.md` frontmatter 仍符合 `name`／`description` 格式。
- 驗證 skill 導覽包含 build routing。
- 驗證 `commit-pr.md` 包含：規範章節、`npm run build`、實際 static 交付路徑及 build 失敗處理。
- 驗證結果：`VALIDATION_OK`。
- `git diff --check -- .agents`：無 whitespace error。
- 本次只修改 Markdown skill／規範文件，不涉及 Angular 原始碼，因此未執行 `npm run build`。

### 尚待人工驗證（建議）

- 在新的 Codex session 中分別提出「幫我跑 build 確認」與「幫我 build 並上傳」，確認前者不提交產物、後者會提交 `static/dgrv4/ac4/` 產物。

## 實作紀錄

### 時間軸

1. Email 驗證工作完成後，原本將 `npm run build` 視為純驗證並清理 static 產物。
2. 使用者更正語意：明確要求 `npm run build` 且上傳時，build 內容必須併入該次上傳。
3. 依更正重新 build，將 static 產物以補充 commit 推送。
4. 使用者要求將此判斷建立到 `.agents` 規範。
5. 讀取 `skill-creator`、`dgr-angular-conventions/SKILL.md` 與 `commit-pr.md`，發現既有「禁止提交建置產物」條款會與新規則衝突。
6. 擴充 skill description／導覽，在 `commit-pr.md` 新增 build 與上傳規範，並為既有禁止條款加入專案 static 交付例外。
7. 執行 frontmatter、routing、必要規則文字及 whitespace 驗證，結果為 `VALIDATION_OK`。
8. 使用者要求建立本次工作日誌並上傳；建立本文件。文件撰寫當下尚未 commit/push，最終結果以本次對話回報為準。

### 經驗紀錄（給未來類似任務參考）

- 「執行 build」可能是驗證，也可能是交付步驟；是否提交產物應以使用者是否同時要求上傳判斷。
- 新規範若與既有一般條款衝突，不能只追加文字，必須同步建立明確例外與優先順序。
- Skill 的詳細規則放在 references 還不夠；description 與導覽必須涵蓋觸發情境，未來 Codex 才會讀取該 reference。
- 本專案的 Angular outputPath 是 tracked 後端 static 目錄，不等同一般不應提交的 `dist/`。

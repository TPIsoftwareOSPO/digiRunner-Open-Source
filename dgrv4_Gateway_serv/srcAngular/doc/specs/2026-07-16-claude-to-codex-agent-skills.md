# 將 Claude 專案指令與規範轉換為 Codex Skills

## 修改內容

### 問題描述

使用者在本次對話中依序提出以下需求：

1. 將 `.claude` 內可供 AI Agent 使用的內容轉換成 Codex 可使用的格式，放入 `.agents`。
2. 整理本次新增工作的完整紀錄，在 `doc/specs/` 建立文件並上傳。

### 修改檔案清單

| 檔案 | 異動內容 |
|------|----------|
| `.agents/skills/spec-recorder/SKILL.md` | 將 Claude 的 `spec-recorder` command 轉為 Codex skill，調整 frontmatter、工具描述與附加參數語意 |
| `.agents/skills/spec-recorder/agents/openai.yaml` | 新增 skill 的 Codex UI 顯示名稱、簡介與預設 prompt |
| `.agents/skills/git-commit-standardizer/SKILL.md` | 將既有 Claude skill 整理為 Codex 可觸發的 commit message skill |
| `.agents/skills/git-commit-standardizer/agents/openai.yaml` | 新增 skill 的 Codex UI metadata |
| `.agents/skills/dgr-angular-conventions/SKILL.md` | 新增專案慣例導覽 skill，依任務類型要求按需讀取 reference |
| `.agents/skills/dgr-angular-conventions/agents/openai.yaml` | 新增專案慣例 skill 的 Codex UI metadata |
| `.agents/skills/dgr-angular-conventions/references/*.md` | 搬移並調整 Angular component、API、commit/PR、Dialog、i18n、style 與 unit test 共 7 份規範 |
| `doc/specs/2026-07-16-claude-to-codex-agent-skills.md` | 新增本次轉換工作的完整紀錄 |

`.claude/settings.json` 與 `.claude/settings.local.json` 未搬移。兩者分別是 Claude plugin 與本機工具權限設定，Codex 不使用相同的設定格式；直接複製不會產生等價效果，且 `settings.local.json` 屬本機設定。

### 詳細程式碼異動

#### 1. 將 Claude command 轉為 Codex skill

修改前的 Claude command 只提供 `description`：

```yaml
---
description: 根據本次對話的需求與實作過程，在 doc/specs/ 產出一份工作紀錄檔（spec）
---
```

修改後補上 Codex skill 必要的 `name` 與可涵蓋觸發情境的 `description`：

```yaml
---
name: spec-recorder
description: 根據本次對話的需求、調查、實作與驗證，在 doc/specs/ 產出如實工作紀錄。當使用者要求記錄 spec、整理工作紀錄、產出實作紀錄或呼叫 spec recorder 時使用。
---
```

原本 Claude command 使用的 `$ARGUMENTS` 改為「使用者同時提供的附加說明」，檔案搜尋工具名稱 `Glob` 改為專案要求的 `rg --files`。

#### 2. 整理 Git commit skill

沿用 `.claude/skills/git-commit-standardizer/SKILL.md` 的專案 type、scope 與繁體中文 subject 規則，但移除 Claude 專用的：

```yaml
tools: Bash
```

Codex 版本只保留 `name` 與 `description` frontmatter，並明確要求使用 shell 執行 `git diff --cached`，且未經使用者明確要求不得直接執行 `git commit`。

#### 3. 將專案規範改為 progressive disclosure

沒有把 7 份長規範全部塞入單一 `SKILL.md`，而是建立精簡導覽：

```markdown
- HTTP API、request/response model 與 service：讀取 references/api-pattern.md。
- Dialog、confirm 或 modal：讀取 references/dialog-modal.md。
- i18n key 或語系檔：讀取 references/i18n.md。
```

詳細規範保留在 `references/`，Codex 僅在任務涉及該領域時讀取，避免每次載入不相關內容。所有 reference 內的 `.claude/RULES/` 與 Claude command 路徑同步改為 `.agents/skills/` 對應位置。

#### 4. 新增 Codex UI metadata

三個 skill 均新增 `agents/openai.yaml`，例如：

```yaml
interface:
  display_name: "DGR Angular Conventions"
  short_description: "套用本專案 Angular、API、樣式與測試規範"
  default_prompt: "Use $dgr-angular-conventions when changing this Angular project."
```

## 預計修改方式

### 需求 1：將 `.claude` 內容轉為 Codex 可使用的 `.agents`

**Context** — 使用者提供 repository 的 `AGENTS.md` 規則，並明確要求修改 `.agents`，將 `.claude` 的內容轉成 Codex 可使用格式。

**分析** — 使用 `rg --files -uu .claude .agents` 盤點後，確認 `.claude` 共有 2 個 command、1 個 skill、7 份規範及 2 份 settings。完整讀取內容後，判斷 commands 適合轉為 skills，專案規範適合集中為一個導覽 skill 並拆成 references；Claude 的 plugin/permission settings 沒有可直接等價搬移的 Codex project skill 格式。另依 `skill-creator` 指引確認 Codex skill 必須具有 `SKILL.md`、僅含 `name`/`description` 的 YAML frontmatter，並建議提供 `agents/openai.yaml` 與 progressive disclosure。

**修正方案** — 建立 `spec-recorder`、`git-commit-standardizer`、`dgr-angular-conventions` 三個 skills；前兩者承接可執行工作流程，後者負責依任務導向 7 份 references。保留原規範語意，只替換 Claude 專用的欄位、工具名稱與內部路徑。

### 需求 2：建立工作紀錄並上傳

**Context** — 完成 `.agents` 轉換後，使用者要求整理本次新增內容，在 `doc/specs/` 建立文件並上傳。

**分析** — 讀取 `$spec-recorder` 完整指引，使用 `Get-Date -Format yyyy-MM-dd` 確認日期為 `2026-07-16`，並用 `rg --files doc/specs` 確認目標檔名未重複。Git 檢查顯示目前分支為 `develop`、remote 為 `eapdd2025`；工作樹另有 `.claude/settings.local.json` 與 `.codex/` 未追蹤內容，均不是本次轉換所新增，必須排除。

**修正方案** — 建立本文件，只 stage `.agents` 與本文件，檢查 staged diff 後建立繁體中文 Conventional Commit，最後推送至 `eapdd2025/develop`。上傳結果以本次對話最終回報的 commit 與 push 輸出為準。

## 驗證方式

### 靜態驗證

- 使用自訂 PowerShell 檢查所有 skill 目錄均有 `SKILL.md`。
- 驗證 skill 目錄名稱符合 `^[a-z0-9-]{1,64}$`。
- 驗證 `SKILL.md` frontmatter 具有合法的 `name` 與 `description`。
- 逐份比對 `.claude/RULES/*.md` 與 `.agents/.../references/*.md`，除預期的內部路徑替換外內容一致。
- 搜尋 `.agents/skills/**/*.md`，確認沒有殘留 `.claude/`、`$ARGUMENTS` 或 `tools:` Claude 專用語法。
- 上述結構檢查結果為 `VALIDATION_OK`，exit code 0。
- 本次只新增 Markdown/YAML Agent 文件，沒有異動 Angular、TypeScript、HTML 或 SCSS，因此未執行 Angular build。

### 尚待人工驗證（建議）

- 在新的 Codex session 中確認三個 project skills 能被偵測。
- 分別以「整理本次 spec」、「幫我寫 commit message」及 Angular/API 修改需求測試 skill 是否正確觸發並按需載入 references。
- `skill-creator` 官方 `quick_validate.py` 未能執行，原因是環境的 WindowsApps `python.exe` 無法啟動；本次改用等價的 PowerShell 結構檢查。

## 實作紀錄

### 時間軸

1. 使用者要求把 `.claude` 內容轉換到 `.agents`，供 Codex 使用。
2. 使用 `rg --files -uu` 盤點 `.claude` 與空的 `.agents`，確認來源包含 commands、rules、skill 與 settings。
3. 逐份讀取來源文件；第一次批次輸出因內容超過工具輸出上限而截斷，之後改用檔案清單與逐檔處理，避免依截斷內容做判斷。
4. 因工作內容屬於建立 Codex skills，讀取 `skill-creator/SKILL.md` 與 `references/openai_yaml.md`，確認格式與 progressive disclosure 原則。
5. 嘗試執行 `init_skill.py` 初始化三個 skills，但環境中的 WindowsApps `python.exe` 無法啟動，命令失敗且未留下預期檔案。
6. 改用 `apply_patch` 建立三個 skill 結構、7 份 references 與三份 `agents/openai.yaml`。
7. 第一次批次轉換 references 時，資料處理發生截斷，造成 `api-pattern.md` 不完整、`commit-pr.md` 遺漏，並出現一個非預期的 `source-command-git-commit-standardizer` skill。
8. 驗證發現上述問題後嘗試修正，但 `.agents` 後續寫入被 sandbox 拒絕；取得使用者核准的 `.agents` 寫入權限後，重新從原始 `.claude/RULES` 複製完整內容、替換路徑並移除多餘 skill。
9. 執行 frontmatter、命名、reference 完整性與 Claude 語法殘留檢查，結果為 `VALIDATION_OK`。
10. 使用者要求建立本次工作文件並上傳；讀取 `$spec-recorder`、確認日期、既有 spec、Git branch、remote 與未追蹤檔案範圍。
11. 建立本文件；文件撰寫當下尚未執行 commit/push，後續提交與推送結果以本次對話最終回報為準。

### 經驗紀錄（給未來類似任務參考）

- Claude command 與 Codex skill 的 frontmatter 不相同：Codex skill 必須提供 `name` 與具體觸發情境的 `description`，Claude 的 `tools: Bash`、`$ARGUMENTS` 與權限白名單不可直接照搬。
- 大型專案規範應使用一個精簡導覽 `SKILL.md` 搭配 `references/`，依任務按需讀取，避免把所有領域規範永久載入 context。
- 工具輸出出現 truncated 提示時，不能把截斷內容當成完整來源；應改為逐檔讀取或直接以來源檔案做精確複製與比對。
- 初始化或驗證腳本無法執行時，需如實記錄原因，並建立涵蓋相同核心條件的替代驗證，不可宣稱官方腳本已通過。
- Git stage 應使用明確路徑，本次只應包含 `.agents` 與對應 spec，避免把使用者既存的 `.claude/settings.local.json`、`.codex/` 一併提交。

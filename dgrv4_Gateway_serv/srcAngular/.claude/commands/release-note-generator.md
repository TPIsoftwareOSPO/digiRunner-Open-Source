---
description: 依 commit messages 產出內部版與外部客戶版 Release Note
---

# Release Note Generator

依 `.claude/skills/release-note-generator/SKILL.md` 的完整規則產生 Release Note，只執行唯讀 Git 操作。

## 輸入範圍

- 將 `$ARGUMENTS` 視為使用者指定的日期、branch、tag 或 revision range。
- 若 `$ARGUMENTS` 為空，使用目前 branch 自本週一 `00:00:00` 至現在的 commits，時區採 `Asia/Taipei`。
- 開始前先顯示實際採用的期間與來源。

## 執行要求

1. 排除 merge commits 並讀取完整 commit message，不得只使用 `--oneline`。
2. 解析 `Release-Note`、`QA`、`QA-Area`、`QA-Reason`，以及 skill 定義的人工免測標記（包含不含 `QA` 的 `test free` 等變體）。
3. 依 skill 規則輸出：
   - 外部客戶版：只收錄 `Release-Note: user`。
   - 內部版：包含使用者可見、QA 必測、QA 免測、QA 免測（待補 metadata）、RD 內部調整與待確認項目。
4. 外部版與內部版都保留 `<short-hash> <原始 commit Header>`。
5. 人工免測標記缺少 trailers 時列入「QA 免測（待補 metadata）」；與 `QA: required` 衝突時改列待確認。其餘缺少 trailers 或 metadata 衝突的項目只列入內部版待確認，不得自行推測或放入外部版。
6. 最後只在必要時列出資料品質提醒。

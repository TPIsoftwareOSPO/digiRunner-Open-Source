---
name: release-note-generator
description: 依日期、每週或 Git tag／revision 範圍讀取 commit messages，產出繁體中文內部版與外部客戶版 Release Note。當使用者要求整理 Release Note、QA 測試摘要、QA 免測清單或客戶更新說明時使用。
---

# Release Note Generator

以 commit message 為主要資料來源，只執行唯讀 Git 操作。

## 流程

1. 使用使用者指定的日期、branch、tag 或 revision range；未指定時，採目前 branch 本週一 `00:00:00` 至現在（`Asia/Taipei`）。
2. 排除 merge commits，讀取完整 message，不能只讀 `--oneline`：

```powershell
git log --no-merges --since="<start>" --until="<end>" --date=iso-strict --format="%H%x1f%ad%x1f%an%x1f%B%x1e"
```

Tag／revision 範圍改用 `<from>..<to>`。

3. 解析 `Release-Note`、`QA`、`QA-Area`、`QA-Reason`：
   - `Release-Note: user`：列入外部版與內部版。
   - `qa`、`internal`：只列入內部版；`none` 排除。
   - `review` 或缺少欄位：列入內部版「待確認」，不得放入外部版。
   - `QA: required`：列入「QA 必測」並顯示 `QA-Area`。
   - `QA: not-required`：列入「QA 免測」，保留 `(QA test free)` 與 `QA-Reason`。
   - `QA: review` 或缺少欄位：列入「QA 待確認」。
4. 若 `(QA test free)`、`QA: not-required`、`QA-Reason` 三者不一致，標示 metadata 衝突，不得自行修正。
5. 另以不分大小寫方式搜尋整則 commit message 的人工免測標記：`QA免測`、`QA 免測`、`QA test free`、`QA-test-free`、`test free`、`test-free`、`testfree`、`免測`。
   - 有人工免測標記但缺少正式 trailers 時，列入「QA 免測（待補 metadata）」，並顯示命中的原始標記。
   - 若同時存在 `QA: required`，視為 metadata 衝突並列入待確認，不得判定免測。
   - `非免測`、`不是免測`、`not test free`、`no test free` 等否定語意不得視為人工免測標記。
   - 人工免測標記只影響 QA 分類；不得據此推定 `Release-Note` 分類或放入外部版。

## 整理規則

- 以 commit subject 為主要內容，不得杜撰 message 未提及的功能或測試方式。
- 外部版與內部版都使用 `<short-hash> <原始 commit Header>`，不得移除 type、scope、QA 標記或改寫 subject。
- 內部版另加 QA 分類及 reason／area，方便追溯與測試。
- 外部版依「新增功能、問題修正、效能改善、其他更新」分組。
- 內部版依「使用者可見變更、QA 必測、QA 免測、QA 免測（待補 metadata）、RD 內部調整、待確認」分組。
- 可合併明確屬於同一功能的 commits，並保留所有來源 hash；不確定時不要合併。
- 空白分類不顯示；無項目時輸出「本區間無可發布項目」。

## 輸出

先註明期間、來源與筆數，再輸出：

```markdown
# Release Note（外部客戶版）
## 新增功能／問題修正／效能改善／其他更新
- `<short-hash>` `<原始 commit Header>`

# Release Note（內部版）
## 使用者可見變更／QA 必測／QA 免測／QA 免測（待補 metadata）／RD 內部調整／待確認
- `<short-hash>` `<原始 Header>`
  - 測試範圍或免測原因：<QA-Area 或 QA-Reason>
```

最後只在必要時列出缺少 trailers 或 metadata 衝突等資料品質提醒。

---
description: 產生 DGR Alpha 測試 License，可指定年度或完整到期日
---

# Generate Alpha License

依 `.claude/skills/generate-alpha-license/SKILL.md` 的完整規則產生 DGR Alpha 測試 License。

將 `$ARGUMENTS` 解讀如下：

- 空白：使用執行當年度的 `12/31`。
- 四位數年度，例如 `2027`：使用該年度的 `12/31`。
- `yyyy-MM-dd` 日期，例如 `2027-06-30`：使用指定完整日期。
- 其他格式：先請使用者改用上述格式，不要自行猜測。

使用 Bash tool 呼叫對應 PowerShell script。只讀取後端建置產物，不得修改任何後端 Java 原始碼、設定或 JAR。執行完成後呈現完整解析結果，並另外清楚標示 License Key。

---
name: generate-alpha-license
description: 產生並解析 DGR Gateway 的 Alpha License Key。當使用者要求產生、建立、執行或驗證 Alpha License，且預設 account 為 TPI、env 為空，到期日未指定時需使用指定年度 12/31 時使用。
---

# Generate Alpha License

使用 `scripts/GenerateAlphaLicense.ps1` 呼叫現有後端 `LicenseUtil.quickGeneratorLicense()`。後端程式與建置產物一律唯讀，不得修改任何 Java 原始碼、設定或 JAR；缺少建置產物時直接停止並回報。

## 預設值

- 固定使用 `LicenseEditionType.Alpha`。
- 固定使用 `account = "TPI"`。
- 固定使用 `env = null`。
- 保留現有方法的 `nearWarnDays = 10` 與 `overBufferDays = 10`。
- 未指定到期日時，使用指定年度的 `12/31`。
- 年度與到期日皆未指定時，使用執行當年度的 `12/31`。

## 執行方式

在 Angular workspace root 執行：

```powershell
# 當年度 12/31
& '.agents\skills\generate-alpha-license\scripts\GenerateAlphaLicense.ps1'

# 指定年度 12/31
& '.agents\skills\generate-alpha-license\scripts\GenerateAlphaLicense.ps1' -Year 2027

# 指定完整到期日
& '.agents\skills\generate-alpha-license\scripts\GenerateAlphaLicense.ps1' -ExpiryDate '2027-06-30'
```

直接呈現 script 的完整輸出，並另外清楚標示產生的 License Key。提醒使用者 Key 內含執行時間，因此每次執行結果會不同。

若找不到已建置的 Gateway 或 Common JAR，先說明缺少的產物，不要改寫或模擬 License 演算法。

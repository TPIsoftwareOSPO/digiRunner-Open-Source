# mTLS 設定說明文件 

## 一、憑證簡介

TLS 憑證用於建立加密通道並驗證雙方身份。

### 常見憑證類型

| 類型 | 用途 | 範例 |
|------|------|------|
| Server 憑證 | 驗證伺服器身份 | HTTPS、API Gateway |
| Client 憑證 | 驗證使用者身份 | mTLS、VPN |
| Intermediate CA | 架在 Root CA 與憑證間 | 信任鏈的一環 |
| Root CA | 預信任的機構憑證 | DigiCert、Let's Encrypt |
| 自簽憑證 | 自建信任 | 測試環境使用 |

### 憑證格式

| 副檔名 | 說明 |
|--------|------|
| .crt/.pem | 憑證（Base64 編碼） |
| .key | 私鑰 |
| .p12/.pfx | 憑證+私鑰打包，可加密 |
| .csr | 憑證簽發請求 |

---

---

## 二、設定步驟

### 1. 開啟驗證功能

在 `application.properties` 中加入：
```
mtls.certificate.verification.enabled=true
```


> 修改後請重啟 `digiRunner` 生效

---

### 2. 設定 Client 憑證（AC 畫面）

操作路徑：AC → 用戶端憑證管理 → 建立 → 啟用

| 欄位 | 說明 |
|------|------|
| Host & Port | 請求符合時自動使用該憑證 |
| Root CA | Server 提供的 CA 憑證 |
| Client Cert | 使用者憑證 (.crt/.pem) |
| Client Key | 對應私鑰 (.key/.pem) |
| 金鑰密碼 | 如私鑰有密碼需填寫（將加密儲存） |

---

## 三、mTLS 憑證產製流程

1. **向 Server 索取：**
   - Server 的 CA 憑證（Root / Intermediate）
   - Client 憑證要求格式（CSR 或完整 cert）

2. **取得 Client 憑證：**
   - 產生 CSR 給對方簽署
   - 或對方提供憑證與私鑰

3. **組合憑證鏈（如需）：**
```
-----BEGIN CERTIFICATE-----
(Client Certificate)
-----END CERTIFICATE-----
-----BEGIN CERTIFICATE-----
(Intermediate CA)
-----END CERTIFICATE-----
```


---

## 四、API 使用 mTLS

在 API 註冊畫面勾選「啟用 mTLS」，否則請求會被拒絕。

---

## 五、常見錯誤排查

| 錯誤訊息 | 原因 | 解法 |
|----------|------|------|
| unable to verify the first certificate | 缺中繼/Root CA | 附完整鏈 |
| self signed certificate | 使用自簽但未信任 | 加入信任或改用受信任 CA |
| certificate expired | 憑證過期 | 重新簽發 |
| hostname mismatch | CN/SAN 不符請求主機名 | 修正憑證或主機名 |

---

## 六、常見 QA

**Q1: 可以用 Server 憑證當 Client 憑證嗎？**  
A: 正常不行。Server 憑證應僅用於 Server。例外：

- Server 未啟用 mTLS
- 測試環境未驗證（不建議）

**Q2: Client 憑證可以只放中繼憑證嗎？**  
A: 不行。必須包含最終使用者身分的憑證。建議：

- 將 Client cert + Intermediate cert 合併為一份 `.crt`

---

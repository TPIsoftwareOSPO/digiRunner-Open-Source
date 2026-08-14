# PrimeNG MCP Server 規範

本專案內建一個 PrimeNG 官方的 MCP server（`@primeng/mcp`），提供 PrimeNG 元件的 API 查詢能力。撰寫或修改任何使用 PrimeNG 元件的畫面時，優先透過它查證元件的 props / events / templates，不要憑印象猜測屬性名稱。

## 設定檔

同一個 server 由兩份設定各自註冊，兩邊必須保持一致：

| 檔案 | 供誰使用 |
|------|----------|
| `.mcp.json` | Claude Code（project scope，會 commit 進版控） |
| `.codex/config.toml` | Codex |

兩份都以相同方式啟動：

```json
// .mcp.json
{
  "mcpServers": {
    "primeng": {
      "command": "node",
      "args": [".tools/primeng-mcp/node_modules/@primeng/mcp/dist/index.js"]
    }
  }
}
```

```toml
# .codex/config.toml
[mcp_servers.primeng]
command = "node"
args = [
    ".tools/primeng-mcp/node_modules/@primeng/mcp/dist/index.js",
]
```

**修改其中一份時，必須同步修改另一份**，否則兩個工具會跑到不同版本的 server。

## ⚠️ 一律使用專案本地安裝，禁止改回 `npx`

server 的實體安裝在 `.tools/primeng-mcp/`，由該目錄自己的 `package.json` 管理版本：

```json
{
  "name": "dgr-primeng-mcp-tool",
  "private": true,
  "dependencies": { "@primeng/mcp": "21.1.9" },
  "overrides": { "@modelcontextprotocol/sdk": "1.25.2" }
}
```

`overrides` 這行是**必要**的，不是可有可無的優化。`@primeng/mcp@21.1.9` 相依的 `@primeuix/mcp@1.0.1` 在註冊 tool 時使用了舊版寫法，搭配較新的 `@modelcontextprotocol/sdk`（實測 1.29.0）會直接啟動失敗：

```text
Failed to start PrimeNG MCP Server: Error: Tool get_migration_guide expected a Zod schema
or ToolAnnotations, but received an unrecognized object
```

```jsonc
// ❌ 錯誤：npx 每次抓最新版，會繞過 overrides、拉到不相容的 SDK，server 起不來
{ "command": "npx", "args": ["-y", "@primeng/mcp"] }

// ✓ 正確：執行本地已 pinned 的安裝
{ "command": "node", "args": [".tools/primeng-mcp/node_modules/@primeng/mcp/dist/index.js"] }
```

升級 `@primeng/mcp` 版本時，務必先在本機實測 server 能正常啟動，確認新版是否還需要鎖定 SDK 版本，再一併更新 `overrides`。判斷何時該升，見下一節。

## 版本對應關係：改 `primeng` 版號時要不要動 MCP？

### 先理解兩件事

1. **`@primeng/mcp` 沒有 20.x**。已發行版本為 `0.0.1-alpha.1`、`0.0.1-alpha.2`，接著直接從 `21.0.1` 起跳至 `21.1.9`，再到 `22.0.0`（目前的 `latest`）。本專案裝的是 `primeng@20.4.0`，**沒有可完全對齊的 MCP 版本**，`21.1.9` 已是最接近的選擇。
2. **元件文件是離線打包的**。`@primeng/mcp/dist/index.js` 約 2.88 MB，元件 metadata 於發版當下 snapshot 進去（`@primeuix/mcp` 只是 37 KB 的查詢引擎），執行時不連網。因此 **MCP 的版本就是文件的版本**，不會因為專案改了 `primeng` 版號而跟著變。

### 判斷表

| 情境 | 是否要動 `.tools/primeng-mcp` |
|------|------------------------------|
| `primeng` patch / minor（例：`20.4.0` → `20.5.x`） | ❌ 不用。MCP 沒有對應的 20.x 可跟 |
| `primeng` major（`20` → `21` / `22`） | ✅ 應同步把 `@primeng/mcp` 升到**同一個 major** |
| 只升 MCP、不動 `primeng` | ⚠️ 不建議。只會讓查到的文件比實際安裝更超前，增加誤用新 API 的風險 |

### 升 MCP 時必須重測 SDK override

**不能**因為換了新版就假設相容性問題已修好。以 `22.0.0` 為例：

```
@primeng/mcp@22.0.0  →  @primeuix/mcp: ^2.0.0
@primeuix/mcp@2.0.0  →  { "zod": "^3.25.76", "@modelcontextprotocol/sdk": "^1.25.2" }
```

注意 `^1.25.2` 是 **caret**，解析結果仍會落在 1.29.x —— 與目前造成啟動失敗的版本區間相同。新版有沒有修掉 `expected a Zod schema` 只能實測，無法從 manifest 推斷。

升級流程：

1. 在 `.tools/primeng-mcp/package.json` 調整 `@primeng/mcp` 版本，**先暫時移除 `overrides`**
2. `cd .tools/primeng-mcp && npm install`
3. 用下方「驗證 server 是否正常」的 stdio handshake 實測
4. 啟動成功 → 不需要 `overrides`；仍失敗 → 把 `overrides` 加回並鎖到可用的 SDK 版本，再測一次
5. 兩份設定檔（`.mcp.json` / `.codex/config.toml`）路徑不變，但要重啟工具確認連線

## Clone 後的安裝步驟

`.tools/primeng-mcp/node_modules` 已列入 `.gitignore`，版控中只有 `package.json` 與 `package-lock.json`。新環境 clone 後必須手動安裝一次：

```bash
cd .tools/primeng-mcp
npm ci
```

此安裝與專案根目錄的 `npm ci` 相互獨立。

## 可用工具與使用時機

| 工具 | 用途 |
|------|------|
| `list_components` | 列出所有 PrimeNG 元件（可用 `category` 過濾） |
| `search_components` | 依名稱或說明搜尋元件 |
| `get_component` | 取得單一元件的完整資訊 |
| `get_component_props` | 取得元件的所有 props |
| `get_component_events` | 取得元件的所有 events |
| `get_component_methods` | 取得元件的所有 methods |
| `get_component_slots` | 取得元件的所有 templates |
| `get_usage_example` | 取得元件的程式碼範例 |

- 需要確認 PrimeNG 元件的屬性、事件或 template 名稱時，先查 MCP，再寫 code。
- **版本落差**：MCP server 是 `@primeng/mcp@21.1.9`（PrimeNG 21.x 文件），專案實際安裝的是 `primeng@20.4.0` + `@primeuix/themes@1.2.5`（Aura 主題）。
  - 實測抽查：`get_component_props` 回傳的 Button **30 個 props 在 20.4.0 全部存在**（其中 `dt`／`unstyled`／`pt`／`ptOptions` 繼承自 `node_modules/primeng/basecomponent/index.d.ts`）。常用元件查起來是準的。
  - 但這只是單一元件的抽查，**不保證所有元件都無落差** —— 21.x 新增的 API 仍可能出現在回應中。查到既有程式碼裡完全沒用過的屬性時，先到 `node_modules/primeng/<component>/index.d.ts` 確認 20.4.0 是否支援再使用。
  - 有衝突時**一律以專案實際安裝的版本與既有程式碼為準**；MCP 只是查詢輔助，不是本專案的規格來源。
- 樣式覆寫、按鈕 class 與版面配置等專案慣例，以 [style.md](style.md) 為準；MCP 範例只供 API 參考。

## 驗證 server 是否正常

```bash
# 檢查 Claude Code 端的連線狀態（應顯示 ✔ Connected）
claude mcp list

# 直接對 server 做一次 stdio handshake
printf '%s\n' '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"probe","version":"1.0"}}}' \
  | node .tools/primeng-mcp/node_modules/@primeng/mcp/dist/index.js
```

正常時應輸出 `PrimeNG MCP Server running on stdio`，並回傳包含 `serverInfo` 的 JSON-RPC 結果。

## 常見問題

| 症狀 | 原因與處理 |
|------|-----------|
| 工具清單中沒有 `mcp__primeng__*` | 設定檔在 session 開始後才變更，請重啟工具；Claude Code 首次啟動需批准 `.mcp.json` |
| 連線失敗、找不到 `dist/index.js` | `.tools/primeng-mcp/` 未安裝，請在該目錄執行 `npm ci` |
| `expected a Zod schema or ToolAnnotations` | 跑到 `npx` 版或 `overrides` 失效，請檢查兩份設定是否使用本地安裝 |
| 設定正確但 Claude Code 仍連到舊設定 | 使用者層級可能有同名註冊，使用 `claude mcp list` 確認實際指令 |

# 操作前提

**此文件需搭配 API  Swagger 規格文件進行實作。**

## 操作環境說明：

環境有兩個：10.20.30.88:20001 & localhost:18080，上面各一台DGR

10.20.30.88:20001 上面有多個 API 與 3個 Client ，之後10.20.30.88:20001 上面的 DGR 簡稱 88

![alt text](png/image.png)
![alt text](png/image-1.png)

localhost:18080 上面沒有 API ，只有一個 Client，之後 localhost:18080 上面的 DGR 簡稱 localhost

![alt text](png/image-2.png)
![alt text](png/image-3.png)

## 操作目的

將 88 的 API 、Client 匯出，匯入到 localhost ，並修改後端連結。

# Swagger 操作流程

## 1. 進入 Swagger 操作畫面
Swagger Editor: https://editor.swagger.io/

![alt text](png/image-4.png)

## 2. 取得並且設定 token

**因為所有 AC API 都會進行 Token 驗證，所以操作前必須先取得並設定 token**

設定 servers 為 88

![alt text](png/image-5.png)

拿 token

![alt text](png/image-7.png)

填入帳號密碼

![alt text](png/image-8.png)

複製 access token

![alt text](png/image-9.png)

設定 Authorize

![alt text](png/image-10.png)

填入 access token

![alt text](png/image-11.png)

![alt text](png/image-12.png)

## 3. 匯出 API LIST

### 3.1 查詢API清單

![alt text](png/image-13.png)
![alt text](png/image-14.png)
![alt text](png/image-15.png)

取得 apikey & modulname
![alt text](png/image-16.png)

### 3.2 匯出指定 API 

填寫一筆、匯出一筆，填寫全部、匯出全部

![alt text](png/image-17.png)

![alt text](png/image-18.png)

![alt text](png/image-19.png)

獲得匯出資料

![alt text](png/image-20.png)

## 4. 匯出 Client

![alt text](png/image-21.png)

![alt text](png/image-22.png)

![alt text](png/image-23.png)

獲得匯出資料

![alt text](png/image-24.png)

## 5. 匯入 API LIST


變更 servers 為 localhost
![alt text](png/image-25.png)

### 5.1 上傳檔案

![alt text](png/image-26.png)

![alt text](png/image-27.png)

指定 API 檔案

![alt text](png/image-28.png)

獲取暫存檔名

![alt text](png/image-29.png)

### 5.2 上傳註冊 / 組合 API

![alt text](png/image-30.png)

![alt text](png/image-31.png)

![alt text](png/image-32.png)

![alt text](png/image-33.png)

### 5.3 匯入註冊 / 組合 API

![alt text](png/image-34.png)

![alt text](png/image-35.png)

![alt text](png/image-36.png)

取得匯入回應
![alt text](png/image-37.png)

localhost 裡面確實已經匯入 API
![alt text](png/image-38.png)

## 6. 匯入 Client

### 6.1 上傳檔案

![alt text](png/image-39.png)

![alt text](png/image-40.png)

![alt text](png/image-41.png)

![alt text](png/image-42.png)

### 6.2 覆蓋設定

![alt text](png/image-43.png)

![alt text](png/image-44.png)

![alt text](png/image-45.png)

![alt text](png/image-46.png)

### 6.3 匯入用戶端確認

![alt text](png/image-47.png)

![alt text](png/image-48.png)

![alt text](png/image-49.png)

![alt text](png/image-50.png)

localhost 匯入 client
![alt text](png/image-51.png)

## 7. 批次修改後端

當前 localhost 上面的 API 的後端為 10.20.30.88，準備把 88 改為 localhost
![alt text](png/image-52.png)

### 7.1 批次修改暫存
![alt text](png/image-53.png)

![alt text](png/image-54.png)

![alt text](png/image-55.png)

![alt text](png/image-56.png)

### 7.2 批次修改確認

![alt text](png/image-57.png)

![alt text](png/image-58.png)

![alt text](png/image-59.png)

![alt text](png/image-60.png)

當前 localhost 上面的 API 的後端替換為 localhost
![alt text](png/image-61.png)

### 8. 取得 API 明細以驗證 API

![alt text](png/image-62.png)

![alt text](png/image-63.png)

![alt text](png/image-64.png)

![alt text](png/image-65.png)
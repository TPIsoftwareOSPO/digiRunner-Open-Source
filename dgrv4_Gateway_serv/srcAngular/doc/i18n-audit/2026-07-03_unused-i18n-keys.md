# 候選未使用語系 Key 清單

比對基準：`en-us.json` ∪ `zh-tw.json` 定義的 key（共 1582 筆）
掃描範圍：`src/app/**/*.html` 與 `src/app/**/*.ts`（共 1309 個檔案）

候選未使用：**394** 筆（已排除 65 筆動態組 key 白名單：`dbha.*`、`client_related.*`、`ai.*`、`embedded_url_tip.1_1/1_2/1_3`）

> 本清單為靜態掃描結果，僅供 review，尚未刪除任何檔案內容。建議動手刪除前針對每筆再人工確認一次。

| Key | zh-tw | en-us |
|---|---|---|
| `aberrant` | 異常 | Aberrant |
| `agent` | 代理 | Agent |
| `alert.text.active_net_site_error` | 不可啟用未綁定Module的site | Can't activate unbinded module site |
| `alert.text.delete_module_error` | 不可刪除綁定的Site為啟用中的Module | Can't delete site due to active binded Module |
| `alert.text.enter_line_token` | 請輸入Line Token | Please enter the Line token |
| `alert.text.export_apiList_error` | 不可以匯出 Java Module 或是 Net Module | Can't export the Java Module or Net Module |
| `alert.text.import_reg_comp_error` | 未選擇任何要匯入的API | No imported API are chosen |
| `alert.text.module_unbind` | Module未綁定 | Unbinded module |
| `alert.text.new_role_alias_error` | 新角色名稱格式錯誤 | New role alias format error |
| `alert.text.site_inactive` | Site未啟用 | Inactive site |
| `alert.title.confirm` | 確認 | Confirm |
| `alert_interval_err` | alertInterval 必須要有值，且為數字 | Alert interval must be >= 0 and cannot be empty |
| `alert_response_time` | Response Time 必須要有值，且為數字 | Response Time must be a number or not zero |
| `apiId` | API ID | API ID |
| `apiStatus.daysLater` | {{day}}天後 | {{day}} days later |
| `apiStatus.noSchedule` | 無預定 | No Schedule |
| `api_composer` | API 來源 | API Composer |
| `api_required` | API不可為空 | API is required |
| `api_theme` | API主題 | API theme |
| `app_name` | 應用名稱 | Application name |
| `app_name_cata` | 應用名稱分類 | Application name category |
| `applicant` | 申請者 | Applicant |
| `apply_status` | API申請狀態 | Applied status |
| `approver` | 簽核者 | Approver |
| `auth_level_definition` | 等級1(高) ~ 等級9(低) | Authorization Level 1(High) ~ 9(Low) |
| `auth_level_placeholder` | 依業務需求填入字串 | Fill in the string according to business needs |
| `author_organ` | 作者/機關 | Author/Organization |
| `bind` | 綁定 | Bind |
| `binding_version` | 綁定版本 | Binded version |
| `button.apply_api` | 申請API | Apply for API |
| `button.can_jwe` | 取消JWE | Cancel JWE |
| `button.choose` | 選擇 | Choose |
| `button.copy` | 拷貝 | Copy |
| `button.deploy` | 部署 | Deploy |
| `button.disabled` | 停止 | Disabled |
| `button.enabled` | 啟動 | Enabled |
| `button.enc_jwe` | JWE加密 | Encoded JWE |
| `button.export_csv` | 匯出CSV | Export CSV |
| `button.fail` | 不通過 | Failed |
| `button.force_bind` | 強制綁定 | Force binding |
| `button.import_openapi` | 匯入OpenAPI Spec | Import openAPI spec |
| `button.pass` | 通過 | Passed |
| `button.prev` | 上一頁 | Previous page |
| `button.previous` | 上一次 | Previous |
| `button.return_list` | 上一頁 | Return to list |
| `button.send` | 送出 | Send |
| `button.settle` | 結案 | Closed |
| `button.start` | 開始 | start |
| `button.stop` | 停止 | stop |
| `button.use` | 套用 | Use |
| `cache_name_label` | 依分類清除 | Clear by classification |
| `cata_name` | 分類名稱 | Category name |
| `cata_req` | 類別不可為空 | Category is required |
| `cfm_close_dialog` | 您確定要關閉目前的彈跳視窗嗎？ | Are you sure to close this dialog？ |
| `cfm_dc` | 您確定要{{value}}這個容器嗎？ | Are you sure to {{value}} this DC? |
| `cfm_del_app` | 您確定要刪除這個應用實例嗎？ | Confirm Delete? |
| `cfm_del_ca` | 您確定要刪除這個憑證嗎？ | Confirm Delete? |
| `cfm_del_cate` | 您確定要刪除這個主題嗎？ | Confirm Delete? |
| `cfm_del_cates` | 您確定要刪除這些主題嗎？ | Confirm Delete? |
| `cfm_del_dc` | 您確定要刪除這個DC嗎？ | Confirm Delete? |
| `cfm_del_doc` | 您確定要刪除這份文件嗎? | Confirm Delete? |
| `cfm_del_faq` | 您確定要刪除這則常見問答嗎？ | Confirm Delete? |
| `cfm_del_host` | 您確定刪除該筆註冊主機? | Confirm Delete? |
| `cfm_del_mdl` | 您確定要刪除這個模組嗎？ | Confirm Delete? |
| `cfm_del_news` | 您確定要刪除這個公告嗎？ | Confirm Delete? |
| `cfm_del_role` | 您確定要刪除這個角色嗎？ | Confirm Delete? |
| `cfm_del_user` | 您確定要刪除這個使用者嗎？ | Confirm Delete? |
| `cfm_mdl` | 您確定要{{value}}這個模組嗎？ | Are you sure to {{value}} this module? |
| `cfm_news` | 您確定要{{value}}這個公告嗎？ | Are you sure to {{value}} this announcement? |
| `change_time` | 異動時間 | Changed time |
| `chs_api_name` | 選擇API名稱 | Choose an API name |
| `chs_client` | 請選擇客戶端 | Please select a client |
| `chs_com_api` | 選擇組合的API | Choose a composed API |
| `chs_jar_war_file` | 請選擇一個jar、war檔案 | Choose a jar or war file |
| `chs_war_file` | 請選擇一個war檔案 | Choose a war file |
| `click_drop_img` | 點擊瀏覽或拖曳圖片至此 | Click to select or Drag and drop image here. |
| `client_apply_explanation` | 用戶申請說明 | Client application instructions |
| `client_ip` | 用戶端IP | Client IP |
| `client_list` | 用戶清單 | Client List |
| `client_qualifications_audit_remark` | 用戶資格審核備註 | Client qualifications audit remark |
| `create_err` | 建立失敗 | Create error |
| `current_status` | 目前狀態 | Current status |
| `cusBackendLoginUrl_tip` | 觸發 CUS 登入的 URL，應該把 CUS 進行登入時的 URL 填入 | URL to trigger CUS login. The URL used for CUS login should be entered here. |
| `cusUserDataUrl_tip` | 提供 DGR 獲取使用者資料的 API URL，CUS 必須提供一支 API，讓 DGR 可以獲取對應的使用者資料 | API URL for DGR to retrieve user data. The CUS must provide an API that allows DGR to obtain the corresponding user data. |
| `dashBoard.title` | License | License |
| `day` | 天 | Day |
| `dc` | 部署容器 | DC |
| `dc_id` | 部署容器編號 | DC ID |
| `dc_memo` | 部署容器備註 | DC memo |
| `dc_status` | 部署容器狀態 | DC status |
| `default_website_proxy` | 預設網站反向代理 | Default Website Reverse Proxy |
| `delivery_time` | 送件時間 | Delivery time |
| `develop_portal` | 入口網 | Develop Portal |
| `dialog.add` | 增加 | Add |
| `dialog.create_alert` | 新增告警 | Create alert |
| `dialog.create_reg_host` | 建立註冊主機 | Create registered host |
| `dialog.detail_alert` | 告警明細 | Alert details |
| `dialog.query` | 查詢 | Query |
| `dialog.update_alert` | 更新告警 | Update Alert |
| `dialog.update_reg_host` | 更新註冊主機 | Update registered host |
| `digirunner_node` | digiRunner Node | digiRunner Node |
| `disabled` | 否 | Disabled |
| `doc_cat` | 文件分類 | Document category |
| `doc_desc` | 文件說明 | Document description |
| `doc_name` | 文件名稱 | Document name |
| `docdesc_required` | 文件說明不可為空 | Document description is required |
| `docname_required` | 文件名稱不可為空 | Document name is required |
| `dot` | 點 | Dot |
| `dp_api_category_update_title` | API主題分類更新 | Update DP API theme category |
| `dp_application_update_title` | 應用實例分類更新 | Update DP application category |
| `dp_example_update_title` | 應用實例更新 | Update DP application title |
| `dp_faq_update_title` | 常見問答更新 | Update DP FAQ |
| `dp_file_update_title` | 文件更新 | Update DP File |
| `duration_err` | duration 必須要有值，且為數字 | Duration must be a number and not zero |
| `e_date` | 迄日 | End Date |
| `enable_hot_mo` | 是否啟用熱監控 | Enable monitor |
| `enabled` | 是 | Enabled |
| `enabled_dc_node_list` | 啟用此容器的節點 | The node where this container is enabled |
| `enabled_heartbeat` | 啟用心跳 | Heartbeat |
| `event_time` | 事件時間 | Event time |
| `exc_type` | 例外類型 | Exception types |
| `failed_login_attempts` | 重置密碼錯誤次數 | Failed login attempts |
| `faq_ans` | 答案 | Answer |
| `faq_ans_required` | 答案不可為空 | FAQ answer is required |
| `faq_name` | 問題 | Question |
| `faq_name_required` | 問題不可為空 | FAQ question name is required |
| `file.pem` | PEM檔 | PEM file |
| `final` | API版本/日期 | Current |
| `form_data` | Form-Data | Form data |
| `form_url_encoded` | Form-UrlEncoded | Form urlEncoded |
| `frequency` | 頻率 | Frequency |
| `funcCode` | 功能代碼 | Function Code |
| `gen_pub` | 一般民眾 | General public |
| `good` | 良好 | Good |
| `group_alias_list` | 群組名稱清單 | Group alias list |
| `group_auth` | 核身型式 | Group authorized authentication |
| `group_id_list` | 群組編號清單 | Group ID list |
| `group_name_list` | 群組代碼清單 | Group name list |
| `heartbeat` | 心跳時間 | Heartbeat status/date |
| `heartbeat_detector` | 是否啟用心跳 | Heartbeat detector |
| `host` | 主機 | Host |
| `host_client` | 主機客戶端 | Host client |
| `host_list` | 主機清單 | Host IP list |
| `hostname_no_empty` | 主機名稱不可為空白 | Host name cannot be empty |
| `hour` | 小時 | Hour |
| `http_validate` | Http Validate | HTTP Validate |
| `idxName` | Index | Index |
| `imgfile_req` | 上傳圖片不可為空 | Image file is required |
| `import_action` | 匯入動作 | Import action |
| `input_role_name` | 請輸入角色名稱 |  |
| `intro_of_works` | 作品介紹 | Introduction of works |
| `intro_req` | 作品介紹不可為空 | Introduction is required |
| `ipSrcUrl` | 來源URL | Source URL |
| `is_login` | 是否登入 | Login |
| `java_mdl` | Java 模組 | Java module |
| `jdbcUrl` | JDBC URL | JDBC URL |
| `jwt_batch_setting` | JWT批次設定 | JWT batch setting |
| `layer.body.cate_1` | 用戶API申請(限入口網) | Client API application(Web developer portal used only) |
| `layer.body.cate_2` | API上下架申請 | API on and off Shelves application |
| `layer.header.cate` | 類別 | Category |
| `ldap_login` | 使用LDAP帳號登入 | Login with LDAP Account |
| `level.LV1` | 等級1 | Level 1 |
| `level.LV2` | 等級2 | Level 2 |
| `level.LV3` | 等級3 | Level 3 |
| `level.LV4` | 等級4 | Level 4 |
| `level.LV5` | 等級5 | Level 5 |
| `level.LV6` | 等級6 | Level 6 |
| `level.LV7` | 等級7 | Level 7 |
| `level.LV8` | 等級8 | Level 8 |
| `level.LV9` | 等級9 | Level 9 |
| `limit_pdf` | 限上傳pdf檔 | Uploaded files limited to pdf format |
| `log_seq` | Log序號 | Log sequence number |
| `mask.body_remain_after_exp` | "name":"test","id":"1" => ###":"1" (保留後5碼用###替換) | "name":"test","id":"1" => ###":"1" (Keep the last 5 chars, masked with ###) |
| `mask.body_remain_before_exp` | "name":"test","id":"1" => "name### (保留前5碼用###替換) | "name":"test","id":"1" => "name### (Keep the first 5 chars, masked with ###) |
| `masterFuncCode` | 主選單 | Main menu |
| `message.app` | 應用實例 | Application |
| `message.app_cata` | 應用實例分類 | Application category |
| `message.client_audit_error.plz_chs_client` | 請勾選要審核的用戶 | Please check clients who needs to be audited |
| `message.client_audit_error.plz_chs_public_flag` | 請選擇用戶身分 | Please check client public flag |
| `message.faq` | 常見問答 | Question |
| `message.file` | 檔案 | File |
| `message.mdl_status` | 模組狀態 | Module status |
| `message.module` | 模組 | Module |
| `message.news` | 公告 | Announcements |
| `message.news_content_length_error` | 。(注意: 文字內容會包含Html Tag, 因此實際字數會多於可見字數) | Announcemnet content exceeded allowed length (Note: If content contains Html Tag, the actual word count will be differ than the visible word count) |
| `message.password` | 密碼 | Password |
| `message.requisition.client_register` | 用戶端註冊申請單 | Client Register Requisition |
| `message.status` | 狀態 | Status |
| `message.theme` | 主題 | Theme |
| `message.token` | Token | Token |
| `method` | Method | Method |
| `missing_required_params` | 缺少必要參數 | Missing required parameters |
| `moduels` | Modules | Modules |
| `module_application` | 應用程式啟動類別 | Module Application |
| `module_version_detail` | 模組版本明細 | Module version detail |
| `module_version_list` | 模組版本清單 | Module Version list |
| `name_req` | 名稱不可為空 | Name is required |
| `net_mdl` | NET 模組 | NET module |
| `newMima` | 新密碼 | New password |
| `newUserBlock` | 新密碼 | New password |
| `new_allow_access_days` | 新允許使用時間 | New Allow Access Times |
| `new_allow_access_use_times` | 新授權次數上限 | New Allow Access Use Number of times |
| `new_api_desc` | 新API說明 | New API description |
| `new_api_key_list` | 新API清單 | New API list |
| `new_api_name` | 新API名稱 | New API name |
| `new_api_status` | 新API狀態 | New API status |
| `new_authorities` | 新授權核身種類 | New authorities |
| `new_client_alias` | 新用戶端名稱 | New client alias |
| `new_client_id` | 新用戶端帳號 | New Client ID |
| `new_client_name` | 新用戶端代號 | New client name |
| `new_emails` | 新電子郵件帳號 | New emails |
| `new_group_access_method` | 新允許存取方式 | New access method |
| `new_group_auth_desc` | 新核身描述 | New group authority description |
| `new_group_auth_level` | 新核身等級 | New group authority level |
| `new_group_auth_name` | 新核身名稱 | New group authority name |
| `new_group_id_list` | 新群組編號清單 | New group ID list |
| `new_host_list` | 新主機清單 | New host list |
| `new_http_method` | New Http Method | New Http method |
| `new_news_content` | 新公告內容 | Content |
| `new_news_post_date` | 新公告日期 | Effective Date |
| `new_news_title` | 新公告標題 | Subject |
| `new_news_type_itme` | 新公告類別 | Type |
| `new_owner` | 新擁有者 | New owner |
| `new_reg_host` | 新註冊主機 | New register host |
| `new_role_list` | 新角色清單 | New role assignment |
| `new_security_level` | 新安全等級 | New security level |
| `new_signup_num` | 新簽呈編號 |  |
| `new_src_url` | 新來源URL | New source URL |
| `new_status` | 新狀態 | New status |
| `news_content` | 公告內容 | Content |
| `news_effective_date` | 新公告日期 | Effective Date |
| `news_id` | 公告編號 | ID |
| `news_post_date` | 公告日期 | Date |
| `news_post_org` | 發布單位 | Posted By |
| `news_title` | 公告標題 | Subject |
| `news_type_itme` | 公告類別 | Type |
| `no_oauth` | No Oauth | No OAuth |
| `no_upload_file` | 無上傳檔案 | No upload files found |
| `node_id` | 節點編號 | Node ID |
| `node_list` | 節點清單 | Node List |
| `normal` | 正常 | Normal |
| `number` | 號 | Number |
| `official` | 市府人員 | official |
| `only_update_partial` | 僅輸入要更新的欄位 | Please enter updated fields only |
| `org_code` | 組織代碼 | Organization code |
| `org_id` | 組織序號 | Organization ID |
| `org_path` | 組織關係 | Organization path |
| `other` | 其他 | Other |
| `owner` | 擁有者 | Owner |
| `parent_id` | 上層組織代號 | Parent ID |
| `parent_org` | 上層組織名稱 | Parent organization |
| `path` | Path | Path |
| `plz_input_mdl_name` | 請輸入模組名稱 | Please input module name |
| `protocol` | 協定 | Protocol |
| `rCode` |  | Return code |
| `rMsg` |  | Return message |
| `recommend_image_size` | 建議圖片大小為 240px × 210px | The recommended image size is 240px × 210px |
| `record_time` | 紀錄時間 | Record time |
| `redirect_url` | Redirect URL | Redirect URL |
| `reg_host_expe_status` | 預期狀態 | Expected status |
| `reg_host_id` | 註冊主機序號 | ID |
| `reg_host_status` | 主機狀態 | Host Status |
| `reg_url` | 要註冊的URL | Registered URL |
| `release` | 放行 | Release |
| `remark_id_req` | 備註編號不可為空 | Remark id is required |
| `req_app` | 申請原因 | Reasons of application |
| `req_body_json` |  | Request Body(json) |
| `req_body_xml` |  | Request Body(xml) |
| `reset_pwd_fail_times` | 重置密碼錯誤次數 | Reset password error tries |
| `reset_status` | 重置狀態 | Reset status |
| `resubmit_date_time` | 重新送審日期 | Review datetime |
| `return` | 退回 | Return |
| `return_api_list_view` | 返回API列表 | Return to API list |
| `review` | 審核中 | Review |
| `review_result` | 審核結果 | Review result |
| `review_sender` | 送審人 | Review sender |
| `review_status` | 審核狀態 | Review status |
| `revoked_time` | 撤銷時間 | Revoked Time |
| `rolelist_err` | 最少需選擇一個角色 | Choose at least one role |
| `s_date` | 起日 | Start date |
| `schedule_seqno` | 排程工作序號 | Schedule seqno |
| `sel_theme_set` | 請選擇要設置的主題 | Please choose a theme for configuration |
| `send_email` | 發送Mail通知 | Send email notification |
| `send_req` | 發送請求 | Send request |
| `site_code_is_null` | {{請輸入SiteCode}} | {{Please enter SiteCode}} |
| `smart_client.help.allowedScopes.example` |  |  |
| `smart_client.scope_cat.context` |  |  |
| `smart_client.scope_cat.identity` |  |  |
| `smart_client.scope_cat.other` |  |  |
| `smart_client.scope_cat.permission` |  |  |
| `smart_launcher.cfg_allowed_scopes` | 允許的 Scope | Allowed Scopes |
| `smart_launcher.cfg_load_btn` | 載入 | Load |
| `smart_launcher.cfg_load_hint` | 輸入既有 client 的 ID 載入其 SMART 設定；查無則可直接新建一筆。 | Enter an existing client's ID to load its SMART config; if none exists, saving will create one. |
| `smart_launcher.cfg_load_label` | Client ID（載入既有 client 的 SMART 設定） | Client ID (load an existing client's SMART config) |
| `smart_launcher.cfg_mode_create` | 查無設定，存檔將新增一筆 SMART 設定（client 須已存在） | No config found — saving will create a new SMART configuration (client must already exist) |
| `smart_launcher.cfg_mode_edit` | 編輯既有 SMART 設定 | Editing existing SMART configuration |
| `smart_launcher.cfg_no_change` | 沒有需要儲存的變更 | No changes to save |
| `smart_launcher.cfg_not_found` | 此 Client 尚無 SMART 設定，無法編輯。 | This client has no SMART configuration to edit. |
| `smart_launcher.cfg_title` | SMART 設定編輯 | Edit SMART Configuration |
| `smart_launcher.client_settings` | Client 設定 | Client Settings |
| `smart_launcher.client_type_backend` | Backend Service | Backend Service |
| `smart_launcher.encounter_auto` | 自動（最近一筆） | Auto (most recent) |
| `smart_launcher.encounter_manual` | 手動選擇 | Manual |
| `smart_launcher.launch_type_backend_services` | Backend Services | Backend Services |
| `smart_launcher.launch_type_desc_backend_services` | 使用 client credentials 的伺服器對伺服器存取 | Server-to-server access using client credentials |
| `smart_launcher.launch_type_desc_patient_portal` | 模擬從病人入口網站啟動 | Simulates launch from a patient portal |
| `smart_launcher.launch_type_desc_patient_standalone` | 病人直接開啟應用程式並連接 FHIR | Patient opens the app directly and connects to FHIR |
| `smart_launcher.launch_type_desc_provider_ehr` | 模擬從 EHR 內部啟動（面向醫療人員） | Simulates launch from within an EHR (provider-facing) |
| `smart_launcher.launch_type_desc_provider_standalone` | 醫療人員直接開啟應用程式並連接 FHIR | Provider opens the app directly and connects to FHIR |
| `smart_launcher.launch_type_patient_portal` | Patient Portal | Patient Portal |
| `smart_launcher.launch_type_patient_standalone` | Patient Standalone | Patient Standalone |
| `smart_launcher.launch_type_provider_ehr` | Provider EHR Launch | Provider EHR Launch |
| `smart_launcher.launch_type_provider_standalone` | Provider Standalone | Provider Standalone |
| `smart_launcher.load_more` | 載入更多 | Load More |
| `smart_launcher.patient_id_placeholder` | 病人 ID | Patient ID |
| `smart_launcher.pkce_always` | 強制 | Always |
| `smart_launcher.pkce_auto` | 自動 | Auto |
| `smart_launcher.pkce_none` | 不使用 | None |
| `smart_launcher.preview` | Request Preview | Request Preview |
| `smart_launcher.preview_authorize` | Authorize | Authorize |
| `smart_launcher.preview_token` | Token | Token |
| `smart_launcher.provider_id_placeholder` | 醫療人員 ID | Provider ID(s) |
| `smart_launcher.proxy_name` | FHIR Proxy 名稱 | FHIR Proxy Name |
| `smart_launcher.redirect_uri` | Redirect URI | Redirect URI |
| `smart_launcher.select_client` | 選擇已註冊的 Client | Select Registered Client |
| `smart_launcher.select_client_hint` | 從已註冊的 SMART Client 中選取，自動帶入設定。或選擇手動輸入自行填寫。 | Select from registered SMART Clients to auto-fill settings, or choose manual input. |
| `smart_launcher.select_client_manual` | -- 手動輸入 -- | -- Manual Input -- |
| `smart_launcher.sim_error_auth_invalid_client_id` | 無效的 client_id | invalid client_id |
| `smart_launcher.sim_error_auth_invalid_client_secret` | 無效的 client_secret | invalid client_secret |
| `smart_launcher.sim_error_auth_invalid_redirect_uri` | 無效的 redirect_uri | invalid redirect_uri |
| `smart_launcher.sim_error_auth_invalid_scope` | 無效的 scope | invalid scope |
| `smart_launcher.sim_error_group_auth` | 授權請求階段 | During the authorize request |
| `smart_launcher.sim_error_group_request` | FHIR 請求階段 | During FHIR requests |
| `smart_launcher.sim_error_group_token` | Token 請求階段 | During the token request |
| `smart_launcher.sim_error_request_expired_token` | 過期的 access token | expired access token |
| `smart_launcher.sim_error_request_invalid_token` | 無效的 access token | invalid access token |
| `smart_launcher.sim_error_token_expired_refresh` | 過期的 refresh token | expired refresh token |
| `smart_launcher.sim_error_token_invalid_scope` | 無效的 scope | invalid scope |
| `smart_launcher.sim_error_token_invalid_token` | 無效的 token | invalid token |
| `status_date` | 狀態更新時間 | Status updated time |
| `status_user` | 狀態更新人員 | Status updated user |
| `sum_rear_path` | summary rear path | summary rear path |
| `table_name_label` | 依資料表名稱清除 | Clear by table name |
| `tar_context_path` | 目標Context Path | Target Context Path |
| `target_version` | 開發版本 | Development version |
| `task_args` | 任務參數 | Arguments |
| `theme` | 主題 | Theme |
| `theme_image` | 上傳圖片 | Theme Image |
| `threshold_err` | threshold 必須要有值，且為數字 | Threshold must be a number and not zero |
| `timezone_quired` |  | Time zone is required |
| `token` | Token | Token |
| `total_version` | 版本總數 | Total version |
| `trustedCertsContent` | 憑證 | Certificate |
| `txsn` | 前端交易序號 | Transaction serial number |
| `unbind` | 解除綁定 | Unbind |
| `upload_date` | 上傳時間 | Upload date |
| `upload_image` | 上傳圖片 | Upload Image |
| `upload_info` | 建議圖片大小為 240px × 210px，檔案大小不可超過5MB，格式限JPG、PNG、GIF | Image shall be JPG, PNG or GIF. Image pixel shall be 240px*240px and within 5MB. |
| `upload_java_mdl` | 上傳Java模組 | Upload Java module |
| `upload_need_jar_or_war` | 上傳檔案必須為jar、war檔 | The uploaded file needs to be in jar or war format |
| `upload_need_pdf` | 上傳的文件必須是pdf | The uploaded file needs to be in pdf format |
| `upload_need_war` | 上傳檔案必須為war檔 | The uploaded file needs to be in war format |
| `upload_net_mdl` | 上傳.Net模組 | Upload .Net Module |
| `upload_success` | 上傳成功 | Upload successfully |
| `upload_user` | 上傳人員 | Upload user |
| `user_agent` | 代理 | User Agent |
| `user_roles` | 使用者持有角色 | User role list |
| `username` | User Name | User Name |
| `validation.aliasmaxlength` | 不可超過{{value}}個字元，一個中文算2個字元 | Please enter no more than {{value}} characters (Note: Chinese character counts as 2 characters). |
| `validation.confirm_mima` | 密碼不匹配 | Password does not matched. |
| `validation.ipaddress` | 請輸入IP格式的字元 | This IP address is not valid for this field. |
| `validation.mailFormat` | Mail格式錯誤 | Mail format error |
| `validation.minlength` | 不可少於{{value}}個字元 | Please enter more than {{value}} characters. |
| `validation.required` | 必填 | Required |
| `validation.stringalias` | 字元中僅限中、英、數字、底線(_)、橫線(-) | Characters restricted to Chinese and English characters, as well as numbers, underline(_) and dash(-) only |
| `validation.stringalias_space` | 字元中僅限中、英、數字、底線(_)、橫線(-)、空白字元 | Characters restricted to Chinese and English characters, as well as numbers, space character, underline(_) and dash(-) only |
| `validation.stringanum` | 字元中僅限英數字 | Characters restricted to English characters and numbers only |
| `validation.stringchart` | 字元中僅限英數字、底線(_)、橫線(-)、點(.)、小老鼠(@) | Characters restricted to English characters, number, underline(_), dash(-), dot(.) and at(@) only |
| `validation.stringcode` | 字元中僅限英數字、底線(_)、橫線(-)、點(.) | Characters restricted to English characters, number, underline(_), dash(-) and dot(.) only |
| `validation.stringname` | 字元中僅限英數字、底線(_)、橫線(-) | Characters restricted to English characters, numbers, underline(_) and dash(-) only |
| `validation.stringname_space` | 字元中僅限英數字、底線(_)、橫線(-)、空白字元 | Characters restricted to English characters, numbers, space character, underline(_) and dash(-) only |
| `version` | 版本 | Version |
| `virtul_group_alias_list` | 授權範圍名稱清單 | API scope alias list |
| `virtul_group_id` | 授權範圍編號 | API scope ID |
| `week_option.fri` | 五 | Fri |
| `week_option.mon` | 一 | Mon |
| `week_option.sat` | 六 | Sat |
| `week_option.sun` | 日 | Sun |
| `week_option.thu` | 四 | Thu |
| `week_option.tue` | 二 | Tue |
| `week_option.wed` | 三 | Wed |
| `whether_bind` | 是否有綁定 | Whether had binded |
| `whether_heartbeat` | 是否啟用心跳 | Heartbeat monitor |
| `xapikey_name` | X-API-Key 名稱 | X-API-Key name |
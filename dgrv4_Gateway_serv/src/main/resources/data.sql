-- [START] TSMPDPAA 專門 --
-- 送傳 emil 之 format/template(內容可以是 URL 使自己 Entity參考自改, 用來組成一份完整的 HTML)
CREATE TABLE IF NOT EXISTS tsmp_dp_mail_tplt ( 
	mailtplt_id			BIGINT NOT NULL,	-- ID(流水號)
	code				VARCHAR(20) NOT NULL,	-- 類型代碼
	template_txt		VARCHAR(2000) NOT NULL,	-- 範本本文/URL
	remark				VARCHAR(100) NULL,	-- 備註
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_date_time	DATETIME,	-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),	-- 更新人員
	version				INT DEFAULT 1,	-- 版號: C/U時, 增量+1
	keyword_search		NVARCHAR(2120),	-- LikeSearch使用: 信件類型 | 範本本文 | 備註
	PRIMARY KEY (mailtplt_id),
	UNIQUE (code)
);

-- email 發送歷程記錄
CREATE TABLE IF NOT EXISTS tsmp_dp_mail_log (
	maillog_id			BIGINT NOT NULL,	-- ID(流水號)
	recipients			VARCHAR(100) NOT NULL,	-- 收件者Mail
	template_txt		VARCHAR(3800) NOT NULL,	-- 傳送內容
	ref_code			VARCHAR(20) NOT NULL,	-- 類型代碼(From tsmp_dp_mail_tplt.code)
	result				VARCHAR(1) NOT NULL DEFAULT '0',	-- 寄送結果(1:成功, 0:失敗)
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_date_time	DATETIME,	-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),	-- 更新人員
	version				INT DEFAULT 1,	-- 版號: C/U時, 增量+1
	keyword_search		VARCHAR(4000),	-- LikeSearch使用: 收件者MAIL| 傳送內容| 類型代碼
	PRIMARY KEY (maillog_id)
);

-- 申請單主檔
CREATE TABLE IF NOT EXISTS tsmp_dp_req_orderm (
	req_orderm_id		BIGINT NOT NULL,	-- ID(流水號)
	req_order_no		VARCHAR(30) NOT NULL,	-- 單號(案件編號), TYPE-YYYYMMDD-0001, TYPE為案件類型, TYPE Ref: TSMP_DP_ITEMS.PARAM1, 若有子類別PARAM1 值可用, 則不使用父類別。{AP / ON / OF / UP}
	req_type			VARCHAR(20) NOT NULL,	-- 簽核類別, Ref: TSMP_DP_ITEMS.ITEM_NO, REVIEW_TYPE(簽核類別代碼) = {API_APPLICATION / API_ON_OFF}
	req_subtype			VARCHAR(20),	-- 簽核子類別, Ref: TSMP_DP_ITEMS.SUBITEM_NO, REVIEW_SUBTYPE(簽核子類別代碼) = {API_ON / API_OFF / API_ON_UPDATE}
	client_id			VARCHAR(40) NOT NULL,	-- Client ID, ex:"YWRtaW5Db25zb2xl"
	org_id				VARCHAR(255),	-- 組織單位ID, 前台的申請為 Null
	req_desc			NVARCHAR(1000) NOT NULL,	-- 申請說明
	req_user_id			VARCHAR(10),	-- 申請人員, 使用者(TUser)工號
	effective_date		DATETIME,	-- 生效日期, null:表示審核通過後立即生效
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_date_time	DATETIME,	-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),	-- 更新人員
	version				INT DEFAULT 1,	-- 版號: C/U時, 增量+1
	keyword_search		NVARCHAR(1020),	-- LikeSearch使用: 單號|說明
	PRIMARY KEY (req_orderm_id),
	UNIQUE (req_order_no)
);

-- 申請單.審核狀態: 申請單建立時, 立即依當前關卡生成空白的關卡數, 等待審核, 重新送審時, 原有的STATUS=停用, 重新建立審核空白資料
CREATE TABLE IF NOT EXISTS tsmp_dp_req_orders (
	req_orders_id		BIGINT NOT NULL,	-- ID(流水號)
	req_orderm_id		BIGINT NOT NULL,	-- MasterPK, ref Master
	layer				INT NOT NULL,	-- 關卡層數
	req_comment			VARCHAR(200),	-- 審核意見
	review_status		VARCHAR(20) DEFAULT 'WAIT1' NOT NULL,	-- 簽核狀態, Ref: TSMP_DP_ITEMS.SUBITEM_NO.ITEM_NO="REVIEW_STATUS"
	status				VARCHAR(1) DEFAULT '1' NOT NULL,	-- 啟用狀態, 1：啟用，0：停用 （預設啟用）
	proc_flag			INT NULL,	-- 現況指標	'1' 會停在待簽核的那關, '0' 會停在最後結束的那關
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_date_time	DATETIME,	-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),	-- 更新人員
	version				INT DEFAULT 1,	-- 版號: C/U時, 增量+1
	keyword_search		NVARCHAR(200),	-- LikeSearch使用: 審核意見
	PRIMARY KEY (req_orders_id)
);

-- 簽核關卡角色設定檔 (關卡與角色對應檔)
CREATE TABLE IF NOT EXISTS tsmp_dp_chk_layer (
	chk_layer_id		BIGINT NOT NULL,	-- ID(流水號)
	review_type			VARCHAR(20) NOT NULL,	-- 簽核類別代碼 ex:REVIEW_TYPE, from TSMP_DP_ITEMS.SUBITEM_NO
	layer				INT NOT NULL,	-- 關卡層數
	role_id				VARCHAR(10) NOT NULL,	-- 角色ID(有哪些角色可以簽核)
	status				VARCHAR(1) DEFAULT '1' NOT NULL,	-- 啟用狀態(1：啟用，0：停用 （預設啟用）)
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_date_time	DATETIME,	-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),	-- 更新人員
	version				INT DEFAULT 1,	-- 版號: C/U時, 增量+1
	PRIMARY KEY (review_type, layer, role_id)
);

CREATE INDEX IF NOT EXISTS index_tsmp_dp_chk_layer_01 ON tsmp_dp_chk_layer (chk_layer_id);

-- 申請單之審核歷程, COPY "TSMP_DP_REQ_ORDERS" UPDATE 完成的當筆記錄					
CREATE TABLE IF NOT EXISTS tsmp_dp_chk_log (
	chk_log_id			BIGINT NOT NULL,	-- ID(流水號)
	req_orders_id		BIGINT NOT NULL,	-- 
	req_orderm_id		BIGINT NOT NULL,	-- 
	layer				INT NOT NULL,	-- 
	req_comment			VARCHAR(200),	-- 審核意見
	review_status		VARCHAR(20) NOT NULL,	-- 簽核狀態
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	PRIMARY KEY (chk_log_id)
);

-- 申請單-用戶申請API明細檔
CREATE TABLE IF NOT EXISTS tsmp_dp_req_orderd1 (
	req_orderd1_id		BIGINT NOT NULL,	-- ID(流水號)
	ref_req_orderm_id	BIGINT NOT NULL,	-- MasterPK, ref Master
	client_id			VARCHAR(40) NOT NULL,
	api_uid				VARCHAR(36) NOT NULL,
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_date_time	DATETIME,	-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),	-- 更新人員
	version				INT DEFAULT 1,	-- 版號: C/U時, 增量+1
	PRIMARY KEY (req_orderd1_id)
);

-- 申請單-API上架/下架/異動(申請單 Detail 明細中包含THEME 之Mapping(TSMP_DP_REQ_ORDERD2D), 及 每支API mapping 說明文件(TSMP_API_EXT + TSMP_DP_FILE))
CREATE TABLE IF NOT EXISTS tsmp_dp_req_orderd2 (
	req_orderd2_id		BIGINT NOT NULL,	-- ID(流水號)
	ref_req_orderm_id	BIGINT NOT NULL,	-- MasterPK, ref Master
	api_uid				VARCHAR(36) NOT NULL,	-- API UUID
	public_flag			CHAR(1) NULL,	-- 開放權限, 0:對內及對外, 1:對外, 2:對內, null:對內
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_date_time	DATETIME,	-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),	-- 更新人員
	version				INT DEFAULT 1,	-- 版號: C/U時, 增量+1
	PRIMARY KEY (req_orderd2_id)
);
CREATE INDEX IF NOT EXISTS index_tsmp_dp_req_orderd2_01 ON tsmp_dp_req_orderd2 (api_uid);

-- 申請單-API上架/下架/異動 API mapping Theme
CREATE TABLE IF NOT EXISTS tsmp_dp_req_orderd2d (
	req_orderd2_id		BIGINT NOT NULL,	-- from TSMP_DP_REQ_ORDERD2
	api_uid				VARCHAR(36) NOT NULL,	-- API UUID, from TSMP_DP_REQ_ORDERD2
	ref_theme_id		BIGINT NOT NULL,	-- from TSMP_DP_THEME_CATEGORY.ID
	req_orderd2d_id		BIGINT NOT NULL,	-- ID(流水號)
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_date_time	DATETIME,	-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),	-- 更新人員
	version				INT DEFAULT 1,	-- 版號: C/U時, 增量+1
	PRIMARY KEY (req_orderd2_id, api_uid, ref_theme_id),
	UNIQUE (req_orderd2d_id)
);

-- 申請單-用戶申請註冊
CREATE TABLE IF NOT EXISTS tsmp_dp_req_orderd3 (
	req_orderd3_id		BIGINT NOT NULL,	-- ID(流水號)
	ref_req_orderm_id	BIGINT NOT NULL,	-- MasterPK, ref Master
	client_id			VARCHAR(40) NOT NULL,
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_date_time	DATETIME,	-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),	-- 更新人員
	version				INT DEFAULT 1,	-- 版號: C/U時, 增量+1
	PRIMARY KEY (req_orderd3_id)
);

-- 申請單-昕力大學文章 (開發範例用，非正式簽核類別，不用上SIT、PROD)
CREATE TABLE IF NOT EXISTS tsmp_dp_req_orderd4 (
	req_orderd4_id		BIGINT NOT NULL,					-- ID(流水號)
	ref_req_orderm_id	BIGINT NOT NULL,					-- MasterPK, ref Master
	user_id				VARCHAR(10) NOT NULL,				-- 員工(使用者)ID
	article				VARCHAR(4000),						-- 文章內容
	is_publish			INT DEFAULT 0 NOT NULL,				-- 公開狀態: 0=不公開, 1=公開
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',		-- 建立人員
	update_date_time	DATETIME,							-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),						-- 更新人員
	version				INT DEFAULT 1,						-- 版號: C/U時, 增量+1
	PRIMARY KEY (req_orderd4_id)
);

-- 申請單-Open API Key 申請/異動/撤銷
CREATE TABLE IF NOT EXISTS tsmp_dp_req_orderd5 (		
	req_orderd5_id		BIGINT NOT NULL,				-- ID (流水號)	
	client_id			VARCHAR(255) NOT NULL,			-- Client ID	
	ref_req_orderm_id	BIGINT NOT NULL,				-- MasterPK	ref Master
	ref_open_apikey_id	BIGINT,							-- ref TSMP_OPEN_APIKEY.OPEN_APIKEY_ID
	open_apikey			VARCHAR(1024),					-- Open API Key	
	secret_key			VARCHAR(1024),					-- Secret KEY	
	open_apikey_alias	VARCHAR(255) NOT NULL,			-- Open  API Key別名	
	times_threshold		INT NOT NULL DEFAULT 0,			-- 使用次數上限	"1.效期內的使用次數上限2. 若值為-1, 則 Open API Key 無使用上限"
	expired_at			BIGINT NOT NULL,				-- 用戶端 API KEY 效期	
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_date_time	DATETIME,						-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),					-- 更新人員
	version				INT DEFAULT 1,					-- 版號: C/U時, 增量+1
 	PRIMARY KEY (req_orderd5_id)
);								
CREATE INDEX IF NOT EXISTS index_tsmp_dp_req_orderd5_01 ON tsmp_dp_req_orderd5 (open_apikey);

-- 申請單-Open API Key 申請/異動/撤銷 mapping API
CREATE TABLE IF NOT EXISTS tsmp_dp_req_orderd5d (	
	ref_req_orderd5_id	BIGINT NOT NULL,				-- from TSMP_DP_REQ_ORDERD5.REQ_ORDERD5_ID
	ref_api_uid			VARCHAR(36)	NOT NULL,			-- API UUID	from TSMP_API.API_UID
	req_orderd5d_id		BIGINT	NOT NULL,				-- ID (流水號)		
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_date_time	DATETIME,						-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),					-- 更新人員
	version				INT DEFAULT 1,					-- 版號: C/U時, 增量+1 
	PRIMARY KEY (ref_req_orderd5_id, ref_api_uid)
);

-- 授權API對應檔
CREATE TABLE IF NOT EXISTS tsmp_dp_api_auth2 (
	api_auth_id			BIGINT NOT NULL,	-- ID(流水號)
	ref_client_id		VARCHAR(40) NOT NULL,	-- Client ID: 關連TSMP_CLIENT的CLIENT_ID
	ref_api_uid			VARCHAR(36) NOT NULL,	-- API UUID: 關連TSMP_API.API_UID(帳號ID+API UID為唯一值)
	apply_status		VARCHAR(10) NOT NULL,	-- 申請狀態: (審核中/通過/不通過 : REVIEW/PASS/FAIL)
	apply_purpose		NVARCHAR(3000) NOT NULL,	-- 申請用途說明
	ref_review_user		NVARCHAR(255) NULL,	-- 審核人員: (關聯帳號資料 TSMP_USER), 執行審核才Update
	review_remark		VARCHAR(3000) NULL,	-- 審核備註=不通過：要檢查「審核備註」要必填
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_date_time	DATETIME,	-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),	-- 更新人員
	version				INT DEFAULT 1,	-- 版號: C/U時, 增量+1
	keyword_search		NVARCHAR(6000),	-- LikeSearch使用: 申請用途|審核備註
	PRIMARY KEY (api_auth_id)
);

-- TSMP API...之入口網延伸欄位
CREATE TABLE IF NOT EXISTS tsmp_api_ext (
	api_key				VARCHAR(30) NOT NULL,	-- API代碼
	module_name			VARCHAR(100) NOT NULL,	--
	dp_status			VARCHAR(1) NOT NULL,	-- 0:下架;1:上架
	dp_stu_date_time	DATETIME,	-- 上下架的異動時間
	ref_orderm_id		BIGINT NOT NULL,	-- 生效單號,from TSMP_DP_REQ_ORDERM.REQ_ORDERM_ID
	api_ext_id			BIGINT NOT NULL,	-- ID(流水號)
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_date_time	DATETIME,	-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),	-- 更新人員
	version				INT DEFAULT 1,	-- 版號: C/U時, 增量+1
	PRIMARY KEY (api_key, module_name),
	UNIQUE (api_ext_id)
);

-- 應用實例主題分類內容資料集對應檔
CREATE TABLE IF NOT EXISTS tsmp_dp_api_theme (
	ref_api_theme_id	BIGINT NOT NULL,	-- ID 不自動產生: 關連TSMP_DP_THEME_CATEGORY.API_THEME_ID
	ref_api_uid			VARCHAR(36) NOT NULL,	-- API UUID
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	PRIMARY KEY (ref_api_theme_id, ref_api_uid)
);

-- 排程工作 CALL API 記錄
CREATE TABLE IF NOT EXISTS tsmp_dp_callapi (
	callapi_id			BIGINT NOT NULL,		-- ID(流水號)
	req_url				VARCHAR(500) NOT NULL,	-- API URL
	req_msg				VARCHAR(4000),			-- request body
	resp_msg			VARCHAR(4000),			-- response body
	token_url			VARCHAR(500),			-- 取得Token的url
	sign_code_url		VARCHAR(500),			-- 取得SignBlock的url
	auth				VARCHAR(500) NOT NULL,	-- "Basic " + Base64(client_id:Base64(clientPwd))
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	PRIMARY KEY (callapi_id)
);

-- 主題分類: 記錄主題分類
CREATE TABLE IF NOT EXISTS tsmp_dp_theme_category (
	id					BIGINT AUTO_INCREMENT NOT NULL,	-- ID(流水號)
	theme_name			NVARCHAR(100) NOT NULL,	-- API主題名稱: 例如：農業、民政、文化、經濟、教育、環保、族群、財稅...
	data_status			CHAR(1) NOT NULL DEFAULT '1',	-- 資料狀態:1=啟用，0=停用(預設啟用)
	data_sort			INT NULL,	-- 資料排序
	org_id				VARCHAR(255) NULL,	-- 組織單位ID: 不可重複組織單位識別碼, null 表示全部User都可看到
	create_time			DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_time			DATETIME,	-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),	-- 更新人員
	version				INT DEFAULT 1,	-- 版號: C/U時, 增量+1
	keyword_search		NVARCHAR(100),	-- LikeSearch使用: API主題名稱
	PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS index_tsmp_dp_theme_category_01 ON tsmp_dp_theme_category (theme_name);

-- TSMP 模組資料
CREATE TABLE IF NOT EXISTS tsmp_api_module (
	id					BIGINT NOT NULL,	-- Module ID
	module_name			VARCHAR(255) NOT NULL,	-- Module Name
	module_version		VARCHAR(255) NOT NULL,	-- Module Version
	module_app_class	VARCHAR(255) NOT NULL,	-- Start Class
	module_bytes		LONGBLOB NOT NULL,
	module_md5			VARCHAR(255) NOT NULL,
	module_type			VARCHAR(255) NOT NULL,	-- jar/war
	upload_time			TIMESTAMP NOT NULL,	-- 上傳時間
	uploader_name		VARCHAR(255) NOT NULL,	-- 上傳人員
	status_time			TIMESTAMP NULL,	-- 狀態更新時間
	status_user			VARCHAR(255) NULL,	-- 狀態更新人員
	active				BOOLEAN NULL,	-- Module狀態: TRUE=Active, FALSE=Disable
	node_task_id		INT NULL,	-- TSMP_NODE_TASK ID: Module啟動/停止的任務識別碼
	v2_flag				INT NULL,	-- V2架構
	org_id				VARCHAR(255) NULL,
	PRIMARY KEY (id),
	UNIQUE (module_name, module_version)
);

-- TSMP DC與Module關聯資料	
CREATE TABLE IF NOT EXISTS tsmp_dc_module (
	dc_id			BIGINT NOT NULL,	-- 部署容器編號
	module_id		BIGINT NOT NULL,	-- Module ID (表示此部署容器要載入啟用的Module)
	node_task_id	INT NULL,	-- TSMP_NODE_TASK ID (Module啟動/停止的任務識別碼)
	PRIMARY KEY (dc_id, module_id)
);

-- TSMP部署容器
CREATE TABLE IF NOT EXISTS tsmp_dc (
	dc_id		BIGINT NOT NULL,	-- 部署容器代號
	dc_code		VARCHAR(30) NOT NULL,	-- 部署容器代碼(格式: "dcv." + "X.X", ex: "ver.1.0")
	dc_memo		VARCHAR(300) NULL,	-- 部署容器備註
	active		BOOLEAN NOT NULL,	-- 部署容器狀態(TRUE: Active, FALSE(Default): Inactive)
	create_user	VARCHAR(255) NOT NULL,	-- 建立人員
	create_time	DATETIME NOT NULL,	-- 建立日期
	update_user	VARCHAR(255) NULL,	-- 異動人員
	update_time	DATETIME NULL,	-- 異動日期
	PRIMARY KEY (dc_id)
);

CREATE SEQUENCE if not exists SEQ_TSMP_DC_PK;

-- TSMP DC與NODE關聯資料 
CREATE TABLE IF NOT EXISTS tsmp_dc_node (
	node 			VARCHAR(30) NOT NULL, 	-- TSMP Node Alias "TSMP_NODE.NODE(Properties中的"tsmp.core.node.alias") 
	dc_id 			BIGINT 		NOT NULL, 	-- 部署容器編號 TSMP_DC.DC_ID 
	node_task_id 	BIGINT, 				-- TSMP_NODE_TASK ID Module啟動/停止的任務識別碼 
	PRIMARY KEY (node, dc_id)
);

--TSMP API詳細資料
CREATE TABLE IF NOT EXISTS tsmp_api_detail (
	id				INT NOT NULL,	-- 序號
	api_module_id	INT NOT NULL,	-- Module ID
	api_key			VARCHAR(255) NOT NULL,	-- API KEY
	api_name		VARCHAR(255) NOT NULL,	-- API說明
	path_of_json	VARCHAR(255) NOT NULL,	-- PATH
	method_of_json	VARCHAR(255) NOT NULL,	-- Http Method
	params_of_json	VARCHAR(255) NOT NULL, 	-- Http Parameters
	headers_of_json	VARCHAR(255) NOT NULL,	-- Http Header
	consumes_of_json	VARCHAR(255) NOT NULL,	-- Http ContentType
	produces_of_json	VARCHAR(255) NOT NULL,	-- Http Response
	PRIMARY KEY(id),
	CONSTRAINT UK_api_detail_1 UNIQUE (api_module_id, api_key),
	CONSTRAINT FK_api_detail_1 FOREIGN KEY (api_module_id) REFERENCES tsmp_api_module
);

CREATE INDEX IF NOT EXISTS index_tsmp_api_detail_01 ON tsmp_api_detail (api_key);

-- TSMP 事件檢視器資料
CREATE TABLE IF NOT EXISTS tsmp_events (
	event_id			BIGINT NOT NULL,		-- ID (流水號)
	event_type_id		VARCHAR(20) NOT NULL,	-- 事件類型代碼
	event_name_id		VARCHAR(20) NOT NULL,	-- 事件名稱代碼
	module_name			NVARCHAR(255) NOT NULL,	-- 模組名稱
	module_version		NVARCHAR(255),			-- 模組版本
	trace_id			VARCHAR(20) NOT NULL,	-- 追踪 Id
	info_msg			NVARCHAR(4000),			-- 任意訊息
	keep_flag			VARCHAR(1) NOT NULL DEFAULT 'N',	-- 是否保留
	archive_flag		VARCHAR(1) NOT NULL DEFAULT 'N',	-- 是否封存
	node_alias			NVARCHAR(200),			-- Node 別名
	node_id				VARCHAR(200),			-- Node 代碼
	thread_name			NVARCHAR(1000),			-- 線程名稱
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	PRIMARY KEY (event_id)
);

--	TSMP NODE 任務表    
CREATE TABLE IF NOT EXISTS tsmp_node_task (
	id				BIGINT			NOT NULL,	
	task_signature	VARCHAR(255)	NOT NULL,	-- 任務簽章
	task_id			VARCHAR(255)	NOT NULL,	-- 任務每次送出時的識別碼
	task_arg		VARCHAR(4095)	NOT NULL,	-- 任務的參數
	coordination	VARCHAR(255)	NOT NULL,	-- 任務的協調性
	execute_time	DATETIME		NOT NULL,	-- 任務要執行的時間
	notice_node		VARCHAR(255)	NOT NULL,	-- 任務是由哪一個TSMP node送出的
	notice_time		DATETIME		NOT NULL,	-- 任務送出時間
	node			VARCHAR(30),				-- TSMP Node Alias	Properties中的"tsmp.core.node.alias"
	module_name		VARCHAR(255),				-- for V3
	module_version	VARCHAR(255), 				-- for V3
	PRIMARY KEY (id),
	UNIQUE (task_signature, task_id)
);

-- TSMP NODE 任務工作表   
CREATE TABLE IF NOT EXISTS tsmp_node_task_work (
	id 					BIGINT 			NOT NULL, 			
 	node_task_id 		BIGINT 			NOT NULL,	-- TSMP_NODE_TASK ID
 	competitive_id 		VARCHAR(255) 	NOT NULL, 	-- TSMP node 競爭ID
 	competitive_time 	DATETIME 		NOT NULL,	-- TSMP node 競爭時間
 	competitive_node 	VARCHAR(255) 	NOT NULL, 	-- TSMP node name
 	update_time 		DATETIME 		NOT NULL,	-- TSMP node 執行該任務會一直update時間, 直到結束(success: true/false)
 	success 			BOOLEAN, 					-- TSMP node 執行該任務成功與否
 	error_msg 			VARCHAR(1023), 				-- TSMP node 執行該任務錯誤時之訊息
 	node 				VARCHAR(30), 				-- TSMP Node Alias Properties中的"tsmp.core.node.alias"  
 	PRIMARY KEY (id),  
 	UNIQUE (node_task_id, competitive_id)  
);

-- TSMP .net模組資料
CREATE TABLE IF NOT EXISTS tsmpn_api_module (
	id					BIGINT NOT NULL,	-- Module ID
	module_name			VARCHAR(255) NOT NULL,	-- Module Name
	module_version		VARCHAR(255) NOT NULL,	-- Module Version
	module_app_class	VARCHAR(255) NOT NULL,	-- Start Class
	module_bytes		LONGBLOB NOT NULL,
	module_md5			VARCHAR(255) NOT NULL,
	module_type			VARCHAR(255) NOT NULL,	-- jar/war
	upload_time			TIMESTAMP NOT NULL,	-- 上傳時間
	uploader_name		VARCHAR(255) NOT NULL,	-- 上傳人員
	status_time			TIMESTAMP NULL,	-- 狀態更新時間
	status_user			VARCHAR(255) NULL,	-- 狀態更新人員
	active				BOOLEAN NOT NULL,	-- Module狀態: TRUE=Active, FALSE=Disable
	target_version		NVARCHAR(30) NOT NULL,
	org_id				VARCHAR(255) NULL,
	PRIMARY KEY (id),
	UNIQUE (module_name, module_version)
);

-- TSMPN部署站台
CREATE TABLE IF NOT EXISTS tsmpn_site (
	site_id			BIGINT AUTO_INCREMENT NOT NULL,	-- 部署站台編號
	site_code		NVARCHAR(30) NOT NULL,	-- 部署站台代碼, 格式: "nev." + "X.X", ex: "nev.1.0"
	site_memo		NVARCHAR(1000000000),	-- 部署站台備註
	active			BOOLEAN NOT NULL,	-- 部署站台狀態, TRUE: Active, FALSE(Default): Inactive
	create_user		VARCHAR(255),	-- 建立人員
	create_time		DATETIME NOT NULL,	-- 建立日期
	update_user		NVARCHAR(30), 	-- 異動人員
	update_time		DATETIME,	-- 異動日期
	protocol_type	NVARCHAR(20) DEFAULT 'http' NOT NULL,	-- 站台 Protocol, http,https
	binding_ip		NVARCHAR(20) NOT NULL,	-- 站台 IP	Default: localhost
	binding_port	INT NOT NULL,	-- 站台 Port
	app_pool		NVARCHAR(255) NOT NULL,	-- 應用程式集區, 預設使用 SITE_CODE
	root_path		NVARCHAR(1000000000),	-- Module實體路徑
	clr_version		NVARCHAR(30),	-- .Net CLR 版本
	PRIMARY KEY (site_id)
);

-- TSMPN SITE與Module關聯資料
CREATE TABLE IF NOT EXISTS tsmpn_site_module (
	site_id			BIGINT NOT NULL,	-- 部屬站台編號
	module_id		BIGINT NOT NULL,	-- 表示此部屬容器要載入啟用的Module
	node_task_id	BIGINT, 			-- TSMP_NODE_TASK_ID, Module啟動/停止的任務識別碼
	PRIMARY KEY (site_id)
);

--TSMP_REPORT_DATA
CREATE TABLE  IF NOT EXISTS TSMP_REPORT_DATA (
    ID	BIGINT NOT NULL,	--ID(流水號) from seq_store
    REPORT_TYPE	INT NOT NULL,	--報表類型
    DATE_TIME_RANGE_TYPE	INT NOT NULL,	--時間類型
    LAST_ROW_DATE_TIME	DATETIME NOT NULL,	--統計LOG資料的最後一筆RTIME欄位時間
    STATISTICS_STATUS	CHAR(1) NOT NULL,	--資料是否已經統計過
    STRING_GROUP1	VARCHAR(255),	--群組 KEY 1
    STRING_GROUP2	VARCHAR(255),	--群組 KEY 2
    STRING_GROUP3	VARCHAR(255),	--群組 KEY 3
    INT_VALUE1	BIGINT,	--統計 VALUE 1
    INT_VALUE2	BIGINT,	--統計 VALUE 2
    INT_VALUE3	BIGINT,	--統計 VALUE 3
	ORGID	VARCHAR(255)	NOT	NULL,        --API隸屬於哪個組織的ID
    CREATE_DATE_TIME	DATETIME DEFAULT CURRENT_TIMESTAMP,	--建立日期
    CREATE_USER	VARCHAR(255) DEFAULT 'SYSTEM',	--建立人員
    UPDATE_DATE_TIME	DATETIME,	--更新日期
    UPDATE_USER	VARCHAR(255),	--更新人員
    VERSION INT DEFAULT 1,	--版號
    CONSTRAINT TSMP_REPORT_DATA_PK PRIMARY KEY (ID)
);

--TSMP_REQ_LOG
CREATE TABLE IF NOT EXISTS TSMP_REQ_LOG (
	ID	VARCHAR(63) NOT NULL,	--ID:NodeRandomCode(4碼)+yyMMddHHmmss(12碼)+000000(6碼流水號, 999999之後從00000開始)
	RTIME	DATETIME NOT NULL,	--record time 當下時間(以server時間為準)
	ATYPE	VARCHAR(3) NOT NULL,	--API type M: module R: registerd C: composed N: .net
	MODULE_NAME	VARCHAR(255) NOT NULL,	--模組名稱
	MODULE_VERSION	VARCHAR(255) NOT NULL,	--模組版本
	NODE_ALIAS	VARCHAR(255) NOT NULL,	--tsmp node alias
	NODE_ID	VARCHAR(255) NOT NULL,	--node id EX: eapdddrdev01/127.0.0.1/sgn2
	URL	VARCHAR(255) NOT NULL,	--呼叫API時的路徑 EX: /tsmpaa/00/aa0012
	CIP	VARCHAR(255) NOT NULL,	--client remote ip
	ORGID	VARCHAR(255) NOT NULL,	--API隸屬於哪個組織的ID
	TXID	VARCHAR(255),	--其實也就是ApiKey 若找不到則不用寫(例如拿token的路徑就不會有ApiKey)
	ENTRY	VARCHAR(255),	--tsmpc , tsmpg 轉導時寫入 , 一般module為null 當ATYPE不等於M的時候此欄位理論上應該都有值
	CID	VARCHAR(255),	--token所攜帶的client id
	TUSER	VARCHAR(255),	--token所攜帶的user
	JTI	VARCHAR(255),	--token的jti
	CONSTRAINT TSMP_REQ_LOG_PK PRIMARY KEY (ID)
);

--TSMP_RES_LOG
CREATE TABLE IF NOT EXISTS TSMP_RES_LOG (
	ID	VARCHAR(63) NOT NULL,	--ID 同TSMP_RES_LOG的值(用於串起一個request+response)
	EXE_STATUS	CHAR(1) NOT NULL,	--此次API呼叫成功與否 Y: API正常執行邏輯 N: API可能因為權限檢查錯誤或其他因素未正常執行邏輯
	ELAPSE	INT NOT NULL,	--API執行耗時(ms) MTYPE:2才要記錄
	RCODE	VARCHAR(63) NOT NULL,	--return code
	HTTP_STATUS	INT NOT NULL,	--response的http status
	ERR_MSG	VARCHAR(4095),	--錯誤訊息 若有錯誤將記錄其message
	CONSTRAINT TSMP_RES_LOG_PK PRIMARY KEY (ID)
);

-- JWE加密憑證內容
CREATE TABLE IF NOT EXISTS tsmp_client_cert (
	client_cert_id		BIGINT NOT NULL,		-- ID (流水號)
	client_id			VARCHAR(40) NOT NULL,	-- Client 代碼(Client 於digiRunner上註冊之ID)
	cert_file_name		VARCHAR(255) NOT NULL,	-- 憑證檔名(Client 於digiRunner上傳之憑證檔案名稱)
	file_content		BINARY(5242880) NOT NULL,	-- 憑證 byte(完整的檔案內容 *.PEM)
	pub_Key				VARCHAR(1024) NOT NULL,	-- 公鑰(Public Key Value)
	cert_version		VARCHAR(255),			-- 憑證版本(Version)
	cert_serial_num		VARCHAR(255) NOT NULL,	-- 憑證序號(Serial Number)
	s_algorithm_id		VARCHAR(255),			-- 簽章演算法(Signature Algorithm ID)
	algorithm_id		VARCHAR(255) NOT NULL,	-- 公鑰演算法(Algorithm ID)
	cert_thumbprint		VARCHAR(1024) NOT NULL,	-- CA數位指紋(CA Digital Signature)
	iuid				VARCHAR(255),			-- 發行方ID(Issuer Unique ID)
	issuer_name			VARCHAR(255) NOT NULL,	-- 發行方名稱(Issuer (CA) X.500 Name)
	suid				VARCHAR(255),			-- 持有者身分ID: "Subject Unique ID (這個ID是憑證內容所發布的Client正式ID, 與digiRunner上註冊的Client ID是不同的請注意)"
	create_at			BIGINT NOT NULL,		-- 憑證創建日: Validity Period (Start Date)
	expired_at			BIGINT NOT NULL,		-- 憑證到期日: Validity Period (Exireation Date)
	key_size			INT NOT NULL DEFAULT 0,	-- Key size	0 表示舊資料
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_date_time	DATETIME,				-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),			-- 更新人員
	version				INT DEFAULT 1,			-- 版號: C/U時, 增量+1
	PRIMARY KEY (client_cert_id)
);
CREATE INDEX IF NOT EXISTS index_tsmp_client_cert_01 ON tsmp_client_cert (client_id);

-- TLS通訊憑證內容
CREATE TABLE IF NOT EXISTS tsmp_client_cert2 (
	client_cert2_id		BIGINT NOT NULL,		-- ID (流水號)
	client_id			VARCHAR(40) NOT NULL,	-- Client 代碼(Client 於DigiRunner上註冊之ID)
	cert_file_name		VARCHAR(255) NOT NULL,	-- 憑證檔名(Client 於digiRunner上傳之憑證檔案名稱)
	file_content		BINARY(5242880) NOT NULL,	-- 憑證 byte(完整的檔案內容 *.PEM)
	pub_Key				VARCHAR(1024) NOT NULL,	-- 公鑰(Public Key Value)
	cert_version		VARCHAR(255),			-- 憑證版本(Version)
	cert_serial_num		VARCHAR(255) NOT NULL,	-- 憑證序號(Serial Number)
	s_algorithm_id		VARCHAR(255),			-- 簽章演算法(Signature Algorithm ID)
	algorithm_id		VARCHAR(255) NOT NULL,	-- 公鑰演算法(Algorithm ID)
	cert_thumbprint		VARCHAR(1024) NOT NULL,	-- CA數位指紋(CA Digital Signature)
	iuid				VARCHAR(255),			-- 發行方ID(Issuer Unique ID)
	issuer_name			VARCHAR(255) NOT NULL,	-- 發行方名稱(Issuer (CA) X.500 Name)
	suid				VARCHAR(255),			-- 持有者身分ID: "Subject Unique ID (這個ID是憑證內容所發布的Client正式ID, 與digiRunner上註冊的Client ID是不同的請注意)"
	create_at			BIGINT NOT NULL,		-- 憑證創建日: Validity Period (Start Date)
	expired_at			BIGINT NOT NULL,		-- 憑證到期日: Validity Period (Exireation Date)
	key_size			INT NOT NULL DEFAULT 0,	-- Key size	0 表示舊資料
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_date_time	DATETIME,				-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),			-- 更新人員
	version				INT DEFAULT 1,			-- 版號: C/U時, 增量+1
	PRIMARY KEY (client_cert2_id)
);
CREATE INDEX IF NOT EXISTS index_tsmp_client_cert2_01 ON tsmp_client_cert2 (client_id);

-- 通知歷程: 紀錄哪個服務透過何種方式在什麼時候發送過通知
CREATE TABLE IF NOT EXISTS tsmp_notice_log (
	notice_log_id			BIGINT NOT NULL, -- ID (流水號)
	notice_src				VARCHAR(100) NOT NULL,	-- 通知來源, e.g. "JWECertExpire" 表示是 "JWE加密憑證到期通知"
	notice_mthd				VARCHAR(10) NOT NULL,	-- 通知方式, e.g. "EMAIL" 表示透過電子郵件發送通知。
	notice_key				VARCHAR(255) NOT NULL,	-- 通知鍵值
	detail_id				BIGINT,	-- 明細鍵值, ex: TSMP_DP_MAIL_LOG.maillog_id
	last_notice_date_time	DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,	-- 上次發送通知時間
	PRIMARY KEY (notice_log_id)
);

--TSMP_CLIENT_LOG
CREATE TABLE IF NOT EXISTS TSMP_CLIENT_LOG (
  LOG_SEQ NVARCHAR(20) NOT NULL,
  IS_LOGIN SMALLINT NOT NULL,
  AGENT NVARCHAR(500) NULL,
  EVENT_TYPE NVARCHAR(10) NULL,
  EVENT_MSG NVARCHAR(300)  NULL,
  EVENT_TIME DATETIME  NULL,
  CLIENT_ID NVARCHAR(40) NOT NULL,
  CLIENT_IP NVARCHAR(15) NOT NULL,
  USER_NAME NVARCHAR(30) DEFAULT NULL,
  TXSN NVARCHAR(20) NOT NULL,
  CREATE_TIME DATETIME NULL,
  PRIMARY KEY (LOG_SEQ)
);

--TSMP_ALERT
create table IF NOT EXISTS TSMP_ALERT (
	ALERT_ID int not null
	, ALERT_NAME VARCHAR(30)  not null
	, ALERT_TYPE VARCHAR(20) not null 
	, ALERT_ENABLED boolean  not null
    ,THRESHOLD int
    ,DURATION int
    ,ALERT_INTERVAL int
    ,C_FLAG boolean  not null
    ,IM_FLAG boolean  not null
    ,IM_TYPE VARCHAR(20)
    ,IM_ID VARCHAR(100)
    ,EX_TYPE CHAR(1)  not null
    ,EX_DAYS VARCHAR(100)
    ,EX_TIME VARCHAR(100)
    ,ALERT_DESC VARCHAR(200)
    ,ALERT_SYS VARCHAR(20) default NULL
    ,ALERT_MSG VARCHAR(300) default NULL
    ,CREATE_TIME timestamp NULL
    ,UPDATE_TIME timestamp NULL
    ,CREATE_USER varchar(30) DEFAULT NULL
    ,UPDATE_USER varchar(30) DEFAULT NULL
    ,ES_SEARCH_PAYLOAD VARCHAR(1024) default NULL
    ,MODULENAME VARCHAR(255) NULL
    ,RESPONSETIME VARCHAR(255) NULL
	, primary key (ALERT_ID)
);

--SEQ_TSMP_ALERT_PK
create sequence if not exists SEQ_TSMP_ALERT_PK increment by 1 start with 10 nomaxvalue;

--TSMP_ROLE_ALERT
create table IF NOT EXISTS TSMP_ROLE_ALERT (
	ROLE_ID varchar(10) not null
	, ALERT_ID int  not null
	, primary key (ROLE_ID, ALERT_ID)
);

--TSMP用戶端Host基本資料
CREATE TABLE IF NOT EXISTS TSMP_CLIENT_HOST (
	HOST_SEQ INT NOT NULL,
	CLIENT_ID VARCHAR(40) NOT NULL,
	HOST_NAME VARCHAR(50) NOT NULL,
	HOST_IP VARCHAR(15) NOT NULL,
	CREATE_TIME DATETIME NOT NULL,
	PRIMARY KEY (HOST_SEQ)
);

--SEQ_TSMP_CLIENT_HOST_PK
create sequence if not exists SEQ_TSMP_CLIENT_HOST_PK increment by 1 start with 1;

-- TSMP 安全等級資料
CREATE TABLE IF NOT EXISTS TSMP_SECURITY_LEVEL
(
   SECURITY_LEVEL_ID    varchar(10)   NOT NULL, -- 安全等級ID
   SECURITY_LEVEL_NAME  varchar(30)   NOT NULL, -- 安全等級名稱 HIGH/MEDIUM/LOW
   SECURITY_LEVEL_DESC  varchar(60),  -- 安全等級描述
   PRIMARY KEY (SECURITY_LEVEL_ID),
   CONSTRAINT UK_SECURITY_LEVEL_1 UNIQUE (SECURITY_LEVEL_NAME)
);

-- 20220127, 增加 LDAP單一登入授權表, Mini Lee
CREATE TABLE IF NOT EXISTS ldap_auth_result ( 
	ldap_id 			BIGINT NOT NULL, 				-- ID (流水號)
	user_name 			VARCHAR(50) NOT NULL, 				-- 使用者名稱 
	code_challenge 		VARCHAR(50) NOT NULL, 				-- 處理過的UUID UUID 經過 Base64UrlEncode(SHA256(UUID)) 的值 
	user_ip				VARCHAR(50),						-- 使用者IP
	use_date_time		DATETIME,							-- 使用時間	
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user 		VARCHAR(255) DEFAULT 'LDAP_SYSTEM', -- 建立人員 
	update_date_time 	DATETIME, 							-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料 
	update_user 		VARCHAR(255), 						-- 更新人員 
	version 			INT DEFAULT 1, 						-- 版號 C/U時, 增量+1 
	PRIMARY KEY (ldap_id) 
);

-- SSO-LDAP單一登入授權表
CREATE TABLE IF NOT EXISTS sso_auth_result ( 
	sso_id 				BIGINT NOT NULL, 					-- ID (流水號)
	user_name 			VARCHAR(50) NOT NULL, 				-- 使用者名稱 
	code_challenge 		VARCHAR(50) NOT NULL, 				-- 處理過的UUID UUID 經過 Base64UrlEncode(SHA256(UUID)) 的值 
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user 		VARCHAR(255) DEFAULT 'SSO SYSTEM', 	-- 建立人員 
	update_date_time 	DATETIME, 							-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料 
	update_user 		VARCHAR(255), 						-- 更新人員 
	version 			INT DEFAULT 1, 						-- 版號 C/U時, 增量+1 
	PRIMARY KEY (sso_id) 
);

-- 20220127, SSO-LDAP單一登入授權表, 增加欄位, Mini Lee
ALTER TABLE sso_auth_result ADD use_date_time DATETIME;

-- SSO的使用者的部份PW       
CREATE TABLE IF NOT EXISTS tsmp_sso_user_secret (       
	user_secret_id 		BIGINT NOT NULL , 					-- ID (流水號)
	user_name 			VARCHAR(50) NOT NULL , 				-- 使用者名稱   
	secret 				VARCHAR(100) NOT NULL , 				-- 部份PW  UUID 經過 Base64UrlEncode(SHA256(UUID)) 的值 
	create_date_time 	DATETIME DEFAULT CURRENT_TIMESTAMP, -- 建立日期  資料初始建立的人, 日期時間
	create_user 		VARCHAR(255) DEFAULT 'SSO SYSTEM', 	-- 建立人員   
	update_date_time 	DATETIME, 							-- 更新日期  表示最後Update的人, 日期時間, Null 表示是新建資料,前台使用 clientName(UK), 後台使用 UserName(UK) 
	update_user 		VARCHAR(255), 						-- 更新人員   
	version 			INT DEFAULT 1, 						-- 版號  C/U時, 增量+1
	PRIMARY KEY (user_secret_id)            
);

--註冊主機
CREATE TABLE IF NOT EXISTS tsmp_reg_host (
    REGHOST_ID 	varchar(10) NOT NULL,	--註冊主機序號
    REGHOST 	varchar(30) NOT NULL,	--主機
    REGHOST_STATUS char(1)  NOT NULL DEFAULT 'S',	--預期狀態
    ENABLED 	char(1)     NOT NULL DEFAULT 'N' ,	--啟用心跳
    CLIENTID 	varchar(40) NOT NULL,	--用戶端帳號
    HEARTBEAT 	DATETIME,				--心跳時間
    MEMO 		varchar(300),			--備註
    CREATE_USER varchar(255),
    CREATE_TIME DATETIME    NOT NULL,
    UPDATE_USER varchar(255),
    UPDATE_TIME DATETIME,
    PRIMARY KEY (REGHOST_ID),
    UNIQUE 		(REGHOST)
);

CREATE SEQUENCE if not exists SEQ_TSMP_REG_HOST_PK;

--tsmp_reg_module TSMP外部註冊API模組資料
CREATE TABLE IF NOT EXISTS tsmp_reg_module (
	reg_module_id		BIGINT NOT NULL,	-- ID(流水號)
	module_name		VARCHAR(255) NOT NULL,	-- 模組名稱
	module_version		VARCHAR(255) NOT NULL,	-- 模組版本, 預設係讀取OpenAPI文件中的版本資訊
	module_src		VARCHAR(1) NOT NULL,	-- 建立來源, 1=WSDL, 2=OAS2.0, 3=OAS3.0
	latest			VARCHAR(1) NOT NULL DEFAULT 'N',	-- 是否為最新版本, Y=是, N=否；同一個模組名稱只能有一個最新版本，新增資料時須同時維護此欄位
	upload_date_time	DATETIME NOT NULL,	-- 上傳時間
	upload_user		VARCHAR(255) NOT NULL, 	-- 上傳人員
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user		VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_date_time	DATETIME,	-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user		VARCHAR(255),	-- 更新人員
	version			INT DEFAULT 1,	-- 版號: C/U時, 增量+1
	PRIMARY KEY(reg_module_id),
	UNIQUE (module_name, module_version)
);

-- TSMP .net API詳細資料
CREATE TABLE IF NOT EXISTS tsmpn_api_detail (
	id					BIGINT NOT NULL,
	api_module_id		BIGINT NOT NULL,
	api_key				VARCHAR(255) NOT NULL,
	api_name			VARCHAR(255) NOT NULL,
	path_of_json		VARCHAR(255) NOT NULL,
	method_of_json		VARCHAR(255) NOT NULL,
	params_of_json		VARCHAR(255) NOT NULL,
	headers_of_json		VARCHAR(255) NOT NULL,
	consumes_of_json	VARCHAR(255) NOT NULL,
	produces_of_json	VARCHAR(255) NOT NULL,
	url_rid				CHAR(1) NOT NULL,	-- URL有ResourceID("0": 沒有(default); "1":有) 
	PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS index_tsmpn_api_detail_01 ON tsmpn_api_detail (api_key);

--	TSMP NODE 紀錄								
CREATE TABLE IF NOT EXISTS tsmp_node (
	id				VARCHAR(255) NOT NULL,	--	TSMP node name	host/ip/random-code
	start_time		DATETIME NOT NULL,		--	TSMP node 啟動時間	
	update_time		DATETIME NOT NULL,		--	TSMP node 更新時間	
	node			VARCHAR(30),			--	TSMP node alias	Properties中的"tsmp.core.node.alias"
	PRIMARY KEY (id)
);

--	TSMP Heartbeat 紀錄								
CREATE TABLE IF NOT EXISTS tsmp_heartbeat (
	node_id			VARCHAR(30)		NOT NULL,	--	TSMP node name	Properties中的Node Name設定值。(若Node Name相同，則更新到同一筆資料)
	start_time		DATETIME	NOT NULL,		--	TSMP node 啟動時間	
	update_time		DATETIME	NOT NULL,		--	TSMP node 更新時間	
	node_info		VARCHAR(100),				--	額外資訊	
	PRIMARY KEY (node_id)
);

-- TSMP .net NODE 任務表       
CREATE TABLE IF NOT EXISTS  tsmpn_node_task (      
 	id 				BIGINT 			NOT NULL,
 	task_signature 	VARCHAR(255) 	NOT NULL, 	-- 任務簽章
	 task_id 		VARCHAR(255) 	NOT NULL, 	-- 任務每次送出時的識別碼
 	task_arg 		VARCHAR(4095) 	NOT NULL, 	-- 任務的參數
 	coordination 	VARCHAR(255) 	NOT NULL, 	-- 任務的協調性
 	execute_time 	DATETIME 		NOT NULL, 	-- 任務要執行的時間
 	notice_node 	VARCHAR(255) 	NOT NULL, 	-- 任務是由哪一個TSMP node送出的
 	notice_time 	DATETIME 		NOT NULL, 	-- 任務送出時間
 	node 			VARCHAR(30) , 				-- TSMP Node Alias Properties中的"tsmp.core.node.alias"
 	PRIMARY KEY (id),
 	UNIQUE (task_signature, task_id)
);

-- TSMP .net NODE 任務工作表							
CREATE TABLE IF NOT EXISTS tsmpn_node_task_work (						
	id					BIGINT			NOT NULL,
	node_task_id		BIGINT			NOT NULL,	-- TSMP_NODE_TASK ID
	competitive_id		VARCHAR(255)	NOT NULL,	-- TSMP node 競爭ID
	competitive_time	DATETIME		NOT NULL,	-- TSMP node 競爭時間
	competitive_node	VARCHAR(255)	NOT NULL,	-- TSMP node name
	update_time			DATETIME		NOT NULL,	-- TSMP node 執行該任務會一直update時間, 直到結束(success: true/false)
	success				BOOLEAN,					-- TSMP node 執行該任務成功與否
	error_msg			VARCHAR(1023),				-- TSMP node 執行該任務錯誤時之訊息
	node				VARCHAR(30),				-- TSMP Node Alias	Properties中的"tsmp.core.node.alias" 
	PRIMARY KEY (id),					
	UNIQUE (node_task_id, competitive_id)					
);

--tsmp_report_url
CREATE TABLE IF NOT EXISTS tsmp_report_url (
    REPORT_ID NVARCHAR(8) NOT NULL,
    TIME_RANGE CHAR(1)  NOT NULL ,
    REPORT_URL     NVARCHAR(2000)  NOT NULL,
    UNIQUE (REPORT_ID,TIME_RANGE)
);

-- 常見問題-問題
CREATE TABLE IF NOT EXISTS tsmp_dp_faq_question (
	question_id			BIGINT AUTO_INCREMENT NOT NULL,	-- ID(流水號)
	question_name		NVARCHAR(4000) NOT NULL,	-- 問題題目
	question_name_en	NVARCHAR(4000) NULL,	-- 問題題目英文: 以防會需要多國，先預開
	data_sort			INT NULL,	-- 資料排序
	data_status			CHAR(1) NOT NULL DEFAULT '1',	-- 資料狀態: 1=啟用，0=停用(預設啟用)
	create_time			DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_time			DATETIME NOT NULL,	-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),	-- 更新人員
	version				INT DEFAULT 1,	-- 版號: C/U時, 增量+1
	keyword_search		NVARCHAR(8000),	-- LikeSearch使用: 問題題目|問題題目英文
	PRIMARY KEY (question_id)
);

-- 常見問題-答案
CREATE TABLE IF NOT EXISTS tsmp_dp_faq_answer (
	answer_id			BIGINT AUTO_INCREMENT NOT NULL,	-- ID(流水號)
	answer_name			NVARCHAR(4000) NOT NULL,	-- 問題題目答案
	answer_name_en		NVARCHAR(4000) NULL,	-- 問題題目答案英文: 以防會需要多國，先預開
	ref_question_id		BIGINT NOT NULL,	-- 參照QUESTION_ID: 關連TSMP_DP_FAQ_QUESTION.QUESTION_ID
	create_time			DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_time			DATETIME NOT NULL,	-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),	-- 更新人員
	version				INT DEFAULT 1,	-- 版號: C/U時, 增量+1
	keyword_search		NVARCHAR(8000),	-- LikeSearch使用: 問題題目答案|問題題目答案英文
	PRIMARY KEY (answer_id)
);

-- 關於網站
CREATE TABLE IF NOT EXISTS tsmp_dp_about (
    seq_id                BIGINT AUTO_INCREMENT NOT NULL,    -- ID(流水號)
    about_subject        NVARCHAR(100) NOT NULL,    -- 標題
    about_desc            NVARCHAR(4000) NOT NULL,    -- 描述
    create_time            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,    -- 建立日期: 資料初始建立的人、日期和時間
    create_user            VARCHAR(255) DEFAULT 'SYSTEM',    -- 建立人員
    update_time            DATETIME,    -- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
    update_user            VARCHAR(255),    -- 更新人員
    version                INT DEFAULT 1,    -- 版號: C/U時, 增量+1
    PRIMARY KEY (seq_id)
);

-- 網站地圖
CREATE TABLE IF NOT EXISTS tsmp_dp_site_map (
    site_id                BIGINT AUTO_INCREMENT NOT NULL,    -- ID(流水號)
    site_parent_id        BIGINT NOT NULL,    -- 父節點
    site_desc            NVARCHAR(200) NOT NULL,    -- 節點名稱
    data_sort            INT NOT NULL,    -- 資料排序
    site_url            NVARCHAR(200) NULL,    -- 網站連結
    create_time            DATETIME DEFAULT CURRENT_TIMESTAMP,    -- 建立日期: 資料初始建立的人、日期和時間
    create_user            VARCHAR(255) DEFAULT 'SYSTEM',    -- 建立人員
    update_time            DATETIME,    -- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
    update_user            VARCHAR(255),    -- 更新人員
    version                INT DEFAULT 1,    -- 版號: C/U時, 增量+1
    PRIMARY KEY (site_id)
);

-- DP拒絶開放之module API-DOCS	
CREATE TABLE IF NOT EXISTS tsmp_dp_denied_module (
	ref_module_name		VARCHAR(255) NOT NULL,	-- 參考 TSMP_API_MODULE. MODULE_NAME
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	PRIMARY KEY (ref_module_name)
);

-- 公告區/最新消息(內容可以是 URL 使自己 Entity參考自改, 用來組成一份完整的 HTML)
CREATE TABLE IF NOT EXISTS tsmp_dp_news (
	news_id				BIGINT NOT NULL,	-- ID(流水號)
	new_title			VARCHAR(100) NOT NULL DEFAULT '_',	-- 標題
	new_content			VARCHAR(3072) NOT NULL,	-- 內容
	status				VARCHAR(1) NOT NULL DEFAULT '1',	-- 啟用狀態(1：啟用,0：停用(預設啟用))
	org_id				VARCHAR(255) NOT NULL, -- 組織代碼(可以用來說明發佈機關)	
	post_date_time		DATETIME NOT NULL, -- 公告日期(前台依此做為查詢條件, 必需到期才能show出資料)
	ref_type_subitem_no VARCHAR(20) NOT NULL, -- 公告類型代碼(EX:上架/下架...etc, ITEM_NO="NEWS_TYPE", from TSMP_DP_ITEMS.SUBITEM_NO)
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_date_time	DATETIME,	-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),	-- 更新人員
	version				INT DEFAULT 1,	-- 版號: C/U時, 增量+1
	keyword_search		VARCHAR(2148),	-- LikeSearch使用: 標題 | 內容
	PRIMARY KEY (news_id)
);

-- 客製資料設定
CREATE TABLE IF NOT EXISTS cus_setting (
	cus_setting_id		BIGINT NOT NULL,	-- ID(流水號)(做為前端Bcrypt 尾碼Index使用)
	setting_no			VARCHAR(20) NOT NULL,	-- 分頁編號(做為群組的代碼, 同一群組中需要程式保證 NAME值為相同)
	setting_name		VARCHAR(100) NOT NULL,	-- 分類名稱(做為群組的代碼, 同一群組中需要程式保證 NAME值為相同)
	subsetting_no		VARCHAR(20) NOT NULL,	-- 子分類編號
	subsetting_name		VARCHAR(100) NOT NULL,	-- 子分類名稱	
	sort_by				INT NOT NULL DEFAULT 0,
	is_default			VARCHAR(1),	-- 是否為選單中的default select(V：select，null：deselect)
	param1				VARCHAR(255),	-- 參數1
	param2				VARCHAR(255),	-- 參數2
	param3				VARCHAR(255),	-- 參數3 	
	param4				VARCHAR(255),	-- 參數4
	param5				VARCHAR(255),	-- 參數5
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_date_time	DATETIME,	-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),	-- 更新人員
	version				INT DEFAULT 1,	-- 版號: C/U時, 增量+1
	keyword_search		NVARCHAR(200),	-- LikeSearch使用: 分類名稱 | 子分類名稱
	PRIMARY KEY (setting_no, subsetting_no)
);

-- 應用實例分類
CREATE TABLE IF NOT EXISTS tsmp_dp_app_category (
	app_cate_id			BIGINT AUTO_INCREMENT NOT NULL,	-- ID(流水號)
	app_cate_name		NVARCHAR(100) NOT NULL,	-- 分類名稱
	data_sort			INT,	-- 資料排序
	org_id				VARCHAR(255) NULL,	-- 組織單位ID: 不可重複組織單位識別碼, null 表示全部User都可看到
	create_time			DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_time			DATETIME,	-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),	-- 更新人員
	version				INT DEFAULT 1,	-- 版號: C/U時, 增量+1
	keyword_search		NVARCHAR(100),	-- LikeSearch使用: 分類名稱
	PRIMARY KEY (app_cate_id)
);

-- 應用實例內容
CREATE TABLE IF NOT EXISTS tsmp_dp_app (
	app_id				BIGINT AUTO_INCREMENT NOT NULL,	-- ID(流水號)
	ref_app_cate_id		BIGINT NOT NULL,	-- 應用實例分類ID: 關連TSMP_DP_APP_CATEGORY.APP_CATE_ID
	name				NVARCHAR(100) NOT NULL,	-- 應用實例名稱
	intro				NVARCHAR(4000) NOT NULL,	-- 作品介紹
	author				NVARCHAR(100),	-- 作者/機關
	data_status			CHAR(1) NOT NULL,	--  資料狀態: 1=啟用, 0=停用(預設啟用)
	org_id				VARCHAR(255) NULL,	-- 組織單位ID: 不可重複組織單位識別碼, null 表示全部User都可看到
	create_time			DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_time			DATETIME,	-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),	-- 更新人員
	version				INT DEFAULT 1,	-- 版號: C/U時, 增量+1
	keyword_search		NVARCHAR(100),	-- LikeSearch使用: 作品介紹|作者/機關
	PRIMARY KEY (app_id)
);

-- 應用實例與API對應檔
CREATE TABLE IF NOT EXISTS tsmp_dp_api_app (
	ref_app_id			BIGINT NOT NULL,	-- ID不自動產生: from TSMP_DP_APP
	ref_api_uid			VARCHAR(36) NOT NULL,	-- API UUID: from TSMP_API.API_UID, 將uuid以base64轉換後最小化, 非SQL Server以String存放, SQL Server以它的型態存放.
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	PRIMARY KEY (ref_app_id, ref_api_uid)
);

-- TSMP API 基本資料
CREATE TABLE IF NOT EXISTS tsmp_api (
	api_key			VARCHAR(30) NOT NULL,	-- API代碼
	module_name		VARCHAR(100) NOT NULL,	-- ex: "default"
	api_name		VARCHAR(30) NOT NULL,	-- API名稱
	api_status		CHAR(1) NOT NULL,	-- API狀態: 1=Enabled, 2=Disabled (新增預設為Disabled)
	api_src			CHAR(1) NOT NULL,	-- API來源: M=Module(Default);R=Registerd;C=Composed;N=.Net;
	api_desc		VARCHAR(300) NULL,	-- 說明
	api_owner		VARCHAR(100) NULL,	-- 擁有者: (文字輸入，給管理者看的)
	create_time		DATETIME NOT NULL,	-- 建立時間
	create_user		VARCHAR(255) NOT NULL,	-- 建立人員
	update_time		DATETIME NULL,	-- 更新時間
	update_user		VARCHAR(255) NULL,	-- 更新人員
	org_id			VARCHAR(255) NULL,	-- 組織單位ID: 關連TSMP_ORGANIZATION.ORG_ID
	public_flag		CHAR(1) NULL,	-- 是否對公眾開放=0：全部，1：公開，2：私有，null：私有
	src_url			NVARCHAR(255),	-- 來源URL
	api_uid			VARCHAR(36) NULL,	-- API UUID: 新增資料時給予一筆, 非SQL Server以String存放, SQL Server以它的型態存放
	data_format		CHAR(1) NULL DEFAULT '1',	-- API格式: 1=JSON, 0=SOAP, null=JSON
	jwe_flag        CHAR(1) NULL,            -- JWE格式: 0：不使用，1：JWE，2：JWS，null:不使用
	jwe_flag_resp    CHAR(1) NULL,              -- JWE格式: 0：不使用，1：JWE，2：JWS，null:不使用
	mock_status_code 	CHAR(3) Null, -- Mock 狀態
	mock_headers 	VARCHAR(2000) Null, -- Mock Header
	mock_body 		NVARCHAR(2000) Null, -- Mock Body
	PRIMARY KEY (api_key, module_name)
);

INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('SERVICE_MAIL_ENABLE', 'true', 'Main SMTP server setting : Sender authentication process enablement ( true/false )');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('SERVICE_MAIL_HOST', 'smtp.gmail.com', 'Main SMTP server setting : SMTP server domain name ( smtp.gmail.com )');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('SERVICE_MAIL_PORT', '587', 'Main SMTP server setting : SMTP server service-port');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('SERVICE_MAIL_AUTH', 'true', 'Main SMTP server setting : Sender authentication enablement ( true/false )');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('SERVICE_MAIL_STARTTLS_ENABLE', 'true', 'Main SMTP server setting : TLS enablement ( true/false )');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('SERVICE_MAIL_USERNAME', 'system@elite-erp.com.tw', 'Main SMTP server setting : Sender display name');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('SERVICE_MAIL_PASSWORD', 'eliteTpower', 'Main SMTP server setting : Sender email password');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('SERVICE_MAIL_FROM', 'system@elite-erp.com.tw', 'Main SMTP server setting : Sender email address ( system@elite-erp.com.tw )');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('SERVICE_MAIL_X_MAILER', 'Thinkpower', 'Main SMTP server setting : mail delivery process name');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('SERVICE_SECONDARY_MAIL_ENABLE', 'true', 'Secondary SMTP server setting : Sender authentication process enablement ( true/false )');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('SERVICE_SECONDARY_MAIL_HOST', 'smtp.gmail.com', 'Secondary SMTP server setting : SMTP server domain name ( smtp.gmail.com )');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('SERVICE_SECONDARY_MAIL_PORT', '587', 'Secondary SMTP server setting : SMTP server service-port');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('SERVICE_SECONDARY_MAIL_AUTH', 'true', 'Secondary SMTP server setting : Sender authentication enablement ( true/false )');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('SERVICE_SECONDARY_MAIL_STARTTLS_ENABLE', 'true', 'Secondary SMTP server setting : TLS enablement ( true/false )');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('SERVICE_SECONDARY_MAIL_USERNAME', 'system@elite-erp.com.tw', 'Secondary SMTP server setting : Sender display name');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('SERVICE_SECONDARY_MAIL_PASSWORD', 'eliteTpower', 'Secondary SMTP server setting : Sender email password');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('SERVICE_SECONDARY_MAIL_FROM', 'system@elite-erp.com.tw', 'Secondary SMTP server setting : Sender email address ( system@elite-erp.com.tw )');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('SERVICE_SECONDARY_MAIL_X_MAILER', 'Thinkpower', 'Secondary SMTP server setting : mail delivery process name');
-- 由 initSQL 執行
--INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('TSMP_EDITION', 'Pn88-nq8x-x8ux-88ng-en68-x#cx-=88#-x2=x-#F=x-#F=F-#x=', 'TSMP license key');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('LDAP_URL', 'ldap://10.20.30.162:389', 'ldap login URL');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('LDAP_DN', 'uid={{0}},dc=tstpi,dc=com', 'ldap login user DN');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('LDAP_TIMEOUT', '3000', 'Connection timeout for ldap login, in milliseconds(ms)');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('LDAP_CHECK_ACCT_ENABLE', 'false', 'LDAP check account function enablement - true/false');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('TSMP_FAIL_THRESHOLD', '6', 'Allowed "User password" fail THRESHOLD');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('SSO_PKCE', 'true', 'PKCE Level AuthCode verification enablement - true/false');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('SSO_DOUBLE_CHECK', 'true', 'Enablement of Double-check verification - true/false');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('SSO_AUTO_CREATE_USER', 'false', 'Enablement of SSO auto - user account creation - true/false');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('TSMP_DPAA_RUNLOOP_INTERVAL', '1', 'The RUNLOOP sett for DPAA, if it is set to 0, the alarm detection will be disabled');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('TSMP_DELETEMODULE_ALERT', 'false', 'Enable the Prompt when deleting MODULE - true/false');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('TSMP_APILOG_FORCE_WRITE_RDB', 'false', 'Write API log into RDB enablement ( true/false )');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('DGR_LOGOUT_URL', '', 'Customized Login Page URL{{scheme}}://{{ip}}:{{port}}/{{path}} ex: "https://203.69.248.109:38452/dgr-cus-scbank-ac/"');
--INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('TSMP_COMPOSER_ADDRESS', 'http://tsmp-composer:1880', 'COMPOSER broadcasting, separated by commas');
--INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('TSMP_COMPOSER_PORT', '48080', 'The Composer access port used for AC');
--INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('TSMP_COMPOSER_PATH', '/editor/tsmpApi', 'The Composer path used for AC');
--COMPOSER SERVER used locally with proxy
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('TSMP_COMPOSER_ADDRESS', 'https://127.0.0.1:8440', 'COMPOSER broadcasting, separated by commas');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('TSMP_COMPOSER_PORT', '8080', 'The Composer access port used for AC');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('TSMP_COMPOSER_PATH', '/website/composer', 'The Composer path used for AC');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('TSMP_PROXY_PORT', '4944', 'Currently inused ; Reserved as the functionality - TSMP_REPORT_ADDRESS');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('CUS_MODULE_EXIST', 'false', '"Customized Module" exist indicator (true/false)');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('CUS_FUNC_ENABLE1', 'false', '"Customized Function" enablement (true/false)');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('CUS_MODULE_NAME1', '', 'Customized Module name');
-- Different settings for each server
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('UDPSSO_LOGIN_NETWORK', '192.168.0.0/23,192.168.0.0/24,127.0.0.0/24', 'Network segments that can be logged in, multiple CIDRs are separated by commas');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('CLIENT_CREDENTIALS_DEFAULT_USERNAME', 'true', 'client_credentials get token "default userName" enablement (true/false)');

DELETE FROM tsmp_dp_mail_tplt WHERE mailtplt_id BETWEEN 10000000 AND 10000050;
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000000, 'subject.member-pass', 'API Portal - User Account Application Notification', 'DPB0005', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000001, 'body.member-pass', '<p><span style="font-weight: 400;">Dear Applicant,</span></p><p><span style="font-weight: 400;">This email is to inform you that the API Client account you applied for API Portal is Approved.</span></p><p>&nbsp;</p><p><span style="font-weight: 400;">Please use the username / password you created to log in.</span></p><p>&nbsp;</p><p><span style="font-weight: 400;">username:{{clientId}}</span></p>Thank you.', 'DPB0005', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000002, 'body.member-fail', '<p><span style="font-weight: 400;">API 用戶您好，</span></p><p><span style="font-weight: 400;">此封信為通知您於 API Portal 申請的 API 用戶帳號結果為不核准。</span></p><p><span style="font-weight: 400;">若有疑問，請聯絡網站管理員。</span></p><p><span style="font-weight: 400;">謝謝。</span></p>', 'DPB0005', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000003, 'subject.api-pass', 'API Portal - API Authorization Notification', 'DPB0002', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000004, 'body.api-pass', '<p><span style="font-weight: 400;">Dear Applicant,</span></p><p><span style="font-weight: 400;">This email is to inform you that your application for API authorization is Approved.</span></p><p>&nbsp;</p><span style="font-weight: 400;">Detail:</span><table> <tbody>{{data-list}}</tbody></table><p><span style="font-weight: 400;">Thank you.</span></p>', 'DPB0002', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000005, 'body.api-pass.list', '<tr><td>API Name:</td><td>{{apiName}}</td></tr><tr> <td>API ID:</td><td>{{apiKey}}</td></tr>', 'DPB0002', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000006, 'subject.api-fail', 'API Portal - API 授權申請通知', 'DPB0002', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000007, 'body.api-fail', '<p><span style="font-weight: 400;">API 用戶您好，</span></p><p><span style="font-weight: 400;">此封信為通知您於 API Portal 申請的 API 授權結果為失敗。</span></p><p>&nbsp;</p><p><span style="font-weight: 400;">申請明細：</span></p><table> <tbody>{{data-list}}</tbody></table><p>&nbsp;</p><p><span style="font-weight: 400;">若有疑問，請聯絡網站管理員。</span></p><p>&nbsp;</p><p><span style="font-weight: 400;">謝謝。</span></p>', 'DPB0002', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000008, 'body.api-fail.list', '<tr><td style="border: 1px solid black; font-family: Microsoft JhengHei, Arial, sans-serif;">{{apiKey}}</td><td style="border: 1px solid black; font-family: Microsoft JhengHei, Arial, sans-serif;">{{moduleName}}</td><td style="border: 1px solid black; font-family: Microsoft JhengHei, Arial, sans-serif;">{{apiName}}</td><td style="border: 1px solid black; font-family: Microsoft JhengHei, Arial, sans-serif;">{{apiDesc}}</td><td style="border: 1px solid black; font-family: Microsoft JhengHei, Arial, sans-serif;">{{reviewRemark}}</td></tr>', 'DPB0002', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000009, 'subject.revi-wait', '【{{projectName}}】{{reqOrderNo}}:{{reviewType}}簽核通知', 'DPB0065,DPB0066,DPB0071', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000010, 'body.revi-wait', '<p style="font-family: Microsoft JhengHei, Arial, sans-serif;">您有一筆【{{reviewType}}】作業待簽核案件通知</p> <p style="font-family: Microsoft JhengHei, Arial, sans-serif;">案件編號&nbsp;{{reqOrderNo}}</p> <p style="font-family: Microsoft JhengHei, Arial, sans-serif;">申請日期&nbsp;<time>{{createDateTime}}</time></p> <p style="font-family: Microsoft JhengHei, Arial, sans-serif;">申請人員 {{applyUserName}}</p> <p style="font-family: Microsoft JhengHei, Arial, sans-serif;">申請單位 {{orgName}}</p> <p style="font-family: Microsoft JhengHei, Arial, sans-serif;">申請項目 {{subTitle}}</p> <p style="font-family: Microsoft JhengHei, Arial, sans-serif;">簽核狀態 {{chkStatusName}}/{{chkPointName}}簽核</p> <table style="border: 1px solid black; border-collapse: collapse;"><caption style="text-align: right; font-family: Microsoft JhengHei, Arial, sans-serif;">&#42;API開放狀態：{{publicFlagName}}</caption><thead> <tr> <th style="border: 1px solid black; font-family: Microsoft JhengHei, Arial, sans-serif;">&nbsp; &nbsp; &nbsp;&nbsp;</th> <th style="border: 1px solid black; font-family: Microsoft JhengHei, Arial, sans-serif;">API名稱</th> <th style="border: 1px solid black; font-family: Microsoft JhengHei, Arial, sans-serif;">主題名稱</th> <th style="border: 1px solid black; font-family: Microsoft JhengHei, Arial, sans-serif;"> <p>API說明文件</p> </th> </tr> {{data-list}} </thead> </table> <p style="font-family: Microsoft JhengHei, Arial, sans-serif;">&nbsp;</p>', 'DPB0065,DPB0066,DPB0071', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000011, 'body.revi-wait.list', '<tr style="height: 45px;"> <th style="border: 1px solid black; font-family: Microsoft JhengHei, Arial, sans-serif; width: 24px; height: 45px;">{{index}}</th> <th style="border: 1px solid black; font-family: Microsoft JhengHei, Arial, sans-serif; width: 81px; height: 45px;">{{apiName}}</th> <th style="border: 1px solid black; font-family: Microsoft JhengHei, Arial, sans-serif; width: 104px; height: 45px;">{{themeName}}</th> <th style="border: 1px solid black; font-family: Microsoft JhengHei, Arial, sans-serif; width: 160.667px; height: 45px;"> <p>{{docFileName}}</p> </th> </tr>', 'DPB0065,DPB0066,DPB0071', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000012, 'subject.revi-wait.D1', '【{{projectName}}】{{reqOrderNo}}:{{reviewType}}簽核通知', 'DPB0065,DPB0066,DPB0071', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000013, 'body.revi-wait.D1', '<p style="font-family: Microsoft JhengHei, Arial, sans-serif;">您有一筆【{{reviewType}}】作業待簽核案件通知</p> <p style="font-family: Microsoft JhengHei, Arial, sans-serif;">案件編號&nbsp;{{reqOrderNo}}</p> <p style="font-family: Microsoft JhengHei, Arial, sans-serif;">申請日期&nbsp;<time>{{createDateTime}}</time></p> <p style="font-family: Microsoft JhengHei, Arial, sans-serif;">申請人員 {{applyUserName}}</p> <p style="font-family: Microsoft JhengHei, Arial, sans-serif;">申請單位 {{orgName}}</p> <p style="font-family: Microsoft JhengHei, Arial, sans-serif;">簽核狀態 {{chkStatusName}}/{{chkPointName}}簽核</p>[[$D, clientApiMappings, '''']]<p style="font-family: Microsoft JhengHei, Arial, sans-serif;">&nbsp;</p>', 'DPB0065,DPB0066,DPB0071', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000014, 'body.revi-wait.D1.D', '<table style="border: 1px solid lightgray; border-collapse: collapse"><tbody><tr style="text-align: center; color: white; background-color: #f29500"><td style="{{^TD}}">用戶端帳號</td><td style="{{^TD}}">用戶端代號</td><td style="{{^TD}}">用戶端名稱</td></tr><tr style="text-align: center; color: white; background-color: #f29500"><td style="{{^TD}}" colspan="3">API名稱</td></tr><tr style="text-align: center"><td style="{{^TD}}">{{clientId}}</td><td style="{{^TD}}">{{clientName}}</td><td style="{{^TD}}">{{clientAlias}}</td></tr><tr><td style="{{^TD}}" colspan="3">[[apiNames, '', '']]</td></tr></tbody></table>', 'DPB0065,DPB0066,DPB0071', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000015, 'body.revi-wait.D1.TD', 'border: 1px solid lightgray; font-family: Microsoft JhengHei, Arial, sans-serif; padding: 0.3em', 'DPB0065,DPB0066,DPB0071', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000016, 'subject.revi-wait.D3', '【{{projectName}}】{{reqOrderNo}}:{{reviewType}}簽核通知', 'DPB0065,DPB0066,DPB0071', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000017, 'body.revi-wait.D3', '<p style="font-family: Microsoft JhengHei, Arial, sans-serif;">您有一筆【{{reviewType}}】作業待簽核案件通知</p> <p style="font-family: Microsoft JhengHei, Arial, sans-serif;">案件編號&nbsp;{{reqOrderNo}}</p> <p style="font-family: Microsoft JhengHei, Arial, sans-serif;">申請日期&nbsp;<time>{{createDateTime}}</time></p> <p style="font-family: Microsoft JhengHei, Arial, sans-serif;">申請人員 {{applyUserName}}</p> <p style="font-family: Microsoft JhengHei, Arial, sans-serif;">申請單位 {{orgName}}</p> <p style="font-family: Microsoft JhengHei, Arial, sans-serif;">簽核狀態 {{chkStatusName}}/{{chkPointName}}簽核</p>{{$D, detail}}<p style="font-family: Microsoft JhengHei, Arial, sans-serif;">&nbsp;</p>', 'DPB0065,DPB0066,DPB0071', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000018, 'body.revi-wait.D3.D', '<table style="border: 1px solid lightgray; border-collapse: collapse"><thead><tr style="text-align: center; color: white; background-color: #f29500"><th style="{{^TD}}">用戶端帳號</th><th style="{{^TD}}">用戶端代號</th><th style="{{^TD}}">郵件地址</th><th style="{{^TD}}">開放權限</th></tr></thead><tbody>[[^DL, clientList, '''']]<tbody></table>', 'DPB0065,DPB0066,DPB0071', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000019, 'body.revi-wait.D3.DL', '<tr><td style="{{^TD}}">{{clientId}}</td><td style="{{^TD}}">{{clientName}}</td><td style="{{^TD}}">{{emails}}</td><td style="{{^TD}}">{{publicFlag}}</td></tr>', 'DPB0065,DPB0066,DPB0071', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000020, 'body.revi-wait.D3.TD', 'border: 1px solid lightgray; font-family: Microsoft JhengHei, Arial, sans-serif; padding: 0.3em', 'DPB0065,DPB0066,DPB0071', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000021, 'subject.addUser-pass', '【{{projectName}}】TSMP重要通知Email-TUser', 'AA0001', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000022, 'body.addUser-pass', 'Dear {{tUser}}, <br /><br /> Your digiRunner account has been created.<br />Please log in with the information below:<br />Username: {{tUser}} <br />Password: ***** <br /><br />digiRunner team {{date}}', 'AA0001', current_timestamp, 'SYSTEM', 1);
INSERT INTO TSMP_DP_MAIL_TPLT (MAILTPLT_ID, CODE, TEMPLATE_TXT, REMARK, CREATE_DATE_TIME, CREATE_USER, VERSION) VALUES (10000023, 'subject.updUser-pwd', '【{{projectName}}】TSMP重要通知Email-TUser', 'AA0004', current_timestamp, 'SYSTEM', 1);
INSERT INTO TSMP_DP_MAIL_TPLT (MAILTPLT_ID, CODE, TEMPLATE_TXT, REMARK, CREATE_DATE_TIME, CREATE_USER, VERSION) VALUES (10000024, 'body.updUser-pwd', 'Dear {{tUser}},<br /><br />Your password has been reset.<br />To keep your account secured, please change your password after login.<br />Password: {{newBlock}} <br /><br />digiRunner team{{date}}', 'AA0004', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000025, 'subject.revi-wait.D5', '【{{projectName}}】{{reqOrderNo}}:{{reviewType}}簽核通知', 'DPB0065,DPB0066,DPB0071', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000026, 'body.revi-wait.D5', '<p style="font-family: Microsoft JhengHei, Arial, sans-serif;">您有一筆【{{reviewType}}】作業待簽核案件通知</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">案件編號&nbsp;{{reqOrderNo}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">申請日期&nbsp;<time>{{createDateTime}}</time></p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">申請人員 {{applyUserName}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">申請單位 {{orgName}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">申請項目 {{subTitle}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">簽核狀態 {{chkStatusName}}/{{chkPointName}}簽核</p>[[$D, apiMappings, '''']]<p style="font-family: Microsoft JhengHei, Arial, sans-serif;">&nbsp;</p>', 'DPB0065,DPB0066,DPB0071', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000027, 'body.revi-wait.D5.D', '<table style="border: 1px solid lightgray; border-collapse: collapse"><tbody><tr style="text-align: center; color: white; background-color: #f29500"><td style="{{^TD}}">用戶端帳號</td><td style="{{^TD}}">用戶端代號</td><td style="{{^TD}}">用戶端名稱</td></tr><tr style="text-align: center; color: white; background-color: #f29500"><td style="{{^TD}}" colspan="3">API名稱</td></tr><tr style="text-align: center"><td style="{{^TD}}">{{clientId}}</td><td style="{{^TD}}">{{clientName}}</td><td style="{{^TD}}">{{clientAlias}}</td></tr><tr><td style="{{^TD}}" colspan="3">[[apiNames, '', '']]</td></tr></tbody></table>', 'DPB0065,DPB0066,DPB0071', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000028, 'body.revi-wait.D5.TD', 'border: 1px solid lightgray; font-family: Microsoft JhengHei, Arial, sans-serif; padding: 0.3em', 'DPB0065,DPB0066,DPB0071', current_timestamp, 'SYSTEM', 1);
INSERT INTO TSMP_DP_MAIL_TPLT (MAILTPLT_ID, CODE, TEMPLATE_TXT, REMARK, CREATE_DATE_TIME, CREATE_USER, VERSION) VALUES (10000029, 'subject.add-client', '【{{projectName}}】TSMP重要通知Email - Client', 'AA020', current_timestamp, 'SYSTEM', 1);
INSERT INTO TSMP_DP_MAIL_TPLT (MAILTPLT_ID, CODE, TEMPLATE_TXT, REMARK, CREATE_DATE_TIME, CREATE_USER, VERSION) VALUES (10000030, 'body.add-client', 'Dear {{client}},<br /><br />Your digiRunner account has bee created.<br />Please log in with the information below:<br />Login ID:{{clientID}} <br />User Name:{{client}} <br />Password: {{newBlock}} <br />clientBlock: {{base64Block}}<br />Hosts:{{hosts}}<br />Service Effective Date:{{clientDate}}<br />Service EffectiveTime:{{serviceTime}}<br />API Quota:{{quota}}<br />TPS:{{tps}}<br /><br />digiRunner team {{date}}', 'AA0201', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000031, 'subject.oak-pass', '【{{projectName}}】Open API Key 授權結果通知', '', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000032, 'body.oak-pass', '<p style="font-family: Microsoft JhengHei, Arial, sans-serif;">您有一筆 {{openApiKeyType}}成功，感謝您使用&nbsp;digiRunner&nbsp;為您服務。</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">Open API Key : {{openApiKey}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">Secret Key : {{secretKey}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">Open API Key 別名 : {{openApiKeyAlias}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">使用次數上限 : {{timesThreshold}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">創建時間 : {{createDateTime}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">效期 : {{expiredAt}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">{{revokedAt}}</p>[[$D, apiMappings, '''']]<p style="font-family: Microsoft JhengHei, Arial, sans-serif;">&nbsp;</p>', '', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000033, 'body.oak-pass.D', '<table style="border: 1px solid lightgray; border-collapse: collapse"><tbody><tr style="text-align: center; color: white; background-color: #f29500"><td style="{{^TD}}">用戶端帳號</td><td style="{{^TD}}">用戶端代號</td><td style="{{^TD}}">用戶端名稱</td></tr><tr style="text-align: center; color: white; background-color: #f29500"><td style="{{^TD}}" colspan="3">API名稱</td></tr><tr style="text-align: center"><td style="{{^TD}}">{{clientId}}</td><td style="{{^TD}}">{{clientName}}</td><td style="{{^TD}}">{{clientAlias}}</td></tr><tr><td style="{{^TD}}" colspan="3">[[apiNames, '', '']]</td></tr></tbody></table>', '', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000034, 'body.oak-pass.TD', 'border: 1px solid lightgray; font-family: Microsoft JhengHei, Arial, sans-serif; padding: 0.3em', '', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000035, 'subject.oak-expi', '【{{projectName}}】 API Key 即將到期通知', '', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000036, 'body.oak-expi', '<p style="font-family: Microsoft JhengHei, Arial, sans-serif;">您有一筆 API Key 即將到期，若要延長效期，請點以下連結:</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;"><a href="{{oakExpiUrl}}/{{par1}}/{{par2}}">立即延長效期</a></p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;"> API Key : {{openApiKey}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">Secret Key : {{secretKey}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;"> API Key 別名 : {{openApiKeyAlias}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">使用次數上限 : {{timesThreshold}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">創建時間 : {{createDateTime}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">效期 : {{expiredAt}}</p><p>[[$D, apiMappings, '''']]</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">&nbsp;</p>', '', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000037, 'body.oak-expi.D', '<table style="border: 1px solid lightgray; border-collapse: collapse"><tbody><tr style="text-align: center; color: white; background-color: #f29500"><td style="{{^TD}}">用戶端帳號</td><td style="{{^TD}}">用戶端代號</td><td style="{{^TD}}">用戶端名稱</td></tr><tr style="text-align: center; color: white; background-color: #f29500"><td style="{{^TD}}" colspan="3">API名稱</td></tr><tr style="text-align: center"><td style="{{^TD}}">{{clientId}}</td><td style="{{^TD}}">{{clientName}}</td><td style="{{^TD}}">{{clientAlias}}</td></tr><tr><td style="{{^TD}}" colspan="3">[[apiNames, '', '']]</td></tr></tbody></table>', '', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000038, 'body.oak-expi.TD', 'border: 1px solid lightgray; font-family: Microsoft JhengHei, Arial, sans-serif; padding: 0.3em', '', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000039, 'subject.oakRoll-pass', '【{{projectName}}】Open API Key 展期結果通知', 'DPF0045', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000040, 'body.oakRoll-pass', '<p style="font-family: Microsoft JhengHei, Arial, sans-serif;">您有一筆 {{openApiKeyType}}成功，感謝您使用&nbsp;digiRunner&nbsp;為您服務。</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">Open API Key : {{openApiKey}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">Secret Key : {{secretKey}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">Open API Key 別名 : {{openApiKeyAlias}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">使用次數上限 : {{timesThreshold}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">創建時間 : {{createDateTime}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">效期 : {{expiredAt}}</p>[[$D, apiMappings, '''']]<p style="font-family: Microsoft JhengHei, Arial, sans-serif;">&nbsp;</p>', 'DPF0045', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000041, 'body.oakRoll-pass.D', '<table style="border: 1px solid lightgray; border-collapse: collapse"><tbody><tr style="text-align: center; color: white; background-color: #f29500"><td style="{{^TD}}">用戶端帳號</td><td style="{{^TD}}">用戶端代號</td><td style="{{^TD}}">用戶端名稱</td></tr><tr style="text-align: center; color: white; background-color: #f29500"><td style="{{^TD}}" colspan="3">API名稱</td></tr><tr style="text-align: center"><td style="{{^TD}}">{{clientId}}</td><td style="{{^TD}}">{{clientName}}</td><td style="{{^TD}}">{{clientAlias}}</td></tr><tr><td style="{{^TD}}" colspan="3">[[apiNames, '', '']]</td></tr></tbody></table>', 'DPF0045', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000042, 'body.oakRoll-pass.TD', 'border: 1px solid lightgray; font-family: Microsoft JhengHei, Arial, sans-serif; padding: 0.3em', 'DPF0045', current_timestamp, 'SYSTEM', 1);
INSERT INTO TSMP_DP_MAIL_TPLT (MAILTPLT_ID, CODE, TEMPLATE_TXT, REMARK, CREATE_DATE_TIME, CREATE_USER, VERSION) VALUES (10000043, 'subject.updClie-pwd', '【{{projectName}}】TSMP重要通知Email - Client', 'AA0231', current_timestamp, 'SYSTEM', 1);
INSERT INTO TSMP_DP_MAIL_TPLT (MAILTPLT_ID, CODE, TEMPLATE_TXT, REMARK, CREATE_DATE_TIME, CREATE_USER, VERSION) VALUES (10000044, 'body.updClie-pwd', '{{client}} 您好:<br /><br />您的用戶端密碼Client Password已經重設。<br />為確保安全，請用新的clientBlock重新設定您的clientBlock後再使用。<br />client ID:{{clientID}} <br />client Password: {{newBlock}} <br />clientBlock: {{base64Block}}<br />TSMP {{date}}', 'AA0231', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000045, 'body.oak-expi2', '<p style="font-family: Microsoft JhengHei, Arial, sans-serif;">您有一筆 API Key 即將到期，若要延長效期，請逕洽系統管理員。</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">API Key : {{openApiKey}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">Secret Key : {{secretKey}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">API Key 別名 : {{openApiKeyAlias}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">使用次數上限 : {{timesThreshold}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">創建時間 : {{createDateTime}}</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">效期 : {{expiredAt}}</p><p>[[$D, apiMappings, '''']]</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif;">&nbsp;</p>', '', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000046, 'body.oak-expi2.D', '<table style="border: 1px solid lightgray; border-collapse: collapse"><tbody><tr style="text-align: center; color: white; background-color: #f29500"><td style="{{^TD}}">用戶端帳號</td><td style="{{^TD}}">用戶端代號</td><td style="{{^TD}}">用戶端名稱</td></tr><tr style="text-align: center; color: white; background-color: #f29500"><td style="{{^TD}}" colspan="3">API名稱</td></tr><tr style="text-align: center"><td style="{{^TD}}">{{clientId}}</td><td style="{{^TD}}">{{clientName}}</td><td style="{{^TD}}">{{clientAlias}}</td></tr><tr><td style="{{^TD}}" colspan="3">[[apiNames, '', '']]</td></tr></tbody></table>', '', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000047, 'body.oak-expi2.TD', 'border: 1px solid lightgray; font-family: Microsoft JhengHei, Arial, sans-serif; padding: 0.3em', '', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000048, 'subject.cert-exp', '憑證到期通知', 'NoticeExpCertJob', current_timestamp, 'SYSTEM', 1);
INSERT INTO tsmp_dp_mail_tplt (mailtplt_id, code, template_txt, remark, create_date_time, create_user, version) VALUES (10000049, 'body.cert-exp', '<p style="font-family: Microsoft JhengHei, Arial, sans-serif;">親愛的&nbsp;digiRunner&nbsp;用戶您好：</p><p style="font-family: Microsoft JhengHei, Arial, sans-serif; padding-left: 2em">提醒您，<i>{{clientId}}</i>&nbsp;之&nbsp;{{certType}}&nbsp;憑證&nbsp;-&nbsp;<i>{{certFileName}}</i>&nbsp;將於&nbsp;<i><b style="color: red">{{expiredDate}}</b></i>&nbsp;到期。</p>', 'NoticeExpCertJob', current_timestamp, 'SYSTEM', 1);
UPDATE tsmp_dp_mail_tplt SET keyword_search = code || '|' || template_txt || '|' || remark WHERE code IS NOT NULL;

INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1101', 'en-US', 'No application instance classification', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1101', 'zh-TW', '查無應用實例分類', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1102', 'en-US', 'No application instance', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1102', 'zh-TW', '查無應用實例', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1103', 'en-US', 'No incoming category Id', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1103', 'zh-TW', '沒有傳入分類Id', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1104', 'en-US', 'No subject classification', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1104', 'zh-TW', '查無主題分類', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1105', 'en-US', 'No subject API content', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1105', 'zh-TW', '查無主題API內容', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1106', 'en-US', 'No subject Id', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1106', 'zh-TW', '沒有傳入主題Id', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1107', 'en-US', 'No basic API information', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1107', 'zh-TW', '查無API基本資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1108', 'en-US', 'No API details', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1108', 'zh-TW', '查無API明細資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1109', 'en-US', 'No API theme information', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1109', 'zh-TW', '查無API主題資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1110', 'en-US', 'No API Module information', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1110', 'zh-TW', '查無API Module資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1111', 'en-US', 'No API organization information', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1111', 'zh-TW', '查無API 組織資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1112', 'en-US', 'No relevant information on application examples', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1112', 'zh-TW', '查無應用實例相關資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1113', 'en-US', 'No application instance Id', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1113', 'zh-TW', '沒有傳入應用實例Id', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1114', 'en-US', 'No Faq information', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1114', 'zh-TW', '查無Faq資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1115', 'en-US', 'No Faq Answer', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1115', 'zh-TW', '查無Faq答案', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1116', 'en-US', 'No API information', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1116', 'zh-TW', '查無API資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1117', 'en-US', 'No website', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1117', 'zh-TW', '查無關於網站', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1118', 'en-US', 'No site map', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1118', 'zh-TW', '查無網站地圖', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1119', 'en-US', 'Failed to add member', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1119', 'zh-TW', '新增會員失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1120', 'en-US', 'Reset password verification letter failed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1120', 'zh-TW', '重設密碼驗證信寄出失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1121', 'en-US', 'Invalid password reset verification code', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1121', 'zh-TW', '無效的密碼重設驗證碼', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1122', 'en-US', 'No member information', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1122', 'zh-TW', '查無會員資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1123', 'en-US', 'Member information update failed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1123', 'zh-TW', '會員資料更新失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1124', 'en-US', 'Resend review member failed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1124', 'zh-TW', '重送審查會員失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1125', 'en-US', 'No authorization API', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1125', 'zh-TW', '查無授權API', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1127', 'en-US', 'No apply API', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1127', 'zh-TW', '查無申請的API', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1128', 'en-US', 'Apply instructions are required', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1128', 'zh-TW', '申請說明為必填', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1129', 'en-US', 'No files found', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1129', 'zh-TW', '查無檔案', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1130', 'en-US', 'No picture', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1130', 'zh-TW', '查無圖檔', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1131', 'en-US', 'No archive classification code', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1131', 'zh-TW', '查無檔案分類代碼', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1132', 'en-US', 'No Category Id', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1132', 'zh-TW', '查無分類Id', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1133', 'en-US', 'File not included', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1133', 'zh-TW', '未包含檔案', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1134', 'en-US', 'No FileId', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1134', 'zh-TW', '查無FileId', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1135', 'en-US', 'No unauthorized API information found', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1135', 'zh-TW', '查無未授權API資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1136', 'en-US', 'No unauthorized API details', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1136', 'zh-TW', '查無未授權API明細', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1137', 'en-US', 'No unauthorized API module found', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1137', 'zh-TW', '查無未授權API模組', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1138', 'en-US', 'Authorization API failed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1138', 'zh-TW', '授權API失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1139', 'en-US', 'Unauthorized API history data', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1139', 'zh-TW', '查無授權API歷程資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1140', 'en-US', 'No unreleased members', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1140', 'zh-TW', '查無未放行會員', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1141', 'en-US', 'Membership processing failed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1141', 'zh-TW', '會員資格處理失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1142', 'en-US', 'Status or start / end date cannot be empty', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1142', 'zh-TW', '狀態或起/迄日不可為空', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1143', 'en-US', 'Category name cannot be empty', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1143', 'zh-TW', '分類名稱不可為空', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1144', 'en-US', 'No case classification information', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1144', 'zh-TW', '查無實例分類資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1145', 'en-US', 'No instance classified data by code', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1145', 'zh-TW', '依代碼查無實例分類資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1146', 'en-US', 'Update instance classification failed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1146', 'zh-TW', '更新實例分類失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1147', 'en-US', 'Failed to delete instance classification', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1147', 'zh-TW', '刪除實例分類失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1148', 'en-US', 'New instance is missing required field', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1148', 'zh-TW', '新增實例缺少必填欄位', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1149', 'en-US', 'Failed to add an instance', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1149', 'zh-TW', '新增實例失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1150', 'en-US', 'No instance information', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1150', 'zh-TW', '查無實例資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1151', 'en-US', 'Update instance failed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1151', 'zh-TW', '更新實例失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1152', 'en-US', 'Failed to delete instance', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1152', 'zh-TW', '刪除實例失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1153', 'en-US', 'No API list', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1153', 'zh-TW', '查無API 清單', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1154', 'en-US', 'Failed to add topic category', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1154', 'zh-TW', '主題分類新增失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1155', 'en-US', 'No subject category list', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1155', 'zh-TW', '查無主題分類清單', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1156', 'en-US', 'No subject classification according to Id', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1156', 'zh-TW', '依照Id查無主題分類', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1157', 'en-US', 'Topic category update failed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1157', 'zh-TW', '主題分類更新失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1158', 'en-US', 'Topic category deletion failed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1158', 'zh-TW', '主題分類刪除失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1159', 'en-US', 'New FAQs are missing required fields', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1159', 'zh-TW', '新增常見問答缺少必填欄位', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1160', 'en-US', 'No FAQs found', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1160', 'zh-TW', '查無常見問答資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1161', 'en-US', 'The query FAQ is missing parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1161', 'zh-TW', '查詢常見問答缺少參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1162', 'en-US', 'Update FAQ is missing parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1162', 'zh-TW', 'update常見問答缺少參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1163', 'en-US', 'Update FAQ failed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1163', 'zh-TW', 'update常見問答失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1164', 'en-US', 'No FAQ Id', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1164', 'zh-TW', '查無常見問答Id', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1165', 'en-US', 'delete FAQ failed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1165', 'zh-TW', 'delete常見問答失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1166', 'en-US', 'About website archiving failure', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1166', 'zh-TW', '關於網站存檔失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1167', 'en-US', 'About missing parameters for website archiving', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1167', 'zh-TW', '關於網站存檔缺少參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1169', 'en-US', 'Query error about the website, multiple data appears', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1169', 'zh-TW', '查詢錯誤關於網站, 出現多筆資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1170', 'en-US', 'Failed to add a node on the site map', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1170', 'zh-TW', '網站地圖新增節點失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1171', 'en-US', 'Site map new node missing parameter', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1171', 'zh-TW', '網站地圖新增節點缺少參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1172', 'en-US', 'Site map update node failed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1172', 'zh-TW', '網站地圖更新節點失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1173', 'en-US', 'Site map update node is missing parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1173', 'zh-TW', '網站地圖更新節點缺少參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1174', 'en-US', 'Site map failed to delete node', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1174', 'zh-TW', '網站地圖刪除節點失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1175', 'en-US', 'Site map delete node missing parameter', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1175', 'zh-TW', '網站地圖刪除節點缺少參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1176', 'en-US', 'Sign-off message setting missing parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1176', 'zh-TW', '簽核訊息設定缺少參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1177', 'en-US', 'Sign-off message setting failed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1177', 'zh-TW', '簽核訊息設定失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1178', 'en-US', 'No sign-off message setting', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1178', 'zh-TW', '查無簽核訊息設定', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1179', 'en-US', 'Query error signing message settings, multiple data appears', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1179', 'zh-TW', '查詢錯誤簽核訊息設定, 出現多筆資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1180', 'en-US', 'No TsmpUser information found', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1180', 'zh-TW', '查無TsmpUser資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1181', 'en-US', 'No module information found', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1181', 'zh-TW', '查無 module 資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1182', 'en-US', 'deniedModule archive failed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1182', 'zh-TW', 'deniedModule存檔失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1183', 'en-US', 'No api-docs available', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1183', 'zh-TW', '查無可用的 api-docs 資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1184', 'en-US', 'The reqHeader parameter or switch is incorrect', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1184', 'zh-TW', 'reqHeader參數or開關不正確', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1185', 'en-US', 'paramType does not match', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1185', 'zh-TW', 'paramType不匹配', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1186', 'en-US', 'apiKey or URL is empty', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1186', 'zh-TW', 'apiKey or URL 為空', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1187', 'en-US', 'API authorized', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1187', 'zh-TW', 'API已授權', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1188', 'en-US', 'Member password must be at least 6 codes', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1188', 'zh-TW', '會員密碼最少6碼', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1189', 'en-US', 'API has applied', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1189', 'zh-TW', 'API已申請', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1190', 'en-US', 'Member account already exists', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1190', 'zh-TW', '會員帳號已存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1192', 'en-US', 'Status is enabled and cannot be deleted', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1192', 'zh-TW', '狀態為啟用不可刪除', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1193', 'en-US', 'You do not have permission to apply for this API', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1193', 'zh-TW', '您沒有權限申請此API', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1194', 'en-US', 'Added [Announcement Message] missing required information', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1194', 'zh-TW', '新增[公告消息]缺少必填資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1195', 'en-US', 'Added [Announcement Message] upload file archive failed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1195', 'zh-TW', '新增[公告消息]上傳檔案存檔失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1196', 'en-US', 'Update [Announcement Message] Missing required information', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1196', 'zh-TW', '更新[公告消息]缺少必填資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1197', 'en-US', 'Information has been changed, update [announcement message] failed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1197', 'zh-TW', '資料已被異動, 更新[公告消息]失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1198', 'en-US', 'No [Announcement News] list information', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1198', 'zh-TW', '查無[公告消息]清單資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1200', 'en-US', 'Unable to completely delete the specified [Announcement Message] material', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1200', 'zh-TW', '無法完整刪除指定的[公告消息]資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1201', 'en-US', 'Incorrect encoding', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1201', 'zh-TW', '編碼不正確', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1203', 'en-US', 'File write error', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1203', 'zh-TW', '檔案寫入錯誤', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1204', 'en-US', 'Can''t find topic classification', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1204', 'zh-TW', '找不到主題分類圖檔', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1206', 'en-US', 'Unable to replace the theme classification file', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1206', 'zh-TW', '無法替換主題分類圖檔', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1208', 'en-US', 'no subject', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1208', 'zh-TW', '查無主題', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1209', 'en-US', 'Unable to completely delete the specified [subject] data', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1209', 'zh-TW', '無法完整刪除指定的[主題]資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1210', 'en-US', 'Level and role archive errors', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1210', 'zh-TW', '關卡與角色存檔錯誤', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1211', 'en-US', 'No level and role', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1211', 'zh-TW', '查無關卡與角色', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1212', 'en-US', 'Failed to add message', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1212', 'zh-TW', '新增消息失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1213', 'en-US', 'New review apply failed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1213', 'zh-TW', '新增審核申請失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1214', 'en-US', 'Resubmission failed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1214', 'zh-TW', '重新送審失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1215', 'en-US', 'No pending orders', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1215', 'zh-TW', '查無待審單', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1216', 'en-US', 'Inquiry about pending order error', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1216', 'zh-TW', '查詢待審單發生錯誤', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1217', 'en-US', 'Document query error', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1217', 'zh-TW', '單據查詢錯誤', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1218', 'en-US', 'Document review / change failed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1218', 'zh-TW', '單據審核/變更失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1220', 'en-US', 'Failed to save, the data length is too large', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1220', 'zh-TW', '儲存失敗，資料長度過大', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1221', 'en-US', 'Some of the selected data do not belong to your organization, the transaction fails', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1221', 'zh-TW', '所選的資料有部份不屬於您的組織,異動失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1222', 'en-US', 'The transaction failed because it does not belong to your organization', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1222', 'zh-TW', '異動失敗,因為不屬於您的組織', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1223', 'en-US', 'Failed to update apply form', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1223', 'zh-TW', '更新申請單失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1224', 'en-US', '[Announcement Message] Unable to query', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1224', 'zh-TW', '[公告消息]無法查詢', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1226', 'zh-TW', '重複的回覆代碼', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1226', 'en-US', 'Duplicate return code', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1228', 'zh-TW', '該用戶端尚未註冊完成，無法申請API', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1228', 'en-US', 'The client you chose is required to be registered', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1229', 'en-US', 'Organization not exist', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1229', 'zh-TW', '組織名稱不存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1232', 'en-US', 'TUser name duplicated', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1232', 'zh-TW', '使用者名稱已存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1233', 'en-US', 'File cannot be empty', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1233', 'zh-TW', '檔案不得為空檔', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1234', 'en-US', 'New Password cannot be empty', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1234', 'zh-TW', '新密碼不可為空', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1235', 'en-US', 'New Password was the same as Original Password', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1235', 'zh-TW', '新密碼與原密碼相同', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1236', 'en-US', 'Original Password cannot be empty', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1236', 'zh-TW', '原密碼不可為空', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1237', 'en-US', 'Enter at least one', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1237', 'zh-TW', '至少輸入一項', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1238', 'en-US', 'Original password is incorrect', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1238', 'zh-TW', '原密碼不正確', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1239', 'en-US', 'Role alias duplicated', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1239', 'zh-TW', '角色代號重複', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1240', 'en-US', 'Role name duplicated', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1240', 'zh-TW', '角色名稱重複', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1241', 'en-US', 'Function not found (with locale)', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1241', 'zh-TW', '功能不存在 (含locale)', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1242', 'en-US', 'An error occured while transiting the schedule form, try to reset', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1242', 'zh-TW', '週期表單轉換錯誤，請重新設定', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1243', 'en-US', 'Role used by user', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1243', 'zh-TW', '該角色有使用者', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1244', 'en-US', 'User email:Email format only', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1244', 'zh-TW', '使用者E-mail:只能為Email格式', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1245', 'en-US', 'User account: only English letters (a~z, A~Z) and numbers can be entered without blanks', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1245', 'zh-TW', '使用者帳號:只能輸入英文字母(a~z,A~Z)及數字且不含空白', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1246', 'en-US', 'User account: length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1246', 'zh-TW', '使用者帳號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1247', 'en-US', 'User name: Length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1247', 'zh-TW', '使用者名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1248', 'en-US', 'Password: Length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1248', 'zh-TW', '密碼:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1249', 'en-US', 'Role list: required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1249', 'zh-TW', '角色清單:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1250', 'en-US', 'Organization name: required parameter', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1250', 'zh-TW', '組織名稱:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1252', 'en-US', 'User mail:length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1252', 'zh-TW', '使用者E-mail:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1253', 'en-US', 'Organization name:length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1253', 'zh-TW', '組織名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1256', 'en-US', 'New role name:length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1256', 'zh-TW', '新角色名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1257', 'en-US', 'User account: required parameter', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1257', 'zh-TW', '使用者帳號:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1258', 'en-US', 'User name: required parameter', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1258', 'zh-TW', '使用者名稱:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1259', 'en-US', 'Password: required parameter', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1259', 'zh-TW', '密碼:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1260', 'en-US', 'User E-mail: Required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1260', 'zh-TW', '使用者E-mail:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1262', 'en-US', 'Authorizable role  [{{0}}]  does not exist', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1262', 'zh-TW', '可授權角色 [{{0}}] 不存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1263', 'en-US', 'Authorizable roles: length limit [{{0}}] characters, [{{1}}] length [{{2}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1263', 'zh-TW', '可授權角色:長度限制 [{{0}}] 字內，[{{1}}] 長度[{{2}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1265', 'en-US', 'Login role: length limit [{{0}}] characters, length [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1265', 'zh-TW', '登入角色:長度限制 [{{0}}] 字內，長度[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1266', 'en-US', 'Authorizable roles: Required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1266', 'zh-TW', '可授權角色:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1267', 'en-US', 'Login role: Required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1267', 'zh-TW', '登入角色:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1268', 'en-US', 'The list of authorized roles for login roles already exists', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1268', 'zh-TW', '登入角色的可授權角色清單已經存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1269', 'en-US', 'Organization name duplicated', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1269', 'zh-TW', '組織名稱已存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1270', 'en-US', 'User account duplicated', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1270', 'zh-TW', '使用者帳號已存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1271', 'en-US', 'Upper layer organization name: Required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1271', 'zh-TW', '上層組織名稱:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1272', 'en-US', 'Upper layer organization name:length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1272', 'zh-TW', '上層組織名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1273', 'en-US', 'Organization ID: Required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1273', 'zh-TW', '組織單位ID:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1274', 'en-US', 'Organization ID:length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1274', 'zh-TW', '組織單位ID:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1275', 'en-US', 'Organization code:length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1275', 'zh-TW', '組織代碼:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1276', 'en-US', 'Contact phone: Required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1276', 'zh-TW', '聯絡人電話:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1277', 'en-US', 'Contact phone:length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1277', 'zh-TW', '聯絡人電話:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1278', 'en-US', 'Contact person name: Required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1278', 'zh-TW', '聯絡人姓名:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1279', 'en-US', 'Contact person name:length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1279', 'zh-TW', '聯絡人姓名:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1280', 'en-US', 'Contact person mail: Required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1280', 'zh-TW', '聯絡人信箱:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1281', 'en-US', 'Contact person mail: length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1281', 'zh-TW', '聯絡人信箱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1282', 'en-US', 'New role name duplicated', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1282', 'zh-TW', '新角色名稱已存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1283', 'en-US', 'New role name: Required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1283', 'zh-TW', '新角色名稱:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1288', 'en-US', 'Add user fail', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1288', 'zh-TW', '新增失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1291', 'en-US', 'File parsing error', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1291', 'zh-TW', '檔案解析錯誤', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1294', 'en-US', 'No query permission', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1294', 'zh-TW', '無查詢權限', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1299', 'en-US', 'Parameter validation error', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1299', 'zh-TW', '參數驗證錯誤', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1300', 'en-US', 'Role code: length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1300', 'zh-TW', '角色代號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1301', 'en-US', 'Role code: only English letters (a~z, A~Z) and numbers can be entered without blanks', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1301', 'zh-TW', '角色代號:只能輸入英文字母(a~z,A~Z)及數字且不含空白', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1302', 'en-US', 'Role Name: length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1302', 'zh-TW', '角色名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1303', 'en-US', 'The organization (parent node) cannot move to its own sub-organization', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1303', 'zh-TW', '節點不可以移動到子節點', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1304', 'en-US', 'The organization contains undeleted API', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1304', 'zh-TW', '組織包含未刪除的API', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1305', 'en-US', 'The organization contains undeleted Users', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1305', 'zh-TW', '該組織包含未刪除的用戶', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1306', 'en-US', 'The organization contains undeleted Java Module', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1306', 'zh-TW', '組織包含未刪除的Java模組', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1307', 'en-US', 'The organization contains undeleted .Net Module', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1307', 'zh-TW', '組織包含未刪除的.Net模組', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1308', 'en-US', 'The organization contains undeleted sub organization', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1308', 'zh-TW', '組織包含未刪除的子組織', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1309', 'en-US', 'Function list: Required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1309', 'zh-TW', '功能清單:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1311', 'en-US', 'Contact person mail:Email format only', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1311', 'zh-TW', '聯絡人信箱:只能為Email格式', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1312', 'en-US', 'Upper layer organization: You cannot choose the node itself', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1312', 'zh-TW', '上層單位組織：不可以選擇節點自己本身', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1313', 'en-US', 'User account: Only English letters (a~z, A~Z) 、@ and numbers can be input without blank', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1313', 'zh-TW', '使用者帳號：只能輸入英文字母(a~z,A~Z)、@及數字且不含空白', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1314', 'en-US', 'Function code: Required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1314', 'zh-TW', '功能代碼:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1315', 'en-US', 'Function code: length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1315', 'zh-TW', '功能代碼:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1316', 'en-US', 'Language: Required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1316', 'zh-TW', '語系:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1317', 'en-US', 'Language: length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1317', 'zh-TW', '語系:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1318', 'en-US', 'Function description: length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1318', 'zh-TW', '功能描述:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1319', 'en-US', 'Function name: length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1319', 'zh-TW', '功能名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1320', 'en-US', 'Function name (English): length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1320', 'zh-TW', '功能名稱(英文):長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1321', 'en-US', 'Function name (English): only enter alphanumeric characters、_、-', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1321', 'zh-TW', '功能名稱(英文):只能輸入英數、_、- ', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1322', 'zh-TW', '用戶端帳號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1322', 'en-US', 'Client account: length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1323', 'zh-TW', '用戶端代號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1323', 'en-US', 'Client code: length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1324', 'zh-TW', '用戶端代號:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1324', 'en-US', 'Client code: Required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1325', 'zh-TW', '用戶端代號:只能輸入英文字母(a~z,A~Z)及數字且不含空白', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1325', 'en-US', 'Client code: only English letters (a~z, A~Z) and numbers can be entered without blanks', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1326', 'zh-TW', '用戶端名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1326', 'en-US', 'Client name: length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1327', 'zh-TW', '用戶端名稱:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1327', 'en-US', 'Client name: Required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1328', 'zh-TW', '簽呈編號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1328', 'en-US', 'Sign number: length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1329', 'zh-TW', '密碼:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1329', 'en-US', 'Password: length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1330', 'zh-TW', '密碼:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1330', 'en-US', 'Password: Required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1331', 'zh-TW', '電子郵件帳號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1331', 'en-US', 'Email account: length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1332', 'zh-TW', '電子郵件帳號:只能為Email格式', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1332', 'en-US', 'Email account:Email format only', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1333', 'zh-TW', '擁有者:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1333', 'en-US', 'Owner: length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1334', 'zh-TW', '擁有者:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1334', 'en-US', 'Owner: Required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1335', 'zh-TW', '狀態:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1335', 'en-US', 'Status: Required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1336', 'zh-TW', '開放狀態:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1336', 'en-US', 'Open state: Required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1337', 'zh-TW', '開始日期:只能輸入日期格式', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1337', 'en-US', 'Start date:Only date format can be entered', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1338', 'zh-TW', '到期日期:只能輸入日期格式', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1338', 'en-US', 'End date:Only date format can be entered', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1339', 'zh-TW', '服務時間:只能輸入時間格式', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1339', 'en-US', 'Service time:Only time format can be entered', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1340', 'zh-TW', '用戶端代號已存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1340', 'en-US', 'Client code duplicated', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1341', 'zh-TW', '用戶端名稱已存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1341', 'en-US', 'Client name duplicated', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1342', 'zh-TW', '用戶端帳號已存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1342', 'en-US', 'Client account duplicated', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1343', 'zh-TW', '用戶端帳號:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1343', 'en-US', 'Client account: Required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1344', 'zh-TW', '用戶端不存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1344', 'en-US', 'Client does not exist', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1345', 'zh-TW', '主機名稱:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1345', 'en-US', 'Host name: Required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1346', 'zh-TW', '主機IP:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1346', 'en-US', 'Host IP: Required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1347', 'zh-TW', '主機名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1347', 'en-US', 'Host name: length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1348', 'zh-TW', '主機IP:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1348', 'en-US', 'Host IP: length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1349', 'zh-TW', '主機IP:格式錯誤', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1349', 'en-US', 'Host IP:wrong format', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1350', 'en-US', 'Required field: [{{0}}]', 'DP共用訊息');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1350', 'zh-TW', '[{{0}}] 為必填欄位', 'DP共用訊息');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1351', 'en-US', '[{{0}}] is no more than [{{1}}] characters ([{{2}}])', 'DP共用訊息');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1351', 'zh-TW', '[{{0}}] 長度限制 [{{1}}] 字內，您輸入[{{2}}] 個字', 'DP共用訊息');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1352', 'en-US', 'String pattern is not matched: [{{0}}]', 'DP共用訊息');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1352', 'zh-TW', '[{{0}}] 格式不正確', 'DP共用訊息');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1353', 'en-US', '[{{0}}] already exists: {{1}}', 'DP共用訊息');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1353', 'zh-TW', '[{{0}}] 已存在: {{1}}', 'DP共用訊息');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1354', 'en-US', '[{{0}}] does not exist: {{1}}', 'DP共用訊息');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1354', 'zh-TW', '[{{0}}] 不存在: {{1}}', 'DP共用訊息');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1355', 'en-US', '[{{0}}] shall not be less than {{1}} ({{2}})', 'DP共用訊息');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1355', 'zh-TW', '[{{0}}] 不得小於 {{1}}, 您輸入 {{2}}', 'DP共用訊息');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1356', 'en-US', '[{{0}}] shall not be greater than {{1}} ({{2}})', 'DP共用訊息');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1356', 'zh-TW', '[{{0}}] 不得大於 {{1}}, 您輸入 {{2}}', 'DP共用訊息');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1358', 'zh-TW', '[群組名稱]超過群組選取上限205，請重新選取', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1358', 'en-US', 'Upper limit of 205 Groups per Group List', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1359', 'zh-TW', '建立新Client Group時，必須是SYSTEM Group', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1359', 'en-US', 'When Create New Client Group, must be SYSTEM group', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1360', 'zh-TW', '群組不存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1360', 'en-US', 'Group was not found', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1362', 'en-US', 'Function code: Required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1362', 'zh-TW', '功能代碼:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1363', 'en-US', 'Security Level: Required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1363', 'zh-TW', '安全等級:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1364', 'en-US', 'Security Level not found', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1364', 'zh-TW', '安全等級不存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1365', 'zh-TW', '用戶端的SECURITY LEVEL ID與群組的SECURITY LEVEL ID不符合，群組ID為[{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1365', 'en-US', 'The SECURITY LEVEL ID of the client does not match the SECURITY LEVEL ID of the group. The group ID is [{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1366', 'zh-TW', '開始日期與到期日期必須填寫', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1366', 'en-US', 'Start date and due date must be filled in', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1367', 'zh-TW', '到期時間不可小於開始時間', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1367', 'en-US', 'The expiration time cannot be less than the start time', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1368', 'zh-TW', '到期日期不可小於開始日期', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1368', 'en-US', 'The expiration date cannot be less than the start date', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1369', 'zh-TW', '開始時間與到期時間必須填寫', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1369', 'en-US', 'Start time and expiration time must be filled in', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1372', 'zh-TW', '未知類型的Grant Type: [{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1372', 'en-US', 'Grant Type of unknown type: [{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1379', 'en-US', 'This Open API Key has been renewed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1379', 'zh-TW', '此 Open API Key 已執行過展期', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1381', 'en-US', 'Starting time: Incorrect format', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1381', 'zh-TW', '開始時間:格式不正確', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1383', 'en-US', 'End Time: Incorrect format', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1383', 'zh-TW', '結束時間:格式不正確', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1384', 'en-US', '[{{0}}] is no less than [{{1}}] characters ([{{2}}])', 'DP共用訊息');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1384', 'zh-TW', '[{{0}}] 長度至少須 [{{1}}] 字，您輸入[{{2}}] 個字', 'DP共用訊息');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1385', 'zh-TW', '模組名稱:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1385', 'en-US', 'Module name: Required parameters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1397', 'en-US', 'Authorized verification type: [{{0}}] does not exist', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1397', 'zh-TW', '授權核身種類:[{{0}}]不存在', '');
INSERT INTO tsmp_rtn_code (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1398', 'en-US','Group name exists','');
INSERT INTO tsmp_rtn_code (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1398', 'zh-TW','群組名稱已存在','');
INSERT INTO tsmp_rtn_code (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1399', 'en-US','Group code exists','');
INSERT INTO tsmp_rtn_code (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1399', 'zh-TW','群組代碼已存在','');
INSERT INTO tsmp_rtn_code (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1400', 'en-US','API: [{{0}}] does not  found','');
INSERT INTO tsmp_rtn_code (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1400', 'zh-TW','API: [{{0}}]不存在','');
INSERT INTO tsmp_rtn_code (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1401', 'en-US','API: [{{0}}]不存在','');
INSERT INTO tsmp_rtn_code (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1401', 'zh-TW','用戶端狀態不正常：[{{0}}]','');
INSERT INTO tsmp_rtn_code (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1402', 'en-US', 'The amount of API can be up to a maximum of [{{0}}], you selected [{{1}}]','');
INSERT INTO tsmp_rtn_code (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1402', 'zh-TW', '虛擬群組的API數量上限為 [{{0}}]，您選擇 [{{1}}]','');
INSERT INTO tsmp_rtn_code (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1403', 'en-US', 'Failed to remove. This virtual group is being used.','');
INSERT INTO tsmp_rtn_code (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1403', 'zh-TW', '無法刪除，請解除用戶端的虛擬授權設定','');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1404', 'zh-TW', '無法刪除，請解除群組的授權核身種類: {{0}}', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1404', 'en-US', 'Failed to remove. Groups are linked to this authority: {{0}}', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1405', 'zh-TW', '請輸入網址格式', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1405', 'en-US', 'Please enter URL format', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1406', 'en-US', 'Amount of [{{0}}] is no less than [{{1}}]: [{{2}}]', 'DP共用訊息');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1406', 'zh-TW', '[{{0}}] 數量不可少於 [{{1}}]，您選擇 [{{2}}]', 'DP共用訊息');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1407', 'en-US', 'Amount of [{{0}}] is no larger than [{{1}}]: [{{2}}]', 'DP共用訊息');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1407', 'zh-TW', '[{{0}}] 數量不可超過 [{{1}}]，您選擇 [{{2}}]', 'DP共用訊息');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1408', 'zh-TW', '新密碼與再次確認密碼不一致', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1408', 'en-US', 'The new password does not match the reconfirmed password', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1411', 'zh-TW', '部署容器已經啟用', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1411', 'en-US', 'Deploy Container is already activated', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1412', 'zh-TW', '部署容器已經停用', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1412', 'en-US', 'Deploy Container is already inactivated', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1413', 'zh-TW', '無法啟用，部署容器未綁定任何節點', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1413', 'en-US', 'Deploy Container is not bound to any nodes', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1414', 'zh-TW', '無法刪除，部署容器啟用中', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1414', 'en-US', 'Failed to remove. This deploy container is activated', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1415', 'zh-TW', 'NodeTaskNotifiers發生錯誤', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1415', 'en-US', 'Error occurred in NodeTaskNotifiers', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1416', 'zh-TW', '查詢日期區間不得超過 {{0}} 天', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1416', 'en-US', 'Query date range is no more than {{0}} days', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1417', 'zh-TW', '該群組有用戶端', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1417', 'en-US', 'Group has been used by Client', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1418', 'zh-TW', '請先匯入外部系統介接規格', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1418', 'en-US', 'You need to upload an openAPI specification or import from an URL', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1419', 'en-US', 'Host name already exists', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1419', 'zh-TW', '主機名稱已存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1420', 'en-US', 'Host does not exist', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1420', 'zh-TW', '主機不存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1421', 'en-US', 'Register Host must be disabled', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1421', 'zh-TW', '註冊主機心跳必須停用', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1422', 'en-US', 'Register Host been refered by Registered API', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1422', 'zh-TW', '註冊主機被註冊API參考', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1423', 'zh-TW', '主機名稱{{0}}有重複相同資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1423', 'en-US', 'The host name {{0}} has duplicate data', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1425', 'zh-TW', '主機IP{{0}}有重複相同資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1425', 'en-US', 'The host IP{{0}} has duplicate data', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1426', 'zh-TW', '無法使用此模組名稱，已由匯入方式建立', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1426', 'en-US', 'Module name has been created by importing OpenAPI doc.', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1424', 'zh-TW', '未指定主機位址(host)', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1424', 'en-US', 'Host is not specified', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1427', 'zh-TW', '例外類型只能為N、W、M、D', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1427', 'en-US', 'The exception type can only be N, W, M, D', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1428', 'zh-TW', '告警名稱已存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1428', 'en-US', 'Alert name duplicated', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1429', 'zh-TW', '告警設定不存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1429', 'en-US', 'Alert Setting not found', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1430', 'en-US', 'Unable to get authorization code', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1430', 'zh-TW', '取得授權碼失敗，請重新再試', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1431', 'en-US', 'Authorization code is invalid', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1431', 'zh-TW', '授權碼不可用', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1432', 'en-US', 'Inconsistent authorization type', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1432', 'zh-TW', '授權類型不正確', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1435', 'en-US', '時間範圍只能為T、W、M、D', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1435', 'zh-TW', 'The time range can only be T, W, M, D', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1436', 'en-US', 'Module not found. No activated or latest update module.', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1436', 'zh-TW', '找不到已啟動或最近上傳的模組', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1437', 'zh-TW', '一個部署容器中不可綁定兩個相同的模組：[{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1437', 'en-US', 'Starting more than 1 module which have same name [{{0}}] in one DC is forbidden', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1438', 'zh-TW', '刪除失敗，模組啟用中', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1438', 'en-US', 'Delete failed. Module is active.', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1439', 'zh-TW', '刪除失敗，模組已綁定', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1439', 'en-US', 'Delete failed. Module is bound to DC.', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1440', 'zh-TW', '請填寫密碼或是重置密碼', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1440', 'en-US', 'Please fill in the password or reset the password', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1441', 'zh-TW', '請填寫密碼', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1441', 'en-US', 'Please fill in the password', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1442', 'zh-TW', '其他組織單位已存在相同名稱的模組：[{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1442', 'en-US', 'Upload moduleName duplicate with Org: [{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1443', 'zh-TW', '不支援的檔案類型', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1443', 'en-US', 'Unsupport file type', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1444', 'zh-TW', '不可匯出 Java 或 .NET 模組的 API', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1444', 'en-US', 'Can not export Java or .NET API', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1445', 'zh-TW', '檔案中未包含任何 API 資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1445', 'en-US', 'API detail not found', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1446', 'zh-TW', '例外日期、開始時間與結束時間不可填寫', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1446', 'en-US', 'Exception date, start time and end time cannot be filled in', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1447', 'zh-TW', '例外日期不可填寫', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1447', 'en-US', 'Exception date cannot be filled', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1448', 'zh-TW', '開始時間與結束時間格式不正確', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1448', 'en-US', 'Incorrect start time and end time format', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1449', 'zh-TW', '開始時間與結束時間為必填', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1449', 'en-US', 'Start time and end time are required', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1450', 'zh-TW', '例外日期為必填', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1450', 'en-US', 'Exception date is required', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1451', 'zh-TW', '例外日期格式不正確', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1451', 'en-US', 'Exception date format is incorrect', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1452', 'zh-TW', '{{0}} API 失敗：apiKey=[{{1}}], moduleName=[{{2}}], msg={{3}}', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1452', 'en-US', '{{0}} API Failed. ApiKey=[{{1}}], moduleName=[{{2}}], msg={{3}}', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1453', 'zh-TW', 'Composer Flow 轉型失敗：apiKey=[{{0}}, moduleName=[{{1}}], msg={{2}}', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1453', 'en-US', 'Fail to parse flow. Apikey=[{{0}}], moduleName=[{{1}}], msg={{2}}', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1454', 'zh-TW', '寫入 Composer 資料錯誤：apiKey=[{{0}}, moduleName=[{{1}}], msg={{2}}', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1454', 'en-US', 'Fail to write Composer data. Apikey=[{{0}}], moduleName=[{{1}}], msg={{2}}', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1455', 'zh-TW', '向 digiRunner 取得 Token 失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1455', 'en-US', 'Fail to get token from digiRunner', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1456', 'zh-TW', '查無組合API', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1456', 'en-US', 'No API found or is not a composed API', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1457', 'zh-TW', '模組已經綁定', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1457', 'en-US', 'Module is bound already', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1458', 'zh-TW', '模組已經解除綁定', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1458', 'en-US', 'Module is currently unbound', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1459', 'zh-TW', '不支援操作此架構的模組', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1459', 'en-US', 'This structure of module is end-of-support', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1460', 'zh-TW', '尚未確認部署，無法啟用組合API', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1460', 'en-US', 'Fail to activate Composed API which is not deployment confirmed.', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1461', 'zh-TW', '尚有 API 未下架，確定繼續執行？', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1461', 'en-US', 'Some APIs are still launched, continue?', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1462', 'zh-TW', '尚有 API 正在申請單流程中，確定繼續執行？', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1462', 'en-US', 'Some APIs are included in application forms which are still in progress, continue?', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1463', 'zh-TW', '刪除失敗，API啟用中', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1463', 'en-US', 'Fail to delete. API is activated', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1464', 'zh-TW', '刪除失敗，API仍有相關的模組紀錄', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1464', 'en-US', 'Some APIs are still referenced by modules', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1465', 'zh-TW', '有些是透過匯入外部介接規格而註冊的 API，確定繼續執行？', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1465', 'en-US', 'Some APIs are registered by importing OpenAPI Spec, continue?', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1466', 'zh-TW', '僅可更新註冊或組合API', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1466', 'en-US', 'This API is only available for Registered or Composed API', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1467', 'zh-TW', '僅可更新Java模組或.NET模組的API', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1467', 'en-US', 'Only available for updating APIs in Java or .NET module', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1468', 'zh-TW', 'Log欄位與回應值為必填欄位', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1468', 'en-US', 'Log field and response value are required fields', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1469', 'zh-TW', '模組名稱與API ID為必填欄位', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1469', 'en-US', 'Module name and API ID are required fields', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1470', 'en-US', 'Cannot reset signBlock within 5 mins', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1470', 'zh-TW', '5分鐘內不可重置signBlock', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1471', 'en-US', 'Password fail over upper limit', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1471', 'zh-TW', '密碼錯誤超過上限', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1472', 'en-US', 'Tuser locked', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1472', 'zh-TW', '使用者已鎖定', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1473', 'en-US', 'Tuser disabled', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1473', 'zh-TW', '使用者已停權', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1475', 'en-US', 'Failed to bind module. DC is not active: {{0}}', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1475', 'zh-TW', '無法綁定，部署容器尚未啟用: {{0}}', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1476', 'en-US', 'Failed to unbind. This is the last system module bound to DC.', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1476', 'zh-TW', '無法解除綁定，已經沒有其他部署容器啟用此系統模組了', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1477', 'en-US', 'Client ID: Only alphanumeric characters, ''_'' and ''-'' are accepted', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1477', 'zh-TW', '用戶帳號：僅可輸入英數字、底線「_」及橫線「-」', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1478', 'en-US', 'Application description: No more than [{{0}}] characters ([{{1}}])', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1478', 'zh-TW', '申請內容說明：長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1479', 'en-US', 'Module is already activated', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1479', 'zh-TW', '模組已經啟用', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1480', 'en-US', 'Module is currently deactivated', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1480', 'zh-TW', '模組已經停用', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1481', 'en-US', 'Data in TSMP_API is not consistent with TSMP_API_REG: ApiKey={{0}}, ModuleName={{1}}', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1481', 'zh-TW', 'TSMP_API 資料與 TSMP_API_REG 不一致: ApiKey={{0}}, ModuleName={{1}}', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1482', 'en-US', 'TSecurityLevel contains undeleted Group: {{0}}', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1482', 'zh-TW', '此安全等級有未刪除的Group: {{0}}', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1483', 'en-US', 'TSecurityLevel contains undeleted Client: {{0}}', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1483', 'zh-TW', '此安全等級有未刪除的Client: {{0}}', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1484', 'en-US', 'Register Host not found', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1484', 'zh-TW', '註冊主機不存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1485', 'en-US', 'Register Host and Client not match', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1485', 'zh-TW', '註冊主機與客戶端不符', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1486', 'en-US', 'Register Host monitor not enabled', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1486', 'zh-TW', '註冊主機監控尚未啟用', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1487', 'en-US', 'The host status can only be A, S', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1487', 'zh-TW', '主機狀態只能為A、S', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1488', 'en-US', 'Register Host is required', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1488', 'zh-TW', '註冊主機為必填', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1489', 'en-US', 'Register Host : length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1489', 'zh-TW', '註冊主機:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1490', 'en-US', 'Register Host Status: length limit [{{0}}] characters, you enter [{{1}}] characters', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1490', 'zh-TW', '註冊主機狀態:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1491', 'en-US', 'BeanName Not Found: {{0}}', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1491', 'zh-TW', '執行工作不存在: {{0}}', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1492', 'en-US', 'Custom package does not exist', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1492', 'zh-TW', '客製包不存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1496', 'en-US', 'Failed to log in to digiRunner', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1496', 'zh-TW', '登入 digiRunner 失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1497', 'en-US', 'Invoke API specification error：[{{0}}] - [{{1}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1497', 'zh-TW', '介接規格錯誤：[{{0}}] - [{{1}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1498', 'en-US', 'HTTP error：[{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1498', 'zh-TW', 'HTTP error：[{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1499', 'en-US', 'Invoke API logic error：[{{0}}] - [{{1}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1499', 'zh-TW', '介接邏輯錯誤：[{{0}}] - [{{1}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1500', 'zh-TW', 'Base64 Decode 錯誤: {{0}}', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1500', 'en-US', 'Base64 Decode error: {{0}}', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1501', 'en-US', 'Group maintenance: {{0}}, authorization scope: {{1}} is currently bound, please remove it first.', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1501', 'zh-TW', '群組代碼：{{0}}，授權範圍代碼：{{1}}目前有綁定，請先移除。', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1502', 'en-US', 'The API list currently has data, please delete it first.', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1502', 'zh-TW', 'API列表目前有資料，請先刪除。', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1503', 'en-US', 'Http Method: Required parameters', ''); 			  
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1503', 'zh-TW', 'Http Method:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1504', 'zh-TW', '可能是髒資料導致系統無法匹配，({{0}})', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1504', 'en-US', 'It may be that dirty data caused the system to fail to match, ({{0}})', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1510', 'zh-TW', 'LDAP驗證失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1510', 'en-US', 'LDAP verification failed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1511', 'zh-TW', 'LDAP未啟用', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1511', 'en-US', 'LDAP is not enabled', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1512', 'zh-TW', 'LDAP連線失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1512', 'en-US', 'LDAP connection failed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1513', 'zh-TW', 'User IP 不在可登入的網段中', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1513', 'en-US', 'User IP is not in the network segment that can be logged in', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1514', 'zh-TW', '您所在的網段,無法登入', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1514', 'en-US', 'You are in the network segment and cannot log in', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1519', 'en-US', 'User is not logged in,please open Composer through digiRunner', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1519', 'zh-TW', '使用者未登入,請透過digiRunner 開啟Composer', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1520', 'en-US', 'Permission denied by digiRunner', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1520', 'zh-TW', 'digiRunner拒絕存取', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1522', 'zh-TW', 'CApiKey驗證失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1522', 'en-US', 'CApiKey verification failed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1523', 'zh-TW', '時區必須填寫', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1523', 'en-US', 'Time zone must be filled in', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2000', 'en-US', 'Required', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2000', 'zh-TW', '必填', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2001', 'en-US', 'Max length [{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2001', 'zh-TW', '最大長度為 [{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2002', 'en-US', 'Min length [{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2002', 'zh-TW', '最小長度為 [{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2003', 'en-US', 'Must contain [{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2003', 'zh-TW', '必須包含 [{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2004', 'en-US', 'Must not contain [{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2004', 'zh-TW', '不得包含 [{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2005', 'en-US', 'No greater than [{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2005', 'zh-TW', '數值不可大於 [{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2006', 'en-US', 'No less than [{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2006', 'zh-TW', '數值不可小於 [{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2007', 'en-US', 'Incorrect format', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2007', 'zh-TW', '格式不正確', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2008', 'en-US', 'Only alphanumeric characters, ''_'' and ''-'' are accepted', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2008', 'zh-TW', '僅可輸入英數字、底線「_」及橫線「-」', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2009', 'en-US', 'Select at least [{{0}}] items', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2009', 'zh-TW', '最少選擇 [{{0}}] 項', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2010', 'en-US', 'Select at most [{{0}}] items', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2010', 'zh-TW', '最多選擇 [{{0}}] 項', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2011', 'en-US', 'Only alphanumeric characters, ''_'', ''-'' and ''.'' are accepted', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2011', 'zh-TW', '僅可輸入英數字、底線「_」、橫線「-」及句號「.」', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2012', 'en-US', 'Approximately', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2012', 'zh-TW', '大約', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2013', 'en-US', 'Every day', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2013', 'zh-TW', '每一天', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2014', 'en-US', 'Every month on the {{0}} day', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2014', 'zh-TW', '每個月 {{0}} 號', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2015', 'en-US', 'Every month on every {{0}}', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2015', 'zh-TW', '每星期{{0}}', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2016', 'en-US', ' at {{0}}', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2016', 'zh-TW', '的 {{0}}', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2017', 'en-US', 'Every 10 minutes', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2017', 'zh-TW', '每十分鐘', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2018', 'en-US', 'Every half an hour', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2018', 'zh-TW', '每半小時', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2019', 'en-US', 'Every hour', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2019', 'zh-TW', '每一小時', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2020', 'en-US', 'Every {{0}} hours', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2020', 'zh-TW', '每 {{0}} 小時', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2021', 'en-US', 'Only numbers are accepted', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2021', 'zh-TW', '僅可輸入數字', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2022', 'zh-TW', '僅可輸入中英數字、底線「_」及橫線「-」', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2022', 'en-US', 'Only Chinese and English numbers, underscore ''_'' and horizontal line ''-'' can be input ', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2023', 'zh-TW', '僅可輸入英數字、底線「_」、橫線「-」、點「.」及「@」', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('2023', 'en-US', 'Only alphanumeric characters, ''_'', ''-'', ''.'' and ''@'' are accepted', '');

INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (1, 'MEMBER_REG_FLAG', '前台會員註冊開關', 'DISABLE', '停用', 10, 'V', NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (1, 'MEMBER_REG_FLAG', 'Front desk member registration switch', 'DISABLE', 'Deactivate', 10, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (2, 'MEMBER_REG_FLAG', '前台會員註冊開關', 'ENABLE', '啟用', 11, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (2, 'MEMBER_REG_FLAG', 'Front desk member registration switch', 'ENABLE', 'Enable', 11, 'en-US', 'V', NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (3, 'API_TYPE', 'API類型', 'OFF', '未上架API清單', 20, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (3, 'API_TYPE', 'API type', 'OFF', 'Unlisted API list', 20, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (4, 'API_TYPE', 'API類型', 'ON', '已上架API清單', 21, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (4, 'API_TYPE', 'API type', 'ON', 'List of APIs listed', 21, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (5, 'NEWS_TYPE', '公告類型', 'UPDATE', 'API異動', 30, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (5, 'NEWS_TYPE', 'Announcement type', 'UPDATE', 'API changes', 30, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (6, 'NEWS_TYPE', '公告類型', 'ON', 'API上架', 31, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (6, 'NEWS_TYPE', 'Announcement type', 'ON', 'API listed', 31, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (7, 'NEWS_TYPE', '公告類型', 'OFF', 'API下架', 32, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (7, 'NEWS_TYPE', 'Announcement type', 'OFF', 'API removal', 32, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (8, 'REVIEW_STATUS', '簽核狀態', 'WAIT1', '待審', 50, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (8, 'REVIEW_STATUS', 'Sign-off status', 'WAIT1', 'Pending', 50, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (9, 'REVIEW_STATUS', '簽核狀態', 'ACCEPT', '同意', 51, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (9, 'REVIEW_STATUS', 'Sign-off status', 'ACCEPT', 'Agree', 51, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (10, 'REVIEW_STATUS', '簽核狀態', 'DENIED', '不同意', 52, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (10, 'REVIEW_STATUS', 'Sign-off status', 'DENIED', 'Disagree', 52, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (11, 'REVIEW_STATUS', '簽核狀態', 'RETURN', '退回', 53, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (11, 'REVIEW_STATUS', 'Sign-off status', 'RETURN', 'Return', 53, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (12, 'REVIEW_STATUS', '簽核狀態', 'WAIT2', '待審(重送)', 54, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (12, 'REVIEW_STATUS', 'Sign-off status', 'WAIT2', 'Pending trial (re-sent)', 54, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (13, 'REVIEW_STATUS', '簽核狀態', 'END', '取消', 55, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (13, 'REVIEW_STATUS', 'Sign-off status', 'END', 'Cancelled', 55, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (14, 'REVIEW_TYPE', '簽核類別', 'API_APPLICATION', '用戶API申請', 60, NULL, 'AP', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (14, 'REVIEW_TYPE', 'Approval category', 'API_APPLICATION', 'User API application', 60, 'en-US', NULL, 'AP', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (15, 'REVIEW_TYPE', '簽核類別', 'API_ON_OFF', 'API上下架管理', 61, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (15, 'REVIEW_TYPE', 'Approval category', 'API_ON_OFF', 'API drop-off management', 61, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (16, 'REVIEW_TYPE', '簽核類別', 'CLIENT_REG', '用戶註冊', 62, NULL, 'CR', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (16, 'REVIEW_TYPE', 'Approval category', 'CLIENT_REG', 'User registration', 62, 'en-US', NULL, 'CR', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (17, 'API_ON_OFF', 'API上下架管理', 'API_ON', 'API上架', 65, NULL, 'ON', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (17, 'API_ON_OFF', 'API drop-off management', 'API_ON', 'API listed', 65, 'en-US', NULL, 'ON', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (18, 'API_ON_OFF', 'API上下架管理', 'API_OFF', 'API下架', 66, NULL, 'OF', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (18, 'API_ON_OFF', 'API drop-off management', 'API_OFF', 'API removal', 66, 'en-US', NULL, 'OF', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (19, 'API_ON_OFF', 'API上下架管理', 'API_ON_UPDATE', 'API異動', 67, NULL, 'UP', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (19, 'API_ON_OFF', 'API drop-off management', 'API_ON_UPDATE', 'API changes', 67, 'en-US', NULL, 'UP', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (20, 'CHK_LAYER', '關卡名稱', '0', '申請者', 70, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (20, 'CHK_LAYER', 'Level name', '0', 'Applicant', 70, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (21, 'CHK_LAYER', '關卡名稱', '1', '經辦', 71, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (21, 'CHK_LAYER', 'Level name', '1', 'Manage', 71, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);

INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (22, 'CHK_LAYER', '關卡名稱', '2', '主管', 72, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (22, 'CHK_LAYER', 'Level name', '2', 'Supervisor', 72, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (23, 'CHK_LAYER', '關卡名稱', '3', '總經理', 73, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (23, 'CHK_LAYER', 'Level name', '3', 'General manager', 73, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (24, 'CHK_LAYER', '關卡名稱', '4', ' ', 74, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (24, 'CHK_LAYER', 'Level name', '4', ' ', 74, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (25, 'API_AUTHORITY', 'API露出權限', '0', '對內及對外', 80, NULL, '0', '1', '2', '-1', NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (25, 'API_AUTHORITY', 'API exposure', '0', 'Internally and externally', 80, 'en-US', NULL, '0', '1', '2', '-1', NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (26, 'API_AUTHORITY', 'API露出權限', '1', '對外', 81, NULL, '1', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (26, 'API_AUTHORITY', 'API exposure', '1', 'foreign', 81, 'en-US', NULL, '1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (27, 'API_AUTHORITY', 'API露出權限', '2', '對內', 82, NULL, '2', '-1', NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (27, 'API_AUTHORITY', 'API exposure', '2', 'Internally', 82, 'en-US', NULL, '2', '-1', NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (28, 'FB_FLAG', '前後台資料', 'FRONT', '前台', 90, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (28, 'FB_FLAG', 'Front and back data', 'FRONT', 'Front desk', 90, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (29, 'FB_FLAG', '前後台資料', 'BACK', '後台', 91, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (29, 'FB_FLAG', 'Front and back data', 'BACK', 'Backstage', 91, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (33, 'JOB_STATUS', '排程狀態', 'W', '等待', 110, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (33, 'JOB_STATUS', 'Schedule status', 'W', 'wait', 110, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (34, 'JOB_STATUS', '排程狀態', 'R', '執行中', 111, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (34, 'JOB_STATUS', 'Schedule status', 'R', 'Executing', 111, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (35, 'JOB_STATUS', '排程狀態', 'E', '失敗', 112, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (35, 'JOB_STATUS', 'Schedule status', 'E', 'failure', 112, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (36, 'JOB_STATUS', '排程狀態', 'D', '完成', 113, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (36, 'JOB_STATUS', 'Schedule status', 'D', 'carry out', 113, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (37, 'JOB_STATUS', '排程狀態', 'C', '取消', 114, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (37, 'JOB_STATUS', 'Schedule status', 'C', 'cancel', 114, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (38, 'JOB_STATUS', '排程狀態', 'A', '全部', 115, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (38, 'JOB_STATUS', 'Schedule status', 'A', 'All', 115, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (39, 'A_SCHEDULE', '獨立排程', 'CALL_API1', '呼叫digiRunner API', 120, NULL, 'chb', 'chbapi', NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (39, 'A_SCHEDULE', 'Independent scheduling', 'CALL_API1', 'Call digiRunner API', 120, 'en-US', NULL, 'chb', 'chbapi', NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (40, 'A_SCHEDULE', '獨立排程', 'CALL_API2', '呼叫digiRunner 入口網 API', 121, NULL, 'token_id2', 'token_pwd2', NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (40, 'A_SCHEDULE', 'Independent scheduling', 'CALL_API2', 'Call the digiRunner portal API', 121, 'en-US', NULL, 'token_id2', 'token_pwd2', NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (41, 'ORG_FLAG', '組織原則', '0', '本組織向下', 130, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (41, 'ORG_FLAG', 'Organization Principle', '0', 'The organization down', 130, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (42, 'ORG_FLAG', '組織原則', '1', '全部組織', 131, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (42, 'ORG_FLAG', 'Organization Principle', '1', 'All organizations', 131, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (43, 'EVENT_TYPE', '事件類型', 'METHOD_INPUT', '接收參數', 150, NULL, '1', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (43, 'EVENT_TYPE', 'Event type', 'METHOD_INPUT', 'Receive parameters', 150, 'en-US', NULL, '1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (44, 'EVENT_TYPE', '事件類型', 'METHOD_CHECK', '參數檢查結果', 151, NULL, '1', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (44, 'EVENT_TYPE', 'Event type', 'METHOD_CHECK', 'Parameter check result', 151, 'en-US', NULL, '1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (45, 'EVENT_TYPE', '事件類型', 'METHOD_START', '開始執行工作', 152, NULL, '1', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (45, 'EVENT_TYPE', 'Event type', 'METHOD_START', 'Start work', 152, 'en-US', NULL, '1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (46, 'EVENT_TYPE', '事件類型', 'INFO', '顯示訊息', 153, NULL, '1', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (46, 'EVENT_TYPE', 'Event type', 'INFO', 'Show message', 153, 'en-US', NULL, '1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (47, 'EVENT_TYPE', '事件類型', 'METHOD_OUTPUT', '產生回傳值', 154, NULL, '1', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (47, 'EVENT_TYPE', 'Event type', 'METHOD_OUTPUT', 'Generate return value', 154, 'en-US', NULL, '1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (48, 'EVENT_TYPE', '事件類型', 'METHOD_END', '結束執行工作', 155, NULL, '1', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (48, 'EVENT_TYPE', 'Event type', 'METHOD_END', 'End execution', 155, 'en-US', NULL, '1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (49, 'EVENT_TYPE', '事件類型', 'ERROR', '未知的錯誤', 156, NULL, '0', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (49, 'EVENT_TYPE', 'Event type', 'ERROR', 'Unknown error', 156, 'en-US', NULL, '0', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (50, 'EVENT_NAME', '事件名稱', 'UPLD_MODULE', '上傳MODULE', 180, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (50, 'EVENT_NAME', 'Event name', 'UPLD_MODULE', 'Upload module', 180, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (51, 'EVENT_NAME', '事件名稱', 'SCHED_RUN', '排程工作生效', 181, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (51, 'EVENT_NAME', 'Event name', 'SCHED_RUN', 'Scheduled work takes effect', 181, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (52, 'EVENT_NAME', '事件名稱', 'SCHED_REFREH', '排程刷新', 182, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (52, 'EVENT_NAME', 'Event name', 'SCHED_REFREH', 'Schedule refresh', 182, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (53, 'SCHED_CATE1', '排程大分類', 'API_ON_OFF', 'API上下架管理', 300, NULL, '5', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (53, 'SCHED_CATE1', 'Schedule big classification', 'API_ON_OFF', 'API drop-off management', 300, 'en-US', NULL, '5', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (54, 'SCHED_CATE1', '排程大分類', 'A_SCHEDULE', '獨立排程', 301, NULL, '11', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (54, 'SCHED_CATE1', 'Schedule big classification', 'A_SCHEDULE', 'Independent scheduling', 301, 'en-US', NULL, '11', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (55, 'SCHED_CATE1', '排程大分類', 'CLIENT_REG', '用戶註冊', 302, NULL, '-1', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (55, 'SCHED_CATE1', 'Schedule big classification', 'CLIENT_REG', 'User registration', 302, 'en-US', NULL, '-1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (56, 'SCHED_CATE1', '排程大分類', 'SEND_MAIL', '寄送信件', 303, NULL, '-1', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (56, 'SCHED_CATE1', 'Schedule big classification', 'SEND_MAIL', 'Send letter', 303, 'en-US', NULL, '-1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (57, 'SEND_MAIL', '寄送信件', 'SEND', '寄送', 190, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (57, 'SEND_MAIL', 'Send letter', 'SEND', 'send', 190, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (58, 'NEWS_TYPE', '公告類型', 'TSP', '合作TSP業者', 33, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (58, 'NEWS_TYPE', 'Announcement type', 'TSP', 'Cooperating with TSP operators', 33, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (59, 'NEWS_TYPE', '公告類型', 'SYSTEM', '系統公告', 34, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (59, 'NEWS_TYPE', 'Announcement type', 'SYSTEM', 'system notification', 34, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (60, 'SCHED_MSG', '排程訊息', 'MANUAL_EXEC', '手動執行', 500, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (60, 'SCHED_MSG', 'Scheduling message', 'MANUAL_EXEC', 'Manual execution', 500, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (61, 'SCHED_MSG', '排程訊息', 'API_PARA', '已取得呼叫API的參數檔', 501, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (61, 'SCHED_MSG', 'Scheduling message', 'API_PARA', 'The parameter file for calling API has been obtained', 501, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (62, 'SCHED_MSG', '排程訊息', 'END_OF_CALL', '呼叫結束', 502, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (62, 'SCHED_MSG', 'Scheduling message', 'END_OF_CALL', 'End of call', 502, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (63, 'SCHED_MSG', '排程訊息', 'SUCCESS', '成功', 503, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (63, 'SCHED_MSG', 'Scheduling message', 'SUCCESS', 'success', 503, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (64, 'SCHED_MSG', '排程訊息', 'PREP_API_ON', '準備上架API', 504, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (64, 'SCHED_MSG', 'Scheduling message', 'PREP_API_ON', 'Ready to list API', 504, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (65, 'SCHED_MSG', '排程訊息', 'PREP_API_OFF', '準備下架API', 505, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (65, 'SCHED_MSG', 'Scheduling message', 'PREP_API_OFF', 'Ready to delist API', 505, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (66, 'SCHED_MSG', '排程訊息', 'PREP_API_UPDATE', '準備異動API', 506, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (66, 'SCHED_MSG', 'Scheduling message', 'PREP_API_UPDATE', 'Prepare transaction API', 506, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (67, 'SCHED_MSG', '排程訊息', 'WAIT_SEND', '等待寄出通知信', 507, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (67, 'SCHED_MSG', 'Scheduling message', 'WAIT_SEND', 'Waiting for the notification letter to be sent', 507, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (68, 'SCHED_MSG', '排程訊息', 'PREP_SEND', '準備寄出通知信', 508, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (68, 'SCHED_MSG', 'Scheduling message', 'PREP_SEND', 'Ready to send notification letter', 508, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (69, 'ORDERM_QUY_TYPE', '申請審核單查詢類別', 'REQ', '申請單', 140, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (69, 'ORDERM_QUY_TYPE', 'Application review list query category', 'REQ', 'Requisition', 140, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (70, 'ORDERM_QUY_TYPE', '申請審核單查詢類別', 'EXA', '待審單', 141, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (70, 'ORDERM_QUY_TYPE', 'Application review list query category', 'EXA', 'Pending order', 141, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (71, 'ORDERM_QUY_TYPE', '申請審核單查詢類別', 'REV', '已審單', 142, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (71, 'ORDERM_QUY_TYPE', 'Application review list query category', 'REV', 'Reviewed', 142, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (72, 'SCHED_MSG', '排程訊息', 'MANUAL_CANCEL', '手動取消', 509, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (72, 'SCHED_MSG', 'Scheduling message', 'MANUAL_CANCEL', 'Manual cancellation', 509, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (73, 'SCHED_MSG', '排程訊息', 'PREP_API_APPLIC', '準備更新API授權', 510, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (73, 'SCHED_MSG', 'Scheduling message', 'PREP_API_APPLIC', 'Ready to update API authorization', 510, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (74, 'SCHED_MSG', '排程訊息', 'PREP_CLIENT_REG', '準備更新用戶註冊狀態', 511, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (74, 'SCHED_MSG', 'Scheduling message', 'PREP_CLIENT_REG', 'Ready to update user registration status', 511, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (76, 'CERT_TYPE', '憑證管理類型', 'JWE', 'JWE加密憑證', 600, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (76, 'CERT_TYPE', 'Credential management type', 'JWE', 'JWE encryption certificate', 600, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (77, 'CERT_TYPE', '憑證管理類型', 'TLS', 'TLS通訊憑證', 601, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (77, 'CERT_TYPE', 'Credential management type', 'TLS', 'TLS communication certificate', 601, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (78, 'SCHED_CATE1', '排程大分類', 'API_APPLICATION', '用戶API申請', 304, NULL, '-1', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (78, 'SCHED_CATE1', 'Schedule big classification', 'API_APPLICATION', 'User API application', 304, 'en-US', NULL, '-1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (79, 'RTN_CODE_LOCALE', '回覆代碼語言地區', 'en-US', '英語-美國', 1000, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (79, 'RTN_CODE_LOCALE', 'Reply code locale', 'en-US', 'English-United States', 1000, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (80, 'RTN_CODE_LOCALE', '回覆代碼語言地區', 'zh-TW', '中文-台灣地區', 1001, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (80, 'RTN_CODE_LOCALE', 'Reply code locale', 'zh-TW', 'Chinese-Taiwan', 1001, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (85, 'REVIEW_TYPE', '簽核類別', 'OPEN_API_KEY', 'Open API Key 管理', 64, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (85, 'REVIEW_TYPE', 'Approval category', 'OPEN_API_KEY', 'Open API Key Management', 47, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (86, 'OPEN_API_KEY', 'Open API Key 管理', 'OPEN_API_KEY_APPLICA', 'Open API Key 申請', 700, NULL, 'KA', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (86, 'OPEN_API_KEY', 'Open API Key Management', 'OPEN_API_KEY_APPLICA', 'Open API Key application', 700, 'en-US', NULL, 'KA', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (87, 'OPEN_API_KEY', 'Open API Key 管理', 'OPEN_API_KEY_UPDATE', 'Open API Key 異動', 701, NULL, 'KU', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (87, 'OPEN_API_KEY', 'Open API Key Management', 'OPEN_API_KEY_UPDATE', 'Open API Key change', 701, 'en-US', NULL, 'KU', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (88, 'OPEN_API_KEY', 'Open API Key 管理', 'OPEN_API_KEY_REVOKE', 'Open API Key 撤銷', 702, NULL, 'KR', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (88, 'OPEN_API_KEY', 'Open API Key Management', 'OPEN_API_KEY_REVOKE', 'Open API Key revocation', 702, 'en-US', NULL, 'KR', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (89, 'SCHED_CATE1', '排程大分類', 'OPEN_API_KEY', 'Open API Key 管理', 305, NULL, '21', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (89, 'SCHED_CATE1', 'Schedule big classification', 'OPEN_API_KEY', 'Open API Key Management', 305, 'en-US', NULL, '21', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (90, 'API_AUTHORITY', 'API露出權限', '-1', '對內', 83, NULL, '-1', '2', NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (90, 'API_AUTHORITY', 'API exposure', '-1', 'Internally', 83, 'en-US', NULL, '-1', '2', NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (91, 'EVENT_NAME', '事件名稱', 'DC_REFRESH', 'DC更新', 183, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (91, 'EVENT_NAME', 'Event name', 'DC_REFRESH', 'DC update', 183, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (92, 'EVENT_NAME', '事件名稱', 'HOUSEKEEPING', '倉庫封存', 184, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (92, 'EVENT_NAME', 'Event name', 'HOUSEKEEPING', 'Warehouse storage', 184, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (93, 'SCHED_MSG', '排程訊息', 'PREP_OAK_APPLICA', '準備建立 Open API Key', 512, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (93, 'SCHED_MSG', 'Scheduling message', 'PREP_OAK_APPLICA', 'Prepare to create Open API Key', 512, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (94, 'SCHED_MSG', '排程訊息', 'PREP_OAK_UPDATE', '準備異動 Open API Key', 513, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (94, 'SCHED_MSG', 'Scheduling message', 'PREP_OAK_UPDATE', 'Prepare to change Open API Key', 513, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (95, 'SCHED_MSG', '排程訊息', 'PREP_OAK_REVOKE', '準備撤銷 Open API Key', 514, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (95, 'SCHED_MSG', 'Scheduling message', 'PREP_OAK_REVOKE', 'Ready to revoke Open API Key', 514, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (96, 'RT_MAP_LIST_TYPE', '角色TXID對應檔名單類型', 'W', '白名單', 1050, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (96, 'RT_MAP_LIST_TYPE', 'Character TXID corresponding file list type', 'W', 'whitelist', 1050, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (97, 'RT_MAP_LIST_TYPE', '角色TXID對應檔名單類型', 'B', '黑名單', 1051, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (97, 'RT_MAP_LIST_TYPE', 'Character TXID corresponding file list type', 'B', 'blacklist', 1051, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (98, 'SCHED_MSG', '排程訊息', 'PREP_OAK_CHK_EXPI', '準備檢查快到期 Open API Key', 515, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (98, 'SCHED_MSG', 'Scheduling message', 'PREP_OAK_CHK_EXPI', 'Ready to check the expiration Open API Key', 515, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (99, 'SCHED_CATE1', '排程大分類', 'OAK_CHK_EXPI', 'Open API Key 快到期檢查', 306, NULL, '-1', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (99, 'SCHED_CATE1', 'Schedule big classification', 'OAK_CHK_EXPI', 'Open API Key expiration check', 306, 'en-US', NULL, '-1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (100, 'OAK_PARA', 'Open API Key 參數', 'A', '使用次數上限_效期天數', 1100, NULL, '100', '60', NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (100, 'OAK_PARA', 'Open API Key parameters', 'A', 'Maximum number of uses_number of days of validity', 1100, 'en-US', NULL, '100', '60', NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (101, 'TIME_UNIT', '時間單位', 's', '秒', 1105, 'V', '1', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (101, 'TIME_UNIT', 'time unit', 's', 'second', 1105, 'en-US', 'V', '1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (102, 'TIME_UNIT', '時間單位', 'm', '分鐘', 1106, NULL, '60', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (102, 'TIME_UNIT', 'time unit', 'm', 'minute', 1106, 'en-US', NULL, '60', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (103, 'TIME_UNIT', '時間單位', 'H', '小時', 1107, NULL, '60', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (103, 'TIME_UNIT', 'time unit', 'H', 'hour', 1107, 'en-US', NULL, '60', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (104, 'TIME_UNIT', '時間單位', 'd', '天', 1108, NULL, '24', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (104, 'TIME_UNIT', 'time unit', 'd', 'day', 1108, 'en-US', NULL, '24', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (105, 'EVENT_NAME', '事件名稱', 'NODE_TASK_NOTIFIERS', '呼叫 Tsmp Module SDK 的 NodeTaskNotifiers', 185, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (105, 'EVENT_NAME', 'Event name', 'NODE_TASK_NOTIFIERS', 'Call NodeTaskNotifiers of Tsmp Module SDK', 185, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (106, 'DB_CACHE_NAME', '資料庫快取分類名稱', 'userrolfunc', '使用者、角色與功能', 1131, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (106, 'DB_CACHE_NAME', 'Database cache category name', 'userrolfunc', 'Users, roles and functions', 1131, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (107, 'DB_CACHE_NAME', '資料庫快取分類名稱', 'clientgroupapi', '用戶端、群組與API', 1132, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (107, 'DB_CACHE_NAME', 'Database cache category name', 'clientgroupapi', 'Clients, groups and APIs', 1132, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (108, 'DB_CACHE_NAME', '資料庫快取分類名稱', 'systemothers', '系統與其他分類', 1133, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (108, 'DB_CACHE_NAME', 'Database cache category name', 'systemothers', 'System and other categories', 1133, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (109, 'REG_SRC', 'API註冊來源', '0', '自訂', 1139, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (109, 'REG_SRC', 'API registration source', '0', 'Custom', 1139, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (111, 'REG_SRC', 'API註冊來源', '2', 'OpenAPI Spec', 1136, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (111, 'REG_SRC', 'API registration source', '2', 'OpenAPI Spec', 1136, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (112, 'API_DATA_FORMAT', 'API資料格式', '0', 'SOAP', 1144, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (112, 'API_DATA_FORMAT', 'API data format', '0', 'SOAP', 1144, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (113, 'API_DATA_FORMAT', 'API資料格式', '1', 'JSON', 1145, 'V', NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (113, 'API_DATA_FORMAT', 'API data format', '1', 'JSON', 1145, 'en-US', 'V', NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (114, 'NODE_HEALTH', '節點健康狀況', 'success', '良好', 1125, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (114, 'NODE_HEALTH', 'Node health', 'success', 'good', 1125, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (115, 'NODE_HEALTH', '節點健康狀況', 'warning', '警示', 1126, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (115, 'NODE_HEALTH', 'Node health', 'warning', 'Warning', 1126, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (116, 'NODE_HEALTH', '節點健康狀況', 'danger', '危險', 1127, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (116, 'NODE_HEALTH', 'Node health', 'danger', 'Danger', 1127, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (117, 'SCHED_CATE1', '排程大分類', 'SYNC_DATA1', '恢復原廠設定', 307, NULL, '-1', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (117, 'SCHED_CATE1', 'Schedule big classification', 'SYNC_DATA1', 'Reset to default settings', 307, 'en-US', NULL, '-1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (118, 'API_DATA_FORMAT', 'API資料格式', '2', 'XML', 1146, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (118, 'API_DATA_FORMAT', 'API data format', '2', 'XML', 1146, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (119, 'API_JWT_FLAG', 'API JWT設定', '0', '不使用', 1148, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (119, 'API_JWT_FLAG', 'API JWT settings', '0', 'Do not use', 1148, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (120, 'API_JWT_FLAG', 'API JWT設定', '1', 'JWE', 1149, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (120, 'API_JWT_FLAG', 'API JWT settings', '1', 'JWE', 1149, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (121, 'API_JWT_FLAG', 'API JWT設定', '2', 'JWS', 1150, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (121, 'API_JWT_FLAG', 'API JWT settings', '2', 'JWS', 1150, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (122, 'HOST_STATUS', '主機狀態', 'A', '啟動', 1141, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (122, 'HOST_STATUS', 'Host Status', 'A', 'start up', 1141, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (123, 'HOST_STATUS', '主機狀態', 'S', '停止', 1142, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (123, 'HOST_STATUS', 'Host Status', 'S', 'stop', 1142, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (124, 'AUTH_CODE_STATUS', '授權碼狀態', '0', '可用', 1151, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (124, 'AUTH_CODE_STATUS', 'Authorization code status', '0', 'Available', 1151, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (125, 'AUTH_CODE_STATUS', '授權碼狀態', '1', '已使用', 1152, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (125, 'AUTH_CODE_STATUS', 'Authorization code status', '1', 'Used', 1152, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (126, 'AUTH_CODE_STATUS', '授權碼狀態', '2', '失效', 1153, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (126, 'AUTH_CODE_STATUS', 'Authorization code status', '2', 'Invalidation', 1153, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (127, 'API_SRC', 'API來源', 'C', '組合', 1154, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (127, 'API_SRC', 'API source', 'C', 'combination', 1154, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (128, 'API_SRC', 'API來源', 'N', '.NET模組', 1155, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (128, 'API_SRC', 'API source', 'N', '.NET module', 1155, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (129, 'API_SRC', 'API來源', 'M', 'Java模組', 1156, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (129, 'API_SRC', 'API source', 'M', 'Java module', 1156, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (130, 'API_SRC', 'API來源', 'R', '註冊', 1157, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (130, 'API_SRC', 'API source', 'R', 'registered', 1157, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (131, 'REPORT_LABLE_CODE', '報表標籤代碼', 'Y', '成功', 1158, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (131, 'REPORT_LABLE_CODE', 'Report label code', 'Y', 'success', 1158, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (132, 'REPORT_LABLE_CODE', '報表標籤代碼', 'N', '失敗', 1159, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (132, 'REPORT_LABLE_CODE', 'Report label code', 'N', 'failure', 1159, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (133, 'REPORT_LABLE_CODE', '報表標籤代碼', 'FREQUENCY', '次數', 1160, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (133, 'REPORT_LABLE_CODE', 'Report label code', 'FREQUENCY', 'frequency', 1160, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (134, 'REPORT_LABLE_CODE', '報表標籤代碼', 'MILLISECOND', '毫秒', 1161, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (134, 'REPORT_LABLE_CODE', 'Report label code', 'MILLISECOND', 'millisecond', 1161, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (135, 'REPORT_LABLE_CODE', '報表標籤代碼', 'TIME', '時間', 1162, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (135, 'REPORT_LABLE_CODE', 'Report label code', 'TIME', 'time', 1162, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (139, 'SCHED_CATE1', '排程大分類', 'REPORT_BATCH', '報表排程', 308, NULL, -1, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (139, 'SCHED_CATE1', 'Schedule big classification', 'REPORT_BATCH', 'Report schedule', 308, 'en-US', NULL, '-1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (143, 'API_IMP_CHECK_ACT', '匯入API檢查動作', 'C', '新增', 1196, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (143, 'API_IMP_CHECK_ACT', 'Import API check action', 'C', 'Add', 1196, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (144, 'API_IMP_CHECK_ACT', '匯入API檢查動作', 'U', '更新', 1197, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (144, 'API_IMP_CHECK_ACT', 'Import API check action', 'U', 'Update', 1197, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (145, 'API_IMP_CHECK_ACT', '匯入API檢查動作', 'N', '無法操作', 1198, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (145, 'API_IMP_CHECK_ACT', 'Import API check action', 'N', 'Inoperable', 1198, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (146, 'API_IMP_RESULT', '匯入API結果', 'S', '成功', 1201, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (146, 'API_IMP_RESULT', 'Import API results', 'S', 'success', 1201, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (147, 'API_IMP_RESULT', '匯入API結果', 'F', '失敗', 1202, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (147, 'API_IMP_RESULT', 'Import API results', 'F', 'failure', 1202, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (148, 'API_IMP_RESULT', '匯入API結果', 'I', '初始', 1203, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (148, 'API_IMP_RESULT', 'Import API results', 'I', 'initial', 1203, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (149, 'REPORT_TIME_TYPE', '報表時間類型', 'DAY', '天', 1211, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (149, 'REPORT_TIME_TYPE', 'Report time type', 'DAY', 'day', 1211, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (150, 'REPORT_TIME_TYPE', '報表時間類型', 'MONTH', '月', 1212, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (150, 'REPORT_TIME_TYPE', 'Report time type', 'MONTH', 'month', 1212, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (151, 'REPORT_NAME', '報表名稱', 'API_USAGE_STATISTICS', 'API使用次數統計', 1225, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (151, 'REPORT_NAME', 'Report name', 'API_USAGE_STATISTICS', 'API usage statistics', 1225, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (152, 'REPORT_NAME', '報表名稱', 'API_TIMESANDTIME', 'API次數-時間分析', 1226, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (152, 'REPORT_NAME', 'Report name', 'API_TIMESANDTIME', 'API times-time analysis', 1226, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (153, 'REPORT_NAME', '報表名稱', 'API_AVERAGETIME', 'API平均時間計算分析', 1227, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (153, 'REPORT_NAME', 'Report name', 'API_AVERAGETIME', 'API average time calculation analysis', 1227, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (154, 'REPORT_NAME', '報表名稱', 'API_TRAFFIC', 'API流量分析', 1228, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (154, 'REPORT_NAME', 'Report name', 'API_TRAFFIC', 'API traffic analysis', 1228, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (155, 'REPORT_NAME', '報表名稱', 'BADATTEMPT', 'Bad Attempt連線報告', 1229, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (155, 'REPORT_NAME', 'Report name', 'BADATTEMPT', 'Bad Attempt connection report', 1229, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (156, 'RESULT_FLAG', '成功/失敗', '1', '成功', 1260, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (156, 'RESULT_FLAG', 'success/failure', '1', 'success', 1260, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (157, 'RESULT_FLAG', '成功/失敗', '0', '失敗', 1261, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (157, 'RESULT_FLAG', 'success/failure', '0', 'failure', 1261, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (158, 'RESULT_FLAG', '成功/失敗', '-1', '全部', 1262, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (158, 'RESULT_FLAG', 'success/failure', '-1', 'All', 1262, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (159, 'MAIL_TIME_TYPE', '信件時間類型', 'SEC', '秒', 1270, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (159, 'MAIL_TIME_TYPE', 'Mail Time Type', 'SEC', 'sec', 1270, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (160, 'MAIL_TIME_TYPE', '信件時間類型', 'MINS', '分', 1271, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (160, 'MAIL_TIME_TYPE', 'Mail Time Type', 'MINS', 'mins', 1271, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (161, 'MAIL_TIME_TYPE', '信件時間類型', 'HRS', '小時', 1272, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (161, 'MAIL_TIME_TYPE', 'Mail Time Type', 'HRS', 'hrs', 1272, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (162, 'MAIL_TIME_TYPE', '信件時間類型', 'DAYS', '天', 1273, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (162, 'MAIL_TIME_TYPE', 'Mail Time Type', 'DAYS', 'days', 1273, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (163, 'OPEN_API_KEY', 'Open API Key 管理', 'OVERDUE', '逾期', 703, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (163, 'OPEN_API_KEY', 'Open API Key Management', 'OVERDUE', 'Overdue', 703, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (164, 'OPEN_API_KEY', 'Open API Key 管理', 'RENEWED', '已展期', 704, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (164, 'OPEN_API_KEY', 'Open API Key Management', 'RENEWED', 'Renewed', 704, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (165, 'HOUSEKEEPING', '資料管理', 'short', '短期', 1280, 'zh-TW', NULL, '30', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (165, 'HOUSEKEEPING', 'housekeeping', 'short', 'Short term', 1280, 'en-US', NULL, '30', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (166, 'HOUSEKEEPING', '資料管理', 'mid', '中期', 1281, 'zh-TW', NULL, '60', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (166, 'HOUSEKEEPING', 'housekeeping', 'mid', 'Mid term', 1281, 'en-US', NULL, '60', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (167, 'HOUSEKEEPING', '資料管理', 'long', '長期', 1282, 'zh-TW', NULL, '90', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (167, 'HOUSEKEEPING', 'housekeeping', 'long', 'long term', 1282, 'en-US', NULL, '90', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (168, 'HOUSEKEEPING', '資料管理', 'gov_short', '政府保存年限短期', 1283, 'zh-TW', NULL, '1825', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (168, 'HOUSEKEEPING', 'housekeeping', 'gov_short', 'Government retention period is short-term', 1283, 'en-US', NULL, '1825', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (169, 'HOUSEKEEPING', '資料管理', 'gov_long', '政府保存年限長期', 1284, 'zh-TW', NULL, '3650', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (169, 'HOUSEKEEPING', 'housekeeping', 'gov_long', 'Long-term government retention', 1284, 'en-US', NULL, '3650', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (170, 'SCHED_CATE1', '排程大分類', 'HOUSEKEEPING_BATCH', 'Housekeeping排程', 309, 'zh-TW', NULL, '-1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (170, 'SCHED_CATE1', 'Schedule big classification', 'HOUSEKEEPING_BATCH', 'Housekeeping schedule', 309, 'en-US', NULL, '-1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (171, 'REG_MODULE_SRC', '註冊模組建立來源', '1', 'WSDL', 1301, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (171, 'REG_MODULE_SRC', 'Registered Module Source', '1', 'WSDL', 1301, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (172, 'REG_MODULE_SRC', '註冊模組建立來源', '2', 'OAS 2.0', 1302, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (172, 'REG_MODULE_SRC', 'Registered Module Source', '2', 'OAS 2.0', 1302, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (173, 'REG_MODULE_SRC', '註冊模組建立來源', '3', 'OAS 3.0', 1303, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (173, 'REG_MODULE_SRC', 'Registered Module Source', '3', 'OAS 3.0', 1303, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (174, 'SCHED_CATE1', '排程大分類', 'NOTICE_EXP_CERT', '憑證到期通知', 310, 'zh-TW', NULL, '43', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (174, 'SCHED_CATE1', 'Schedule big classification', 'NOTICE_EXP_CERT', 'Notice Of Expiration Of Certification', 310, 'en-US', NULL, '43', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (175, 'NOTICE_EXP_CERT', '憑證到期通知', 'JWE', 'JWE加密憑證到期通知', 1306, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (175, 'NOTICE_EXP_CERT', 'Notice Of Expiration Of Certification', 'JWE', 'Notice Of Expiration Of JWE Certification', 1306, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (176, 'NOTICE_EXP_CERT', '憑證到期通知', 'TLS', 'TLS通訊憑證到期通知', 1307, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (176, 'NOTICE_EXP_CERT', 'Notice Of Expiration Of Certification', 'TLS', 'Notice Of Expiration Of TLS Certification', 1307, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (177, 'SCHED_CATE1', '排程大分類', 'CUS_INVOKE', '客製介接調用API', 311, 'zh-TW', NULL, '-1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (177, 'SCHED_CATE1', 'Schedule big classification', 'CUS_INVOKE', 'Customized interface invoke API', 311, 'en-US', NULL, '-1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (178, 'A_SCHEDULE', '獨立排程', 'CALL_API_CUS', '呼叫客製化API', 122, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (178, 'A_SCHEDULE', 'Independent scheduling', 'CALL_API_CUS', 'Call customized API', 122, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (181, 'ES_INDEX_EXCLUDER', '索引名稱排除條件', 'DPB0126', 'DPB0126', 1330, 'zh-TW', NULL, 'XihcLnx0c21wfGVycm9yKS4qJA==', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (181, 'ES_INDEX_EXCLUDER', 'Index excluder', 'DPB0126', 'DPB0126', 1330, 'en-US', NULL, 'XihcLnx0c21wfGVycm9yKS4qJA==', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (182, 'ES_INDEX_FLAG', 'ES Index開關', 'OPEN', '啟用', 1350, 'zh-TW', NULL, '1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (182, 'ES_INDEX_FLAG', 'Set ES Index open or close', 'OPEN', 'Open', 1350, 'en-US', NULL, '1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (183, 'ES_INDEX_FLAG', 'ES Index開關', 'CLOSE', '關閉', 1351, 'zh-TW', NULL, '0', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (183, 'ES_INDEX_FLAG', 'Set ES Index open or close', 'CLOSE', 'Close', 1351, 'en-US', NULL, '0', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (184, 'SCHED_CATE1', '排程大分類', 'RUNLOOP', '循環排程', 313, 'zh-TW', NULL, '47', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (184, 'SCHED_CATE1', 'Schedule big classification', 'RUNLOOP', 'Job Loop', 313, 'en-US', NULL, '47', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (185, 'RUNLOOP', '循環排程', 'SYS_MONITOR', '系統監控', 1352, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (185, 'RUNLOOP', 'Job Loop', 'SYS_MONITOR', 'System Monitor', 1352, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (186, 'SCHED_CATE1', '排程大分類', 'BROADCAST', '廣播排程', 314, 'zh-TW', NULL, '48', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (186, 'SCHED_CATE1', 'Schedule big classification', 'BROADCAST', 'Job Broadcast', 314, 'en-US', NULL, '48', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (187, 'BROADCAST', '廣播排程', 'RESTART_DGR_MODULE', '重啟dgR模組', 1401, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (187, 'BROADCAST', 'Job Broadcast', 'RESTART_DGR_MODULE', 'Restart dgR Module', 1401, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (188, 'SCHED_CATE1', '排程大分類', 'RESTART_DGR_MODULE', '重啟dgR模組', 315, 'zh-TW', NULL, '-1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (188, 'SCHED_CATE1', 'Schedule big classification', 'RESTART_DGR_MODULE', 'Restart dgR Module', 315, 'en-US', NULL, '-1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (198, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'login', '登入', 1480, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (198, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'login', 'Login', 1480, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (199, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'logout', '登出', 1481, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (199, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'logout', 'Logout', 1481, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (200, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'addUser', '新增使用者', 1482, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (200, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'addUser', 'Add User', 1482, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (201, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'deleteUser', '刪除使用者', 1483, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (201, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'deleteUser', 'Delete User', 1483, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (202, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'updateUser', '更新使用者', 1484, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (202, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'updateUser', 'Update User', 1484, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (203, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'addRole', '新增角色', 1485, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (203, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'addRole', 'Add Role', 1485, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (204, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'deleteRole', '刪除角色', 1486, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (204, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'deleteRole', 'Delete Role', 1486, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (205, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'updateRole', '更新角色', 1487, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (205, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'updateRole', 'Update Role', 1487, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (206, 'TABLE_ACT', '資料表動作', 'C', '新增', 1600, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (206, 'TABLE_ACT', '資料表動作', 'C', 'Add', 1600, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (207, 'TABLE_ACT', '資料表動作', 'U', '更新', 1601, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (207, 'TABLE_ACT', '資料表動作', 'U', 'Update', 1601, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (208, 'TABLE_ACT', '資料表動作', 'D', '刪除', 1602, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (208, 'TABLE_ACT', '資料表動作', 'D', 'Delete', 1602, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (209, 'MOCK_CONFIG', 'Mock調用(Y) or 直接調用(N)', 'DPB0123Udp', 'Udp 確認User是否有登入', 1311, 'zh-TW', NULL, 'N', 'https://127.0.0.1:38452/tsmpdpaa/udpssotoken/DPB0123Udp', '10', NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (209, 'MOCK_CONFIG', 'Mock invoke(Y) or direct invoke(N)', 'DPB0123Udp', 'Udp Double check login', 1311, 'en-US', NULL, 'N', 'https://127.0.0.1:38452/tsmpdpaa/udpssotoken/DPB0123Udp', '10', NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (210, 'UDPSSO', 'UDPSSO環境', 'ENV1', '預設工作區', 1610, 'zh-TW', NULL, 'https://10.20.30.162:38452/tsmpac3/#/udpsso/', '192.168.0.0/23,127.0.0.1/23', NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (210, 'UDPSSO', 'UDPSSO環境', 'ENV1', 'Default Workspace', 1610, 'en-US', NULL, 'https://10.20.30.162:38452/tsmpac3/#/udpsso/', '192.168.0.0/23,127.0.0.1/23', NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (211, 'UDPSSO', 'UDPSSO環境', 'ENV2', '渣打工作區', 1611, 'zh-TW', NULL, 'https://10.20.30.162:38452/dgr-cus-scbank/#/udpsso/', '192.168.0.0/23,127.0.0.1/23', NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (211, 'UDPSSO', 'UDPSSO環境', 'ENV2', 'SCB Workspace', 1611, 'en-US', NULL, 'https://10.20.30.162:38452/dgr-cus-scbank/#/udpsso/', '192.168.0.0/23,127.0.0.1/23', NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (213, 'SCHED_CATE1', '排程大分類', 'CREATE_REPORT', '產生報表', 316, 'zh-TW', NULL, '-1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (213, 'SCHED_CATE1', 'Schedule big classification', 'CREATE_REPORT', 'Create report', 316, 'en-US', NULL, '-1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (214, 'SCHED_CATE1', '排程大分類', 'SCB_DISABLE_USER', '停用閒置使用者', 317, 'zh-TW', NULL, '-1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (214, 'SCHED_CATE1', 'Schedule big classification', 'SCB_DISABLE_USER', 'Disable idle users', 317, 'en-US', NULL, '-1', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (219, 'FILE_CATE_CODE', '檔案分類', 'API', 'API', 1660, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (219, 'FILE_CATE_CODE', '檔案分類', 'API', 'API', 1660, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (220, 'FILE_CATE_CODE', '檔案分類', 'API_TH', '主題圖示', 1661, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (220, 'FILE_CATE_CODE', '檔案分類', 'API_TH', '主題圖示', 1661, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (221, 'FILE_CATE_CODE', '檔案分類', 'API_ATTACHMENT', 'API說明文件', 1662, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (221, 'FILE_CATE_CODE', '檔案分類', 'API_ATTACHMENT', 'API說明文件', 1662, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (222, 'FILE_CATE_CODE', '檔案分類', 'APP_IMG', '實例圖示', 1663, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (222, 'FILE_CATE_CODE', '檔案分類', 'APP_IMG', '實例圖示', 1663, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (223, 'FILE_CATE_CODE', '檔案分類', 'APP_ATTACHMENT', '實例附件', 1664, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (223, 'FILE_CATE_CODE', '檔案分類', 'APP_ATTACHMENT', '實例附件', 1664, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (224, 'FILE_CATE_CODE', '檔案分類', 'DOC', 'DOC', 1665, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (224, 'FILE_CATE_CODE', '檔案分類', 'DOC', 'DOC', 1665, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (225, 'FILE_CATE_CODE', '檔案分類', 'DOC_API', 'DOC_API', 1666, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (225, 'FILE_CATE_CODE', '檔案分類', 'DOC_API', 'DOC_API', 1666, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (226, 'FILE_CATE_CODE', '檔案分類', 'DOC_GUIDELINE', 'API開發標準作業手冊', 1667, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (226, 'FILE_CATE_CODE', '檔案分類', 'DOC_GUIDELINE', 'API開發標準作業手冊', 1667, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (227, 'FILE_CATE_CODE', '檔案分類', 'FAQ_ATTACHMENT', '問答附件', 1668, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (227, 'FILE_CATE_CODE', '檔案分類', 'FAQ_ATTACHMENT', '問答附件', 1668, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (228, 'FILE_CATE_CODE', '檔案分類', 'MEMBER_APPLY', '會員申請上傳檔案', 1669, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (228, 'FILE_CATE_CODE', '檔案分類', 'MEMBER_APPLY', '會員申請上傳檔案', 1669, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (229, 'FILE_CATE_CODE', '檔案分類', 'D2_ATTACHMENT', '審查明細(TSMP_DP_REQ_ORDERD2)附件', 1670, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (229, 'FILE_CATE_CODE', '檔案分類', 'D2_ATTACHMENT', '審查明細(TSMP_DP_REQ_ORDERD2)附件', 1670, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (230, 'FILE_CATE_CODE', '檔案分類', 'TSMP_DP_REQ_ORDERM', '審查單(TSMP_DP_REQ_ORDERM)附件', 1671, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (230, 'FILE_CATE_CODE', '檔案分類', 'TSMP_DP_REQ_ORDERM', '審查單(TSMP_DP_REQ_ORDERM)附件', 1671, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (231, 'FILE_CATE_CODE', '檔案分類', 'MAIL_CONTENT', 'Mail內容檔案', 1672, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (231, 'FILE_CATE_CODE', '檔案分類', 'MAIL_CONTENT', 'Mail內容檔案', 1672, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (232, 'FILE_CATE_CODE', '檔案分類', 'TEMP', '暫存檔', 1673, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (232, 'FILE_CATE_CODE', '檔案分類', 'TEMP', '暫存檔', 1673, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (233, 'FILE_CATE_CODE', '檔案分類', 'KEY_PAIR', '公、私鑰', 1674, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (233, 'FILE_CATE_CODE', '檔案分類', 'KEY_PAIR', '公、私鑰', 1674, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (234, 'FILE_CATE_CODE', '檔案分類', 'REG_MODULE_DOC', '註冊模組介接規格文件', 1675, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (234, 'FILE_CATE_CODE', '檔案分類', 'REG_MODULE_DOC', '註冊模組介接規格文件', 1675, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (235, 'FILE_CATE_CODE', '檔案分類', 'DPB0082', '暫存檔(controller unit test)', 1676, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (235, 'FILE_CATE_CODE', '檔案分類', 'DPB0082', '暫存檔(controller unit test)', 1676, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (236, 'FILE_CATE_CODE', '檔案分類', 'REG_COMP_API', '註冊/組合API的匯出檔案', 1677, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (236, 'FILE_CATE_CODE', '檔案分類', 'REG_COMP_API', '註冊/組合API的匯出檔案', 1677, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (237, 'FILE_CATE_CODE', '檔案分類', 'TSMP_DP_APPT_JOB', '排程作業相關檔案', 1678, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (237, 'FILE_CATE_CODE', '檔案分類', 'TSMP_DP_APPT_JOB', '排程作業相關檔案', 1678, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (238, 'RUNLOOP', '循環排程', 'ALERT_KEYWORD', 'Keyword告警', 1353, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (238, 'RUNLOOP', 'Job Loop', 'ALERT_KEYWORD', 'Alert of Keyword', 1353, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (239, 'RUNLOOP', '循環排程', 'ALERT_SYSTEM_BASIC', '系統效能告警', 1354, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (239, 'RUNLOOP', 'Job Loop', 'ALERT_SYSTEM_BASIC', 'Alert of System', 1354, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (240, 'SCHED_CATE1', '排程大分類', 'DPAA_ALERT', '告警通知', 318, 'zh-TW', NULL, '56', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (240, 'SCHED_CATE1', 'Schedule big classification', 'DPAA_ALERT', 'Alert Notify', 318, 'en-US', NULL, '56', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (241, 'DPAA_ALERT', '告警通知', 'ROLE_EMAIL', '依角色寄送電子郵件', 1800, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (241, 'DPAA_ALERT', 'Alert Notify', 'ROLE_EMAIL', 'Send email acc. alert-role', 1800, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (242, 'DPAA_ALERT', '告警通知', 'LINE', 'LINE 告警', 1801, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (242, 'DPAA_ALERT', 'Alert Notify', 'LINE', 'LINE Notify', 1801, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
-- [END] TSMPDPAA 專門 --
--20221115 TSMP_DP_REQ_ORDERM 更改為 REQ_ORDER_NO varchar(30) Zoe_Lee
ALTER TABLE TSMP_DP_REQ_ORDERM ALTER COLUMN REQ_ORDER_NO varchar(30) ;

--20230330 TSMP_DP_REQ_ORDERM 更改為 REQ_USER_ID varchar(255) Zoe_Lee
ALTER TABLE TSMP_DP_REQ_ORDERM ALTER COLUMN REQ_USER_ID varchar(255) ;

--20230428 新增 TSMP_DP_ITEMS Kevin.Cheng
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (262, 'DP_AUTH_FLAG', '入口網-身份驗證狀態', '0', '是', 1825, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (262, 'DP_AUTH_FLAG', '入口網-身份驗證狀態', '0', 'Yes', 1825, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (263, 'DP_AUTH_FLAG', '入口網-身份驗證狀態', '1', '否', 1826, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (263, 'DP_AUTH_FLAG', '入口網-身份驗證狀態', '1', 'No', 1826, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (264, 'DP_PUBLIC_FLAG', '入口網-貨架狀態', 'E', '上架', 1830, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (264, 'DP_PUBLIC_FLAG', '入口網-貨架狀態', 'E', 'Listed', 1830, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (265, 'DP_PUBLIC_FLAG', '入口網-貨架狀態', 'D', '下架', 1831, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (265, 'DP_PUBLIC_FLAG', '入口網-貨架狀態', 'D', 'Unlisted', 1831, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (266, 'DP_PUBLIC_FLAG', '入口網-貨架狀態', 'ALL', '全部', 1832, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (266, 'DP_PUBLIC_FLAG', '入口網-貨架狀態', 'ALL', 'ALL', 1832, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (267, 'DP_DGRK_FLAG', '入口網-申請DGRK', 'Y', '是', 1840, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (267, 'DP_DGRK_FLAG', '入口網-申請DGRK', 'Y', 'Yes', 1840, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (268, 'DP_DGRK_FLAG', '入口網-申請DGRK', 'N', '否', 1841, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (268, 'DP_DGRK_FLAG', '入口網-申請DGRK', 'N', 'No', 1841, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (269, 'DP_DGRK_STATUS', '入口網-DGRK設定', 'Disabled', '停用', 1850, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (269, 'DP_DGRK_STATUS', '入口網-DGRK設定', 'Disabled', 'Disabled', 1850, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (270, 'DP_DGRK_STATUS', '入口網-DGRK設定', 'ReApply', '重新申請', 1851, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (270, 'DP_DGRK_STATUS', '入口網-DGRK設定', 'ReApply', 'Re-apply', 1851, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (271, 'DP_DGRK_STATUS', '入口網-DGRK設定', 'Rollover', '展期', 1852, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (271, 'DP_DGRK_STATUS', '入口網-DGRK設定', 'Rollover', 'Rollover', 1852, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (272, 'DP_DGRK_STATUS', '入口網-DGRK設定', 'Apply', '申請', 1853, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (272, 'DP_DGRK_STATUS', '入口網-DGRK設定', 'Apply', 'Apply', 1853, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (273, 'DP_USER_FLAG', '入口網-使用者身份', 'U', '一般使用者', 1860, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (273, 'DP_USER_FLAG', '入口網-使用者身份', 'U', 'common user', 1860, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (274, 'DP_USER_FLAG', '入口網-使用者身份', 'A', '管理者', 1861, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (274, 'DP_USER_FLAG', '入口網-使用者身份', 'A', 'manager', 1861, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (275, 'DP_USER_FLAG', '入口網-使用者身份', 'ALL', '全部', 1862, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (275, 'DP_USER_FLAG', '入口網-使用者身份', 'ALL', 'ALL', 1862, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);

--20240419 新增 TSMP_DP_ITEMS FUNC_TYPE Webber Luo
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (287, 'FUNC_TYPE', '功能類型', '0', '原功能', 1870, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (287, 'FUNC_TYPE', '功能類型', '0', 'TSMP function', 1870, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (288, 'FUNC_TYPE', '功能類型', '1', '嵌入功能', 1871, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (288, 'FUNC_TYPE', '功能類型', '1', 'Embeded function', 1871, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);

--20230508 新增 DP admin, Kevin.Cheng
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('DP_ADMIN', 'dpmanager', 'DP Default Admin ID TOKEN SUB');


-- 20230919, 來源IP 增加填寫Hostname, 增加欄位長度 , Zoe Lee
ALTER TABLE TSMP_CLIENT_HOST ALTER COLUMN HOST_IP nvarchar(255)  NOT NULL;

-- 20240619, 排程作業增加使用HttpUtil呼叫API , Webber Luo
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (309, 'SCHED_CATE1', '排程大分類', 'HTTP_UTIL_CALL', '使用HttpUtil呼叫API', 321, 'zh-TW', NULL, '68', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (309, 'SCHED_CATE1', 'Schedule big classification', 'HTTP_UTIL_CALL', 'Call API by HttpUtil', 321, 'en-US', NULL, '68', NULL, NULL, NULL, NULL);

INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (310, 'HTTP_UTIL_CALL', '認證類型', 'C_APIKEY', 'cAPI-KEY', 1950, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (310, 'HTTP_UTIL_CALL', 'Authentication Type', 'C_APIKEY', 'c-apikey authentication', 1950, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (311, 'HTTP_UTIL_CALL', '認證類型', 'NO_AUTH', 'NO-AUTH', 1951, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (311, 'HTTP_UTIL_CALL', 'Authentication Type', 'NO_AUTH', 'no auth authentication', 1951, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (312, 'HTTP_UTIL_CALL', '認證類型', 'BASIC', 'BASIC', 1952, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (312, 'HTTP_UTIL_CALL', 'Authentication Type', 'BASIC', 'basic authentication', 1952, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);

-- 20240812, 排程作業增加排程訊息 FAILED , Webber Luo
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (313, 'SCHED_MSG', '排程訊息', 'FAILED', '失敗', 526, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (313, 'SCHED_MSG', 'Scheduling message', 'FAILED', 'failed', 526, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);


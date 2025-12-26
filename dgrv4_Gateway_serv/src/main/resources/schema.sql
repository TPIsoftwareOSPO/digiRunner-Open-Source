
-- TSMP群組與API關聯資料
CREATE TABLE IF NOT EXISTS tsmp_group_api (
	group_id		VARCHAR(10) NOT NULL, 	-- Group ID
	api_key			VARCHAR(30) NOT NULL,	-- API KEY
	module_name		VARCHAR(100) NOT NULL,	-- Module Name
	module_version	VARCHAR(20) NULL,		-- 保留, default 0(latest version)
	create_time		DATETIME NOT NULL,		-- 建立時間
	PRIMARY KEY (group_id, api_key, module_name)
);

-- TSMP用戶端與群組關係
CREATE TABLE IF NOT EXISTS tsmp_client_group (
	client_id	VARCHAR(40) NOT NULL,
	group_id	VARCHAR(10) NOT NULL,
	PRIMARY KEY (client_id, group_id)
);

-- TSMP用戶端群組
CREATE TABLE IF NOT EXISTS tsmp_group (
	group_id	VARCHAR(10) NOT NULL,	-- 群組代碼: 系統中不可重複的TSMP群組代碼(系統預設自動產生，ex: 900000001)。
	group_name	VARCHAR(30) NOT NULL,	-- 群組名稱
	create_time	DATETIME NOT NULL,		-- 建立日期
	create_user	VARCHAR(255) NOT NULL,	-- 建立人員
	update_time	DATETIME NULL,	--異動日期
	update_user	VARCHAR(255) NULL,	-- 異動人員
	GROUP_ALIAS varchar(30) default null,
	GROUP_DESC varchar(60) default null,
	GROUP_ACCESS varchar(255) default null,
	SECURITY_LEVEL_ID varchar(10) default 'SYSTEM',
	ALLOW_DAYS int default 0,
	ALLOW_TIMES int default 0,
	VGROUP_FLAG char(1) NOT NULL DEFAULT 0,
	VGROUP_ID VARCHAR(10),
	VGROUP_NAME VARCHAR(30),
	PRIMARY KEY (group_id)
);

--TSMP_CLIENT_VGROUP
CREATE TABLE IF NOT EXISTS TSMP_CLIENT_VGROUP (
  CLIENT_ID VARCHAR(40) NOT NULL,
  VGROUP_ID VARCHAR(10) NOT NULL,
  PRIMARY KEY (CLIENT_ID,VGROUP_ID)
);

-- 客製擴展會員資料表: 會員資料,不記密碼, 含審核階段狀態【新增】,由tsmp平台管理會員資料, 此Table只記錄額外的客製資訊
CREATE TABLE IF NOT EXISTS tsmp_dp_clientext (
	client_id			VARCHAR(40) NOT NULL,	-- Client ID: 以Client ID進行驗證，ex. "YWRtaW5Db25zb2xl"
	client_seq_id		BIGINT NOT NULL,	-- ID(流水號): INT型態提供給DP使用, 系統自動產生的流水編號, 用以作為此筆資料的唯一識別值
	content_txt			NVARCHAR(1000) NOT NULL,	-- 申請會員說明
	reg_status			CHAR(1) NOT NULL DEFAULT '0',	-- 會員資格狀態=0：儲存，1：送審，2：放行，3：退回，4：重新送審
	pwd_status			CHAR(1) NOT NULL DEFAULT '1',	-- 密碼狀態=1：可使用，2：重置等待確認
	pwd_reset_key		VARCHAR(22) NULL,	-- 重置確認Key: base64(UUID ), 重置後給值, 確認後清除
	review_remark		VARCHAR(3000) NULL,	-- 會員資格審核備註=退回：要檢查「審核備註」要必填
	ref_review_user		NVARCHAR(255) NULL,	-- 審核人員: (關聯帳號資料 TSMP_USER), 執行審核才Update
	resubmit_date_time	DATETIME NULL,	-- 重新送審日期
	public_flag			CHAR(1) NULL,	-- 是否對公眾開放=0：全部，1：公開，2：私有，null：私有
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_date_time	DATETIME,	-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),	-- 更新人員
	version				INT DEFAULT 1,	-- 版號: C/U時, 增量+1
	keyword_search		NVARCHAR(4000),	-- LikeSearch使用: 申請內容說明|審核備註
	PRIMARY KEY (client_id),
	UNIQUE (client_seq_id)
);

--TSMP_GROUP_AUTHORITIES 核身
CREATE TABLE IF NOT EXISTS TSMP_GROUP_AUTHORITIES (
	GROUP_AUTHORITIE_ID VARCHAR(10) NOT NULL,
	GROUP_AUTHORITIE_NAME VARCHAR(30) NOT NULL,
	GROUP_AUTHORITIE_DESC VARCHAR(60),
	GROUP_AUTHORITIE_LEVEL VARCHAR(10),
	PRIMARY KEY (GROUP_AUTHORITIE_ID),
	UNIQUE (GROUP_AUTHORITIE_NAME)
);

--TSMP_GROUP_AUTHORITIES_MAP
CREATE TABLE IF NOT EXISTS TSMP_GROUP_AUTHORITIES_MAP (
     GROUP_ID VARCHAR(10) NOT NULL,
     GROUP_AUTHORITIE_ID VARCHAR(10) NOT NULL,
     PRIMARY KEY (GROUP_ID,GROUP_AUTHORITIE_ID)
);

--TSMP_VGROUP
CREATE TABLE IF NOT EXISTS TSMP_VGROUP (
  VGROUP_ID VARCHAR(10) NOT NULL,
  VGROUP_NAME VARCHAR(30) NOT NULL,
  VGROUP_ALIAS VARCHAR(30) NULL,
  VGROUP_DESC VARCHAR(60) NULL,
  VGROUP_ACCESS VARCHAR(255)  NULL,
  SECURITY_LEVEL_ID VARCHAR(10)  NULL,
  ALLOW_DAYS INT NOT NULL DEFAULT 0,
  ALLOW_TIMES INT NOT NULL DEFAULT 0,
  CREATE_USER VARCHAR(255) NOT NULL,
  CREATE_TIME DATETIME NOT NULL,
  UPDATE_TIME DATETIME NULL,
  UPDATE_USER VARCHAR(255) NULL,
  PRIMARY KEY (VGROUP_ID)
);

--TSMP_VGROUP_GROUP
CREATE TABLE IF NOT EXISTS TSMP_VGROUP_GROUP (
  VGROUP_ID VARCHAR(10),
  GROUP_ID VARCHAR(10),
  CREATE_TIME DATETIME NOT NULL,
  PRIMARY KEY (VGROUP_ID,GROUP_ID)
);

--TSMP_VGROUP_AUTHORITIES_MAP
CREATE TABLE IF NOT EXISTS TSMP_VGROUP_AUTHORITIES_MAP (
 	VGROUP_ID VARCHAR(10) NOT NULL,
 	VGROUP_AUTHORITIE_ID VARCHAR(10) NOT NULL,
 	PRIMARY KEY (VGROUP_ID,VGROUP_AUTHORITIE_ID)
);

-- TSMP access_token 歷史紀錄
CREATE TABLE IF NOT EXISTS TSMP_TOKEN_HISTORY (
	SEQ_NO 			BIGINT NOT NULL,
	USER_NID 		VARCHAR(255) NULL,
	USER_NAME 		VARCHAR(50) NULL,
	CLIENT_ID 		VARCHAR(40) NOT NULL,
	TOKEN_JTI 		VARCHAR(100) NOT NULL,
	SCOPE 			VARCHAR(255) NOT NULL,
	EXPIRED_AT		DATETIME NOT NULL,
	CREATE_AT 		DATETIME NOT NULL,
	STIME 			DATETIME NULL,
	REVOKED_AT 		DATETIME NULL,
	REVOKED_STATUS 	CHAR(2) NULL,
	RETOKEN_JTI 	VARCHAR(100) NOT NULL,
	REEXPIRED_AT 	DATETIME NOT NULL,
	PRIMARY KEY (SEQ_NO)
);

-- TSMP API 基本資料
CREATE TABLE IF NOT EXISTS tsmp_api (
	api_key			VARCHAR(255) NOT NULL,	-- API代碼
	module_name		VARCHAR(150) NOT NULL,	-- ex: "default"
	api_name		VARCHAR(255),	-- API名稱
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
	api_cache_flag  CHAR(1) NOT NULL DEFAULT '1', -- CACHE的方式 1:無(預設), 2:自適應, 3:固定
	mock_status_code CHAR(3) Null, -- Mock 狀態
	mock_headers VARCHAR(2000) Null, -- Mock Header
	mock_body NVARCHAR(2000) Null, -- Mock Body
	success BIGINT NOT NULL DEFAULT 0,
	fail BIGINT NOT NULL DEFAULT 0,
	total BIGINT NOT NULL DEFAULT 0,
	elapse BIGINT NOT NULL DEFAULT 0,
	fixed_cache_time INT NOT NULL DEFAULT 0,
	PRIMARY KEY (api_key, module_name)
);

CREATE TABLE IF NOT EXISTS seq_store (
	sequence_name	VARCHAR(255) NOT NULL,
	next_val		BIGINT,
	PRIMARY KEY (sequence_name)
);

-- TSMP後台使用者				
CREATE TABLE IF NOT EXISTS tsmp_user (
	user_id			VARCHAR(10) NOT NULL,			-- 使用者工號: 系統中不可重複的TSMP使用者識別碼。(系統預設自重產生，由1,000,000,000開始的數字往上加)
	user_name		VARCHAR(30) NOT NULL,			-- 使用者名稱: 以USER_NAME登入TSMP系，員工名稱不可重複
	user_status		CHAR(1) NOT NULL DEFAULT '1',	-- 使用者狀態: 0: Deleted, 1: 正常 (預設), 2: 停權, 3: 鎖定
	user_alias		VARCHAR(30) NULL,				-- 使用者代號
	user_email		VARCHAR(100) NOT NULL,			-- Email
	logon_date		DATETIME NULL,					-- 登入時間: Last Login time
	logoff_date		DATETIME NULL,					-- 登出時間: Last Logout time
	update_user		VARCHAR(255) NULL,				-- 異動人員: Last Editor
	update_time		DATETIME NULL,					-- 異動日期: Last Editor time
	create_user		VARCHAR(255) NOT NULL,			-- 建立人員: manager建立人員為他自已
	create_time		DATETIME NOT NULL,				-- 建立日期
	pwd_fail_times	INT NOT NULL DEFAULT 0,			-- 密碼錯誤次數: ex. 錯誤第六次就鎖定帳號
	org_id			VARCHAR(255) NULL,				-- 組織單位ID: 不可重複組織單位識別碼(Intitial: 000001)
	PRIMARY KEY (user_id),
	UNIQUE (user_name)
);

-- TSMP管理者角色
CREATE TABLE IF NOT EXISTS tsmp_role (
	role_id		VARCHAR(10) NOT NULL,	-- 角色代碼,系統中不可重複的TSMP角色代碼
	role_name	VARCHAR(30) NOT NULL,	-- 角色名稱,EX:"ADMIN"
	role_alias	VARCHAR(30) NULL,	-- 角色代號,不限中英文,供頁面顯示用,不可重複
	create_user	VARCHAR(255) NOT NULL,	-- 建立人員
	create_time	DATETIME NOT NULL,	-- 建立日期
	PRIMARY KEY (role_id)
);

-- TSMP用戶端基本資料
CREATE TABLE IF NOT EXISTS tsmp_client (
	client_id		VARCHAR(40) NOT NULL,
	client_name		VARCHAR(30) NOT NULL,
	client_alias	VARCHAR(60),
	client_status	CHAR(1) NOT NULL DEFAULT '1',
	tps				INT NOT NULL DEFAULT 10,
	emails			VARCHAR(500) NULL,
	client_sd		DATETIME NULL,
	client_ed		DATETIME NULL,
	svc_st			VARCHAR(4) NULL,
	svc_et			VARCHAR(4) NULL,
	api_quota		INT NULL,
	api_used		INT NULL,
	c_priority		INT NOT NULL DEFAULT 5,
	create_time		DATETIME NOT NULL,
	update_time		DATETIME NULL,
	owner			VARCHAR(100) NOT NULL,
	remark			VARCHAR(300) NULL,
	create_user		VARCHAR(255) NOT NULL,
	update_user		VARCHAR(255) NULL,
	security_level_id VARCHAR(10) NULL,
	signup_num      VARCHAR(100) NULL,
	pwd_fail_times  INT NOT NULL DEFAULT 0,
	fail_treshhold  INT NOT NULL DEFAULT 3,
	ACCESS_TOKEN_QUOTA		INT NULL,
	REFRESH_TOKEN_QUOTA		INT NULL,
	CLIENT_SECRET 	VARCHAR(128),
	PRIMARY KEY (client_id)
);

CREATE TABLE IF NOT EXISTS tsmp_func (
	func_code		NVARCHAR(10) NOT NULL,
	func_name		NVARCHAR(50) NOT NULL,
	func_name_en	NVARCHAR(50) NULL,
	func_desc		NVARCHAR(300) NULL,
	locale			NVARCHAR(10) NOT NULL,
	update_user		VARCHAR(255) NULL,
	update_time		DATETIME NOT NULL,
	func_url		NVARCHAR(300) NULL,
	PRIMARY KEY (func_code, locale)
);

CREATE TABLE IF NOT EXISTS tsmp_role_func (
	role_id		VARCHAR(10) NOT NULL,
	func_code	VARCHAR(10) NOT NULL,
	PRIMARY KEY (role_id, func_code)
);

-- TSMP 參數表
CREATE TABLE IF NOT EXISTS tsmp_setting (
   id				nvarchar(255) NOT NULL,
   value			nvarchar(max) DEFAULT (NULL),	-- 參數內容
   memo				varchar(512) DEFAULT (NULL),
   PRIMARY KEY (id)
);

-- API回覆代碼表
CREATE TABLE IF NOT EXISTS tsmp_rtn_code (
	tsmp_rtn_code		varchar(20)	NOT NULL,	-- 回覆代碼	
	locale 				VARCHAR(10) NOT NULL,	-- 語言地區	ex. "zh-TW"
	tsmp_rtn_msg 		varchar(300) NOT NULL, 	-- 顯示的回覆訊息	
	tsmp_rtn_desc 		varchar(300), 			-- 說明 (internal use only)
	PRIMARY KEY (tsmp_rtn_code, locale)
);

-- 各類類型清單列表	
CREATE TABLE IF NOT EXISTS tsmp_dp_items (
	item_id				BIGINT NOT NULL,	-- ID(流水號)(做為前端Bcrypt 尾碼Index使用)
	item_no				VARCHAR(20) NOT NULL,	-- 分頁編號(做為群組的代碼, 同一群組中需要程式保證 NAME值為相同)
	item_name			VARCHAR(100) NOT NULL,	-- 分類名稱(做為群組的代碼, 同一群組中需要程式保證 NAME值為相同)
	subitem_no			VARCHAR(20) NOT NULL,	-- 子分類編號
	subitem_name		VARCHAR(100) NOT NULL,	-- 子分類名稱	
	sort_by				INT NOT NULL DEFAULT 0,
	is_default			VARCHAR(1),	-- 是否為選單中的default select(V：select，null：deselect)
	param1				VARCHAR(255),	-- 參數1
	param2				VARCHAR(255),	-- 參數2
	param3				VARCHAR(255),	-- 參數3 	
	param4				VARCHAR(255),	-- 參數4
	param5				VARCHAR(255),	-- 參數5
	locale              VARCHAR(10) NOT NULL DEFAULT 'zh-TW',    -- 語言地區
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_date_time	DATETIME,	-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),	-- 更新人員
	version				INT DEFAULT 1,	-- 版號: C/U時, 增量+1
	keyword_search		NVARCHAR(200),	-- LikeSearch使用: 分類名稱 | 子分類名稱
	PRIMARY KEY (item_no, subitem_no, locale)
);

-- 上傳檔案: 記錄DP所有種類上傳檔案 path等, 但不含檔案本身的byte, 所有不同分類的檔案以 [檔案分類代碼] 區分, 允許一個REF_ID有多個檔案
CREATE TABLE IF NOT EXISTS tsmp_dp_file (
	file_id				BIGINT NOT NULL,	-- ID(流水號)
	file_name			NVARCHAR(100) NOT NULL,	-- 檔案名稱
	file_path			NVARCHAR(300) NOT NULL,	-- 檔案路徑: prefix記錄在application.properties設定檔，只記錄又再建立旳子資料夾
	ref_file_cate_code	NVARCHAR(50) NOT NULL,	-- 檔案分類代碼: 參考Java Enum: TsmpDpFileType 
	ref_id				BIGINT NOT NULL,	-- 參照ID: 例如：TSMP_DP_FAQ_QUESTION.QUESTION_ID
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_date_time	DATETIME,	-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),	-- 更新人員
	version				INT DEFAULT 1,	-- 版號: C/U時, 增量+1
	keyword_search		NVARCHAR(400),	-- LikeSearch使用: 檔案名稱|檔案路徑
	is_blob		VARCHAR(1) DEFAULT 'N',	-- 本筆資料是否使用BLOB: Y：使用，N/Null：不停用 （預設）
	is_tmpfile		VARCHAR(1) DEFAULT 'N',	-- 是否為暫存檔: Y：暫存檔，N/Null：正式服役檔案 （預設）
	blob_data		LONGBLOB,	-- 檔案本體
	PRIMARY KEY (file_id),
	UNIQUE (file_name,ref_file_cate_code,ref_id)
);

CREATE INDEX IF NOT EXISTS index_tsmp_dp_file_01 ON tsmp_dp_file (ref_file_cate_code, ref_id);

-- 預約工作單-排程模組功能
CREATE TABLE IF NOT EXISTS tsmp_dp_appt_job (
	appt_job_id			BIGINT NOT NULL,	-- ID(流水號)
	ref_item_no			VARCHAR(50) NOT NULL,	-- 分類名稱(說明工作項目)
	ref_subitem_no		VARCHAR(100),	-- 子分類編號(說明工作項目)
	status				VARCHAR(1) DEFAULT 'W' NOT NULL,	-- 狀態(W：等待，R：執行中，E：失敗，D：完成；工作被Queue取出執行時才會Update狀態)
	in_params			VARCHAR(4000),	-- input參數
	exec_result			VARCHAR(4000),	-- 執行結果值
	exec_owner			VARCHAR(20) DEFAULT 'SYS',	-- 工作執行者(如果是自動執行=SYS, 如果是人員指定執行 = ClientId)
	stack_trace			VARCHAR(4000),	-- Exception Message(超過長度就截掉)
	job_step			VARCHAR(50),	-- 工作進度(長時間工作可於此記錄執行狀態)
	start_date_time		DATETIME NOT NULL,	-- 開始時間(產生Record時由程式決定)
	from_job_id			BIGINT,	-- 重複工作ID(重複執行的Job id)
	period_uid			VARCHAR(36) NOT NULL,	-- 週期工作ID
	period_items_id		BIGINT NOT NULL, 		-- 週期工作項目ID, 參考 TSMP_DP_APPT_RJOB_D.APPT_RJOB_D_ID
	period_nexttime		BIGINT,					-- 週期工作開始時間
	identif_data		VARCHAR(4000),	-- 識別資料(單純提供顯示的資料給 UI 展示, ex: 以 mail 為例: mailQty=12,userName=JUNIT_TEST,reqOrdermId=ON-20200505-0001,recipients=xxxx@gmail.com,yyy@gmail.com,...etc)
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_date_time	DATETIME,	-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),	-- 更新人員
	version				INT DEFAULT 1,	-- 版號: C/U時, 增量+1
	PRIMARY KEY (appt_job_id),
	UNIQUE (period_uid, period_items_id, period_nexttime)
);

-- 週期排程設定檔
CREATE TABLE IF NOT EXISTS tsmp_dp_appt_rjob (
	appt_rjob_id			VARCHAR(36) NOT NULL,				-- 週期工作 UID
	rjob_name				VARCHAR(60) NOT NULL,				-- 週期排程名稱(使用者自定義名稱, ex: "鬧鐘")
	cron_expression			VARCHAR(700) NOT NULL,				-- 排程表達式(Quartz Cron Expression)
	cron_json				VARCHAR(4000) NOT NULL,				-- 表達式設定表單(對應前端表單設定JSON)
	cron_desc				VARCHAR(300),						-- 表達式的語文描述(從表單設定轉換成非系統相關人員看得懂的頻率描述)
	next_date_time			BIGINT NOT NULL,					-- 下次執行時間(instant)
	last_date_time			BIGINT,								-- 上次執行時間(instant)
	eff_date_time			BIGINT,								-- 排程生效時間-Effective Date Time(instant), 不設定表示即時生效
	inv_date_time			BIGINT,								-- 排程失效時間-Invalidated Date Time(instant), 不設定表示永不失效
	remark					VARCHAR(300),						-- 週期排程備註
	status					VARCHAR(1) DEFAULT '1' NOT NULL,	-- 狀態：0=停用, 1=啟動, 2=暫停, 3=執行中
	create_date_time		DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user				VARCHAR(255) DEFAULT 'SYSTEM',		-- 建立人員
	update_date_time		DATETIME,							-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user				VARCHAR(255),						-- 更新人員
	version					INT DEFAULT 1,						-- 版號: C/U時, 增量+1
	keyword_search			VARCHAR(396),						-- LikeSearch使用: APPT_RJOB_ID | RJOB_NAME | REMARK
	PRIMARY KEY (appt_rjob_id)
);

-- 週期排程工作項目
CREATE TABLE IF NOT EXISTS tsmp_dp_appt_rjob_d (
	appt_rjob_d_id		BIGINT NOT NULL,		-- ID(流水號)
	appt_rjob_id		VARCHAR(36) NOT NULL,	-- 週期排程ID(TSMP_DP_APPT_RJOB.APPT_RJOB_ID)
	ref_item_no			VARCHAR(50) NOT NULL,	-- 分類名稱(說明工作項目)
	ref_subitem_no		VARCHAR(100),			-- 子分類編號(說明工作項目)
	in_params			VARCHAR(4000),			-- input參數
	identif_data		VARCHAR(4000),			-- 識別資料
	sort_by				INT NOT NULL DEFAULT 0,	-- 執行順序
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',		-- 建立人員
	update_date_time	DATETIME,							-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),						-- 更新人員
	version				INT DEFAULT 1,						-- 版號: C/U時, 增量+1
	keyword_search		VARCHAR(186),						-- LikeSearch使用: APPT_RJOB_ID | REF_ITEM_NO | REF_SUBITEM_NO
	PRIMARY KEY (appt_rjob_d_id)
);

-- TSMP Open API Key
CREATE TABLE IF NOT EXISTS tsmp_open_apikey (							
	open_apikey_id		BIGINT	NOT NULL,				-- ID (流水號)	
	client_id			VARCHAR(255) NOT NULL,			-- Client ID	
	open_apikey			VARCHAR(1024) NOT NULL,			-- Open API Key	利用UUID生成128bit經由SHA1後取得Hash Value(160bit), 轉Base64存入, 代表身分識別
	secret_key			VARCHAR(1024) NOT NULL,			-- Secret KEY	"利用UUID生成128bit經由SHA256後取得Hash Value(256bit), 轉Base64存入, 為內容加解密使用"
	open_apikey_alias	VARCHAR(255) NOT NULL,			-- Open  API Key別名	
	times_quota			INT	NOT NULL DEFAULT -1,			-- 可使用次數	"1.若使用次數上限為10時,當已使用1次,則此欄位為9;當已使用2次,則此欄位為8;依此類推2. 若值為 -1, 則 Open API Key 無使用次數限制"
	times_threshold		INT	NOT NULL DEFAULT -1,			-- 使用次數上限	"1.效期內的使用次數上限2. 若值為-1, 則 Open API Key 無使用上限"
	expired_at			BIGINT NOT NULL,				-- Open  API Key 效期	
	revoked_at			BIGINT,							-- Open  API Key 撤銷時間	
	open_apikey_status	VARCHAR(1) NOT NULL DEFAULT 1,	-- Open  API Key 狀態	1：啟用，0：停用 （預設啟用）
	rollover_flag		VARCHAR(1) NOT NULL DEFAULT 'N',-- 是否已展期	Y：已展期，N：未展期
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_date_time	DATETIME,						-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),					-- 更新人員
	version				INT DEFAULT 1,					-- 版號: C/U時, 增量+1
	PRIMARY KEY (open_apikey_id),
	UNIQUE (open_apikey)
);

-- TSMP Open API Key mapping API
CREATE TABLE IF NOT EXISTS tsmp_open_apikey_map (	
	open_apikey_map_id	BIGINT NOT NULL,				--	ID (流水號)	系統自動產生的流水編號，用以作為此筆資料的唯一識別值
	ref_open_apikey_id	BIGINT NOT NULL,				--	Open API Key ID	from TSMP_OPEN_APIKEY
	ref_api_uid			VARCHAR(36) NOT NULL,			--	API UUID	from TSMP_API.API_UID
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	update_date_time	DATETIME,						-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),					-- 更新人員
	version				INT DEFAULT 1,					-- 版號: C/U時, 增量+1
	PRIMARY KEY (open_apikey_map_id)
);

-- 20220214, v3.10.3, 安全稽核日誌主表, Min
-- 20220328, v3.10.3, 重建安全稽核日誌主表, 因要增加第2個PK值, Mini
CREATE TABLE IF NOT EXISTS DGR_AUDIT_LOGM (
    AUDIT_LONG_ID           BIGINT NOT NULL, 			-- ID (流水號)
    AUDIT_EXT_ID            BIGINT NOT NULL DEFAULT 0, 	-- ID2 (流水號2)
    TXN_UID					VARCHAR(50) NOT NULL,		-- UUID-b4bit
    USER_NAME               VARCHAR(50) NOT NULL,		-- 使用者名稱
    CLIENT_ID               VARCHAR(50) NOT NULL,		-- 用戶端 ID 
	API_URL			        VARCHAR(500) NOT NULL,		-- API URL
	ORIG_API_URL			VARCHAR(500),    			-- 客製包API URL
	EVENT_NO			    VARCHAR(50) NOT NULL,    	-- API 事件編號
	USER_IP      		    VARCHAR(200),    			-- 使用者 IP
	USER_HOSTNAME			VARCHAR(200),    			-- 使用者主機名稱
	USER_ROLE   			VARCHAR(4000),    			-- 使用者角色
	PARAM1			  		VARCHAR(4000),    			-- 自訂欄位1
	PARAM2			   		VARCHAR(4000),    			-- 自訂欄位2
	PARAM3			   		VARCHAR(4000),    			-- 自訂欄位3	
	PARAM4			   		VARCHAR(4000),    			-- 自訂欄位4
	PARAM5			   		VARCHAR(4000),    			-- 自訂欄位5
	STACK_TRACE				VARCHAR(4000),    			-- 堆疊追蹤		
    CREATE_DATE_TIME    	DATETIME DEFAULT CURRENT_TIMESTAMP,
    CREATE_USER         	VARCHAR(255) DEFAULT 'SYSTEM',
    UPDATE_DATE_TIME    	DATETIME,
    UPDATE_USER         	VARCHAR(255),
    VERSION             	INT DEFAULT 1,	
    CONSTRAINT PK_dgr_audit_logm_1 PRIMARY KEY (AUDIT_LONG_ID, AUDIT_EXT_ID),
    CONSTRAINT UK_dgr_audit_logm_1 UNIQUE (TXN_UID)
);

-- 20220216, v3.10.3, 安全稽核日誌明細表, Min
CREATE TABLE IF NOT EXISTS dgr_audit_logd (
    audit_long_id		BIGINT NOT NULL, -- ID (流水號)
    txn_uid				VARCHAR(50) NOT NULL,    -- UUID-b4bit
    entity_name			VARCHAR(50) NOT NULL,    -- Entity 的名稱
    cud					VARCHAR(50) NOT NULL,   -- 對資料的操作, C/U/D 
	old_row				LONGBLOB ,    -- 舊資料
	new_row				LONGBLOB ,    -- 新資料
	param1				VARCHAR(4000),    -- 自訂欄位1
	param2				VARCHAR(4000),    -- 自訂欄位2
	param3				VARCHAR(4000),    --	自訂欄位3	
	param4				VARCHAR(4000),    -- 自訂欄位4
	param5				VARCHAR(4000),    -- 自訂欄位5
	stack_trace			VARCHAR(4000),    --		
    create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_user			VARCHAR(255) DEFAULT 'SYSTEM',
    update_date_time	DATETIME,
    update_user			VARCHAR(255),
    version				INT DEFAULT 1,	
    PRIMARY KEY (audit_long_id)
);

-- users
CREATE TABLE IF NOT EXISTS users (
	username NVARCHAR(50)  NOT NULL,
    password NVARCHAR(100) NOT NULL,
    enabled SMALLINT NOT NULL,
    PRIMARY KEY (username)
);

-- TSMP管理者權限表(Spring Security)
-- 註: (此Table即為管理者與角色關係USER_ROLE。TSMP不再另行定義schema。)
CREATE TABLE IF NOT EXISTS authorities (
	username	NVARCHAR(400) NOT NULL,	-- 使用者名稱
	authority	NVARCHAR(50) NOT NULL,	-- 權限(角色代碼 role_id)
	UNIQUE (username, authority)
);

-- 角色與txID對應檔
CREATE TABLE IF NOT EXISTS tsmp_role_txid_map (
	role_txid_map_id	BIGINT NOT NULL,		-- ID(流水號)
	role_id				VARCHAR(10) NOT NULL,	-- 角色代碼, TSMP_ROLE.ROLE_ID
	txid				VARCHAR(10) NOT NULL,	-- 交易代碼, EX: "AA0001", "DPB0001"
	list_type			VARCHAR(1) NOT NULL,	-- 名單類型, "W": 白名單; "B": 黑名單
	create_date_time	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	create_user			VARCHAR(255) DEFAULT 'SYSTEM',		-- 建立人員
	update_date_time	DATETIME,							-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	update_user			VARCHAR(255),						-- 更新人員
	version				INT DEFAULT 1,						-- 版號: C/U時, 增量+1
	keyword_search		VARCHAR(20),						-- LikeSearch使用: ROLE_ID | TXID
	PRIMARY KEY (role_txid_map_id),
	UNIQUE (role_id, txid)
);

-- TSMP 組織單位表
CREATE TABLE IF NOT EXISTS tsmp_organization (
	org_id			VARCHAR(255) NOT NULL,	-- 組織單位ID: 不可重複組織單位識別碼
	org_name		VARCHAR(30) NOT NULL,	-- 組織單位名稱: 不可重複組織單位名稱
	parent_id		VARCHAR(10) NULL,	-- 父組織單位ID
	org_path		VARCHAR(255) NOT NULL,	-- 組織階層路徑
	org_code		VARCHAR(100) NULL,	-- 組織代碼-1. 政府機關代碼填寫欄,2. 格式:機關代碼＋單位代碼
	create_user		VARCHAR(255) NOT NULL,	-- 建立者
	create_time		TIMESTAMP NOT NULL,	-- 建立時間
	update_user		VARCHAR(255) NULL,	-- 更新者
	update_time		TIMESTAMP NULL,	-- 更新時間
	contact_name	NVARCHAR(50) NULL,	-- 聯絡人: 所屬管理機關聯絡人
	contact_tel		NVARCHAR(50) NULL,	-- 聯絡人電話: 所屬管理機關聯絡人電話
	contact_mail	NVARCHAR(100) NULL,	-- 聯絡人EMAIL: 所屬管理機關聯絡人電子郵件
	PRIMARY KEY (org_id),
	CONSTRAINT UK_organization_1 UNIQUE (org_name)
);

--TSMP_ROLE_ROLE_MAPPING
CREATE TABLE IF NOT EXISTS TSMP_ROLE_ROLE_MAPPING(
ROLE_NAME          VARCHAR(50),
ROLE_NAME_MAPPING  VARCHAR(50),
ROLE_ROLE_ID BIGINT  NOT NULL,
PRIMARY KEY (ROLE_ROLE_ID)
);

-- OAuth2 標準表格
CREATE TABLE IF NOT EXISTS oauth_client_details (
	client_id               VARCHAR(256) PRIMARY KEY,
	resource_ids            VARCHAR(256),
	client_secret           VARCHAR(256),
	scope                   VARCHAR(2048),
	authorized_grant_types  VARCHAR(256),
	web_server_redirect_uri VARCHAR(256),
	authorities             VARCHAR(256),
	access_token_validity   INT,
	refresh_token_validity  INT,
	additional_information  VARCHAR(4096),
	autoapprove             VARCHAR(256)
);

-- TSMP外部API註冊資料
CREATE TABLE IF NOT EXISTS tsmp_api_reg (
	api_key				VARCHAR(255) NOT NULL,	-- API KEY
	module_name			VARCHAR(50) NOT NULL,	-- Module Name
	src_url				VARCHAR(255) NOT NULL,	-- 來源URL
	url_rid				CHAR(1) NOT NULL,	-- URL有ResourceID ("0": 沒有(default); "1":有 )
	reg_status			CHAR(1) NOT NULL,	-- 註冊狀態 ("0": 暫存; "1": 確認 (for C:先暫存，回呼再確認; for R: 儲存時直接確認))
	api_uuid			VARCHAR(64) NULL,	-- API UUID	Only for Composed API
	reghost_id			VARCHAR(10) NULL,	-- 註冊主機代碼
	path_of_json		VARCHAR(255) NOT NULL,	-- 
	method_of_json		VARCHAR(200) NOT NULL,	-- 
	params_of_json		VARCHAR(255) NULL,	-- 
	headers_of_json		VARCHAR(255) NULL,	-- 
	consumes_of_json	VARCHAR(100) NULL,	-- 
	produces_of_json	VARCHAR(255) NULL, 	-- 
	create_time			DATETIME NOT NULL,	-- 
	create_user			VARCHAR(255) NOT NULL,	-- 
	update_time			DATETIME NULL,	-- 
	update_user			VARCHAR(255) NULL,	-- 	
	no_oauth			CHAR(1),
	fun_flag			INT,				--tsmpg 會檢查此欄位，決定是否允許轉導，如果是 0 則不轉導，1反之
	PRIMARY KEY (api_key, module_name)		--1:tokenpayload:會將token的payload往後帶
);

-- 授權碼記錄檔 (紀錄已核發的授權碼(Authorization Code)及效期...等資訊)
CREATE TABLE IF NOT EXISTS tsmp_auth_code (
	AUTH_CODE_ID		BIGINT NOT NULL,	-- ID(流水號)
	AUTH_CODE			VARCHAR(1000) NOT NULL,	-- 授權碼, 使用 digiRunner 核發 Token 使用的非對稱式加密金鑰，將 AUTH_TYPE, EXPIRE_DATE_TIME, CREATE_USER...等資訊加密而產生
	EXPIRE_DATE_TIME	BIGINT NOT NULL,	-- 有效期限, 超過此期限即不可使用此授權碼
	STATUS				VARCHAR(1) NOT NULL DEFAULT '0',	-- 狀態, 0：可用；1：已使用；2：失效
	AUTH_TYPE			VARCHAR(20),	-- 授權類型, 發出授權碼的用途，ex: "Composer"、"Net"...。
	CLIENT_NAME			VARCHAR(150),	-- 用戶端代號, 使用此授權碼的Client。TSMP_CLIENT.client_name
	CREATE_DATE_TIME	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	CREATE_USER			VARCHAR(255) DEFAULT 'SYSTEM',		-- 建立人員
	UPDATE_DATE_TIME	DATETIME,							-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	UPDATE_USER			VARCHAR(255),						-- 更新人員
	VERSION				INT DEFAULT 1,						-- 版號: C/U時, 增量+1
	PRIMARY KEY (AUTH_CODE_ID),
	UNIQUE (AUTH_CODE)
);

-- 系統預設的使用者、角色及組織
INSERT INTO tsmp_user (user_id, user_name, user_alias, user_status, user_email, create_user, create_time, pwd_fail_times, org_id) VALUES ('1000000000', 'manager', 'Manager', '1', 'manager@thinkpower.com.tw', 'manager', now(), 0, '100000');
-- password=Bcrypt(Base64Encode(manager123))
INSERT INTO users (username, password, enabled) VALUES ('manager', '$2y$12$C8B6D4SzvM7qL1NUo4Uwa.daXzIRlYFkeZ62fhnVeK4smJL6ZqFra', '1');
INSERT INTO tsmp_organization (org_id, org_name, parent_id, org_path, create_time, create_user) VALUES ('100000', 'TSMPDefaultRoot', '', '100000', current_timestamp, 'manager');
INSERT INTO tsmp_role (role_id, role_name, role_alias, create_user, create_time) VALUES ('1000', 'ADMIN', 'Administrator', 'manager', current_timestamp);
INSERT INTO tsmp_role_role_mapping (role_role_id, role_name, role_name_mapping) VALUES (1, 'ADMIN', 'ADMIN');
INSERT INTO authorities (username, authority) VALUES ('manager', '1000');

INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (30, 'ENABLE_FLAG', '停用/啟用', '1', '啟用', 100, NULL, '1', 'Y', NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (30, 'ENABLE_FLAG', 'Disable/enable', '1', 'Active', 100, 'en-US', NULL, '1', 'Y', NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (31, 'ENABLE_FLAG', '停用/啟用', '0', '停用', 101, NULL, '2', 'N', NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (31, 'ENABLE_FLAG', 'Disable/enable', '0', 'Dormant', 101, 'en-US', NULL, '2', 'N', NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (32, 'ENABLE_FLAG', '停用/啟用', '-1', '全部', 102, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (32, 'ENABLE_FLAG', 'Disable/enable', '-1', 'All', 102, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (75, 'ENABLE_FLAG', '停用/啟用', '2', '鎖定', 103, NULL, '3', NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (75, 'ENABLE_FLAG', 'Disable/enable', '2', 'Locked', 103, 'en-US', NULL, '3', NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (81, 'RJOB_STATUS', 'Cycle schedule status', '0', 'invalid', 200, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (81, 'RJOB_STATUS', '週期排程狀態', '0', '作廢', 200, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (82, 'RJOB_STATUS', 'Cycle schedule status', '1', 'start up', 201, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (82, 'RJOB_STATUS', '週期排程狀態', '1', '啟動', 201, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (83, 'RJOB_STATUS', 'Cycle schedule status', '2', 'pause', 202, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (83, 'RJOB_STATUS', '週期排程狀態', '2', '暫停', 202, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (84, 'RJOB_STATUS', 'Cycle schedule status', '3', 'Executing', 203, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5, LOCALE) VALUES (84, 'RJOB_STATUS', '週期排程狀態', '3', '執行中', 203, NULL, NULL, NULL, NULL, NULL, NULL, 'zh-TW');
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (243, 'API_CACHE_FLAG', 'Api Cache 方式', '1', '無', 1810, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (243, 'API_CACHE_FLAG', 'Api Cache type', '1', 'None', 1810, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (244, 'API_CACHE_FLAG', 'Api Cache 方式', '2', '自適應', 1811, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (244, 'API_CACHE_FLAG', 'Api Cache type', '2', 'Auto', 1811, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (245, 'API_CACHE_FLAG', 'Api Cache 方式', '3', '固定', 1812, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (245, 'API_CACHE_FLAG', 'Api Cache type', '3', 'Fixed', 1812, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (246, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'addClient', '新增用戶', 1493, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (246, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'addClient', 'Add Client', 1493, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (247, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'deleteClient', '刪除用戶', 1494, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (247, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'deleteClient', 'Delete Client', 1494, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (248, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'updateClient', '更新用戶', 1495, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (248, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'updateClient', 'Update Client', 1495, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (249, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'addGroup', '新增群組', 1496, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (249, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'addGroup', 'Add Group', 1496, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (250, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'deleteGroup', '刪除群組', 1497, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (250, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'deleteGroup', 'Delete Group', 1497, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (251, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'updateGroup', '更新群組', 1498, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (251, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'updateGroup', 'Update Group', 1498, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (252, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'addRegisterApi', '新增註冊API', 1499, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (252, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'addRegisterApi', 'Add Register API', 1499, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (253, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'deleteRegisterApi', '刪除註冊API', 1500, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (253, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'deleteRegisterApi', 'Delete Register API', 1500, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (254, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'updateRegisterApi', '更新註冊API', 1501, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (254, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'updateRegisterApi', 'Update Register API', 1501, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (255, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'addComposerApi', '新增組合API', 1502, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (255, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'addComposerApi', 'Add Composer API', 1502, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (256, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'deleteComposerApi', '刪除組合API', 1503, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (256, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'deleteComposerApi', 'Delete Composer API', 1503, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (257, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'updateComposerApi', '更新組合API', 1504, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (257, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'updateComposerApi', 'Update Composer API', 1504, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (258, 'REPORT_TIME_TYPE', '報表時間類型', 'MINUTE', '10分', 1213, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (258, 'REPORT_TIME_TYPE', 'Report time type', 'MINUTE', 'ten minute', 1213, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);

INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1100', 'en-US', 'Success', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1100', 'zh-TW', '成功', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1191', 'zh-TW', '資料已被異動', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1191', 'en-US', 'Information has been changed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1199', 'en-US', 'system error', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1199', 'zh-TW', '系統錯誤', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1219', 'en-US', 'Permission denied', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1219', 'zh-TW', '沒有權限', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1202', 'en-US', 'No type list', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1202', 'zh-TW', '查無類型清單', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1227', 'zh-TW', '生效日期不可小於今天', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1227', 'en-US', 'Dates in the past is not allowed on Effective date', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1230', 'zh-TW', '角色不存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1230', 'en-US', 'Role not exist', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1231', 'zh-TW', '使用者不存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1231', 'en-US', 'User not exist', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1251', 'zh-TW', '當前排程狀態不允許異動', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1251', 'en-US', 'Inconsistent operation with current scheduler status', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1261', 'zh-TW', '狀態:必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1261', 'en-US', 'Status: required parameter', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1264', 'zh-TW', '登入角色不存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1264', 'en-US', 'Login role does not exist', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1284', 'en-US', 'Duplicated value: [{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1284', 'zh-TW', '[{{0}}] 不得重複', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1285', 'en-US', 'Return code parameter does not meet the definition of multi-language', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1285', 'zh-TW', 'Return code 參數不符合多國語系定義', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1286', 'zh-TW', '更新失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1286', 'en-US', 'Update failed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1287', 'zh-TW', '刪除失敗', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1287', 'en-US', 'Delete failed', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1289', 'en-US', 'No Rtn Code  [{{1}}] messages from Locale [{{0}}] ', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1289', 'zh-TW', '查無 Locale [{{0}}] 的 Rtn Code [{{1}}] 訊息', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1290', 'zh-TW', '參數錯誤', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1290', 'en-US', 'Parameter error', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1292', 'en-US', 'Work Queue is full, please execute later', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1292', 'zh-TW', '工作佇例已滿, 請稍後再執行', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1293', 'en-US', 'Database error', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1293', 'zh-TW', '資料庫錯誤', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1295', 'zh-TW', '日期格式不正確', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1295', 'en-US', 'Date format is incorrect', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1296', 'zh-TW', '缺少必填參數', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1296', 'en-US', 'Missing required parameter', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1297', 'en-US', 'Execution error', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1297', 'zh-TW', '執行錯誤', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1298', 'zh-TW', '查無資料', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1298', 'en-US', 'No information found', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1357', 'zh-TW', '您的角色並未授權使用 API txID [{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1357', 'en-US', 'The roles assigned to you are not authorized to call API txID [{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1410', 'zh-TW', '群組[{{0}}]不存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1410', 'en-US', 'Group [{{0}}] does not exist', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1433', 'zh-TW', '非對稱式加密失敗：[{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1433', 'en-US', 'Asymmetric encryption error: [{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1434', 'zh-TW', '非對稱式解密失敗：[{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1434', 'en-US', 'Asymmetric decryption error: [{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1474', 'zh-TW', '設定檔缺少參數 [{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1474', 'en-US', 'The profile is missing parameters [{{0}}]', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1515', 'zh-TW', '單個用戶不能存在於多個群組中', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1515', 'en-US', 'A single user cannot exist in multiple group', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1516', 'zh-TW', '使用者沒有任何群組', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1516', 'en-US', 'User already exists with No Group', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('9901', 'zh-TW', '系統錯誤', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('9901', 'en-US', 'System error', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('9906', 'zh-TW', 'Client 請求數量超過限制', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('9906', 'en-US', 'Client requests exceeds TPS limit', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('9912', 'zh-TW', 'API 已被停用', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('9912', 'en-US', 'API disable', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('9926', 'zh-TW', '不合法字元', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('9926', 'en-US', 'Invalid Character', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('9930', 'zh-TW', '有不合法的字串', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('9930', 'en-US', 'Invalid String', '');

-- 20220303, Audit Log 增加參數, Mini Lee
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('AUDIT_LOG_ENABLE', 'true', '"Audit Log" Recode Function enablement (true/false)');

-- 20220302, Audit Log 增加參數, Mini Lee
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (212, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'updateUserProfile', '更新使用者個人資料', 1488, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (212, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'updateUserProfile', 'Update User Profile', 1488, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (215, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'addTsmpSetting', '新增TSMP SETTING', 1489, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (215, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'addTsmpSetting', 'Add TSMP SETTING', 1489, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (216, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'deleteTsmpSetting', '刪除TSMP SETTING', 1490, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (216, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'deleteTsmpSetting', 'Delete TSMP SETTING', 1490, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (217, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'updateTsmpSetting', '更新TSMP SETTING', 1491, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (217, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'updateTsmpSetting', 'UpdateTSMP SETTING', 1491, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);

-- 20220310, Sso/UdpSso, 增加rtn code, Mini Lee
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1517', 'zh-TW', '不合法的SSO登入錯誤', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1517', 'en-US', 'Invalid SSO login error', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1518', 'zh-TW', '此使用者不能以SSO方式登入', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('1518', 'en-US', 'This user cannot log in with SSO', '');

--20220324, Audit Log 增加 items 參數, Mini Lee
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (218, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'forceLogout', '強制登出', 1492, 'zh-TW', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO TSMP_DP_ITEMS (ITEM_ID, ITEM_NO, ITEM_NAME, SUBITEM_NO, SUBITEM_NAME, SORT_BY, LOCALE, IS_DEFAULT, PARAM1, PARAM2, PARAM3, PARAM4, PARAM5) VALUES (218, 'AUDIT_LOG_EVENT', '安全稽核日誌事件', 'forceLogout', 'Force Logout', 1492, 'en-US', NULL, NULL, NULL, NULL, NULL, NULL);

-- 2022/04/13 DGRKEEPER設定
INSERT INTO TSMP_SETTING (ID,VALUE,MEMO) VALUES ('DGRKEEPER_IP','127.0.0.1','DGRKEEPER Server Host IP');
INSERT INTO TSMP_SETTING (ID,VALUE,MEMO) VALUES ('DGRKEEPER_PORT','8085','DGRKEEPER Server Host PORT');

-- 2022/04/13 Online Console Switch Setting
INSERT INTO TSMP_SETTING (ID,VALUE,MEMO) VALUES ('TSMP_ONLINE_CONSOLE','true','Online Console enablement - true/false');

-- 2022/04/19 Logger Level設定
-- INSERT INTO TSMP_SETTING (ID,VALUE,MEMO) VALUES ('LOGGER_LEVEL','INFO','Logger log output level setting');

-- 20220606, 授權範圍紀錄, Mini
CREATE TABLE IF NOT EXISTS DGR_OAUTH_APPROVALS (
	OAUTH_APPROVALS_ID 	BIGINT NOT NULL, 	-- ID (流水號)
	USER_NAME 			VARCHAR(256),		-- 使用者名稱
	CLIENT_ID 			VARCHAR(256),		-- 用戶端ID
	SCOPE 				VARCHAR(256),		-- 授權項目
	STATUS 				VARCHAR(10),		-- 是否授權
	EXPIRES_AT 			DATETIME,			-- 過期時間
	LAST_MODIFIED_AT 	DATETIME,			-- 最後更新時間	
	CREATE_DATE_TIME    DATETIME DEFAULT CURRENT_TIMESTAMP,
    CREATE_USER         VARCHAR(255) DEFAULT 'SYSTEM',
    UPDATE_DATE_TIME    DATETIME,
    UPDATE_USER         VARCHAR(255),
    VERSION             INT DEFAULT 1,	
    CONSTRAINT PK_dgr_oauth_approvals PRIMARY KEY (OAUTH_APPROVALS_ID)
);

-- 20220606, ES的設定, tom chu
INSERT INTO TSMP_SETTING (ID,VALUE,MEMO) VALUES ('ES_URL','https://10.20.30.88:19200/','The URL of ES, there must be a / line at the end, multiple groups are separated by commas (,), EX: https://10.20.30.88:19200/, https://10.20.30.88:29200/');
INSERT INTO TSMP_SETTING (ID,VALUE,MEMO) VALUES ('ES_ID_PWD','ENC(cGxlYXNlIHNldCB5b3VyIGVzIGlkIGFuZCBwYXNzd29yZCwgU2V0dGluZyBpcyBFU19JRF9QV0Q=)','ES''s ID:PWD is combined and encrypted with Base64; you can press the button"ENC" to encrypt with ENC. If you want to set for multiple URLs - Separated by commas before ENC encryption, EX:ENC(id1:pwd1,id2:pwd2)');
INSERT INTO TSMP_SETTING (ID,VALUE,MEMO) VALUES ('ES_TEST_TIMEOUT','3000','ES connection timeout setting for testing');
INSERT INTO TSMP_SETTING (ID,VALUE,MEMO) VALUES ('ES_MBODY_MASK_FLAG','false','Mask all mbody, true is masking, false is not masking');
INSERT INTO TSMP_SETTING (ID,VALUE,MEMO) VALUES ('ES_IGNORE_API','','Ignore the assigned API transation recording in the ES, API list can be list and separated by commas (,), and the value is moduleName/apiId');
INSERT INTO TSMP_SETTING (ID,VALUE,MEMO) VALUES ('ES_MBODY_MASK_API','','Do mbody mask for the API of tsmpc, API list can be separated by commas (,), and the value is moduleName/apiId');
INSERT INTO TSMP_SETTING (ID,VALUE,MEMO) VALUES ('ES_TOKEN_MASK_FLAG','true','Mask token, true is masking, false is not masking');
INSERT INTO TSMP_SETTING (ID,VALUE,MEMO) VALUES ('ES_MAX_SIZE_MBODY_MASK','0','Auto mbody mask when the length exceeding the mbody content value byte, the unit is byte, and the value below 10 (inclusive) will not be masked');
INSERT INTO TSMP_SETTING (ID,VALUE,MEMO) VALUES ('ES_DGRC_MBODY_MASK_URI','','Make an mbody mask on the URI of dgrc (value contains /dgrc), URL lists are separate by commas, Value format / dgrc/aa/bb/cc');
INSERT INTO TSMP_SETTING (ID,VALUE,MEMO) VALUES ('ES_DGRC_IGNORE_URI','','ES does not record URIs for dgrc(Value contains /dgrc), URL lists are separate by commas (,),Value is /dgrc/aa/bb/cc');
INSERT INTO TSMP_SETTING (ID,VALUE,MEMO) VALUES ('ES_LOG_DISABLE','true','Whether to prohibit the recording of ES LOG, true means yes, false means no');

-- 20220712, token的設定, Mini Lee
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('DGR_TOKEN_JWE_ENABLE', 'false', '"Token JWE encryption" enablement (true/false), default:false');

-- 20220718, TSMP access_token 歷史紀錄, 增加欄位, Mini Lee
ALTER TABLE TSMP_TOKEN_HISTORY ADD RFT_REVOKED_AT DATETIME;
ALTER TABLE TSMP_TOKEN_HISTORY ADD RFT_REVOKED_STATUS VARCHAR(10);

-- 20220801, token settings, Mini Lee
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('DGR_TOKEN_WHITELIST_ENABLE', 'false', '"Token whitelist" enablement (true/false), default:false');

-- 20220805, API message settings, Mini Lee
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('DGR_TW_FAPI_ENABLE', 'false', '"API information follow TW Open Banking Format" enablement (true/false), default:false');

-- 20220810, v4 paths compatibility settings, KevinC
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('DGR_PATHS_COMPATIBILITY', '2', 'URL Path compatibility, 0: tsmpc only, 1 : dgrc only, 2 : tsmpc dgrc Compatible, default:2');

-- 20220810,  filter rtnCode, Zoe Lee
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('0125', 'zh-TW', 'API不存在', '');
INSERT INTO TSMP_RTN_CODE (TSMP_RTN_CODE, LOCALE, TSMP_RTN_MSG, TSMP_RTN_DESC) VALUES ('0125', 'en-US', 'API not found', '');

-- 20220815, fixedCache time, Tom
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('FIXED_CACHE_TIME', '1', 'Static cache time, set in minutes');

-- 20220829, es monitor host settings, tom
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('ES_SYS_TYPE', 'DGR_LOCAL', 'Used to identify data purposes');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('ES_MONITOR_DISABLE', 'true', 'Whether to prohibit record ES monitoring, true means yes, false means no');

-- 20220901, TSMP用戶端基本資料表, 增加欄位, Mini Lee
ALTER TABLE tsmp_client ADD START_DATE BIGINT;
ALTER TABLE tsmp_client ADD END_DATE BIGINT;
ALTER TABLE tsmp_client ADD START_TIME_PER_DAY BIGINT;
ALTER TABLE tsmp_client ADD END_TIME_PER_DAY BIGINT;

-- 20220912, v4, TSMP用戶端基本資料表, 增加欄位, Mini Lee
ALTER TABLE tsmp_client ADD TIME_ZONE VARCHAR(200);

-- 20220920, 本機測試資料(ControllerMockTest),勿上到正式環境, Tom
INSERT INTO users (username, password, enabled) VALUES ('mockTestUser', '$2a$10$CTBieJSO.uai69kPHWpiHuPZ6NaaM7IMQ6qNtZjA7Z6DYvp8CCzZe', '1');
INSERT INTO authorities (username, authority) VALUES ('mockTestUser', '1000');
INSERT INTO tsmp_user (user_id, user_name, user_alias, user_status, user_email, create_user, create_time, pwd_fail_times, org_id) VALUES ('-1', 'mockTestUser', '單元測試員', '1', 'mockTestUser@tpisoftware.com', 'manager', now(), 0, '100000');

-- 20220922, 建立節點失聯紀錄, Tom
CREATE TABLE IF NOT EXISTS DGR_NODE_LOST_CONTACT (
	LOST_CONTACT_ID	  BIGINT NOT NULL, -- id
	NODE_NAME		  NVARCHAR(100) NOT NULL, -- 節點名稱
	IP	              NVARCHAR(100) NOT NULL, -- 位址
	PORT		      INT NOT NULL, -- 埠
	LOST_TIME		  NVARCHAR(100) NOT NULL, -- 失去聯繫時間 UTC
	CREATE_TIMESTAMP  BIGINT NOT NULL, -- 建立時間戳
	CREATE_DATE_TIME	DATETIME DEFAULT CURRENT_TIMESTAMP,	-- 建立日期: 資料初始建立的人、日期和時間
	CREATE_USER			VARCHAR(255) DEFAULT 'SYSTEM',	-- 建立人員
	UPDATE_DATE_TIME	DATETIME,	-- 更新日期: 表示最後Update的人、日期和時間. Null 表示是新建資料
	UPDATE_USER			VARCHAR(255),	-- 更新人員
	VERSION				INT DEFAULT 1,	-- 版號: C/U時, 增量+1
	CONSTRAINT PK_DGR_NODE_LOST_CONTACT PRIMARY KEY (lost_contact_id)
);

-- 20220923, v4, Composer的flow資料, JH_Min
CREATE TABLE DGR_COMPOSER_FLOW (
	FLOW_ID	  			BIGINT NOT NULL, 		-- id
	MODULE_NAME		  	NVARCHAR(150) NOT NULL,
	API_ID	            NVARCHAR(255) NOT NULL,
	FLOW_DATA			LONGBLOB ,
	CREATE_DATE_TIME    DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATE_DATE_TIME    DATETIME,
    VERSION             INT DEFAULT 1,
    CONSTRAINT u_DGR_COMPOSER_FLOW UNIQUE (MODULE_NAME, API_ID),
	CONSTRAINT PK_DGR_COMPOSER_FLOW PRIMARY KEY (FLOW_ID)
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

-- TSMP API 匯入資料
CREATE TABLE IF NOT EXISTS tsmp_api_imp (
	api_key				VARCHAR(255)	NOT NULL,
	module_name			VARCHAR(50)		NOT NULL,
	record_type			CHAR(1)			NOT NULL,	-- 紀錄種類, I: for Import; B: for Restore
	batch_no			INT				NOT NULL,	-- 上傳批號, 從1000開始編號
	filename			VARCHAR(100)	NOT NULL,	-- 上傳檔名
	api_name			VARCHAR(255),				-- API名稱
	api_desc			VARCHAR(300),				-- 說明
	api_owner			VARCHAR(100),				-- 擁有者
	url_rid				CHAR(1),					-- URL有ResourceID, "0": 沒有(default); "1":有
	api_src				CHAR(1),					-- API來源, M': Module(Default); 'R': Registerd; 'C': Composed; 'N': .Net;
	src_url				VARCHAR(255),				-- 來源URL
	api_uuid			VARCHAR(64),				-- API UUID, Only for Composed API
	path_of_json		VARCHAR(255)	NOT NULL,	-- Path
	method_of_json		VARCHAR(200)	NOT NULL,	-- Http Methods
	params_of_json		VARCHAR(255),				-- Http Parameters
	headers_of_json		VARCHAR(255),				-- Http Headers
	consumes_of_json	VARCHAR(100),				-- Http ContentTypes
	produces_of_json	VARCHAR(255),				-- Http Response
	flow				VARCHAR(MAX),				-- Composer Flow
	create_time			DATETIME		NOT NULL,	-- 建立時間
	create_user			VARCHAR(255),				-- 建立人員
	check_act			CHAR(1)			NOT NULL,	-- 檢查動作, C: Create; U: Update; N: Not Available
	result				CHAR(1)			NOT NULL,	-- 匯入結果, S':Successful; 'F':Failure; 'I': Init
	memo				VARCHAR(255),				-- 解析說明, 當check='N'時有此說明
	no_oauth			CHAR(1),					-- tsmpg允許轉導的設定, tsmpg 會檢查此欄位，決定是否允許轉導，如果是 0 則不轉導，1反之
	jwe_flag			VARCHAR(1),					-- JWE Flag (Request), 0：不使用，1：JWE，2：JWS，null:不使用
	jwe_flag_resp		VARCHAR(1),					-- JWE Flag (Response),	0：不使用，1：JWE，2：JWS，null:不使用
	fun_flag			INT,						-- 功能Flag, 1:tokenpayload:會將token的payload往後帶
	mock_status_code CHAR(3), -- Mock 狀態
	mock_headers VARCHAR(2000), -- Mock Header
	mock_body NVARCHAR(2000), -- Mock Body
	api_cache_flag  CHAR(1) NOT NULL DEFAULT '1', -- CACHE的方式 1:無(預設), 2:自適應, 3:固定
	fixed_cache_time INT NOT NULL DEFAULT 0,
	api_status		CHAR(1) NOT NULL,	-- API狀態: 1=Enabled, 2=Disabled (新增預設為Disabled)
	PRIMARY KEY (api_key, module_name, record_type, batch_no)
);

-- 20221007, v4, TSMP Token 歷史紀錄, 增加欄位, Mini Lee
ALTER TABLE tsmp_token_history ADD TOKEN_QUOTA INT;
ALTER TABLE tsmp_token_history ADD TOKEN_USED INT;
ALTER TABLE tsmp_token_history ADD RFT_QUOTA INT;
ALTER TABLE tsmp_token_history ADD RFT_USED INT;

-- 20221018, v4 Added DEFAULT_PAGE_SIZE, Kevin Cheng
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('DEFAULT_PAGE_SIZE', '20', 'Default Page Size');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('MAIL_BODY_API_FAIL_SERVICE_MAIL', 'service@thinkpower.com.tw', 'System email body : Service email account (service@thinkpower.com.tw)');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('MAIL_BODY_API_FAIL_SERVICE_TEL', '+886-2-8751-1610', 'System email body : Service number ( 02-12345678 )');
-- 20221018, v4 Added Setting Parameters, Kevin Cheng
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('ERRORLOG_KEYWORD', 'tpi.dgrv4,com.thinkpower', 'Print error messages contains specific texts, set up multiple texts - separates by commas (,)');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('FILE_TEMP_EXP_TIME', '3600000', 'Temporary file expiry time');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('AUTH_CODE_EXP_TIME', '600000', '"Auth code Expired Time" setting(ms)');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('QUERY_DURATION', '30', 'ES query date interval upper limit');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('SHUTDOWN_ENDPOINT_ALLOWED_IPS', '127.0.0.1,0:0:0:0:0:0:0:1', 'List of IP hosts allowed to access the shutdown endpoint, separated by commas (,), if the setting empty, no one allow to call');
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('MAIL_SEND_TIME', '600000', 'How long does it take to send the mail (ms) after writing the mail into the schedule');

--20221020, v4 sequence Kevin K
create sequence  SEQ_TSMP_USER_PK increment by 1 start with 2000000000;
create sequence  SEQ_TSMP_NODE_TASK_WORK_PK increment by 1 start with 2000000000;
create sequence  SEQ_TSMP_NODE_TASK_PK increment by 1 start with 2000000000;
create sequence  SEQ_TSMP_NODE_PK increment by 1 start with 2000000000;
create sequence  SEQ_TSMP_DC_PK increment by 1 start with 2000000000;
create sequence  SEQ_TSMP_CLIENT_HOST_PK increment by 1 start with 2000000000;
create sequence  SEQ_TSMP_API_MODULE_PK increment by 1 start with 2000000000;
create sequence  SEQ_TSMP_API_DETAIL_PK increment by 1 start with 2000000000;
create sequence  SEQ_TOKEN_USAGE_HISTORY_PK increment by 1 start with 2000000000;
create sequence  SEQ_TOKEN_HISTORY_HOUSING_PK increment by 1 start with 2000000000;
create sequence  SEQ_TSMP_GROUP_TIMES_LOG_PK increment by 1 start with 2000000000;
create sequence  SEQ_TSMP_TOKEN_HISTORY_PK increment by 1 start with 2000000000;
create sequence  SEQ_TSMP_VGROUP_PK increment by 1 start with 2000000000;
create sequence  SEQ_TSMP_EVENTS_PK increment by 1 start with 2000000000;
create sequence  SEQ_TSMP_ALERT_LOG_PK start with 1 increment by 2000000000;
create sequence  SEQ_TSMP_GROUP_PK increment by 1 start with 2000000000;
create sequence  SEQ_TSMP_ROLE_PK increment by 1 start with 2000000000;
create sequence  SEQ_TSMP_ORGANIZATION_PK increment by 1 start with 2000000000;
create sequence  SEQ_TSMP_ALERT_PK increment by 1 start with 2000000000;

--20221025 TSMP_ROLE.ROLE_ALIAS 更改為 NVARCHAR(255) Kevin_K
ALTER TABLE TSMP_ROLE ALTER COLUMN ROLE_ALIAS NVARCHAR(255);
 
-- 20221129, 將 CORS, Access-Control-Allow-Origin 的值加入 Setting, Kevin Cheng
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('DGR_CORS_VAL', '*', 'CORS default setting *, can change domain,Example:https://dgRv4.io/');

-- 20221201, v4, Gateway LB 加大欄位長度, Mini Lee
ALTER TABLE TSMP_API ALTER COLUMN SRC_URL NVARCHAR(2000);
ALTER TABLE TSMP_API_REG ALTER COLUMN SRC_URL NVARCHAR(2000);
ALTER TABLE TSMP_API_IMP ALTER COLUMN SRC_URL NVARCHAR(2000);
 
-- 20221216, v4 新增 SSO IdP使用者基本資料, Mini Lee
CREATE TABLE IF NOT EXISTS dgr_ac_idp_user ( 
	ac_idp_user_id 			BIGINT 			NOT NULL, 				-- ID 使用 RandomSeqLongUtil.getRandomLongByDefault() 產生 
	user_name 				NVARCHAR(400)	NOT NULL, 				-- 使用者名稱(視IdP類型決定) 
	user_alias 				VARCHAR(200), 							-- 使用者別名 
	user_status 			VARCHAR(1) 		NOT NULL DEFAULT '1', 	-- 使用者狀態 1：request(預設)，2：allow，"3：deny 
	user_email 				VARCHAR(500), 							-- 使用者E-Mail 
	org_id 					VARCHAR(200), 							-- 組織ID from TSMP_ORGANIZATION.org_id 
	idp_type 				VARCHAR(50) 	NOT NULL, 				-- IdP類型 例如:"MS" 或 "GOOGLE" 
	code1					BIGINT,									-- 安全驗證碼1
	code2					BIGINT,									-- 安全驗證碼2
	id_token_jwtstr			VARCHAR(4000),							-- IdP ID Token 的 JWT  	
	access_token_jwtstr		VARCHAR(4000),							-- IdP Access Token 的 JWT  	
	refresh_token_jwtstr	VARCHAR(4000),							-- IdP Refresh Token 的 JWT  	
	create_date_time 		DATETIME 		DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間
	create_user 			VARCHAR(255) 	DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 		DATETIME, 								-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料
	update_user 			VARCHAR(255), 							-- 更新人員 
	version 				INT DEFAULT 1, 							-- 版號 C/U時, 增量+1
	PRIMARY KEY (ac_idp_user_id), 
	UNIQUE (user_name, idp_type) 
);

-- 20221216, v4 新增 SSO IdP授權碼記錄檔, Mini Lee
CREATE TABLE IF NOT EXISTS dgr_ac_idp_auth_code ( 
	ac_idp_auth_code_id BIGINT 			NOT NULL, 				-- ID (流水號) 
	auth_code 			VARCHAR(50) 	NOT NULL, 				-- 授權碼, 即 dgRcode 
	expire_date_time 	BIGINT 			NOT NULL, 				-- 有效期限 超過此期限即不可使用此授權碼 
	status 				VARCHAR(1) 		NOT NULL DEFAULT '0', 	-- 狀態 0：可用；1：已使用；2：失效 
	idp_type 			VARCHAR(50), 							-- IdP類型 例如: "MS" 或 "GOOGLE" 
	user_name 			NVARCHAR(400) 	NOT NULL, 				-- 使用者名稱(視IdP類型決定) from DGR_AC_IDP_USER.user_name 
	create_date_time 	DATETIME 		DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	create_user 		VARCHAR(255) 	DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 	DATETIME, 								-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料
	update_user 		VARCHAR(255), 							-- 更新人員 
	version 			INT 			DEFAULT 1, 				-- 版號 C/U時, 增量+1 
	PRIMARY KEY (ac_idp_auth_code_id), 
	UNIQUE (auth_code) 
);

-- 20221226, v4 新增 SSO IdP資料, Mini Lee
CREATE TABLE IF NOT EXISTS dgr_ac_idp_info ( 
	ac_idp_info_id 		BIGINT 			NOT NULL, 				-- ID 使用 RandomSeqLongUtil 機制產生 
	idp_type 			VARCHAR(50) 	NOT NULL, 				-- IdP類型 例如:"MS" 或 "GOOGLE"  
	client_id 			NVARCHAR(400) 	NOT NULL, 				-- 用戶端編號(視IdP類型決定) 
	client_mima 		VARCHAR(200) 	NOT NULL, 				-- 用戶端密碼 
	client_name 		VARCHAR(200), 							-- 用戶端名稱 
	client_status 		VARCHAR(1) 		NOT NULL DEFAULT 'Y', 	-- 用戶端狀態 Y: 啟用 (預設), N: 停用 
	well_known_url 		VARCHAR(4000) 	NOT NULL, 				-- IdP 的 Well Known URL 
	callback_url 		VARCHAR(400) 	NOT NULL, 				-- 已授權的重新導向 URI
	auth_url 			VARCHAR(4000), 							-- IdP 的 Auth URL 
	access_token_url 	VARCHAR(4000), 							-- IdP 的 Access Token URL 
	scope 				VARCHAR(4000), 							-- IdP 的 scope 
	create_date_time 	DATETIME 		DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間
	create_user 		VARCHAR(255) 	DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 	DATETIME, 								-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料
	update_user 		VARCHAR(255), 							-- 更新人員 
	version 			INT DEFAULT 1, 							-- 版號 C/U時, 增量+1
	PRIMARY KEY (ac_idp_info_id), 
	UNIQUE (idp_type, client_id) 
); 

-- 20230223 v4 新增 SSO AC IdP資料 (LDAP), Mini Lee
CREATE TABLE IF NOT EXISTS dgr_ac_idp_info_ldap (  
	ac_idp_info_ldap_id 	BIGINT 			NOT NULL, 				-- ID 
	ldap_url 				VARCHAR(4000) 	NOT NULL, 				-- Ldap登入的URL 
	ldap_dn 				VARCHAR(4000) 	NOT NULL, 				-- Ldap登入的使用者DN 
	ldap_timeout 			INT 			NOT NULL, 				-- Ldap登入的連線timeout,單位毫秒 
	ldap_status 			VARCHAR(1) 		NOT NULL DEFAULT 'Y', 	-- Ldap狀態 
	approval_result_mail 	VARCHAR(4000) 	NOT NULL, 				-- 審核結果收件人,多組以逗號(,)隔開 
	icon_file				VARCHAR(4000),							-- 圖示檔案
	create_date_time 		DATETIME 		DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	create_user 			VARCHAR(255) 	DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 		DATETIME, 								-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料 
	update_user 			VARCHAR(255), 							-- 更新人員 
	version 				INT 			DEFAULT 1, 				-- 版號 C/U時, 增量+1
	PRIMARY KEY (ac_idp_info_ldap_id) 
);
 
-- 20230320, v4, SSO AC IdP資料 (LDAP), 增加欄位, Mini Lee
ALTER TABLE DGR_AC_IDP_INFO_LDAP ADD page_title VARCHAR(400);

-- 20230323, v4 tsmp_group_api,修改欄位長度, Zoe Lee
ALTER TABLE tsmp_group_api ALTER COLUMN api_key varchar(255);
 
-- 20230327, v4 新增 Gateway IdP Auth記錄檔主檔, Mini Lee
CREATE TABLE IF NOT EXISTS dgr_gtw_idp_auth_m (  
	gtw_idp_auth_m_id 	BIGINT 			NOT NULL, 		-- ID (流水號) 
	state 				VARCHAR(40) 	NOT NULL, 		-- 隨機字串UUID 
	idp_type 			VARCHAR(50) 	NOT NULL, 		-- IdP類型 
	client_id 			VARCHAR(40) 	NOT NULL, 		-- dgR 的 client_id
	auth_code 			VARCHAR(50),					-- 授權碼, 即 dgRcode
	create_date_time 	DATETIME 		DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間
	create_user 		VARCHAR(255) 	DEFAULT 'SYSTEM', -- 建立人員 
	update_date_time 	DATETIME, 						-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料
	update_user 		VARCHAR(255), 					-- 更新人員 
	version 			INT DEFAULT 1, 					-- 版號 C/U時, 增量+1
	PRIMARY KEY (gtw_idp_auth_m_id), 
	UNIQUE (state)  
);

-- 20230327 v4 新增 Gateway IdP Auth記錄檔明細檔, Mini Lee
CREATE TABLE IF NOT EXISTS dgr_gtw_idp_auth_d (  
	gtw_idp_auth_d_id 		BIGINT 			NOT NULL, 				-- ID (流水號) 
	ref_gtw_idp_auth_m_id 	BIGINT 			NOT NULL, 				-- MasterPK 
	scope 					VARCHAR(200) 	NOT NULL, 				-- OpenID Connect Scope
	create_date_time 		DATETIME 		DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	create_user 			VARCHAR(255) 	DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 		DATETIME, 								-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料 
	update_user 			VARCHAR(255), 							-- 更新人員 
	version 				INT 			DEFAULT 1, 				-- 版號 C/U時, 增量+1
	PRIMARY KEY (gtw_idp_auth_d_id)
);

-- 20230327, v4 新增 Gateway IdP授權碼記錄檔, Mini Lee
CREATE TABLE IF NOT EXISTS dgr_gtw_idp_auth_code ( 
	gtw_idp_auth_code_id 	BIGINT 			NOT NULL, 				-- ID (流水號) 
	auth_code 				VARCHAR(50) 	NOT NULL,				-- 授權碼, 即 dgRcode 
	phase					VARCHAR(10) 	NOT NULL,				-- 階段, "STATE" 或 "AUTH CODE"
	status 					VARCHAR(1)		NOT NULL DEFAULT 'A', 	-- 授權碼狀態 
	expire_date_time 		BIGINT 			NOT NULL,				-- 授權碼有效期限 
	idp_type 				VARCHAR(50) 	NOT NULL, 				-- IdP類型
	client_id 				VARCHAR(40),							-- dgRunner 的 client_id
	user_name 				VARCHAR(400)	NOT NULL, 				-- 使用者名稱 
	user_alias 				NVARCHAR(400), 							-- 使用者別名 
	user_email 				VARCHAR(500), 							-- 使用者E-Mail 
	user_picture 			VARCHAR(4000), 							-- 使用者圖示 
	id_token_jwtstr 		VARCHAR(4000), 							-- IdP ID Token 的 JWT 
	access_token_jwtstr 	VARCHAR(4000), 							-- IdP Access Token 的 JWT 
	refresh_token_jwtstr 	VARCHAR(4000), 							-- IdP Refresh Token 的 JWT 
	create_date_time 		DATETIME 		DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間
	create_user 			VARCHAR(255) 	DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 		DATETIME, 								-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料
	update_user 			VARCHAR(255), 							-- 更新人員 
	version 				INT 			DEFAULT 1, 				-- 版號 C/U時, 增量+1
	PRIMARY KEY (gtw_idp_auth_code_id), 
	UNIQUE (auth_code) 
);

-- 20230327 v4 新增 Gateway IdP資料 (Oauth2.0 GOOGLE / MS), Mini Lee
CREATE TABLE IF NOT EXISTS dgr_gtw_idp_info_o (
	gtw_idp_info_o_id 		BIGINT 			NOT NULL, 				-- ID, 使用 RandomSeqLongUtil 機制產生
	client_id 				VARCHAR(40) 	NOT NULL, 				-- digiRunner 的 client_id, from TSMP_CLIENT.client_id 
	idp_type 				VARCHAR(50) 	NOT NULL, 				-- IdP類型, 全大寫, 例如:"MS" 或 "GOOGLE" 
	status 					VARCHAR(1) 		NOT NULL DEFAULT 'Y', 	-- 狀態, Y: 啟用 (預設), N: 停用
	remark					VARCHAR(200), 							-- 說明
	idp_client_id 			NVARCHAR(400)	NOT NULL, 				-- IdP用戶端編號
	idp_client_mima 		VARCHAR(200)	NOT NULL, 				-- IdP用戶端密碼 
	idp_client_name 		VARCHAR(200), 							-- IdP用戶端名稱 
	well_known_url 			VARCHAR(4000), 							-- IdP 的 Well Known URL 
	callback_url 			VARCHAR(400) 	NOT NULL, 				-- 已授權的重新導向 URI 
	auth_url 				VARCHAR(4000), 							-- IdP 的 Auth URL 
	access_token_url 		VARCHAR(4000), 							-- IdP 的 Access Token URL 
	scope 					VARCHAR(4000), 							-- IdP 的 scope
	create_date_time 		DATETIME 		DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	create_user 			VARCHAR(255) 	DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 		DATETIME, 								-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料 
	update_user 			VARCHAR(255), 							-- 更新人員 
	version 				INT 			DEFAULT 1, 				-- 版號 C/U時, 增量+1
	PRIMARY KEY (gtw_idp_info_o_id)
); 

-- 20230327 v4 新增 Gateway IdP資料 (LDAP), Mini Lee
CREATE TABLE IF NOT EXISTS dgr_gtw_idp_info_l (  
	gtw_idp_info_l_id 		BIGINT 			NOT NULL, 				-- ID, 使用 RandomSeqLongUtil 機制產生
	client_id 				VARCHAR(40) 	NOT NULL, 				-- digiRunner 的 client_id, from TSMP_CLIENT.client_id
	status 					VARCHAR(1) 		NOT NULL DEFAULT 'Y', 	-- 狀態, Y: 啟用 (預設), N: 停用
	remark					VARCHAR(200), 							-- 說明
	ldap_url 				VARCHAR(4000) 	NOT NULL, 				-- Ldap登入的URL 
	ldap_dn 				VARCHAR(4000) 	NOT NULL, 				-- Ldap登入的使用者DN 
	ldap_timeout 			INT 			NOT NULL, 				-- Ldap登入的連線timeout,單位毫秒
	icon_file 				VARCHAR(4000), 							-- 登入頁圖示檔案 
	page_title 				VARCHAR(400) 	NOT NULL, 				-- 登入頁標題 
	create_date_time 		DATETIME 		DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	create_user 			VARCHAR(255) 	DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 		DATETIME, 								-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料 
	update_user 			VARCHAR(255), 							-- 更新人員 
	version 				INT 			DEFAULT 1, 				-- 版號 C/U時, 增量+1
	PRIMARY KEY (gtw_idp_info_l_id)
);
  
-- 20230327 v4 新增 Gateway IdP資料 (JDBC), Mini Lee
CREATE TABLE IF NOT EXISTS dgr_gtw_idp_info_j (  
	gtw_idp_info_j_id 		BIGINT 			NOT NULL, 				-- ID, 使用 RandomSeqLongUtil 機制產生
	client_id 				VARCHAR(40) 	NOT NULL, 				-- digiRunner 的 client_id, from TSMP_CLIENT.client_id 
	idp_type 				VARCHAR(50) 	NOT NULL, 				-- IdP類型, 全大寫
	status 					VARCHAR(1) 		NOT NULL DEFAULT 'Y', 	-- 狀態, Y: 啟用 (預設), N: 停用
	remark					VARCHAR(200), 							-- 說明
	host 					VARCHAR(4000) 	NOT NULL, 				-- 資料庫的Host 
	port 					INT 			NOT NULL, 				-- 資料庫的Port 
	db_schema 				VARCHAR(200) 	NOT NULL, 				-- 資料庫的名稱 
	db_user_name 			VARCHAR(200) 	NOT NULL, 				-- 資料庫的使用者名稱 
	db_user_mima 			VARCHAR(200) 	NOT NULL, 				-- 資料庫的密碼 
	icon_file				VARCHAR(4000),							-- 登入頁圖示檔案
	page_title				VARCHAR(400)	NOT NULL, 				-- 登入頁標題
	create_date_time 		DATETIME 		DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	create_user 			VARCHAR(255) 	DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 		DATETIME, 								-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料 
	update_user 			VARCHAR(255), 							-- 更新人員 
	version 				INT 			DEFAULT 1, 				-- 版號 C/U時, 增量+1
	PRIMARY KEY (gtw_idp_info_j_id)
);

-- 20230406 v4 新增websocket proxy, Tom
CREATE TABLE IF NOT EXISTS dgr_web_socket_mapping (  
	ws_mapping_id 	        BIGINT 			NOT NULL, 				-- ID 
	site_name 				VARCHAR(50) 	NOT NULL, 			    -- 站點名稱
	target_ws               VARCHAR(200) 	NOT NULL,	            -- web socket server的目標
	memo                    VARCHAR(4000), 				            -- 備註
	create_date_time 		DATETIME 		DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	create_user 			VARCHAR(255) 	DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 		DATETIME, 								-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料 
	update_user 			VARCHAR(255), 							-- 更新人員 
	version 				INT 			DEFAULT 1, 				-- 版號 C/U時, 增量+1
	keyword_search		    VARCHAR(250),						-- LikeSearch使用: site_name | target_ws
	CONSTRAINT PK_DGR_WEB_SOCKET_MAPPING PRIMARY KEY (ws_mapping_id),
	CONSTRAINT UK_DGR_WEB_SOCKET_MAPPING UNIQUE (site_name)
);

--INSERT INTO dgr_web_socket_mapping(ws_mapping_id, site_name, target_ws, memo)VALUES(1, 'ws1', 'wss://127.0.0.1:8811/wsTest/t1', '');

-- 20230407, v4 網站反向代理主檔, Kevin Cheng
CREATE TABLE DGR_WEBSITE (
	DGR_WEBSITE_ID BIGINT NOT NULL,
	WEBSITE_NAME VARCHAR(50) NOT NULL,
	WEBSITE_STATUS VARCHAR(1) DEFAULT 'Y' NOT NULL,
	REMARK VARCHAR(500),
	AUTH VARCHAR(1) DEFAULT 'N' NOT NULL,
	SQL_INJECTION VARCHAR(1) DEFAULT 'N' NOT NULL,
	TRAFFIC VARCHAR(1) DEFAULT 'N' NOT NULL,
	XSS VARCHAR(1) DEFAULT 'N' NOT NULL,
	XXE VARCHAR(1) DEFAULT 'N' NOT NULL,
	SHOW_LOG VARCHAR(1) DEFAULT 'N' NOT NULL,
	TPS INT DEFAULT 0 NOT NULL,
	IGNORE_API NVARCHAR(4000),
	CREATE_DATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	CREATE_USER VARCHAR(255) DEFAULT 'SYSTEM',
	UPDATE_DATE_TIME TIMESTAMP,
	UPDATE_USER VARCHAR(255),
	VERSION INTEGER DEFAULT 1,
	KEYWORD_SEARCH NVARCHAR(600),
	CONSTRAINT CONSTRAINT_DGR_WEBSITE PRIMARY KEY (DGR_WEBSITE_ID)
);

-- 20230407, v4 網站反向代理明細檔, Kevin Cheng
CREATE TABLE DGR_WEBSITE_DETAIL (
	DGR_WEBSITE_DETAIL_ID BIGINT NOT NULL,
	DGR_WEBSITE_ID BIGINT NOT NULL,
	PROBABILITY INTEGER NOT NULL,
	URL VARCHAR(1000) NOT NULL,
	CREATE_DATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	CREATE_USER VARCHAR(255) DEFAULT 'SYSTEM',
	UPDATE_DATE_TIME TIMESTAMP,
	UPDATE_USER VARCHAR(255),
	VERSION INTEGER DEFAULT 1,
	KEYWORD_SEARCH NVARCHAR(1500),
	CONSTRAINT CONSTRAINT_DGR_WEBSITE_DETAIL PRIMARY KEY (DGR_WEBSITE_DETAIL_ID)
);

-- 20230419, TSMP access_token 歷史紀錄, 增加欄位, Mini Lee
ALTER TABLE TSMP_TOKEN_HISTORY ADD IDP_TYPE VARCHAR(50);

-- 20230421, TSMP Token 歷史紀錄, 增加欄位, Mini Lee
ALTER TABLE TSMP_TOKEN_HISTORY ADD ID_TOKEN_JWTSTR VARCHAR(4000);
ALTER TABLE TSMP_TOKEN_HISTORY ADD REFRESH_TOKEN_JWTSTR VARCHAR(4000);

-- 20230511, SSO AC IdP資料 (LDAP), 增加欄位, Mini Lee
ALTER TABLE dgr_ac_idp_info_ldap ADD ldap_base_dn VARCHAR(4000);

-- 20230511, Gateway IdP資料 (LDAP), 增加欄位, Mini Lee
ALTER TABLE dgr_gtw_idp_info_l ADD ldap_base_dn VARCHAR(4000);

-- 20230616, dashboard最新資料, Tom chu
CREATE TABLE IF NOT EXISTS dgr_dashboard_last_data (
	dashboard_id BIGINT NOT NULL,
	dashboard_type INT NOT NULL,
	time_type INT NOT NULL,
	str1 NVARCHAR(500),
	str2 NVARCHAR(500),
	str3 NVARCHAR(500),
	num1 BIGINT,
	num2 BIGINT,
	num3 BIGINT,
	num4 BIGINT,
	sort_num INT DEFAULT 1,                                  
    CONSTRAINT pk_dgr_dashboard_last_data PRIMARY KEY (dashboard_id)
);
--  20230705,tsmp_req_res_log_history  ,zoe Lee
CREATE TABLE IF NOT EXISTS tsmp_req_res_log_history (
    id    VARCHAR(63) NOT NULL,    -- ID
    rtime    TIMESTAMP NOT NULL,    -- record time
    atype    VARCHAR(3) NOT NULL,    -- API type
    module_name    VARCHAR(255) NOT NULL,    -- 模組名稱
    module_version    VARCHAR(255) NOT NULL,    -- 模組版本
    node_alias    VARCHAR(255) NOT NULL,    -- tsmp node alias
    node_id    VARCHAR(255) NOT NULL,    -- node id
    url    VARCHAR(255) NOT NULL,    -- 呼叫API時的路徑
    cip    VARCHAR(255) NOT NULL,    -- client remote ip
    orgid    VARCHAR(255) NOT NULL,    -- API隸屬於哪個組織的ID
    txid    VARCHAR(255),    -- 其實也就是ApiKey
    entry    VARCHAR(255),    -- tsmpc , tsmpg 轉導時寫入 , 一般module為null
    cid    VARCHAR(255),    -- token所攜帶的client id
    tuser    VARCHAR(255),    -- token所攜帶的user
    jti    VARCHAR(255),    -- token的jti
    exe_status    CHAR NOT NULL,    -- 此次API呼叫成功與否
    elapse    INT NOT NULL,    -- API執行耗時(ms)
    rcode    VARCHAR(63) NOT NULL,    -- return code
    http_status    INT NOT NULL,    -- response的http status
    err_msg    VARCHAR(4000),    -- 錯誤訊息

    PRIMARY KEY(id)
);

-- 20230717, Gateway IdP資料 (API), Mini Lee
CREATE TABLE IF NOT EXISTS dgr_gtw_idp_info_a (  
	gtw_idp_info_a_id 	BIGINT NOT NULL, 					-- ID, 使用 RandomSeqLongUtil 機制產生 
	client_id 			VARCHAR(40) NOT NULL, 				-- 在 digiRunner 註冊的 client_id 
	status 				VARCHAR(1) 	NOT NULL DEFAULT 'Y', 	-- 狀態 
	remark 				VARCHAR(200), 						-- 說明 
	api_method 			VARCHAR(10) NOT NULL, 				-- 登入的 API HTTP method 
	api_url 			VARCHAR(4000) NOT NULL, 			-- 登入的 API URL
	req_header 			VARCHAR(4000), 						-- 調用 API 的 Request Header 內容 
	req_body_type 		VARCHAR(1) NOT NULL DEFAULT 'N', 	-- 調用 API 的 Request Body 類型 
	req_body 			VARCHAR(4000), 						-- 調用 API 的 Request Body 內容 
	suc_by_type 		VARCHAR(1) NOT NULL DEFAULT 'H', 	-- 判定登入成功的類型 
	suc_by_field 		VARCHAR(200), 						-- 當 suc_by_type 為 "R",判定登入成功的 Response JSON 欄位 
	suc_by_value 		VARCHAR(200), 						-- 當 suc_by_type 為 "R",判定登入成功的 Response JSON 值
	idt_name 			VARCHAR(200), 						-- ID token 的 name 值,來源 Response JSON 欄位 
	idt_email 			VARCHAR(200), 						-- ID token 的 email 值,來源 Response JSON 欄位 
	idt_picture 		VARCHAR(200), 						-- ID token 的 picture 值,來源 Response JSON 欄位
	icon_file 			VARCHAR(4000), 						-- 登入頁圖示檔案 
	page_title 			VARCHAR(400) 	NOT NULL, 			-- 登入頁標題 
	create_date_time 	DATETIME DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	create_user 		VARCHAR(255) DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 	DATETIME, 							-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料 
	update_user 		VARCHAR(255), 						-- 更新人員 
	version 			INT DEFAULT 1, 						-- 版號 C/U時, 增量+1  
	PRIMARY KEY (gtw_idp_info_a_id)   
);

-- 20230801, SSO AC IdP資料 (Multi-LDAP) 主檔, Mini Lee
CREATE TABLE IF NOT EXISTS dgr_ac_idp_info_mldap_m(
	ac_idp_info_mldap_m_id 	BIGINT NOT NULL, 					-- ID 
	ldap_timeout 			INT NOT NULL, 						-- Ldap登入的連線timeout,單位毫秒 
	status 					VARCHAR(1) NOT NULL DEFAULT 'Y', 	-- 狀態 
	policy 					VARCHAR(1) NOT NULL DEFAULT 'S', 	-- 驗證的方式, 依順序或隨機 
	approval_result_mail	VARCHAR(4000) NOT NULL, 			-- 審核結果收件人,多組以逗號(,)隔開 
	icon_file 				VARCHAR(4000), 						-- 圖示檔案 
	page_title				VARCHAR(400) NOT NULL, 				-- 登入頁標題 
	create_date_time 		DATETIME DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	create_user 			VARCHAR(255) DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 		DATETIME, 							-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料 
	update_user 			VARCHAR(255), 						-- 更新人員 
	version 				INT DEFAULT 1, 						-- 版號 C/U時, 增量+1 
	PRIMARY KEY (ac_idp_info_mldap_m_id) 
);

-- 20230801, SSO AC IdP資料 (Multi-LDAP) 明細檔, Mini Lee
CREATE TABLE IF NOT EXISTS dgr_ac_idp_info_mldap_d (  
	ac_idp_info_mldap_d_id 		BIGINT NOT NULL, 					-- ID 
	ref_ac_idp_info_mldap_m_id 	BIGINT NOT NULL, 					-- Master PK 
	order_no 					INT NOT NULL, 						-- 順序 
	ldap_url 					VARCHAR(4000) NOT NULL, 			-- Ldap登入的URL 
	ldap_dn 					VARCHAR(4000) NOT NULL, 			-- Ldap登入的使用者DN 
	ldap_base_dn 				VARCHAR(4000) NOT NULL, 			-- Ldap基礎DN   
	create_date_time 			DATETIME DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	create_user 				VARCHAR(255) DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 			DATETIME, 							-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料 
	update_user 				VARCHAR(255), 						-- 更新人員 
	version 					INT DEFAULT 1, 						-- 版號 C/U時, 增量+1
	PRIMARY KEY (ac_idp_info_mldap_d_id)    
);  

-- 20230802 , tsmp_req_res_log_history.rtime 格式改為datetime  , Zoe Lee
ALTER TABLE tsmp_req_res_log_history DROP COLUMN rtime;
ALTER TABLE tsmp_req_res_log_history ADD rtime datetime ;
-- 20230802 , tsmp_req_res_log_history增加新欄位rtime_year_month  , Zoe Lee
ALTER TABLE tsmp_req_res_log_history ADD rtime_year_month varchar(8);

-- 20230821 , dgr_dashboard_es_log  , Zoe Lee
CREATE TABLE IF NOT EXISTS dgr_dashboard_es_log (
    id    VARCHAR(63) NOT NULL,    -- ID
    rtime    DATETIME NOT NULL,    -- record time
    module_name    VARCHAR(255) NOT NULL,    -- 模組名稱
    orgid    VARCHAR(255) NOT NULL,    -- API隸屬於哪個組織的ID
    txid    VARCHAR(255),    -- 其實也就是ApiKey
    cid    VARCHAR(255),    -- token所攜帶的client id
    exe_status    CHAR NOT NULL,    -- 此次API呼叫成功與否
    elapse    INT NOT NULL,    -- API執行耗時(ms)
    http_status    INT NOT NULL,    -- response的http status
    rtime_year_month    VARCHAR(8),    -- RTIME的年月
    PRIMARY KEY(id)
);

-- 20230407, v4 入口網(DP)的Application	, Kevin Cheng
CREATE TABLE IF NOT EXISTS dp_app (
	dp_application_id BIGINT NOT NULL,                         -- ID
	application_name NVARCHAR(50) NOT NULL,                     -- Application名稱
	application_desc NVARCHAR(500),                             -- Application說明
	client_id VARCHAR(40) NOT NULL,                            -- CLIENT_ID
	open_apikey_id BIGINT,                                     -- 
	open_apikey_status VARCHAR(1),                             -- DGRK狀態
	user_name NVARCHAR(400) NOT NULL,                          -- 使用者名稱(視IdP類型決定)
	id_token_jwtstr VARCHAR(4000) NOT NULL,                    -- IdP ID Token 的 JWT
	create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,      -- 建立日期
	create_user VARCHAR(255) DEFAULT 'SYSTEM',                 -- 建立人員
	update_date_time TIMESTAMP,                                -- 更新日期
	update_user VARCHAR(255),                                  -- 更新人員
	version INT DEFAULT 1,                                     -- 版號
	KEYWORD_SEARCH NVARCHAR(600),
	PRIMARY KEY (dp_application_id)
);

-- 20230420, v4 入口網(DP)的使用者	, Kevin Cheng
CREATE TABLE IF NOT EXISTS dp_user (
    dp_user_id BIGINT NOT NULL,                           -- ID
    user_name NVARCHAR(400) NOT NULL,                     -- 使用者名稱(視IdP類型決定)
    user_alias VARCHAR(200),                              -- 使用者別名
    id_token_jwtstr VARCHAR(4000) NOT NULL,               -- IdP ID Token 的 JWT
    user_identity VARCHAR(1) NOT NULL DEFAULT 'U',                 -- 使用者身份
    create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 建立日期
    create_user VARCHAR(255) DEFAULT 'SYSTEM',            -- 建立人員
    update_date_time TIMESTAMP,                           -- 更新日期
    update_user VARCHAR(255),                             -- 更新人員
    version INT DEFAULT 1,                                -- 版號
	KEYWORD_SEARCH NVARCHAR(800),
    PRIMARY KEY (dp_user_id),
    UNIQUE (user_name)
);

-- 20230420, v4 入口網(DP)的API_DOC檔案	, Kevin Cheng
CREATE TABLE IF NOT EXISTS dp_file (
    dp_file_id BIGINT NOT NULL,                              -- ID
    file_name NVARCHAR(100) NOT NULL,                        -- 檔案名稱
    module_name NVARCHAR(150) NOT NULL,                      -- Module Name
    api_key NVARCHAR(255) NOT NULL,                                   -- API Key
    blob_data BLOB NOT NULL,                                 -- 檔案本體
    create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,    -- 建立日期
    create_user VARCHAR(255) DEFAULT 'SYSTEM',               -- 建立人員
    update_date_time TIMESTAMP,                              -- 更新日期
    update_user VARCHAR(255),                                -- 更新人員
    version INT DEFAULT 1,                                   -- 版號
    PRIMARY KEY (dp_file_id)
);

-- 20230515, v4 入口網(DP)的Application	, 刪除欄位 , Kevin Cheng
ALTER TABLE dp_app DROP COLUMN open_apikey_status;

-- 20230706, v4 入口網(DP) API Version , Kevin Cheng
CREATE TABLE IF NOT EXISTS dp_api_version (
  dp_api_version_id BIGINT NOT NULL,                    -- ID
  module_name NVARCHAR(150) NOT NULL,                    -- Module Name
  api_key NVARCHAR(255) NOT NULL,                        -- API Key
  dp_api_version NVARCHAR(10) NOT NULL,                  -- API版本號
  start_of_life BIGINT NOT NULL,                        -- API生命週期(起)
  end_of_life BIGINT,                                   -- API生命週期(迄)
  remark NVARCHAR(500),                                  -- 備註
  time_zone VARCHAR(200) NOT NULL,                      -- 時區
  create_date_time TIMESTAMP AS CURRENT_TIMESTAMP,      -- 建立日期
  create_user VARCHAR(255) DEFAULT 'SYSTEM',            -- 建立人員
  update_date_time TIMESTAMP,                           -- 更新日期
  update_user VARCHAR(255),                             -- 更新人員
  version INT DEFAULT 1,                                -- 版號
  PRIMARY KEY (dp_api_version_id)
);

-- 20230105, v4 修改 dp_app 欄位型態 , min
ALTER TABLE dp_app ALTER COLUMN application_name NVARCHAR(50);
ALTER TABLE dp_app ALTER COLUMN application_desc NVARCHAR(500);
 
-- 20230906, Gateway IdP授權碼記錄檔, 增加欄位, Mini Lee
ALTER TABLE DGR_GTW_IDP_AUTH_CODE ADD API_RESP NVARCHAR(4000);

-- 20230906, TSMP Token 歷史紀錄, 增加欄位, Mini Lee
ALTER TABLE TSMP_TOKEN_HISTORY ADD API_RESP NVARCHAR(4000);

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
	stack_trace		VARCHAR(4000),	-- 紀錄寄信失敗log
	PRIMARY KEY (maillog_id)
);

-- 20230912, Gateway IdP Auth記錄檔主檔, 增加欄位, Mini Lee
ALTER TABLE DGR_GTW_IDP_AUTH_M ADD REDIRECT_URI VARCHAR(400); 

-- 20230912, TSMP用戶端OAuth2驗證資料(Spring), 增加欄位, Mini Lee
ALTER TABLE OAUTH_CLIENT_DETAILS ADD WEB_SERVER_REDIRECT_URI1 NVARCHAR(255); 
ALTER TABLE OAUTH_CLIENT_DETAILS ADD WEB_SERVER_REDIRECT_URI2 NVARCHAR(255);
ALTER TABLE OAUTH_CLIENT_DETAILS ADD WEB_SERVER_REDIRECT_URI3 NVARCHAR(255);
ALTER TABLE OAUTH_CLIENT_DETAILS ADD WEB_SERVER_REDIRECT_URI4 NVARCHAR(255);
ALTER TABLE OAUTH_CLIENT_DETAILS ADD WEB_SERVER_REDIRECT_URI5 NVARCHAR(255);

-- 20230920, TSMP API基本資料, 增加欄位 API_RELEASE_TIME, Kevin Cheng
ALTER TABLE TSMP_API ADD API_RELEASE_TIME DATETIME NULL;

-- 20231003, 增加 dgr_ac_idp_info_api, zoe lee 
CREATE TABLE IF NOT EXISTS dgr_ac_idp_info_api (
    ac_idp_info_api_id    BIGINT NOT NULL,    -- ID
    status    VARCHAR(1) NOT NULL DEFAULT 'Y',    -- 狀態
    approval_result_mail    VARCHAR(4000) NOT NULL,    -- 審核結果收件人,多組以逗號(,)隔開
    api_method    VARCHAR(10) NOT NULL,    -- 登入 API 的 HTTP method
    api_url    VARCHAR(4000) NOT NULL,    -- 登入 API 的 URL
    req_header    VARCHAR(4000),    -- 調用 API 的 Request Header 內容
	req_body_type    VARCHAR(1) NOT NULL DEFAULT 'N',    -- 調用 API 的 Request Body 類型
    req_body    VARCHAR(4000),    -- 調用 API 的 Request Body 內容
	suc_by_type    VARCHAR(1) NOT NULL DEFAULT 'H',    -- 判定登入成功的類型
	suc_by_field    VARCHAR(200),    -- 當 SUC_BY_TYPE 為 "R",判定登入成功的 Response JSON 欄位
	suc_by_value    VARCHAR(200),    -- 當 SUC_BY_TYPE 為 "R",判定登入成功的 Response JSON 值,多個以逗號分隔(不要有空格)
    idt_name    VARCHAR(200),    -- ID token 的 name 值,對應登入 API Response JSON 欄位
    idt_email    VARCHAR(200),    -- ID token 的 email 值,對應登入 API Response JSON 欄位
    idt_picture    VARCHAR(200),    -- ID token 的 picture 值,對應登入 API Response JSON 欄位
    icon_file    VARCHAR(4000),    -- 登入頁圖示檔案
    page_title    VARCHAR(400) NOT NULL,    -- 登入頁標題
	create_date_time    TIMESTAMP,    -- 建立日期
    create_user    VARCHAR(255) DEFAULT 'SYSTEM',    -- 建立人員
    update_date_time    TIMESTAMP,    -- 更新日期
    update_user    VARCHAR(255),    -- 更新人員
    version    INT DEFAULT '1',    -- 版號
    keyword_search    VARCHAR(200),    -- LikeSearch使用
    PRIMARY KEY(ac_idp_info_api_id)
);

-- 20231003, 更改 dgr_ac_idp_user user_alias 型態, zoe lee 
ALTER TABLE dgr_ac_idp_user ALTER COLUMN user_alias nvarchar(200)  NULL;

-- 20231011, rdb連線資訊, tom
CREATE TABLE IF NOT EXISTS dgr_rdb_connection (
    connection_name    NVARCHAR(50) NOT NULL,    -- 名稱
    jdbc_url    VARCHAR(200) NOT NULL,    -- 連線URL
    user_name    VARCHAR(100) NOT NULL,    -- 帳號
    mima    VARCHAR(500) NOT NULL,    -- MIMA
    max_pool_size    INT NOT NULL DEFAULT 10,    -- 最大連線數量
    connection_timeout    INT NOT NULL DEFAULT 30000,    -- 連線取得超時設定(ms)
    idle_timeout    INT NOT NULL DEFAULT 600000,    -- 空閒連線的存活時間(ms)
    max_lifetime    INT NOT NULL DEFAULT 1800000,    -- 連線的最大存活時間(ms)
    data_source_property    VARCHAR(4000),    -- DataSourceProperty的設定
    create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,      -- 建立日期
    create_user VARCHAR(255) DEFAULT 'SYSTEM',            -- 建立人員
    update_date_time TIMESTAMP,                           -- 更新日期
    update_user VARCHAR(255),                             -- 更新人員
    version INT DEFAULT 1,                                -- 版號
    CONSTRAINT pk_dgr_rdb_connection PRIMARY KEY(connection_name)
);

-- 20231020, 增加欄位長度, Mini Lee
ALTER TABLE DGR_GTW_IDP_AUTH_M ALTER COLUMN STATE VARCHAR(1000) NOT NULL;
ALTER TABLE DGR_GTW_IDP_AUTH_CODE ALTER COLUMN AUTH_CODE VARCHAR(1000) NOT NULL;
 
-- 20231101, Gateway IdP Auth記錄檔主檔, 增加欄位, Mini Lee
ALTER TABLE DGR_GTW_IDP_AUTH_M ADD CODE_CHALLENGE NVARCHAR(1000);
ALTER TABLE DGR_GTW_IDP_AUTH_M ADD CODE_CHALLENGE_METHOD VARCHAR(10);
 
-- 20231101, Gateway IdP授權碼記錄檔, 增加欄位, Mini Lee
ALTER TABLE DGR_GTW_IDP_AUTH_CODE ADD STATE VARCHAR(1000);

-- 20231102, 增加欄位, Zoe Lee
ALTER TABLE TSMP_API_REG ADD REDIRECT_BY_IP VARCHAR2(1) DEFAULT 'N' NULL;
ALTER TABLE TSMP_API_REG ADD IP_FOR_REDIRECT1 nvarchar(2000) NULL;
ALTER TABLE TSMP_API_REG ADD IP_SRC_URL1 nvarchar(2000) NULL;
ALTER TABLE TSMP_API_REG ADD IP_FOR_REDIRECT2 nvarchar(2000) NULL;
ALTER TABLE TSMP_API_REG ADD IP_SRC_URL2 nvarchar(2000) NULL;
ALTER TABLE TSMP_API_REG ADD IP_FOR_REDIRECT3 nvarchar(2000) NULL;
ALTER TABLE TSMP_API_REG ADD IP_SRC_URL3 nvarchar(2000) NULL;
ALTER TABLE TSMP_API_REG ADD IP_FOR_REDIRECT4 nvarchar(2000) NULL;
ALTER TABLE TSMP_API_REG ADD IP_SRC_URL4 nvarchar(2000) NULL;
ALTER TABLE TSMP_API_REG ADD IP_FOR_REDIRECT5 nvarchar(2000) NULL;
ALTER TABLE TSMP_API_REG ADD IP_SRC_URL5 nvarchar(2000) NULL;

ALTER TABLE TSMP_API_REG ADD HEADER_MASK_KEY nvarchar(2000) NULL;
ALTER TABLE TSMP_API_REG ADD HEADER_MASK_POLICY VARCHAR2(1) DEFAULT '0'  NULL;
ALTER TABLE TSMP_API_REG ADD HEADER_MASK_POLICY_NUM int NULL;
ALTER TABLE TSMP_API_REG ADD HEADER_MASK_POLICY_SYMBOL varchar(10)  NULL;

ALTER TABLE TSMP_API_REG ADD BODY_MASK_KEYWORD nvarchar(2000) NULL;
ALTER TABLE TSMP_API_REG ADD BODY_MASK_POLICY VARCHAR2(1) DEFAULT '0' NULL;
ALTER TABLE TSMP_API_REG ADD BODY_MASK_POLICY_NUM int NULL;
ALTER TABLE TSMP_API_REG ADD BODY_MASK_POLICY_SYMBOL varchar(10) NULL;
 
--20231108, 增加欄位 ,Zoe Lee
ALTER TABLE TSMP_API_IMP ADD REDIRECT_BY_IP VARCHAR2(1) DEFAULT 'N' NULL;
ALTER TABLE TSMP_API_IMP ADD IP_FOR_REDIRECT1 nvarchar(2000) NULL;
ALTER TABLE TSMP_API_IMP ADD IP_SRC_URL1 nvarchar(2000) NULL;
ALTER TABLE TSMP_API_IMP ADD IP_FOR_REDIRECT2 nvarchar(2000) NULL;
ALTER TABLE TSMP_API_IMP ADD IP_SRC_URL2 nvarchar(2000) NULL;
ALTER TABLE TSMP_API_IMP ADD IP_FOR_REDIRECT3 nvarchar(2000) NULL;
ALTER TABLE TSMP_API_IMP ADD IP_SRC_URL3 nvarchar(2000) NULL;
ALTER TABLE TSMP_API_IMP ADD IP_FOR_REDIRECT4 nvarchar(2000) NULL;
ALTER TABLE TSMP_API_IMP ADD IP_SRC_URL4 nvarchar(2000) NULL;
ALTER TABLE TSMP_API_IMP ADD IP_FOR_REDIRECT5 nvarchar(2000) NULL;
ALTER TABLE TSMP_API_IMP ADD IP_SRC_URL5 nvarchar(2000) NULL;

ALTER TABLE TSMP_API_IMP ADD HEADER_MASK_KEY nvarchar(2000) NULL;
ALTER TABLE TSMP_API_IMP ADD HEADER_MASK_POLICY VARCHAR2(1) DEFAULT '0'  NULL;
ALTER TABLE TSMP_API_IMP ADD HEADER_MASK_POLICY_NUM int NULL;
ALTER TABLE TSMP_API_IMP ADD HEADER_MASK_POLICY_SYMBOL varchar(10)  NULL;

ALTER TABLE TSMP_API_IMP ADD BODY_MASK_KEYWORD nvarchar(2000) NULL;
ALTER TABLE TSMP_API_IMP ADD BODY_MASK_POLICY VARCHAR2(1) DEFAULT '0' NULL;
ALTER TABLE TSMP_API_IMP ADD BODY_MASK_POLICY_NUM int NULL;
ALTER TABLE TSMP_API_IMP ADD BODY_MASK_POLICY_SYMBOL varchar(10) NULL;

-- 20231114, Gateway IdP資料 (JDBC), Mini Lee  
CREATE TABLE IF NOT EXISTS dgr_gtw_idp_info_jdbc (  
	GTW_IDP_INFO_JDBC_ID BIGINT NOT NULL, 		-- ID 
	CLIENT_ID 			VARCHAR(40) NOT NULL, 	-- digiRunner 的 client_id 
	STATUS 				VARCHAR(1) NOT NULL DEFAULT 'Y', -- 狀態 
	REMARK 				NVARCHAR(200), 			-- 說明 
	CONNECTION_NAME 	NVARCHAR(50) NOT NULL, 	-- RDB連線資訊的名稱 
	SQL_PTMT 			NVARCHAR(1000) NOT NULL, -- 查詢RDB的SQL(Prepare Statement) 
	SQL_PARAMS	 		NVARCHAR(1000) NOT NULL, -- 查詢RDB的SQL參數 
	USER_MIMA_ALG 		VARCHAR(40) NOT NULL, 	-- RDB存放密碼所使用的演算法 
	USER_MIMA_COL_NAME 	VARCHAR(200) NOT NULL, 	-- RDB的密碼欄位名稱 
	IDT_SUB 			VARCHAR(200) NOT NULL, 	-- ID token 的 sub(唯一值) 值,對應RDB的欄位 
	IDT_NAME 			VARCHAR(200), 			-- ID token 的 name 值,對應RDB的欄位 
	IDT_EMAIL 			VARCHAR(200), 			-- ID token 的 email 值,對應RDB的欄位 
	IDT_PICTURE 		VARCHAR(200), 			-- ID token 的 picture 值,對應RDB的欄位 
	ICON_FILE 			VARCHAR(4000), 			-- 登入頁圖示檔案 
	PAGE_TITLE 			NVARCHAR(400) NOT NULL, -- 登入頁標題
	CREATE_DATE_TIME 	DATETIME DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人
	CREATE_USER 		NVARCHAR(1000) DEFAULT 'SYSTEM', -- 建立人員 
	UPDATE_DATE_TIME 	DATETIME, 				-- 更新日期 表示最後Update的人
	UPDATE_USER 		NVARCHAR(1000), 		-- 更新人員 
	VERSION 			INT DEFAULT 1,  		-- 版號 C/U時, 增量+1 
	PRIMARY KEY (GTW_IDP_INFO_JDBC_ID)    
);

-- 20231123, v4 入口網(DP) DP USER 新增 ISS 欄位, Kevin Cheng
ALTER TABLE dp_user ADD iss VARCHAR(4000) NOT NULL DEFAULT 'NULL';
-- 20231123, v4 入口網(DP) DP APP 新增 ISS 欄位, Kevin Cheng
ALTER TABLE dp_app ADD iss VARCHAR(4000) NOT NULL DEFAULT 'NULL';

--20231123, 增加欄位 ,Zoe Lee
ALTER TABLE TSMP_API ADD LABEL1 nvarchar(20) NULL;
ALTER TABLE TSMP_API ADD LABEL2 nvarchar(20) NULL;
ALTER TABLE TSMP_API ADD LABEL3 nvarchar(20) NULL;
ALTER TABLE TSMP_API ADD LABEL4 nvarchar(20) NULL;
ALTER TABLE TSMP_API ADD LABEL5 nvarchar(20) NULL;
ALTER TABLE TSMP_API_IMP ADD LABEL1 nvarchar(20) NULL;
ALTER TABLE TSMP_API_IMP ADD LABEL2 nvarchar(20) NULL;
ALTER TABLE TSMP_API_IMP ADD LABEL3 nvarchar(20) NULL;
ALTER TABLE TSMP_API_IMP ADD LABEL4 nvarchar(20) NULL;
ALTER TABLE TSMP_API_IMP ADD LABEL5 nvarchar(20) NULL;
-- 20231127, v4 入口網(DP) DP USER 移除舊有的 user name uk, 新增 user name 與 iss uk, Kevin Cheng
ALTER TABLE dp_user ADD dp_user_name NVARCHAR(400) NOT NULL DEFAULT 'NULL';
UPDATE dp_user SET dp_user_name = user_name WHERE user_name IS NOT NULL;
ALTER TABLE dp_user ALTER COLUMN user_name DROP NOT NULL;
ALTER TABLE dp_app  ADD dp_user_name NVARCHAR(400);
UPDATE dp_app SET dp_user_name = user_name WHERE user_name IS NOT NULL;
ALTER TABLE dp_app DROP COLUMN user_name;
--20231130, SRC_URL 拿掉NOT NULL ,Zoe Lee
ALTER TABLE TSMP_API_REG ALTER COLUMN SRC_URL DROP NOT NULL;

-- 20231202, 增加DP JDBC登入資訊 ,jhmin
CREATE TABLE DP_USER_INFO_RDB
(
   USER_ID       bigint          NOT NULL,
   USER_NAME     nvarchar(60)    NOT NULL,
   MIMA      nvarchar(200)   NOT NULL,
   USER_ALIAS    nvarchar(60),
   USER_EMAIL    nvarchar(100)   NOT NULL,
   USER_PICTURE  nvarchar(400),
   CREATE_DATE_TIME 	DATETIME DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人
   CREATE_USER 		NVARCHAR(1000) DEFAULT 'SYSTEM', -- 建立人員 
   UPDATE_DATE_TIME 	DATETIME, 				-- 更新日期 表示最後Update的人
   UPDATE_USER 		NVARCHAR(1000), 		-- 更新人員 
   VERSION 			INT DEFAULT 1,  		-- 版號 C/U時, 增量+1 
  PRIMARY KEY (USER_ID)   
);

-- 20231207, X-Api-Key資料, Mini Lee
CREATE TABLE IF NOT EXISTS DGR_X_API_KEY (  
	API_KEY_ID 			BIGINT NOT NULL, 		-- ID 
	CLIENT_ID 			VARCHAR(40) NOT NULL, 	-- digiRunner 的 client_id 
	API_KEY_ALIAS 		NVARCHAR(100) NOT NULL, -- X-Api-Key 別名 
	EFFECTIVE_AT 		BIGINT NOT NULL, 		-- 生效日期 
	EXPIRED_AT 			BIGINT NOT NULL, 		-- 到期日期 
	API_KEY 			VARCHAR(100), 			-- X-Api-Key 的值 	
	API_KEY_MASK 		VARCHAR(100) NOT NULL, 	-- X-Api-Key 經過遮罩的值 
	API_KEY_EN 			VARCHAR(100) NOT NULL, 	-- X-Api-Key 經過SHA256 的值 
	CREATE_DATE_TIME 	DATETIME DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	CREATE_USER 		NVARCHAR(1000) DEFAULT 'SYSTEM', -- 建立人員 
	UPDATE_DATE_TIME 	DATETIME, 				-- 更新日期 表示最後Update的人, 日期時間
	UPDATE_USER 		NVARCHAR(1000), 		-- 更新人員 
	VERSION 			INT DEFAULT 1, 			-- 版號 C/U時, 增量+1  
	PRIMARY KEY (API_KEY_ID)    
);  

-- 20231207, X-Api-Key與群組關係, Mini Lee
CREATE TABLE IF NOT EXISTS DGR_X_API_KEY_MAP (  
	API_KEY_MAP_ID 		BIGINT NOT NULL, 		-- ID 
	REF_API_KEY_ID 		BIGINT NOT NULL, 		-- Master PK 
	GROUP_ID 			NVARCHAR(10) NOT NULL, 	-- 群組 ID 
	CREATE_DATE_TIME 	DATETIME DEFAULT CURRENT_TIMESTAMP, -- 建立日期 
	CREATE_USER 		NVARCHAR(1000) DEFAULT 'SYSTEM', -- 建立人員 
	UPDATE_DATE_TIME 	DATETIME, 				-- 更新日期 
	UPDATE_USER 		NVARCHAR(1000), 		-- 更新人員 
	VERSION 			INT DEFAULT 1, 			-- 版號
	PRIMARY KEY (API_KEY_MAP_ID)  
);   

-- 20231207, 移除 user_name 欄位, jhmin
ALTER TABLE dp_user DROP COLUMN user_name;

-- 20231212, 增加欄位 DP_CLIENT_SECRET, Kevin Cheng
ALTER TABLE tsmp_client ADD dp_client_secret varchar(128);

-- 20231222, 調整欄位 dp_client_secret 為 dp_client_entry, 資料型態 nvarchar, Kevin Cheng
ALTER TABLE tsmp_client ALTER COLUMN dp_client_secret RENAME TO dp_client_entry;
ALTER TABLE tsmp_client ALTER COLUMN dp_client_entry NVARCHAR(128);

-- 20231225, TSMP外部API註冊資料, 增加欄位, Mini Lee
ALTER TABLE TSMP_API_REG ADD FAIL_DISCOVERY_POLICY VARCHAR(1) DEFAULT '0';
ALTER TABLE TSMP_API_REG ADD FAIL_HANDLE_POLICY VARCHAR(1) DEFAULT '0';

-- 20231225, TSMP API 匯入資料, 增加欄位, Mini Lee
ALTER TABLE TSMP_API_IMP ADD FAIL_DISCOVERY_POLICY VARCHAR(1) DEFAULT '0';
ALTER TABLE TSMP_API_IMP ADD FAIL_HANDLE_POLICY VARCHAR(1) DEFAULT '0';

-- 20231228, 調整欄位 dp_client_secret 長度從 128 到 1000, Kevin Cheng
ALTER TABLE tsmp_client ALTER COLUMN dp_client_entry NVARCHAR(1000);

-- 20240122,TSMP 功能維護資料, 增加欄位  ,Zoe Lee
ALTER TABLE TSMP_FUNC ADD FUNC_TYPE VARCHAR2(1) DEFAULT '1' ;

-- 20240306, 用戶端匯出/入, Tom
CREATE TABLE IF NOT EXISTS DGR_IMPORT_CLIENT_RELATED_TEMP (  
	TEMP_ID 		BIGINT NOT NULL, 		-- ID 
	IMPORT_CLIENT_RELATED 	LONGBLOB NOT NULL, 	-- 匯入的資料
	ANALYZE_CLIENT_RELATED 	LONGBLOB NOT NULL, 	-- 分析的資料
	CREATE_DATE_TIME 	DATETIME DEFAULT CURRENT_TIMESTAMP, -- 建立日期 
	CREATE_USER 		NVARCHAR(1000) DEFAULT 'SYSTEM', -- 建立人員 
	UPDATE_DATE_TIME 	DATETIME, 				-- 更新日期 
	UPDATE_USER 		NVARCHAR(1000), 		-- 更新人員 
	VERSION 			INT DEFAULT 1, 			-- 版號
	PRIMARY KEY (TEMP_ID)  
); 

-- 20240306, 為了controllerMockTest不用每次改TsmpSettingTableInitializer而建立的, Tom
-- 不能寫此行, 這會造成 open source 及 memory 版都預設不檢查 token, Mini
-- INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('CHECK_JTI_ENABLE', 'false', 'When the request includes Authorization, it will check if the jti is expired in the database. If this HTTP header is not included, no check will be performed. By default, the check function is enabled (true), and it will be disabled (false) otherwise.');

-- 20240401, API匯出入沒有PUBLIC_FLAG和API_RELEASE_TIME, Webber Luo
ALTER TABLE TSMP_API_IMP ADD PUBLIC_FLAG VARCHAR2(1) NULL;
ALTER TABLE TSMP_API_IMP ADD API_RELEASE_TIME DATETIME NULL;

INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (178,N'A_SCHEDULE',N'独立排程',N'CALL_API_CUS',N'调用客制化API',122,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (39,N'A_SCHEDULE',N'独立排程',N'CALL_API1',N'调用digiRunner API',120,NULL,N'chb',N'chbapi',NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (40,N'A_SCHEDULE',N'独立排程',N'CALL_API2',N'调用digiRunner 入口网API',121,NULL,N'token_id2',N'token_pwd2',NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (197,N'ACTION_MODULE',N'模组动作',N'delete',N'删除',1468,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (195,N'ACTION_MODULE',N'模组动作',N'restart',N'重新启动',1466,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (194,N'ACTION_MODULE',N'模组动作',N'start',N'启动',1465,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (196,N'ACTION_MODULE',N'模组动作',N'stop',N'停用',1467,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (25,N'API_AUTHORITY',N'API露出权限',N'0',N'对内及对外',80,NULL,N'0',N'1',N'2',N'-1',NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (26,N'API_AUTHORITY',N'API露出权限',N'1',N'对外',81,NULL,N'1',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (90,N'API_AUTHORITY',N'API露出权限',N'-1',N'对内',83,NULL,N'-1',N'2',NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (27,N'API_AUTHORITY',N'API露出权限',N'2',N'对内',82,NULL,N'2',N'-1',NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (243,N'API_CACHE_FLAG',N'Api Cache方式',N'1',N'无',1810,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (244,N'API_CACHE_FLAG',N'Api Cache方式',N'2',N'自适应',1811,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (245,N'API_CACHE_FLAG',N'Api Cache方式',N'3',N'固定',1811,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (112,N'API_DATA_FORMAT',N'API资料格式',N'0',N'SOAP',1144,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (113,N'API_DATA_FORMAT',N'API资料格式',N'1',N'JSON',1145,N'V',NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (118,N'API_DATA_FORMAT',N'API资料格式',N'2',N'XML',1146,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (143,N'API_IMP_CHECK_ACT',N'汇入API检查动作',N'C',N'新增',1196,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (145,N'API_IMP_CHECK_ACT',N'汇入API检查动作',N'N',N'无法操作',1198,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (144,N'API_IMP_CHECK_ACT',N'汇入API检查动作',N'U',N'更新',1197,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (147,N'API_IMP_RESULT',N'汇入API结果',N'F',N'失败',1202,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (148,N'API_IMP_RESULT',N'汇入API结果',N'I',N'初始',1203,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (146,N'API_IMP_RESULT',N'汇入API结果',N'S',N'成功',1201,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (119,N'API_JWT_FLAG',N'API JWT设定',N'0',N'不使用',1148,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (120,N'API_JWT_FLAG',N'API JWT设定',N'1',N'JWE',1149,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (121,N'API_JWT_FLAG',N'API JWT设定',N'2',N'JWS',1150,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (18,N'API_ON_OFF',N'API上下架管理',N'API_OFF',N'API下架',66,NULL,N'OF',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (17,N'API_ON_OFF',N'API上下架管理',N'API_ON',N'API上架',65,NULL,N'ON',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (19,N'API_ON_OFF',N'API上下架管理',N'API_ON_UPDATE',N'API异动',67,NULL,N'UP',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (127,N'API_SRC',N'API来源',N'C',N'组合',1154,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (129,N'API_SRC',N'API来源',N'M',N'Java模组',1156,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (128,N'API_SRC',N'API来源',N'N',N'.NET模组',1155,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (130,N'API_SRC',N'API来源',N'R',N'注册',1157,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (3,N'API_TYPE',N'API类型',N'OFF',N'未上架API清单',20,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (4,N'API_TYPE',N'API类型',N'ON',N'已上架API清单',21,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (246,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'addClient',N'新增用户',1493,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (255,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'addComposerApi',N'新增组合API',1452,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (249,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'addGroup',N'新增群组',1496,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (259,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'addIdPUser',N'新增IdP使用者',1505,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (252,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'addRegisterApi',N'新增注册API',1499,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (203,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'addRole',N'新增角色',1485,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (215,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'addTsmpSetting',N'新增TSMP SETTING',1489,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (200,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'addUser',N'新增使用者',1482,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (247,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'deleteClient',N'删除用户',1494,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (256,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'deleteComposerApi',N'删除组合API',1453,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (250,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'deleteGroup',N'删除群组',1497,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (261,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'deleteIdPUser',N'删除IdP使用者',1507,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (253,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'deleteRegisterApi',N'删除注册API',1450,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (204,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'deleteRole',N'删除角色',1486,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (216,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'deleteTsmpSetting',N'删除TSMP SETTING',1490,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (201,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'deleteUser',N'删除使用者',1483,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (277,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'disableAppClient',N'停用Application Client',1509,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (276,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'enableAppClient',N'启用Application Client',1508,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (218,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'forceLogout',N'强制登出',1492,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (278,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'lockAppClient',N'锁定Application Client',1510,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (198,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'login',N'登入',1480,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (199,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'logout',N'登出',1481,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (248,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'updateClient',N'更新用户',1495,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (257,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'updateComposerApi',N'更新组合API',1454,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (251,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'updateGroup',N'更新群组',1498,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (260,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'updateIdPUser',N'更新IdP使用者',1506,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (254,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'updateRegisterApi',N'更新注册API',1451,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (205,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'updateRole',N'更新角色',1487,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (217,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'updateTsmpSetting',N'更新TSMP SETTING',1491,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (202,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'updateUser',N'更新使用者',1484,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (212,N'AUDIT_LOG_EVENT',N'安全稽核日志事件',N'updateUserProfile',N'更新使用者个人资料',1488,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (124,N'AUTH_CODE_STATUS',N'授权码状态',N'0',N'可用',1151,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (125,N'AUTH_CODE_STATUS',N'授权码状态',N'1',N'已使用',1152,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (126,N'AUTH_CODE_STATUS',N'授权码状态',N'2',N'失效',1153,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (187,N'BROADCAST',N'广播排程',N'RESTART_DGR_MODULE',N'重启dgR模组',1401,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (76,N'CERT_TYPE',N'凭证管理类型',N'JWE',N'JWE加密凭证',600,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (77,N'CERT_TYPE',N'凭证管理类型',N'TLS',N'TLS通讯凭证',601,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (20,N'CHK_LAYER',N'关卡名称',N'0',N'申请者',70,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (21,N'CHK_LAYER',N'关卡名称',N'1',N'经办',71,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (22,N'CHK_LAYER',N'关卡名称',N'2',N'主管',72,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (23,N'CHK_LAYER',N'关卡名称',N'3',N'总经理',73,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (24,N'CHK_LAYER',N'关卡名称',N'4',N' ',74,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (107,N'DB_CACHE_NAME',N'资料库快取分类名称',N'clientgroupapi',N'用户端、群组与API',1132,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (108,N'DB_CACHE_NAME',N'资料库快取分类名称',N'systemothers',N'系统与其他分类',1133,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (106,N'DB_CACHE_NAME',N'资料库快取分类名称',N'userrolfunc',N'使用者、角色与功能',1131,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (262,N'DP_AUTH_FLAG',N'入口网-身份验证状态',N'0',N'是',1825,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (263,N'DP_AUTH_FLAG',N'入口网-身份验证状态',N'1',N'否',1826,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (268,N'DP_DGRK_FLAG',N'入口网-申请DGRK',N'N',N'否',1841,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (267,N'DP_DGRK_FLAG',N'入口网-申请DGRK',N'Y',N'是',1840,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (272,N'DP_DGRK_STATUS',N'入口网-DGRK设定',N'Apply',N'申请',1853,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (269,N'DP_DGRK_STATUS',N'入口网-DGRK设定',N'Disabled',N'停用',1850,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (270,N'DP_DGRK_STATUS',N'入口网-DGRK设定',N'ReApply',N'重新申请',1851,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (271,N'DP_DGRK_STATUS',N'入口网-DGRK设定',N'Rollover',N'展期',1852,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (266,N'DP_PUBLIC_FLAG',N'入口网-货架状态',N'ALL',N'全部',1832,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (265,N'DP_PUBLIC_FLAG',N'入口网-货架状态',N'D',N'下架',1831,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (264,N'DP_PUBLIC_FLAG',N'入口网-货架状态',N'E',N'上架',1830,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (274,N'DP_USER_FLAG',N'入口网-使用者身份',N'A',N'管理者',1861,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (275,N'DP_USER_FLAG',N'入口网-使用者身份',N'ALL',N'全部',1862,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (273,N'DP_USER_FLAG',N'入口网-使用者身份',N'U',N'一般使用者',1860,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (242,N'DPAA_ALERT',N'告警通知',N'LINE',N'LINE告警',1801,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (241,N'DPAA_ALERT',N'告警通知',N'ROLE_EMAIL',N'依角色寄送电子邮件',1800,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (31,N'ENABLE_FLAG',N'停用/启用',N'0',N'停用',101,NULL,N'2',N'N',NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (30,N'ENABLE_FLAG',N'停用/启用',N'1',N'启用',100,NULL,N'1',N'Y',NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (32,N'ENABLE_FLAG',N'停用/启用',N'-1',N'全部',102,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (75,N'ENABLE_FLAG',N'停用/启用',N'2',N'锁定',103,NULL,N'3',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (181,N'ES_INDEX_EXCLUDER',N'索引名称排除条件',N'DPB0126',N'DPB0126',1330,NULL,N'XihcLnxlcnJvcikuKiQ=',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (183,N'ES_INDEX_FLAG',N'ES Index开关',N'CLOSE',N'关闭',1351,NULL,N'0',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (182,N'ES_INDEX_FLAG',N'ES Index开关',N'OPEN',N'启用',1350,NULL,N'1',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (91,N'EVENT_NAME',N'事件名称',N'DC_REFRESH',N'DC更新',183,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (92,N'EVENT_NAME',N'事件名称',N'HOUSEKEEPING',N'仓库封存',184,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (105,N'EVENT_NAME',N'事件名称',N'NODE_TASK_NOTIFIERS',N'调用Tsmp Module SDK 的NodeTaskNotifiers',185,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (52,N'EVENT_NAME',N'事件名称',N'SCHED_REFREH',N'排程刷新',182,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (51,N'EVENT_NAME',N'事件名称',N'SCHED_RUN',N'排程工作生效',181,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (50,N'EVENT_NAME',N'事件名称',N'UPLD_MODULE',N'上传MODULE',180,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (49,N'EVENT_TYPE',N'事件类型',N'ERROR',N'未知的错误',156,NULL,N'0',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (46,N'EVENT_TYPE',N'事件类型',N'INFO',N'显示讯息',153,NULL,N'1',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (44,N'EVENT_TYPE',N'事件类型',N'METHOD_CHECK',N'参数检查结果',151,NULL,N'1',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (48,N'EVENT_TYPE',N'事件类型',N'METHOD_END',N'结束执行工作',155,NULL,N'1',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (43,N'EVENT_TYPE',N'事件类型',N'METHOD_INPUT',N'接收参数',150,NULL,N'1',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (47,N'EVENT_TYPE',N'事件类型',N'METHOD_OUTPUT',N'产生回传值',154,NULL,N'1',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (45,N'EVENT_TYPE',N'事件类型',N'METHOD_START',N'开始执行工作',152,NULL,N'1',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (29,N'FB_FLAG',N'前后台资料',N'BACK',N'后台',91,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (28,N'FB_FLAG',N'前后台资料',N'FRONT',N'前台',90,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (219,N'FILE_CATE_CODE',N'档案分类',N'API',N'API',1660,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (221,N'FILE_CATE_CODE',N'档案分类',N'API_ATTACHMENT',N'API说明文件',1662,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (282,N'FILE_CATE_CODE',N'档案分类',N'API_MODIFY_BATCH',N'API批量修改暂存档',1679,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (220,N'FILE_CATE_CODE',N'档案分类',N'API_TH',N'主题图示',1661,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (223,N'FILE_CATE_CODE',N'档案分类',N'APP_ATTACHMENT',N'实例附件',1664,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (222,N'FILE_CATE_CODE',N'档案分类',N'APP_IMG',N'实例图示',1663,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (229,N'FILE_CATE_CODE',N'档案分类',N'D2_ATTACHMENT',N'审查明细TSMP_DP_REQ_ORDERD2附件',1670,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (224,N'FILE_CATE_CODE',N'档案分类',N'DOC',N'DOC',1665,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (225,N'FILE_CATE_CODE',N'档案分类',N'DOC_API',N'DOC_API',1666,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (226,N'FILE_CATE_CODE',N'档案分类',N'DOC_GUIDELINE',N'API开发标准作业手册',1667,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (235,N'FILE_CATE_CODE',N'档案分类',N'DPB0082',N'暂存档controller unit test',1676,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (227,N'FILE_CATE_CODE',N'档案分类',N'FAQ_ATTACHMENT',N'问答附件',1668,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (233,N'FILE_CATE_CODE',N'档案分类',N'KEY_PAIR',N'公、私钥',1674,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (231,N'FILE_CATE_CODE',N'档案分类',N'MAIL_CONTENT',N'Mail内容档案',1672,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (228,N'FILE_CATE_CODE',N'档案分类',N'MEMBER_APPLY',N'会员申请上传档案',1669,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (236,N'FILE_CATE_CODE',N'档案分类',N'REG_COMP_API',N'注册/组合API的汇出档案',1677,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (234,N'FILE_CATE_CODE',N'档案分类',N'REG_MODULE_DOC',N'注册模组介接规格文件',1675,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (232,N'FILE_CATE_CODE',N'档案分类',N'TEMP',N'暂存档',1673,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (237,N'FILE_CATE_CODE',N'档案分类',N'TSMP_DP_APPT_JOB',N'排程作业相关档案',1678,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (230,N'FILE_CATE_CODE',N'档案分类',N'TSMP_DP_REQ_ORDERM',N'审查单TSMP_DP_REQ_ORDERM附件',1671,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (122,N'HOST_STATUS',N'主机状态',N'A',N'启动',1141,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (123,N'HOST_STATUS',N'主机状态',N'S',N'停止',1142,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (169,N'HOUSEKEEPING',N'资料管理',N'gov_long',N'政府保存年限长期',1284,NULL,N'3650',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (168,N'HOUSEKEEPING',N'资料管理',N'gov_short',N'政府保存年限短期',1283,NULL,N'1825',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (167,N'HOUSEKEEPING',N'资料管理',N'long',N'长期',1282,NULL,N'90',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (166,N'HOUSEKEEPING',N'资料管理',N'mid',N'中期',1281,NULL,N'60',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (165,N'HOUSEKEEPING',N'资料管理',N'short',N'短期',1280,NULL,N'30',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (38,N'JOB_STATUS',N'排程状态',N'A',N'全部',115,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (37,N'JOB_STATUS',N'排程状态',N'C',N'取消',114,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (36,N'JOB_STATUS',N'排程状态',N'D',N'完成',113,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (35,N'JOB_STATUS',N'排程状态',N'E',N'失败',112,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (34,N'JOB_STATUS',N'排程状态',N'R',N'执行中',111,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (33,N'JOB_STATUS',N'排程状态',N'W',N'等待',110,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (162,N'MAIL_TIME_TYPE',N'信件时间类型',N'DAYS',N'天',1273,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (161,N'MAIL_TIME_TYPE',N'信件时间类型',N'HRS',N'小时',1272,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (160,N'MAIL_TIME_TYPE',N'信件时间类型',N'MINS',N'分',1271,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (159,N'MAIL_TIME_TYPE',N'信件时间类型',N'SEC',N'秒',1270,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (1,N'MEMBER_REG_FLAG',N'前台会员注册开关',N'DISABLE',N'停用',10,N'V',N'',N'',N'',N'',N'','2024-04-10 08:36:52.447',N'SYSTEM','2024-04-10 17:27:17.0',N'chad',22,N'前台会员注册开关|停用',N'zh-CN'),
	 (2,N'MEMBER_REG_FLAG',N'前台会员注册开关',N'ENABLE',N'启用',11,NULL,N'',N'',N'',N'',N'','2024-04-10 08:36:52.447',N'SYSTEM','2024-04-10 17:27:17.0',N'chad',23,N'前台会员注册开关|启用',N'zh-CN'),
	 (209,N'MOCK_CONFIG',N'Mock调用Y or 直接调用N',N'DPB0123Udp',N'Udp确认User是否有登入',1311,NULL,N'N',N'https://127.0.0.1:38452/tsmpdpaa/udpssotoken/DPB0123Udp',N'10',NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (7,N'NEWS_TYPE',N'公告类型',N'OFF',N'API下架',32,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (6,N'NEWS_TYPE',N'公告类型',N'ON',N'API上架',31,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (59,N'NEWS_TYPE',N'公告类型',N'SYSTEM',N'系统公告',34,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (58,N'NEWS_TYPE',N'公告类型',N'TSP',N'合作TSP业者',33,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (5,N'NEWS_TYPE',N'公告类型',N'UPDATE',N'API异动',30,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (116,N'NODE_HEALTH',N'节点健康状况',N'danger',N'危险',1127,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (114,N'NODE_HEALTH',N'节点健康状况',N'success',N'良好',1125,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (115,N'NODE_HEALTH',N'节点健康状况',N'warning',N'警示',1126,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (190,N'NODE_STATUS',N'节点状态',N'0',N'停用',1451,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (191,N'NODE_STATUS',N'节点状态',N'1',N'启动',1452,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (189,N'NODE_STATUS',N'节点状态',N'-1',N'全部',1450,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (175,N'NOTICE_EXP_CERT',N'凭证到期通知',N'JWE',N'JWE加密凭证到期通知',1306,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (176,N'NOTICE_EXP_CERT',N'凭证到期通知',N'TLS',N'TLS通讯凭证到期通知',1307,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (100,N'OAK_PARA',N'Open API Key参数',N'A',N'使用次数上限_效期天数',1100,NULL,N'100',N'60',NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (86,N'OPEN_API_KEY',N'Open API Key管理',N'OPEN_API_KEY_APPLICA',N'Open API Key申请',700,NULL,N'KA',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (88,N'OPEN_API_KEY',N'Open API Key管理',N'OPEN_API_KEY_REVOKE',N'Open API Key撤销',702,NULL,N'KR',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (87,N'OPEN_API_KEY',N'Open API Key管理',N'OPEN_API_KEY_UPDATE',N'Open API Key异动',701,NULL,N'KU',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (163,N'OPEN_API_KEY',N'Open API Key管理',N'OVERDUE',N'逾期',703,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (164,N'OPEN_API_KEY',N'Open API Key管理',N'RENEWED',N'已展期',704,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (70,N'ORDERM_QUY_TYPE',N'申请审核单查询类别',N'EXA',N'待审单',141,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (69,N'ORDERM_QUY_TYPE',N'申请审核单查询类别',N'REQ',N'申请单',140,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (71,N'ORDERM_QUY_TYPE',N'申请审核单查询类别',N'REV',N'已审单',142,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (41,N'ORG_FLAG',N'组织原则',N'0',N'本组织向下',130,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (42,N'ORG_FLAG',N'组织原则',N'1',N'全部组织',131,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (279,N'PATHS_COMPATIBILITY',N'路径兼容性',N'0',N'tsmpc',1820,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (280,N'PATHS_COMPATIBILITY',N'路径兼容性',N'1',N'dgrc',1821,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (171,N'REG_MODULE_SRC',N'注册模组建立来源',N'1',N'WSDL',1301,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (172,N'REG_MODULE_SRC',N'注册模组建立来源',N'2',N'OAS 2.0',1302,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (173,N'REG_MODULE_SRC',N'注册模组建立来源',N'3',N'OAS 3.0',1303,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (109,N'REG_SRC',N'API注册来源',N'0',N'自订',1139,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (111,N'REG_SRC',N'API注册来源',N'2',N'OpenAPI Spec',1136,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (133,N'REPORT_LABLE_CODE',N'报表标签代码',N'FREQUENCY',N'次数',1160,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (134,N'REPORT_LABLE_CODE',N'报表标签代码',N'MILLISECOND',N'毫秒',1161,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (132,N'REPORT_LABLE_CODE',N'报表标签代码',N'N',N'失败',1159,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (135,N'REPORT_LABLE_CODE',N'报表标签代码',N'TIME',N'时间',1162,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (131,N'REPORT_LABLE_CODE',N'报表标签代码',N'Y',N'成功',1158,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (153,N'REPORT_NAME',N'报表名称',N'API_AVERAGETIME',N'API平均时间计算分析',1227,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (152,N'REPORT_NAME',N'报表名称',N'API_TIMESANDTIME',N'API次数-时间分析',1226,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (154,N'REPORT_NAME',N'报表名称',N'API_TRAFFIC',N'API流量分析',1228,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (151,N'REPORT_NAME',N'报表名称',N'API_USAGE_STATISTICS',N'API使用次数统计',1225,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (155,N'REPORT_NAME',N'报表名称',N'BADATTEMPT',N'Bad Attempt连线报告',1229,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (149,N'REPORT_TIME_TYPE',N'报表时间类型',N'DAY',N'天',1211,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (258,N'REPORT_TIME_TYPE',N'报表时间类型',N'MINUTE',N'10分',1213,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (150,N'REPORT_TIME_TYPE',N'报表时间类型',N'MONTH',N'月',1212,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (193,N'RESTART_DGR_MODULE',N'重启dgR模组',N' ',N' ',1460,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (157,N'RESULT_FLAG',N'成功/失败',N'0',N'失败',1261,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (156,N'RESULT_FLAG',N'成功/失败',N'1',N'成功',1260,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (158,N'RESULT_FLAG',N'成功/失败',N'-1',N'全部',1262,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (9,N'REVIEW_STATUS',N'签核状态',N'ACCEPT',N'同意',51,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (10,N'REVIEW_STATUS',N'签核状态',N'DENIED',N'不同意',52,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (13,N'REVIEW_STATUS',N'签核状态',N'END',N'结案/终止',55,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (11,N'REVIEW_STATUS',N'签核状态',N'RETURN',N'退回',53,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (8,N'REVIEW_STATUS',N'签核状态',N'WAIT1',N'待审',50,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (12,N'REVIEW_STATUS',N'签核状态',N'WAIT2',N'待审重送',54,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (14,N'REVIEW_TYPE',N'签核类别',N'API_APPLICATION',N'用户API申请',60,NULL,N'AP',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (15,N'REVIEW_TYPE',N'签核类别',N'API_ON_OFF',N'API上下架管理',61,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (16,N'REVIEW_TYPE',N'签核类别',N'CLIENT_REG',N'用户注册',62,NULL,N'CR',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (85,N'REVIEW_TYPE',N'签核类别',N'OPEN_API_KEY',N'Open API Key管理',64,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (81,N'RJOB_STATUS',N'周期排程状态',N'0',N'作废',200,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (82,N'RJOB_STATUS',N'周期排程状态',N'1',N'启动',201,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (83,N'RJOB_STATUS',N'周期排程状态',N'2',N'暂停',202,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (84,N'RJOB_STATUS',N'周期排程状态',N'3',N'执行中',203,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (97,N'RT_MAP_LIST_TYPE',N'角色TXID对应档名单类型',N'B',N'黑名单',1051,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (96,N'RT_MAP_LIST_TYPE',N'角色TXID对应档名单类型',N'W',N'白名单',1050,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (79,N'RTN_CODE_LOCALE',N'回覆代码语言地区',N'en-US',N'英语-美国',1000,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (286,N'RTN_CODE_LOCALE',N'回覆代码语言地区',N'zh-CN',N'中文-简体',1002,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 11:09:19.0',N'SYSTEM',NULL,NULL,1,N'回覆代码语言地区|中文-简体',N'zh-CN'),
	 (80,N'RTN_CODE_LOCALE',N'回覆代码语言地区',N'zh-TW',N'中文-繁体',1001,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (238,N'RUNLOOP',N'循环排程',N'ALERT_KEYWORD',N'Keyword告警',1353,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (239,N'RUNLOOP',N'循环排程',N'ALERT_SYSTEM_BASIC',N'系统效能告警',1354,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (185,N'RUNLOOP',N'循环排程',N'SYS_MONITOR',N'系统监控',1352,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (54,N'SCHED_CATE1',N'排程大分类',N'A_SCHEDULE',N'独立排程',301,NULL,N'11',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (78,N'SCHED_CATE1',N'排程大分类',N'API_APPLICATION',N'用户API申请',304,NULL,N'-1',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (53,N'SCHED_CATE1',N'排程大分类',N'API_ON_OFF',N'API上下架管理',300,NULL,N'5',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (186,N'SCHED_CATE1',N'排程大分类',N'BROADCAST',N'广播排程',314,NULL,N'48',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (55,N'SCHED_CATE1',N'排程大分类',N'CLIENT_REG',N'用户注册',302,NULL,N'-1',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (213,N'SCHED_CATE1',N'排程大分类',N'CREATE_REPORT',N'产生报表',316,NULL,N'-1',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (177,N'SCHED_CATE1',N'排程大分类',N'CUS_INVOKE',N'客制介接调用API',311,NULL,N'-1',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (240,N'SCHED_CATE1',N'排程大分类',N'DPAA_ALERT',N'告警通知',318,NULL,N'56',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (170,N'SCHED_CATE1',N'排程大分类',N'HOUSEKEEPING_BATCH',N'Housekeeping排程',309,NULL,N'-1',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (174,N'SCHED_CATE1',N'排程大分类',N'NOTICE_EXP_CERT',N'凭证到期通知',310,NULL,N'43',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (99,N'SCHED_CATE1',N'排程大分类',N'OAK_CHK_EXPI',N'Open API Key快到期检查',306,NULL,N'-1',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (89,N'SCHED_CATE1',N'排程大分类',N'OPEN_API_KEY',N'Open API Key管理',305,NULL,N'21',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (139,N'SCHED_CATE1',N'排程大分类',N'REPORT_BATCH',N'报表排程',308,NULL,N'-1',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (188,N'SCHED_CATE1',N'排程大分类',N'RESTART_DGR_MODULE',N'重启dgR模组',315,NULL,N'-1',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (184,N'SCHED_CATE1',N'排程大分类',N'RUNLOOP',N'循环排程',313,NULL,N'47',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (214,N'SCHED_CATE1',N'排程大分类',N'SCB_DISABLE_USER',N'停用闲置使用者',317,NULL,N'-1',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (56,N'SCHED_CATE1',N'排程大分类',N'SEND_MAIL',N'寄送信件',303,NULL,N'-1',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (117,N'SCHED_CATE1',N'排程大分类',N'SYNC_DATA1',N'恢复原厂设定',307,NULL,N'-1',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (61,N'SCHED_MSG',N'排程讯息',N'API_PARA',N'已取得调用API的参数档',501,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (62,N'SCHED_MSG',N'排程讯息',N'END_OF_CALL',N'调用结束',502,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (72,N'SCHED_MSG',N'排程讯息',N'MANUAL_CANCEL',N'手动取消',509,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (60,N'SCHED_MSG',N'排程讯息',N'MANUAL_EXEC',N'手动执行',500,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (73,N'SCHED_MSG',N'排程讯息',N'PREP_API_APPLIC',N'准备更新API授权',510,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (65,N'SCHED_MSG',N'排程讯息',N'PREP_API_OFF',N'准备下架API',505,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (64,N'SCHED_MSG',N'排程讯息',N'PREP_API_ON',N'准备上架API',504,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (66,N'SCHED_MSG',N'排程讯息',N'PREP_API_UPDATE',N'准备异动API',506,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (74,N'SCHED_MSG',N'排程讯息',N'PREP_CLIENT_REG',N'准备更新用户注册状态',511,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (93,N'SCHED_MSG',N'排程讯息',N'PREP_OAK_APPLICA',N'准备建立Open API Key',512,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (98,N'SCHED_MSG',N'排程讯息',N'PREP_OAK_CHK_EXPI',N'准备检查快到期Open API Key',515,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (95,N'SCHED_MSG',N'排程讯息',N'PREP_OAK_REVOKE',N'准备撤销Open API Key',514,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (94,N'SCHED_MSG',N'排程讯息',N'PREP_OAK_UPDATE',N'准备异动Open API Key',513,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (68,N'SCHED_MSG',N'排程讯息',N'PREP_SEND',N'准备寄出通知信',508,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (63,N'SCHED_MSG',N'排程讯息',N'SUCCESS',N'成功',503,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (67,N'SCHED_MSG',N'排程讯息',N'WAIT_SEND',N'等待寄出通知信',507,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (57,N'SEND_MAIL',N'寄送信件',N'SEND',N'寄送',190,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (206,N'TABLE_ACT',N'资料表动作',N'C',N'新增',1600,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (208,N'TABLE_ACT',N'资料表动作',N'D',N'删除',1602,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (207,N'TABLE_ACT',N'资料表动作',N'U',N'更新',1601,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (104,N'TIME_UNIT',N'时间单位',N'd',N'天',1108,NULL,N'24',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (103,N'TIME_UNIT',N'时间单位',N'H',N'小时',1107,NULL,N'60',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (102,N'TIME_UNIT',N'时间单位',N'm',N'分钟',1106,NULL,N'60',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (101,N'TIME_UNIT',N'时间单位',N's',N'秒',1105,N'V',N'1',NULL,NULL,NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
INSERT INTO TSMP_dp_items (ITEM_ID,ITEM_NO,ITEM_NAME,SUBITEM_NO,SUBITEM_NAME,SORT_BY,IS_DEFAULT,PARAM1,PARAM2,PARAM3,PARAM4,PARAM5,CREATE_DATE_TIME,CREATE_USER,UPDATE_DATE_TIME,UPDATE_USER,VERSION,KEYWORD_SEARCH,LOCALE) VALUES
	 (210,N'UDPSSO',N'UDPSSO环境',N'ENV1',N'预设工作区',1610,NULL,N'N',N'https://127.0.0.1:38452/tsmpdpaa/udpssotoken/DPB0123Udp',N'10',NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN'),
	 (211,N'UDPSSO',N'UDPSSO环境',N'ENV2',N'渣打工作区',1611,NULL,N'N',N'https://127.0.0.1:38452/tsmpdpaa/udpssotoken/DPB0123Udp',N'10',NULL,NULL,'2024-04-10 08:36:52.447',N'SYSTEM',NULL,NULL,1,NULL,N'zh-CN');
-- 20240429 , dgr_web_socket_mapping 新增欄位 ,Zoe Lee
ALTER TABLE dgr_web_socket_mapping ADD auth varchar(1) DEFAULT 'N' NOT NULL; 

-- 20240422, 添加兩個欄位用於預定DP上下架功能, Kevin Cheng
ALTER TABLE TSMP_API ADD COLUMN SCHEDULED_LAUNCH_DATE BIGINT DEFAULT 0;
ALTER TABLE TSMP_API ADD COLUMN SCHEDULED_REMOVAL_DATE BIGINT DEFAULT 0;

-- 20240507,
INSERT INTO TSMP_SETTING (ID, VALUE, MEMO) VALUES ('DEFAULT_DATA_CHANGE_ENABLED', 'false', 'System default data modification must be enabled (true/false). The following features will be affected:
1. User maintenance: Prevent deletion for (manager, DpUser).
2. Role maintenance: Prevent deletion for (ADMIN).
3. Role list configuration: Prevent deletion for (Administrator).
4. Client maintenance: Prevent deletion for (adminConsole, DpClient).
5. Group maintenance: Prevent deletion for (SMS(Admin Console)).');

-- 20240516, 添加兩個欄位用於預定DGR API啟用停用功能, Kevin Cheng
ALTER TABLE TSMP_API ADD COLUMN ENABLE_SCHEDULED_DATE BIGINT DEFAULT 0;
ALTER TABLE TSMP_API ADD COLUMN DISABLE_SCHEDULED_DATE BIGINT DEFAULT 0;

-- 20240603, TSMP_API_IMP API匯入匯出添加四個欄位,兩個預定DP上下架功能, 兩個預定DGR API啟用停用功能, Webber Luo
ALTER TABLE TSMP_API_IMP ADD COLUMN SCHEDULED_LAUNCH_DATE BIGINT DEFAULT 0;
ALTER TABLE TSMP_API_IMP ADD COLUMN SCHEDULED_REMOVAL_DATE BIGINT DEFAULT 0;
ALTER TABLE TSMP_API_IMP ADD COLUMN ENABLE_SCHEDULED_DATE BIGINT DEFAULT 0;
ALTER TABLE TSMP_API_IMP ADD COLUMN DISABLE_SCHEDULED_DATE BIGINT DEFAULT 0;

-- 20240718 , 第三方 AC IDP INFO , Kevin Cheng
CREATE TABLE IF NOT EXISTS dgr_ac_idp_info_cus (
    ac_idp_info_cus_id     BIGINT          NOT NULL,                -- ID
	ac_idp_info_cus_name   NVARCHAR(200),                           -- 第三方可識別名稱  
    cus_status             VARCHAR(1)      NOT NULL DEFAULT 'Y',    -- Cus 狀態
    cus_login_url          VARCHAR(4000)   NOT NULL,                -- 第三方前端頁面 URL
    cus_backend_login_url  VARCHAR(4000)   NOT NULL,                -- 第三方後端 URL
    cus_user_data_url      VARCHAR(4000)   NOT NULL,                -- 第三方使用者資料 URL
    create_date_time       DATETIME        DEFAULT CURRENT_TIMESTAMP, -- 建立日期
    create_user            NVARCHAR(1000)   DEFAULT 'SYSTEM',        -- 建立人員
    update_date_time       DATETIME,                                -- 更新日期
    update_user            NVARCHAR(1000),                           -- 更新人員
    version                INT             DEFAULT 1,               -- 版號
    PRIMARY KEY (ac_idp_info_cus_id)
);

-- 20240902 , CUS GATE IDP INFO , Kevin Cheng
CREATE TABLE IF NOT EXISTS dgr_gtw_idp_info_cus (
    gtw_idp_info_cus_id    BIGINT          NOT NULL,                -- ID
    client_id              VARCHAR(40)     NOT NULL,                -- digiRunner 的 client_id
    status                 VARCHAR(1)      NOT NULL DEFAULT 'Y',    -- 狀態
    cus_login_url          VARCHAR(4000)   NOT NULL,                -- CUS 登入 URL
    cus_user_data_url      VARCHAR(4000)   NOT NULL,                -- CUS 使用者資料 URL
    icon_file              VARCHAR(4000),                           -- 登入頁圖示檔案
    page_title             NVARCHAR(400),                           -- 登入頁標題
    create_date_time       DATETIME        DEFAULT CURRENT_TIMESTAMP, -- 建立日期
    create_user            NVARCHAR(1000)   DEFAULT 'SYSTEM',        -- 建立人員
    update_date_time       DATETIME,                                -- 更新日期
    update_user            NVARCHAR(1000),                           -- 更新人員
    version                INT             DEFAULT 1,               -- 版號
    PRIMARY KEY (gtw_idp_info_cus_id)
);
-- 20240911 , DGR_GTW_IDP_INFO_A  ADD COLUMN , Zoe Lee
ALTER TABLE DGR_GTW_IDP_INFO_A ADD COLUMN IDT_LIGHT_ID VARCHAR(200);
ALTER TABLE DGR_GTW_IDP_INFO_A ADD COLUMN IDT_ROLE_NAME NVARCHAR(200);
-- 20240911 , DGR_GTW_IDP_AUTH_CODE  ADD COLUMN , Zoe Lee
ALTER TABLE DGR_GTW_IDP_AUTH_CODE ADD COLUMN USER_LIGHT_ID VARCHAR(200);
ALTER TABLE DGR_GTW_IDP_AUTH_CODE ADD COLUMN USER_ROLE_NAME NVARCHAR(200);

-- 20241007, AC IdP授權碼記錄檔, 增加欄位, Mini Lee
Alter table dgr_ac_idp_auth_code add api_resp nvarchar(4000);

-- 20241022 , DGR_BOT_DETECTION , Kevin Cheng
CREATE TABLE IF NOT EXISTS dgr_bot_detection (
    bot_detection_id   BIGINT          NOT NULL,                -- ID
    bot_detection_rule VARCHAR(4000)   NOT NULL,                -- 規則
    type               VARCHAR(1)      NOT NULL DEFAULT 'W',    -- 名單種類
    create_date_time   DATETIME        DEFAULT CURRENT_TIMESTAMP, -- 建立日期
    create_user        NVARCHAR(1000)  DEFAULT 'SYSTEM',        -- 建立人員
    update_date_time   DATETIME,                                -- 更新日期
    update_user        NVARCHAR(1000),                           -- 更新人員
    version            INT             DEFAULT 1,               -- 版號
    PRIMARY KEY (bot_detection_id)
);

-- 20250801 , dgr_mtls_client_cert, Zoe Lee
CREATE TABLE  dgr_mtls_client_cert (
    dgr_mtls_client_cert_id    BIGINT NOT NULL,    -- ID
    host    VARCHAR(255) NOT NULL,    -- 主機
    port    INT NOT NULL,    -- 通訊埠
    root_ca    VARCHAR(4000) NOT NULL,    -- 根憑證
    client_cert    VARCHAR(4000) NOT NULL,    -- 客戶端憑證
    client_key    VARCHAR(4000) NOT NULL,    -- 客戶端金鑰
    key_mima    NVARCHAR(2000),    -- 客戶端憑證密碼
    remark    NVARCHAR(200),    -- 備註
    enable    VARCHAR(1) NOT NULL,    -- 啟用=Y / 停用=N
    root_ca_expire_date    DATETIME NOT NULL,    -- ROOT_CA 的到期時間
    crt_expire_date    DATETIME NOT NULL,    -- CLIENT_CERT 的到期時間
    create_date_time    DATETIME,    -- 建立日期
    create_user    NVARCHAR(1000) DEFAULT 'SYSTEM',    -- 建立人員
    update_date_time    DATETIME,    -- 更新日期
    update_user    NVARCHAR(1000),    -- 更新人員
    version    INT DEFAULT '1',    -- 版號
    PRIMARY KEY  (dgr_mtls_client_cert_id),
	 UNIQUE (host,port)
);

-- 20250120 , TSMP Token 歷史紀錄, Mini Lee
ALTER TABLE TSMP_TOKEN_HISTORY ALTER COLUMN API_RESP TEXT;
-- 20250120 , SSO AC IdP授權碼記錄檔, Mini Lee
ALTER TABLE DGR_AC_IDP_AUTH_CODE ALTER COLUMN API_RESP TEXT;
-- 20250120 , Gateway IdP授權碼記錄檔, Mini Lee
ALTER TABLE DGR_GTW_IDP_AUTH_CODE ALTER COLUMN API_RESP TEXT;

-- 20250203, dashboard相關table建立index(若有資料存在可能要執行一段時間), tom
CREATE INDEX idx_tsmp_req_log ON tsmp_req_log(rtime);
CREATE INDEX idx_dgr_dashboard_es_log ON dgr_dashboard_es_log(rtime);
CREATE INDEX idx_tsmp_req_res_log_history ON tsmp_req_res_log_history(rtime);

-- 20250801 , dgr_ai_provider, Vulcan Shen
CREATE TABLE IF NOT EXISTS dgr_ai_provider (
    ai_provider_id              BIGINT          NOT NULL,                       -- ID
    ai_provider_name            VARCHAR(4000)   NOT NULL,                       -- 供應商名稱
    ai_provider_alias           VARCHAR(4000)   NOT NULL,                       -- 任意名稱
    ai_model                    VARCHAR(4000)   NOT NULL,                       -- LLM 模型名稱
    generate_api                VARCHAR(4000)    NOT NULL,                       -- 此模型生成內容 API URL
    count_token_api             VARCHAR(4000)    NOT NULL,                       -- 此模型計算 TOKEN API URL
    create_date_time            DATETIME        DEFAULT CURRENT_TIMESTAMP,      -- 建立日期
    create_user                 VARCHAR(1000)   DEFAULT 'SYSTEM',               -- 建立人員
    update_date_time            DATETIME,                                       -- 更新日期
    update_user                 VARCHAR(1000),                                  -- 更新人員
    version                     INT             DEFAULT '1',                    -- 版號
    ai_provider_enable          VARCHAR(1)      NOT NULL DEFAULT 'Y',   -- Y = 啟用, N = 停用
    PRIMARY KEY(ai_provider_id)
    );

-- 20250801 , dgr_ai_provider name, model index, Vulcan Shen
CREATE INDEX IF NOT EXISTS index_dgr_ai_provider_ai_provider_name_ai_provider ON dgr_ai_provider (ai_provider_name);
CREATE INDEX IF NOT EXISTS index_dgr_ai_provider_ai_provider_name_ai_model ON dgr_ai_provider (ai_model);

-- 20250801 , dgr_ai_apikey, Vulcan Shen
CREATE TABLE IF NOT EXISTS dgr_ai_apikey (
    ai_apikey_id                BIGINT          NOT NULL,                   -- ID
    ai_apikey_name              VARCHAR(1000)   NOT NULL,                   -- 此 APIKEY 名稱 (任意命名)
    ai_provider_id              BIGINT,                                     -- AI 供應商 ID
    ai_apikey_code              VARCHAR(500)    NOT NULL,                   -- API KEY CODE 內容
    usage_limit_input_token     BIGINT          NOT NULL DEFAULT 0,       -- 此 APIKEY 使用 input token 上限 0 = 不設限"
    usage_limit_output_token    BIGINT          NOT NULL DEFAULT 0,       -- 此 APIKEY 使用 output token 上限 0 = 不設限"
    usage_input_token_count     BIGINT          NOT NULL DEFAULT 0,       -- 此 APIKEY 使用 input token 總數
    usage_output_token_count    BIGINT          NOT NULL DEFAULT 0,       -- 此 APIKEY 使用 output token 總數
    usage_limit_policy          VARCHAR(500)    NOT NULL DEFAULT 'REJECT',  -- 當 input / output 達到上限時的執行策略 REJECT = 拒絕使用 => USE_ANYWAY = 不管限制，強制使用"
    ai_apikey_enable            VARCHAR(1)      NOT NULL DEFAULT 'Y',        -- Y = 啟用, N = 停用
    create_date_time            DATETIME        DEFAULT CURRENT_TIMESTAMP,  -- 建立日期
    create_user                 VARCHAR(1000)   DEFAULT 'SYSTEM',           -- 建立人員
    update_date_time            DATETIME,                                   -- 更新日期
    update_user                 VARCHAR(1000),                              -- 更新人員
    version                     INT             DEFAULT '1',                -- 版號
    PRIMARY KEY(ai_apikey_id)
    );

-- 20250801 , dgr_ai_apikey pk, Vulcan Shen
CREATE INDEX IF NOT EXISTS index_dgr_ai_apikey_ai_provider_id ON dgr_ai_apikey (ai_provider_id);

-- 20250801 , dgr_ai_apikey_usage, Vulcan Shen
CREATE TABLE IF NOT EXISTS dgr_ai_apikey_usage (
    ai_apikey_usage_id      BIGINT NOT NULL,                -- ID
    ai_apikey_consumer_type VARCHAR(1000),                  -- APIKEY 使用者類型 user = 個人使用者, client = client
    ai_apikey_consumer_id   VARCHAR(1000),                  -- APIKEY 使用者 ID
    ai_apikey_id            BIGINT NOT NULL,                -- APIKEY ID
    requst_ts               BIGINT NOT NULL,                -- 使用者請求時間，以毫秒數記錄
    input_token_count       BIGINT NOT NULL DEFAULT 0,    -- 請求內容 input token 數
    output_token_count      BIGINT NOT NULL DEFAULT 0,    -- 請求回傳 output token 數
    ai_prompt_template_id   BIGINT,                -- AI 提示模板 ID
    http_transaction_status VARCHAR(4000),                  -- HTTP 交易狀態
    ai_usage_prompt_input   VARCHAR(4000),                  -- AI 使用者請求內容
    ai_usage_prompt_output  VARCHAR(4000),                  -- AI 使用者請求回傳內容
    create_date_time            DATETIME        DEFAULT CURRENT_TIMESTAMP,  -- 建立日期
    PRIMARY KEY(ai_apikey_usage_id)
    );

-- 20250801 , dgr_ai_apikey_usage pk, Vulcan Shen
CREATE INDEX IF NOT EXISTS index_dgr_ai_apikey_usage_ai_apikey_id ON dgr_ai_apikey_usage (ai_apikey_id);

-- 20250801 , dgr_ai_prompt_template, Vulcan Shen
CREATE TABLE IF NOT EXISTS dgr_ai_prompt_template (
    ai_prompt_template_id       BIGINT          NOT NULL,               -- ID
    ai_prompt_template_name     VARCHAR(4000)   NOT NULL,               -- AI 提示模板名稱
    ai_prompt_template_content  VARCHAR(4000)   NOT NULL,               -- AI 提示模板內容
    ai_prompt_template_enable   VARCHAR(1)      NOT NULL DEFAULT 'Y',   -- Y = 啟用, N = 停用
    ai_prompt_template_remark   VARCHAR(400),                           -- AI 提示模板備註
    PRIMARY KEY(ai_prompt_template_id),
    UNIQUE (ai_prompt_template_name)
    );

-- 20250801 , dgr_ai_prompt_template_binding, Vulcan Shen
CREATE TABLE IF NOT EXISTS dgr_ai_prompt_template_binding (
    ai_consumer_prompt_template_binding_id BIGINT NOT NULL,  -- ID
    ai_prompt_template_id                  BIGINT NOT NULL,  -- AI 提示模板 ID
    ai_apikey_consumer_type                VARCHAR(1000),    -- APIKEY 使用者類型 user / client
    ai_apikey_consumer_id                  VARCHAR(1000),    -- APIKEY 使用者 ID
    PRIMARY KEY(ai_consumer_prompt_template_binding_id)
    );

-- 20250801 , dgr_ai_prompt_template_binding pk, Vulcan Shen
CREATE INDEX IF NOT EXISTS index_dgr_ai_prompt_template_binding_ai_prompt_template_id ON dgr_ai_prompt_template_binding (ai_prompt_template_id);


-- 20250213, 增加欄位長度, Zoe Lee
ALTER TABLE dgr_rdb_connection ALTER COLUMN mima VARCHAR(2000) NOT NULL;

-- 20250227, DGR_WEBHOOK_NOTIFY , Webber Luo
CREATE TABLE IF NOT EXISTS dgr_webhook_notify (
    webhook_notify_id    BIGINT 				NOT NULL,    -- ID
    notify_name    		VARCHAR(100) 			NOT NULL,    -- 通知名稱
    notify_type    		VARCHAR(100) 			NOT NULL,    -- 通知種類
    enable    			VARCHAR(1) 				NOT NULL,    -- 啟用=Y / 停用=N
    message    			VARCHAR(2000),    					 -- 發送內容
    payload_flag    		VARCHAR(1) 				DEFAULT '0', -- 
    create_date_time            DATETIME        DEFAULT CURRENT_TIMESTAMP,  -- 建立日期
    create_user                 VARCHAR(1000)   DEFAULT 'SYSTEM',           -- 建立人員
    update_date_time            DATETIME,                                   -- 更新日期
    update_user                 VARCHAR(1000),                              -- 更新人員
    version                     INT             DEFAULT '1',                -- 版號
    PRIMARY KEY(webhook_notify_id)
);
-- 20250227, DGR_WEBHOOK_NOTIFY_FIELD , Webber Luo
CREATE TABLE IF NOT EXISTS dgr_webhook_notify_field (
    webhook_notify_field_id    	BIGINT 			NOT NULL,    -- ID
    webhook_notify_id    		BIGINT 			NOT NULL,    -- 通知ID
    field_key    				VARCHAR(100) 	NOT NULL,    -- KEY
    field_value    				VARCHAR(2000) 	NOT NULL,    -- VALUE
    field_type    				VARCHAR(1) 		NOT NULL DEFAULT '0',    -- TYPE
    mapping_url    				VARCHAR(2000), 	    		 -- URL
    create_date_time            DATETIME        DEFAULT CURRENT_TIMESTAMP,  -- 建立日期
    create_user                 VARCHAR(1000)   DEFAULT 'SYSTEM',           -- 建立人員
    update_date_time            DATETIME,                                   -- 更新日期
    update_user                 VARCHAR(1000),                              -- 更新人員
    version                     INT             DEFAULT '1',                -- 版號
    PRIMARY KEY(webhook_notify_field_id)
);
-- 20250227, DGR_WEBHOOK_NOTIFY_LOG , Webber Luo
CREATE TABLE IF NOT EXISTS DGR_WEBHOOK_NOTIFY_LOG (
    webhook_notify_log_id    	BIGINT 			NOT NULL,    -- ID
    webhook_notify_id    		BIGINT 			NOT NULL,    -- WEBHOOK_NOTIFY_ID
    client_id    				VARCHAR(40) 	NOT NULL,    -- 用戶端
    content    					VARCHAR(2000),    			 -- 發送內容
    remark    					VARCHAR(2000),    		 	 -- 備註
    create_date_time            DATETIME        DEFAULT CURRENT_TIMESTAMP,  -- 建立日期
    create_user                 VARCHAR(1000)   DEFAULT 'SYSTEM',           -- 建立人員
    update_date_time            DATETIME,                                   -- 更新日期
    update_user                 VARCHAR(1000),                              -- 更新人員
    version                     INT             DEFAULT '1',                -- 版號
    PRIMARY KEY(webhook_notify_log_id)
);

-- 20250227, DGR_WEBHOOK_API_MAP , Webber Luo
CREATE TABLE IF NOT EXISTS dgr_webhook_api_map (
    webhook_api_map_id    		BIGINT 			NOT NULL,    -- ID
    api_key    					VARCHAR(255) 	NOT NULL,	 -- API代碼
    module_name    				VARCHAR(150) 	NOT NULL,  	 -- API_MODULE
    webhook_notify_id    		BIGINT 			NOT NULL,    -- WEBHOOK_NOTIFY_I
    create_date_time            DATETIME        DEFAULT CURRENT_TIMESTAMP,  -- 建立日期
    create_user                 VARCHAR(1000)   DEFAULT 'SYSTEM',           -- 建立人員
    update_date_time            DATETIME,                                   -- 更新日期
    update_user                 VARCHAR(1000),                              -- 更新人員
    version                     INT             DEFAULT '1',                -- 版號
    PRIMARY KEY(webhook_api_map_id)
);

-- 20250331, DGR_GRPCPROXY_MAP, Alvin Chiu
CREATE TABLE IF NOT EXISTS dgr_grpcproxy_map (
    grpcproxy_map_id       BIGINT NOT NULL,                           -- ID
    service_name           VARCHAR(255) NOT NULL,                     -- 目標服務名稱
    proxy_hostname         VARCHAR(255) NOT NULL,                     -- 轉發位置
    target_hostname        VARCHAR(255) NOT NULL,                     -- 目標服務位置
    target_port            INT NOT NULL,                              -- 目標服務PORT
    connect_timeout_ms     INT NOT NULL    DEFAULT 5000,              -- 連線超時毫秒
    send_timeout_ms        INT NOT NULL    DEFAULT 10000,             -- 發送超時毫秒
    read_timeout_ms        INT NOT NULL    DEFAULT 30000,             -- 讀取超時毫秒
    secure_mode            VARCHAR(10)     DEFAULT 'AUTO',            -- TLS安全模式 (AUTO, SECURE, PLAINTEXT)
    server_cert_content    TEXT,                                      -- X509憑證內容 (PEM格式)
    server_key_content     TEXT,                                      -- 私鑰內容 (PEM格式)
    auto_trust_upstream_certs VARCHAR(1)   DEFAULT 'N',             -- 是否自動信任上游憑證
    trusted_certs_content  TEXT,                                      -- 受信任的CA憑證內容 (PEM格式)
    enable                 VARCHAR(1)      DEFAULT 'N',               -- 啟用/停用
    create_date_time       DATETIME        DEFAULT CURRENT_TIMESTAMP, -- 建立日期
    create_user            NVARCHAR(1000)  DEFAULT 'SYSTEM',          -- 建立人員
    update_date_time       DATETIME,                                  -- 更新日期
    update_user            NVARCHAR(1000),                            -- 更新人員
    version                INT             DEFAULT 1,                 -- 版號
    PRIMARY KEY(grpcproxy_map_id)
    );
    
-- 20250801 , add column , Zoe Lee
 ALTER TABLE TSMP_API_IMP ADD COLUMN NOTIFY_NAME_LIST VARCHAR(2000);

-- 20250819, TSMP外部API註冊資料, 增加欄位 ,Mini Lee
ALTER TABLE TSMP_API_REG ADD IS_CORS_ALLOW_ORIGIN VARCHAR2(1) DEFAULT 'N' NULL;
ALTER TABLE TSMP_API_REG ADD IS_CORS_ALLOW_METHODS VARCHAR2(1) DEFAULT 'N' NULL;
ALTER TABLE TSMP_API_REG ADD IS_CORS_ALLOW_HEADERS VARCHAR2(1) DEFAULT 'N' NULL;
ALTER TABLE TSMP_API_REG ADD CORS_ALLOW_ORIGIN varchar(1000) NULL;
ALTER TABLE TSMP_API_REG ADD CORS_ALLOW_METHODS varchar(200) NULL;
ALTER TABLE TSMP_API_REG ADD CORS_ALLOW_HEADERS varchar(1000) NULL;

-- 20250819, TSMP API 匯入資料, 增加欄位 ,Mini Lee
ALTER TABLE TSMP_API_IMP ADD IS_CORS_ALLOW_ORIGIN VARCHAR2(1) DEFAULT 'N' NULL;
ALTER TABLE TSMP_API_IMP ADD IS_CORS_ALLOW_METHODS VARCHAR2(1) DEFAULT 'N' NULL;
ALTER TABLE TSMP_API_IMP ADD IS_CORS_ALLOW_HEADERS VARCHAR2(1) DEFAULT 'N' NULL;
ALTER TABLE TSMP_API_IMP ADD CORS_ALLOW_ORIGIN varchar(1000) NULL;
ALTER TABLE TSMP_API_IMP ADD CORS_ALLOW_METHODS varchar(200) NULL;
ALTER TABLE TSMP_API_IMP ADD CORS_ALLOW_HEADERS varchar(1000) NULL;

-- 20250825, dgr_otp , Tom
CREATE TABLE IF NOT EXISTS dgr_otp (
    otp_id    	           BIGINT 			NOT NULL,    -- ID
    email    			   VARCHAR(100) 	NOT NULL,	 -- email
    otp_code    		   VARCHAR(10) 	    NOT NULL,  	 -- 驗證碼
    expire_key    		   VARCHAR(400) 	NOT NULL,  	 -- 到期key
    error_limit            int NOT NULL DEFAULT 0,       -- 錯誤上限
    CONSTRAINT pk_dgr_otp PRIMARY KEY (otp_id),
    CONSTRAINT uk_dgr_otp UNIQUE (email)
 );

-- 20251003, v4, 加大欄位長度, Mini Lee
ALTER TABLE TSMP_TOKEN_HISTORY ALTER COLUMN USER_NAME NVARCHAR(400);

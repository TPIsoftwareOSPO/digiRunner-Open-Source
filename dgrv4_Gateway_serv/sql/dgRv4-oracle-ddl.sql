CREATE SEQUENCE SEQ_TOKEN_HISTORY_HOUSING_PK

INCREMENT BY 1

START WITH 2000000000

NOCYCLE;
CREATE SEQUENCE SEQ_TOKEN_USAGE_HISTORY_PK

INCREMENT BY 1

START WITH 2000000000

NOCYCLE;
CREATE SEQUENCE SEQ_TSMP_ALERT_LOG_PK

INCREMENT BY 2000000000

START WITH 1

NOCYCLE;
CREATE SEQUENCE SEQ_TSMP_ALERT_PK

INCREMENT BY 1

START WITH 2000000000

NOCYCLE;
CREATE SEQUENCE SEQ_TSMP_API_DETAIL_PK

INCREMENT BY 1

START WITH 2000000000

NOCYCLE;
CREATE SEQUENCE SEQ_TSMP_API_MODULE_PK
INCREMENT BY 1
START WITH 2000000000
NOCYCLE;
CREATE SEQUENCE SEQ_TSMP_CLIENT_HOST_PK

INCREMENT BY 1

START WITH 2000000000

NOCYCLE;
CREATE SEQUENCE SEQ_TSMP_DC_PK 
INCREMENT BY 1 
START WITH 2000000000
NOCYCLE;
CREATE SEQUENCE SEQ_TSMP_EVENTS_PK

INCREMENT BY 1

START WITH 2000000000

NOCYCLE;
CREATE SEQUENCE SEQ_TSMP_GROUP_PK

INCREMENT BY 1

START WITH 2000000000

NOCYCLE;
CREATE SEQUENCE SEQ_TSMP_GROUP_TIMES_LOG_PK

INCREMENT BY 1

START WITH 2000000000

NOCYCLE;
CREATE SEQUENCE SEQ_TSMP_NODE_PK

INCREMENT BY 1

START WITH 2000000000

NOCYCLE;
CREATE SEQUENCE SEQ_TSMP_NODE_TASK_PK

INCREMENT BY 1

START WITH 2000000000

NOCYCLE;
CREATE SEQUENCE SEQ_TSMP_NODE_TASK_WORK_PK

INCREMENT BY 1

START WITH 2000000000

NOCYCLE;
CREATE SEQUENCE SEQ_TSMP_ORGANIZATION_PK

INCREMENT BY 1

START WITH 2000000000

NOCYCLE;
CREATE SEQUENCE SEQ_TSMP_ROLE_PK

INCREMENT BY 1

START WITH 2000000000

NOCYCLE;
CREATE SEQUENCE SEQ_TSMP_TOKEN_HISTORY_PK

INCREMENT BY 1

START WITH 2000000000

NOCYCLE;
CREATE SEQUENCE SEQ_TSMP_USER_PK

INCREMENT BY 1

START WITH 2000000000

NOCYCLE;
CREATE SEQUENCE SEQ_TSMP_VGROUP_PK
INCREMENT BY 1
START WITH 2000000000
NOCYCLE;
CREATE TABLE AUTHORITIES 
(
  USERNAME    NVARCHAR2(400) NOT NULL,
  AUTHORITY   NVARCHAR2(50) NOT NULL,
  CONSTRAINT IX_AUTH_USERNAME UNIQUE (USERNAME,AUTHORITY)
);
CREATE TABLE cus_setting
(
   cus_setting_id    NUMBER(19)          NOT NULL,
   setting_no        VARCHAR2(20)    NOT NULL,
   setting_name      VARCHAR2(100)   NOT NULL,
   subsetting_no     VARCHAR2(20)    NOT NULL,
   subsetting_name   VARCHAR2(100)   NOT NULL,
   sort_by           INT DEFAULT 0 NOT NULL,
   is_default        CHAR(1),
   param1            VARCHAR2(255),
   param2            VARCHAR2(255),
   param3            VARCHAR2(255),
   param4            VARCHAR2(255),
   param5            VARCHAR2(255),
   create_date_time  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
   create_user       VARCHAR2(255)   DEFAULT 'SYSTEM',
   update_date_time  TIMESTAMP,
   update_user       VARCHAR2(255),
   version           INT          DEFAULT 1,
   keyword_search    NVARCHAR2(200),
   CONSTRAINT pk_cus_setting PRIMARY KEY (setting_no, subsetting_no)
);
CREATE TABLE dgr_ac_idp_auth_code
(
   ac_idp_auth_code_id  NUMBER(19)         NOT NULL,
   auth_code            varchar(50)     NOT NULL,
   expire_date_time     NUMBER(19)         NOT NULL,
   status               varchar(1)      DEFAULT ('0') NOT NULL,
   idp_type             varchar(50),        
   user_name            nvarchar2(400)   NOT NULL,
   create_date_time     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
   create_user          varchar(255)    DEFAULT ('SYSTEM'),
   update_date_time     TIMESTAMP,
   update_user          varchar(255),
   version              int             DEFAULT ((1)),
           CONSTRAINT dgr_ac_idp_auth_code_PK PRIMARY KEY (ac_idp_auth_code_id),
        CONSTRAINT dgr_ac_idp_auth_code_UQ UNIQUE (auth_code)
);
CREATE TABLE dgr_ac_idp_info

  (

     ac_idp_info_id   NUMBER(19) NOT NULL,

     idp_type         VARCHAR(50) NOT NULL,

     client_id        NVARCHAR2(400) NOT NULL,

     client_mima      VARCHAR(200) NOT NULL,

     client_name      VARCHAR(200),

     client_status    VARCHAR(1) DEFAULT ('Y') NOT NULL,

     well_known_url   VARCHAR(4000) NOT NULL,

     callback_url     VARCHAR(400) NOT NULL,

     auth_url         VARCHAR(4000),

     access_token_url VARCHAR(4000),

     scope            VARCHAR(4000),

     create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

     create_user      VARCHAR(255) DEFAULT ('SYSTEM'),

     update_date_time TIMESTAMP,

     update_user      VARCHAR(255),

     version          INT DEFAULT ((1)),

     CONSTRAINT dgr_ac_idp_info_pk PRIMARY KEY (ac_idp_info_id),

     CONSTRAINT dgr_ac_idp_info_uq UNIQUE (idp_type, client_id)

  ); 
CREATE TABLE dgr_ac_idp_info_ldap
(
   ac_idp_info_ldap_id   NUMBER(19)      NOT NULL,
   ldap_url              varchar(4000)   NOT NULL,
   ldap_dn               varchar(4000)   NOT NULL,
   ldap_timeout          int             NOT NULL,
   ldap_status           varchar(1)      DEFAULT ('Y') NOT NULL,
   approval_result_mail  varchar(4000)   NOT NULL,
   icon_file             varchar(4000),
   create_date_time      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
   create_user           varchar(255)    DEFAULT ('SYSTEM'),
   update_date_time      TIMESTAMP,
   update_user           varchar(255),
   version               int             DEFAULT ((1)),
   page_title            varchar(400),
   ldap_base_dn          varchar(4000),
   CONSTRAINT dgr_ac_idp_info_ldap_PK PRIMARY KEY (ac_idp_info_ldap_id)
);
CREATE TABLE dgr_ac_idp_info_mldap_d
(
   ac_idp_info_mldap_d_id      NUMBER(19)           NOT NULL,
   ref_ac_idp_info_mldap_m_id  NUMBER(19)          NOT NULL,
   order_no                    int             NOT NULL,
   ldap_url                    varchar(4000)   NOT NULL,
   ldap_dn                     varchar(4000)   NOT NULL,
   ldap_base_dn                varchar(4000)   NOT NULL,
   create_date_time            TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
   create_user                 varchar(255)    DEFAULT ('SYSTEM'),
   update_date_time            TIMESTAMP,
   update_user                 varchar(255),
   version                     int             DEFAULT ((1)),
           CONSTRAINT dgr_ac_idp_info_mldap_d_PK PRIMARY KEY (ac_idp_info_mldap_d_id)
);
CREATE TABLE dgr_ac_idp_info_mldap_m (
  ac_idp_info_mldap_m_id NUMBER(19) NOT NULL, 
  ldap_timeout int NOT NULL, 
  status varchar(1) DEFAULT ('Y') NOT NULL, 
  policy varchar(1) DEFAULT ('S') NOT NULL, 
  approval_result_mail varchar(4000) NOT NULL, 
  icon_file varchar(4000), 
  page_title varchar(400) NOT NULL, 
  create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, 
  create_user varchar(255) DEFAULT ('SYSTEM'), 
  update_date_time TIMESTAMP, 
  update_user varchar(255), 
  version int DEFAULT (
    (1)
  ), 
  CONSTRAINT dgr_ac_idp_info_mldap_m_PK PRIMARY KEY (ac_idp_info_mldap_m_id)
);
CREATE TABLE dgr_ac_idp_user
  (
     ac_idp_user_id       NUMBER(19) NOT NULL,
     user_name            NVARCHAR2(400) NOT NULL,
     user_alias           VARCHAR(200),
     user_status          VARCHAR(1) DEFAULT ('1') NOT NULL,
     user_email           VARCHAR(500),
     org_id               VARCHAR(200),
     idp_type             VARCHAR(50) NOT NULL,
     code1                NUMBER(19),
     code2                NUMBER(19),
     id_token_jwtstr      VARCHAR(4000),
     access_token_jwtstr  VARCHAR(4000),
     refresh_token_jwtstr VARCHAR(4000),
     create_date_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     create_user          VARCHAR(255) DEFAULT ('SYSTEM'),
     update_date_time     TIMESTAMP,
     update_user          VARCHAR(255),
     version              INT DEFAULT ((1)),
     CONSTRAINT dgr_ac_idp_user_pk PRIMARY KEY (ac_idp_user_id),
     CONSTRAINT dgr_ac_idp_user_uq UNIQUE (user_name, idp_type)
  ); 
CREATE TABLE DGR_AUDIT_LOGM
(
   AUDIT_LONG_ID     NUMBER(19)      NOT NULL,
   AUDIT_EXT_ID      NUMBER(19)      DEFAULT ((0)) NOT NULL,
   TXN_UID           varchar(50)     NOT NULL,
   USER_NAME         varchar(50)     NOT NULL,
   CLIENT_ID         varchar(50)     NOT NULL,
   API_URL           varchar(500),
   ORIG_API_URL      varchar(500),
   EVENT_NO          varchar(50)     NOT NULL,
   USER_IP           varchar(200),
   USER_HOSTNAME     varchar(200),
   USER_ROLE         varchar(4000),
   PARAM1            varchar(4000),
   PARAM2            varchar(4000),
   PARAM3            varchar(4000),
   PARAM4            varchar(4000),
   PARAM5            varchar(4000),
   STACK_TRACE       varchar(4000),
   CREATE_DATE_TIME  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER       varchar(255)    DEFAULT ('SYSTEM'),
   UPDATE_DATE_TIME  TIMESTAMP,
   UPDATE_USER       varchar(255),
   VERSION           INT DEFAULT 1,
   PRIMARY KEY (AUDIT_LONG_ID, AUDIT_EXT_ID),
   UNIQUE (TXN_UID)
);
CREATE TABLE DGR_AUDIT_LOGD
(
   AUDIT_LONG_ID     NUMBER(19)       NOT NULL,
   TXN_UID           varchar(50)      NOT NULL,
   ENTITY_NAME       varchar(50)      NOT NULL,
   CUD               varchar(50)      NOT NULL,
   OLD_ROW           BLOB,
   NEW_ROW           BLOB,
   PARAM1            varchar(4000),
   PARAM2            varchar(4000),
   PARAM3            varchar(4000),
   PARAM4            varchar(4000),
   PARAM5            varchar(4000),
   STACK_TRACE       varchar(4000),
   CREATE_DATE_TIME  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER       varchar(255)     DEFAULT ('SYSTEM'),
   UPDATE_DATE_TIME  TIMESTAMP,
   UPDATE_USER       varchar(255),
   VERSION           INT DEFAULT 1,
   PRIMARY KEY (AUDIT_LONG_ID)
);
CREATE TABLE dgr_composer_flow

  (

     flow_id          NUMBER(19) NOT NULL,

     module_name      NVARCHAR2(150) NOT NULL,

     api_id           NVARCHAR2(255) NOT NULL,

     flow_data        BLOB,

     create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

     update_date_time TIMESTAMP,

     version          INT DEFAULT ((1)),

     CONSTRAINT dgr_composer_flow_pk PRIMARY KEY (flow_id),

     CONSTRAINT dgr_composer_flow_uq UNIQUE (module_name, api_id)

  );
CREATE TABLE dgr_dashboard_es_log
(
   id                varchar(63)    NOT NULL,
   rtime             TIMESTAMP      NOT NULL,
   module_name       varchar(255)   NOT NULL,
   orgid             varchar(255),
   txid              varchar(255),
   cid               varchar(255),
   exe_status        char(1)        NOT NULL,
   elapse            int            NOT NULL,
   http_status       int            NOT NULL,
   rtime_year_month  varchar(8),
           CONSTRAINT dgr_dashboard_es_log_PK PRIMARY KEY (id)
);
CREATE TABLE dgr_dashboard_last_data
  (
     dashboard_id   NUMBER(19) NOT NULL,
     dashboard_type INT NOT NULL,
     time_type      INT NOT NULL,
     str1           NVARCHAR2(500),
     str2           NVARCHAR2(500),
     str3           NVARCHAR2(500),
     num1           NUMBER(19),
     num2           NUMBER(19),
     num3           NUMBER(19),
     num4           NUMBER(19),
     sort_num       INT DEFAULT ((1)),
     CONSTRAINT dgr_dashboard_last_data_pk PRIMARY KEY (dashboard_id)
  ); 
CREATE TABLE dgr_gtw_idp_auth_code
(
   gtw_idp_auth_code_id  NUMBER(19)       NOT NULL,
   auth_code             varchar(50)      NOT NULL,
   phase                 varchar(10)      NOT NULL,
   status                varchar(1)       DEFAULT ('A') NOT NULL,
   expire_date_time      NUMBER(19)       NOT NULL,
   idp_type              varchar(50)      NOT NULL,
   client_id             varchar(40),
   user_name             varchar(400)     NOT NULL,
   user_alias            NVARCHAR2(400),
   user_email            varchar(500),
   user_picture          varchar(4000),
   id_token_jwtstr       varchar(4000),
   access_token_jwtstr   varchar(4000),
   refresh_token_jwtstr  varchar(4000),
   create_date_time      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   create_user           varchar(255)     DEFAULT ('SYSTEM'),
   update_date_time      TIMESTAMP,
   update_user           varchar(255),
   version               int DEFAULT (1),
   API_RESP              NVARCHAR2(2000),
   PRIMARY KEY (gtw_idp_auth_code_id),
   UNIQUE (auth_code)   
);
 
CREATE TABLE dgr_gtw_idp_auth_d
(
   gtw_idp_auth_d_id      NUMBER(19)     NOT NULL,
   ref_gtw_idp_auth_m_id  NUMBER(19)     NOT NULL,
   scope                  varchar(200)   NOT NULL,
   create_date_time       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   create_user            varchar(255)   DEFAULT ('SYSTEM'),
   update_date_time       TIMESTAMP,
   update_user            varchar(255),
   version                int            DEFAULT ((1)),
   PRIMARY KEY (gtw_idp_auth_d_id)
);
CREATE TABLE dgr_gtw_idp_auth_m
(
   gtw_idp_auth_m_id  NUMBER(19)     NOT NULL,
   state              varchar(40)    NOT NULL,
   idp_type           varchar(50)    NOT NULL,
   client_id          varchar(40)    NOT NULL,
   auth_code          varchar(50),
   create_date_time   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   create_user        varchar(255)   DEFAULT ('SYSTEM'),
   update_date_time   TIMESTAMP,
   update_user        varchar(255),
   version            int            DEFAULT (1),
   PRIMARY KEY (gtw_idp_auth_m_id),
   UNIQUE (state)
);
CREATE TABLE dgr_gtw_idp_info_j

(

   gtw_idp_info_j_id  NUMBER(19)      NOT NULL,

   client_id          varchar(40)     NOT NULL,

   idp_type           varchar(50)     NOT NULL,

   status             varchar(1)      DEFAULT ('Y') NOT NULL,

   remark             varchar(200),

   host               varchar(4000)   NOT NULL,

   port               int             NOT NULL,

   db_schema          varchar(200)    NOT NULL,

   db_user_name       varchar(200)    NOT NULL,

   db_user_mima       varchar(200)    NOT NULL,

   icon_file          varchar(4000),

   page_title         varchar(400)    NOT NULL,

   create_date_time   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

   create_user        varchar(255)    DEFAULT ('SYSTEM'),

   update_date_time   TIMESTAMP,

   update_user        varchar(255),

   version            int             DEFAULT (1),

   PRIMARY KEY (gtw_idp_info_j_id)

);
CREATE TABLE dgr_gtw_idp_info_l
(
   gtw_idp_info_l_id  NUMBER(19)      NOT NULL,
   client_id          varchar(40)     NOT NULL,
   status             varchar(1)      DEFAULT ('Y') NOT NULL,
   remark             varchar(200),
   ldap_url           varchar(4000)   NOT NULL,
   ldap_dn            varchar(4000)   NOT NULL,
   ldap_timeout       int             NOT NULL,
   icon_file          varchar(4000),
   page_title         varchar(400),
   create_date_time   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   create_user        varchar(255)    DEFAULT ('SYSTEM'),
   update_date_time   TIMESTAMP,
   update_user        varchar(255),
   version            int             DEFAULT (1),
   ldap_base_dn       varchar(4000),
   PRIMARY KEY (gtw_idp_info_l_id)
);
CREATE TABLE dgr_gtw_idp_info_o

(

   gtw_idp_info_o_id  NUMBER(19)      NOT NULL,

   client_id          varchar(40)     NOT NULL,

   idp_type           varchar(50)     NOT NULL,

   status             varchar(1)      DEFAULT ('Y') NOT NULL,

   remark             varchar(200),

   idp_client_id      nvarchar2(400)   NOT NULL,

   idp_client_mima    varchar(200)    NOT NULL,

   idp_client_name    varchar(200),

   well_known_url     varchar(4000),

   callback_url       varchar(400)    NOT NULL,

   auth_url           varchar(4000),

   access_token_url   varchar(4000),

   scope              varchar(4000),

   create_date_time   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

   create_user        varchar(255)    DEFAULT ('SYSTEM'),

   update_date_time   TIMESTAMP,

   update_user        varchar(255),

   version            int             DEFAULT (1),

   PRIMARY KEY (gtw_idp_info_o_id)

);
CREATE TABLE DGR_NODE_LOST_CONTACT

(

   LOST_CONTACT_ID   NUMBER(19)      NOT NULL,

   NODE_NAME         nvarchar2(100)   NOT NULL,

   IP                nvarchar2(100)   NOT NULL,

   PORT              int             NOT NULL,

   LOST_TIME         nvarchar2(100)   NOT NULL,

   CREATE_TIMESTAMP  NUMBER(19)      NOT NULL,

   CREATE_DATE_TIME  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

   CREATE_USER       varchar(255)    DEFAULT ('SYSTEM'),

   UPDATE_DATE_TIME  TIMESTAMP,

   UPDATE_USER       varchar(255),

   VERSION           int             DEFAULT (1),

   PRIMARY KEY (LOST_CONTACT_ID)

);
CREATE TABLE DGR_OAUTH_APPROVALS

(

   OAUTH_APPROVALS_ID  NUMBER(19)         NOT NULL,

   USER_NAME           varchar(256),

   CLIENT_ID           varchar(256),

   SCOPE               varchar(256),

   STATUS              varchar(10),

   EXPIRES_AT          TIMESTAMP,

   LAST_MODIFIED_AT    TIMESTAMP,

   CREATE_DATE_TIME    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,

   CREATE_USER         varchar(255)   DEFAULT ('SYSTEM'),

   UPDATE_DATE_TIME    TIMESTAMP,

   UPDATE_USER         varchar(255),

   VERSION             int            DEFAULT ((1)),

           CONSTRAINT DGR_OAUTH_APPROVALS_PK PRIMARY KEY (OAUTH_APPROVALS_ID)

);
CREATE TABLE dgr_web_socket_mapping
(
   ws_mapping_id     NUMBER(19)           NOT NULL,
   site_name         VARCHAR2(50)     NOT NULL,
   target_ws         VARCHAR2(200)    NOT NULL,
   memo              NVARCHAR2(2000),
   create_date_time  DATE             DEFAULT SYSDATE,
   create_user       VARCHAR2(255)    DEFAULT 'SYSTEM',
   update_date_time  DATE,
   update_user       VARCHAR2(255),
   version           INT           DEFAULT 1,
   keyword_search    VARCHAR2(250),
   CONSTRAINT PK_DGR_WEB_SOCKET_MAPPING PRIMARY KEY (ws_mapping_id),
   CONSTRAINT UK_DGR_WEB_SOCKET_MAPPING UNIQUE (site_name)
);
CREATE TABLE DGR_WEBSITE 
(
  DGR_WEBSITE_ID     NUMBER(19) NOT NULL,
  WEBSITE_NAME       VARCHAR2(50) NOT NULL,
  WEBSITE_STATUS     CHAR(1) DEFAULT 'Y' NOT NULL,
  REMARK             VARCHAR2(500),
  CREATE_DATE_TIME   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CREATE_USER        VARCHAR2(255) DEFAULT 'SYSTEM',
  UPDATE_DATE_TIME   TIMESTAMP,
  UPDATE_USER        VARCHAR2(255),
  VERSION            INT DEFAULT 1,
  KEYWORD_SEARCH     NVARCHAR2(600),
  CONSTRAINT PK_DGR_WEBSITE PRIMARY KEY (DGR_WEBSITE_ID)
);
CREATE TABLE DGR_WEBSITE_DETAIL
(
   DGR_WEBSITE_DETAIL_ID  NUMBER(19)           NOT NULL,
   DGR_WEBSITE_ID         NUMBER(19)           NOT NULL,
   PROBABILITY            INT           NOT NULL,
   URL                    VARCHAR2(1000)   NOT NULL,
   CREATE_DATE_TIME       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER            VARCHAR2(255)    DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME       TIMESTAMP,
   UPDATE_USER            VARCHAR2(255),
   VERSION                INT           DEFAULT 1,
   KEYWORD_SEARCH         NVARCHAR2(1500),
   CONSTRAINT PK_DGR_WEBSITE_DETAIL PRIMARY KEY (DGR_WEBSITE_DETAIL_ID)
);
CREATE TABLE dp_api_version (
  dp_api_version_id NUMBER(19) NOT NULL,                  -- ID
  module_name NVARCHAR2(150) NOT NULL,                   -- Module Name
  api_key NVARCHAR2(255) NOT NULL,                       -- API Key
  dp_api_version NVARCHAR2(10) NOT NULL,                 -- API版本號
  start_of_life NUMBER(19) NOT NULL,                     -- API生命週期(起)
  end_of_life NUMBER(19),                                -- API生命週期(迄)
  remark NVARCHAR2(500),                                 -- 備註
  time_zone VARCHAR(200 CHAR) NOT NULL,                 -- 時區
  create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 建立日期
  create_user VARCHAR(255 CHAR) DEFAULT 'SYSTEM',       -- 建立人員
  update_date_time TIMESTAMP,                            -- 更新日期
  update_user VARCHAR(255 CHAR),                        -- 更新人員
  version INT DEFAULT 1,                          -- 版號
  PRIMARY KEY (dp_api_version_id)
);
CREATE TABLE dp_app (
        dp_application_id NUMBER(19) NOT NULL,                     -- ID
        application_name NVARCHAR2(50) NOT NULL,               -- Application名稱
        application_desc NVARCHAR2(500),                       -- Application說明
        client_id VARCHAR(40) NOT NULL,                      -- CLIENT_ID
        open_apikey_id NUMBER(19),                                 -- 
        user_name NVARCHAR2(400) NOT NULL,                         -- 使用者名稱(視IdP類型決定)
        id_token_jwtstr CLOB NOT NULL,                             -- IdP ID Token 的 JWT
        create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,      -- 建立日期
        create_user VARCHAR(255) DEFAULT 'SYSTEM',           -- 建立人員
        update_date_time TIMESTAMP,                                -- 更新日期
        update_user VARCHAR(255),                            -- 更新人員
        version INT DEFAULT 1,                              -- 版號
        keyword_search NVARCHAR2(600) DEFAULT NULL,
        PRIMARY KEY (dp_application_id)
);
CREATE TABLE dp_file (
    dp_file_id NUMBER(19) NOT NULL,                          -- ID
    file_name NVARCHAR2(100) NOT NULL,                       -- 檔案名稱
    module_name NVARCHAR2(150) NOT NULL,                     -- Module Name
    api_key NVARCHAR2(255) NOT NULL,                         -- API Key
    blob_data BLOB NOT NULL,                                 -- 檔案本體
    create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,    -- 建立日期
    create_user VARCHAR(255) DEFAULT 'SYSTEM',         -- 建立人員
    update_date_time TIMESTAMP,                              -- 更新日期
    update_user VARCHAR(255),                          -- 更新人員
    version INT DEFAULT 1,                            -- 版號
    PRIMARY KEY (dp_file_id)
);
CREATE TABLE dp_user (
    dp_user_id NUMBER(19) NOT NULL,                        -- ID
    user_name NVARCHAR2(400) NOT NULL,                     -- 使用者名稱(視IdP類型決定)
    user_alias VARCHAR(200),                         -- 使用者別名
    id_token_jwtstr CLOB NOT NULL,                         -- IdP ID Token 的 JWT
    user_identity VARCHAR(1) DEFAULT 'U' NOT NULL,   -- 使用者身份
    create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 建立日期
    create_user VARCHAR(255) DEFAULT 'SYSTEM',       -- 建立人員
    update_date_time TIMESTAMP,                            -- 更新日期
    update_user VARCHAR(255),                        -- 更新人員
    version NUMBER(10) DEFAULT 1,                          -- 版號
    keyword_search NVARCHAR2(800),
    PRIMARY KEY (dp_user_id),
    UNIQUE (user_name)
);
CREATE TABLE groups (
  id NUMBER(20) NOT NULL,
  group_name NVARCHAR2(50) NOT NULL,
  PRIMARY KEY (id)
);
CREATE TABLE group_authorities (

  group_id NUMBER(20) NOT NULL,
  authority NVARCHAR2(50) NOT NULL,

  CONSTRAINT ix_group_authorities_group UNIQUE (group_id),
  CONSTRAINT GROUP_AUTHORITIES_FK FOREIGN KEY (GROUP_ID)
  REFERENCES GROUPS (ID)
);
CREATE TABLE group_members (
  id NUMBER(19) NOT NULL,
  username VARCHAR(50 CHAR) NOT NULL,
  group_id NUMBER(19) NOT NULL,
  CONSTRAINT GROUP_MEMBERS_PK PRIMARY KEY (ID),
  CONSTRAINT GROUP_MEMBERS_UQ UNIQUE (GROUP_ID),
  CONSTRAINT GROUP_MEMBERS_FK FOREIGN KEY (GROUP_ID)
  REFERENCES GROUPS (ID)
);
-- digirunner.ldap_auth_result definition
CREATE TABLE ldap_auth_result (
  ldap_id NUMBER(20) NOT NULL,
  user_name VARCHAR(50 CHAR) NOT NULL,
  code_challenge VARCHAR(50 CHAR) NOT NULL,
  user_ip VARCHAR(50 CHAR) DEFAULT NULL,
  use_date_time TIMESTAMP DEFAULT NULL,
  create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_user VARCHAR(255 CHAR) DEFAULT 'ldap_system',
  update_date_time TIMESTAMP DEFAULT NULL,
  update_user VARCHAR(255 CHAR) DEFAULT NULL,
  version INT DEFAULT 1,
  PRIMARY KEY (ldap_id)
);
CREATE TABLE oauth_approvals (
  userid VARCHAR(256 CHAR) DEFAULT NULL,
  clientid VARCHAR(256 CHAR) DEFAULT NULL,
  scope VARCHAR(256 CHAR) DEFAULT NULL,
  status VARCHAR(10 CHAR) DEFAULT NULL,
  expiresat TIMESTAMP DEFAULT NULL,
  lastmodifiedat TIMESTAMP DEFAULT NULL
);
CREATE TABLE OAUTH_CLIENT_DETAILS  (
CLIENT_ID NVARCHAR2(255) NOT NULL,
RESOURCE_IDS NVARCHAR2(255) DEFAULT NULL,
CLIENT_SECRET NVARCHAR2(255) DEFAULT NULL,
SCOPE varchar (4000) DEFAULT NULL,
AUTHORIZED_GRANT_TYPES NVARCHAR2(255) DEFAULT NULL,
WEB_SERVER_REDIRECT_URI NVARCHAR2(255) DEFAULT NULL,
AUTHORITIES NVARCHAR2(255) DEFAULT NULL,
ACCESS_TOKEN_VALIDITY INT DEFAULT NULL,
REFRESH_TOKEN_VALIDITY INT DEFAULT NULL,
ADDITIONAL_INFORMATION NVARCHAR2(2000) DEFAULT NULL,
AUTOAPPROVE NVARCHAR2(255) DEFAULT NULL,
 CONSTRAINT OAUTH_CLIENT_DETAILS_PK PRIMARY KEY (CLIENT_ID)
);
CREATE TABLE OAUTH_CODE (
  CODE VARCHAR(256),
  AUTHENTICATION BLOB,
  CREATED TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
-- digirunner.seq_store definition

CREATE TABLE seq_store (

  sequence_name VARCHAR(255 CHAR) NOT NULL,

  next_val NUMBER(20) DEFAULT NULL,

  PRIMARY KEY (sequence_name)

);
-- digirunner.sso_auth_result definition
CREATE TABLE sso_auth_result (
  sso_id NUMBER(20) NOT NULL,
  user_name VARCHAR(50 CHAR) DEFAULT NULL,
  code_challenge VARCHAR(50 CHAR) NOT NULL,
  create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_user VARCHAR(255 CHAR) DEFAULT 'SSO SYSTEM',
  update_date_time TIMESTAMP DEFAULT NULL,
  update_user VARCHAR(255 CHAR) DEFAULT NULL,
  version INT DEFAULT 1,
  use_date_time TIMESTAMP DEFAULT NULL,
  PRIMARY KEY (sso_id)
);
CREATE TABLE  TSMP_ALERT
(
   ALERT_ID INT PRIMARY KEY NOT NULL,
   ALERT_NAME NVARCHAR2(30) NOT NULL,
   ALERT_TYPE VARCHAR2(20) NOT NULL,
   ALERT_ENABLED CHAR(1) NOT NULL,
   THRESHOLD INT,
   DURATION INT,
   ALERT_INTERVAL INT,
   C_FLAG CHAR(1) NOT NULL,
   IM_FLAG CHAR(1) NOT NULL,
   IM_TYPE NVARCHAR2(20),
   IM_ID NVARCHAR2(100),
   EX_TYPE CHAR(1) NOT NULL,
   EX_DAYS NVARCHAR2(100),
   EX_TIME NVARCHAR2(100),
   ALERT_DESC NVARCHAR2(200),
   ALERT_SYS VARCHAR2(20) DEFAULT NULL,
   ALERT_MSG VARCHAR2(300) DEFAULT NULL,
   MODULENAME NVARCHAR2(255),
   RESPONSETIME NVARCHAR2(255),
   ES_SEARCH_PAYLOAD VARCHAR(1024) DEFAULT NULL,
   CREATE_TIME TIMESTAMP,
   UPDATE_TIME TIMESTAMP,
   CREATE_USER NVARCHAR2(30) DEFAULT NULL,
   UPDATE_USER NVARCHAR2(30) DEFAULT NULL
);
CREATE TABLE  TSMP_ALERT_LOG
(
   ALERT_LOG_ID     NUMBER(19) PRIMARY KEY NOT NULL,
   ALERT_ID         INT DEFAULT (-1) NOT NULL,
   ROLE_ID          VARCHAR(500),
   ALERT_MSG        VARCHAR(300) DEFAULT '' NOT NULL,
   SENDER_TYPE      VARCHAR(20) NOT NULL,
   RESULT           CHAR(1) DEFAULT '0' NOT NULL,
   CREATE_DATE_TIME TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER      VARCHAR(255) DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME TIMESTAMP,
   UPDATE_USER      VARCHAR(255),
   VERSION          INT DEFAULT 1
); 
CREATE TABLE TSMP_API

(

   API_KEY           nvarchar2(255)    NOT NULL,

   MODULE_NAME       nvarchar2(150)    NOT NULL,

   API_NAME          nvarchar2(255),

   API_STATUS        char(1)          NOT NULL,

   API_SRC           char(1)          NOT NULL,

   API_DESC          nvarchar2(1500),

   CREATE_TIME       TIMESTAMP         NOT NULL,

   UPDATE_TIME       TIMESTAMP,

   CREATE_USER       varchar(128),

   UPDATE_USER       varchar(128),

   API_OWNER         varchar(100)     DEFAULT (NULL),

   ORG_ID            varchar(255)     DEFAULT (NULL),

   PUBLIC_FLAG       char(1)          DEFAULT (NULL),

   SRC_URL           nvarchar2(2000)   DEFAULT (NULL),

   API_UID           varchar(36),

   DATA_FORMAT       char(1)          DEFAULT (NULL),

   JWE_FLAG          varchar(1)       DEFAULT (NULL),

   JWE_FLAG_RESP     varchar(1),

   API_CACHE_FLAG    char(1)          DEFAULT ('1') NOT NULL,

   MOCK_STATUS_CODE  char(3)          DEFAULT (NULL),

   MOCK_HEADERS      varchar(2000)    DEFAULT (NULL),

   MOCK_BODY         nvarchar2(2000)  DEFAULT (NULL),

   success           NUMBER(19)       DEFAULT ((0)) NOT NULL,

   fail              NUMBER(19)       DEFAULT ((0)) NOT NULL,

   total             NUMBER(19)       DEFAULT ((0)) NOT NULL,

   elapse            NUMBER(19)       DEFAULT ((0)) NOT NULL,

    CONSTRAINT TSMP_API_PK PRIMARY KEY (API_KEY, MODULE_NAME)

);
CREATE TABLE TSMP_API_DETAIL
(
   ID                NUMBER(19)        NOT NULL,
   API_MODULE_ID     NUMBER(19)        NOT NULL,
   API_KEY           nvarchar2(255)    NOT NULL,
   API_NAME          nvarchar2(255)    NOT NULL,
   PATH_OF_JSON      nvarchar2(1024)   NOT NULL,
   METHOD_OF_JSON    nvarchar2(1023)   NOT NULL,
   PARAMS_OF_JSON    nvarchar2(1023)   NOT NULL,
   HEADERS_OF_JSON   nvarchar2(1023)   NOT NULL,
   CONSUMES_OF_JSON  nvarchar2(1023)   NOT NULL,
   PRODUCES_OF_JSON  nvarchar2(1023)   NOT NULL,
   CONSTRAINT TSMP_API_DETAIL_PK PRIMARY KEY (ID),
   CONSTRAINT TSMP_API_DETAIL_UQ UNIQUE (API_MODULE_ID, API_KEY)
);
CREATE TABLE tsmp_api_ext (
  api_key VARCHAR(30) NOT NULL,
  module_name VARCHAR(100) NOT NULL,
  dp_status VARCHAR(1) NOT NULL,
  dp_stu_date_time TIMESTAMP,
  ref_orderm_id NUMBER(19) NOT NULL,
  api_ext_id NUMBER(19) NOT NULL,
  create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_user VARCHAR(255) DEFAULT 'system',
  update_date_time TIMESTAMP,
  update_user VARCHAR(255),
  version INT DEFAULT 1,
  CONSTRAINT pk_tsmp_api_ext PRIMARY KEY (api_key, module_name),
  CONSTRAINT uk_api_ext_id UNIQUE (api_ext_id)
);
CREATE TABLE tsmp_api_imp (
  api_key VARCHAR(255 CHAR) NOT NULL,
  module_name VARCHAR(50 CHAR) NOT NULL,
  record_type CHAR(1 CHAR) NOT NULL,
  batch_no INT NOT NULL,
  filename VARCHAR(100 CHAR) NOT NULL,
  api_name VARCHAR(255 CHAR),
  api_desc VARCHAR(300 CHAR),
  api_owner VARCHAR(100 CHAR),
  url_rid CHAR(1 CHAR) DEFAULT '0',
  api_src CHAR(1 CHAR) DEFAULT 'M',
  src_url VARCHAR(2000 CHAR),
  api_uuid VARCHAR(64 CHAR),
  path_of_json VARCHAR(255 CHAR) NOT NULL,
  method_of_json VARCHAR(50 CHAR) NOT NULL,
  params_of_json VARCHAR(255 CHAR),
  headers_of_json VARCHAR(255 CHAR),
  consumes_of_json VARCHAR(100 CHAR),
  produces_of_json VARCHAR(255 CHAR),
  flow CLOB,
  create_time TIMESTAMP NOT NULL,
  create_user VARCHAR(255 CHAR),
  check_act CHAR(1 CHAR) NOT NULL,
  result CHAR(1 CHAR) NOT NULL,
  memo VARCHAR(255 CHAR),
  no_oauth CHAR(1 CHAR),
  jwe_flag VARCHAR(1 CHAR),
  jwe_flag_resp VARCHAR(1 CHAR),
  fun_flag INT,
  CONSTRAINT pk_tsmp_api_imp PRIMARY KEY (api_key, module_name, record_type, batch_no)
);
CREATE TABLE tsmp_api_module (
  id NUMBER(19) NOT NULL,
  module_name NVARCHAR2(255) NOT NULL,
  module_version NVARCHAR2(255) NOT NULL,
  module_app_class NVARCHAR2(255) NOT NULL,
  module_bytes BLOB NOT NULL,
  module_md5 NVARCHAR2(255) NOT NULL,
  module_type NVARCHAR2(255) NOT NULL,
  upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  uploader_name NVARCHAR2(255) NOT NULL,
  status_time TIMESTAMP,
  status_user NVARCHAR2(255),
  active NUMBER(1) NOT NULL,
  node_task_id NUMBER(19),
  v2_flag CHAR(1),
  org_id VARCHAR(255),
  CONSTRAINT pk_tsmp_api_module PRIMARY KEY (id),
  CONSTRAINT uk_api_module_1 UNIQUE (module_name, module_version)
);
CREATE TABLE TSMP_API_REG (
API_KEY NVARCHAR2(30) NOT NULL,
MODULE_NAME NVARCHAR2(50) NOT NULL,
SRC_URL NVARCHAR2(255) NOT NULL,
REG_STATUS CHAR(1) NOT NULL,
API_UUID NVARCHAR2(64) ,
PATH_OF_JSON NVARCHAR2(255) NULL,
METHOD_OF_JSON NVARCHAR2(200) NOT NULL,
PARAMS_OF_JSON NVARCHAR2(255)  ,
HEADERS_OF_JSON NVARCHAR2(255) ,
CONSUMES_OF_JSON NVARCHAR2(100) ,
PRODUCES_OF_JSON NVARCHAR2(255) ,
CREATE_TIME TIMESTAMP NOT NULL,
CREATE_USER VARCHAR(30) NOT NULL,
UPDATE_TIME TIMESTAMP,
UPDATE_USER VARCHAR(30),
URL_RID     CHAR(1)          DEFAULT 0 NOT NULL,
REGHOST_ID  VARCHAR(10),
NO_OAUTH    CHAR(1),
FUN_FLAG    int              DEFAULT 0,
CONSTRAINT UK_API_REG PRIMARY KEY (API_KEY, MODULE_NAME)
);
CREATE TABLE tsmp_auth_code (
  auth_code_id NUMBER(19) NOT NULL,
  auth_code VARCHAR(1000) NOT NULL,
  expire_date_time NUMBER(19) NOT NULL,
  status VARCHAR(1) DEFAULT '0' NOT NULL,
  auth_type VARCHAR(20),
  client_name VARCHAR(150),
  create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_user VARCHAR(255) DEFAULT 'system',
  update_date_time TIMESTAMP,
  update_user VARCHAR(255),
  version INT DEFAULT 1,
  PRIMARY KEY (auth_code_id),
  CONSTRAINT TSMP_AUTH_CODE_UK UNIQUE (auth_code)
);
CREATE TABLE TSMP_CLIENT
(
   CLIENT_ID            nvarchar2(40)    NOT NULL,
   CLIENT_NAME          nvarchar2(150)   NOT NULL,
   CLIENT_STATUS        nchar(1)        NOT NULL,
   TPS                  int             NOT NULL,
   EMAILS               nvarchar2(500)   DEFAULT NULL,
   CREATE_TIME          TIMESTAMP       NOT NULL,
   UPDATE_TIME          TIMESTAMP       DEFAULT NULL,
   OWNER                nvarchar2(100)   NOT NULL,
   REMARK               nvarchar2(300)   DEFAULT NULL,
   CREATE_USER          varchar(255),
   UPDATE_USER          nvarchar2(30)    DEFAULT NULL,
   API_QUOTA            int             DEFAULT NULL,
   API_USED             int             DEFAULT NULL,
   C_PRIORITY           int             DEFAULT 5,
   PWD_FAIL_TIMES       int             DEFAULT 0,
   CLIENT_ALIAS         varchar(150)    DEFAULT NULL,
   SECURITY_LEVEL_ID    varchar(10)     DEFAULT 'SYSTEM',
   SIGNUP_NUM           varchar(100)    DEFAULT NULL,
   FAIL_TRESHHOLD       int             DEFAULT 3,
   ACCESS_TOKEN_QUOTA   int             DEFAULT 0,
   REFRESH_TOKEN_QUOTA  int             DEFAULT 0,
   CLIENT_SECRET        varchar(128),
   START_DATE           NUMBER(19),
   END_DATE             NUMBER(19),
   START_TIME_PER_DAY   NUMBER(19),
   END_TIME_PER_DAY     NUMBER(19),
   TIME_ZONE            varchar(200),
    CONSTRAINT TSMP_CLIENT_PK PRIMARY KEY (CLIENT_ID)
);
CREATE TABLE tsmp_client_cert (
  client_cert_id NUMBER(19) NOT NULL,
  client_id VARCHAR(40) NOT NULL,
  cert_file_name VARCHAR(255) NOT NULL,
  file_content BLOB NOT NULL,
  pub_key VARCHAR(1024) NOT NULL,
  cert_version VARCHAR(255),
  cert_serial_num VARCHAR(255) NOT NULL,
  s_algorithm_id VARCHAR(255),
  algorithm_id VARCHAR(255) NOT NULL,
  cert_thumbprint VARCHAR(1024) NOT NULL,
  iuid VARCHAR(255),
  issuer_name VARCHAR(255) NOT NULL,
  suid VARCHAR(255),
  create_at NUMBER(19) NOT NULL,
  expired_at NUMBER(19) NOT NULL,
  create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_user VARCHAR(255) DEFAULT 'SYSTEM',
  update_date_time TIMESTAMP,
  update_user VARCHAR(255),
  version INT DEFAULT 1,
  key_size INT DEFAULT 0,
  PRIMARY KEY (client_cert_id)
);
CREATE TABLE tsmp_client_cert2 (
  client_cert2_id NUMBER(19) NOT NULL,
  client_id VARCHAR(40) NOT NULL,
  cert_file_name VARCHAR(255) NOT NULL,
  file_content BLOB NOT NULL,
  pub_key VARCHAR(1024) NOT NULL,
  cert_version VARCHAR(255),
  cert_serial_num VARCHAR(255) NOT NULL,
  s_algorithm_id VARCHAR(255),
  algorithm_id VARCHAR(255) NOT NULL,
  cert_thumbprint VARCHAR(1024) NOT NULL,
  iuid VARCHAR(255),
  issuer_name VARCHAR(255) NOT NULL,
  suid VARCHAR(255),
  create_at NUMBER(19) NOT NULL,
  expired_at NUMBER(19) NOT NULL,
  key_size INT DEFAULT 0,
  create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_user VARCHAR(255) DEFAULT 'SYSTEM',
  update_date_time TIMESTAMP,
  update_user VARCHAR(255),
  version INT DEFAULT 1,
  PRIMARY KEY (client_cert2_id)
);
CREATE TABLE TSMP_CLIENT_GROUP
(
   CLIENT_ID  nvarchar2(40)   NOT NULL,
   GROUP_ID   nvarchar2(10)   NOT NULL,
    CONSTRAINT TSMP_CLIENT_GROUP_PK PRIMARY KEY (CLIENT_ID, GROUP_ID)
);
CREATE TABLE tsmp_client_host (
  host_seq INT NOT NULL,
  client_id NVARCHAR2(40) NOT NULL,
  host_name NVARCHAR2(50) NOT NULL,
  host_ip NVARCHAR2(15) NOT NULL,
  create_time TIMESTAMP NOT NULL,
  PRIMARY KEY (host_seq)
);
CREATE TABLE tsmp_client_log (
  log_seq NVARCHAR2(20) NOT NULL,
  is_login NUMBER(1) NOT NULL,
  agent NVARCHAR2(500) NOT NULL,
  event_type NVARCHAR2(10) NOT NULL,
  event_msg NVARCHAR2(300) NOT NULL,
  event_time TIMESTAMP NOT NULL,
  client_id NVARCHAR2(40) NOT NULL,
  client_ip NVARCHAR2(15) NOT NULL,
  user_name NVARCHAR2(30),
  txsn NVARCHAR2(20) NOT NULL,
  create_time TIMESTAMP NOT NULL,
  PRIMARY KEY (log_seq)
);
CREATE TABLE tsmp_client_vgroup (
  client_id VARCHAR(40) NOT NULL,
  vgroup_id VARCHAR(10) NOT NULL,
  PRIMARY KEY (client_id, vgroup_id)
);
CREATE TABLE TSMP_DC
(
   DC_ID        NUMBER(19)         NOT NULL,
   DC_CODE      VARCHAR2(30)   NOT NULL,
   DC_MEMO      VARCHAR2(300),
   ACTIVE       NUMBER(1),
   CREATE_USER  VARCHAR2(255),
   CREATE_TIME  TIMESTAMP      NOT NULL,
   UPDATE_USER  VARCHAR2(255),
   UPDATE_TIME  TIMESTAMP,
   CONSTRAINT DC_ID_PK PRIMARY KEY (DC_ID)
);
CREATE TABLE TSMP_DC_MODULE
(
   DC_ID         NUMBER(19)   NOT NULL,
   MODULE_ID     NUMBER(19) NOT NULL,
   NODE_TASK_ID  NUMBER(19) ,
   CONSTRAINT TSMP_DC_MODULE_PK PRIMARY KEY (DC_ID, MODULE_ID)
);
CREATE TABLE TSMP_DC_NODE
(
   NODE          VARCHAR2(30)   NOT NULL,
   DC_ID         NUMBER(19)         NOT NULL,
   NODE_TASK_ID  NUMBER(19),
   CONSTRAINT TSMP_DC_NODE_PK PRIMARY KEY (NODE, DC_ID)
);
CREATE TABLE TSMP_DP_ABOUT 
(
  SEQ_ID          NUMBER(19) NOT NULL,
  ABOUT_SUBJECT   NVARCHAR2(100) NOT NULL,
  ABOUT_DESC      NVARCHAR2(2000) NOT NULL,
  CREATE_TIME     TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CREATE_USER     VARCHAR2(255) DEFAULT 'SYSTEM',
  UPDATE_TIME     TIMESTAMP,
  UPDATE_USER     VARCHAR2(255),
  VERSION         INT DEFAULT 1,
  CONSTRAINT TSMP_DP_ABOUT_PK PRIMARY KEY (SEQ_ID)
);
CREATE TABLE TSMP_DP_API_APP
(
   REF_APP_ID        NUMBER(19)         NOT NULL,
   REF_API_UID       VARCHAR2(36)   NOT NULL,
   CREATE_DATE_TIME  TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER       VARCHAR2(255)  DEFAULT 'SYSTEM',
   CONSTRAINT TSMP_DP_API_APP_PK PRIMARY KEY (REF_APP_ID, REF_API_UID)
);
CREATE TABLE TSMP_DP_API_AUTH2
(
   API_AUTH_ID       NUMBER(19)           NOT NULL,
   REF_CLIENT_ID     VARCHAR2(40)     NOT NULL,
   REF_API_UID       VARCHAR2(36)     NOT NULL,
   APPLY_STATUS      VARCHAR2(10)     NOT NULL,
   APPLY_PURPOSE     NVARCHAR2(2000)  NOT NULL,
   REF_REVIEW_USER   NVARCHAR2(255),
   REVIEW_REMARK     VARCHAR2(3000),
   CREATE_DATE_TIME  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER       VARCHAR2(255)   DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME  TIMESTAMP,
   UPDATE_USER       VARCHAR2(255),
   VERSION           INT           DEFAULT 1,
   KEYWORD_SEARCH    NCLOB,
   CONSTRAINT TSMP_DP_API_AUTH2_PK PRIMARY KEY (API_AUTH_ID)
);
CREATE TABLE TSMP_DP_API_THEME
(
   REF_API_THEME_ID  NUMBER(19)         NOT NULL,
   REF_API_UID       VARCHAR2(36)   NOT NULL,
   CREATE_DATE_TIME  TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER       VARCHAR2(255)  DEFAULT 'SYSTEM',
   CONSTRAINT TSMP_DP_API_THEME_PK PRIMARY KEY (REF_API_THEME_ID, REF_API_UID)
);
CREATE TABLE TSMP_DP_API_VIEW_LOG 
(
  SEQ_ID        NUMBER(19) NOT NULL,
  API_ID        VARCHAR2(36) NOT NULL,
  FROM_IP       NVARCHAR2(50),
  VIEW_DATE     DATE NOT NULL,
  CREATE_TIME   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CREATE_USER   VARCHAR2(255) DEFAULT 'SYSTEM',
  CONSTRAINT TSMP_DP_API_VIEW_LOG_PK PRIMARY KEY (SEQ_ID)
);
CREATE TABLE TSMP_DP_APP 
(
  APP_ID            NUMBER(19) NOT NULL,
  REF_APP_CATE_ID   NUMBER(19) NOT NULL,
  NAME              NVARCHAR2(100) NOT NULL,
  INTRO             NVARCHAR2(2000) NOT NULL,
  AUTHOR            NVARCHAR2(100),
  DATA_STATUS       CHAR(1) NOT NULL,
  ORG_ID            VARCHAR2(255),
  CREATE_TIME       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CREATE_USER       VARCHAR2(255) DEFAULT 'SYSTEM',
  UPDATE_TIME       TIMESTAMP,
  UPDATE_USER       VARCHAR2(255),
  VERSION           INT DEFAULT 1,
  KEYWORD_SEARCH    NVARCHAR2(100),
  CONSTRAINT TSMP_DP_APP_PK PRIMARY KEY (APP_ID)
);
CREATE TABLE TSMP_DP_APP_CATEGORY 
(
  APP_CATE_ID      NUMBER(19) NOT NULL,
  APP_CATE_NAME    NVARCHAR2(100) NOT NULL,
  DATA_SORT        INT,
  ORG_ID           VARCHAR2(255),
  CREATE_TIME      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CREATE_USER      VARCHAR2(255) DEFAULT 'SYSTEM',
  UPDATE_TIME      TIMESTAMP,
  UPDATE_USER      VARCHAR2(255),
  VERSION          INT DEFAULT 1,
  KEYWORD_SEARCH   NVARCHAR2(100),
  CONSTRAINT TSMP_DP_APP_CATEGORY_PK PRIMARY KEY (APP_CATE_ID)
);
CREATE TABLE TSMP_DP_APPT_JOB
(
   APPT_JOB_ID      NUMBER(19) PRIMARY KEY NOT NULL,
   REF_ITEM_NO      VARCHAR2(50) NOT NULL,
   REF_SUBITEM_NO   VARCHAR2(100),
   STATUS           VARCHAR2(1) DEFAULT 'W' NOT NULL,
   IN_PARAMS        VARCHAR2(4000),
   EXEC_RESULT      VARCHAR2(4000),
   EXEC_OWNER       VARCHAR2(20) DEFAULT 'SYS',
   STACK_TRACE      VARCHAR2(4000),
   JOB_STEP         VARCHAR2(50),
   START_DATE_TIME  TIMESTAMP NOT NULL,
   FROM_JOB_ID      NUMBER(19),
   CREATE_DATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER      VARCHAR2(255) DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME TIMESTAMP,
   UPDATE_USER      VARCHAR2(255),
   VERSION          INT DEFAULT 1,
   IDENTIF_DATA     VARCHAR2(4000),
   PERIOD_UID       VARCHAR2(36) DEFAULT SYS_GUID() NOT NULL,
   PERIOD_ITEMS_ID  NUMBER(19) DEFAULT 0 NOT NULL,
   PERIOD_NEXTTIME  NUMBER(19),
   CONSTRAINT UK_TSMP_DP_APPT_JOB_1 UNIQUE (PERIOD_UID, PERIOD_ITEMS_ID, PERIOD_NEXTTIME)
);
CREATE TABLE TSMP_DP_APPT_RJOB
(
   APPT_RJOB_ID     VARCHAR2(36) PRIMARY KEY NOT NULL,
   RJOB_NAME        VARCHAR2(60) NOT NULL,
   CRON_EXPRESSION  VARCHAR2(700) NOT NULL,
   CRON_JSON        VARCHAR2(4000) NOT NULL,
   CRON_DESC        VARCHAR2(300),
   NEXT_DATE_TIME   NUMBER(19) NOT NULL,
   LAST_DATE_TIME   NUMBER(19),
   EFF_DATE_TIME    NUMBER(19),
   INV_DATE_TIME    NUMBER(19),
   REMARK           VARCHAR2(300),
   STATUS           VARCHAR2(1) DEFAULT '1' NOT NULL,
   CREATE_DATE_TIME TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER      VARCHAR2(255) DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME TIMESTAMP,
   UPDATE_USER      VARCHAR2(255),
   VERSION          NUMBER(10) DEFAULT 1,
   KEYWORD_SEARCH   VARCHAR2(396)
);
CREATE TABLE TSMP_DP_APPT_RJOB_D
(
   APPT_RJOB_D_ID   NUMBER(19) PRIMARY KEY NOT NULL,
   APPT_RJOB_ID     VARCHAR2(36) NOT NULL,
   REF_ITEM_NO      VARCHAR2(50) NOT NULL,
   REF_SUBITEM_NO   VARCHAR2(100),
   IN_PARAMS        VARCHAR2(4000),
   IDENTIF_DATA     VARCHAR2(4000),
   SORT_BY          NUMBER(10) DEFAULT 0 NOT NULL,
   CREATE_DATE_TIME TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER      VARCHAR2(255) DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME TIMESTAMP,
   UPDATE_USER      VARCHAR2(255),
   VERSION          NUMBER(10) DEFAULT 1,
   KEYWORD_SEARCH   VARCHAR2(186)
);
CREATE TABLE TSMP_DP_CALLAPI

(

   CALLAPI_ID        NUMBER(19)      NOT NULL,

   REQ_URL           varchar(500)    NOT NULL,

   REQ_MSG           varchar(4000),

   RESP_MSG          varchar(4000),

   TOKEN_URL         varchar(500),

   SIGN_CODE_URL     varchar(500),

   AUTH              varchar(500)    NOT NULL,

   CREATE_DATE_TIME  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,

   CREATE_USER       varchar(255)    DEFAULT ('SYSTEM'),

           CONSTRAINT TSMP_DP_CALLAPI_PK PRIMARY KEY (CALLAPI_ID)

);
CREATE TABLE TSMP_DP_CHK_LAYER
(
   CHK_LAYER_ID     NUMBER(19) NOT NULL,
   REVIEW_TYPE      VARCHAR2(20) NOT NULL,
   LAYER            NUMBER(10) NOT NULL,
   ROLE_ID          VARCHAR2(10) NOT NULL,
   STATUS           VARCHAR2(1) DEFAULT '1' NOT NULL,
   CREATE_DATE_TIME TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER      VARCHAR2(255) DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME TIMESTAMP,
   UPDATE_USER      VARCHAR2(255),
   VERSION          NUMBER(10) DEFAULT 1,
   CONSTRAINT PK_TSMP_DP_CHK_LAYER PRIMARY KEY (REVIEW_TYPE, LAYER, ROLE_ID)
);

CREATE INDEX TSMP_DP_CHK_LAYER_IDX_01 ON  TSMP_DP_CHK_LAYER (CHK_LAYER_ID);
CREATE TABLE  TSMP_DP_CHK_LOG
(
   CHK_LOG_ID       NUMBER(19) PRIMARY KEY NOT NULL,
   REQ_ORDERS_ID    NUMBER(19) NOT NULL,
   REQ_ORDERM_ID    NUMBER(19) NOT NULL,
   LAYER            NUMBER(10) NOT NULL,
   REQ_COMMENT      VARCHAR2(200) NULL,
   REVIEW_STATUS    VARCHAR2(20) NOT NULL,
   CREATE_DATE_TIME TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER      VARCHAR2(255) DEFAULT 'SYSTEM'
);
CREATE TABLE TSMP_DP_CLIENTEXT

(

   CLIENT_ID           varchar(40)      NOT NULL,

   CLIENT_SEQ_ID       NUMBER(19)       NOT NULL,

   CONTENT_TXT         nvarchar2(1000)  NOT NULL,

   REG_STATUS          char(1)          DEFAULT ('0') NOT NULL,

   PWD_STATUS          char(1)          DEFAULT ('1') NOT NULL,

   PWD_RESET_KEY       varchar(22),

   REVIEW_REMARK       varchar(3000),

   REF_REVIEW_USER     nvarchar2(255),

   RESUBMIT_DATE_TIME  TIMESTAMP,

   PUBLIC_FLAG         char(1),

   CREATE_DATE_TIME    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,

   CREATE_USER         varchar(255)     DEFAULT ('SYSTEM'),

   UPDATE_DATE_TIME    TIMESTAMP,

   UPDATE_USER         varchar(255),

   VERSION             int              DEFAULT ((1)),

   KEYWORD_SEARCH      NVARCHAR2(2000),

   CONSTRAINT TSMP_DP_CLIENTEXT_PK PRIMARY KEY (CLIENT_ID),

   CONSTRAINT TSMP_DP_CLIENTEXT_UQ UNIQUE (CLIENT_SEQ_ID)

);
CREATE TABLE TSMP_DP_DENIED_MODULE
(
   REF_MODULE_NAME   varchar(255)   NOT NULL,
   CREATE_DATE_TIME  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER       varchar(255)   DEFAULT 'SYSTEM',
   PRIMARY KEY (REF_MODULE_NAME)
);
CREATE TABLE TSMP_DP_FAQ_ANSWER
(
   ANSWER_ID        NUMBER(19)   NOT NULL,
   ANSWER_NAME      NVARCHAR2(2000)    NOT NULL,
   ANSWER_NAME_EN   NVARCHAR2(2000),
   REF_QUESTION_ID  NUMBER(19)            NOT NULL,
   CREATE_TIME      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER      varchar(255)      DEFAULT 'SYSTEM',
   UPDATE_TIME      TIMESTAMP          NOT NULL,
   UPDATE_USER      varchar(255),
   VERSION          int               DEFAULT 1,
   KEYWORD_SEARCH   NVARCHAR2(2000),
   PRIMARY KEY (ANSWER_ID)
);
CREATE TABLE TSMP_DP_FAQ_QUESTION
(
   QUESTION_ID       NUMBER(19)  NOT NULL,
   QUESTION_NAME     NVARCHAR2(2000)    NOT NULL,
   QUESTION_NAME_EN  NVARCHAR2(2000),
   DATA_SORT         int,
   DATA_STATUS       char(1)           DEFAULT '1' NOT NULL,
   CREATE_TIME       TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER       varchar(255)      DEFAULT 'SYSTEM',
   UPDATE_TIME       TIMESTAMP          NOT NULL,
   UPDATE_USER       varchar(255),
   VERSION           int               DEFAULT 1,
   KEYWORD_SEARCH    NVARCHAR2(2000),
   PRIMARY KEY (QUESTION_ID)
);
CREATE TABLE  TSMP_DP_FILE
(
   FILE_ID NUMBER(19) PRIMARY KEY NOT NULL,
   FILE_NAME NVARCHAR2(100) NOT NULL,
   FILE_PATH NVARCHAR2(300) NOT NULL,
   REF_FILE_CATE_CODE NVARCHAR2(50) NOT NULL,
   REF_ID NUMBER(19) NOT NULL,
   CREATE_DATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER VARCHAR2(255) DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME TIMESTAMP,
   UPDATE_USER VARCHAR2(255),
   VERSION INT DEFAULT 1,
   KEYWORD_SEARCH NVARCHAR2(400),
   IS_BLOB VARCHAR2(1) DEFAULT 'N',
   IS_TMPFILE VARCHAR2(1) DEFAULT 'N',
   BLOB_DATA BLOB
);

CREATE UNIQUE INDEX TSMP_DP_FILE_UQ ON  TSMP_DP_FILE
(
   FILE_NAME,
   REF_FILE_CATE_CODE,
   REF_ID
);

CREATE INDEX TSMP_DP_FILE_IDX_01 ON  TSMP_DP_FILE
(
   REF_FILE_CATE_CODE,
   REF_ID
);
CREATE TABLE  TSMP_DP_ITEMS
(
   ITEM_ID          NUMBER(19) NOT NULL,
   ITEM_NO          VARCHAR2(20) NOT NULL,
   ITEM_NAME        VARCHAR2(100) NOT NULL,
   SUBITEM_NO       VARCHAR2(20) NOT NULL,
   SUBITEM_NAME     VARCHAR2(100) NOT NULL,
   SORT_BY          NUMBER(10) DEFAULT 0 NOT NULL,
   IS_DEFAULT       VARCHAR2(1),
   PARAM1           VARCHAR2(255),
   PARAM2           VARCHAR2(255),
   PARAM3           VARCHAR2(255),
   PARAM4           VARCHAR2(255),
   PARAM5           VARCHAR2(255),
   CREATE_DATE_TIME TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER      VARCHAR2(255) DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME TIMESTAMP,
   UPDATE_USER      VARCHAR2(255),
   VERSION          INT DEFAULT 1,
   KEYWORD_SEARCH   NVARCHAR2(200),
   LOCALE           VARCHAR2(10) DEFAULT 'zh-TW' NOT NULL,
   CONSTRAINT TSMP_DP_ITEMS_PK PRIMARY KEY (ITEM_NO, SUBITEM_NO, LOCALE)
);
CREATE TABLE  TSMP_DP_MAIL_LOG
(
   MAILLOG_ID       NUMBER(19) PRIMARY KEY NOT NULL,
   RECIPIENTS       VARCHAR2(100) NOT NULL,
   TEMPLATE_TXT     VARCHAR2(3800) NOT NULL,
   REF_CODE         VARCHAR2(20) NOT NULL,
   RESULT           VARCHAR2(1) DEFAULT '0' NOT NULL,
   CREATE_DATE_TIME TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER      VARCHAR2(255) DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME TIMESTAMP,
   UPDATE_USER      VARCHAR2(255),
   VERSION          INT DEFAULT 1,
   KEYWORD_SEARCH   VARCHAR2(4000)
);
CREATE TABLE  TSMP_DP_MAIL_TPLT
(
   MAILTPLT_ID      NUMBER(19) PRIMARY KEY NOT NULL,
   CODE             VARCHAR2(20) NOT NULL,
   TEMPLATE_TXT     VARCHAR2(2000) NOT NULL,
   REMARK           VARCHAR2(100),
   CREATE_DATE_TIME TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER      VARCHAR2(255) DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME TIMESTAMP,
   UPDATE_USER      VARCHAR2(255),
   VERSION          INT DEFAULT 1,
   KEYWORD_SEARCH   VARCHAR2(4000),
   CONSTRAINT TSMP_DP_MAIL_TPLT_UQ UNIQUE (CODE)
);
CREATE TABLE  TSMP_DP_MAIL_TPLTEN
(
   MAILTPLT_ID      NUMBER(19) PRIMARY KEY NOT NULL,
   CODE             VARCHAR2(20) NOT NULL,
   TEMPLATE_TXT     VARCHAR2(2000) NOT NULL,
   REMARK           VARCHAR2(100),
   CREATE_DATE_TIME TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER      VARCHAR2(255) DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME TIMESTAMP,
   UPDATE_USER      VARCHAR2(255),
   VERSION          INT DEFAULT 1,
   KEYWORD_SEARCH   VARCHAR2(4000),
   CONSTRAINT TSMP_DP_MAIL_TPLTEN_UQ UNIQUE (CODE)
);
CREATE TABLE  TSMP_DP_MAIL_TPLTTW
(
   MAILTPLT_ID      NUMBER(19) PRIMARY KEY NOT NULL,
   CODE             VARCHAR2(20) NOT NULL,
   TEMPLATE_TXT     VARCHAR2(2000) NOT NULL,
   REMARK           VARCHAR2(100),
   CREATE_DATE_TIME TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER      VARCHAR2(255) DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME TIMESTAMP,
   UPDATE_USER      VARCHAR2(255),
   VERSION          INT DEFAULT 1,
   KEYWORD_SEARCH   VARCHAR2(4000),
   CONSTRAINT TSMP_DP_MAIL_TPLTTW_UQ UNIQUE (CODE)
);
CREATE TABLE tsmp_dp_news
  (
     news_id             NUMBER(19) NOT NULL,
     new_title           VARCHAR(100) DEFAULT '_' NOT NULL,
     new_content         VARCHAR(4000) NOT NULL,
     status              VARCHAR(1) DEFAULT '1' NOT NULL,
     org_id              VARCHAR(255) NOT NULL,
     post_date_time      TIMESTAMP NOT NULL,
     ref_type_subitem_no VARCHAR(20) NOT NULL,
     create_date_time    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     create_user         VARCHAR(255) DEFAULT 'SYSTEM',
     update_date_time    TIMESTAMP,
     update_user         VARCHAR(255),
     version             INT DEFAULT 1,
     keyword_search      VARCHAR(2148),
     CONSTRAINT tsmp_dp_news_pk PRIMARY KEY (news_id)
  ); 
CREATE TABLE  TSMP_DP_REQ_ORDERD1
(
   REQ_ORDERD1_ID    NUMBER(19) PRIMARY KEY NOT NULL,
   REF_REQ_ORDERM_ID NUMBER(19) NOT NULL,
   CLIENT_ID         VARCHAR2(40) NOT NULL,
   API_UID           VARCHAR2(36) NOT NULL,
   CREATE_DATE_TIME  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER       VARCHAR2(255) DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME  TIMESTAMP,
   UPDATE_USER       VARCHAR2(255),
   VERSION           INT DEFAULT 1
);
CREATE TABLE  TSMP_DP_REQ_ORDERD2
(
   REQ_ORDERD2_ID    NUMBER(19) PRIMARY KEY NOT NULL,
   REF_REQ_ORDERM_ID NUMBER(19) NOT NULL,
   API_UID           VARCHAR2(36) NOT NULL,
   CREATE_DATE_TIME  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER       VARCHAR2(255) DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME  TIMESTAMP,
   UPDATE_USER       VARCHAR2(255),
   VERSION           INT DEFAULT 1,
   PUBLIC_FLAG       CHAR(1)
);

CREATE INDEX TSMP_DP_REQ_ORDERD2_IDX_01 ON  TSMP_DP_REQ_ORDERD2 (API_UID);
CREATE TABLE  TSMP_DP_REQ_ORDERD2D
(
   REQ_ORDERD2_ID   NUMBER(19) NOT NULL,
   API_UID          VARCHAR2(36) NOT NULL,
   REF_THEME_ID     NUMBER(19) NOT NULL,
   REQ_ORDERD2D_ID  NUMBER(19) NOT NULL,
   CREATE_DATE_TIME TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER      VARCHAR2(255) DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME TIMESTAMP,
   UPDATE_USER      VARCHAR2(255),
   VERSION          INT DEFAULT 1,
   CONSTRAINT TSMP_DP_REQ_ORDERD2D_PK PRIMARY KEY (REQ_ORDERD2_ID, API_UID, REF_THEME_ID),
   CONSTRAINT TSMP_DP_REQ_ORDERD2D_UQ UNIQUE (REQ_ORDERD2D_ID)
);
CREATE TABLE  TSMP_DP_REQ_ORDERD3
(
   REQ_ORDERD3_ID    NUMBER(19) PRIMARY KEY NOT NULL,
   REF_REQ_ORDERM_ID NUMBER(19) NOT NULL,
   CLIENT_ID         VARCHAR2(40) NOT NULL,
   CREATE_DATE_TIME  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER       VARCHAR2(255) DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME  TIMESTAMP,
   UPDATE_USER       VARCHAR2(255),
   VERSION           INT DEFAULT 1
);
CREATE TABLE  TSMP_DP_REQ_ORDERD5
(
   REQ_ORDERD5_ID     NUMBER(19) PRIMARY KEY NOT NULL,
   CLIENT_ID          VARCHAR2(255) NOT NULL,
   REF_REQ_ORDERM_ID  NUMBER(19) NOT NULL,
   REF_OPEN_APIKEY_ID NUMBER(19),
   OPEN_APIKEY        VARCHAR2(1024),
   SECRET_KEY         VARCHAR2(1024),
   OPEN_APIKEY_ALIAS  VARCHAR2(255) NOT NULL,
   TIMES_THRESHOLD    NUMBER(10) DEFAULT 0 NOT NULL,
   EXPIRED_AT         NUMBER(19) NOT NULL,
   CREATE_DATE_TIME   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER        VARCHAR2(255) DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME   TIMESTAMP,
   UPDATE_USER        VARCHAR2(255),
   VERSION           INT DEFAULT 1
);

CREATE INDEX INDEX_TSMP_DP_REQ_ORDERD5_01 ON  TSMP_DP_REQ_ORDERD5 (OPEN_APIKEY);
CREATE TABLE  TSMP_DP_REQ_ORDERD5D
(
   REF_REQ_ORDERD5_ID NUMBER(19) NOT NULL,
   REF_API_UID        VARCHAR2(36) NOT NULL,
   REQ_ORDERD5D_ID    NUMBER(19) NOT NULL,
   CREATE_DATE_TIME   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER        VARCHAR2(255) DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME   TIMESTAMP,
   UPDATE_USER        VARCHAR2(255),
   VERSION            INT DEFAULT 1,
   CONSTRAINT PK_TSMP_DP_REQ_ORDERD5D PRIMARY KEY (REF_REQ_ORDERD5_ID, REF_API_UID)
);
CREATE TABLE  TSMP_DP_REQ_ORDERM
(
   REQ_ORDERM_ID    NUMBER(19) PRIMARY KEY NOT NULL,
   REQ_ORDER_NO     VARCHAR2(30) NOT NULL,
   REQ_TYPE         VARCHAR2(20) NOT NULL,
   REQ_SUBTYPE      VARCHAR2(20),
   CLIENT_ID        VARCHAR2(40) NOT NULL,
   ORG_ID           VARCHAR2(255),
   REQ_DESC         NVARCHAR2(1000) NOT NULL,
   REQ_USER_ID      VARCHAR2(10),
   EFFECTIVE_DATE   TIMESTAMP,
   CREATE_DATE_TIME TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER      VARCHAR2(255) DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME TIMESTAMP,
   UPDATE_USER      VARCHAR2(255),
   VERSION          INT DEFAULT 1,
   KEYWORD_SEARCH   NVARCHAR2(1020),
   CONSTRAINT TSMP_DP_REQ_ORDERM_UQ UNIQUE (REQ_ORDER_NO)
);
CREATE TABLE  TSMP_DP_REQ_ORDERS
(
   REQ_ORDERS_ID    NUMBER(19) PRIMARY KEY NOT NULL,
   REQ_ORDERM_ID    NUMBER(19) NOT NULL,
   LAYER            NUMBER(10) NOT NULL,
   REQ_COMMENT      VARCHAR2(200),
   REVIEW_STATUS    VARCHAR2(20) DEFAULT 'WAIT1' NOT NULL,
   STATUS           VARCHAR2(1) DEFAULT '1' NOT NULL,
   PROC_FLAG        NUMBER(10),
   CREATE_DATE_TIME TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER      VARCHAR2(255) DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME TIMESTAMP,
   UPDATE_USER      VARCHAR2(255),
   VERSION         INT DEFAULT 1,
   KEYWORD_SEARCH   NVARCHAR2(200)
);
CREATE TABLE TSMP_DP_SITE_MAP
(
   SITE_ID          NUMBER(19)     NOT NULL,
   SITE_PARENT_ID   NUMBER(19)            NOT NULL,
   SITE_DESC       nvarchar2(200)     NOT NULL,
   DATA_SORT       int               NOT NULL,
   SITE_URL        nvarchar2(200),
   CREATE_TIME     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER     varchar(255)      DEFAULT 'SYSTEM',
   UPDATE_TIME     TIMESTAMP,
   UPDATE_USER     varchar(255),
   VERSION         int               DEFAULT 1,
  CONSTRAINT TSMP_DP_SITE_MAP_PK PRIMARY KEY (SITE_ID)
);
CREATE TABLE TSMP_DP_THEME_CATEGORY

(

   ID              NUMBER(19)                   NOT NULL,

   THEME_NAME      nvarchar2(100)    NOT NULL,

   DATA_STATUS     char(1)           DEFAULT '1' NOT NULL,

   DATA_SORT       int,

   ORG_ID          varchar(255),

   CREATE_TIME     TIMESTAMP                DEFAULT CURRENT_TIMESTAMP,

   CREATE_USER     varchar(255)      DEFAULT 'SYSTEM',

   UPDATE_TIME     TIMESTAMP,

   UPDATE_USER     varchar(255),

   VERSION         int               DEFAULT 1,

   KEYWORD_SEARCH  nvarchar2(100),

           CONSTRAINT TSMP_DP_THEME_CATEGORY_PK PRIMARY KEY (ID)

);
CREATE TABLE TSMP_EVENTS
(
   EVENT_ID          NUMBER(19)           NOT NULL,
   EVENT_TYPE_ID     VARCHAR(20)      NOT NULL,
   EVENT_NAME_ID     VARCHAR(20)      NOT NULL,
   MODULE_NAME       NVARCHAR2(255)    NOT NULL,
   MODULE_VERSION    NVARCHAR2(255),
   TRACE_ID          VARCHAR(20)      NOT NULL,
   INFO_MSG          NVARCHAR2(2000),
   KEEP_FLAG         VARCHAR(1)       DEFAULT 'N' NOT NULL,
   ARCHIVE_FLAG      VARCHAR(1)       DEFAULT 'N' NOT NULL,
   NODE_ALIAS        NVARCHAR2(200),
   NODE_ID           VARCHAR(200),
   THREAD_NAME       NVARCHAR2(1000),
   CREATE_DATE_TIME  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER       VARCHAR(255) DEFAULT 'SYSTEM',
   CONSTRAINT TSMP_EVENTS_PK PRIMARY KEY (EVENT_ID)
);
CREATE TABLE TSMP_FUNC
(
   FUNC_CODE     NVARCHAR2(10)    NOT NULL,
   FUNC_NAME     NVARCHAR2(50)    NOT NULL,
   FUNC_NAME_EN  NVARCHAR2(50),
   FUNC_DESC     NVARCHAR2(300),
   LOCALE        NVARCHAR2(10)    NOT NULL,
   UPDATE_USER   VARCHAR2(255),
   UPDATE_TIME   TIMESTAMP             NOT NULL,
   FUNC_URL      NVARCHAR2(300),
   CONSTRAINT TSMP_FUNC_PK PRIMARY KEY (FUNC_CODE, LOCALE)
);
CREATE TABLE TSMP_FUNC_EDITION
(
   FUNC_CODE     NVARCHAR2(10)    NOT NULL,
   FUNC_NAME     NVARCHAR2(50)    NOT NULL,
   FUNC_NAME_EN  NVARCHAR2(50),
   FUNC_DESC     NVARCHAR2(300),
   FUNC_URL      NVARCHAR2(300),
   LOCALE        NVARCHAR2(10)    DEFAULT 'zh-TW' NOT NULL,
   UPDATE_USER   NVARCHAR2(255)   NOT NULL,
   UPDATE_TIME   TIMESTAMP             NOT NULL,
   V_ENTERPRISE  INT           DEFAULT 0 NOT NULL,
   V_EXPRESS     INT           DEFAULT 0 NOT NULL,
   V_DIGILOGS    INT           DEFAULT 0 NOT NULL,
   V_TAIPEIGOV   INT           DEFAULT 0 NOT NULL,
   V_SCB         INT           DEFAULT 0 NOT NULL,
   CONSTRAINT TSMP_FUNC_EDITION_PK PRIMARY KEY (FUNC_CODE, LOCALE)
);
CREATE TABLE tsmp_group (
  group_id NVARCHAR2(10) NOT NULL,
  group_name NVARCHAR2(150) NOT NULL,
  create_time TIMESTAMP NOT NULL,
  update_time TIMESTAMP,
  create_user VARCHAR(255),
  update_user VARCHAR(255),
  group_alias VARCHAR(150),
  group_desc VARCHAR(1500),
  group_access VARCHAR(255),
  security_level_id VARCHAR(10 CHAR) DEFAULT 'system',
  allow_days INT DEFAULT 0,
  allow_times INT DEFAULT 0,
  vgroup_flag CHAR(1) DEFAULT '0',
  vgroup_id VARCHAR(10),
  vgroup_name VARCHAR(30),
  PRIMARY KEY (group_id)
);
CREATE TABLE tsmp_group_api (
  group_id NVARCHAR2(10) NOT NULL,
  api_key NVARCHAR2(255) NOT NULL,
  module_name NVARCHAR2(100) NOT NULL,
  module_version NVARCHAR2(20),
  create_time TIMESTAMP NOT NULL,
  PRIMARY KEY (group_id, api_key, module_name)
);
CREATE TABLE TSMP_GROUP_AUTHORITIES (
  GROUP_AUTHORITIE_ID VARCHAR(10) NOT NULL,
  GROUP_AUTHORITIE_NAME VARCHAR(30) NOT NULL,
  GROUP_AUTHORITIE_DESC VARCHAR(60),
  GROUP_AUTHORITIE_LEVEL VARCHAR(10),
  PRIMARY KEY (GROUP_AUTHORITIE_ID),
  CONSTRAINT TSMP_GROUP_AUTHORITIES_UQ UNIQUE (GROUP_AUTHORITIE_NAME)
);
-- digirunner.tsmp_group_authorities_map definition for Oracle

CREATE TABLE tsmp_group_authorities_map (
  group_id VARCHAR(10 CHAR) NOT NULL,
  group_authoritie_id VARCHAR(10 CHAR) NOT NULL,
  PRIMARY KEY (group_id, group_authoritie_id)
);
CREATE TABLE TSMP_GROUP_TIMES_LOG(
SEQ_NO NUMBER(19)  NOT NULL,
JTI                        VARCHAR(100) NOT NULL,
GROUP_ID        VARCHAR(10),
EXPIRE_TIME  TIMESTAMP,
CREATE_TIME TIMESTAMP,
REEXPIRED_TIME TIMESTAMP,
TIMES_QUOTA INT,
TIMES_THRESHOLD INT,
REJTI VARCHAR(100),
PRIMARY KEY (SEQ_NO),
UNIQUE  (JTI,GROUP_ID)
);
CREATE TABLE TSMP_HEARTBEAT
(
   NODE_ID      NVARCHAR2(30)   NOT NULL,
   START_TIME   TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
   UPDATE_TIME  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
   NODE_INFO    NVARCHAR2(100),
   CONSTRAINT TSMP_HEARTBEAT_PK PRIMARY KEY (NODE_ID)
);
CREATE TABLE TSMP_NODE
(
   ID           NVARCHAR2(255)   NOT NULL,
   START_TIME   TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
   UPDATE_TIME  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
   NODE         NVARCHAR2(30),
   CONSTRAINT TSMP_NODE_PK PRIMARY KEY (ID)
);
CREATE TABLE TSMP_NODE_TASK
(
   ID              NUMBER(19)           NOT NULL,
   TASK_SIGNATURE  NVARCHAR2(255)   NOT NULL,
   TASK_ID         NVARCHAR2(255)   NOT NULL,
   TASK_ARG        VARCHAR(2000),
   COORDINATION    NVARCHAR2(255)   NOT NULL,
   EXECUTE_TIME    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
   NOTICE_NODE     NVARCHAR2(255)   NOT NULL,
   NODE            NVARCHAR2(255)   NOT NULL,
   NOTICE_TIME     TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
   MODULE_NAME     VARCHAR2(255),
   MODULE_VERSION  VARCHAR2(255),
   CONSTRAINT TSMP_NODE_TASK_PK PRIMARY KEY (ID),
   CONSTRAINT TSMP_NODE_TASK_UQ UNIQUE (TASK_SIGNATURE, TASK_ID)
);
CREATE TABLE TSMP_NODE_TASK_WORK
(
   ID                NUMBER(19)           NOT NULL,
   NODE_TASK_ID      NUMBER(19)           NOT NULL,
   COMPETITIVE_ID    NVARCHAR2(255)   NOT NULL,
   COMPETITIVE_TIME  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
   COMPETITIVE_NODE  NVARCHAR2(255)   NOT NULL,
   NODE              NVARCHAR2(255)   NOT NULL,
   UPDATE_TIME       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
   SUCCESS           NUMBER(1),
   ERROR_MSG         NVARCHAR2(1023),
   CONSTRAINT TSMP_NODE_TASK_WORK_PK PRIMARY KEY (ID),
   CONSTRAINT TSMP_NODE_TASK_WORK_UQ UNIQUE (NODE_TASK_ID, COMPETITIVE_ID)
);
CREATE TABLE tsmp_notice_log (
  notice_log_id NUMBER(19)  NOT NULL,
  notice_src VARCHAR(100 CHAR) NOT NULL,
  notice_mthd VARCHAR(10 CHAR) NOT NULL,
  notice_key VARCHAR(255 CHAR) NOT NULL,
  detail_id NUMBER(19) ,
  last_notice_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (notice_log_id)
);
CREATE TABLE TSMP_OPEN_APIKEY
(
   OPEN_APIKEY_ID      NUMBER(19)      NOT NULL,
   CLIENT_ID           varchar(255)    NOT NULL,
   OPEN_APIKEY         varchar(1024)   NOT NULL,
   SECRET_KEY          varchar(1024)   NOT NULL,
   OPEN_APIKEY_ALIAS   varchar(255)    NOT NULL,
   TIMES_QUOTA         int             DEFAULT -1 NOT NULL,
   TIMES_THRESHOLD     int             DEFAULT -1 NOT NULL,
   EXPIRED_AT          NUMBER(19)      NOT NULL,
   REVOKED_AT          NUMBER(19),
   OPEN_APIKEY_STATUS  varchar(1)      DEFAULT 1 NOT NULL,
   CREATE_DATE_TIME    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER         varchar(255)    DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME    TIMESTAMP,
   UPDATE_USER         varchar(255),
   VERSION             int             DEFAULT 1,
   ROLLOVER_FLAG       varchar(1)      DEFAULT 'N' NOT NULL,
   PRIMARY KEY (OPEN_APIKEY_ID)
);
CREATE TABLE TSMP_OPEN_APIKEY_MAP
(
   OPEN_APIKEY_MAP_ID  NUMBER(19)         NOT NULL,
   REF_OPEN_APIKEY_ID  NUMBER(19)         NOT NULL,
   REF_API_UID         varchar(36)    NOT NULL,
   CREATE_DATE_TIME    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,        
   CREATE_USER         varchar(255)   DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME    TIMESTAMP,
   UPDATE_USER         varchar(255),
   VERSION             int            DEFAULT 1,
   PRIMARY KEY (OPEN_APIKEY_MAP_ID)
);
-- digirunner.tsmp_organization definition for Oracle

CREATE TABLE tsmp_organization (
  org_id VARCHAR(255 CHAR) NOT NULL,
  org_name VARCHAR(30 CHAR) UNIQUE,
  parent_id VARCHAR(10 CHAR),
  org_path VARCHAR(255 CHAR),
  create_user VARCHAR(255 CHAR),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_user VARCHAR(255 CHAR),
  update_time TIMESTAMP,
  contact_name VARCHAR(50 CHAR),
  contact_tel VARCHAR(50 CHAR),
  contact_mail VARCHAR(100 CHAR),
  org_code VARCHAR(100 CHAR),
  PRIMARY KEY (org_id)
);
CREATE TABLE TSMP_OTP(
OPAQUE         VARCHAR(100) NOT NULL,
OTP            VARCHAR(10) ,
ERR_TIMES      INT         ,
CREATE_TIME    TIMESTAMP    ,
VALID_TIME     TIMESTAMP    ,
CHECK_TIME     TIMESTAMP    ,
USED           CHAR(1)     ,
PRIMARY KEY (OPAQUE)
);
CREATE TABLE TSMP_REG_HOST
(
   REGHOST_ID      VARCHAR2(10)   NOT NULL,
   REGHOST         VARCHAR2(30)   NOT NULL,
   REGHOST_STATUS  CHAR(1) ,
   ENABLED         CHAR(1)        NOT NULL,
   CLIENTID        VARCHAR2(40)   NOT NULL,
   HEARTBEAT       TIMESTAMP,
   MEMO            VARCHAR2(300),
   CREATE_USER     VARCHAR2(255),
   CREATE_TIME     TIMESTAMP      NOT NULL,
   UPDATE_USER     VARCHAR2(255),
   UPDATE_TIME     TIMESTAMP,
   CONSTRAINT TSMP_REG_HOST_PK PRIMARY KEY (REGHOST_ID),
   CONSTRAINT TSMP_REG_HOST_UQ UNIQUE (REGHOST)
);
CREATE TABLE TSMP_REG_MODULE
(
   REG_MODULE_ID     NUMBER(19)         NOT NULL,
   MODULE_NAME       VARCHAR2(255)  NOT NULL,
   MODULE_VERSION    VARCHAR2(255)  NOT NULL,
   MODULE_SRC        CHAR(1)        NOT NULL,
   LATEST            CHAR(1)        DEFAULT 'N' NOT NULL,
   UPLOAD_DATE_TIME  TIMESTAMP      NOT NULL,
   UPLOAD_USER       VARCHAR2(255)  NOT NULL,
   CREATE_DATE_TIME  TIMESTAMP      DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER       VARCHAR2(255)  DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME  TIMESTAMP,
   UPDATE_USER       VARCHAR2(255),
   VERSION           INT         DEFAULT 1,
   CONSTRAINT TSMP_REG_MODULE_PK PRIMARY KEY (REG_MODULE_ID),
   CONSTRAINT TSMP_REG_MODULE_UK UNIQUE (MODULE_NAME, MODULE_VERSION)
);
CREATE TABLE TSMP_REPORT_DATA (
    ID NUMBER(19) NOT NULL,
    REPORT_TYPE INT NOT NULL,
    DATE_TIME_RANGE_TYPE INT NOT NULL,
    LAST_ROW_DATE_TIME TIMESTAMP NOT NULL,
    STATISTICS_STATUS CHAR(1) NOT NULL,
    STRING_GROUP1 VARCHAR2(255),
    STRING_GROUP2 VARCHAR2(255),
    STRING_GROUP3 VARCHAR2(255),
    INT_VALUE1 NUMBER(19),
    INT_VALUE2 NUMBER(19),
    INT_VALUE3 NUMBER(19),
    ORGID VARCHAR2(255) NOT NULL,
    CREATE_DATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CREATE_USER VARCHAR2(255) DEFAULT 'SYSTEM',
    UPDATE_DATE_TIME TIMESTAMP,
    UPDATE_USER VARCHAR2(255),
    VERSION INT DEFAULT 1,
    CONSTRAINT TSMP_REPORT_DATA_PK PRIMARY KEY (ID)
);
CREATE TABLE TSMP_REPORT_URL 
(
  REPORT_ID    NVARCHAR2(8) NOT NULL,
  TIME_RANGE   CHAR(1) NOT NULL,
  REPORT_URL   NVARCHAR2(2000),
  CONSTRAINT TSMP_REPORT_URL_UQ UNIQUE (REPORT_ID,TIME_RANGE)
);
CREATE TABLE TSMP_REQ_LOG (
    ID VARCHAR2(63) NOT NULL,
    RTIME TIMESTAMP NOT NULL,
    ATYPE VARCHAR2(3) NOT NULL,
    MODULE_NAME VARCHAR2(255) NOT NULL,
    MODULE_VERSION VARCHAR2(255) NOT NULL,
    NODE_ALIAS VARCHAR2(255) NOT NULL,
    NODE_ID VARCHAR2(255) NOT NULL,
    URL VARCHAR2(255) NOT NULL,
    CIP VARCHAR2(255) NOT NULL,
    ORGID VARCHAR2(255) ,
    TXID VARCHAR2(255),
    ENTRY VARCHAR2(255),
    CID VARCHAR2(255),
    TUSER VARCHAR2(255),
    JTI VARCHAR2(255),
    CONSTRAINT TSMP_REQ_LOG_PK PRIMARY KEY (ID)
);
CREATE TABLE TSMP_REQ_LOG_HISTORY (
    ID VARCHAR2(63) NOT NULL,
    RTIME TIMESTAMP NOT NULL,
    ATYPE VARCHAR2(3) NOT NULL,
    MODULE_NAME VARCHAR2(255) NOT NULL,
    MODULE_VERSION VARCHAR2(255) NOT NULL,
    NODE_ALIAS VARCHAR2(255) NOT NULL,
    NODE_ID VARCHAR2(255) NOT NULL,
    URL VARCHAR2(255) NOT NULL,
    CIP VARCHAR2(255) NOT NULL,
    ORGID VARCHAR2(255) NOT NULL,
    TXID VARCHAR2(255),
    ENTRY VARCHAR2(255),
    CID VARCHAR2(255),
    TUSER VARCHAR2(255),
    JTI VARCHAR2(255),
    CREATE_DATE_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CREATE_USER VARCHAR2(255) DEFAULT 'SYSTEM',
    UPDATE_DATE_TIME TIMESTAMP,
    UPDATE_USER VARCHAR2(255),
    VERSION INT DEFAULT 1,
    CONSTRAINT TSMP_REQ_LOG_HISTORY_PK PRIMARY KEY (ID)
);
CREATE TABLE TSMP_REQ_RES_LOG_HISTORY
(
   ID                VARCHAR(63)     NOT NULL,
   ATYPE             VARCHAR(3)      ,
   MODULE_NAME       VARCHAR(255)    NOT NULL,
   MODULE_VERSION    VARCHAR(255)    ,
   NODE_ALIAS        VARCHAR(255)    ,
   NODE_ID           VARCHAR(255)    ,
   URL               VARCHAR(255)    ,
   CIP               VARCHAR(255)    ,
   ORGID             VARCHAR(255)    ,
   TXID              VARCHAR(255),
   ENTRY             VARCHAR(255),
   CID               VARCHAR(255),
   TUSER             VARCHAR(255),
   JTI               VARCHAR(255),
   EXE_STATUS        CHAR(1),
   ELAPSE            NUMBER(10) ,
   RCODE             VARCHAR(63) ,
   HTTP_STATUS       NUMBER(10),
   ERR_MSG           VARCHAR(4000),
   RTIME             TIMESTAMP,
   RTIME_YEAR_MONTH  VARCHAR(8),
   CONSTRAINT TSMP_REQ_RES_LOG_HISTORY_PK PRIMARY KEY (ID)
);
CREATE TABLE TSMP_RES_LOG
(
   ID           VARCHAR(63)     NOT NULL,
   EXE_STATUS   CHAR(1)         NOT NULL,
   ELAPSE       NUMBER(10)      NOT NULL,
   RCODE        VARCHAR(63)     ,
   HTTP_STATUS  NUMBER(10)      NOT NULL,
   ERR_MSG      VARCHAR(4000),
   CONSTRAINT TSMP_RES_LOG_PK PRIMARY KEY (ID)
);
CREATE TABLE TSMP_RES_LOG_HISTORY
(
   ID                VARCHAR(63)     NOT NULL,
   EXE_STATUS        CHAR(1)         NOT NULL,
   ELAPSE            NUMBER(10)             NOT NULL,
   RCODE             VARCHAR(63)     NOT NULL,
   HTTP_STATUS       NUMBER(10)             NOT NULL,
   ERR_MSG           VARCHAR(4000),
   CREATE_DATE_TIME  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER       VARCHAR(255) DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME  TIMESTAMP,
   UPDATE_USER       VARCHAR(255),
   VERSION           INT      DEFAULT 1,
   CONSTRAINT TSMP_RES_LOG_HISTORY_PK PRIMARY KEY (ID)
);
CREATE TABLE TSMP_ROLE
(
   ROLE_ID      NVARCHAR2(10)   NOT NULL,
   ROLE_NAME    NVARCHAR2(30)   NOT NULL,
   CREATE_USER  VARCHAR(255),
   CREATE_TIME  TIMESTAMP      NOT NULL,
   ROLE_ALIAS   VARCHAR2(255),
   CONSTRAINT TSMP_ROLE_PK PRIMARY KEY (ROLE_ID)
);
CREATE TABLE TSMP_ROLE_ALERT
( 
   ROLE_ID   NVARCHAR2(10)   NOT NULL,
   ALERT_ID  INT DEFAULT -1 NOT NULL,
    CONSTRAINT TSMP_ROLE_ALERT_PK PRIMARY KEY (ROLE_ID, ALERT_ID)
);
CREATE TABLE TSMP_ROLE_FUNC
(
   ROLE_ID    nvarchar2(10)   NOT NULL,
   FUNC_CODE  nvarchar2(10)   NOT NULL,
           CONSTRAINT TSMP_ROLE_FUNC_PK PRIMARY KEY (ROLE_ID, FUNC_CODE)
);
CREATE TABLE TSMP_ROLE_PRIVILEGE
(
   ROLE_ID     varchar(10)   NOT NULL,
   ROLE_SCOPE  varchar(30),
   CONSTRAINT TSMP_ROLE_PRIVILEGE_PK PRIMARY KEY (ROLE_ID)
);
CREATE TABLE TSMP_ROLE_ROLE_MAPPING
(
   ROLE_NAME          varchar(50),
   ROLE_NAME_MAPPING  varchar(50),
   ROLE_ROLE_ID       NUMBER(19)        NOT NULL,
         CONSTRAINT TSMP_ROLE_ROLE_MAPPING_PK PRIMARY KEY (ROLE_ROLE_ID)
);
CREATE TABLE TSMP_ROLE_TXID_MAP
(
   ROLE_TXID_MAP_ID  NUMBER(19)    NOT NULL,
   ROLE_ID           varchar(10)    NOT NULL,
   TXID              varchar(10)    NOT NULL,
   LIST_TYPE         varchar(1)     NOT NULL,
   CREATE_DATE_TIME  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
   CREATE_USER       varchar(255)   DEFAULT 'SYSTEM',
   UPDATE_DATE_TIME  TIMESTAMP,
   UPDATE_USER       varchar(255),
   VERSION           int            DEFAULT 1,
   KEYWORD_SEARCH    varchar(20),
           CONSTRAINT TSMP_ROLE_TXID_MAP_PK PRIMARY KEY (ROLE_TXID_MAP_ID),
        CONSTRAINT TSMP_ROLE_TXID_MAP_UQ UNIQUE (ROLE_ID, TXID)
);
CREATE TABLE TSMP_RTN_CODE
(
   TSMP_RTN_CODE  nvarchar2(20)    NOT NULL,
   LOCALE         nvarchar2(10)    NOT NULL,
   TSMP_RTN_MSG   nvarchar2(300) ,
   TSMP_RTN_DESC  nvarchar2(300),
           CONSTRAINT TSMP_RTN_CODE_PK PRIMARY KEY (TSMP_RTN_CODE, LOCALE)
);
CREATE TABLE TSMP_SECURITY_LEVEL
(
   SECURITY_LEVEL_ID    varchar(10)   NOT NULL,
   SECURITY_LEVEL_NAME  varchar(30)   NOT NULL,
   SECURITY_LEVEL_DESC  varchar(60),
   CONSTRAINT TSMP_SECURITY_LEVEL_PK PRIMARY KEY (SECURITY_LEVEL_ID),
   CONSTRAINT TSMP_SECURITY_LEVEL_UQ UNIQUE (SECURITY_LEVEL_NAME)
);
CREATE TABLE TSMP_SESS_ATTRS
(
   API_SESSION_ID  nvarchar2(100)    NOT NULL,
   ATTR_NAME       nvarchar2(20)     NOT NULL,
   ATTR_VALUES     NVARCHAR2(2000),
   UPDATE_TIME     TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
   CONSTRAINT TSMP_SESS_ATTRS_PK PRIMARY KEY (API_SESSION_ID, ATTR_NAME)
);
CREATE TABLE TSMP_SESSION
(
   API_SESSION_ID  nvarchar2(100)    NOT NULL,
   CUST_ID         nvarchar2(30),
   CUST_NAME       nvarchar2(20),
   CONST_DATA      nvarchar2(1000)   NOT NULL,
   CREATE_TIME     TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
   USER_IP         nvarchar2(15),
   CONSTRAINT TSMP_SESSION_PK PRIMARY KEY (API_SESSION_ID)
);
CREATE TABLE TSMP_SETTING
(
   ID     nvarchar2(255)    NOT NULL,
   VALUE  varchar(2000),
   MEMO   varchar(512)     DEFAULT NULL,
   CONSTRAINT TSMP_SETTING_PK PRIMARY KEY (ID)
);
CREATE TABLE tsmp_sso_user_secret

(

   user_secret_id    NUMBER(19)          NOT NULL,

   user_name         varchar(50),

   secret            varchar(100),

   create_date_time  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,

   create_user       varchar(255)   DEFAULT 'SSO SYSTEM',

   update_date_time  TIMESTAMP,

   update_user       varchar(255),

   version           int            DEFAULT 1,

   CONSTRAINT tsmp_sso_user_secret_PK PRIMARY KEY (user_secret_id)

);
CREATE TABLE TSMP_TOKEN_HISTORY
(
   SEQ_NO                NUMBER(19)       NOT NULL,
   USER_NID              varchar(255),
   USER_NAME             varchar(50),
   CLIENT_ID             varchar(40)      NOT NULL,
   TOKEN_JTI             varchar(100)     NOT NULL,
   SCOPE                 varchar (4000),
   EXPIRED_AT            TIMESTAMP         NOT NULL,
   CREATE_AT             TIMESTAMP         NOT NULL,
   STIME                 TIMESTAMP,
   REVOKED_AT            TIMESTAMP,
   REVOKED_STATUS        char(2),
   RETOKEN_JTI           varchar(100)     NOT NULL,
   REEXPIRED_AT          TIMESTAMP         NOT NULL,
   RFT_REVOKED_AT        TIMESTAMP,
   RFT_REVOKED_STATUS    varchar(10),
   TOKEN_QUOTA           int,
   TOKEN_USED            int,
   RFT_QUOTA             int,
   RFT_USED              int,
   IDP_TYPE              varchar(50),
   ID_TOKEN_JWTSTR       varchar(4000),
   REFRESH_TOKEN_JWTSTR  varchar(4000),
   CONSTRAINT TSMP_TOKEN_HISTORY_PK PRIMARY KEY (SEQ_NO)
);
CREATE TABLE TSMP_TOKEN_HISTORY_HOUSING
(
   SEQ_NO          NUMBER(19)           NOT NULL,
   USER_NID        varchar(255),
   USER_NAME       varchar(50),
   CLIENT_ID       varchar(40)      NOT NULL,
   TOKEN_JTI       varchar(100)     NOT NULL,
   SCOPE           varchar (4000),
   EXPIRED_AT      TIMESTAMP         NOT NULL,
   CREATE_AT       TIMESTAMP         NOT NULL,
   STIME           TIMESTAMP,
   REVOKED_AT      TIMESTAMP,
   REVOKED_STATUS  char(2),
   RETOKEN_JTI     varchar(100)     NOT NULL,
   REEXPIRED_AT    TIMESTAMP         NOT NULL,
   CONSTRAINT TSMP_TOKEN_HISTORY_HOUSING_PK PRIMARY KEY (SEQ_NO)
);
CREATE TABLE TSMP_TOKEN_USAGE_COUNT
(
   TOKEN_JTI        varchar(100)   NOT NULL,
   TIMES_THRESHOLD  int            NOT NULL,
   TOKEN_TYPE       char(1)        NOT NULL,
   EXPIRED_AT       TIMESTAMP      NOT NULL,
    CONSTRAINT TSMP_TOKEN_USAGE_COUNT_PK PRIMARY KEY (TOKEN_JTI)
);
CREATE TABLE TSMP_TOKEN_USAGE_HISTORY
(
   SEQ_ID       NUMBER(10)       NOT NULL,
   TGTL_SEQ_ID  NUMBER(10)       NOT NULL,
   TOKEN_JTI    VARCHAR(100)     NOT NULL,
   SCOPE        VARCHAR(4000),
   TXTIME       TIMESTAMP,
   EXPIREDTIME  TIMESTAMP,
   CONSTRAINT TSMP_TOKEN_USAGE_HISTORY_PK PRIMARY KEY (SEQ_ID, TGTL_SEQ_ID)
);
CREATE TABLE TSMP_TXKEY
(
   KEY_ID       NUMBER(19)      NOT NULL,
   TX_KEY       NVARCHAR2(64)   NOT NULL,
   IV           NVARCHAR2(64),
   ALG          CHAR(1)        NOT NULL,
   CREATE_TIME  TIMESTAMP      NOT NULL
);
CREATE TABLE TSMP_TXTOKEN
(
   TXTOKEN         NVARCHAR2(64)   NOT NULL,
   TXTOKEN_STATUS  CHAR(1)        NOT NULL,
   CREATE_TIME     TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
   USE_TIME        TIMESTAMP,
   CONSTRAINT TSMP_TXTOKEN_PK PRIMARY KEY (TXTOKEN)
);
CREATE TABLE TSMP_USER
(
   USER_ID         NVARCHAR2(10)   NOT NULL,
   USER_NAME       NVARCHAR2(50),
   USER_STATUS     CHAR(1)         NOT NULL,
   USER_EMAIL      NVARCHAR2(100)  NOT NULL,
   LOGON_DATE      TIMESTAMP,
   LOGOFF_DATE     TIMESTAMP,
   UPDATE_USER     VARCHAR(255),
   UPDATE_TIME     TIMESTAMP,
   CREATE_USER     VARCHAR(255),
   CREATE_TIME     TIMESTAMP       NOT NULL,
   PWD_FAIL_TIMES  NUMBER(10)      NOT NULL,
   ORG_ID          VARCHAR(255)    DEFAULT NULL,
   USER_ALIAS      VARCHAR(30)     DEFAULT NULL,
   CONSTRAINT TSMP_USER_UQ UNIQUE (USER_NAME),
   CONSTRAINT TSMP_USER_PK PRIMARY KEY (USER_ID)
);
CREATE TABLE TSMP_VGROUP (
    VGROUP_ID VARCHAR2(10) NOT NULL,
    VGROUP_NAME VARCHAR2(150) NOT NULL,
    VGROUP_ALIAS NVARCHAR2(255),
    VGROUP_DESC VARCHAR2(1500),
    VGROUP_ACCESS VARCHAR2(255),
    SECURITY_LEVEL_ID VARCHAR2(10),
    ALLOW_DAYS INT DEFAULT 0 NOT NULL,
    ALLOW_TIMES INT DEFAULT 0 NOT NULL,
    CREATE_USER VARCHAR2(255) NOT NULL,
    CREATE_TIME TIMESTAMP NOT NULL,
    UPDATE_TIME TIMESTAMP,
    UPDATE_USER VARCHAR2(255),
    CONSTRAINT PK_TSMP_VGROUP PRIMARY KEY (VGROUP_ID)
);
CREATE TABLE TSMP_VGROUP_AUTHORITIES_MAP (
    VGROUP_ID VARCHAR2(10) NOT NULL,
    VGROUP_AUTHORITIE_ID VARCHAR2(10) NOT NULL,
    CONSTRAINT PK_TSMP_VGROUP_AUTH_MAP PRIMARY KEY (VGROUP_ID,VGROUP_AUTHORITIE_ID)
);
CREATE TABLE TSMP_VGROUP_GROUP (
    VGROUP_ID VARCHAR2(10) NOT NULL,
    GROUP_ID VARCHAR2(10) NOT NULL,
    CREATE_TIME TIMESTAMP NOT NULL,
    CONSTRAINT PK_TSMP_VGROUP_GROUP PRIMARY KEY (VGROUP_ID,GROUP_ID)
);
CREATE TABLE TSMPN_API_DETAIL (
    ID NUMBER(19) NOT NULL,
    API_MODULE_ID NUMBER(19) NOT NULL,
    API_KEY NVARCHAR2(255) NOT NULL,
    API_NAME NVARCHAR2(255) NOT NULL,
    PATH_OF_JSON NVARCHAR2(1024) NOT NULL,
    METHOD_OF_JSON NVARCHAR2(1023) NOT NULL,
    PARAMS_OF_JSON NVARCHAR2(1023) NOT NULL,
    HEADERS_OF_JSON NVARCHAR2(1023) NOT NULL,
    CONSUMES_OF_JSON NVARCHAR2(1023) NOT NULL,
    PRODUCES_OF_JSON NVARCHAR2(1023) NOT NULL,
    URL_RID NUMBER(1) NOT NULL,
    CONSTRAINT TSMPN_API_DETAIL_PK PRIMARY KEY (ID),
    CONSTRAINT TSMPN_API_DETAIL_UQ UNIQUE (API_MODULE_ID,API_KEY)
);
CREATE TABLE TSMPN_API_MODULE (
    ID NUMBER NOT NULL,
    MODULE_NAME NVARCHAR2(255) NOT NULL,
    MODULE_VERSION NVARCHAR2(255) NOT NULL,
    MODULE_APP_CLASS NVARCHAR2(255) NOT NULL,
    MODULE_BYTES BLOB NOT NULL,
    MODULE_MD5 NVARCHAR2(255) NOT NULL,
    MODULE_TYPE NVARCHAR2(255) NOT NULL,
    UPLOAD_TIME TIMESTAMP NOT NULL,
    UPLOADER_NAME NVARCHAR2(255) NOT NULL,
    STATUS_TIME TIMESTAMP,
    STATUS_USER NVARCHAR2(255),
    ACTIVE NUMBER(1) NOT NULL,
    NODE_TASK_ID NUMBER,
    ORG_ID VARCHAR2(255) DEFAULT NULL,
    TARGET_VERSION VARCHAR2(30) DEFAULT NULL,
    CONSTRAINT TSMPN_API_MODULE_PK PRIMARY KEY (ID),
    CONSTRAINT TSMPN_API_MODULE_UQ UNIQUE (MODULE_NAME,MODULE_VERSION)
);
CREATE TABLE TSMPN_NODE_TASK
(
   ID              NUMBER(19)      NOT NULL,
   TASK_SIGNATURE  VARCHAR(255)    NOT NULL,
   TASK_ID         VARCHAR(255)    NOT NULL,
   TASK_ARG        VARCHAR(4000),
   COORDINATION    VARCHAR(255)    NOT NULL,
   EXECUTE_TIME    TIMESTAMP       NOT NULL,
   NOTICE_NODE     VARCHAR(255)    NOT NULL,
   NODE            VARCHAR(255)    NOT NULL,
   NOTICE_TIME     TIMESTAMP       NOT NULL,
   CONSTRAINT TSMPN_NODE_TASK_UQ UNIQUE (TASK_SIGNATURE, TASK_ID),
   CONSTRAINT TSMPN_NODE_TASK_PK PRIMARY KEY(ID)
);
CREATE TABLE TSMPN_NODE_TASK_WORK (
    ID NUMBER(19) NOT NULL,
    NODE_TASK_ID NUMBER(19) NOT NULL,
    COMPETITIVE_ID NVARCHAR2(255) NOT NULL,
    COMPETITIVE_TIME TIMESTAMP NOT NULL,
    COMPETITIVE_NODE NVARCHAR2(255) NOT NULL,
    NODE NVARCHAR2(255) NOT NULL,
    UPDATE_TIME TIMESTAMP NOT NULL,
    SUCCESS NUMBER(1),
    ERROR_MSG NVARCHAR2(1023),
    CONSTRAINT TSMPN_NODE_TASK_WORK_PK PRIMARY KEY (ID),
    CONSTRAINT TSMPN_NODE_TASK_WORK_UQ UNIQUE (NODE_TASK_ID,COMPETITIVE_ID)
);
CREATE TABLE TSMPN_SITE (
    SITE_ID INT NOT NULL,
    SITE_CODE NVARCHAR2(30) NOT NULL,
    SITE_MEMO NCLOB,
    ACTIVE NUMBER(1) NOT NULL,
    CREATE_USER VARCHAR2(255),
    CREATE_TIME TIMESTAMP NOT NULL,
    UPDATE_USER VARCHAR2(255),
    UPDATE_TIME TIMESTAMP,
    PROTOCOL_TYPE NVARCHAR2(20) NOT NULL,
    BINDING_IP NVARCHAR2(20) NOT NULL,
    BINDING_PORT INT NOT NULL,
    APP_POOL NVARCHAR2(255) NOT NULL,
    ROOT_PATH NCLOB,
    CLR_VERSION NVARCHAR2(30),
    CONSTRAINT PK__TSMPN_DC__46564CF959B403E7 PRIMARY KEY (SITE_ID)
);
CREATE TABLE TSMPN_SITE_MODULE (
        SITE_ID int NOT NULL,
        MODULE_ID int NOT NULL,
        NODE_TASK_ID int NULL,
        CONSTRAINT PK__TSMPN_SITE__MODULE PRIMARY KEY (SITE_ID)
);
CREATE TABLE TSMPN_SITE_NODE (
NODE VARCHAR(30) NOT NULL,
SITE_ID INT NOT NULL,
NODE_TASK_ID INT,
CONSTRAINT PK__TSMPN_SITE__NODE PRIMARY KEY (NODE,SITE_ID)
);
CREATE TABLE USERS (
    USERNAME NVARCHAR2(50) NOT NULL,
    PASSWORD NVARCHAR2(60),
    ENABLED NUMBER(1) NOT NULL,
    CONSTRAINT USERS_PK PRIMARY KEY (USERNAME)
);

-- 20230906, v4, 移除多餘欄位, Kevin K
ALTER TABLE tsmp_dp_mail_log DROP COLUMN  keyword_search;
-- 20230906, v4, 增加紀錄錯誤訊息欄位, Kevin K
ALTER TABLE tsmp_dp_mail_log ADD stack_trace VARCHAR(2000);

-- 20230908, v4, 修正欄位長度與移除not null  , min
ALTER TABLE TSMP_DP_FILE MODIFY FILE_PATH NVARCHAR2(300) NULL;
ALTER TABLE TSMP_DP_REQ_ORDERM MODIFY  REQ_ORDER_NO VARCHAR2(30);
ALTER TABLE TSMP_REPORT_DATA MODIFY ORGID VARCHAR2(255) NULL;
ALTER TABLE TSMP_API_REG MODIFY API_KEY NVARCHAR2(255);

-- 20230908, 移除不用的table, Tom
DROP TABLE TSMP_REQ_LOG_HISTORY;
DROP TABLE TSMP_RES_LOG_HISTORY;

-- 20230912, Gateway IdP資料 (API), Mini Lee
CREATE TABLE dgr_gtw_idp_info_a (  
	gtw_idp_info_a_id 	NUMBER(19) NOT NULL, 					-- ID, 使用 RandomSeqLongUtil 機制產生 
	client_id 			VARCHAR(40) NOT NULL, 				-- 在 digiRunner 註冊的 client_id 
	status 				VARCHAR(1) DEFAULT ('Y') NOT NULL , 	-- 狀態 
	remark 				VARCHAR(200), 						-- 說明 
	api_method 			VARCHAR(10) NOT NULL, 				-- 登入的 API HTTP method 
	api_url 			VARCHAR(4000) NOT NULL, 			-- 登入的 API URL
	req_header 			VARCHAR(4000), 						-- 調用 API 的 Request Header 內容 
	req_body_type 		VARCHAR(1) DEFAULT ('N') NOT NULL , 	-- 調用 API 的 Request Body 類型 
	req_body 			VARCHAR(4000), 						-- 調用 API 的 Request Body 內容 
	suc_by_type 		VARCHAR(1) DEFAULT ('H') NOT NULL , 	-- 判定登入成功的類型 
	suc_by_field 		VARCHAR(200), 						-- 當 suc_by_type 為 "R",判定登入成功的 Response JSON 欄位 
	suc_by_value 		VARCHAR(200), 						-- 當 suc_by_type 為 "R",判定登入成功的 Response JSON 值
	idt_name 			VARCHAR(200), 						-- ID token 的 name 值,來源 Response JSON 欄位 
	idt_email 			VARCHAR(200), 						-- ID token 的 email 值,來源 Response JSON 欄位 
	idt_picture 		VARCHAR(200), 						-- ID token 的 picture 值,來源 Response JSON 欄位
	icon_file 			VARCHAR(4000), 						-- 登入頁圖示檔案 
	page_title 			VARCHAR(400) NOT NULL, 				-- 登入頁標題 
	create_date_time 	TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	create_user 		VARCHAR(255) DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 	TIMESTAMP, 							-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料 
	update_user 		VARCHAR(255), 						-- 更新人員 
	version 			INT DEFAULT 1, 						-- 版號 C/U時, 增量+1  
	PRIMARY KEY (gtw_idp_info_a_id)   
);

-- 20230912, Gateway IdP授權碼記錄檔, 增加欄位, Mini Lee
ALTER TABLE DGR_GTW_IDP_AUTH_CODE MODIFY API_RESP NVARCHAR2(2000);

-- 20230912, TSMP Token 歷史紀錄, 增加欄位, Mini Lee
ALTER TABLE TSMP_TOKEN_HISTORY ADD API_RESP NVARCHAR2(2000);

-- 20230914, 移除DP的table, min
DROP TABLE dp_app;
DROP TABLE dp_user;
DROP TABLE dp_file;
DROP TABLE dp_api_version;

-- 20230918, Gateway IdP Auth記錄檔主檔, 增加欄位, Mini Lee
ALTER TABLE dgr_gtw_idp_auth_m ADD redirect_uri VARCHAR2(400); 

-- 20230918, TSMP用戶端OAuth2驗證資料(Spring), 增加欄位, Mini Lee
ALTER TABLE oauth_client_details ADD web_server_redirect_uri1 NVARCHAR2(255); 
ALTER TABLE oauth_client_details ADD web_server_redirect_uri2 NVARCHAR2(255);
ALTER TABLE oauth_client_details ADD web_server_redirect_uri3 NVARCHAR2(255);
ALTER TABLE oauth_client_details ADD web_server_redirect_uri4 NVARCHAR2(255);
ALTER TABLE oauth_client_details ADD web_server_redirect_uri5 NVARCHAR2(255);

-- 20230919, 來源IP 增加填寫Hostname, 增加欄位長度 , Zoe Lee
ALTER TABLE tsmp_client_host MODIFY host_ip nvarchar2(255);

-- 20230920, TSMP API基本資料, 增加欄位 API_RELEASE_TIME, Kevin Cheng
ALTER TABLE TSMP_API ADD API_RELEASE_TIME TIMESTAMP NULL;


-- 20230926, API匯入匯出要有MOCK資料, Tom
ALTER TABLE tsmp_api_imp ADD mock_status_code CHAR(3);
ALTER TABLE tsmp_api_imp ADD mock_headers VARCHAR2(2000);
ALTER TABLE tsmp_api_imp ADD mock_body NVARCHAR2(2000);

-- 20231003 , zoe lee 增加 dgr_ac_idp_info_api 
CREATE TABLE dgr_ac_idp_info_api (
    ac_idp_info_api_id    NUMBER(19) NOT NULL,    -- ID
    status    VARCHAR2(1) DEFAULT 'Y' NOT NULL,    -- 狀態
    approval_result_mail    VARCHAR2(4000) NOT NULL,    -- 審核結果收件人,多組以逗號(,)隔開
    api_method    VARCHAR2(10) NOT NULL,    -- 登入 API 的 HTTP method
    api_url    VARCHAR2(4000) NOT NULL,    -- 登入 API 的 URL
    req_header    VARCHAR2(4000),    -- 調用 API 的 Request Header 內容
    req_body_type    VARCHAR2(1) DEFAULT 'N' NOT NULL,    -- 調用 API 的 Request Body 類型
    req_body    VARCHAR2(4000),    -- 調用 API 的 Request Body 內容
    suc_by_type    VARCHAR2(1) DEFAULT 'H' NOT NULL,    -- 判定登入成功的類型
    suc_by_field    VARCHAR2(200),    -- 當 SUC_BY_TYPE 為 "R",判定登入成功的 Response JSON 欄位
    suc_by_value    VARCHAR2(200),    -- 當 SUC_BY_TYPE 為 "R",判定登入成功的 Response JSON 值,多個以逗號分隔(不要有空格)
    idt_name    VARCHAR2(200),    -- ID token 的 name 值,對應登入 API Response JSON 欄位
    idt_email    VARCHAR2(200),    -- ID token 的 email 值,對應登入 API Response JSON 欄位
    idt_picture    VARCHAR2(200),    -- ID token 的 picture 值,對應登入 API Response JSON 欄位
    icon_file    VARCHAR2(4000),    -- 登入頁圖示檔案
    page_title    VARCHAR2(400) NOT NULL,    -- 登入頁標題
    create_date_time    TIMESTAMP,    -- 建立日期
    create_user    VARCHAR2(255) DEFAULT 'SYSTEM',    -- 建立人員
    update_date_time    TIMESTAMP,    -- 更新日期
    update_user    VARCHAR2(255),    -- 更新人員
    version    INT DEFAULT '1',    -- 版號
    keyword_search    NVARCHAR2(200),    -- LikeSearch使用
    CONSTRAINT PK_ PRIMARY KEY (ac_idp_info_api_id)
);

-- 20231003 , zoe lee 更改 dgr_ac_idp_user user_alias 型態
ALTER TABLE dgr_ac_idp_user MODIFY user_alias nvarchar2(200);

-- 20231011, rdb連線資訊, tom
CREATE TABLE dgr_rdb_connection (
    connection_name    NVARCHAR2(50) NOT NULL,    -- 名稱
    jdbc_url    VARCHAR2(200) NOT NULL,    -- 連線URL
    user_name    VARCHAR2(100) NOT NULL,    -- 帳號
    mima    VARCHAR2(500) NOT NULL,    -- MIMA
    max_pool_size    INT DEFAULT 10 NOT NULL ,    -- 最大連線數量
    connection_timeout    INT DEFAULT 30000 NOT NULL,    -- 連線取得超時設定(ms)
    idle_timeout    INT DEFAULT 600000 NOT NULL,    -- 空閒連線的存活時間(ms)
    max_lifetime    INT DEFAULT 1800000 NOT NULL,    -- 連線的最大存活時間(ms)
    data_source_property    VARCHAR2(4000),    -- DataSourceProperty的設定
    create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,      -- 建立日期
    create_user VARCHAR2(255) DEFAULT 'SYSTEM',            -- 建立人員
    update_date_time TIMESTAMP,                           -- 更新日期
    update_user VARCHAR2(255),                             -- 更新人員
    version INT DEFAULT 1,                                -- 版號
    CONSTRAINT pk_dgr_rdb_connection PRIMARY KEY(connection_name)
);

-- 20231018, 調整TSMP_API_REG的SRC_URL長度, min
ALTER TABLE TSMP_API_REG MODIFY SRC_URL NVARCHAR2(2000);

-- 20231019, DGR_WEBSITE 網站反向代理主檔, 增加欄位, Mini Lee
ALTER TABLE DGR_WEBSITE ADD AUTH VARCHAR(1) DEFAULT 'N' NOT NULL;
ALTER TABLE DGR_WEBSITE ADD SQL_INJECTION VARCHAR(1) DEFAULT 'N' NOT NULL;
ALTER TABLE DGR_WEBSITE ADD TRAFFIC VARCHAR(1) DEFAULT 'N' NOT NULL;
ALTER TABLE DGR_WEBSITE ADD XSS VARCHAR(1) DEFAULT 'N' NOT NULL;
ALTER TABLE DGR_WEBSITE ADD XXE VARCHAR(1) DEFAULT 'N' NOT NULL;
ALTER TABLE DGR_WEBSITE ADD TPS NUMBER(10) DEFAULT 0 NOT NULL;
ALTER TABLE DGR_WEBSITE ADD IGNORE_API NVARCHAR2(2000);

-- 20231020, 增加欄位長度, Mini Lee
ALTER TABLE DGR_GTW_IDP_AUTH_M MODIFY STATE VARCHAR(1000);
ALTER TABLE DGR_GTW_IDP_AUTH_CODE MODIFY AUTH_CODE VARCHAR(1000);

-- 20231030, DGR_WEBSITE 網站反向代理主檔, 增加欄位, TOM
ALTER TABLE DGR_WEBSITE ADD SHOW_LOG VARCHAR(1) DEFAULT 'N' NOT NULL;

-- 20231103, 增加欄位 ,Zoe Lee
ALTER TABLE TSMP_API_REG ADD REDIRECT_BY_IP char(1) DEFAULT 'N' NULL;
ALTER TABLE TSMP_API_REG ADD IP_FOR_REDIRECT1 nvarchar2(2000) NULL;
ALTER TABLE TSMP_API_REG ADD IP_SRC_URL1 nvarchar2(2000) NULL;
ALTER TABLE TSMP_API_REG ADD IP_FOR_REDIRECT2 nvarchar2(2000) NULL;
ALTER TABLE TSMP_API_REG ADD IP_SRC_URL2 nvarchar2(2000) NULL;
ALTER TABLE TSMP_API_REG ADD IP_FOR_REDIRECT3 nvarchar2(2000) NULL;
ALTER TABLE TSMP_API_REG ADD IP_SRC_URL3 nvarchar2(2000) NULL;
ALTER TABLE TSMP_API_REG ADD IP_FOR_REDIRECT4 nvarchar2(2000) NULL;
ALTER TABLE TSMP_API_REG ADD IP_SRC_URL4 nvarchar2(2000) NULL;
ALTER TABLE TSMP_API_REG ADD IP_FOR_REDIRECT5 nvarchar2(2000) NULL;
ALTER TABLE TSMP_API_REG ADD IP_SRC_URL5 nvarchar2(2000) NULL;

ALTER TABLE TSMP_API_REG ADD HEADER_MASK_KEY nvarchar2(2000) NULL;
ALTER TABLE TSMP_API_REG ADD HEADER_MASK_POLICY char(1) DEFAULT '0'  NULL;
ALTER TABLE TSMP_API_REG ADD HEADER_MASK_POLICY_NUM int NULL;
ALTER TABLE TSMP_API_REG ADD HEADER_MASK_POLICY_SYMBOL varchar(10)  NULL;

ALTER TABLE TSMP_API_REG ADD BODY_MASK_KEYWORD nvarchar2(2000) NULL;
ALTER TABLE TSMP_API_REG ADD BODY_MASK_POLICY char(1) DEFAULT '0' NULL;
ALTER TABLE TSMP_API_REG ADD BODY_MASK_POLICY_NUM int NULL;
ALTER TABLE TSMP_API_REG ADD BODY_MASK_POLICY_SYMBOL varchar(10) NULL;

-- 20231108, 增加欄位 ,Zoe Lee
ALTER TABLE tsmp_api_imp ADD redirect_by_ip char(1) DEFAULT 'N' NULL;
ALTER TABLE tsmp_api_imp ADD ip_for_redirect1 nvarchar2(2000) NULL;
ALTER TABLE tsmp_api_imp ADD ip_src_url1 nvarchar2(2000) NULL;
ALTER TABLE tsmp_api_imp ADD ip_for_redirect2 nvarchar2(2000) NULL;
ALTER TABLE tsmp_api_imp ADD ip_src_url2 nvarchar2(2000) NULL;
ALTER TABLE tsmp_api_imp ADD ip_for_redirect3 nvarchar2(2000) NULL;
ALTER TABLE tsmp_api_imp ADD ip_src_url3 nvarchar2(2000) NULL;
ALTER TABLE tsmp_api_imp ADD ip_for_redirect4 nvarchar2(2000) NULL;
ALTER TABLE tsmp_api_imp ADD ip_src_url4 nvarchar2(2000) NULL;
ALTER TABLE tsmp_api_imp ADD ip_for_redirect5 nvarchar2(2000) NULL;
ALTER TABLE tsmp_api_imp ADD ip_src_url5 nvarchar2(2000) NULL;

ALTER TABLE tsmp_api_imp ADD header_mask_key nvarchar2(2000) NULL;
ALTER TABLE tsmp_api_imp ADD header_mask_policy char(1) DEFAULT '0'  NULL;
ALTER TABLE tsmp_api_imp ADD header_mask_policy_num NUMBER(4) NULL;
ALTER TABLE tsmp_api_imp ADD header_mask_policy_symbol varchar(10)  NULL;

ALTER TABLE tsmp_api_imp ADD body_mask_keyword nvarchar2(2000) NULL;
ALTER TABLE tsmp_api_imp ADD body_mask_policy char(1) DEFAULT '0' NULL;
ALTER TABLE tsmp_api_imp ADD body_mask_policy_num NUMBER(4) NULL;
ALTER TABLE tsmp_api_imp ADD body_mask_policy_symbol varchar(10) NULL;

-- 20231110, Gateway IdP Auth記錄檔主檔	, 增加欄位, Mini Lee
ALTER TABLE DGR_GTW_IDP_AUTH_M ADD CODE_CHALLENGE NVARCHAR2(1000);
ALTER TABLE DGR_GTW_IDP_AUTH_M ADD CODE_CHALLENGE_METHOD VARCHAR(10);
 
-- 20231110, Gateway IdP授權碼記錄檔, 增加欄位, Mini Lee
ALTER TABLE DGR_GTW_IDP_AUTH_CODE ADD STATE VARCHAR(1000);

-- 20231117 刪除 dgr_gtw_idp_info_j, Mini Lee
DROP TABLE dgr_gtw_idp_info_j;

-- 20231117, Gateway IdP資料 (JDBC), Mini Lee
CREATE TABLE dgr_gtw_idp_info_jdbc (  
	GTW_IDP_INFO_JDBC_ID NUMBER(19) NOT NULL, 		-- ID 
	CLIENT_ID 			VARCHAR(40) NOT NULL, 	-- digiRunner 的 client_id 
	STATUS 				VARCHAR(1) DEFAULT 'Y' NOT NULL , -- 狀態 
	REMARK 				NVARCHAR2(200), 			-- 說明 
	CONNECTION_NAME 	NVARCHAR2(50) NOT NULL, 	-- RDB連線資訊的名稱 
	SQL_PTMT 			NVARCHAR2(1000) NOT NULL, -- 查詢RDB的SQL(Prepare Statement) 
	SQL_PARAMS	 		NVARCHAR2(1000) NOT NULL, -- 查詢RDB的SQL參數 
	USER_MIMA_ALG 		VARCHAR(40) NOT NULL, 	-- RDB存放密碼所使用的演算法 
	USER_MIMA_COL_NAME 	VARCHAR(200) NOT NULL, 	-- RDB的密碼欄位名稱 
	IDT_SUB 			VARCHAR(200) NOT NULL, 	-- ID token 的 sub(唯一值) 值,對應RDB的欄位 
	IDT_NAME 			VARCHAR(200), 			-- ID token 的 name 值,對應RDB的欄位 
	IDT_EMAIL 			VARCHAR(200), 			-- ID token 的 email 值,對應RDB的欄位 
	IDT_PICTURE 		VARCHAR(200), 			-- ID token 的 picture 值,對應RDB的欄位 
	ICON_FILE 			VARCHAR(4000), 			-- 登入頁圖示檔案 
	PAGE_TITLE 			NVARCHAR2(400) NOT NULL, -- 登入頁標題
	CREATE_DATE_TIME 	TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人
	CREATE_USER 		NVARCHAR2(1000) DEFAULT 'SYSTEM', -- 建立人員 
	UPDATE_DATE_TIME 	TIMESTAMP, 				-- 更新日期 表示最後Update的人
	UPDATE_USER 		NVARCHAR2(1000), 		-- 更新人員 
	VERSION 			   NUMBER(10) DEFAULT 1,  		-- 版號 C/U時, 增量+1 
	PRIMARY KEY (GTW_IDP_INFO_JDBC_ID)    
);

-- 20231121, 檢查Oracle DDL資料型態與長度, min
ALTER TABLE TSMP_ALERT MODIFY CREATE_USER NVARCHAR2(1000);
ALTER TABLE TSMP_ALERT MODIFY UPDATE_USER NVARCHAR2(1000);
ALTER TABLE GROUP_MEMBERS MODIFY USERNAME NVARCHAR2(50);
ALTER TABLE TSMP_API_IMP MODIFY SRC_URL NVARCHAR2(2000);
ALTER TABLE TSMP_API_IMP MODIFY METHOD_OF_JSON NVARCHAR2(200);

--20231123, 增加欄位 ,Zoe Lee
ALTER TABLE TSMP_API ADD LABEL1 nvarchar2(20) NULL;
ALTER TABLE TSMP_API ADD LABEL2 nvarchar2(20) NULL;
ALTER TABLE TSMP_API ADD LABEL3 nvarchar2(20) NULL;
ALTER TABLE TSMP_API ADD LABEL4 nvarchar2(20) NULL;
ALTER TABLE TSMP_API ADD LABEL5 nvarchar2(20) NULL;

ALTER TABLE tsmp_api_imp ADD label1 nvarchar2(20) NULL;
ALTER TABLE tsmp_api_imp ADD label2 nvarchar2(20) NULL;
ALTER TABLE tsmp_api_imp ADD label3 nvarchar2(20) NULL;
ALTER TABLE tsmp_api_imp ADD label4 nvarchar2(20) NULL;
ALTER TABLE tsmp_api_imp ADD label5 nvarchar2(20) NULL;

--20231130, TSMP_API_REG.SRC_URL 拿掉NOT NULL ,Zoe Lee
ALTER TABLE TSMP_API_REG MODIFY SRC_URL nvarchar2(2000) NULL;

--20231201, 增加固定快取時間欄位, Tom
ALTER TABLE TSMP_API ADD FIXED_CACHE_TIME NUMBER(10) DEFAULT 0 NOT NULL;
ALTER TABLE tsmp_api_imp ADD api_cache_flag CHAR(1) DEFAULT '1' NOT NULL;
ALTER TABLE tsmp_api_imp ADD fixed_cache_time NUMBER(10) DEFAULT 0 NOT NULL;

-- 20231207, X-Api-Key資料, Mini Lee
CREATE TABLE DGR_X_API_KEY (  
	API_KEY_ID 			NUMBER(19) NOT NULL, 		-- ID 
	CLIENT_ID 			VARCHAR(40) NOT NULL, 	-- digiRunner 的 client_id 
	API_KEY_ALIAS 		NVARCHAR2(100) NOT NULL, -- X-Api-Key 別名 
	EFFECTIVE_AT 		NUMBER(19) NOT NULL, 		-- 生效日期 
	EXPIRED_AT 			NUMBER(19) NOT NULL, 		-- 到期日期 
	API_KEY 			VARCHAR(100), 			-- X-Api-Key 的值 	
	API_KEY_MASK 		VARCHAR(100) NOT NULL, 	-- X-Api-Key 經過遮罩的值 
	API_KEY_EN 			VARCHAR(100) NOT NULL, 	-- X-Api-Key 經過SHA256 的值 
	CREATE_DATE_TIME 	TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	CREATE_USER 		NVARCHAR2(1000) DEFAULT 'SYSTEM', -- 建立人員 
	UPDATE_DATE_TIME 	TIMESTAMP, 				-- 更新日期 表示最後Update的人, 日期時間
	UPDATE_USER 		NVARCHAR2(1000), 		-- 更新人員 
	VERSION 			NUMBER(10) DEFAULT 1, 			-- 版號 C/U時, 增量+1  
	PRIMARY KEY (API_KEY_ID)    
);  

-- 20231207, X-Api-Key與群組關係, Mini Lee
CREATE TABLE DGR_X_API_KEY_MAP (  
	API_KEY_MAP_ID 		NUMBER(19) NOT NULL, 		-- ID 
	REF_API_KEY_ID 		NUMBER(19) NOT NULL, 		-- Master PK 
	GROUP_ID 			NVARCHAR2(10) NOT NULL, 	-- 群組 ID 
	CREATE_DATE_TIME 	TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 建立日期 
	CREATE_USER 		NVARCHAR2(1000) DEFAULT 'SYSTEM', -- 建立人員 
	UPDATE_DATE_TIME 	TIMESTAMP, 				-- 更新日期 
	UPDATE_USER 		NVARCHAR2(1000), 		-- 更新人員 
	VERSION 			NUMBER(10) DEFAULT 1, 			-- 版號
	PRIMARY KEY (API_KEY_MAP_ID)  
);

-- 20231212, 增加欄位 DP_CLIENT_SECRET, Kevin Cheng
ALTER TABLE tsmp_client ADD dp_client_secret VARCHAR2(128);

-- 20231222, 調整欄位 dp_client_secret 為 dp_client_entry, 資料型態 nvarchar, Kevin Cheng
ALTER TABLE tsmp_client RENAME COLUMN dp_client_secret TO dp_client_entry;
ALTER TABLE tsmp_client MODIFY dp_client_entry NVARCHAR2(128);

-- 20231228, 調整欄位 dp_client_secret 長度從 128 到 1000, Kevin Cheng
ALTER TABLE tsmp_client MODIFY dp_client_entry NVARCHAR2(1000);

-- 20240108, TSMP外部API註冊資料, 增加欄位, Mini Lee
ALTER TABLE TSMP_API_REG ADD FAIL_DISCOVERY_POLICY VARCHAR(1) DEFAULT '0';
ALTER TABLE TSMP_API_REG ADD FAIL_HANDLE_POLICY VARCHAR(1) DEFAULT '0';

-- 20240108, TSMP API 匯入資料, 增加欄位, Mini Lee
ALTER TABLE tsmp_api_imp ADD fail_discovery_policy VARCHAR(1) DEFAULT '0';
ALTER TABLE tsmp_api_imp ADD fail_handle_policy VARCHAR(1) DEFAULT '0';

-- 20240122,TSMP 功能維護資料, 增加欄位  ,Zoe Lee
ALTER TABLE TSMP_FUNC ADD FUNC_TYPE char(1) DEFAULT '1' ;

-- 20240306, 用戶端匯出/入, Tom
CREATE TABLE DGR_IMPORT_CLIENT_RELATED_TEMP (  
	TEMP_ID 		NUMBER(19) NOT NULL, 		-- ID 
	IMPORT_CLIENT_RELATED 	BLOB NOT NULL, 	-- 匯入的資料
	ANALYZE_CLIENT_RELATED 	BLOB NOT NULL, 	-- 分析的資料
	CREATE_DATE_TIME 	TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	CREATE_USER 		NVARCHAR2(1000) DEFAULT 'SYSTEM', -- 建立人員 
	UPDATE_DATE_TIME 	TIMESTAMP, 				-- 更新日期 表示最後Update的人, 日期時間
	UPDATE_USER 		NVARCHAR2(1000), 		-- 更新人員 
	VERSION 			INT DEFAULT 1, 			-- 版號 C/U時, 增量+1  
	CONSTRAINT DGR_IMPORT_CLIENT_RELATED_TEMP_PK PRIMARY KEY (TEMP_ID)  
); 

-- 20240402, 增加api狀態, Tom
ALTER TABLE tsmp_api_imp ADD api_status CHAR(1) DEFAULT '2' NOT NULL;

-- 20240402,新增PUBLIC_FLAG,API_RELEASE_TIME匯入欄位, Webber
ALTER TABLE tsmp_api_imp ADD public_flag CHAR(1) NULL;
ALTER TABLE tsmp_api_imp ADD api_release_time TIMESTAMP NULL;

-- 20240429 , dgr_web_socket_mapping 新增欄位 ,Zoe Lee
ALTER TABLE dgr_web_socket_mapping ADD auth varchar(1) DEFAULT 'N' NOT NULL;

-- 20240430, 添加兩個欄位用於預定DP上下架功能, Kevin Cheng
ALTER TABLE TSMP_API ADD SCHEDULED_LAUNCH_DATE NUMBER(19) DEFAULT 0;
ALTER TABLE TSMP_API ADD SCHEDULED_REMOVAL_DATE NUMBER(19) DEFAULT 0;

-- 20240516, 添加兩個欄位用於預定DGR API啟用停用功能, Kevin Cheng
ALTER TABLE TSMP_API ADD ENABLE_SCHEDULED_DATE NUMBER(19) DEFAULT 0;
ALTER TABLE TSMP_API ADD DISABLE_SCHEDULED_DATE NUMBER(19) DEFAULT 0;

-- 20240603, TSMP_API_IMP API匯入匯出添加四個欄位,兩個預定DP上下架功能, 兩個預定DGR API啟用停用功能, Webber Luo
ALTER TABLE TSMP_API_IMP ADD SCHEDULED_LAUNCH_DATE NUMBER(19) DEFAULT 0;
ALTER TABLE TSMP_API_IMP ADD SCHEDULED_REMOVAL_DATE NUMBER(19) DEFAULT 0;
ALTER TABLE TSMP_API_IMP ADD ENABLE_SCHEDULED_DATE NUMBER(19) DEFAULT 0;
ALTER TABLE TSMP_API_IMP ADD DISABLE_SCHEDULED_DATE NUMBER(19) DEFAULT 0;

-- 20240625, 這是DP的dp_app TABLE 因為DGR的用戶端匯出入有用到所以移動到這, Tom
---- start DP的dp_app TABLE ----
-- 20230407, v4 入口網(DP)的Application	, Kevin Cheng
CREATE TABLE dp_app (
	dp_application_id NUMBER(19) NOT NULL,                         -- ID
	application_name NVARCHAR2(50) NOT NULL,                     -- Application名稱
	application_desc NVARCHAR2(500),                             -- Application說明
	client_id VARCHAR2(40) NOT NULL,                            -- CLIENT_ID
	open_apikey_id NUMBER(19),                                     -- 
	open_apikey_status VARCHAR2(1),                             -- DGRK狀態
	user_name NVARCHAR2(400) NOT NULL,                          -- 使用者名稱(視IdP類型決定)
	id_token_jwtstr NVARCHAR2(2000) NOT NULL,                    -- IdP ID Token 的 JWT
	create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,      -- 建立日期
	create_user NVARCHAR2(1000) DEFAULT 'SYSTEM',                 -- 建立人員
	update_date_time TIMESTAMP,                                -- 更新日期
	update_user NVARCHAR2(1000),                                  -- 更新人員
	version INT DEFAULT 1,                                     -- 版號
	KEYWORD_SEARCH NVARCHAR2(600),
	PRIMARY KEY (dp_application_id)
);

-- 20230515, v4 入口網(DP)的Application	, 刪除欄位 , Kevin Cheng
ALTER TABLE dp_app DROP COLUMN open_apikey_status;

-- 20231105, v4 修改 dp_app 欄位型態 , min (20240625補上的)
ALTER TABLE dp_app MODIFY application_name NVARCHAR2(50);
ALTER TABLE dp_app MODIFY application_desc NVARCHAR2(500);

-- 20231123, v4 入口網(DP) DP APP 新增 ISS 欄位, Kevin Cheng
ALTER TABLE dp_app ADD iss VARCHAR(4000) DEFAULT 'NULL' NOT NULL;

-- 20231128, v4 入口網(DP) 改欄位名, Kevin Cheng
ALTER TABLE dp_app RENAME COLUMN user_name TO dp_user_name;

-- 20231228, 移除 open_apikey_id 欄位, jhmin
ALTER TABLE dp_app DROP COLUMN open_apikey_id;
---- end DP的dp_app TABLE ----

-- 20240718 , 第三方 AC IDP INFO , Kevin Cheng
CREATE TABLE dgr_ac_idp_info_cus
(
    ac_idp_info_cus_id    NUMBER(19)                    NOT NULL,    -- ID
    ac_idp_info_cus_name  NVARCHAR2(200),                            -- 第三方可識別名稱
    cus_status            VARCHAR(1)      DEFAULT ('Y') NOT NULL,    -- Cus 狀態
    cus_login_url         VARCHAR(4000)                 NOT NULL,    -- 第三方前端頁面 URL
    cus_backend_login_url VARCHAR(4000)                 NOT NULL,    -- 第三方後端 URL
    cus_user_data_url     VARCHAR(4000)                 NOT NULL,    -- 第三方使用者資料 URL
    create_date_time      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP, -- 建立日期
    create_user           NVARCHAR2(1000) DEFAULT 'SYSTEM',          -- 建立人員
    update_date_time      TIMESTAMP,                                 -- 更新日期
    update_user           NVARCHAR2(1000),                           -- 更新人員
    version               NUMBER(10)      DEFAULT 1,                 -- 版號
    CONSTRAINT dgr_ac_idp_info_cus_pk PRIMARY KEY (ac_idp_info_cus_id)
);

-- 20240911 , DGR_GTW_IDP_INFO_A  ADD COLUMN , Zoe Lee
ALTER TABLE DGR_GTW_IDP_INFO_A ADD  IDT_LIGHT_ID VARCHAR(200);
ALTER TABLE DGR_GTW_IDP_INFO_A ADD  IDT_ROLE_NAME NVARCHAR2(200);
-- 20240911 , DGR_GTW_IDP_AUTH_CODE  ADD COLUMN , Zoe Lee
ALTER TABLE DGR_GTW_IDP_AUTH_CODE ADD  USER_LIGHT_ID VARCHAR(200);
ALTER TABLE DGR_GTW_IDP_AUTH_CODE ADD  USER_ROLE_NAME NVARCHAR2(200);
-- 20240902 , CUS GATE IDP INFO , Kevin Cheng
CREATE TABLE dgr_gtw_idp_info_cus
(
    gtw_idp_info_cus_id NUMBER(19)                  NOT NULL,      -- ID
    client_id           VARCHAR(40)                 NOT NULL,      -- digiRunner 的 client_id
    status              VARCHAR(1)      DEFAULT 'Y' NOT NULL,      -- 狀態
    cus_login_url       VARCHAR(4000)               NOT NULL,      -- CUS 登入 URL
    cus_user_data_url   VARCHAR(4000)               NOT NULL,      -- CUS 使用者資料 URL
    icon_file           VARCHAR(4000),                             -- 登入頁圖示檔案
    page_title          NVARCHAR2(400),                            -- 登入頁標題
    create_date_time    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP, -- 建立日期
    create_user         NVARCHAR2(1000) DEFAULT 'SYSTEM',          -- 建立人員
    update_date_time    TIMESTAMP,                                 -- 更新日期
    update_user         NVARCHAR2(1000),                           -- 更新人員
    version             INT             DEFAULT 1,                 -- 版號
    CONSTRAINT gtw_idp_info_cus_pk PRIMARY KEY (gtw_idp_info_cus_id)
);

-- 20241007, AC IdP授權碼記錄檔, 增加欄位, Mini Lee
Alter TABLE dgr_ac_idp_auth_code ADD api_resp NVARCHAR2(2000);

-- 20241022 , DGR_BOT_DETECTION , Kevin Cheng
CREATE TABLE dgr_bot_detection (
    bot_detection_id   NUMBER          NOT NULL,                -- ID
    bot_detection_rule VARCHAR(4000)   NOT NULL,                -- 規則
    type               VARCHAR(1)       DEFAULT 'W' NOT NULL,    -- 名單種類
    create_date_time   TIMESTAMP        DEFAULT CURRENT_TIMESTAMP, -- 建立日期
    create_user        NVARCHAR2(1000)  DEFAULT 'SYSTEM',        -- 建立人員
    update_date_time   TIMESTAMP,                                -- 更新日期
    update_user        NVARCHAR2(1000),                           -- 更新人員
    version            INT             DEFAULT 1,               -- 版號
    CONSTRAINT BOT_DETECTION_PK PRIMARY KEY (bot_detection_id)
);

-- 20250120 , TSMP Token 歷史紀錄, Mini Lee
-- 1. 新增新欄位
ALTER TABLE tsmp_token_history ADD api_resp_new CLOB;
ALTER TABLE dgr_ac_idp_auth_code ADD api_resp_new CLOB;
ALTER TABLE dgr_gtw_idp_auth_code ADD api_resp_new CLOB;

-- 2. 分批更新資料（每次處理 1000 筆）
DECLARE
    v_total NUMBER;
    v_processed NUMBER := 0;
    v_batch_size NUMBER := 1000;
BEGIN
    -- 處理 tsmp_token_history
    SELECT COUNT(*) INTO v_total FROM tsmp_token_history;
    WHILE v_processed < v_total LOOP
            UPDATE tsmp_token_history
            SET api_resp_new = api_resp
            WHERE ROWID IN (
                SELECT ROWID FROM tsmp_token_history
                WHERE api_resp_new IS NULL
                  AND ROWNUM <= v_batch_size
            );
            v_processed := v_processed + SQL%ROWCOUNT;
            COMMIT;
        END LOOP;

    -- 重設計數器
    v_processed := 0;

    -- 處理 dgr_ac_idp_auth_code
    SELECT COUNT(*) INTO v_total FROM dgr_ac_idp_auth_code;
    WHILE v_processed < v_total LOOP
            UPDATE dgr_ac_idp_auth_code
            SET api_resp_new = api_resp
            WHERE ROWID IN (
                SELECT ROWID FROM dgr_ac_idp_auth_code
                WHERE api_resp_new IS NULL
                  AND ROWNUM <= v_batch_size
            );
            v_processed := v_processed + SQL%ROWCOUNT;
            COMMIT;
        END LOOP;

    -- 重設計數器
    v_processed := 0;

    -- 處理 dgr_gtw_idp_auth_code
    SELECT COUNT(*) INTO v_total FROM dgr_gtw_idp_auth_code;
    WHILE v_processed < v_total LOOP
            UPDATE dgr_gtw_idp_auth_code
            SET api_resp_new = api_resp
            WHERE ROWID IN (
                SELECT ROWID FROM dgr_gtw_idp_auth_code
                WHERE api_resp_new IS NULL
                  AND ROWNUM <= v_batch_size
            );
            v_processed := v_processed + SQL%ROWCOUNT;
            COMMIT;
        END LOOP;
END;


-- 3. 驗證資料是否正確複製
SELECT COUNT(*) FROM tsmp_token_history WHERE api_resp_new IS NULL;
SELECT COUNT(*) FROM dgr_ac_idp_auth_code WHERE api_resp_new IS NULL;
SELECT COUNT(*) FROM dgr_gtw_idp_auth_code WHERE api_resp_new IS NULL;

-- 4. 如果確認無誤，刪除舊欄位
ALTER TABLE tsmp_token_history DROP COLUMN api_resp;
ALTER TABLE dgr_ac_idp_auth_code DROP COLUMN api_resp;
ALTER TABLE dgr_gtw_idp_auth_code DROP COLUMN api_resp;

-- 5. 重新命名新欄位
ALTER TABLE tsmp_token_history RENAME COLUMN api_resp_new TO api_resp;
ALTER TABLE dgr_ac_idp_auth_code RENAME COLUMN api_resp_new TO api_resp;
ALTER TABLE dgr_gtw_idp_auth_code RENAME COLUMN api_resp_new TO api_resp;

-- 20250203, dashboard相關table建立index(若有資料存在可能要執行一段時間), tom
CREATE INDEX idx_tsmp_req_log ON tsmp_req_log(rtime);
CREATE INDEX idx_dgr_dashboard_es_log ON dgr_dashboard_es_log(rtime);
CREATE INDEX idx_tsmp_req_res_log_history ON tsmp_req_res_log_history(rtime);

-- 20250213, 增加欄位長度, Zoe Lee
ALTER TABLE dgr_rdb_connection MODIFY mima VARCHAR2(2000);

-- 20250227, DGR_WEBHOOK_NOTIFY , Webber Luo
CREATE TABLE dgr_webhook_notify (
    webhook_notify_id    NUMBER(19,0)         NOT NULL,    -- ID
    notify_name          VARCHAR2(100)        NOT NULL,    -- 通知名稱
    notify_type          VARCHAR2(100)        NOT NULL,    -- 通知種類
    enable               VARCHAR2(1)          NOT NULL,    -- 啟用=Y / 停用=N
    message              VARCHAR2(2000),                   -- 發送內容
    payload_flag         VARCHAR2(1)          DEFAULT '0', --
    create_date_time     DATE                 DEFAULT SYSDATE,        -- 建立日期
    create_user          VARCHAR2(1000)       DEFAULT 'SYSTEM',      -- 建立人員
    update_date_time     DATE,                                       -- 更新日期
    update_user          VARCHAR2(1000),                             -- 更新人員
    version              NUMBER(10,0)         DEFAULT 1,             -- 版號
    CONSTRAINT pk_webhook_notify PRIMARY KEY(webhook_notify_id)
);

-- 20250227, DGR_WEBHOOK_NOTIFY_FIELD , Webber Luo
CREATE TABLE dgr_webhook_notify_field (
    webhook_notify_field_id  NUMBER(19,0)     NOT NULL,    -- ID
    webhook_notify_id        NUMBER(19,0)     NOT NULL,    -- 通知ID
    field_key                VARCHAR2(100)    NOT NULL,    -- KEY
    field_value              VARCHAR2(2000)   NOT NULL,    -- VALUE
    field_type               VARCHAR2(1)      DEFAULT '0',    -- TYPE
    mapping_url              VARCHAR2(2000),                          -- URL
    create_date_time         DATE             DEFAULT SYSDATE,        -- 建立日期
    create_user              VARCHAR2(1000)   DEFAULT 'SYSTEM',      -- 建立人員
    update_date_time         DATE,                                   -- 更新日期
    update_user              VARCHAR2(1000),                         -- 更新人員
    version                  NUMBER(10,0)     DEFAULT 1,             -- 版號
    CONSTRAINT pk_webhook_notify_field PRIMARY KEY(webhook_notify_field_id)
);

-- 20250227, DGR_WEBHOOK_NOTIFY_LOG , Webber Luo
CREATE TABLE DGR_WEBHOOK_NOTIFY_LOG (
    webhook_notify_log_id    NUMBER(19,0)     NOT NULL,    -- ID
    webhook_notify_id        NUMBER(19,0)     NOT NULL,    -- WEBHOOK_NOTIFY_ID
    client_id                VARCHAR2(40)     NOT NULL,    -- 用戶端
    content                  VARCHAR2(2000),               -- 發送內容
    remark                   VARCHAR2(2000),               -- 備註
    create_date_time         DATE             DEFAULT SYSDATE,        -- 建立日期
    create_user              VARCHAR2(1000)   DEFAULT 'SYSTEM',      -- 建立人員
    update_date_time         DATE,                                   -- 更新日期
    update_user              VARCHAR2(1000),                         -- 更新人員
    version                  NUMBER(10,0)     DEFAULT 1,             -- 版號
    CONSTRAINT pk_webhook_notify_log PRIMARY KEY(webhook_notify_log_id)
);

-- 20250227, DGR_WEBHOOK_API_MAP , Webber Luo
CREATE TABLE dgr_webhook_api_map (
    webhook_api_map_id       NUMBER(19,0)     NOT NULL,    -- ID
    api_key                  VARCHAR2(255)    NOT NULL,    -- API代碼
    module_name              VARCHAR2(150)    NOT NULL,    -- API_MODULE
    webhook_notify_id        NUMBER(19,0)     NOT NULL,    -- WEBHOOK_NOTIFY_I
    create_date_time         DATE             DEFAULT SYSDATE,        -- 建立日期
    create_user              VARCHAR2(1000)   DEFAULT 'SYSTEM',      -- 建立人員
    update_date_time         DATE,                                   -- 更新日期
    update_user              VARCHAR2(1000),                         -- 更新人員
    version                  NUMBER(10,0)     DEFAULT 1,             -- 版號
    CONSTRAINT pk_webhook_api_map PRIMARY KEY(webhook_api_map_id)
);

-- 20250331, DGR_GRPCPROXY_MAP, Alvin Chiu
CREATE TABLE dgr_grpcproxy_map
(
    grpcproxy_map_id          NUMBER(19, 0) NOT NULL,               -- ID
    service_name              VARCHAR2(255) NOT NULL,               -- 目標服務名稱
    proxy_hostname            VARCHAR2(255) NOT NULL,               -- 轉發位置
    target_hostname           VARCHAR2(255) NOT NULL,               -- 目標服務位置
    target_port               NUMBER(10, 0) NOT NULL,               -- 目標服務PORT
    connect_timeout_ms        NUMBER(10, 0) DEFAULT 5000,  -- 連線超時毫秒
    send_timeout_ms           NUMBER(10, 0) DEFAULT 10000, -- 發送超時毫秒
    read_timeout_ms           NUMBER(10, 0) DEFAULT 30000, -- 讀取超時毫秒
    secure_mode               VARCHAR2(10)   DEFAULT 'AUTO',        -- TLS安全模式 (AUTO, SECURE, PLAINTEXT)
    server_cert_content       CLOB,                                 -- X509憑證內容 (PEM格式)
    server_key_content        CLOB,                                 -- 私鑰內容 (PEM格式)
    auto_trust_upstream_certs VARCHAR2(1)    DEFAULT 'N',           -- 是否自動信任上游憑證
    trusted_certs_content     CLOB,                                 -- 受信任的CA憑證內容 (PEM格式)
    enable                    VARCHAR2(1)    DEFAULT 'N',           -- 啟用/停用
    create_date_time          DATE           DEFAULT SYSDATE,       -- 建立日期
    create_user               VARCHAR2(1000) DEFAULT 'SYSTEM',      -- 建立人員
    update_date_time          DATE,                                 -- 更新日期
    update_user               VARCHAR2(1000),                       -- 更新人員
    version                   NUMBER(10, 0)  DEFAULT 1,             -- 版號
    CONSTRAINT pk_grpcproxy_map PRIMARY KEY (grpcproxy_map_id)
);

-- 20250812 , DGR_MTLS_CLIENT_CERT , Zoe Lee
CREATE TABLE dgr_mtls_client_cert (
    dgr_mtls_client_cert_id    NUMBER(19) NOT NULL,    -- ID
    host    VARCHAR(255) NOT NULL,    -- 主機
    port    NUMBER(10) NOT NULL,    -- 通訊埠
    root_ca    VARCHAR(4000) NOT NULL,    -- 根憑證
    client_cert    VARCHAR(4000) NOT NULL,    -- 客戶端憑證
    client_key    VARCHAR(4000) NOT NULL,    -- 客戶端金鑰
    key_mima    NVARCHAR2(2000),    -- 客戶端憑證密碼
    remark    NVARCHAR2(200),    -- 備註
    enable    VARCHAR(1) NOT NULL,    -- 啟用=Y / 停用=N
    root_ca_expire_date    TIMESTAMP NOT NULL,    -- ROOT_CA 的到期時間
    crt_expire_date    TIMESTAMP NOT NULL,    -- CLIENT_CERT 的到期時間
    create_date_time    TIMESTAMP,    -- 建立日期
    create_user    NVARCHAR2(1000) DEFAULT 'SYSTEM',    -- 建立人員
    update_date_time    TIMESTAMP,    -- 更新日期
    update_user    NVARCHAR2(1000),    -- 更新人員
    version    NUMBER(10) DEFAULT 1,    -- 版號
    CONSTRAINT PK_DGR_MTLS_CLIENT_CERT PRIMARY KEY (dgr_mtls_client_cert_id)
);
-- 20250801 , add column , Zoe Lee
ALTER TABLE TSMP_API_IMP ADD NOTIFY_NAME_LIST VARCHAR2(2000);

-- 20250812 , 檢查並建立 dgr_ai_provider 表 , Vulcan
CREATE TABLE dgr_ai_provider (
    ai_provider_id              NUMBER(19)       NOT NULL,                       -- ID
    ai_provider_name            VARCHAR2(1700)   NOT NULL,                       -- 供應商名稱
    ai_provider_alias           CLOB             NOT NULL,                       -- 任意名稱
    ai_model                    VARCHAR2(1700)   NOT NULL,                       -- LLM 模型名稱
    generate_api                CLOB             NOT NULL,                       -- 此模型生成內容 API URL
    count_token_api             CLOB             NOT NULL,                       -- 此模型計算 TOKEN API URL
    create_date_time            TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,      -- 建立日期
    create_user                 NVARCHAR2(1000)  DEFAULT 'SYSTEM',              -- 建立人員
    update_date_time            TIMESTAMP,                                       -- 更新日期
    update_user                 NVARCHAR2(1000),                                -- 更新人員
    version                     NUMBER(10)       DEFAULT 1,                      -- 版號
    ai_provider_enable          VARCHAR2(1)      DEFAULT 'Y' NOT NULL,           -- Y = 啟用, N = 停用
    CONSTRAINT PK_dgr_ai_provider PRIMARY KEY(ai_provider_id)
);

-- 20250812 , 檢查並建立索引 , Vulcan
CREATE INDEX index_dgr_ai_provider_ai_provider_name_ai_provider ON dgr_ai_provider (ai_provider_name);

CREATE INDEX index_dgr_ai_provider_ai_provider_name_ai_model ON dgr_ai_provider (ai_model);

-- 20250812 , 檢查並建立 dgr_ai_apikey 表 , Vulcan
CREATE TABLE dgr_ai_apikey (
    ai_apikey_id                NUMBER(19)       NOT NULL,                   -- ID
    ai_apikey_name              VARCHAR2(1000)   NOT NULL,                   -- 此 APIKEY 名稱 (任意命名)
    ai_provider_id              NUMBER(19),                                  -- AI 供應商 ID
    ai_apikey_code              VARCHAR2(500)    NOT NULL,                   -- API KEY CODE 內容
    usage_limit_input_token     NUMBER(19)       DEFAULT 0 NOT NULL,         -- 此 APIKEY 使用 input token 上限 0 = 不設限
    usage_limit_output_token    NUMBER(19)       DEFAULT 0 NOT NULL,         -- 此 APIKEY 使用 output token 上限 0 = 不設限
    usage_input_token_count     NUMBER(19)       DEFAULT 0 NOT NULL,         -- 此 APIKEY 使用 input token 總數
    usage_output_token_count    NUMBER(19)       DEFAULT 0 NOT NULL,         -- 此 APIKEY 使用 output token 總數
    usage_limit_policy          VARCHAR2(500)    DEFAULT 'REJECT' NOT NULL,  -- 當 input / output 達到上限時的執行策略
    ai_apikey_enable            VARCHAR2(1)      DEFAULT 'Y' NOT NULL,       -- Y = 啟用, N = 停用
    create_date_time            TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,  -- 建立日期
    create_user                 NVARCHAR2(1000)  DEFAULT 'SYSTEM',           -- 建立人員
    update_date_time            TIMESTAMP,                                   -- 更新日期
    update_user                 NVARCHAR2(1000),                             -- 更新人員
    version                     NUMBER(10)       DEFAULT 1,                  -- 版號
    CONSTRAINT PK_dgr_ai_apikey PRIMARY KEY(ai_apikey_id)
);

-- 20250812 , 檢查並建立索引 , Vulcan
CREATE INDEX index_dgr_ai_apikey_ai_provider_id ON dgr_ai_apikey (ai_provider_id);

-- 20250812 , 檢查並建立 dgr_ai_apikey_usage 表 , Vulcan
CREATE TABLE dgr_ai_apikey_usage (
    ai_apikey_usage_id          NUMBER(19)       NOT NULL,                   -- ID
    ai_apikey_consumer_type     VARCHAR2(1000),                              -- APIKEY 使用者類型 user = 個人使用者, client = client
    ai_apikey_consumer_id       VARCHAR2(1000),                              -- APIKEY 使用者 ID
    ai_apikey_id                NUMBER(19)       NOT NULL,                   -- APIKEY ID
    requst_ts                   NUMBER(19)       NOT NULL,                   -- 使用者請求時間，以毫秒數記錄
    input_token_count           NUMBER(19)       DEFAULT 0 NOT NULL,         -- 請求內容 input token 數
    output_token_count          NUMBER(19)       DEFAULT 0 NOT NULL,         -- 請求回傳 output token 數
    ai_prompt_template_id       NUMBER(19),                                  -- AI 提示模板 ID
    http_transaction_status     CLOB,                                        -- HTTP 交易狀態
    ai_usage_prompt_input       CLOB,                                        -- AI 使用者請求內容
    ai_usage_prompt_output      CLOB,                                        -- AI 使用者請求回傳內容
    create_date_time            TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,  -- 建立日期
    CONSTRAINT PK_dgr_ai_apikey_usage PRIMARY KEY(ai_apikey_usage_id)
);

-- 20250812 , 檢查並建立索引 , Vulcan
CREATE INDEX index_dgr_ai_apikey_usage_ai_apikey_id ON dgr_ai_apikey_usage (ai_apikey_id);

-- 20250812 , 檢查並建立 dgr_ai_prompt_template 表 , Vulcan
CREATE TABLE dgr_ai_prompt_template (
    ai_prompt_template_id       NUMBER(19)       NOT NULL,               -- ID
    ai_prompt_template_name     VARCHAR2(1700)   NOT NULL,               -- AI 提示模板名稱
    ai_prompt_template_content  CLOB             NOT NULL,               -- AI 提示模板內容
    ai_prompt_template_enable   VARCHAR2(1)      DEFAULT 'Y' NOT NULL,   -- Y = 啟用, N = 停用
    ai_prompt_template_remark   VARCHAR2(400),                           -- AI 提示模板備註
    CONSTRAINT PK_dgr_ai_prompt_template PRIMARY KEY(ai_prompt_template_id),
    CONSTRAINT UQ_dgr_ai_prompt_template_name UNIQUE (ai_prompt_template_name)
);

-- 20250812 , 檢查並建立 dgr_ai_prompt_template_binding 表 , Vulcan
CREATE TABLE dgr_ai_prompt_template_binding (
    ai_consumer_prompt_template_binding_id NUMBER(19) NOT NULL,  -- ID
    ai_prompt_template_id                  NUMBER(19) NOT NULL,  -- AI 提示模板 ID
    ai_apikey_consumer_type                VARCHAR2(1000),       -- APIKEY 使用者類型 user / client
    ai_apikey_consumer_id                  VARCHAR2(1000),       -- APIKEY 使用者 ID
    CONSTRAINT PK_dgr_ai_prompt_template_binding PRIMARY KEY(ai_consumer_prompt_template_binding_id)
);

-- 20250812 , 檢查並建立索引 , Vulcan
CREATE INDEX index_dgr_ai_prompt_template_binding_ai_prompt_template_id ON dgr_ai_prompt_template_binding (ai_prompt_template_id);

-- 20250819, TSMP外部API註冊資料, 增加欄位, Mini Lee
ALTER TABLE TSMP_API_REG ADD IS_CORS_ALLOW_ORIGIN VARCHAR2(1) DEFAULT 'N' NULL;
ALTER TABLE TSMP_API_REG ADD IS_CORS_ALLOW_METHODS VARCHAR2(1) DEFAULT 'N' NULL;
ALTER TABLE TSMP_API_REG ADD IS_CORS_ALLOW_HEADERS VARCHAR2(1) DEFAULT 'N' NULL;
ALTER TABLE TSMP_API_REG ADD CORS_ALLOW_ORIGIN VARCHAR2(1000) NULL;
ALTER TABLE TSMP_API_REG ADD CORS_ALLOW_METHODS VARCHAR2(200) NULL;
ALTER TABLE TSMP_API_REG ADD CORS_ALLOW_HEADERS VARCHAR2(1000) NULL;

-- 20250819, TSMP API 匯入資料, 增加欄位, Mini Lee
ALTER TABLE TSMP_API_IMP ADD IS_CORS_ALLOW_ORIGIN VARCHAR2(1) DEFAULT 'N' NULL;
ALTER TABLE TSMP_API_IMP ADD IS_CORS_ALLOW_METHODS VARCHAR2(1) DEFAULT 'N' NULL;
ALTER TABLE TSMP_API_IMP ADD IS_CORS_ALLOW_HEADERS VARCHAR2(1) DEFAULT 'N' NULL;
ALTER TABLE TSMP_API_IMP ADD CORS_ALLOW_ORIGIN VARCHAR2(1000) NULL;
ALTER TABLE TSMP_API_IMP ADD CORS_ALLOW_METHODS VARCHAR2(200) NULL;
ALTER TABLE TSMP_API_IMP ADD CORS_ALLOW_HEADERS VARCHAR2(1000) NULL;

-- 20250825, dgr_otp , Tom
CREATE TABLE dgr_otp (
    otp_id              NUMBER(19)      NOT NULL,       -- ID
    email               VARCHAR(100)    NOT NULL,       -- email
    otp_code            VARCHAR(10)     NOT NULL,       -- 驗證碼
    expire_key          VARCHAR(400)    NOT NULL,       -- 到期key
    error_limit         NUMBER(10)      DEFAULT 0 NOT NULL, -- 錯誤上限
    CONSTRAINT pk_dgr_otp PRIMARY KEY (otp_id),
    CONSTRAINT uk_dgr_otp UNIQUE (email)
);

-- 20251003, v4, 加大欄位長度, Mini Lee
ALTER TABLE TSMP_TOKEN_HISTORY MODIFY USER_NAME NVARCHAR2(400);

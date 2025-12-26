-- digirunner.dgr_audit_logd definition

CREATE TABLE IF NOT EXISTS `dgr_audit_logd` (
  `audit_long_id` bigint(20) NOT NULL,
  `txn_uid` varchar(50) NOT NULL,
  `entity_name` varchar(50) NOT NULL,
  `cud` varchar(50) NOT NULL,
  `old_row` longblob DEFAULT NULL,
  `new_row` longblob DEFAULT NULL,
  `param1` text DEFAULT NULL,
  `param2` text DEFAULT NULL,
  `param3` text DEFAULT NULL,
  `param4` text DEFAULT NULL,
  `param5` text DEFAULT NULL,
  `stack_trace` text DEFAULT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`audit_long_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.dgr_audit_logm definition

CREATE TABLE IF NOT EXISTS `dgr_audit_logm` (
  `audit_long_id` bigint(20) NOT NULL,
  `audit_ext_id` bigint(20) NOT NULL DEFAULT 0,
  `txn_uid` varchar(50) NOT NULL,
  `user_name` varchar(50) NOT NULL,
  `client_id` varchar(50) NOT NULL,
  `api_url` varchar(500) NOT NULL,
  `orig_api_url` varchar(500) DEFAULT NULL,
  `event_no` varchar(50) NOT NULL,
  `user_ip` varchar(200) DEFAULT NULL,
  `user_hostname` varchar(200) DEFAULT NULL,
  `user_role` text DEFAULT NULL,
  `param1` text DEFAULT NULL,
  `param2` text DEFAULT NULL,
  `param3` text DEFAULT NULL,
  `param4` text DEFAULT NULL,
  `param5` text DEFAULT NULL,
  `stack_trace` text DEFAULT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`audit_long_id`,`audit_ext_id`),
  UNIQUE KEY `uk_dgr_audit_logm_1` (`txn_uid`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.dgr_composer_flow definition

CREATE TABLE IF NOT EXISTS `dgr_composer_flow` (
  `flow_id` bigint(20) NOT NULL,
  `module_name` varchar(150) NOT NULL,
  `api_id` varchar(255) NOT NULL,
  `flow_data` longblob DEFAULT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `update_date_time` datetime DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`flow_id`),
  UNIQUE KEY `u_dgr_composer_flow` (`module_name`,`api_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.dgr_node_lost_contact definition

CREATE TABLE IF NOT EXISTS `dgr_node_lost_contact` (
  `lost_contact_id` bigint(20) NOT NULL,
  `node_name` varchar(100) NOT NULL,
  `ip` varchar(100) NOT NULL,
  `port` int(11) NOT NULL,
  `lost_time` varchar(100) NOT NULL,
  `create_timestamp` bigint(20) NOT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`lost_contact_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.dgr_oauth_approvals definition

CREATE TABLE IF NOT EXISTS `dgr_oauth_approvals` (
  `oauth_approvals_id` bigint(20) NOT NULL,
  `user_name` varchar(256) DEFAULT NULL,
  `client_id` varchar(256) DEFAULT NULL,
  `scope` varchar(256) DEFAULT NULL,
  `status` varchar(10) DEFAULT NULL,
  `expires_at` datetime DEFAULT NULL,
  `last_modified_at` datetime DEFAULT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`oauth_approvals_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.groups definition

CREATE TABLE IF NOT EXISTS `groups` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.ldap_auth_result definition

CREATE TABLE IF NOT EXISTS `ldap_auth_result` (
  `ldap_id` bigint(20) NOT NULL,
  `user_name` varchar(50) NOT NULL,
  `code_challenge` varchar(50) NOT NULL,
  `user_ip` varchar(50) DEFAULT NULL,
  `use_date_time` datetime DEFAULT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'ldap_system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`ldap_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.oauth_approvals definition

CREATE TABLE IF NOT EXISTS `oauth_approvals` (
  `userid` varchar(256) DEFAULT NULL,
  `clientid` varchar(256) DEFAULT NULL,
  `scope` varchar(256) DEFAULT NULL,
  `status` varchar(10) DEFAULT NULL,
  `expiresat` datetime DEFAULT NULL,
  `lastmodifiedat` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.oauth_client_details definition

CREATE TABLE IF NOT EXISTS `oauth_client_details` (
  `client_id` varchar(256) NOT NULL,
  `resource_ids` varchar(256) DEFAULT NULL,
  `client_secret` varchar(256) DEFAULT NULL,
  `scope` varchar(2048) DEFAULT NULL,
  `authorized_grant_types` varchar(256) DEFAULT NULL,
  `web_server_redirect_uri` varchar(256) DEFAULT NULL,
  `authorities` varchar(256) DEFAULT NULL,
  `access_token_validity` int(11) DEFAULT NULL,
  `refresh_token_validity` int(11) DEFAULT NULL,
  `additional_information` varchar(4096) DEFAULT NULL,
  `autoapprove` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`client_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.oauth_code definition

CREATE TABLE IF NOT EXISTS `oauth_code` (
  `code` varchar(256) DEFAULT NULL,
  `authentication` blob DEFAULT NULL,
  `created` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.seq_store definition

CREATE TABLE IF NOT EXISTS `seq_store` (
  `sequence_name` varchar(255) NOT NULL,
  `next_val` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`sequence_name`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.sso_auth_result definition

CREATE TABLE IF NOT EXISTS `sso_auth_result` (
  `sso_id` bigint(20) NOT NULL,
  `user_name` varchar(50) DEFAULT NULL,
  `code_challenge` varchar(50) NOT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'sso system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `use_date_time` datetime DEFAULT NULL,
  PRIMARY KEY (`sso_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_alert definition

CREATE TABLE IF NOT EXISTS `tsmp_alert` (
  `alert_id` int(11) NOT NULL,
  `alert_name` varchar(30) NOT NULL,
  `alert_type` varchar(20) NOT NULL,
  `alert_enabled` tinyint(1) NOT NULL,
  `threshold` int(11) DEFAULT NULL,
  `duration` int(11) DEFAULT NULL,
  `alert_interval` int(11) DEFAULT NULL,
  `c_flag` tinyint(1) NOT NULL,
  `im_flag` tinyint(1) NOT NULL,
  `im_type` varchar(20) DEFAULT NULL,
  `im_id` varchar(100) DEFAULT NULL,
  `ex_type` char(1) NOT NULL,
  `ex_days` varchar(100) DEFAULT NULL,
  `ex_time` varchar(100) DEFAULT NULL,
  `alert_desc` varchar(200) DEFAULT NULL,
  `alert_sys` varchar(20) DEFAULT NULL,
  `alert_msg` varchar(300) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT NULL,
  `create_user` varchar(30) DEFAULT NULL,
  `update_user` varchar(30) DEFAULT NULL,
  `es_search_payload` varchar(1024) DEFAULT NULL,
  `modulename` varchar(255) DEFAULT NULL,
  `responsetime` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`alert_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_alert_log definition

CREATE TABLE IF NOT EXISTS `tsmp_alert_log` (
  `alert_log_id` bigint(20) NOT NULL,
  `alert_id` int(11) NOT NULL DEFAULT -1,
  `role_id` varchar(500) DEFAULT NULL,
  `alert_msg` varchar(300) NOT NULL DEFAULT '',
  `sender_type` varchar(20) NOT NULL,
  `result` varchar(1) NOT NULL DEFAULT '0',
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`alert_log_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_api definition

CREATE TABLE IF NOT EXISTS `tsmp_api` (
  `api_key` varchar(255) NOT NULL,
  `module_name` varchar(150) NOT NULL,
  `api_name` varchar(255) DEFAULT NULL,
  `api_status` char(1) NOT NULL,
  `api_src` char(1) NOT NULL,
  `api_desc` varchar(1500) DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `create_user` varchar(255) DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `api_owner` varchar(100) DEFAULT NULL,
  `org_id` varchar(255) DEFAULT NULL,
  `public_flag` char(1) DEFAULT NULL,
  `src_url` varchar(2000) DEFAULT NULL,
  `api_uid` varchar(36) DEFAULT NULL,
  `data_format` char(1) DEFAULT NULL,
  `jwe_flag` varchar(1) DEFAULT NULL,
  `jwe_flag_resp` varchar(1) DEFAULT NULL,
  `api_cache_flag` char(1) NOT NULL DEFAULT '1',
  `mock_status_code` char(3) DEFAULT Null,
  `mock_headers` varchar(2000) DEFAULT Null,
  `mock_body` varchar(2000) DEFAULT Null,
  PRIMARY KEY (`api_key`,`module_name`),
  UNIQUE KEY `uk_api_1` (`api_key`,`module_name`),
  KEY `tsmp_api_api_uid_idx` (`api_uid`) USING BTREE
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_api_ext definition

CREATE TABLE IF NOT EXISTS `tsmp_api_ext` (
  `api_key` varchar(30) NOT NULL,
  `module_name` varchar(100) NOT NULL,
  `dp_status` varchar(1) NOT NULL,
  `dp_stu_date_time` datetime DEFAULT NULL,
  `ref_orderm_id` bigint(20) NOT NULL,
  `api_ext_id` bigint(20) NOT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`api_key`,`module_name`),
  UNIQUE KEY `api_ext_id` (`api_ext_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_api_imp definition

CREATE TABLE IF NOT EXISTS `tsmp_api_imp` (
  `api_key` varchar(255) NOT NULL,
  `module_name` varchar(50) NOT NULL,
  `record_type` char(1) NOT NULL,
  `batch_no` int(11) NOT NULL,
  `filename` varchar(100) NOT NULL,
  `api_name` varchar(255) DEFAULT NULL,
  `api_desc` varchar(300) DEFAULT NULL,
  `api_owner` varchar(100) DEFAULT NULL,
  `url_rid` char(1) DEFAULT '0',
  `api_src` char(1) DEFAULT 'm',
  `src_url` varchar(2000) DEFAULT NULL,
  `api_uuid` varchar(64) DEFAULT NULL,
  `path_of_json` varchar(255) NOT NULL,
  `method_of_json` varchar(50) NOT NULL,
  `params_of_json` varchar(255) DEFAULT NULL,
  `headers_of_json` varchar(255) DEFAULT NULL,
  `consumes_of_json` varchar(100) DEFAULT NULL,
  `produces_of_json` varchar(255) DEFAULT NULL,
  `flow` longtext DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `create_user` varchar(255) DEFAULT NULL,
  `check_act` char(1) NOT NULL,
  `result` char(1) NOT NULL,
  `memo` varchar(255) DEFAULT NULL,
  `no_oauth` char(1) DEFAULT NULL,
  `jwe_flag` varchar(1) DEFAULT NULL,
  `jwe_flag_resp` varchar(1) DEFAULT NULL,
  `fun_flag` int(11) DEFAULT NULL,
  PRIMARY KEY (`api_key`,`module_name`,`record_type`,`batch_no`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_api_module definition

CREATE TABLE IF NOT EXISTS `tsmp_api_module` (
  `id` bigint(20) NOT NULL,
  `module_name` varchar(255) NOT NULL,
  `module_version` varchar(255) NOT NULL,
  `module_app_class` varchar(255) NOT NULL,
  `module_bytes` longblob NOT NULL,
  `module_md5` varchar(255) NOT NULL,
  `module_type` varchar(255) NOT NULL,
  `upload_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `uploader_name` varchar(255) NOT NULL,
  `status_time` timestamp NULL DEFAULT NULL,
  `status_user` varchar(255) DEFAULT NULL,
  `active` tinyint(1) NOT NULL,
  `node_task_id` bigint(20) DEFAULT NULL,
  `v2_flag` char(1) DEFAULT NULL,
  `org_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_api_module_1` (`module_name`,`module_version`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_api_reg definition

CREATE TABLE IF NOT EXISTS `tsmp_api_reg` (
  `api_key` varchar(255) NOT NULL,
  `module_name` varchar(50) NOT NULL,
  `src_url` varchar(2000) NOT NULL,
  `reg_status` char(1) NOT NULL,
  `api_uuid` varchar(64) DEFAULT NULL,
  `path_of_json` varchar(255) DEFAULT NULL,
  `method_of_json` varchar(50) NOT NULL,
  `params_of_json` varchar(255) DEFAULT NULL,
  `headers_of_json` varchar(255) DEFAULT NULL,
  `consumes_of_json` varchar(100) DEFAULT NULL,
  `produces_of_json` varchar(255) DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `create_user` varchar(255) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `url_rid` char(1) NOT NULL DEFAULT '0',
  `reghost_id` varchar(10) DEFAULT NULL,
  `no_oauth` char(1) DEFAULT NULL,
  `fun_flag` int(11) DEFAULT 0,
  PRIMARY KEY (`api_key`,`module_name`),
  UNIQUE KEY `uk_api_reg_1` (`api_key`,`module_name`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_auth_code definition

CREATE TABLE IF NOT EXISTS `tsmp_auth_code` (
  `auth_code_id` bigint(20) NOT NULL,
  `auth_code` varchar(1000) NOT NULL,
  `expire_date_time` bigint(20) NOT NULL,
  `status` varchar(1) NOT NULL DEFAULT '0',
  `auth_type` varchar(20) DEFAULT NULL,
  `client_name` varchar(150) DEFAULT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`auth_code_id`),
  UNIQUE KEY `tsmp_auth_code_uk` (`auth_code`) USING HASH
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_client definition

CREATE TABLE IF NOT EXISTS `tsmp_client` (
  `client_id` varchar(40) NOT NULL,
  `client_name` varchar(150) NOT NULL,
  `client_status` char(1) NOT NULL,
  `tps` int(11) NOT NULL,
  `emails` varchar(500) DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `owner` varchar(100) NOT NULL,
  `remark` varchar(300) DEFAULT NULL,
  `create_user` varchar(255) DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `client_sd` datetime DEFAULT NULL,
  `client_ed` datetime DEFAULT NULL,
  `svc_st` varchar(4) DEFAULT NULL,
  `svc_et` varchar(4) DEFAULT NULL,
  `api_quota` int(11) DEFAULT NULL,
  `api_used` int(11) DEFAULT NULL,
  `c_priority` int(11) DEFAULT 5,
  `client_alias` varchar(150) DEFAULT NULL,
  `pwd_fail_times` int(11) DEFAULT 0,
  `fail_treshhold` int(11) DEFAULT 3,
  `security_level_id` varchar(10) DEFAULT 'system',
  `signup_num` varchar(100) DEFAULT NULL,
  `access_token_quota` int(11) DEFAULT 0,
  `refresh_token_quota` int(11) DEFAULT 0,
  `client_secret` varchar(128) DEFAULT NULL,
  `start_date` bigint(20) DEFAULT NULL,
  `end_date` bigint(20) DEFAULT NULL,
  `start_time_per_day` bigint(20) DEFAULT NULL,
  `end_time_per_day` bigint(20) DEFAULT NULL,
  `time_zone` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`client_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_client_cert definition

CREATE TABLE IF NOT EXISTS `tsmp_client_cert` (
  `client_cert_id` bigint(20) NOT NULL,
  `client_id` varchar(40) NOT NULL,
  `cert_file_name` varchar(255) NOT NULL,
  `file_content` longblob NOT NULL,
  `pub_key` varchar(1024) NOT NULL,
  `cert_version` varchar(255) DEFAULT NULL,
  `cert_serial_num` varchar(255) NOT NULL,
  `s_algorithm_id` varchar(255) DEFAULT NULL,
  `algorithm_id` varchar(255) NOT NULL,
  `cert_thumbprint` varchar(1024) NOT NULL,
  `iuid` varchar(255) DEFAULT NULL,
  `issuer_name` varchar(255) NOT NULL,
  `suid` varchar(255) DEFAULT NULL,
  `create_at` bigint(20) NOT NULL,
  `expired_at` bigint(20) NOT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `key_size` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`client_cert_id`),
  KEY `index_tsmp_client_cert_01` (`client_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_client_cert2 definition

CREATE TABLE IF NOT EXISTS `tsmp_client_cert2` (
  `client_cert2_id` bigint(20) NOT NULL,
  `client_id` varchar(40) NOT NULL,
  `cert_file_name` varchar(255) NOT NULL,
  `file_content` longblob NOT NULL,
  `pub_key` varchar(1024) NOT NULL,
  `cert_version` varchar(255) DEFAULT NULL,
  `cert_serial_num` varchar(255) NOT NULL,
  `s_algorithm_id` varchar(255) DEFAULT NULL,
  `algorithm_id` varchar(255) NOT NULL,
  `cert_thumbprint` varchar(1024) NOT NULL,
  `iuid` varchar(255) DEFAULT NULL,
  `issuer_name` varchar(255) NOT NULL,
  `suid` varchar(255) DEFAULT NULL,
  `create_at` bigint(20) NOT NULL,
  `expired_at` bigint(20) NOT NULL,
  `key_size` int(11) NOT NULL DEFAULT 0,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`client_cert2_id`),
  KEY `index_tsmp_client_cert2_01` (`client_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_client_group definition

CREATE TABLE IF NOT EXISTS `tsmp_client_group` (
  `client_id` varchar(40) NOT NULL,
  `group_id` varchar(10) NOT NULL,
  PRIMARY KEY (`client_id`,`group_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_client_host definition

CREATE TABLE IF NOT EXISTS `tsmp_client_host` (
  `host_seq` int(11) NOT NULL,
  `client_id` varchar(40) NOT NULL,
  `host_name` varchar(50) NOT NULL,
  `host_ip` varchar(15) NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`host_seq`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_client_log definition

CREATE TABLE IF NOT EXISTS `tsmp_client_log` (
  `log_seq` varchar(20) NOT NULL,
  `is_login` tinyint(1) NOT NULL,
  `agent` varchar(500) NOT NULL,
  `event_type` varchar(10) NOT NULL,
  `event_msg` varchar(300) NOT NULL,
  `event_time` datetime NOT NULL,
  `client_id` varchar(40) NOT NULL,
  `client_ip` varchar(15) NOT NULL,
  `user_name` varchar(30) DEFAULT NULL,
  `txsn` varchar(20) NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`log_seq`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_client_vgroup definition

CREATE TABLE IF NOT EXISTS `tsmp_client_vgroup` (
  `client_id` varchar(40) NOT NULL,
  `vgroup_id` varchar(10) NOT NULL,
  PRIMARY KEY (`client_id`,`vgroup_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dc definition

CREATE TABLE IF NOT EXISTS `tsmp_dc` (
  `dc_id` bigint(20) NOT NULL,
  `dc_code` varchar(30) NOT NULL,
  `dc_memo` varchar(300) DEFAULT NULL,
  `active` tinyint(1) DEFAULT NULL,
  `create_user` varchar(255) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `update_user` varchar(255) DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`dc_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dc_module definition

CREATE TABLE IF NOT EXISTS `tsmp_dc_module` (
  `dc_id` bigint(20) NOT NULL,
  `module_id` bigint(20) NOT NULL,
  `node_task_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`dc_id`,`module_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dc_node definition

CREATE TABLE IF NOT EXISTS `tsmp_dc_node` (
  `node` varchar(30) NOT NULL,
  `dc_id` bigint(20) NOT NULL,
  `node_task_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`node`,`dc_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_about definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_about` (
  `seq_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `about_subject` varchar(100) NOT NULL,
  `about_desc` text NOT NULL,
  `create_time` datetime NOT NULL DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`seq_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_api_app definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_api_app` (
  `ref_app_id` bigint(20) NOT NULL,
  `ref_api_uid` varchar(36) NOT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ref_app_id`,`ref_api_uid`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_api_auth2 definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_api_auth2` (
  `api_auth_id` bigint(20) NOT NULL,
  `ref_client_id` varchar(40) NOT NULL,
  `ref_api_uid` varchar(36) NOT NULL,
  `apply_status` varchar(10) NOT NULL,
  `apply_purpose` varchar(3000) NOT NULL,
  `ref_review_user` varchar(255) DEFAULT NULL,
  `review_remark` varchar(3000) DEFAULT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT NULL,
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `keyword_search` varchar(6000) DEFAULT NULL,
  PRIMARY KEY (`api_auth_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_api_theme definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_api_theme` (
  `ref_api_theme_id` bigint(20) NOT NULL,
  `ref_api_uid` varchar(36) NOT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ref_api_theme_id`,`ref_api_uid`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_api_view_log definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_api_view_log` (
  `seq_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `api_id` varchar(36) NOT NULL,
  `from_ip` varchar(50) DEFAULT NULL,
  `view_date` date NOT NULL,
  `create_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`seq_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_app definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_app` (
  `app_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ref_app_cate_id` bigint(20) NOT NULL,
  `name` varchar(100) NOT NULL,
  `intro` text NOT NULL,
  `author` varchar(100) DEFAULT NULL,
  `data_status` char(1) NOT NULL,
  `org_id` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `keyword_search` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`app_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_app_category definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_app_category` (
  `app_cate_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `app_cate_name` varchar(100) NOT NULL,
  `data_sort` int(11) DEFAULT NULL,
  `org_id` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `keyword_search` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`app_cate_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_appt_job definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_appt_job` (
  `appt_job_id` bigint(20) NOT NULL,
  `ref_item_no` varchar(50) NOT NULL,
  `ref_subitem_no` varchar(100) DEFAULT NULL,
  `status` varchar(1) NOT NULL DEFAULT 'w',
  `in_params` text DEFAULT NULL,
  `exec_result` text DEFAULT NULL,
  `exec_owner` varchar(20) DEFAULT 'sys',
  `stack_trace` text DEFAULT NULL,
  `job_step` varchar(50) DEFAULT NULL,
  `start_date_time` datetime NOT NULL,
  `from_job_id` bigint(20) DEFAULT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `identif_data` text DEFAULT NULL,
  `period_uid` varchar(36) NOT NULL DEFAULT uuid(),
  `period_items_id` bigint(20) NOT NULL DEFAULT 0,
  `period_nexttime` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`appt_job_id`),
  UNIQUE KEY `uk_tsmp_dp_appt_job_1` (`period_uid`,`period_items_id`,`period_nexttime`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_appt_rjob definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_appt_rjob` (
  `appt_rjob_id` varchar(36) NOT NULL,
  `rjob_name` varchar(60) NOT NULL,
  `cron_expression` varchar(700) NOT NULL,
  `cron_json` text NOT NULL,
  `cron_desc` varchar(300) DEFAULT NULL,
  `next_date_time` bigint(20) NOT NULL,
  `last_date_time` bigint(20) DEFAULT NULL,
  `eff_date_time` bigint(20) DEFAULT NULL,
  `inv_date_time` bigint(20) DEFAULT NULL,
  `remark` varchar(300) DEFAULT NULL,
  `status` varchar(1) NOT NULL DEFAULT '1',
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `keyword_search` varchar(396) DEFAULT NULL,
  PRIMARY KEY (`appt_rjob_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_appt_rjob_d definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_appt_rjob_d` (
  `appt_rjob_d_id` bigint(20) NOT NULL,
  `appt_rjob_id` varchar(36) NOT NULL,
  `ref_item_no` varchar(50) NOT NULL,
  `ref_subitem_no` varchar(100) DEFAULT NULL,
  `in_params` text DEFAULT NULL,
  `identif_data` text DEFAULT NULL,
  `sort_by` int(11) NOT NULL DEFAULT 0,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `keyword_search` varchar(186) DEFAULT NULL,
  PRIMARY KEY (`appt_rjob_d_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_callapi definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_callapi` (
  `callapi_id` bigint(20) NOT NULL,
  `req_url` varchar(500) NOT NULL,
  `req_msg` text DEFAULT NULL,
  `resp_msg` text DEFAULT NULL,
  `token_url` varchar(500) DEFAULT NULL,
  `sign_code_url` varchar(500) DEFAULT NULL,
  `auth` varchar(500) NOT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  PRIMARY KEY (`callapi_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_chk_layer definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_chk_layer` (
  `chk_layer_id` bigint(20) NOT NULL,
  `review_type` varchar(20) NOT NULL,
  `layer` int(11) NOT NULL,
  `role_id` varchar(10) NOT NULL,
  `status` varchar(1) NOT NULL DEFAULT '1',
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`review_type`,`layer`,`role_id`),
  KEY `index_tsmp_dp_chk_layer_01` (`chk_layer_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_chk_log definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_chk_log` (
  `chk_log_id` bigint(20) NOT NULL,
  `req_orders_id` bigint(20) NOT NULL,
  `req_orderm_id` bigint(20) NOT NULL,
  `layer` int(11) NOT NULL,
  `req_comment` varchar(200) DEFAULT NULL,
  `review_status` varchar(20) NOT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  PRIMARY KEY (`chk_log_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_clientext definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_clientext` (
  `client_id` varchar(40) NOT NULL,
  `client_seq_id` bigint(20) NOT NULL,
  `content_txt` varchar(1000) NOT NULL,
  `reg_status` char(1) NOT NULL DEFAULT '0',
  `pwd_status` char(1) NOT NULL DEFAULT '1',
  `pwd_reset_key` varchar(22) DEFAULT NULL,
  `review_remark` varchar(3000) DEFAULT NULL,
  `ref_review_user` varchar(255) DEFAULT NULL,
  `resubmit_date_time` datetime DEFAULT NULL,
  `public_flag` char(1) DEFAULT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT NULL,
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `keyword_search` text DEFAULT NULL,
  PRIMARY KEY (`client_id`),
  UNIQUE KEY `uk_tsmp_dp_clientext_1` (`client_seq_id`),
  KEY `index_tsmp_dp_clientext_01` (`client_seq_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_denied_module definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_denied_module` (
  `ref_module_name` varchar(255) NOT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ref_module_name`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_faq_answer definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_faq_answer` (
  `answer_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `answer_name` text NOT NULL,
  `answer_name_en` text DEFAULT NULL,
  `ref_question_id` bigint(20) NOT NULL,
  `create_time` datetime DEFAULT current_timestamp(),
  `create_user` text DEFAULT NULL,
  `update_time` datetime NOT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `keyword_search` text DEFAULT NULL,
  PRIMARY KEY (`answer_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_faq_question definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_faq_question` (
  `question_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `question_name` text NOT NULL,
  `question_name_en` text DEFAULT NULL,
  `data_sort` int(11) DEFAULT NULL,
  `data_status` char(1) NOT NULL DEFAULT '1',
  `create_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT NULL,
  `update_time` datetime NOT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `keyword_search` text DEFAULT NULL,
  PRIMARY KEY (`question_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_file definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_file` (
  `file_id` bigint(20) NOT NULL,
  `file_name` varchar(100) NOT NULL,
  `file_path` varchar(300) NOT NULL,
  `ref_file_cate_code` varchar(50) NOT NULL,
  `ref_id` bigint(20) NOT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT NULL,
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `keyword_search` varchar(400) DEFAULT NULL,
  `is_blob` varchar(1) DEFAULT 'n',
  `is_tmpfile` varchar(1) DEFAULT 'n',
  `blob_data` longblob DEFAULT NULL,
  PRIMARY KEY (`file_id`),
  UNIQUE KEY `tsmp_dp_file_uq` (`file_name`,`ref_file_cate_code`,`ref_id`),
  KEY `index_tsmp_dp_file_01` (`ref_file_cate_code`,`ref_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_items definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_items` (
  `item_id` bigint(20) NOT NULL,
  `item_no` varchar(20) NOT NULL,
  `item_name` varchar(100) NOT NULL,
  `subitem_no` varchar(20) NOT NULL,
  `subitem_name` varchar(100) NOT NULL,
  `sort_by` int(11) NOT NULL DEFAULT 0,
  `is_default` varchar(1) DEFAULT NULL,
  `param1` varchar(255) DEFAULT NULL,
  `param2` varchar(255) DEFAULT NULL,
  `param3` varchar(255) DEFAULT NULL,
  `param4` varchar(255) DEFAULT NULL,
  `param5` varchar(255) DEFAULT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `keyword_search` varchar(200) DEFAULT NULL,
  `locale` varchar(10) NOT NULL DEFAULT 'zh-tw',
  PRIMARY KEY (`item_no`,`subitem_no`,`locale`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_mail_log definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_mail_log` (
  `maillog_id` bigint(20) NOT NULL,
  `recipients` varchar(100) NOT NULL,
  `template_txt` varchar(3800) NOT NULL,
  `ref_code` varchar(20) NOT NULL,
  `result` varchar(1) NOT NULL DEFAULT '0',
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT NULL,
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `keyword_search` text DEFAULT NULL,
  PRIMARY KEY (`maillog_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_mail_tplt definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_mail_tplt` (
  `mailtplt_id` bigint(20) NOT NULL,
  `code` varchar(20) NOT NULL,
  `template_txt` varchar(2000) NOT NULL,
  `remark` varchar(100) DEFAULT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT NULL,
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `keyword_search` varchar(2120) DEFAULT NULL,
  PRIMARY KEY (`mailtplt_id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_mail_tplten definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_mail_tplten` (
  `mailtplt_id` bigint(20) NOT NULL,
  `code` varchar(20) NOT NULL,
  `template_txt` varchar(2000) NOT NULL,
  `remark` varchar(100) DEFAULT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `keyword_search` varchar(2120) DEFAULT NULL,
  PRIMARY KEY (`mailtplt_id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_mail_tplttw definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_mail_tplttw` (
  `mailtplt_id` bigint(20) NOT NULL,
  `code` varchar(20) NOT NULL,
  `template_txt` varchar(2000) NOT NULL,
  `remark` varchar(100) DEFAULT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `keyword_search` varchar(2120) DEFAULT NULL,
  PRIMARY KEY (`mailtplt_id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_news definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_news` (
  `news_id` bigint(20) NOT NULL,
  `new_title` varchar(100) NOT NULL DEFAULT '_',
  `new_content` text NOT NULL,
  `status` varchar(1) NOT NULL DEFAULT '1',
  `org_id` varchar(255) NOT NULL,
  `post_date_time` datetime NOT NULL,
  `ref_type_subitem_no` varchar(20) NOT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `keyword_search` varchar(2148) DEFAULT NULL,
  PRIMARY KEY (`news_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_req_orderd1 definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_req_orderd1` (
  `req_orderd1_id` bigint(20) NOT NULL,
  `ref_req_orderm_id` bigint(20) NOT NULL,
  `client_id` varchar(40) NOT NULL,
  `api_uid` varchar(36) NOT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`req_orderd1_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_req_orderd2 definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_req_orderd2` (
  `req_orderd2_id` bigint(20) NOT NULL,
  `ref_req_orderm_id` bigint(20) NOT NULL,
  `api_uid` varchar(36) NOT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `public_flag` char(1) DEFAULT NULL,
  PRIMARY KEY (`req_orderd2_id`),
  KEY `index_tsmp_dp_req_orderd2_01` (`api_uid`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_req_orderd2d definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_req_orderd2d` (
  `req_orderd2_id` bigint(20) NOT NULL,
  `api_uid` varchar(36) NOT NULL,
  `ref_theme_id` bigint(20) NOT NULL,
  `req_orderd2d_id` bigint(20) NOT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`req_orderd2_id`,`api_uid`,`ref_theme_id`),
  UNIQUE KEY `req_orderd2d_id` (`req_orderd2d_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_req_orderd3 definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_req_orderd3` (
  `req_orderd3_id` bigint(20) NOT NULL,
  `ref_req_orderm_id` bigint(20) NOT NULL,
  `client_id` varchar(40) NOT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`req_orderd3_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_req_orderd5 definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_req_orderd5` (
  `req_orderd5_id` bigint(20) NOT NULL,
  `client_id` varchar(255) NOT NULL,
  `ref_req_orderm_id` bigint(20) NOT NULL,
  `ref_open_apikey_id` bigint(20) DEFAULT NULL,
  `open_apikey` varchar(1024) DEFAULT NULL,
  `secret_key` varchar(1024) DEFAULT NULL,
  `open_apikey_alias` varchar(255) NOT NULL,
  `times_threshold` int(11) NOT NULL DEFAULT 0,
  `expired_at` bigint(20) NOT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`req_orderd5_id`),
  KEY `index_tsmp_dp_req_orderd5_01` (`open_apikey`(768))
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_req_orderd5d definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_req_orderd5d` (
  `ref_req_orderd5_id` bigint(20) NOT NULL,
  `ref_api_uid` varchar(36) NOT NULL,
  `req_orderd5d_id` bigint(20) NOT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`ref_req_orderd5_id`,`ref_api_uid`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_req_orderm definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_req_orderm` (
  `req_orderm_id` bigint(20) NOT NULL,
  `req_order_no` varchar(30) NOT NULL,
  `req_type` varchar(20) NOT NULL,
  `req_subtype` varchar(20) DEFAULT NULL,
  `client_id` varchar(40) NOT NULL,
  `org_id` varchar(255) DEFAULT NULL,
  `req_desc` varchar(1000) NOT NULL,
  `req_user_id` varchar(10) DEFAULT NULL,
  `effective_date` datetime DEFAULT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `keyword_search` varchar(1020) DEFAULT NULL,
  PRIMARY KEY (`req_orderm_id`),
  UNIQUE KEY `req_order_no` (`req_order_no`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_req_orders definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_req_orders` (
  `req_orders_id` bigint(20) NOT NULL,
  `req_orderm_id` bigint(20) NOT NULL,
  `layer` int(11) NOT NULL,
  `req_comment` varchar(200) DEFAULT NULL,
  `review_status` varchar(20) NOT NULL DEFAULT 'wait1',
  `status` varchar(1) NOT NULL DEFAULT '1',
  `proc_flag` int(11) DEFAULT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `keyword_search` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`req_orders_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_site_map definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_site_map` (
  `site_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `site_parent_id` bigint(20) NOT NULL,
  `site_desc` varchar(200) NOT NULL,
  `data_sort` int(11) NOT NULL,
  `site_url` varchar(200) DEFAULT NULL,
  `create_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`site_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_dp_theme_category definition

CREATE TABLE IF NOT EXISTS `tsmp_dp_theme_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `theme_name` varchar(100) NOT NULL,
  `data_status` char(1) NOT NULL DEFAULT '1',
  `data_sort` int(11) DEFAULT NULL,
  `org_id` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `keyword_search` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `index_tsmp_dp_theme_category_01` (`theme_name`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_events definition

CREATE TABLE IF NOT EXISTS `tsmp_events` (
  `event_id` bigint(20) NOT NULL,
  `event_type_id` varchar(20) NOT NULL,
  `event_name_id` varchar(20) NOT NULL,
  `module_name` varchar(255) NOT NULL,
  `module_version` varchar(255) DEFAULT NULL,
  `trace_id` varchar(20) NOT NULL,
  `info_msg` text DEFAULT NULL,
  `keep_flag` varchar(1) NOT NULL DEFAULT 'n',
  `archive_flag` varchar(1) NOT NULL DEFAULT 'n',
  `node_alias` varchar(200) DEFAULT NULL,
  `node_id` varchar(200) DEFAULT NULL,
  `thread_name` varchar(1000) DEFAULT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  PRIMARY KEY (`event_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_func definition

CREATE TABLE IF NOT EXISTS `tsmp_func` (
  `func_code` varchar(10) NOT NULL,
  `func_name` varchar(50) NOT NULL,
  `func_name_en` varchar(50) DEFAULT NULL,
  `func_desc` varchar(300) DEFAULT NULL,
  `locale` varchar(10) NOT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `update_time` datetime NOT NULL,
  `func_url` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`func_code`,`locale`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;

-- digirunner.tsmp_group definition

CREATE TABLE IF NOT EXISTS `tsmp_group` (
  `group_id` varchar(10) NOT NULL,
  `group_name` varchar(150) NOT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `create_user` varchar(255) DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `group_alias` varchar(150) DEFAULT NULL,
  `group_desc` varchar(1500) DEFAULT NULL,
  `group_access` varchar(255) DEFAULT NULL,
  `security_level_id` varchar(10) DEFAULT 'system',
  `allow_days` int(11) DEFAULT 0,
  `allow_times` int(11) DEFAULT 0,
  `vgroup_flag` char(1) NOT NULL DEFAULT '0',
  `vgroup_id` varchar(10) DEFAULT NULL,
  `vgroup_name` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`group_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_group_api definition

CREATE TABLE IF NOT EXISTS `tsmp_group_api` (
  `group_id` varchar(10) NOT NULL,
  `api_key` varchar(255) NOT NULL,
  `module_name` varchar(100) NOT NULL,
  `module_version` varchar(20) DEFAULT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`group_id`,`api_key`,`module_name`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_group_authorities definition

CREATE TABLE IF NOT EXISTS `tsmp_group_authorities` (
  `group_authoritie_id` varchar(10) NOT NULL,
  `group_authoritie_name` varchar(30) NOT NULL,
  `group_authoritie_desc` varchar(60) DEFAULT NULL,
  `group_authoritie_level` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`group_authoritie_id`),
  UNIQUE KEY `group_authoritie_name` (`group_authoritie_name`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_group_authorities_map definition

CREATE TABLE IF NOT EXISTS `tsmp_group_authorities_map` (
  `group_id` varchar(10) NOT NULL,
  `group_authoritie_id` varchar(10) NOT NULL,
  PRIMARY KEY (`group_id`,`group_authoritie_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_group_times_log definition

CREATE TABLE IF NOT EXISTS `tsmp_group_times_log` (
  `seq_no` bigint(20) NOT NULL,
  `jti` varchar(100) NOT NULL,
  `group_id` varchar(10) DEFAULT NULL,
  `expire_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `reexpired_time` datetime DEFAULT NULL,
  `times_quota` int(11) DEFAULT NULL,
  `times_threshold` int(11) DEFAULT NULL,
  `rejti` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`seq_no`),
  UNIQUE KEY `jti` (`jti`,`group_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_heartbeat definition

CREATE TABLE IF NOT EXISTS `tsmp_heartbeat` (
  `node_id` varchar(30) NOT NULL,
  `start_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `node_info` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`node_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_node definition

CREATE TABLE IF NOT EXISTS `tsmp_node` (
  `id` varchar(255) NOT NULL,
  `start_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `node` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_node_task definition

CREATE TABLE IF NOT EXISTS `tsmp_node_task` (
  `id` bigint(20) NOT NULL,
  `task_signature` varchar(255) NOT NULL,
  `task_id` varchar(255) NOT NULL,
  `task_arg` varchar(4095) DEFAULT NULL,
  `coordination` varchar(255) NOT NULL,
  `execute_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `notice_node` varchar(255) NOT NULL,
  `node` varchar(255) NOT NULL,
  `notice_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `module_name` varchar(255) DEFAULT NULL,
  `module_version` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_node_task_1` (`task_signature`,`task_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_notice_log definition

CREATE TABLE IF NOT EXISTS `tsmp_notice_log` (
  `notice_log_id` bigint(20) NOT NULL,
  `notice_src` varchar(100) NOT NULL,
  `notice_mthd` varchar(10) NOT NULL,
  `notice_key` varchar(255) NOT NULL,
  `detail_id` bigint(20) DEFAULT NULL,
  `last_notice_date_time` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`notice_log_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_open_apikey definition

CREATE TABLE IF NOT EXISTS `tsmp_open_apikey` (
  `open_apikey_id` bigint(20) NOT NULL,
  `client_id` varchar(255) NOT NULL,
  `open_apikey` varchar(1024) NOT NULL,
  `secret_key` varchar(1024) NOT NULL,
  `open_apikey_alias` varchar(255) NOT NULL,
  `times_quota` int(11) NOT NULL DEFAULT -1,
  `times_threshold` int(11) NOT NULL DEFAULT -1,
  `expired_at` bigint(20) NOT NULL,
  `revoked_at` bigint(20) DEFAULT NULL,
  `open_apikey_status` varchar(1) NOT NULL DEFAULT '1',
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `rollover_flag` varchar(1) NOT NULL DEFAULT 'n',
  PRIMARY KEY (`open_apikey_id`),
  UNIQUE KEY `uk_tsmp_open_apikey_01` (`open_apikey`) USING HASH
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_open_apikey_map definition

CREATE TABLE IF NOT EXISTS `tsmp_open_apikey_map` (
  `open_apikey_map_id` bigint(20) NOT NULL,
  `ref_open_apikey_id` bigint(20) NOT NULL,
  `ref_api_uid` varchar(36) NOT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`open_apikey_map_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_organization definition

CREATE TABLE IF NOT EXISTS `tsmp_organization` (
  `org_id` varchar(255) NOT NULL,
  `org_name` varchar(30) DEFAULT NULL,
  `parent_id` varchar(10) DEFAULT NULL,
  `org_path` varchar(255) DEFAULT NULL,
  `create_user` varchar(255) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `update_user` varchar(255) DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `contact_name` varchar(50) DEFAULT NULL,
  `contact_tel` varchar(50) DEFAULT NULL,
  `contact_mail` varchar(100) DEFAULT NULL,
  `org_code` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`org_id`),
  UNIQUE KEY `org_name` (`org_name`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_otp definition

CREATE TABLE IF NOT EXISTS `tsmp_otp` (
  `opaque` varchar(100) NOT NULL,
  `otp` varchar(10) DEFAULT NULL,
  `err_times` int(11) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `valid_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `check_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `used` char(1) DEFAULT NULL,
  PRIMARY KEY (`opaque`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_reg_host definition

CREATE TABLE IF NOT EXISTS `tsmp_reg_host` (
  `reghost_id` varchar(10) NOT NULL,
  `reghost` varchar(30) NOT NULL,
  `reghost_status` char(1) NOT NULL DEFAULT 's',
  `enabled` char(1) NOT NULL DEFAULT 'n',
  `clientid` varchar(40) NOT NULL,
  `heartbeat` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `memo` varchar(300) DEFAULT NULL,
  `create_user` varchar(255) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `update_user` varchar(255) DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`reghost_id`),
  UNIQUE KEY `reghost` (`reghost`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_reg_module definition

CREATE TABLE IF NOT EXISTS `tsmp_reg_module` (
  `reg_module_id` bigint(20) NOT NULL,
  `module_name` varchar(255) NOT NULL,
  `module_version` varchar(255) NOT NULL,
  `module_src` varchar(1) NOT NULL,
  `latest` varchar(1) NOT NULL DEFAULT 'n',
  `upload_date_time` datetime NOT NULL,
  `upload_user` varchar(255) NOT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`reg_module_id`),
  UNIQUE KEY `tsmp_reg_module_uk` (`module_name`,`module_version`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_report_data definition

CREATE TABLE IF NOT EXISTS `tsmp_report_data` (
  `id` bigint(20) NOT NULL,
  `report_type` int(11) NOT NULL,
  `date_time_range_type` int(11) NOT NULL,
  `last_row_date_time` datetime NOT NULL,
  `statistics_status` char(1) NOT NULL,
  `string_group1` varchar(255) DEFAULT NULL,
  `string_group2` varchar(255) DEFAULT NULL,
  `string_group3` varchar(255) DEFAULT NULL,
  `int_value1` bigint(20) DEFAULT NULL,
  `int_value2` bigint(20) DEFAULT NULL,
  `int_value3` bigint(20) DEFAULT NULL,
  `orgid` varchar(255) NOT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_report_url definition

CREATE TABLE IF NOT EXISTS `tsmp_report_url` (
  `report_id` varchar(8) NOT NULL,
  `time_range` char(1) NOT NULL,
  `report_url` varchar(2000) NOT NULL,
  UNIQUE KEY `uk_report` (`report_id`,`time_range`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_req_log definition

CREATE TABLE IF NOT EXISTS `tsmp_req_log` (
  `id` varchar(63) NOT NULL,
  `rtime` datetime NOT NULL,
  `atype` varchar(3) NOT NULL,
  `module_name` varchar(255) NOT NULL,
  `module_version` varchar(255) NOT NULL,
  `node_alias` varchar(255) NOT NULL,
  `node_id` varchar(255) NOT NULL,
  `url` varchar(255) NOT NULL,
  `cip` varchar(255) NOT NULL,
  `orgid` varchar(255) NOT NULL,
  `txid` varchar(255) DEFAULT NULL,
  `entry` varchar(255) DEFAULT NULL,
  `cid` varchar(255) DEFAULT NULL,
  `tuser` varchar(255) DEFAULT NULL,
  `jti` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_req_log_history definition

CREATE TABLE IF NOT EXISTS `tsmp_req_log_history` (
  `id` varchar(63) NOT NULL,
  `rtime` datetime NOT NULL,
  `atype` varchar(3) NOT NULL,
  `module_name` varchar(255) NOT NULL,
  `module_version` varchar(255) NOT NULL,
  `node_alias` varchar(255) NOT NULL,
  `node_id` varchar(255) NOT NULL,
  `url` varchar(255) NOT NULL,
  `cip` varchar(255) NOT NULL,
  `orgid` varchar(255) NOT NULL,
  `txid` varchar(255) DEFAULT NULL,
  `entry` varchar(255) DEFAULT NULL,
  `cid` varchar(255) DEFAULT NULL,
  `tuser` varchar(255) DEFAULT NULL,
  `jti` varchar(255) DEFAULT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_res_log definition

CREATE TABLE IF NOT EXISTS `tsmp_res_log` (
  `id` varchar(63) NOT NULL,
  `exe_status` char(1) NOT NULL,
  `elapse` int(11) NOT NULL,
  `rcode` varchar(63) NOT NULL,
  `http_status` int(11) NOT NULL,
  `err_msg` text DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_res_log_history definition

CREATE TABLE IF NOT EXISTS `tsmp_res_log_history` (
  `id` varchar(63) NOT NULL,
  `exe_status` char(1) NOT NULL,
  `elapse` int(11) NOT NULL,
  `rcode` varchar(63) NOT NULL,
  `http_status` int(11) NOT NULL,
  `err_msg` text DEFAULT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_role definition

CREATE TABLE IF NOT EXISTS `tsmp_role` (
  `role_id` varchar(10) NOT NULL,
  `role_name` varchar(30) NOT NULL,
  `create_user` varchar(255) DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `role_alias` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_role_alert definition

CREATE TABLE IF NOT EXISTS `tsmp_role_alert` (
  `role_id` varchar(10) NOT NULL,
  `alert_id` int(11) NOT NULL,
  PRIMARY KEY (`role_id`,`alert_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_role_func definition

CREATE TABLE IF NOT EXISTS `tsmp_role_func` (
  `role_id` varchar(10) NOT NULL,
  `func_code` varchar(10) NOT NULL,
  PRIMARY KEY (`role_id`,`func_code`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_role_privilege definition

CREATE TABLE IF NOT EXISTS `tsmp_role_privilege` (
  `role_id` varchar(10) NOT NULL,
  `role_scope` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_role_role_mapping definition

CREATE TABLE IF NOT EXISTS `tsmp_role_role_mapping` (
  `role_name` varchar(50) DEFAULT NULL,
  `role_name_mapping` varchar(50) DEFAULT NULL,
  `role_role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`role_role_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_role_txid_map definition

CREATE TABLE IF NOT EXISTS `tsmp_role_txid_map` (
  `role_txid_map_id` bigint(20) NOT NULL,
  `role_id` varchar(10) NOT NULL,
  `txid` varchar(10) NOT NULL,
  `list_type` varchar(1) NOT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `keyword_search` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`role_txid_map_id`),
  UNIQUE KEY `role_id` (`role_id`,`txid`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_rtn_code definition

CREATE TABLE IF NOT EXISTS `tsmp_rtn_code` (
  `tsmp_rtn_code` varchar(20) NOT NULL,
  `locale` varchar(10) NOT NULL,
  `tsmp_rtn_msg` varchar(300) NOT NULL,
  `tsmp_rtn_desc` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`tsmp_rtn_code`,`locale`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_security_level definition

CREATE TABLE IF NOT EXISTS `tsmp_security_level` (
  `security_level_id` varchar(10) NOT NULL,
  `security_level_name` varchar(30) NOT NULL,
  `security_level_desc` varchar(60) DEFAULT NULL,
  PRIMARY KEY (`security_level_id`),
  UNIQUE KEY `security_level_name` (`security_level_name`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_sess_attrs definition

CREATE TABLE IF NOT EXISTS `tsmp_sess_attrs` (
  `api_session_id` varchar(100) NOT NULL,
  `attr_name` varchar(20) NOT NULL,
  `attr_values` text DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`api_session_id`,`attr_name`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_session definition

CREATE TABLE IF NOT EXISTS `tsmp_session` (
  `api_session_id` varchar(100) NOT NULL,
  `cust_id` varchar(30) DEFAULT NULL,
  `cust_name` varchar(20) DEFAULT NULL,
  `const_data` varchar(1000) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `user_ip` varchar(15) DEFAULT NULL,
  `is_login` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`api_session_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_setting definition

CREATE TABLE IF NOT EXISTS `tsmp_setting` (
  `id` varchar(255) NOT NULL,
  `value` varchar(4095) DEFAULT NULL,
  `memo` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_sso_user_secret definition

CREATE TABLE IF NOT EXISTS `tsmp_sso_user_secret` (
  `user_secret_id` bigint(20) NOT NULL,
  `user_name` varchar(50) DEFAULT NULL,
  `secret` varchar(100) DEFAULT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'sso system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`user_secret_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_token_history definition

CREATE TABLE IF NOT EXISTS `tsmp_token_history` (
  `seq_no` bigint(20) NOT NULL,
  `user_nid` varchar(255) DEFAULT NULL,
  `user_name` varchar(50) DEFAULT NULL,
  `client_id` varchar(40) NOT NULL,
  `token_jti` varchar(100) NOT NULL,
  `scope` varchar(2048) DEFAULT NULL,
  `expired_at` datetime NOT NULL,
  `create_at` datetime NOT NULL,
  `stime` datetime DEFAULT NULL,
  `revoked_at` datetime DEFAULT NULL,
  `revoked_status` char(2) DEFAULT NULL,
  `retoken_jti` varchar(100) NOT NULL,
  `reexpired_at` datetime NOT NULL,
  `rft_revoked_at` datetime DEFAULT NULL,
  `rft_revoked_status` varchar(10) DEFAULT NULL,
  `token_quota` int(11) DEFAULT NULL,
  `token_used` int(11) DEFAULT NULL,
  `rft_quota` int(11) DEFAULT NULL,
  `rft_used` int(11) DEFAULT NULL,
  PRIMARY KEY (`seq_no`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_token_history_housing definition

CREATE TABLE IF NOT EXISTS `tsmp_token_history_housing` (
  `seq_no` bigint(20) NOT NULL,
  `user_nid` varchar(255) DEFAULT NULL,
  `user_name` varchar(50) DEFAULT NULL,
  `client_id` varchar(40) NOT NULL,
  `token_jti` varchar(100) NOT NULL,
  `scope` varchar(2048) DEFAULT NULL,
  `expired_at` datetime NOT NULL,
  `create_at` datetime NOT NULL,
  `stime` datetime DEFAULT NULL,
  `revoked_at` datetime DEFAULT NULL,
  `revoked_status` char(2) DEFAULT NULL,
  `retoken_jti` varchar(100) NOT NULL,
  `reexpired_at` datetime NOT NULL,
  PRIMARY KEY (`seq_no`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_token_usage_count definition

CREATE TABLE IF NOT EXISTS `tsmp_token_usage_count` (
  `token_jti` varchar(100) NOT NULL,
  `times_threshold` int(11) NOT NULL,
  `token_type` char(1) NOT NULL,
  `expired_at` datetime NOT NULL,
  PRIMARY KEY (`token_jti`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_token_usage_history definition

CREATE TABLE IF NOT EXISTS `tsmp_token_usage_history` (
  `seq_id` int(11) NOT NULL,
  `tgtl_seq_id` int(11) NOT NULL,
  `token_jti` varchar(100) NOT NULL,
  `scope` varchar(2048) DEFAULT NULL,
  `txtime` datetime DEFAULT NULL,
  `expiredtime` datetime DEFAULT NULL,
  PRIMARY KEY (`seq_id`,`tgtl_seq_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_txkey definition

CREATE TABLE IF NOT EXISTS `tsmp_txkey` (
  `key_id` bigint(20) NOT NULL,
  `tx_key` varchar(64) NOT NULL,
  `iv` varchar(64) DEFAULT NULL,
  `alg` char(1) NOT NULL,
  `create_time` datetime NOT NULL
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_txtoken definition

CREATE TABLE IF NOT EXISTS `tsmp_txtoken` (
  `txtoken` varchar(64) NOT NULL,
  `txtoken_status` char(1) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `use_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`txtoken`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_user definition

CREATE TABLE IF NOT EXISTS `tsmp_user` (
  `user_id` varchar(10) NOT NULL,
  `user_name` varchar(50) NOT NULL,
  `user_status` char(1) NOT NULL,
  `user_email` varchar(100) NOT NULL,
  `logon_date` datetime DEFAULT NULL,
  `logoff_date` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `create_user` varchar(255) DEFAULT NULL,
  `create_time` datetime NOT NULL,
  `pwd_fail_times` int(11) NOT NULL,
  `org_id` varchar(255) DEFAULT NULL,
  `user_alias` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `user_name` (`user_name`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_vgroup definition

CREATE TABLE IF NOT EXISTS `tsmp_vgroup` (
  `vgroup_id` varchar(10) NOT NULL,
  `vgroup_name` varchar(150) NOT NULL,
  `vgroup_alias` varchar(255) DEFAULT NULL,
  `vgroup_desc` varchar(1500) DEFAULT NULL,
  `vgroup_access` varchar(255) DEFAULT NULL,
  `security_level_id` varchar(10) DEFAULT NULL,
  `allow_days` int(11) NOT NULL DEFAULT 0,
  `allow_times` int(11) NOT NULL DEFAULT 0,
  `create_user` varchar(255) NOT NULL,
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`vgroup_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_vgroup_authorities_map definition

CREATE TABLE IF NOT EXISTS `tsmp_vgroup_authorities_map` (
  `vgroup_id` varchar(10) NOT NULL,
  `vgroup_authoritie_id` varchar(10) NOT NULL,
  PRIMARY KEY (`vgroup_id`,`vgroup_authoritie_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_vgroup_group definition

CREATE TABLE IF NOT EXISTS `tsmp_vgroup_group` (
  `vgroup_id` varchar(10) NOT NULL,
  `group_id` varchar(10) NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`vgroup_id`,`group_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmpn_api_module definition

CREATE TABLE IF NOT EXISTS `tsmpn_api_module` (
  `id` bigint(20) NOT NULL,
  `module_name` varchar(255) NOT NULL,
  `module_version` varchar(255) NOT NULL,
  `module_app_class` varchar(255) NOT NULL,
  `module_bytes` longblob NOT NULL,
  `module_md5` varchar(255) NOT NULL,
  `module_type` varchar(255) NOT NULL,
  `upload_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `uploader_name` varchar(255) NOT NULL,
  `status_time` timestamp NULL DEFAULT NULL,
  `status_user` varchar(255) DEFAULT NULL,
  `active` tinyint(1) NOT NULL,
  `node_task_id` bigint(20) DEFAULT NULL,
  `org_id` varchar(255) DEFAULT NULL,
  `target_version` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_api_module_1` (`module_name`,`module_version`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmpn_node_task definition

CREATE TABLE IF NOT EXISTS `tsmpn_node_task` (
  `id` bigint(20) NOT NULL,
  `task_signature` varchar(255) NOT NULL,
  `task_id` varchar(255) NOT NULL,
  `task_arg` varchar(4095) DEFAULT NULL,
  `coordination` varchar(255) NOT NULL,
  `execute_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `notice_node` varchar(255) NOT NULL,
  `node` varchar(255) NOT NULL,
  `notice_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_node_task_2` (`task_signature`,`task_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmpn_site definition

CREATE TABLE IF NOT EXISTS `tsmpn_site` (
  `site_id` int(11) NOT NULL AUTO_INCREMENT,
  `site_code` varchar(30) NOT NULL,
  `site_memo` text DEFAULT NULL,
  `active` bit(1) NOT NULL,
  `create_user` varchar(255) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `update_user` varchar(255) DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `protocol_type` varchar(20) NOT NULL,
  `binding_ip` varchar(20) NOT NULL,
  `binding_port` int(11) NOT NULL,
  `app_pool` varchar(255) NOT NULL,
  `root_path` text DEFAULT NULL,
  `clr_version` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`site_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmpn_site_module definition

CREATE TABLE IF NOT EXISTS `tsmpn_site_module` (
  `site_id` int(11) NOT NULL,
  `module_id` int(11) NOT NULL,
  `node_task_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`site_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmpn_site_node definition

CREATE TABLE IF NOT EXISTS `tsmpn_site_node` (
  `node` varchar(30) NOT NULL,
  `site_id` int(11) NOT NULL,
  `node_task_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`node`,`site_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.users definition

CREATE TABLE IF NOT EXISTS `users` (
  `username` varchar(50) NOT NULL,
  `password` varchar(60) NOT NULL,
  `enabled` tinyint(1) NOT NULL,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.authorities definition

CREATE TABLE IF NOT EXISTS `authorities` (
  `username` varchar(50) NOT NULL,
  `authority` varchar(50) NOT NULL,
  UNIQUE KEY `ix_auth_username` (`username`,`authority`),
  CONSTRAINT `fk_authorities_users` FOREIGN KEY (`username`) REFERENCES `users` (`username`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.group_authorities definition

CREATE TABLE IF NOT EXISTS `group_authorities` (
  `group_id` bigint(20) NOT NULL,
  `authority` varchar(50) NOT NULL,
  UNIQUE KEY `ix_group_authorities_group` (`group_id`),
  CONSTRAINT `fk_group_authorities_group` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.group_members definition

CREATE TABLE IF NOT EXISTS `group_members` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `group_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ik_group_members_group` (`group_id`),
  CONSTRAINT `fk_group_members_group` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_api_detail definition

CREATE TABLE IF NOT EXISTS `tsmp_api_detail` (
  `id` bigint(20) NOT NULL,
  `api_module_id` bigint(20) NOT NULL,
  `api_key` varchar(255) DEFAULT NULL,
  `api_name` varchar(255) DEFAULT NULL,
  `path_of_json` varchar(1024) NOT NULL,
  `method_of_json` varchar(1023) NOT NULL,
  `params_of_json` varchar(1023) NOT NULL,
  `headers_of_json` varchar(1023) NOT NULL,
  `consumes_of_json` varchar(1023) NOT NULL,
  `produces_of_json` varchar(1023) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_api_detail_1` (`api_module_id`,`api_key`),
  CONSTRAINT `fk_api_detail_1` FOREIGN KEY (`api_module_id`) REFERENCES `tsmp_api_module` (`id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmp_node_task_work definition

CREATE TABLE IF NOT EXISTS `tsmp_node_task_work` (
  `id` bigint(20) NOT NULL,
  `node_task_id` bigint(20) NOT NULL,
  `competitive_id` varchar(255) NOT NULL,
  `competitive_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `competitive_node` varchar(255) NOT NULL,
  `node` varchar(255) NOT NULL,
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `success` tinyint(1) DEFAULT NULL,
  `error_msg` varchar(1023) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_node_task_work_1` (`node_task_id`,`competitive_id`),
  CONSTRAINT `fk_node_task_work_2` FOREIGN KEY (`node_task_id`) REFERENCES `tsmp_node_task` (`id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmpn_api_detail definition

CREATE TABLE IF NOT EXISTS `tsmpn_api_detail` (
  `id` bigint(20) NOT NULL,
  `api_module_id` bigint(20) NOT NULL,
  `api_key` varchar(255) NOT NULL,
  `api_name` varchar(255) NOT NULL,
  `path_of_json` varchar(1024) NOT NULL,
  `method_of_json` varchar(1023) NOT NULL,
  `params_of_json` varchar(1023) NOT NULL,
  `headers_of_json` varchar(1023) NOT NULL,
  `consumes_of_json` varchar(1023) NOT NULL,
  `produces_of_json` varchar(1023) NOT NULL,
  `url_rid` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_apin_detail_1` (`api_module_id`,`api_key`),
  CONSTRAINT `fk_apin_detail_1` FOREIGN KEY (`api_module_id`) REFERENCES `tsmpn_api_module` (`id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;


-- digirunner.tsmpn_node_task_work definition

CREATE TABLE IF NOT EXISTS `tsmpn_node_task_work` (
  `id` bigint(20) NOT NULL,
  `node_task_id` bigint(20) NOT NULL,
  `competitive_id` varchar(255) NOT NULL,
  `competitive_time` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `competitive_node` varchar(255) NOT NULL,
  `node` varchar(255) NOT NULL,
  `update_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `success` tinyint(1) DEFAULT NULL,
  `error_msg` varchar(1023) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_node_task_work_3` (`node_task_id`,`competitive_id`),
  CONSTRAINT `fk_node_task_work_4` FOREIGN KEY (`node_task_id`) REFERENCES `tsmpn_node_task` (`id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;



-- digirunner.cus_setting definition

CREATE TABLE IF NOT EXISTS `cus_setting` (
  `cus_setting_id` bigint(20) NOT NULL,
  `setting_no` varchar(20) NOT NULL,
  `setting_name` varchar(100) NOT NULL,
  `subsetting_no` varchar(20) NOT NULL,
  `subsetting_name` varchar(100) NOT NULL,
  `sort_by` int(11) NOT NULL DEFAULT 0,
  `is_default` varchar(1) DEFAULT NULL,
  `param1` varchar(255) DEFAULT NULL,
  `param2` varchar(255) DEFAULT NULL,
  `param3` varchar(255) DEFAULT NULL,
  `param4` varchar(255) DEFAULT NULL,
  `param5` varchar(255) DEFAULT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'SYSTEM',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `keyword_search` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`setting_no`,`subsetting_no`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE SEQUENCE  SEQ_TSMP_USER_PK INCREMENT BY 1 START WITH 2000000000;
CREATE SEQUENCE  SEQ_TSMP_NODE_TASK_WORK_PK INCREMENT BY 1 START WITH 2000000000;
CREATE SEQUENCE  SEQ_TSMP_NODE_TASK_PK INCREMENT BY 1 START WITH 2000000000;
CREATE SEQUENCE  SEQ_TSMP_NODE_PK INCREMENT BY 1 START WITH 2000000000;
CREATE SEQUENCE  SEQ_TSMP_DC_PK INCREMENT BY 1 START WITH 2000000000;
CREATE SEQUENCE  SEQ_TSMP_CLIENT_HOST_PK INCREMENT BY 1 START WITH 2000000000;
CREATE SEQUENCE  SEQ_TSMP_API_MODULE_PK INCREMENT BY 1 START WITH 2000000000;
CREATE SEQUENCE  SEQ_TSMP_API_DETAIL_PK INCREMENT BY 1 START WITH 2000000000;
CREATE SEQUENCE  SEQ_TOKEN_USAGE_HISTORY_PK INCREMENT BY 1 START WITH 2000000000;
CREATE SEQUENCE  SEQ_TOKEN_HISTORY_HOUSING_PK INCREMENT BY 1 START WITH 2000000000;
CREATE SEQUENCE  SEQ_TSMP_GROUP_TIMES_LOG_PK INCREMENT BY 1 START WITH 2000000000;
CREATE SEQUENCE  SEQ_TSMP_TOKEN_HISTORY_PK INCREMENT BY 1 START WITH 2000000000;
CREATE SEQUENCE  SEQ_TSMP_VGROUP_PK INCREMENT BY 1 START WITH 2000000000;
CREATE SEQUENCE  SEQ_TSMP_EVENTS_PK INCREMENT BY 1 START WITH 2000000000;
CREATE SEQUENCE  SEQ_TSMP_ALERT_LOG_PK START WITH 1 INCREMENT BY 2000000000;
CREATE SEQUENCE  SEQ_TSMP_GROUP_PK INCREMENT BY 1 START WITH 2000000000;
CREATE SEQUENCE  SEQ_TSMP_ROLE_PK INCREMENT BY 1 START WITH 2000000000;
CREATE SEQUENCE  SEQ_TSMP_ORGANIZATION_PK INCREMENT BY 1 START WITH 2000000000;
CREATE SEQUENCE  SEQ_TSMP_ALERT_PK INCREMENT BY 1 START WITH 2000000000;


-- 20230105, v4 新增 SSO IdP使用者基本資料, Mini Lee
CREATE TABLE IF NOT EXISTS dgr_ac_idp_user ( 
	ac_idp_user_id 			BIGINT 			NOT NULL, 				-- ID 使用 RandomSeqLongUtil.getRandomLongByDefault() 產生 
	user_name 				NVARCHAR(400)	NOT NULL, 				-- 使用者名稱(視IdP類型決定) 
	user_alias 				VARCHAR(200), 							-- 使用者別名 
	user_status 			VARCHAR(1) 		NOT NULL DEFAULT '1', 	-- 使用者狀態 1：request(預設)，2：allow，"3：denny 
	user_email 				VARCHAR(500), 							-- 使用者E-Mail 
	org_id 					VARCHAR(200), 							-- 組織ID from TSMP_ORGANIZATION.org_id 
	idp_type 				VARCHAR(50) 	NOT NULL, 				-- IdP類型 例如:"MS" 或 "GOOGLE" 
	code1					BIGINT,									-- 安全驗證碼1
	code2					BIGINT,									-- 安全驗證碼2
	id_token_jwtstr			TEXT,							-- IdP ID Token 的 JWT  	
	access_token_jwtstr		TEXT,							-- IdP Access Token 的 JWT  	
	refresh_token_jwtstr	TEXT,							-- IdP Refresh Token 的 JWT  	
	create_date_time 		DATETIME 		DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間
	create_user 			VARCHAR(255) 	DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 		DATETIME, 								-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料
	update_user 			VARCHAR(255), 							-- 更新人員 
	version 				INT DEFAULT 1, 							-- 版號 C/U時, 增量+1
	PRIMARY KEY (ac_idp_user_id), 
	UNIQUE (user_name, idp_type) 
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20230105, v4 新增 SSO IdP授權碼記錄檔, Mini Lee
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
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20230105, v4 新增 SSO IdP資料, Mini Lee
CREATE TABLE IF NOT EXISTS dgr_ac_idp_info ( 
	ac_idp_info_id 		BIGINT 			NOT NULL, 				-- ID 使用 RandomSeqLongUtil 機制產生 
	idp_type 			VARCHAR(50) 	NOT NULL, 				-- IdP類型 例如:"MS" 或 "GOOGLE"  
	client_id 			NVARCHAR(400) 	NOT NULL, 				-- 用戶端編號(視IdP類型決定) 
	client_mima 		VARCHAR(200) 	NOT NULL, 				-- 用戶端密碼 
	client_name 		VARCHAR(200), 							-- 用戶端名稱 
	client_status 		VARCHAR(1) 		NOT NULL DEFAULT 'Y', 	-- 用戶端狀態 Y: 啟用 (預設), N: 停用 
	well_known_url 		TEXT 	NOT NULL, 				-- IdP 的 Well Known URL 
	callback_url 		VARCHAR(400) 	NOT NULL, 				-- 已授權的重新導向 URI
	auth_url 			TEXT, 							-- IdP 的 Auth URL 
	access_token_url 	TEXT, 							-- IdP 的 Access Token URL 
	scope 				TEXT, 							-- IdP 的 scope 
	create_date_time 	DATETIME 		DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間
	create_user 		VARCHAR(255) 	DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 	DATETIME, 								-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料
	update_user 		VARCHAR(255), 							-- 更新人員 
	version 			INT DEFAULT 1, 							-- 版號 C/U時, 增量+1
	PRIMARY KEY (ac_idp_info_id), 
	UNIQUE (idp_type, client_id) 
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20230105, v4 拿掉 Authorities 的 FOREIGN KEY (username) REFERENCES users(username), Mini Lee
ALTER TABLE authorities DROP CONSTRAINT fk_authorities_users ;

-- 20230105, v4 增加 Authorities.username 長度為 NVARCHAR(400), Mini Lee
ALTER TABLE authorities MODIFY COLUMN username NVARCHAR(400);

-- 20230223 v4 新增 SSO AC IdP資料 (LDAP), Mini Lee
CREATE TABLE IF NOT EXISTS dgr_ac_idp_info_ldap (  
	ac_idp_info_ldap_id 	BIGINT 			NOT NULL, 				-- ID 
	ldap_url 				TEXT 	NOT NULL, 				-- Ldap登入的URL 
	ldap_dn 				TEXT 	NOT NULL, 				-- Ldap登入的使用者DN 
	ldap_timeout 			INT 			NOT NULL, 				-- Ldap登入的連線timeout,單位毫秒 
	ldap_status 			VARCHAR(1) 		NOT NULL DEFAULT 'Y', 	-- Ldap狀態 
	approval_result_mail 	TEXT 	NOT NULL, 				-- 審核結果收件人,多組以逗號(,)隔開 
	icon_file				TEXT,							-- 圖示檔案
	create_date_time 		DATETIME 		DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	create_user 			VARCHAR(255) 	DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 		DATETIME, 								-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料 
	update_user 			VARCHAR(255), 							-- 更新人員 
	version 				INT 			DEFAULT 1, 				-- 版號 C/U時, 增量+1
	PRIMARY KEY (ac_idp_info_ldap_id) 
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20230320, v4, SSO AC IdP資料 (LDAP), 增加欄位, Mini Lee
ALTER TABLE dgr_ac_idp_info_ldap ADD page_title VARCHAR(400);

-- 20230325, v4, 增加長度, Tom
ALTER TABLE tsmp_api_reg MODIFY COLUMN method_of_json NVARCHAR(200);
ALTER TABLE tsmp_api_imp MODIFY COLUMN method_of_json VARCHAR(200);

-- 20230330 TSMP_DP_REQ_ORDERM 更改為 REQ_USER_ID varchar(255) Zoe_Lee
ALTER TABLE tsmp_dp_req_orderm MODIFY COLUMN req_user_id varchar(255) ;

-- 20230406 v4 新增websocket proxy, Tom
CREATE TABLE IF NOT EXISTS dgr_web_socket_mapping (  
	ws_mapping_id 	        BIGINT 			NOT NULL, 				-- ID 
	site_name 				VARCHAR(50) 	NOT NULL, 			    -- 站點名稱
	target_ws               VARCHAR(200) 	NOT NULL,	            -- web socket server的目標
	memo                    TEXT, 				            -- 備註
	create_date_time 		DATETIME 		DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	create_user 			VARCHAR(255) 	DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 		DATETIME, 								-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料 
	update_user 			VARCHAR(255), 							-- 更新人員 
	version 				INT 			DEFAULT 1, 				-- 版號 C/U時, 增量+1
	keyword_search		    VARCHAR(250),						-- LikeSearch使用: site_name | target_ws
	CONSTRAINT PK_DGR_WEB_SOCKET_MAPPING PRIMARY KEY (ws_mapping_id),
	CONSTRAINT UK_DGR_WEB_SOCKET_MAPPING UNIQUE (site_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20230327, v4 新增 Gateway IdP Auth記錄檔主檔	, Mini Lee
CREATE TABLE IF NOT EXISTS dgr_gtw_idp_auth_m (
	gtw_idp_auth_m_id BIGINT NOT NULL, -- ID (流水號)
	state VARCHAR(40) NOT NULL, -- 隨機字串UUID
	idp_type VARCHAR(50) NOT NULL, -- IdP類型
	client_id VARCHAR(40) NOT NULL, -- dgR 的 client_id
	auth_code VARCHAR(50), -- 授權碼, 即 dgRcode
	create_date_time DATETIME DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間
	create_user VARCHAR(255) DEFAULT 'SYSTEM', -- 建立人員
	update_date_time DATETIME, -- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料
	update_user VARCHAR(255), -- 更新人員
	version INT DEFAULT 1, -- 版號 C/U時, 增量+1
	PRIMARY KEY (gtw_idp_auth_m_id),
	UNIQUE KEY (state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20230327 v4 新增 Gateway IdP Auth記錄檔明細檔, Mini Lee
CREATE TABLE IF NOT EXISTS `dgr_gtw_idp_auth_d` (
  `gtw_idp_auth_d_id` bigint(20) NOT NULL,
  `ref_gtw_idp_auth_m_id` bigint(20) NOT NULL,
  `scope` varchar(200) NOT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`gtw_idp_auth_d_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20230327, v4 新增 Gateway IdP授權碼記錄檔, Mini Lee
CREATE TABLE IF NOT EXISTS `dgr_gtw_idp_auth_code` (
  `gtw_idp_auth_code_id` bigint(20) NOT NULL,
  `auth_code` varchar(50) NOT NULL,
  `phase` varchar(10) NOT NULL,
  `status` varchar(1) NOT NULL DEFAULT 'A',
  `expire_date_time` bigint(20) NOT NULL,
  `idp_type` varchar(50) NOT NULL,
  `client_id` varchar(40) DEFAULT NULL,
  `user_name` varchar(400) NOT NULL,
  `user_alias` nvarchar(400) DEFAULT NULL,
  `user_email` varchar(500) DEFAULT NULL,
  `user_picture` TEXT(4000) DEFAULT NULL,
  `id_token_jwtstr` TEXT(4000) DEFAULT NULL,
  `access_token_jwtstr` TEXT(4000) DEFAULT NULL,
  `refresh_token_jwtstr` TEXT(4000) DEFAULT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`gtw_idp_auth_code_id`),
  UNIQUE KEY `auth_code` (`auth_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20230327 v4 新增 Gateway IdP資料 (Oauth2.0 GOOGLE / MS), Mini Lee
CREATE TABLE IF NOT EXISTS `dgr_gtw_idp_info_o` (
  `gtw_idp_info_o_id` bigint(20) NOT NULL,
  `client_id` varchar(40) NOT NULL,
  `idp_type` varchar(50) NOT NULL,
  `status` varchar(1) NOT NULL DEFAULT 'Y',
  `remark` varchar(200) DEFAULT NULL,
  `idp_client_id` nvarchar(400) NOT NULL,
  `idp_client_mima` varchar(200) NOT NULL,
  `idp_client_name` varchar(200) DEFAULT NULL,
  `well_known_url` TEXT(4000) DEFAULT NULL,
  `callback_url` varchar(400) NOT NULL,
  `auth_url` TEXT(4000) DEFAULT NULL,
  `access_token_url` TEXT(4000) DEFAULT NULL,
  `scope` TEXT(4000) DEFAULT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`gtw_idp_info_o_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20230327 v4 新增 Gateway IdP資料 (LDAP), Mini Lee
CREATE TABLE IF NOT EXISTS `dgr_gtw_idp_info_l` (
  `gtw_idp_info_l_id` bigint(20) NOT NULL,
  `client_id` varchar(40) NOT NULL,
  `status` varchar(1) NOT NULL DEFAULT 'Y',
  `remark` varchar(200) DEFAULT NULL,
  `ldap_url` TEXT(4000) NOT NULL,
  `ldap_dn` TEXT(4000) NOT NULL,
  `ldap_timeout` int(11) NOT NULL,
  `icon_file` TEXT(4000) DEFAULT NULL,
  `page_title` varchar(400) NOT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`gtw_idp_info_l_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- 20230327 v4 新增 Gateway IdP資料 (JDBC), Mini Lee
CREATE TABLE IF NOT EXISTS `dgr_gtw_idp_info_j` (
  `gtw_idp_info_j_id` bigint(20) NOT NULL,
  `client_id` varchar(40) NOT NULL,
  `idp_type` varchar(50) NOT NULL,
  `status` varchar(1) NOT NULL DEFAULT 'Y',
  `remark` varchar(200) DEFAULT NULL,
  `host` TEXT(4000) NOT NULL,
  `port` int(11) NOT NULL,
  `db_schema` varchar(200) NOT NULL,
  `db_user_name` varchar(200) NOT NULL,
  `db_user_mima` varchar(200) NOT NULL,
  `icon_file` TEXT(4000) DEFAULT NULL,
  `page_title` varchar(400) NOT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  PRIMARY KEY (`gtw_idp_info_j_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20230407, v4 網站反向代理主檔, Kevin Cheng
CREATE TABLE IF NOT EXISTS `dgr_website` (
  `dgr_website_id` bigint(20) NOT NULL,
  `website_name` varchar(50) NOT NULL,
  `website_status` varchar(1) NOT NULL DEFAULT 'Y',
  `remark` varchar(500) DEFAULT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `keyword_search` varchar(600) DEFAULT NULL,
  PRIMARY KEY (`dgr_website_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20230407, v4 網站反向代理明細檔, Kevin Cheng
CREATE TABLE IF NOT EXISTS `dgr_website_detail` (
  `dgr_website_detail_id` bigint(20) NOT NULL,
  `dgr_website_id` bigint(20) NOT NULL,
  `probability` int(11) NOT NULL,
  `url` varchar(1000) NOT NULL,
  `content_path` varchar(200) DEFAULT NULL,
  `create_date_time` datetime DEFAULT current_timestamp(),
  `create_user` varchar(255) DEFAULT 'system',
  `update_date_time` datetime DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  `version` int(11) DEFAULT 1,
  `keyword_search` varchar(1500) DEFAULT NULL,
  PRIMARY KEY (`dgr_website_detail_id`)
) ENGINE=InnoDB DEFAULT charset=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20230407, v4 入口網(DP)的Application	, Kevin Cheng
CREATE TABLE IF NOT EXISTS dp_app (
	dp_application_id BIGINT NOT NULL,                         -- ID
	application_name VARCHAR(50) NOT NULL,                     -- Application名稱
	application_desc VARCHAR(500),                             -- Application說明
	client_id VARCHAR(40) NOT NULL,                            -- CLIENT_ID
	open_apikey_id BIGINT,                                     -- 
	open_apikey_status VARCHAR(1),                             -- DGRK狀態
	user_name NVARCHAR(400) NOT NULL,                          -- 使用者名稱(視IdP類型決定)
	id_token_jwtstr TEXT NOT NULL,                    -- IdP ID Token 的 JWT
	create_date_time DATETIME DEFAULT CURRENT_TIMESTAMP,       -- 建立日期
	create_user VARCHAR(255) DEFAULT 'SYSTEM',                 -- 建立人員
	update_date_time DATETIME,                                 -- 更新日期
	update_user VARCHAR(255),                                  -- 更新人員
	version INT DEFAULT 1,                                     -- 版號
  keyword_search varchar(600) DEFAULT NULL,
	PRIMARY KEY (dp_application_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20230420, v4 入口網(DP)的使用者	, Kevin Cheng
CREATE TABLE IF NOT EXISTS dp_user (
    dp_user_id BIGINT NOT NULL,                           -- ID
    user_name NVARCHAR(400) NOT NULL,                     -- 使用者名稱(視IdP類型決定)
    user_alias VARCHAR(200),                              -- 使用者別名
    id_token_jwtstr TEXT NOT NULL,               -- IdP ID Token 的 JWT
    user_identity VARCHAR(1) NOT NULL DEFAULT 'U',                 -- 使用者身份
    create_date_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 建立日期
    create_user VARCHAR(255) DEFAULT 'SYSTEM',            -- 建立人員
    update_date_time DATETIME,                            -- 更新日期
    update_user VARCHAR(255),                             -- 更新人員
    version INT DEFAULT 1,                                -- 版號
    keyword_search varchar(800) DEFAULT NULL,
    PRIMARY KEY (dp_user_id),
    UNIQUE KEY (user_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20230420, v4 入口網(DP)的API_DOC檔案	, Kevin Cheng
CREATE TABLE IF NOT EXISTS dp_file (
    dp_file_id BIGINT NOT NULL,                              -- ID
    file_name NVARCHAR(100) NOT NULL,                        -- 檔案名稱
    module_name NVARCHAR(150) NOT NULL,                      -- Module Name
    api_key NVARCHAR(255) NOT NULL,                                   -- API Key
    blob_data BLOB NOT NULL,                                 -- 檔案本體
    create_date_time DATETIME DEFAULT CURRENT_TIMESTAMP,     -- 建立日期
    create_user VARCHAR(255) DEFAULT 'SYSTEM',               -- 建立人員
    update_date_time DATETIME,                               -- 更新日期
    update_user VARCHAR(255),                                -- 更新人員
    version INT DEFAULT 1,                                   -- 版號
    PRIMARY KEY (dp_file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- 20230421, TSMP access_token 歷史紀錄, 增加欄位, Mini Lee
ALTER TABLE tsmp_token_history ADD idp_type VARCHAR(50);

-- 20230502, TSMP Token 歷史紀錄, 增加欄位, Mini Lee
ALTER TABLE tsmp_token_history ADD id_token_jwtstr TEXT;
ALTER TABLE tsmp_token_history ADD refresh_token_jwtstr TEXT;

-- 20230515, v4 入口網(DP)的Application	, 刪除欄位 , Kevin Cheng
ALTER TABLE dp_app DROP COLUMN open_apikey_status;

-- 20230531, SSO AC IdP資料 (LDAP), 增加欄位, Mini Lee
ALTER TABLE dgr_ac_idp_info_ldap ADD ldap_base_dn TEXT;

-- 20230531, Gateway IdP資料 (LDAP), 增加欄位, Mini Lee
ALTER TABLE dgr_gtw_idp_info_l ADD ldap_base_dn TEXT;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--  20230616,dgr_dashboard_api_elapse dashboard   ,zoe Lee
CREATE TABLE IF NOT EXISTS dgr_dashboard_api_elapse (
    id    BIGINT NOT NULL,    -- ID
    rtime    TIMESTAMP NOT NULL,    -- record time
    cid    VARCHAR(50) NOT NULL,    -- token所攜帶的client id
    elapse    INT NOT NULL,    -- API執行耗時(ms)
    module_name    VARCHAR(150) NOT NULL,    -- 模組名稱
    txid    VARCHAR(255) NOT NULL,    -- 其實也就是ApiKey
    api_name    NVARCHAR(500) NOT NULL,    -- api名稱
    exe_status    CHAR NOT NULL,    -- 此次API呼叫成功與否
    http_status    INT NOT NULL,    -- response的http status
    PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20230105, v4 修改 dp_app 欄位型態 , min
ALTER TABLE dp_app MODIFY COLUMN application_name NVARCHAR(50);
ALTER TABLE dp_app MODIFY COLUMN application_desc NVARCHAR(500);

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
    err_msg    TEXT,    -- 錯誤訊息

    PRIMARY KEY(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20230706 刪除dgr_dashboard_api_elapse ,zoe Lee
DROP TABLE dgr_dashboard_api_elapse;

-- 20230726, 增加欄位給dashboard統計用, Tom
ALTER TABLE tsmp_api ADD success BIGINT NOT NULL DEFAULT 0;
ALTER TABLE tsmp_api ADD fail BIGINT NOT NULL DEFAULT 0;
ALTER TABLE tsmp_api ADD total BIGINT NOT NULL DEFAULT 0;
ALTER TABLE tsmp_api ADD elapse BIGINT NOT NULL DEFAULT 0;

-- 20230802 , tsmp_req_res_log_history.rtime 格式改為datetime  , Zoe Lee
ALTER TABLE tsmp_req_res_log_history DROP COLUMN rtime;
ALTER TABLE tsmp_req_res_log_history ADD rtime datetime;

-- 20230802 , tsmp_req_res_log_history增加新欄位year_month  , Zoe Lee
ALTER TABLE tsmp_req_res_log_history ADD rtime_year_month varchar(8);

-- 20230808 , v4 入口網(DP)的 API_VERSION	, Kevin Cheng
CREATE TABLE IF NOT EXISTS dp_api_version (
  dp_api_version_id BIGINT NOT NULL,                  -- ID
  module_name NVARCHAR(150) NOT NULL,                 -- Module Name
  api_key NVARCHAR(255) NOT NULL,                     -- API Key
  dp_api_version NVARCHAR(10) NOT NULL,               -- API版本號
  start_of_life BIGINT NOT NULL,                      -- API生命週期(起)
  end_of_life BIGINT,                                 -- API生命週期(迄)
  remark NVARCHAR(500),                               -- 備註
  time_zone varchar(200) NOT NULL,                    -- 時區
  create_date_time DATETIME DEFAULT CURRENT_TIMESTAMP,-- 建立日期
  create_user VARCHAR(255) DEFAULT 'SYSTEM',          -- 建立人員
  update_date_time DATETIME,                          -- 更新日期
  update_user VARCHAR(255),                           -- 更新人員
  version INT DEFAULT 1,                              -- 版號
  PRIMARY KEY (dp_api_version_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20230824, website proxy	, 刪除content path欄位 , min
ALTER TABLE dgr_website_detail DROP COLUMN content_path;

-- 20230824 SSO AC IdP資料 (Multi-LDAP) 主檔, Mini Lee
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20230824, SSO AC IdP資料 (Multi-LDAP) 明細檔, Mini Lee
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20230829 , dgr_dashboard_es_log  , Zoe Lee
CREATE TABLE dgr_dashboard_es_log (
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
    CONSTRAINT pk_dgr_dashboard_es_log  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 202300906, v4, 移除多餘欄位, Kevin K
ALTER TABLE tsmp_dp_mail_log DROP COLUMN IF EXISTS keyword_search;
-- 202300906, v4, 增加紀錄錯誤訊息欄位, Kevin K
ALTER TABLE tsmp_dp_mail_log ADD COLUMN stack_trace VARCHAR(4000);

-- 20230908, 移除不用的table, Tom
DROP TABLE tsmp_req_log_history;
DROP TABLE tsmp_res_log_history;

-- 20230912, Gateway IdP資料 (API), Mini Lee
CREATE TABLE IF NOT EXISTS dgr_gtw_idp_info_a (  
	gtw_idp_info_a_id 	BIGINT NOT NULL, 					-- ID, 使用 RandomSeqLongUtil 機制產生 
	client_id 			VARCHAR(40) NOT NULL, 				-- 在 digiRunner 註冊的 client_id 
	status 				VARCHAR(1) 	NOT NULL DEFAULT 'Y', 	-- 狀態 
	remark 				VARCHAR(200), 						-- 說明 
	api_method 			VARCHAR(10) NOT NULL, 				-- 登入的 API HTTP method 
	api_url 			TEXT NOT NULL, 			-- 登入的 API URL
	req_header 			TEXT, 						-- 調用 API 的 Request Header 內容 
	req_body_type 		VARCHAR(1) NOT NULL DEFAULT 'N', 	-- 調用 API 的 Request Body 類型 
	req_body 			TEXT, 						-- 調用 API 的 Request Body 內容 
	suc_by_type 		VARCHAR(1) NOT NULL DEFAULT 'H', 	-- 判定登入成功的類型 
	suc_by_field 		VARCHAR(200), 						-- 當 suc_by_type 為 "R",判定登入成功的 Response JSON 欄位 
	suc_by_value 		VARCHAR(200), 						-- 當 suc_by_type 為 "R",判定登入成功的 Response JSON 值
	idt_name 			VARCHAR(200), 						-- ID token 的 name 值,來源 Response JSON 欄位 
	idt_email 			VARCHAR(200), 						-- ID token 的 email 值,來源 Response JSON 欄位 
	idt_picture 		VARCHAR(200), 						-- ID token 的 picture 值,來源 Response JSON 欄位
	icon_file 			TEXT, 						-- 登入頁圖示檔案 
	page_title 			VARCHAR(400) NOT NULL, 				-- 登入頁標題 
	create_date_time 	DATETIME DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	create_user 		VARCHAR(255) DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 	DATETIME, 							-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料 
	update_user 		VARCHAR(255), 						-- 更新人員 
	version 			INT DEFAULT 1, 						-- 版號 C/U時, 增量+1  
	PRIMARY KEY (gtw_idp_info_a_id)   
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20230912, Gateway IdP授權碼記錄檔, 增加欄位, Mini Lee
ALTER TABLE dgr_gtw_idp_auth_code ADD api_resp NVARCHAR(4000);

-- 20230912, TSMP Token 歷史紀錄, 增加欄位, Mini Lee
ALTER TABLE tsmp_token_history ADD api_resp NVARCHAR(4000);

-- 20230914, 移除DP的table, min
DROP TABLE dp_app;
DROP TABLE dp_user;
DROP TABLE dp_file;
DROP TABLE dp_api_version;

-- 20230918, Gateway IdP Auth記錄檔主檔, 增加欄位, Mini Lee
ALTER TABLE dgr_gtw_idp_auth_m ADD redirect_uri VARCHAR(400); 

-- 20230918, TSMP用戶端OAuth2驗證資料(Spring), 增加欄位, Mini Lee
ALTER TABLE oauth_client_details ADD web_server_redirect_uri1 NVARCHAR(255); 
ALTER TABLE oauth_client_details ADD web_server_redirect_uri2 NVARCHAR(255);
ALTER TABLE oauth_client_details ADD web_server_redirect_uri3 NVARCHAR(255);
ALTER TABLE oauth_client_details ADD web_server_redirect_uri4 NVARCHAR(255);
ALTER TABLE oauth_client_details ADD web_server_redirect_uri5 NVARCHAR(255);

-- 20230919, 來源IP 增加填寫Hostname, 增加欄位長度 , Zoe Lee
ALTER TABLE tsmp_client_host MODIFY COLUMN host_ip nvarchar(255)  NOT NULL;

-- 20230920, TSMP API基本資料, 增加欄位 API_RELEASE_TIME, Kevin Cheng
ALTER TABLE tsmp_api ADD api_release_time DATETIME NULL;

-- 20230926, API匯入匯出要有MOCK資料, Tom
ALTER TABLE tsmp_api_imp ADD mock_status_code CHAR(3);
ALTER TABLE tsmp_api_imp ADD mock_headers VARCHAR(2000);
ALTER TABLE tsmp_api_imp ADD mock_body NVARCHAR(2000);

-- 20231003 , zoe lee 增加 dgr_ac_idp_info_api 
CREATE TABLE dgr_ac_idp_info_api (
    ac_idp_info_api_id    BIGINT NOT NULL,    -- ID
    status    VARCHAR(1) NOT NULL DEFAULT 'Y',    -- 狀態
    approval_result_mail    TEXT NOT NULL,    -- 審核結果收件人,多組以逗號(,)隔開
    api_method    VARCHAR(10) NOT NULL,    -- 登入 API 的 HTTP method
    api_url    TEXT NOT NULL,    -- 登入 API 的 URL
    req_header    TEXT,    -- 調用 API 的 Request Header 內容
    req_body_type    VARCHAR(1) NOT NULL DEFAULT 'N',    -- 調用 API 的 Request Body 類型
    req_body    TEXT,    -- 調用 API 的 Request Body 內容
    suc_by_type    VARCHAR(1) NOT NULL DEFAULT 'H',    -- 判定登入成功的類型
    suc_by_field    VARCHAR(200),    -- 當 SUC_BY_TYPE 為 "R",判定登入成功的 Response JSON 欄位
    suc_by_value    VARCHAR(200),    -- 當 SUC_BY_TYPE 為 "R",判定登入成功的 Response JSON 值,多個以逗號分隔(不要有空格)
    idt_name    VARCHAR(200),    -- ID token 的 name 值,對應登入 API Response JSON 欄位
    idt_email    VARCHAR(200),    -- ID token 的 email 值,對應登入 API Response JSON 欄位
    idt_picture    VARCHAR(200),    -- ID token 的 picture 值,對應登入 API Response JSON 欄位
    icon_file    TEXT,    -- 登入頁圖示檔案
    page_title    VARCHAR(400) NOT NULL,    -- 登入頁標題
    create_date_time    DATETIME,    -- 建立日期
    create_user    VARCHAR(255) DEFAULT 'SYSTEM',    -- 建立人員
    update_date_time    DATETIME,    -- 更新日期
    update_user    VARCHAR(255),    -- 更新人員
    version    INT DEFAULT '1',    -- 版號
    keyword_search    NVARCHAR(200),    -- LikeSearch使用
    CONSTRAINT PK_ PRIMARY KEY (ac_idp_info_api_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20231003 , zoe lee 更改 dgr_ac_idp_user user_alias 型態
ALTER TABLE dgr_ac_idp_user MODIFY COLUMN user_alias nvarchar(200)  NULL;

-- 20231011, rdb連線資訊, tom
CREATE TABLE dgr_rdb_connection (
    connection_name    NVARCHAR(50) NOT NULL,    -- 名稱
    jdbc_url    VARCHAR(200) NOT NULL,    -- 連線URL
    user_name    VARCHAR(100) NOT NULL,    -- 帳號
    mima    VARCHAR(500) NOT NULL,    -- MIMA
    max_pool_size    INT NOT NULL DEFAULT 10,    -- 最大連線數量
    connection_timeout    INT NOT NULL DEFAULT 30000,    -- 連線取得超時設定(ms)
    idle_timeout    INT NOT NULL DEFAULT 600000,    -- 空閒連線的存活時間(ms)
    max_lifetime    INT NOT NULL DEFAULT 1800000,    -- 連線的最大存活時間(ms)
    data_source_property    VARCHAR(4000),    -- DataSourceProperty的設定
    create_date_time DATETIME DEFAULT CURRENT_TIMESTAMP,      -- 建立日期
    create_user VARCHAR(255) DEFAULT 'SYSTEM',            -- 建立人員
    update_date_time DATETIME,                           -- 更新日期
    update_user VARCHAR(255),                             -- 更新人員
    version INT DEFAULT 1,                                -- 版號
    CONSTRAINT pk_dgr_rdb_connection PRIMARY KEY(connection_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20231019, DGR_WEBSITE 網站反向代理主檔, 增加欄位, Mini Lee
ALTER TABLE dgr_website ADD auth VARCHAR(1) DEFAULT 'N' NOT NULL;
ALTER TABLE dgr_website ADD sql_injection VARCHAR(1) DEFAULT 'N' NOT NULL;
ALTER TABLE dgr_website ADD traffic VARCHAR(1) DEFAULT 'N' NOT NULL;
ALTER TABLE dgr_website ADD xss VARCHAR(1) DEFAULT 'N' NOT NULL;
ALTER TABLE dgr_website ADD xxe VARCHAR(1) DEFAULT 'N' NOT NULL;
ALTER TABLE dgr_website ADD tps INT DEFAULT 0 NOT NULL;
ALTER TABLE dgr_website ADD ignore_api NVARCHAR(4000);

-- 20231020, 增加欄位長度, Mini Lee
ALTER TABLE dgr_gtw_idp_auth_m MODIFY COLUMN state VARCHAR(1000) NOT NULL;
ALTER TABLE dgr_gtw_idp_auth_code MODIFY COLUMN auth_code VARCHAR(1000) NOT NULL;

-- 20231030, DGR_WEBSITE 網站反向代理主檔, 增加欄位, TOM
ALTER TABLE dgr_website ADD show_log VARCHAR(1) DEFAULT 'N' NOT NULL;

-- 20231103, 增加欄位 ,Zoe Lee
ALTER TABLE tsmp_api_reg ADD redirect_by_ip char(1) DEFAULT 'N' NULL;
ALTER TABLE tsmp_api_reg ADD ip_for_redirect1 TEXT NULL;
ALTER TABLE tsmp_api_reg ADD ip_src_url1 TEXT NULL;
ALTER TABLE tsmp_api_reg ADD ip_for_redirect2 TEXT NULL;
ALTER TABLE tsmp_api_reg ADD ip_src_url2 TEXT NULL;
ALTER TABLE tsmp_api_reg ADD ip_for_redirect3 TEXT NULL;
ALTER TABLE tsmp_api_reg ADD ip_src_url3 TEXT NULL;
ALTER TABLE tsmp_api_reg ADD ip_for_redirect4 TEXT NULL;
ALTER TABLE tsmp_api_reg ADD ip_src_url4 TEXT NULL;
ALTER TABLE tsmp_api_reg ADD ip_for_redirect5 TEXT NULL;
ALTER TABLE tsmp_api_reg ADD ip_src_url5 TEXT NULL;

ALTER TABLE tsmp_api_reg ADD header_mask_key TEXT NULL;
ALTER TABLE tsmp_api_reg ADD header_mask_policy char(1) DEFAULT '0'  NULL;
ALTER TABLE tsmp_api_reg ADD header_mask_policy_num int NULL;
ALTER TABLE tsmp_api_reg ADD header_mask_policy_symbol varchar(10)  NULL;

ALTER TABLE tsmp_api_reg ADD body_mask_keyword TEXT NULL;
ALTER TABLE tsmp_api_reg ADD body_mask_policy char(1) DEFAULT '0' NULL;
ALTER TABLE tsmp_api_reg ADD body_mask_policy_num int NULL;
ALTER TABLE tsmp_api_reg ADD body_mask_policy_symbol varchar(10) NULL;

-- 20231108, 增加欄位 ,Zoe Lee
ALTER TABLE tsmp_api_imp ADD redirect_by_ip char(1) DEFAULT 'N' NULL;
ALTER TABLE tsmp_api_imp ADD ip_for_redirect1 TEXT NULL;
ALTER TABLE tsmp_api_imp ADD ip_src_url1 TEXT NULL;
ALTER TABLE tsmp_api_imp ADD ip_for_redirect2 TEXT NULL;
ALTER TABLE tsmp_api_imp ADD ip_src_url2 TEXT NULL;
ALTER TABLE tsmp_api_imp ADD ip_for_redirect3 TEXT NULL;
ALTER TABLE tsmp_api_imp ADD ip_src_url3 TEXT NULL;
ALTER TABLE tsmp_api_imp ADD ip_for_redirect4 TEXT NULL;
ALTER TABLE tsmp_api_imp ADD ip_src_url4 TEXT NULL;
ALTER TABLE tsmp_api_imp ADD ip_for_redirect5 TEXT NULL;
ALTER TABLE tsmp_api_imp ADD ip_src_url5 TEXT NULL;

ALTER TABLE tsmp_api_imp ADD header_mask_key TEXT NULL;
ALTER TABLE tsmp_api_imp ADD header_mask_policy char(1) DEFAULT '0'  NULL;
ALTER TABLE tsmp_api_imp ADD header_mask_policy_num int NULL;
ALTER TABLE tsmp_api_imp ADD header_mask_policy_symbol varchar(10)  NULL;

ALTER TABLE tsmp_api_imp ADD body_mask_keyword TEXT NULL;
ALTER TABLE tsmp_api_imp ADD body_mask_policy char(1) DEFAULT '0' NULL;
ALTER TABLE tsmp_api_imp ADD body_mask_policy_num int NULL;
ALTER TABLE tsmp_api_imp ADD body_mask_policy_symbol varchar(10) NULL;

-- 20231110, Gateway IdP Auth記錄檔主檔	, 增加欄位, Mini Lee
ALTER TABLE dgr_gtw_idp_auth_m ADD code_challenge NVARCHAR(1000);
ALTER TABLE dgr_gtw_idp_auth_m ADD code_challenge_method VARCHAR(10);
 
-- 20231110, Gateway IdP授權碼記錄檔, 增加欄位, Mini Lee
ALTER TABLE dgr_gtw_idp_auth_code ADD state VARCHAR(1000);

-- 20231117 刪除 dgr_gtw_idp_info_j, Mini Lee
DROP TABLE dgr_gtw_idp_info_j;

-- 20231117, Gateway IdP資料 (JDBC), Mini Lee
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20231123, 增加欄位 ,Zoe Lee
ALTER TABLE tsmp_api ADD label1 nvarchar(20) NULL;
ALTER TABLE tsmp_api ADD label2 nvarchar(20) NULL;
ALTER TABLE tsmp_api ADD label3 nvarchar(20) NULL;
ALTER TABLE tsmp_api ADD label4 nvarchar(20) NULL;
ALTER TABLE tsmp_api ADD label5 nvarchar(20) NULL;

ALTER TABLE tsmp_api_imp ADD label1 nvarchar(20) NULL;
ALTER TABLE tsmp_api_imp ADD label2 nvarchar(20) NULL;
ALTER TABLE tsmp_api_imp ADD label3 nvarchar(20) NULL;
ALTER TABLE tsmp_api_imp ADD label4 nvarchar(20) NULL;
ALTER TABLE tsmp_api_imp ADD label5 nvarchar(20) NULL;

-- 20231130, TSMP_API_REG.SRC_URL 拿掉NOT NULL ,Zoe Lee
ALTER TABLE tsmp_api_reg MODIFY COLUMN src_url nvarchar(2000) NULL;

-- 20231201, 增加固定快取時間欄位, Tom
ALTER TABLE tsmp_api ADD fixed_cache_time INT DEFAULT 0 NOT NULL;
ALTER TABLE tsmp_api_imp ADD api_cache_flag CHAR(1) DEFAULT '1' NOT NULL;
ALTER TABLE tsmp_api_imp ADD fixed_cache_time INT DEFAULT 0 NOT NULL;

-- 20231207, X-Api-Key資料, Mini Lee
CREATE TABLE DGR_X_API_KEY (  
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;  

-- 20231207, X-Api-Key與群組關係, Mini Lee
CREATE TABLE DGR_X_API_KEY_MAP (  
	API_KEY_MAP_ID 		BIGINT NOT NULL, 		-- ID 
	REF_API_KEY_ID 		BIGINT NOT NULL, 		-- Master PK 
	GROUP_ID 			NVARCHAR(10) NOT NULL, 	-- 群組 ID 
	CREATE_DATE_TIME 	DATETIME DEFAULT CURRENT_TIMESTAMP, -- 建立日期 
	CREATE_USER 		NVARCHAR(1000) DEFAULT 'SYSTEM', -- 建立人員 
	UPDATE_DATE_TIME 	DATETIME, 				-- 更新日期 
	UPDATE_USER 		NVARCHAR(1000), 		-- 更新人員 
	VERSION 			INT DEFAULT 1, 			-- 版號
	PRIMARY KEY (API_KEY_MAP_ID)  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20231212, 增加欄位 DP_CLIENT_SECRET, Kevin Cheng
ALTER TABLE tsmp_client ADD dp_client_secret varchar(128);

-- 20231222, 調整欄位 dp_client_secret 為 dp_client_entry, 資料型態 nvarchar, Kevin Cheng
ALTER TABLE tsmp_client CHANGE dp_client_secret dp_client_entry NVARCHAR(128);

-- 20231228, 調整欄位 dp_client_secret 長度從 128 到 1000, Kevin Cheng
ALTER TABLE tsmp_client MODIFY dp_client_entry NVARCHAR(1000);

-- 20240108, TSMP外部API註冊資料, 增加欄位, Mini Lee
ALTER TABLE tsmp_api_reg ADD fail_discovery_policy VARCHAR(1) DEFAULT '0';
ALTER TABLE tsmp_api_reg ADD fail_handle_policy VARCHAR(1) DEFAULT '0';

-- 20240108, TSMP API 匯入資料, 增加欄位, Mini Lee
ALTER TABLE tsmp_api_imp ADD fail_discovery_policy VARCHAR(1) DEFAULT '0';
ALTER TABLE tsmp_api_imp ADD fail_handle_policy VARCHAR(1) DEFAULT '0';

-- 20240122,TSMP 功能維護資料, 增加欄位  ,Zoe Lee
ALTER TABLE tsmp_func ADD func_type char(1) DEFAULT '1' ;

-- 20240306, 用戶端匯出/入, Tom
CREATE TABLE DGR_IMPORT_CLIENT_RELATED_TEMP (  
	TEMP_ID 		BIGINT NOT NULL, 		-- ID 
	IMPORT_CLIENT_RELATED 	LONGBLOB NOT NULL, 	-- 匯入的資料
	ANALYZE_CLIENT_RELATED 	LONGBLOB NOT NULL, 	-- 分析的資料
	CREATE_DATE_TIME 	DATETIME DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	CREATE_USER 		NVARCHAR(1000) DEFAULT 'SYSTEM', -- 建立人員 
	UPDATE_DATE_TIME 	DATETIME, 				-- 更新日期 表示最後Update的人, 日期時間
	UPDATE_USER 		NVARCHAR(1000), 		-- 更新人員 
	VERSION 			INT DEFAULT 1, 			-- 版號 C/U時, 增量+1  
	CONSTRAINT DGR_IMPORT_CLIENT_RELATED_TEMP_PK PRIMARY KEY (TEMP_ID)  
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20240402, 增加api狀態, Tom
ALTER TABLE tsmp_api_imp ADD api_status CHAR(1) DEFAULT '2' NOT NULL;

-- 20240402,新增PUBLIC_FLAG,API_RELEASE_TIME匯入欄位, Webber
ALTER TABLE tsmp_api_imp ADD public_flag CHAR(1) NULL;
ALTER TABLE tsmp_api_imp ADD api_release_time DATETIME NULL;

-- 20240429 , dgr_web_socket_mapping 新增欄位 ,Zoe Lee
ALTER TABLE dgr_web_socket_mapping ADD auth varchar(1) DEFAULT 'N' NOT NULL;

-- 20240430, 添加兩個欄位用於預定DP上下架功能, Kevin Cheng
ALTER TABLE tsmp_api ADD scheduled_launch_date BIGINT DEFAULT 0;
ALTER TABLE tsmp_api ADD scheduled_removal_date BIGINT DEFAULT 0;

-- 20240516, 添加兩個欄位用於預定DGR API啟用停用功能, Kevin Cheng
ALTER TABLE tsmp_api ADD enable_scheduled_date BIGINT DEFAULT 0;
ALTER TABLE tsmp_api ADD disable_scheduled_date BIGINT DEFAULT 0;

-- 20240603, TSMP_API_IMP API匯入匯出添加四個欄位,兩個預定DP上下架功能, 兩個預定DGR API啟用停用功能, Webber Luo
ALTER TABLE tsmp_api_imp ADD scheduled_launch_date BIGINT DEFAULT 0;
ALTER TABLE tsmp_api_imp ADD scheduled_removal_date BIGINT DEFAULT 0;
ALTER TABLE tsmp_api_imp ADD enable_scheduled_date BIGINT DEFAULT 0;
ALTER TABLE tsmp_api_imp ADD disable_scheduled_date BIGINT DEFAULT 0;

-- 20240625, 這是DP的dp_app TABLE 因為DGR的用戶端匯出入有用到所以移動到這, Tom
-- start DP的dp_app TABLE --
-- 20230407, v4 入口網(DP)的Application	, Kevin Cheng
CREATE TABLE IF NOT EXISTS dp_app (
	dp_application_id BIGINT NOT NULL,                         -- ID
	application_name NVARCHAR(50) NOT NULL,                     -- Application名稱
	application_desc NVARCHAR(500),                             -- Application說明
	client_id VARCHAR(40) NOT NULL,                            -- CLIENT_ID
	open_apikey_id BIGINT,                                     -- 
	open_apikey_status VARCHAR(1),                             -- DGRK狀態
	user_name NVARCHAR(400) NOT NULL,                          -- 使用者名稱(視IdP類型決定)
	id_token_jwtstr TEXT NOT NULL,                    -- IdP ID Token 的 JWT
	create_date_time DATETIME DEFAULT CURRENT_TIMESTAMP,       -- 建立日期
	create_user NVARCHAR(1000) DEFAULT 'SYSTEM',                 -- 建立人員
	update_date_time DATETIME,                                 -- 更新日期
	update_user NVARCHAR(1000),                                  -- 更新人員
	version INT DEFAULT 1,                                     -- 版號
  keyword_search NVARCHAR(600) DEFAULT NULL,
	PRIMARY KEY (dp_application_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20230515, v4 入口網(DP)的Application	, 刪除欄位 , Kevin Cheng
ALTER TABLE dp_app DROP COLUMN open_apikey_status;

-- 20231105, v4 修改 dp_app 欄位型態 , min (NOT NULL 20240625補上的)
ALTER TABLE dp_app MODIFY COLUMN application_name NVARCHAR(50) NOT NULL;
ALTER TABLE dp_app MODIFY COLUMN application_desc NVARCHAR(500);

-- 20231123, v4 入口網(DP) DP APP 新增 ISS 欄位, Kevin Cheng
ALTER TABLE dp_app ADD iss VARCHAR(4000) NOT NULL DEFAULT 'NULL';

-- 20231128, v4 入口網(DP) 改欄位名, Kevin Cheng
ALTER TABLE dp_app CHANGE user_name dp_user_name NVARCHAR(400);

-- end DP的dp_app TABLE --
-- 20231228, 移除 open_apikey_id 欄位, jhmin
ALTER TABLE dp_app DROP COLUMN open_apikey_id;

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
    CONSTRAINT DGR_AC_IDP_INFO_CUS_PK PRIMARY KEY (ac_idp_info_cus_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20240911 , DGR_GTW_IDP_INFO_A  ADD COLUMN , Zoe Lee
ALTER TABLE dgr_gtw_idp_info_a ADD  idt_light_id VARCHAR(200);
ALTER TABLE dgr_gtw_idp_info_a ADD  idt_role_name NVARCHAR(200);
-- 20240911 , DGR_GTW_IDP_AUTH_CODE  ADD COLUMN , Zoe Lee
ALTER TABLE dgr_gtw_idp_auth_code ADD  user_light_id VARCHAR(200);
ALTER TABLE dgr_gtw_idp_auth_code ADD  user_role_name NVARCHAR(200);

-- 20240902 , CUS GATE IDP INFO , Kevin Cheng
CREATE TABLE IF NOT EXISTS dgr_gtw_idp_info_cus
(
    gtw_idp_info_cus_id BIGINT        NOT NULL,                           -- ID
    client_id           VARCHAR(40)   NOT NULL,                           -- digiRunner 的 client_id
    status              VARCHAR(1)    NOT NULL DEFAULT 'Y',               -- 狀態
    cus_login_url       VARCHAR(4000) NOT NULL,                           -- CUS 登入 URL
    cus_user_data_url   VARCHAR(4000) NOT NULL,                           -- CUS 使用者資料 URL
    icon_file           VARCHAR(4000),                                    -- 登入頁圖示檔案
    page_title          NVARCHAR(400),                                    -- 登入頁標題
    create_date_time    DATETIME               DEFAULT CURRENT_TIMESTAMP, -- 建立日期
    create_user         NVARCHAR(1000)         DEFAULT 'SYSTEM',          -- 建立人員
    update_date_time    DATETIME,                                         -- 更新日期
    update_user         NVARCHAR(1000),                                   -- 更新人員
    version             INT                    DEFAULT 1,                 -- 版號
    CONSTRAINT GTW_IDP_INFO_CUS_PK PRIMARY KEY (gtw_idp_info_cus_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20241007, AC IdP授權碼記錄檔, 增加欄位, Mini Lee
Alter TABLE dgr_ac_idp_auth_code ADD api_resp NVARCHAR(4000);

-- 20241022 , DGR_BOT_DETECTION , Kevin Cheng
CREATE TABLE IF NOT EXISTS dgr_bot_detection
(
    bot_detection_id   BIGINT        NOT NULL,                           -- ID
    bot_detection_rule VARCHAR(4000) NOT NULL,                           -- 規則
    type               VARCHAR(1)    NOT NULL DEFAULT 'W',               -- 名單種類
    create_date_time   DATETIME               DEFAULT CURRENT_TIMESTAMP, -- 建立日期
    create_user        NVARCHAR(1000)         DEFAULT 'SYSTEM',          -- 建立人員
    update_date_time   DATETIME,                                         -- 更新日期
    update_user        NVARCHAR(1000),                                   -- 更新人員
    version            INT                    DEFAULT 1,                 -- 版號
    CONSTRAINT BOT_DETECTION_PK PRIMARY KEY (bot_detection_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20250120 , TSMP Token 歷史紀錄, Mini Lee
ALTER TABLE tsmp_token_history MODIFY COLUMN api_resp TEXT;
-- 20250120 , SSO AC IdP授權碼記錄檔, Mini Lee
ALTER TABLE dgr_ac_idp_auth_code MODIFY COLUMN api_resp TEXT;
-- 20250120 , Gateway IdP授權碼記錄檔, Mini Lee
ALTER TABLE dgr_gtw_idp_auth_code MODIFY COLUMN api_resp TEXT;

-- 20250203, dashboard相關table建立index(若有資料存在可能要執行一段時間), Tom
CREATE INDEX idx_tsmp_req_log ON tsmp_req_log(rtime);
CREATE INDEX idx_dgr_dashboard_es_log ON dgr_dashboard_es_log(rtime);
CREATE INDEX idx_tsmp_req_res_log_history ON tsmp_req_res_log_history(rtime);

-- 20250213, 增加欄位長度, Zoe Lee
ALTER TABLE dgr_rdb_connection MODIFY COLUMN mima VARCHAR(2000) NOT NULL;

-- 20250227, DGR_WEBHOOK_NOTIFY , Webber Luo
CREATE TABLE IF NOT EXISTS dgr_webhook_notify (
    webhook_notify_id    BIGINT              NOT NULL,    -- ID
    notify_name          VARCHAR(100)        NOT NULL,    -- 通知名稱
    notify_type          VARCHAR(100)        NOT NULL,    -- 通知種類
    enable               VARCHAR(1)          NOT NULL,    -- 啟用=Y / 停用=N
    message              VARCHAR(2000),                   -- 發送內容
    payload_flag         VARCHAR(1)          DEFAULT '0', --
    create_date_time     DATETIME            DEFAULT CURRENT_TIMESTAMP,  -- 建立日期
    create_user          VARCHAR(1000)       DEFAULT 'SYSTEM',           -- 建立人員
    update_date_time     DATETIME            ON UPDATE CURRENT_TIMESTAMP, -- 更新日期
    update_user          VARCHAR(1000),                                  -- 更新人員
    version              INT                 DEFAULT 1,                  -- 版號
    PRIMARY KEY(webhook_notify_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 20250227, DGR_WEBHOOK_NOTIFY_FIELD , Webber Luo
CREATE TABLE IF NOT EXISTS dgr_webhook_notify_field (
    webhook_notify_field_id  BIGINT           NOT NULL,    -- ID
    webhook_notify_id        BIGINT           NOT NULL,    -- 通知ID
    field_key                VARCHAR(100)     NOT NULL,    -- KEY
    field_value              VARCHAR(2000)    NOT NULL,    -- VALUE
    field_type               VARCHAR(1)       NOT NULL DEFAULT '0',    -- TYPE
    mapping_url              VARCHAR(2000),                            -- URL
    create_date_time         DATETIME         DEFAULT CURRENT_TIMESTAMP,  -- 建立日期
    create_user              VARCHAR(1000)    DEFAULT 'SYSTEM',           -- 建立人員
    update_date_time         DATETIME         ON UPDATE CURRENT_TIMESTAMP, -- 更新日期
    update_user              VARCHAR(1000),                              -- 更新人員
    version                  INT              DEFAULT 1,                 -- 版號
    PRIMARY KEY(webhook_notify_field_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 20250227, DGR_WEBHOOK_NOTIFY_LOG , Webber Luo
CREATE TABLE IF NOT EXISTS DGR_WEBHOOK_NOTIFY_LOG (
    webhook_notify_log_id    BIGINT           NOT NULL,    -- ID
    webhook_notify_id        BIGINT           NOT NULL,    -- WEBHOOK_NOTIFY_ID
    client_id                VARCHAR(40)      NOT NULL,    -- 用戶端
    content                  VARCHAR(2000),                -- 發送內容
    remark                   VARCHAR(2000),                -- 備註
    create_date_time         DATETIME         DEFAULT CURRENT_TIMESTAMP,  -- 建立日期
    create_user              VARCHAR(1000)    DEFAULT 'SYSTEM',           -- 建立人員
    update_date_time         DATETIME         ON UPDATE CURRENT_TIMESTAMP, -- 更新日期
    update_user              VARCHAR(1000),                              -- 更新人員
    version                  INT              DEFAULT 1,                 -- 版號
    PRIMARY KEY(webhook_notify_log_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 20250227, DGR_WEBHOOK_API_MAP , Webber Luo
CREATE TABLE IF NOT EXISTS dgr_webhook_api_map (
    webhook_api_map_id       BIGINT           NOT NULL,    -- ID
    api_key                  VARCHAR(255)     NOT NULL,    -- API代碼
    module_name              VARCHAR(150)     NOT NULL,    -- API_MODULE
    webhook_notify_id        BIGINT           NOT NULL,    -- WEBHOOK_NOTIFY_I
    create_date_time         DATETIME         DEFAULT CURRENT_TIMESTAMP,  -- 建立日期
    create_user              VARCHAR(1000)    DEFAULT 'SYSTEM',           -- 建立人員
    update_date_time         DATETIME         ON UPDATE CURRENT_TIMESTAMP, -- 更新日期
    update_user              VARCHAR(1000),                              -- 更新人員
    version                  INT              DEFAULT 1,                 -- 版號
    PRIMARY KEY(webhook_api_map_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 20250331, DGR_GRPCPROXY_MAP, Alvin Chiu
CREATE TABLE IF NOT EXISTS dgr_grpcproxy_map
(
    grpcproxy_map_id          BIGINT       NOT NULL,                           -- ID
    service_name              VARCHAR(255) NOT NULL,                           -- 目標服務名稱
    proxy_hostname            VARCHAR(255) NOT NULL,                           -- 轉發位置
    target_hostname           VARCHAR(255) NOT NULL,                           -- 目標服務位置
    target_port               INT          NOT NULL,                           -- 目標服務PORT
    connect_timeout_ms        INT          NOT NULL DEFAULT 5000,              -- 連線超時毫秒
    send_timeout_ms           INT          NOT NULL DEFAULT 10000,             -- 發送超時毫秒
    read_timeout_ms           INT          NOT NULL DEFAULT 30000,             -- 讀取超時毫秒
    secure_mode               VARCHAR(10)           DEFAULT 'AUTO',            -- TLS安全模式 (AUTO, SECURE, PLAINTEXT)
    server_cert_content       TEXT,                                            -- X509憑證內容 (PEM格式)
    server_key_content        TEXT,                                            -- 私鑰內容 (PEM格式)
    auto_trust_upstream_certs VARCHAR(1)            DEFAULT 'N',               -- 是否自動信任上游憑證
    trusted_certs_content     TEXT,                                            -- 受信任的CA憑證內容 (PEM格式)
    enable                    VARCHAR(1)            DEFAULT 'N',               -- 啟用/停用
    create_date_time          DATETIME              DEFAULT CURRENT_TIMESTAMP, -- 建立日期
    create_user               VARCHAR(1000)         DEFAULT 'SYSTEM',          -- 建立人員
    update_date_time          DATETIME ON UPDATE CURRENT_TIMESTAMP,            -- 更新日期
    update_user               VARCHAR(1000),                                   -- 更新人員
    version                   INT                   DEFAULT 1,                 -- 版號
    PRIMARY KEY (grpcproxy_map_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 20250812 , DGR_MTLS_CLIENT_CERT , Zoe Lee
CREATE TABLE IF NOT EXISTS dgr_mtls_client_cert (
    dgr_mtls_client_cert_id    bigint NOT NULL,    -- ID
    host    varchar(255) NOT NULL,    -- 主機
    port    int NOT NULL,    -- 通訊埠
    root_ca    varchar(4000) NOT NULL,    -- 根憑證
    client_cert    varchar(4000) NOT NULL,    -- 客戶端憑證
    client_key    varchar(4000) NOT NULL,    -- 客戶端金鑰
    key_mima    nvarchar(2000),    -- 客戶端憑證密碼
    remark    nvarchar(200),    -- 備註
    enable    varchar(1) NOT NULL,    -- 啟用=Y / 停用=N
    root_ca_expire_date    datetime NOT NULL,    -- ROOT_CA 的到期時間
    crt_expire_date    datetime NOT NULL,    -- CLIENT_CERT 的到期時間
    create_date_time    datetime DEFAULT CURRENT_TIMESTAMP,    -- 建立日期
    create_user    nvarchar(1000) DEFAULT 'SYSTEM',    -- 建立人員
    update_date_time    datetime,    -- 更新日期
    update_user    nvarchar(1000),    -- 更新人員
    version    int DEFAULT 1,    -- 版號
    keyword_search    nvarchar(250),    -- LikeSearch使用
    CONSTRAINT pk_dgr_mtls_client_cert PRIMARY KEY (dgr_mtls_client_cert_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20250801 , add column , Zoe Lee
ALTER TABLE tsmp_api_imp ADD COLUMN notify_name_list VARCHAR(2000);

-- 20250812 , 檢查並建立 dgr_ai_provider 表 , Vulcan
CREATE TABLE IF NOT EXISTS dgr_ai_provider (
    ai_provider_id              BIGINT          NOT NULL,                       -- ID
    ai_provider_name            VARCHAR(1700)   NOT NULL,                       -- 供應商名稱
    ai_provider_alias           TEXT            NOT NULL,                       -- 任意名稱
    ai_model                    VARCHAR(1700)   NOT NULL,                       -- LLM 模型名稱
    generate_api                TEXT            NOT NULL,                       -- 此模型生成內容 API URL
    count_token_api             TEXT            NOT NULL,                       -- 此模型計算 TOKEN API URL
    create_date_time            DATETIME        DEFAULT CURRENT_TIMESTAMP,      -- 建立日期
    create_user                 VARCHAR(1000)   DEFAULT 'SYSTEM',               -- 建立人員
    update_date_time            DATETIME,                                       -- 更新日期
    update_user                 VARCHAR(1000),                                  -- 更新人員
    version                     INT             DEFAULT 1,                      -- 版號
    ai_provider_enable          VARCHAR(1)      NOT NULL DEFAULT 'Y',           -- Y = 啟用, N = 停用
    PRIMARY KEY(ai_provider_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20250812 , 檢查並建立索引 , Vulcan
CREATE INDEX index_dgr_ai_provider_ai_provider_name_ai_provider ON dgr_ai_provider (ai_provider_name);
CREATE INDEX index_dgr_ai_provider_ai_provider_name_ai_model ON dgr_ai_provider (ai_model);

-- 20250812 , 檢查並建立 dgr_ai_apikey 表 , Vulcan
CREATE TABLE IF NOT EXISTS dgr_ai_apikey (
    ai_apikey_id                BIGINT          NOT NULL,                   -- ID
    ai_apikey_name              VARCHAR(1000)   NOT NULL,                   -- 此 APIKEY 名稱 (任意命名)
    ai_provider_id              BIGINT,                                     -- AI 供應商 ID
    ai_apikey_code              VARCHAR(500)    NOT NULL,                   -- API KEY CODE 內容
    usage_limit_input_token     BIGINT          NOT NULL DEFAULT 0,         -- 此 APIKEY 使用 input token 上限 0 = 不設限
    usage_limit_output_token    BIGINT          NOT NULL DEFAULT 0,         -- 此 APIKEY 使用 output token 上限 0 = 不設限
    usage_input_token_count     BIGINT          NOT NULL DEFAULT 0,         -- 此 APIKEY 使用 input token 總數
    usage_output_token_count    BIGINT          NOT NULL DEFAULT 0,         -- 此 APIKEY 使用 output token 總數
    usage_limit_policy          VARCHAR(500)    NOT NULL DEFAULT 'REJECT',  -- 當 input / output 達到上限時的執行策略
    ai_apikey_enable            VARCHAR(1)      NOT NULL DEFAULT 'Y',       -- Y = 啟用, N = 停用
    create_date_time            DATETIME        DEFAULT CURRENT_TIMESTAMP,  -- 建立日期
    create_user                 VARCHAR(1000)   DEFAULT 'SYSTEM',           -- 建立人員
    update_date_time            DATETIME,                                   -- 更新日期
    update_user                 VARCHAR(1000),                              -- 更新人員
    version                     INT             DEFAULT 1,                  -- 版號
    PRIMARY KEY(ai_apikey_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 檢查並建立索引
CREATE INDEX index_dgr_ai_apikey_ai_provider_id ON dgr_ai_apikey (ai_provider_id);

-- 20250812 , 檢查並建立 dgr_ai_apikey_usage 表 , Vulcan
CREATE TABLE IF NOT EXISTS dgr_ai_apikey_usage (
    ai_apikey_usage_id          BIGINT          NOT NULL,                   -- ID
    ai_apikey_consumer_type     VARCHAR(1000),                              -- APIKEY 使用者類型 user = 個人使用者, client = client
    ai_apikey_consumer_id       VARCHAR(1000),                              -- APIKEY 使用者 ID
    ai_apikey_id                BIGINT          NOT NULL,                   -- APIKEY ID
    requst_ts                   BIGINT          NOT NULL,                   -- 使用者請求時間，以毫秒數記錄
    input_token_count           BIGINT          NOT NULL DEFAULT 0,         -- 請求內容 input token 數
    output_token_count          BIGINT          NOT NULL DEFAULT 0,         -- 請求回傳 output token 數
    ai_prompt_template_id       BIGINT,                                     -- AI 提示模板 ID
    http_transaction_status     TEXT,                                       -- HTTP 交易狀態
    ai_usage_prompt_input       TEXT,                                       -- AI 使用者請求內容
    ai_usage_prompt_output      TEXT,                                       -- AI 使用者請求回傳內容
    create_date_time            DATETIME        DEFAULT CURRENT_TIMESTAMP,  -- 建立日期
    PRIMARY KEY(ai_apikey_usage_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20250812 , 檢查並建立索引 , Vulcan
CREATE INDEX index_dgr_ai_apikey_usage_ai_apikey_id ON dgr_ai_apikey_usage (ai_apikey_id);

-- 20250812 , 檢查並建立 dgr_ai_prompt_template 表 , Vulcan
CREATE TABLE IF NOT EXISTS dgr_ai_prompt_template (
    ai_prompt_template_id       BIGINT          NOT NULL,               -- ID
    ai_prompt_template_name     VARCHAR(1700)   NOT NULL,               -- AI 提示模板名稱
    ai_prompt_template_content  TEXT            NOT NULL,               -- AI 提示模板內容
    ai_prompt_template_enable   VARCHAR(1)      NOT NULL DEFAULT 'Y',   -- Y = 啟用, N = 停用
    ai_prompt_template_remark   VARCHAR(400),                           -- AI 提示模板備註
    PRIMARY KEY(ai_prompt_template_id),
    UNIQUE KEY UQ_dgr_ai_prompt_template_name (ai_prompt_template_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20250812 , 檢查並建立 dgr_ai_prompt_template_binding 表 , Vulcan
CREATE TABLE IF NOT EXISTS dgr_ai_prompt_template_binding (
    ai_consumer_prompt_template_binding_id BIGINT NOT NULL,  -- ID
    ai_prompt_template_id                  BIGINT NOT NULL,  -- AI 提示模板 ID
    ai_apikey_consumer_type                VARCHAR(1000),    -- APIKEY 使用者類型 user / client
    ai_apikey_consumer_id                  VARCHAR(1000),    -- APIKEY 使用者 ID
    PRIMARY KEY(ai_consumer_prompt_template_binding_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20250812 , 檢查並建立索引 , Vulcan
CREATE INDEX index_dgr_ai_prompt_template_binding_ai_prompt_template_id ON dgr_ai_prompt_template_binding (ai_prompt_template_id);

-- 20250819, TSMP外部API註冊資料, 增加欄位 ,Mini Lee
ALTER TABLE tsmp_api_reg ADD is_cors_allow_origin VARCHAR(1) DEFAULT 'N' NULL;
ALTER TABLE tsmp_api_reg ADD is_cors_allow_methods VARCHAR(1) DEFAULT 'N' NULL;
ALTER TABLE tsmp_api_reg ADD is_cors_allow_headers VARCHAR(1) DEFAULT 'N' NULL;
ALTER TABLE tsmp_api_reg ADD cors_allow_origin VARCHAR(1000) NULL;
ALTER TABLE tsmp_api_reg ADD cors_allow_methods VARCHAR(200) NULL;
ALTER TABLE tsmp_api_reg ADD cors_allow_headers VARCHAR(1000) NULL;

-- 20250819, TSMP API 匯入資料, 增加欄位 ,Mini Lee
ALTER TABLE tsmp_api_imp ADD is_cors_allow_origin VARCHAR(1) DEFAULT 'N' NULL;
ALTER TABLE tsmp_api_imp ADD is_cors_allow_methods VARCHAR(1) DEFAULT 'N' NULL;
ALTER TABLE tsmp_api_imp ADD is_cors_allow_headers VARCHAR(1) DEFAULT 'N' NULL;
ALTER TABLE tsmp_api_imp ADD cors_allow_origin VARCHAR(1000) NULL;
ALTER TABLE tsmp_api_imp ADD cors_allow_methods VARCHAR(200) NULL;
ALTER TABLE tsmp_api_imp ADD cors_allow_headers VARCHAR(1000) NULL;

-- 20250825, dgr_otp , Tom
CREATE TABLE IF NOT EXISTS dgr_otp (
    otp_id                 BIGINT          NOT NULL,                    -- ID
    email                  VARCHAR(100)    NOT NULL,                    -- email
    otp_code               VARCHAR(10)     NOT NULL,                    -- 驗證碼
    expire_key             VARCHAR(400)    NOT NULL,                    -- 到期key
    error_limit            INT             NOT NULL DEFAULT 0,          -- 錯誤上限
    PRIMARY KEY (otp_id),
    UNIQUE KEY (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 20251003, v4, 加大欄位長度, Mini Lee
ALTER TABLE tsmp_token_history MODIFY COLUMN user_name NVARCHAR(400);

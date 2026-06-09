-- dgr_dev.cus_setting definition

-- Drop table

-- DROP TABLE cus_setting;

CREATE TABLE cus_setting (
	cus_setting_id int8 NOT NULL,
	setting_no varchar(20) NOT NULL,
	setting_name varchar(100) NOT NULL,
	subsetting_no varchar(20) NOT NULL,
	subsetting_name varchar(100) NOT NULL,
	sort_by int4 NOT NULL DEFAULT 0,
	is_default varchar(1) NULL,
	param1 varchar(255) NULL,
	param2 varchar(255) NULL,
	param3 varchar(255) NULL,
	param4 varchar(255) NULL,
	param5 varchar(255) NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	keyword_search varchar(200) NULL,
	CONSTRAINT cus_setting_pkey PRIMARY KEY (setting_no, subsetting_no)
);


-- dgr_dev.dgr_audit_logd definition

-- Drop table

-- DROP TABLE dgr_audit_logd;

CREATE TABLE dgr_audit_logd (
	audit_long_id int8 NOT NULL,
	txn_uid varchar(50) NOT NULL,
	entity_name varchar(50) NOT NULL,
	cud varchar(50) NOT NULL,
	old_row bytea NULL,
	new_row bytea NULL,
	param1 varchar(4000) NULL,
	param2 varchar(4000) NULL,
	param3 varchar(4000) NULL,
	param4 varchar(4000) NULL,
	param5 varchar(4000) NULL,
	stack_trace varchar(4000) NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	CONSTRAINT dgr_audit_logd_pkey PRIMARY KEY (audit_long_id)
);


-- dgr_dev.dgr_audit_logm definition

-- Drop table

-- DROP TABLE dgr_audit_logm;

CREATE TABLE dgr_audit_logm (
	audit_long_id int8 NOT NULL,
	audit_ext_id int8 NOT NULL DEFAULT 0,
	txn_uid varchar(50) NOT NULL,
	user_name varchar(400) NOT NULL,
	client_id varchar(50) NOT NULL,
	api_url varchar(500) NOT NULL,
	orig_api_url varchar(500) NULL,
	event_no varchar(50) NOT NULL,
	user_ip varchar(200) NULL,
	user_hostname varchar(200) NULL,
	user_role varchar(4000) NULL,
	param1 varchar(4000) NULL,
	param2 varchar(4000) NULL,
	param3 varchar(4000) NULL,
	param4 varchar(4000) NULL,
	param5 varchar(4000) NULL,
	stack_trace varchar(4000) NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	CONSTRAINT pk_dgr_audit_logm_1 PRIMARY KEY (audit_long_id, audit_ext_id),
	CONSTRAINT uk_dgr_audit_logm_1 UNIQUE (txn_uid)
);


-- dgr_dev.dgr_composer_flow definition

-- Drop table

-- DROP TABLE dgr_composer_flow;

CREATE TABLE dgr_composer_flow (
	flow_id int8 NOT NULL,
	module_name varchar(150) NOT NULL,
	api_id varchar(255) NOT NULL,
	flow_data bytea NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	update_date_time timestamp NULL,
	"version" int4 NULL DEFAULT 1,
	CONSTRAINT pk_dgr_composer_flow PRIMARY KEY (flow_id),
	CONSTRAINT u_dgr_composer_flow UNIQUE (module_name, api_id)
);


-- dgr_dev.dgr_node_lost_contact definition

-- Drop table

-- DROP TABLE dgr_node_lost_contact;

CREATE TABLE dgr_node_lost_contact (
	lost_contact_id int8 NOT NULL,
	node_name varchar(100) NOT NULL,
	ip varchar(100) NOT NULL,
	port int4 NOT NULL,
	lost_time varchar(100) NOT NULL,
	create_timestamp int8 NOT NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	CONSTRAINT pk_dgr_node_lost_contact PRIMARY KEY (lost_contact_id)
);


-- dgr_dev."groups" definition

-- Drop table

-- DROP TABLE "groups";

CREATE TABLE "groups" (
	id bigserial NOT NULL,
	group_name varchar(50) NOT NULL,
	CONSTRAINT groups_pkey PRIMARY KEY (id)
);


-- dgr_dev.ldap_auth_result definition

-- Drop table

-- DROP TABLE ldap_auth_result;

CREATE TABLE ldap_auth_result (
	ldap_id int8 NOT NULL,
	user_name varchar(400) NOT NULL,
	code_challenge varchar(50) NOT NULL,
	user_ip varchar(50) NULL,
	use_date_time timestamp NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'LDAP_SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	CONSTRAINT ldap_auth_result_pkey PRIMARY KEY (ldap_id)
);


-- dgr_dev.oauth_approvals definition

-- Drop table

-- DROP TABLE oauth_approvals;

CREATE TABLE oauth_approvals (
	userid varchar(256) NULL,
	clientid varchar(256) NULL,
	"scope" varchar(256) NULL,
	status varchar(10) NULL,
	expiresat timestamp NULL,
	lastmodifiedat timestamp NULL
);


-- dgr_dev.oauth_client_details definition

-- Drop table

-- DROP TABLE oauth_client_details;

CREATE TABLE oauth_client_details (
	client_id varchar(255) NOT NULL,
	resource_ids varchar(255) NULL DEFAULT NULL::character varying,
	client_secret varchar(255) NULL DEFAULT NULL::character varying,
	"scope" varchar(2048) NULL DEFAULT NULL::character varying,
	authorized_grant_types varchar(255) NULL DEFAULT NULL::character varying,
	web_server_redirect_uri varchar(255) NULL DEFAULT NULL::character varying,
	authorities varchar(255) NULL DEFAULT NULL::character varying,
	access_token_validity int4 NULL,
	refresh_token_validity int4 NULL,
	additional_information varchar(4096) NULL DEFAULT NULL::character varying,
	autoapprove varchar(255) NULL DEFAULT NULL::character varying,
	CONSTRAINT oauth_client_details_pkey PRIMARY KEY (client_id)
);


-- dgr_dev.oauth_code definition

-- Drop table

-- DROP TABLE oauth_code;

CREATE TABLE oauth_code (
	code varchar(256) NULL,
	authentication bytea NULL,
	created timestamp NULL DEFAULT CURRENT_TIMESTAMP
);


-- dgr_dev.seq_store definition

-- Drop table

-- DROP TABLE seq_store;

CREATE TABLE seq_store (
	sequence_name varchar(255) NOT NULL,
	next_val int8 NULL,
	CONSTRAINT seq_store_pkey PRIMARY KEY (sequence_name)
);


-- dgr_dev.sso_auth_result definition

-- Drop table

-- DROP TABLE sso_auth_result;

CREATE TABLE sso_auth_result (
	sso_id int8 NOT NULL,
	user_name varchar(400) NOT NULL,
	code_challenge varchar(50) NOT NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SSO SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	use_date_time timestamp NULL,
	CONSTRAINT sso_auth_result_pkey PRIMARY KEY (sso_id)
);


-- dgr_dev.tsmp_alert definition

-- Drop table

-- DROP TABLE tsmp_alert;

CREATE TABLE tsmp_alert (
	alert_id int4 NOT NULL,
	alert_name varchar(30) NOT NULL,
	alert_type varchar(20) NOT NULL,
	alert_enabled bool NOT NULL,
	threshold int4 NULL,
	duration int4 NULL,
	alert_interval int4 NULL,
	c_flag bool NOT NULL,
	im_flag bool NOT NULL,
	im_type varchar(20) NULL,
	im_id varchar(100) NULL,
	ex_type bpchar(1) NOT NULL,
	ex_days varchar(100) NULL,
	ex_time varchar(100) NULL,
	alert_desc varchar(200) NULL,
	alert_sys varchar(20) NULL DEFAULT NULL::character varying,
	alert_msg varchar(300) NULL DEFAULT NULL::character varying,
	create_time timestamp NULL,
	update_time timestamp NULL,
	create_user VARCHAR(1000) NULL DEFAULT NULL::character varying,
	update_user VARCHAR(1000) NULL DEFAULT NULL::character varying,
	es_search_payload varchar(1024) NULL DEFAULT NULL::character varying,
	modulename varchar(255) NULL,
	responsetime varchar(255) NULL,
	CONSTRAINT tsmp_alert_pkey PRIMARY KEY (alert_id)
);


-- dgr_dev.tsmp_alert_log definition

-- Drop table

-- DROP TABLE tsmp_alert_log;

CREATE TABLE tsmp_alert_log (
	alert_log_id int8 NOT NULL,
	alert_id int4 NOT NULL DEFAULT '-1'::integer,
	role_id varchar(500) NULL,
	alert_msg varchar(300) NOT NULL DEFAULT ''::character varying,
	sender_type varchar(20) NOT NULL,
	"result" varchar(1) NOT NULL DEFAULT '0'::character varying,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	CONSTRAINT tsmp_alert_log_pkey PRIMARY KEY (alert_log_id)
);


-- dgr_dev.tsmp_api definition

-- Drop table

-- DROP TABLE tsmp_api;

CREATE TABLE tsmp_api (
	api_key varchar(255) NOT NULL,
	module_name varchar(150) NOT NULL,
	api_name varchar(255) NOT NULL,
	api_status bpchar(1) NOT NULL,
	api_src bpchar(1) NOT NULL,
	api_desc varchar(1500) NULL,
	create_time timestamp NOT NULL,
	update_time timestamp NULL,
	create_user VARCHAR(1000) NOT NULL,
	update_user VARCHAR(1000) NULL,
	api_owner varchar(100) NULL DEFAULT NULL::character varying,
	org_id varchar(255) NULL DEFAULT NULL::character varying,
	public_flag bpchar(1) NULL DEFAULT NULL::bpchar,
	src_url varchar(2000) NULL DEFAULT NULL::character varying,
	api_uid varchar(36) NULL,
	data_format bpchar(1) NULL DEFAULT NULL::bpchar,
	jwe_flag varchar(1) NULL DEFAULT NULL::character varying,
	jwe_flag_resp varchar(1) NULL,
	api_cache_flag varchar(1) NOT NULL DEFAULT '1'::character varying,
	mock_status_code char(3) Null,
	mock_headers varchar(2000) Null,
	mock_body varchar(2000) Null,
	CONSTRAINT uk_api_1 PRIMARY KEY (api_key, module_name)
);
CREATE INDEX tsmp_api_api_uid_idx ON tsmp_api USING btree (api_uid);


-- dgr_dev.tsmp_api_ext definition

-- Drop table

-- DROP TABLE tsmp_api_ext;

CREATE TABLE tsmp_api_ext (
	api_key varchar(30) NOT NULL,
	module_name varchar(100) NOT NULL,
	dp_status varchar(1) NOT NULL,
	dp_stu_date_time timestamp NULL,
	ref_orderm_id int8 NOT NULL,
	api_ext_id int8 NOT NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	CONSTRAINT tsmp_api_ext_api_ext_id_key UNIQUE (api_ext_id),
	CONSTRAINT tsmp_api_ext_pkey PRIMARY KEY (api_key, module_name)
);


-- dgr_dev.tsmp_api_imp definition

-- Drop table

-- DROP TABLE tsmp_api_imp;

CREATE TABLE tsmp_api_imp (
	api_key varchar(255) NOT NULL,
	module_name varchar(50) NOT NULL,
	record_type bpchar(1) NOT NULL,
	batch_no int4 NOT NULL,
	filename varchar(100) NOT NULL,
	api_name varchar(255) NULL,
	api_desc varchar(300) NULL,
	api_owner varchar(100) NULL,
	url_rid bpchar(1) NULL DEFAULT '0'::bpchar,
	api_src bpchar(1) NULL DEFAULT 'M'::bpchar,
	src_url varchar(2000) NULL,
	api_uuid varchar(64) NULL,
	path_of_json varchar(255) NOT NULL,
	method_of_json varchar(50) NOT NULL,
	params_of_json varchar(255) NULL,
	headers_of_json varchar(255) NULL,
	consumes_of_json varchar(100) NULL,
	produces_of_json varchar(255) NULL,
	flow text NULL,
	create_time timestamp NOT NULL,
	create_user VARCHAR(1000) NOT NULL,
	check_act bpchar(1) NOT NULL,
	"result" bpchar(1) NOT NULL,
	memo varchar(255) NULL,
	no_oauth bpchar(1) NULL,
	jwe_flag varchar(1) NULL,
	jwe_flag_resp varchar(1) NULL,
	fun_flag int4 NULL,
	CONSTRAINT tsmp_api_imp_pkey PRIMARY KEY (api_key, module_name, record_type, batch_no)
);


-- dgr_dev.tsmp_api_module definition

-- Drop table

-- DROP TABLE tsmp_api_module;

CREATE TABLE tsmp_api_module (
	id int8 NOT NULL,
	module_name varchar(255) NOT NULL,
	module_version varchar(255) NOT NULL,
	module_app_class varchar(255) NOT NULL,
	module_bytes bytea NOT NULL,
	module_md5 varchar(255) NOT NULL,
	module_type varchar(255) NOT NULL,
	upload_time timestamp NOT NULL,
	uploader_name varchar(255) NOT NULL,
	status_time timestamp NULL,
	status_user varchar(255) NULL,
	active bool NOT NULL,
	node_task_id int8 NULL,
	v2_flag int4 NULL,
	org_id varchar(255) NULL DEFAULT NULL::character varying,
	CONSTRAINT tsmp_api_module_pkey PRIMARY KEY (id),
	CONSTRAINT uk_api_module_1 UNIQUE (module_name, module_version)
);


-- dgr_dev.tsmp_api_reg definition

-- Drop table

-- DROP TABLE tsmp_api_reg;

CREATE TABLE tsmp_api_reg (
	api_key varchar(255) NOT NULL,
	module_name varchar(50) NOT NULL,
	src_url varchar(2000) NOT NULL,
	reg_status bpchar(1) NOT NULL,
	api_uuid varchar(64) NULL,
	path_of_json varchar(255) NULL,
	method_of_json varchar(50) NOT NULL,
	params_of_json varchar(255) NULL,
	headers_of_json varchar(255) NULL,
	consumes_of_json varchar(100) NULL,
	produces_of_json varchar(255) NULL,
	create_time timestamp NOT NULL,
	create_user VARCHAR(1000) NOT NULL,
	update_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	url_rid bpchar(1) NOT NULL DEFAULT 0,
	reghost_id varchar(10) NULL DEFAULT NULL::character varying,
	no_oauth bpchar(1) NULL,
	fun_flag int4 NULL DEFAULT 0,
	CONSTRAINT uk_api_reg_1 PRIMARY KEY (api_key, module_name)
);


-- dgr_dev.tsmp_auth_code definition

-- Drop table

-- DROP TABLE tsmp_auth_code;

CREATE TABLE tsmp_auth_code (
	auth_code_id int8 NOT NULL,
	auth_code varchar(1000) NOT NULL,
	expire_date_time int8 NOT NULL,
	status varchar(1) NOT NULL DEFAULT '0'::character varying,
	auth_type varchar(20) NULL,
	client_name varchar(150) NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	CONSTRAINT tsmp_auth_code_pk PRIMARY KEY (auth_code_id),
	CONSTRAINT tsmp_auth_code_uk UNIQUE (auth_code)
);


-- dgr_dev.tsmp_client definition

-- Drop table

-- DROP TABLE tsmp_client;

CREATE TABLE tsmp_client (
	client_id varchar(40) NOT NULL,
	client_name varchar(150) NOT NULL,
	client_status bpchar(1) NOT NULL,
	tps int4 NOT NULL,
	emails varchar(500) NULL,
	create_time timestamp NOT NULL,
	update_time timestamp NULL,
	"owner" varchar(100) NOT NULL,
	remark varchar(300) NULL,
	create_user VARCHAR(1000) NOT NULL,
	update_user VARCHAR(1000) NULL,
	client_sd timestamp NULL,
	client_ed timestamp NULL,
	svc_st varchar(4) NULL,
	svc_et varchar(4) NULL,
	api_quota int4 NULL,
	api_used int4 NULL,
	c_priority int4 NULL DEFAULT 5,
	client_alias varchar(150) NULL DEFAULT NULL::character varying,
	pwd_fail_times int4 NULL DEFAULT 0,
	fail_treshhold int4 NULL DEFAULT 3,
	security_level_id varchar(10) NULL DEFAULT 'SYSTEM'::character varying,
	signup_num varchar(100) NULL DEFAULT NULL::character varying,
	access_token_quota int4 NULL DEFAULT 0,
	refresh_token_quota int4 NULL DEFAULT 0,
	client_secret varchar(128) NULL,
	start_date int8 NULL,
	end_date int8 NULL,
	start_time_per_day int8 NULL,
	end_time_per_day int8 NULL,
	time_zone varchar(200) NULL,
	CONSTRAINT tsmp_client_pkey PRIMARY KEY (client_id)
);


-- dgr_dev.tsmp_client_cert definition

-- Drop table

-- DROP TABLE tsmp_client_cert;

CREATE TABLE tsmp_client_cert (
	client_cert_id int8 NOT NULL,
	client_id varchar(40) NOT NULL,
	cert_file_name varchar(255) NOT NULL,
	file_content bytea NOT NULL,
	pub_key varchar(1024) NOT NULL,
	cert_version varchar(255) NULL,
	cert_serial_num varchar(255) NOT NULL,
	s_algorithm_id varchar(255) NULL,
	algorithm_id varchar(255) NOT NULL,
	cert_thumbprint varchar(1024) NOT NULL,
	iuid varchar(255) NULL,
	issuer_name varchar(255) NOT NULL,
	suid varchar(255) NULL,
	create_at int8 NOT NULL,
	expired_at int8 NOT NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	key_size int4 NOT NULL DEFAULT 0,
	CONSTRAINT tsmp_client_cert_pkey PRIMARY KEY (client_cert_id)
);
CREATE INDEX index_tsmp_client_cert_01 ON tsmp_client_cert USING btree (client_id);


-- dgr_dev.tsmp_client_cert2 definition

-- Drop table

-- DROP TABLE tsmp_client_cert2;

CREATE TABLE tsmp_client_cert2 (
	client_cert2_id int8 NOT NULL,
	client_id varchar(40) NOT NULL,
	cert_file_name varchar(255) NOT NULL,
	file_content bytea NOT NULL,
	pub_key varchar(1024) NOT NULL,
	cert_version varchar(255) NULL,
	cert_serial_num varchar(255) NOT NULL,
	s_algorithm_id varchar(255) NULL,
	algorithm_id varchar(255) NOT NULL,
	cert_thumbprint varchar(1024) NOT NULL,
	iuid varchar(255) NULL,
	issuer_name varchar(255) NOT NULL,
	suid varchar(255) NULL,
	create_at int8 NOT NULL,
	expired_at int8 NOT NULL,
	key_size int4 NOT NULL DEFAULT 0,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	CONSTRAINT tsmp_client_cert2_pkey PRIMARY KEY (client_cert2_id)
);
CREATE INDEX index_tsmp_client_cert2_01 ON tsmp_client_cert2 USING btree (client_id);


-- dgr_dev.tsmp_client_group definition

-- Drop table

-- DROP TABLE tsmp_client_group;

CREATE TABLE tsmp_client_group (
	client_id varchar(40) NOT NULL,
	group_id varchar(10) NOT NULL,
	CONSTRAINT tsmp_client_group_pkey PRIMARY KEY (client_id, group_id)
);


-- dgr_dev.tsmp_client_host definition

-- Drop table

-- DROP TABLE tsmp_client_host;

CREATE TABLE tsmp_client_host (
	host_seq int4 NOT NULL,
	client_id varchar(40) NOT NULL,
	host_name varchar(50) NOT NULL,
	host_ip varchar(15) NOT NULL,
	create_time timestamp NOT NULL,
	CONSTRAINT tsmp_client_host_pkey PRIMARY KEY (host_seq)
);


-- dgr_dev.tsmp_client_log definition

-- Drop table

-- DROP TABLE tsmp_client_log;

CREATE TABLE tsmp_client_log (
	log_seq varchar(20) NOT NULL,
	is_login int4 NOT NULL,
	agent varchar(500) NOT NULL,
	event_type varchar(10) NOT NULL,
	event_msg varchar(300) NOT NULL,
	event_time timestamp NOT NULL,
	client_id varchar(40) NOT NULL,
	client_ip varchar(15) NOT NULL,
	user_name varchar(30) NULL,
	txsn varchar(20) NOT NULL,
	create_time timestamp NOT NULL,
	CONSTRAINT tsmp_client_log_pkey PRIMARY KEY (log_seq)
);


-- dgr_dev.tsmp_client_vgroup definition

-- Drop table

-- DROP TABLE tsmp_client_vgroup;

CREATE TABLE tsmp_client_vgroup (
	client_id varchar(40) NOT NULL,
	vgroup_id varchar(10) NOT NULL,
	CONSTRAINT tsmp_client_vgroup_pkey PRIMARY KEY (client_id, vgroup_id)
);


-- dgr_dev.tsmp_dc definition

-- Drop table

-- DROP TABLE tsmp_dc;

CREATE TABLE tsmp_dc (
	dc_id int8 NOT NULL,
	dc_code varchar(30) NOT NULL,
	dc_memo varchar(300) NULL,
	active bool NULL,
	create_user VARCHAR(1000) NOT NULL,
	create_time timestamp NOT NULL,
	update_user VARCHAR(1000) NULL,
	update_time timestamp NULL,
	CONSTRAINT tsmp_dc_pkey PRIMARY KEY (dc_id)
);


-- dgr_dev.tsmp_dc_module definition

-- Drop table

-- DROP TABLE tsmp_dc_module;

CREATE TABLE tsmp_dc_module (
	dc_id int8 NOT NULL,
	module_id int8 NOT NULL,
	node_task_id int8 NULL,
	CONSTRAINT tsmp_dc_module_pkey PRIMARY KEY (dc_id, module_id)
);


-- dgr_dev.tsmp_dc_node definition

-- Drop table

-- DROP TABLE tsmp_dc_node;

CREATE TABLE tsmp_dc_node (
	node varchar(30) NOT NULL,
	dc_id int8 NOT NULL,
	node_task_id int8 NULL,
	CONSTRAINT tsmp_dc_node_pkey PRIMARY KEY (node, dc_id)
);


-- dgr_dev.tsmp_dp_about definition

-- Drop table

-- DROP TABLE tsmp_dp_about;

CREATE TABLE tsmp_dp_about (
	seq_id bigserial NOT NULL,
	about_subject varchar(100) NOT NULL,
	about_desc varchar(4000) NOT NULL,
	create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	CONSTRAINT tsmp_dp_about_pkey PRIMARY KEY (seq_id)
);


-- dgr_dev.tsmp_dp_api_app definition

-- Drop table

-- DROP TABLE tsmp_dp_api_app;

CREATE TABLE tsmp_dp_api_app (
	ref_app_id int8 NOT NULL,
	ref_api_uid varchar(36) NOT NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	CONSTRAINT tsmp_dp_api_app_pkey PRIMARY KEY (ref_app_id, ref_api_uid)
);


-- dgr_dev.tsmp_dp_api_auth2 definition

-- Drop table

-- DROP TABLE tsmp_dp_api_auth2;

CREATE TABLE tsmp_dp_api_auth2 (
	api_auth_id int8 NOT NULL,
	ref_client_id varchar(40) NOT NULL,
	ref_api_uid varchar(36) NOT NULL,
	apply_status varchar(10) NOT NULL,
	apply_purpose varchar(3000) NOT NULL,
	ref_review_user varchar(255) NULL,
	review_remark varchar(3000) NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	keyword_search varchar(6000) NULL,
	CONSTRAINT tsmp_dp_api_auth2_pkey PRIMARY KEY (api_auth_id)
);


-- dgr_dev.tsmp_dp_api_theme definition

-- Drop table

-- DROP TABLE tsmp_dp_api_theme;

CREATE TABLE tsmp_dp_api_theme (
	ref_api_theme_id int8 NOT NULL,
	ref_api_uid varchar(36) NOT NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	CONSTRAINT tsmp_dp_api_theme_pkey PRIMARY KEY (ref_api_theme_id, ref_api_uid)
);


-- dgr_dev.tsmp_dp_api_view_log definition

-- Drop table

-- DROP TABLE tsmp_dp_api_view_log;

CREATE TABLE tsmp_dp_api_view_log (
	seq_id bigserial NOT NULL,
	api_id varchar(36) NOT NULL,
	from_ip varchar(50) NULL,
	view_date date NOT NULL,
	create_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	CONSTRAINT tsmp_dp_api_view_log_pkey PRIMARY KEY (seq_id)
);


-- dgr_dev.tsmp_dp_app definition

-- Drop table

-- DROP TABLE tsmp_dp_app;

CREATE TABLE tsmp_dp_app (
	app_id bigserial NOT NULL,
	ref_app_cate_id int8 NOT NULL,
	"name" varchar(100) NOT NULL,
	intro varchar(4000) NOT NULL,
	author varchar(100) NULL,
	data_status bpchar(1) NOT NULL,
	org_id varchar(255) NULL,
	create_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	keyword_search varchar(100) NULL,
	CONSTRAINT tsmp_dp_app_pkey PRIMARY KEY (app_id)
);


-- dgr_dev.tsmp_dp_app_category definition

-- Drop table

-- DROP TABLE tsmp_dp_app_category;

CREATE TABLE tsmp_dp_app_category (
	app_cate_id bigserial NOT NULL,
	app_cate_name varchar(100) NOT NULL,
	data_sort int4 NULL,
	org_id varchar(255) NULL,
	create_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	keyword_search varchar(100) NULL,
	CONSTRAINT tsmp_dp_app_category_pkey PRIMARY KEY (app_cate_id)
);


-- dgr_dev.tsmp_dp_appt_job definition

-- Drop table

-- DROP TABLE tsmp_dp_appt_job;

CREATE TABLE tsmp_dp_appt_job (
	appt_job_id int8 NOT NULL,
	ref_item_no varchar(50) NOT NULL,
	ref_subitem_no varchar(100) NULL,
	status varchar(1) NOT NULL DEFAULT 'W'::character varying,
	in_params varchar(4000) NULL,
	exec_result varchar(4000) NULL,
	exec_owner varchar(20) NULL DEFAULT 'SYS'::character varying,
	stack_trace varchar(4000) NULL,
	job_step varchar(50) NULL,
	start_date_time timestamp NOT NULL,
	from_job_id int8 NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	identif_data varchar(4000) NULL,
	period_uid varchar(36) NOT NULL,
	period_items_id int8 NOT NULL DEFAULT 0,
	period_nexttime int8 NULL,
	CONSTRAINT tsmp_dp_appt_job_pkey PRIMARY KEY (appt_job_id),
	CONSTRAINT uk_tsmp_dp_appt_job_1 UNIQUE (period_uid, period_items_id, period_nexttime)
);


-- dgr_dev.tsmp_dp_appt_rjob definition

-- Drop table

-- DROP TABLE tsmp_dp_appt_rjob;

CREATE TABLE tsmp_dp_appt_rjob (
	appt_rjob_id varchar(36) NOT NULL,
	rjob_name varchar(60) NOT NULL,
	cron_expression varchar(700) NOT NULL,
	cron_json varchar(4000) NOT NULL,
	cron_desc varchar(300) NULL,
	next_date_time int8 NOT NULL,
	last_date_time int8 NULL,
	eff_date_time int8 NULL,
	inv_date_time int8 NULL,
	remark varchar(300) NULL,
	status varchar(1) NOT NULL DEFAULT '1'::character varying,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	keyword_search varchar(396) NULL,
	CONSTRAINT tsmp_dp_appt_rjob_pkey PRIMARY KEY (appt_rjob_id)
);


-- dgr_dev.tsmp_dp_appt_rjob_d definition

-- Drop table

-- DROP TABLE tsmp_dp_appt_rjob_d;

CREATE TABLE tsmp_dp_appt_rjob_d (
	appt_rjob_d_id int8 NOT NULL,
	appt_rjob_id varchar(36) NOT NULL,
	ref_item_no varchar(50) NOT NULL,
	ref_subitem_no varchar(100) NULL,
	in_params varchar(4000) NULL,
	identif_data varchar(4000) NULL,
	sort_by int4 NOT NULL DEFAULT 0,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	keyword_search varchar(186) NULL,
	CONSTRAINT tsmp_dp_appt_rjob_d_pkey PRIMARY KEY (appt_rjob_d_id)
);


-- dgr_dev.tsmp_dp_callapi definition

-- Drop table

-- DROP TABLE tsmp_dp_callapi;

CREATE TABLE tsmp_dp_callapi (
	callapi_id int8 NOT NULL,
	req_url varchar(500) NOT NULL,
	req_msg varchar(4000) NULL,
	resp_msg varchar(4000) NULL,
	token_url varchar(500) NULL,
	sign_code_url varchar(500) NULL,
	auth varchar(500) NOT NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	CONSTRAINT tsmp_dp_callapi_pkey PRIMARY KEY (callapi_id)
);


-- dgr_dev.tsmp_dp_chk_layer definition

-- Drop table

-- DROP TABLE tsmp_dp_chk_layer;

CREATE TABLE tsmp_dp_chk_layer (
	chk_layer_id int8 NOT NULL,
	review_type varchar(20) NOT NULL,
	layer int4 NOT NULL,
	role_id varchar(10) NOT NULL,
	status varchar(1) NOT NULL DEFAULT '1'::character varying,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	CONSTRAINT tsmp_dp_chk_layer_pkey PRIMARY KEY (review_type, layer, role_id)
);
CREATE INDEX index_tsmp_dp_chk_layer_01 ON tsmp_dp_chk_layer USING btree (chk_layer_id);


-- dgr_dev.tsmp_dp_chk_log definition

-- Drop table

-- DROP TABLE tsmp_dp_chk_log;

CREATE TABLE tsmp_dp_chk_log (
	chk_log_id int8 NOT NULL,
	req_orders_id int8 NOT NULL,
	req_orderm_id int8 NOT NULL,
	layer int4 NOT NULL,
	req_comment varchar(200) NOT NULL,
	review_status varchar(20) NOT NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	CONSTRAINT tsmp_dp_chk_log_pkey PRIMARY KEY (chk_log_id)
);


-- dgr_dev.tsmp_dp_clientext definition

-- Drop table

-- DROP TABLE tsmp_dp_clientext;

CREATE TABLE tsmp_dp_clientext (
	client_id varchar(40) NOT NULL,
	client_seq_id int8 NOT NULL,
	content_txt varchar(1000) NOT NULL,
	reg_status bpchar(1) NOT NULL DEFAULT '0'::bpchar,
	pwd_status bpchar(1) NOT NULL DEFAULT '1'::bpchar,
	pwd_reset_key varchar(22) NULL,
	review_remark varchar(3000) NULL,
	ref_review_user varchar(255) NULL,
	resubmit_date_time timestamp NULL,
	public_flag bpchar(1) NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	keyword_search varchar(4000) NULL,
	CONSTRAINT tsmp_dp_clientext_pkey PRIMARY KEY (client_id),
	CONSTRAINT uk_tsmp_dp_clientext_1 UNIQUE (client_seq_id)
);
CREATE INDEX index_tsmp_dp_clientext_01 ON tsmp_dp_clientext USING btree (client_seq_id);


-- dgr_dev.tsmp_dp_denied_module definition

-- Drop table

-- DROP TABLE tsmp_dp_denied_module;

CREATE TABLE tsmp_dp_denied_module (
	ref_module_name varchar(255) NOT NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	CONSTRAINT tsmp_dp_denied_module_pkey PRIMARY KEY (ref_module_name)
);


-- dgr_dev.tsmp_dp_faq_answer definition

-- Drop table

-- DROP TABLE tsmp_dp_faq_answer;

CREATE TABLE tsmp_dp_faq_answer (
	answer_id bigserial NOT NULL,
	answer_name varchar(4000) NOT NULL,
	answer_name_en varchar(4000) NULL,
	ref_question_id int8 NOT NULL,
	create_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_time timestamp NOT NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	keyword_search varchar(8000) NULL,
	CONSTRAINT tsmp_dp_faq_answer_pkey PRIMARY KEY (answer_id)
);


-- dgr_dev.tsmp_dp_faq_question definition

-- Drop table

-- DROP TABLE tsmp_dp_faq_question;

CREATE TABLE tsmp_dp_faq_question (
	question_id bigserial NOT NULL,
	question_name varchar(4000) NOT NULL,
	question_name_en varchar(4000) NULL,
	data_sort int4 NULL,
	data_status bpchar(1) NOT NULL DEFAULT '1'::bpchar,
	create_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_time timestamp NOT NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	keyword_search varchar(8000) NULL,
	CONSTRAINT tsmp_dp_faq_question_pkey PRIMARY KEY (question_id)
);


-- dgr_dev.tsmp_dp_file definition

-- Drop table

-- DROP TABLE tsmp_dp_file;

CREATE TABLE tsmp_dp_file (
	file_id int8 NOT NULL,
	file_name varchar(100) NOT NULL,
	file_path varchar(300) NOT NULL,
	ref_file_cate_code varchar(50) NOT NULL,
	ref_id int8 NOT NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	keyword_search varchar(400) NULL,
	is_blob varchar(1) NULL DEFAULT 'N'::character varying,
	is_tmpfile varchar(1) NULL DEFAULT 'N'::character varying,
	blob_data bytea NULL,
	CONSTRAINT tsmp_dp_file_pkey PRIMARY KEY (file_id),
	CONSTRAINT tsmp_dp_file_uq UNIQUE (file_name, ref_file_cate_code, ref_id)
);
CREATE INDEX index_tsmp_dp_file_01 ON tsmp_dp_file USING btree (ref_file_cate_code, ref_id);


-- dgr_dev.tsmp_dp_items definition

-- Drop table

-- DROP TABLE tsmp_dp_items;

CREATE TABLE tsmp_dp_items (
	item_id int8 NOT NULL,
	item_no varchar(20) NOT NULL,
	item_name varchar(100) NOT NULL,
	subitem_no varchar(20) NOT NULL,
	subitem_name varchar(100) NOT NULL,
	sort_by int4 NOT NULL DEFAULT 0,
	is_default varchar(1) NULL,
	param1 varchar(255) NULL,
	param2 varchar(255) NULL,
	param3 varchar(255) NULL,
	param4 varchar(255) NULL,
	param5 varchar(255) NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	keyword_search varchar(200) NULL,
	locale varchar(10) NOT NULL DEFAULT 'zh-TW'::character varying,
	CONSTRAINT tsmp_dp_items_pk PRIMARY KEY (item_no, subitem_no, locale)
);


-- dgr_dev.tsmp_dp_mail_log definition

-- Drop table

-- DROP TABLE tsmp_dp_mail_log;

CREATE TABLE tsmp_dp_mail_log (
	maillog_id int8 NOT NULL,
	recipients varchar(100) NOT NULL,
	template_txt varchar(3800) NOT NULL,
	ref_code varchar(20) NOT NULL,
	"result" varchar(1) NOT NULL DEFAULT '0'::character varying,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	keyword_search varchar(4000) NULL,
	CONSTRAINT tsmp_dp_mail_log_pkey PRIMARY KEY (maillog_id)
);


-- dgr_dev.tsmp_dp_mail_tplt definition

-- Drop table

-- DROP TABLE tsmp_dp_mail_tplt;

CREATE TABLE tsmp_dp_mail_tplt (
	mailtplt_id int8 NOT NULL,
	code varchar(20) NOT NULL,
	template_txt varchar(2000) NOT NULL,
	remark varchar(100) NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	keyword_search varchar(2120) NULL,
	CONSTRAINT tsmp_dp_mail_tplt_code_key UNIQUE (code),
	CONSTRAINT tsmp_dp_mail_tplt_pkey PRIMARY KEY (mailtplt_id)
);


-- dgr_dev.tsmp_dp_mail_tplten definition

-- Drop table

-- DROP TABLE tsmp_dp_mail_tplten;

CREATE TABLE tsmp_dp_mail_tplten (
	mailtplt_id int8 NOT NULL,
	code varchar(20) NOT NULL,
	template_txt varchar(2000) NOT NULL,
	remark varchar(100) NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	keyword_search varchar(2120) NULL,
	CONSTRAINT tsmp_dp_mail_tplten_code_key UNIQUE (code),
	CONSTRAINT tsmp_dp_mail_tplten_pkey PRIMARY KEY (mailtplt_id)
);


-- dgr_dev.tsmp_dp_mail_tplttw definition

-- Drop table

-- DROP TABLE tsmp_dp_mail_tplttw;

CREATE TABLE tsmp_dp_mail_tplttw (
	mailtplt_id int8 NOT NULL,
	code varchar(20) NOT NULL,
	template_txt varchar(2000) NOT NULL,
	remark varchar(100) NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	keyword_search varchar(2120) NULL,
	CONSTRAINT tsmp_dp_mail_tplttw_code_key UNIQUE (code),
	CONSTRAINT tsmp_dp_mail_tplttw_pkey PRIMARY KEY (mailtplt_id)
);


-- dgr_dev.tsmp_dp_news definition

-- Drop table

-- DROP TABLE tsmp_dp_news;

CREATE TABLE tsmp_dp_news (
	news_id int8 NOT NULL,
	new_title varchar(100) NOT NULL DEFAULT '_'::character varying,
	new_content varchar(4000) NOT NULL,
	status varchar(1) NOT NULL DEFAULT '1'::character varying,
	org_id varchar(255) NOT NULL,
	post_date_time timestamp NOT NULL,
	ref_type_subitem_no varchar(20) NOT NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	keyword_search varchar(2148) NULL,
	CONSTRAINT tsmp_dp_news_pkey PRIMARY KEY (news_id)
);


-- dgr_dev.tsmp_dp_req_orderd1 definition

-- Drop table

-- DROP TABLE tsmp_dp_req_orderd1;

CREATE TABLE tsmp_dp_req_orderd1 (
	req_orderd1_id int8 NOT NULL,
	ref_req_orderm_id int8 NOT NULL,
	client_id varchar(40) NOT NULL,
	api_uid varchar(36) NOT NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	CONSTRAINT tsmp_dp_req_orderd1_pkey PRIMARY KEY (req_orderd1_id)
);


-- dgr_dev.tsmp_dp_req_orderd2 definition

-- Drop table

-- DROP TABLE tsmp_dp_req_orderd2;

CREATE TABLE tsmp_dp_req_orderd2 (
	req_orderd2_id int8 NOT NULL,
	ref_req_orderm_id int8 NOT NULL,
	api_uid varchar(36) NOT NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	public_flag bpchar(1) NULL,
	CONSTRAINT tsmp_dp_req_orderd2_pkey PRIMARY KEY (req_orderd2_id)
);
CREATE INDEX index_tsmp_dp_req_orderd2_01 ON tsmp_dp_req_orderd2 USING btree (api_uid);


-- dgr_dev.tsmp_dp_req_orderd2d definition

-- Drop table

-- DROP TABLE tsmp_dp_req_orderd2d;

CREATE TABLE tsmp_dp_req_orderd2d (
	req_orderd2_id int8 NOT NULL,
	api_uid varchar(36) NOT NULL,
	ref_theme_id int8 NOT NULL,
	req_orderd2d_id int8 NOT NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	CONSTRAINT tsmp_dp_req_orderd2d_pkey PRIMARY KEY (req_orderd2_id, api_uid, ref_theme_id),
	CONSTRAINT tsmp_dp_req_orderd2d_req_orderd2d_id_key UNIQUE (req_orderd2d_id)
);


-- dgr_dev.tsmp_dp_req_orderd3 definition

-- Drop table

-- DROP TABLE tsmp_dp_req_orderd3;

CREATE TABLE tsmp_dp_req_orderd3 (
	req_orderd3_id int8 NOT NULL,
	ref_req_orderm_id int8 NOT NULL,
	client_id varchar(40) NOT NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	CONSTRAINT tsmp_dp_req_orderd3_pkey PRIMARY KEY (req_orderd3_id)
);


-- dgr_dev.tsmp_dp_req_orderd5 definition

-- Drop table

-- DROP TABLE tsmp_dp_req_orderd5;

CREATE TABLE tsmp_dp_req_orderd5 (
	req_orderd5_id int8 NOT NULL,
	client_id varchar(255) NOT NULL,
	ref_req_orderm_id int8 NOT NULL,
	ref_open_apikey_id int8 NULL,
	open_apikey varchar(1024) NULL,
	secret_key varchar(1024) NULL,
	open_apikey_alias varchar(255) NOT NULL,
	times_threshold int4 NOT NULL DEFAULT 0,
	expired_at int8 NOT NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	CONSTRAINT tsmp_dp_req_orderd5_pkey PRIMARY KEY (req_orderd5_id)
);
CREATE INDEX index_tsmp_dp_req_orderd5_01 ON tsmp_dp_req_orderd5 USING btree (open_apikey);


-- dgr_dev.tsmp_dp_req_orderd5d definition

-- Drop table

-- DROP TABLE tsmp_dp_req_orderd5d;

CREATE TABLE tsmp_dp_req_orderd5d (
	ref_req_orderd5_id int8 NOT NULL,
	ref_api_uid varchar(36) NOT NULL,
	req_orderd5d_id int8 NOT NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	CONSTRAINT tsmp_dp_req_orderd5d_pkey PRIMARY KEY (ref_req_orderd5_id, ref_api_uid)
);


-- dgr_dev.tsmp_dp_req_orderm definition

-- Drop table

-- DROP TABLE tsmp_dp_req_orderm;

CREATE TABLE tsmp_dp_req_orderm (
	req_orderm_id int8 NOT NULL,
	req_order_no varchar(30) NOT NULL,
	req_type varchar(20) NOT NULL,
	req_subtype varchar(20) NULL,
	client_id varchar(40) NOT NULL,
	org_id varchar(255) NULL,
	req_desc varchar(1000) NOT NULL,
	req_user_id varchar(400) NULL,
	effective_date timestamp NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	keyword_search varchar(1020) NULL,
	CONSTRAINT tsmp_dp_req_orderm_pkey PRIMARY KEY (req_orderm_id),
	CONSTRAINT tsmp_dp_req_orderm_req_order_no_key UNIQUE (req_order_no)
);


-- dgr_dev.tsmp_dp_req_orders definition

-- Drop table

-- DROP TABLE tsmp_dp_req_orders;

CREATE TABLE tsmp_dp_req_orders (
	req_orders_id int8 NOT NULL,
	req_orderm_id int8 NOT NULL,
	layer int4 NOT NULL,
	req_comment varchar(200) NOT NULL,
	review_status varchar(20) NOT NULL DEFAULT 'WAIT1'::character varying,
	status varchar(1) NOT NULL DEFAULT '1'::character varying,
	proc_flag int4 NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	keyword_search varchar(200) NULL,
	CONSTRAINT tsmp_dp_req_orders_pkey PRIMARY KEY (req_orders_id)
);


-- dgr_dev.tsmp_dp_site_map definition

-- Drop table

-- DROP TABLE tsmp_dp_site_map;

CREATE TABLE tsmp_dp_site_map (
	site_id bigserial NOT NULL,
	site_parent_id int8 NOT NULL,
	site_desc varchar(200) NOT NULL,
	data_sort int4 NOT NULL,
	site_url varchar(200) NULL,
	create_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	CONSTRAINT tsmp_dp_site_map_pkey PRIMARY KEY (site_id)
);


-- dgr_dev.tsmp_dp_theme_category definition

-- Drop table

-- DROP TABLE tsmp_dp_theme_category;

CREATE TABLE tsmp_dp_theme_category (
	id bigserial NOT NULL,
	theme_name varchar(100) NOT NULL,
	data_status bpchar(1) NOT NULL DEFAULT '1'::bpchar,
	data_sort int4 NULL,
	org_id varchar(255) NULL,
	create_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	keyword_search varchar(100) NULL,
	CONSTRAINT tsmp_dp_theme_category_pkey PRIMARY KEY (id)
);
CREATE INDEX index_tsmp_dp_theme_category_01 ON tsmp_dp_theme_category USING btree (theme_name);


-- dgr_dev.tsmp_events definition

-- Drop table

-- DROP TABLE tsmp_events;

CREATE TABLE tsmp_events (
	event_id int8 NOT NULL,
	event_type_id varchar(20) NOT NULL,
	event_name_id varchar(20) NOT NULL,
	module_name varchar(255) NOT NULL,
	module_version varchar(255) NULL,
	trace_id varchar(20) NOT NULL,
	info_msg varchar(4000) NULL,
	keep_flag varchar(1) NOT NULL DEFAULT 'N'::character varying,
	archive_flag varchar(1) NOT NULL DEFAULT 'N'::character varying,
	node_alias varchar(200) NULL,
	node_id varchar(200) NULL,
	thread_name varchar(1000) NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	CONSTRAINT tsmp_events_pkey PRIMARY KEY (event_id)
);


-- dgr_dev.tsmp_func definition

-- Drop table

-- DROP TABLE tsmp_func;

CREATE TABLE tsmp_func (
	func_code varchar(10) NOT NULL,
	func_name varchar(50) NOT NULL,
	func_name_en varchar(50) NULL DEFAULT NULL::character varying,
	func_desc varchar(300) NULL DEFAULT NULL::character varying,
	locale varchar(10) NOT NULL,
	update_user VARCHAR(1000) NOT NULL,
	update_time timestamp NOT NULL,
	func_url varchar(300) NULL DEFAULT NULL::character varying,
	CONSTRAINT tsmp_func_pkey PRIMARY KEY (func_code, locale)
);


-- dgr_dev.tsmp_group definition

-- Drop table

-- DROP TABLE tsmp_group;

CREATE TABLE tsmp_group (
	group_id varchar(10) NOT NULL,
	group_name varchar(150) NOT NULL,
	create_time timestamp NOT NULL,
	update_time timestamp NULL,
	create_user VARCHAR(1000) NOT NULL,
	update_user VARCHAR(1000) NULL,
	group_alias varchar(150) NULL DEFAULT NULL::character varying,
	group_desc varchar(1500) NULL DEFAULT NULL::character varying,
	group_access varchar(255) NULL DEFAULT NULL::character varying,
	security_level_id varchar(10) NULL DEFAULT 'SYSTEM'::character varying,
	allow_days int4 NULL DEFAULT 0,
	allow_times int4 NULL DEFAULT 0,
	vgroup_flag bpchar(1) NOT NULL DEFAULT 0,
	vgroup_id varchar(10) NULL,
	vgroup_name varchar(30) NULL,
	CONSTRAINT tsmp_group_pkey PRIMARY KEY (group_id)
);


-- dgr_dev.tsmp_group_api definition

-- Drop table

-- DROP TABLE tsmp_group_api;

CREATE TABLE tsmp_group_api (
	group_id varchar(10) NOT NULL,
	api_key varchar(255) NOT NULL,
	module_name varchar(100) NOT NULL,
	module_version varchar(20) NULL,
	create_time timestamp NOT NULL,
	CONSTRAINT tsmp_group_api_pkey PRIMARY KEY (group_id, api_key, module_name)
);


-- dgr_dev.tsmp_group_authorities definition

-- Drop table

-- DROP TABLE tsmp_group_authorities;

CREATE TABLE tsmp_group_authorities (
	group_authoritie_id varchar(10) NOT NULL,
	group_authoritie_name varchar(30) NOT NULL,
	group_authoritie_desc varchar(60) NULL,
	group_authoritie_level varchar(10) NULL,
	CONSTRAINT tsmp_group_authorities_group_authoritie_name_key UNIQUE (group_authoritie_name),
	CONSTRAINT tsmp_group_authorities_pkey PRIMARY KEY (group_authoritie_id)
);


-- dgr_dev.tsmp_group_authorities_map definition

-- Drop table

-- DROP TABLE tsmp_group_authorities_map;

CREATE TABLE tsmp_group_authorities_map (
	group_id varchar(10) NOT NULL,
	group_authoritie_id varchar(10) NOT NULL,
	CONSTRAINT tsmp_group_authorities_map_pkey PRIMARY KEY (group_id, group_authoritie_id)
);


-- dgr_dev.tsmp_group_times_log definition

-- Drop table

-- DROP TABLE tsmp_group_times_log;

CREATE TABLE tsmp_group_times_log (
	seq_no int8 NOT NULL,
	jti varchar(100) NOT NULL,
	group_id varchar(10) NULL,
	expire_time timestamp NULL,
	create_time timestamp NULL,
	reexpired_time timestamp NULL,
	times_quota int4 NULL,
	times_threshold int4 NULL,
	rejti varchar(100) NULL,
	CONSTRAINT tsmp_group_times_log_jti_group_id_key UNIQUE (jti, group_id),
	CONSTRAINT tsmp_group_times_log_pkey PRIMARY KEY (seq_no)
);


-- dgr_dev.tsmp_heartbeat definition

-- Drop table

-- DROP TABLE tsmp_heartbeat;

CREATE TABLE tsmp_heartbeat (
	node_id varchar(30) NOT NULL,
	start_time timestamp NOT NULL,
	update_time timestamp NOT NULL,
	node_info varchar(100) NULL,
	CONSTRAINT tsmp_heartbeat_pkey PRIMARY KEY (node_id)
);


-- dgr_dev.tsmp_node definition

-- Drop table

-- DROP TABLE tsmp_node;

CREATE TABLE tsmp_node (
	id varchar(255) NOT NULL,
	start_time timestamp NOT NULL,
	update_time timestamp NOT NULL,
	node varchar(30) NULL,
	CONSTRAINT tsmp_node_pkey PRIMARY KEY (id)
);


-- dgr_dev.tsmp_node_task definition

-- Drop table

-- DROP TABLE tsmp_node_task;

CREATE TABLE tsmp_node_task (
	id int8 NOT NULL,
	task_signature varchar(255) NOT NULL,
	task_id varchar(255) NOT NULL,
	task_arg varchar(4095) NOT NULL,
	coordination varchar(255) NOT NULL,
	execute_time timestamp NOT NULL,
	notice_node varchar(255) NOT NULL,
	notice_time timestamp NOT NULL,
	node varchar(30) NULL,
	module_name varchar(255) NULL,
	module_version varchar(255) NULL,
	CONSTRAINT tsmp_node_task_pkey PRIMARY KEY (id),
	CONSTRAINT tsmp_node_task_task_signature_task_id_key UNIQUE (task_signature, task_id)
);


-- dgr_dev.tsmp_node_task_work definition

-- Drop table

-- DROP TABLE tsmp_node_task_work;

CREATE TABLE tsmp_node_task_work (
	id int8 NOT NULL,
	node_task_id int8 NOT NULL,
	competitive_id varchar(255) NOT NULL,
	competitive_time timestamp NOT NULL,
	competitive_node varchar(255) NOT NULL,
	update_time timestamp NOT NULL,
	success bool NULL,
	error_msg varchar(1023) NULL,
	node varchar(30) NULL,
	CONSTRAINT tsmp_node_task_work_node_task_id_competitive_id_key UNIQUE (node_task_id, competitive_id),
	CONSTRAINT tsmp_node_task_work_pkey PRIMARY KEY (id)
);


-- dgr_dev.tsmp_notice_log definition

-- Drop table

-- DROP TABLE tsmp_notice_log;

CREATE TABLE tsmp_notice_log (
	notice_log_id int8 NOT NULL,
	notice_src varchar(100) NOT NULL,
	notice_mthd varchar(10) NOT NULL,
	notice_key varchar(255) NOT NULL,
	detail_id int8 NULL,
	last_notice_date_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT tsmp_notice_log_pkey PRIMARY KEY (notice_log_id)
);


-- dgr_dev.tsmp_open_apikey definition

-- Drop table

-- DROP TABLE tsmp_open_apikey;

CREATE TABLE tsmp_open_apikey (
	open_apikey_id int8 NOT NULL,
	client_id varchar(255) NOT NULL,
	open_apikey varchar(1024) NOT NULL,
	secret_key varchar(1024) NOT NULL,
	open_apikey_alias varchar(255) NOT NULL,
	times_quota int4 NOT NULL DEFAULT '-1'::integer,
	times_threshold int4 NOT NULL DEFAULT '-1'::integer,
	expired_at int8 NOT NULL,
	revoked_at int8 NULL,
	open_apikey_status varchar(1) NOT NULL DEFAULT 1,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	rollover_flag varchar(1) NOT NULL DEFAULT 'N'::character varying,
	CONSTRAINT tsmp_open_apikey_pkey PRIMARY KEY (open_apikey_id),
	CONSTRAINT uk_tsmp_open_apikey_01 UNIQUE (open_apikey)
);


-- dgr_dev.tsmp_open_apikey_map definition

-- Drop table

-- DROP TABLE tsmp_open_apikey_map;

CREATE TABLE tsmp_open_apikey_map (
	open_apikey_map_id int8 NOT NULL,
	ref_open_apikey_id int8 NOT NULL,
	ref_api_uid varchar(36) NOT NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	CONSTRAINT tsmp_open_apikey_map_pkey PRIMARY KEY (open_apikey_map_id)
);


-- dgr_dev.tsmp_organization definition

-- Drop table

-- DROP TABLE tsmp_organization;

CREATE TABLE tsmp_organization (
	org_id varchar(255) NOT NULL,
	org_name varchar(100) NULL,
	parent_id varchar(10) NULL,
	org_path varchar(255) NULL,
	create_user VARCHAR(1000) NULL,
	create_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	update_time timestamp NULL,
	contact_name varchar(50) NULL,
	contact_tel varchar(50) NULL,
	contact_mail varchar(100) NULL,
	org_code varchar(100) NULL,
	CONSTRAINT tsmp_organization_org_name_key UNIQUE (org_name),
	CONSTRAINT tsmp_organization_pkey PRIMARY KEY (org_id)
);


-- dgr_dev.tsmp_otp definition

-- Drop table

-- DROP TABLE tsmp_otp;

CREATE TABLE tsmp_otp (
	opaque varchar(100) NOT NULL,
	otp varchar(10) NULL,
	err_times int4 NULL,
	create_time timestamp NULL,
	valid_time timestamp NULL,
	check_time timestamp NULL,
	used bpchar(1) NULL,
	CONSTRAINT tsmp_otp_pkey PRIMARY KEY (opaque)
);


-- dgr_dev.tsmp_reg_host definition

-- Drop table

-- DROP TABLE tsmp_reg_host;

CREATE TABLE tsmp_reg_host (
	reghost_id varchar(10) NOT NULL,
	reghost varchar(30) NOT NULL,
	reghost_status bpchar(1) NOT NULL DEFAULT 'S'::bpchar,
	enabled bpchar(1) NOT NULL DEFAULT 'N'::bpchar,
	clientid varchar(40) NOT NULL,
	heartbeat timestamp NULL,
	memo varchar(300) NULL,
	create_user VARCHAR(1000) NOT NULL,
	create_time timestamp NOT NULL,
	update_user VARCHAR(1000) NULL,
	update_time timestamp NULL,
	CONSTRAINT tsmp_reg_host_pkey PRIMARY KEY (reghost_id),
	CONSTRAINT tsmp_reg_host_reghost_key UNIQUE (reghost)
);


-- dgr_dev.tsmp_reg_module definition

-- Drop table

-- DROP TABLE tsmp_reg_module;

CREATE TABLE tsmp_reg_module (
	reg_module_id int8 NOT NULL,
	module_name varchar(255) NOT NULL,
	module_version varchar(255) NOT NULL,
	module_src varchar(1) NOT NULL,
	latest varchar(1) NOT NULL DEFAULT 'N'::character varying,
	upload_date_time timestamp NOT NULL,
	upload_user varchar(255) NOT NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	CONSTRAINT tsmp_reg_module_pk PRIMARY KEY (reg_module_id),
	CONSTRAINT tsmp_reg_module_uk UNIQUE (module_name, module_version)
);


-- dgr_dev.tsmp_report_data definition

-- Drop table

-- DROP TABLE tsmp_report_data;

CREATE TABLE tsmp_report_data (
	id int8 NOT NULL,
	report_type int4 NOT NULL,
	date_time_range_type int4 NOT NULL,
	last_row_date_time timestamp NOT NULL,
	statistics_status bpchar(1) NOT NULL,
	string_group1 varchar(255) NULL,
	string_group2 varchar(255) NULL,
	string_group3 varchar(255) NULL,
	int_value1 int8 NULL,
	int_value2 int8 NULL,
	int_value3 int8 NULL,
	orgid varchar(255) NOT NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	CONSTRAINT tsmp_report_data_pk PRIMARY KEY (id)
);


-- dgr_dev.tsmp_report_url definition

-- Drop table

-- DROP TABLE tsmp_report_url;

CREATE TABLE tsmp_report_url (
	report_id varchar(8) NOT NULL,
	time_range bpchar(1) NOT NULL,
	report_url varchar(2000) NOT NULL,
	CONSTRAINT uk_report UNIQUE (report_id, time_range)
);


-- dgr_dev.tsmp_req_log definition

-- Drop table

-- DROP TABLE tsmp_req_log;

CREATE TABLE tsmp_req_log (
	id varchar(63) NOT NULL,
	rtime timestamp NOT NULL,
	atype varchar(3) NOT NULL,
	module_name varchar(255) NOT NULL,
	module_version varchar(255) NOT NULL,
	node_alias varchar(255) NOT NULL,
	node_id varchar(255) NOT NULL,
	url varchar(255) NOT NULL,
	cip varchar(255) NOT NULL,
	orgid varchar(255) NOT NULL,
	txid varchar(255) NULL,
	entry varchar(255) NULL,
	cid varchar(255) NULL,
	tuser varchar(255) NULL,
	jti varchar(255) NULL,
	CONSTRAINT tsmp_req_log_pk PRIMARY KEY (id)
);


-- dgr_dev.tsmp_req_log_history definition

-- Drop table

-- DROP TABLE tsmp_req_log_history;

CREATE TABLE tsmp_req_log_history (
	id varchar(63) NOT NULL,
	rtime timestamp NOT NULL,
	atype varchar(3) NOT NULL,
	module_name varchar(255) NOT NULL,
	module_version varchar(255) NOT NULL,
	node_alias varchar(255) NOT NULL,
	node_id varchar(255) NOT NULL,
	url varchar(255) NOT NULL,
	cip varchar(255) NOT NULL,
	orgid varchar(255) NOT NULL,
	txid varchar(255) NULL,
	entry varchar(255) NULL,
	cid varchar(255) NULL,
	tuser varchar(255) NULL,
	jti varchar(255) NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	CONSTRAINT tsmp_req_log_history_pk PRIMARY KEY (id)
);


-- dgr_dev.tsmp_res_log definition

-- Drop table

-- DROP TABLE tsmp_res_log;

CREATE TABLE tsmp_res_log (
	id varchar(63) NOT NULL,
	exe_status bpchar(1) NOT NULL,
	elapse int4 NOT NULL,
	rcode varchar(63) NOT NULL,
	http_status int4 NOT NULL,
	err_msg varchar(4000) NULL,
	CONSTRAINT tsmp_res_log_pk PRIMARY KEY (id)
);


-- dgr_dev.tsmp_res_log_history definition

-- Drop table

-- DROP TABLE tsmp_res_log_history;

CREATE TABLE tsmp_res_log_history (
	id varchar(63) NOT NULL,
	exe_status bpchar(1) NOT NULL,
	elapse int4 NOT NULL,
	rcode varchar(63) NOT NULL,
	http_status int4 NOT NULL,
	err_msg varchar(4000) NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	CONSTRAINT tsmp_res_log_history_pk PRIMARY KEY (id)
);


-- dgr_dev.tsmp_role definition

-- Drop table

-- DROP TABLE tsmp_role;

CREATE TABLE tsmp_role (
	role_id varchar(10) NOT NULL,
	role_name varchar(30) NOT NULL,
	create_user VARCHAR(1000) NOT NULL,
	create_time timestamp NOT NULL,
	role_alias varchar(255) NULL,
	CONSTRAINT tsmp_role_pkey PRIMARY KEY (role_id)
);


-- dgr_dev.tsmp_role_alert definition

-- Drop table

-- DROP TABLE tsmp_role_alert;

CREATE TABLE tsmp_role_alert (
	role_id varchar(10) NOT NULL,
	alert_id int4 NOT NULL,
	CONSTRAINT tsmp_role_alert_pkey PRIMARY KEY (role_id, alert_id)
);


-- dgr_dev.tsmp_role_func definition

-- Drop table

-- DROP TABLE tsmp_role_func;

CREATE TABLE tsmp_role_func (
	role_id varchar(10) NOT NULL,
	func_code varchar(10) NOT NULL,
	CONSTRAINT tsmp_role_func_pkey PRIMARY KEY (role_id, func_code)
);


-- dgr_dev.tsmp_role_privilege definition

-- Drop table

-- DROP TABLE tsmp_role_privilege;

CREATE TABLE tsmp_role_privilege (
	role_id varchar(10) NOT NULL,
	role_scope varchar(30) NULL,
	CONSTRAINT tsmp_role_privilege_pkey PRIMARY KEY (role_id)
);


-- dgr_dev.tsmp_role_role_mapping definition

-- Drop table

-- DROP TABLE tsmp_role_role_mapping;

CREATE TABLE tsmp_role_role_mapping (
	role_name varchar(50) NULL,
	role_name_mapping varchar(50) NULL,
	role_role_id int8 NOT NULL,
	CONSTRAINT tsmp_role_role_mapping_pkey PRIMARY KEY (role_role_id)
);


-- dgr_dev.tsmp_role_txid_map definition

-- Drop table

-- DROP TABLE tsmp_role_txid_map;

CREATE TABLE tsmp_role_txid_map (
	role_txid_map_id int8 NOT NULL,
	role_id varchar(10) NOT NULL,
	txid varchar(10) NOT NULL,
	list_type varchar(1) NOT NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	keyword_search varchar(20) NULL,
	CONSTRAINT tsmp_role_txid_map_pkey PRIMARY KEY (role_txid_map_id),
	CONSTRAINT tsmp_role_txid_map_role_id_txid_key UNIQUE (role_id, txid)
);


-- dgr_dev.tsmp_rtn_code definition

-- Drop table

-- DROP TABLE tsmp_rtn_code;

CREATE TABLE tsmp_rtn_code (
	tsmp_rtn_code varchar(20) NOT NULL,
	locale varchar(10) NOT NULL,
	tsmp_rtn_msg varchar(300) NOT NULL,
	tsmp_rtn_desc varchar(300) NULL,
	CONSTRAINT tsmp_rtn_code_pkey PRIMARY KEY (tsmp_rtn_code, locale)
);


-- dgr_dev.tsmp_security_level definition

-- Drop table

-- DROP TABLE tsmp_security_level;

CREATE TABLE tsmp_security_level (
	security_level_id varchar(10) NOT NULL,
	security_level_name varchar(30) NOT NULL,
	security_level_desc varchar(60) NULL,
	CONSTRAINT tsmp_security_level_pkey PRIMARY KEY (security_level_id),
	CONSTRAINT tsmp_security_level_security_level_name_key UNIQUE (security_level_name)
);


-- dgr_dev.tsmp_sess_attrs definition

-- Drop table

-- DROP TABLE tsmp_sess_attrs;

CREATE TABLE tsmp_sess_attrs (
	api_session_id varchar(100) NOT NULL,
	attr_name varchar(20) NOT NULL,
	attr_values varchar(4000) NULL,
	update_time timestamp NULL,
	CONSTRAINT tsmp_sess_attrs_pkey PRIMARY KEY (api_session_id, attr_name)
);


-- dgr_dev.tsmp_session definition

-- Drop table

-- DROP TABLE tsmp_session;

CREATE TABLE tsmp_session (
	api_session_id varchar(100) NOT NULL,
	cust_id varchar(30) NULL,
	cust_name varchar(20) NULL,
	const_data varchar(1000) NOT NULL,
	create_time timestamp NOT NULL,
	user_ip varchar(15) NULL,
	is_login bool NULL,
	CONSTRAINT tsmp_session_pkey PRIMARY KEY (api_session_id)
);


-- dgr_dev.tsmp_setting definition

-- Drop table

-- DROP TABLE tsmp_setting;

CREATE TABLE tsmp_setting (
	id varchar(255) NOT NULL,
	value varchar(4095) NULL,
	memo varchar(512) NULL DEFAULT NULL::character varying,
	CONSTRAINT tsmp_setting_pkey PRIMARY KEY (id)
);


-- dgr_dev.tsmp_sso_user_secret definition

-- Drop table

-- DROP TABLE tsmp_sso_user_secret;

CREATE TABLE tsmp_sso_user_secret (
	user_secret_id int8 NOT NULL,
	user_name varchar(400) NOT NULL,
	secret varchar(100) NOT NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SSO SYSTEM'::character varying,
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	"version" int4 NULL DEFAULT 1,
	CONSTRAINT tsmp_sso_user_secret_pkey PRIMARY KEY (user_secret_id)
);


-- dgr_dev.tsmp_token_history definition

-- Drop table

-- DROP TABLE tsmp_token_history;

CREATE TABLE tsmp_token_history (
	seq_no int8 NOT NULL,
	user_nid varchar(255) NULL,
	user_name varchar(400) NULL,
	client_id varchar(40) NOT NULL,
	token_jti varchar(100) NOT NULL,
	"scope" varchar(2048) NOT NULL,
	expired_at timestamp NOT NULL,
	create_at timestamp NOT NULL,
	stime timestamp NULL,
	revoked_at timestamp NULL,
	revoked_status bpchar(2) NULL,
	retoken_jti varchar(100) NOT NULL,
	reexpired_at timestamp NOT NULL,
	rft_revoked_at timestamp NULL,
	rft_revoked_status varchar(10) NULL,
	token_quota int4 NULL,
	token_used int4 NULL,
	rft_quota int4 NULL,
	rft_used int4 NULL,
	CONSTRAINT tsmp_token_history_pkey PRIMARY KEY (seq_no)
);


-- dgr_dev.tsmp_token_history_housing definition

-- Drop table

-- DROP TABLE tsmp_token_history_housing;

CREATE TABLE tsmp_token_history_housing (
	seq_no int8 NOT NULL,
	user_nid varchar(255) NULL,
	user_name varchar(50) NULL,
	client_id varchar(40) NOT NULL,
	token_jti varchar(100) NOT NULL,
	"scope" varchar(2048) NOT NULL,
	expired_at timestamp NOT NULL,
	create_at timestamp NOT NULL,
	stime timestamp NULL,
	revoked_at timestamp NULL,
	revoked_status bpchar(2) NULL,
	retoken_jti varchar(100) NOT NULL,
	reexpired_at timestamp NOT NULL,
	CONSTRAINT tsmp_token_history_housing_pkey PRIMARY KEY (seq_no)
);


-- dgr_dev.tsmp_token_usage_count definition

-- Drop table

-- DROP TABLE tsmp_token_usage_count;

CREATE TABLE tsmp_token_usage_count (
	token_jti varchar(100) NOT NULL,
	times_threshold int4 NOT NULL,
	token_type bpchar(1) NOT NULL,
	expired_at timestamp NOT NULL,
	CONSTRAINT tsmp_token_usage_count_token_jti_key UNIQUE (token_jti)
);


-- dgr_dev.tsmp_token_usage_history definition

-- Drop table

-- DROP TABLE tsmp_token_usage_history;

CREATE TABLE tsmp_token_usage_history (
	seq_id int4 NOT NULL,
	tgtl_seq_id int4 NOT NULL,
	token_jti varchar(100) NOT NULL,
	"scope" varchar(2048) NULL,
	txtime timestamp NULL,
	expiredtime timestamp NULL,
	CONSTRAINT tsmp_token_usage_history_pkey PRIMARY KEY (seq_id, tgtl_seq_id)
);


-- dgr_dev.tsmp_txkey definition

-- Drop table

-- DROP TABLE tsmp_txkey;

CREATE TABLE tsmp_txkey (
	key_id int8 NOT NULL,
	tx_key varchar(64) NOT NULL,
	iv varchar(64) NULL,
	alg bpchar(1) NOT NULL,
	create_time timestamp NOT NULL
);


-- dgr_dev.tsmp_txtoken definition

-- Drop table

-- DROP TABLE tsmp_txtoken;

CREATE TABLE tsmp_txtoken (
	txtoken varchar(64) NOT NULL,
	txtoken_status bpchar(1) NOT NULL,
	create_time timestamp NOT NULL,
	use_time timestamp NULL,
	CONSTRAINT tsmp_txtoken_pkey PRIMARY KEY (txtoken)
);


-- dgr_dev.tsmp_user definition

-- Drop table

-- DROP TABLE tsmp_user;

CREATE TABLE tsmp_user (
	user_id varchar(10) NOT NULL,
	user_name varchar(50) NOT NULL,
	user_status bpchar(1) NOT NULL,
	user_email varchar(100) NOT NULL,
	logon_date timestamp NULL,
	logoff_date timestamp NULL,
	update_user VARCHAR(1000) NULL,
	update_time timestamp NULL,
	create_user VARCHAR(1000) NOT NULL,
	create_time timestamp NOT NULL,
	pwd_fail_times int4 NOT NULL,
	org_id varchar(255) NULL DEFAULT NULL::character varying,
	user_alias VARCHAR(1000) NULL DEFAULT NULL::character varying,
	CONSTRAINT tsmp_user_pkey PRIMARY KEY (user_id),
	CONSTRAINT tsmp_user_user_name_key UNIQUE (user_name)
);


-- dgr_dev.tsmp_vgroup definition

-- Drop table

-- DROP TABLE tsmp_vgroup;

CREATE TABLE tsmp_vgroup (
	vgroup_id varchar(10) NOT NULL,
	vgroup_name varchar(150) NOT NULL,
	vgroup_alias varchar(255) NULL,
	vgroup_desc varchar(1500) NULL,
	vgroup_access varchar(255) NULL,
	security_level_id varchar(10) NULL,
	allow_days int4 NOT NULL DEFAULT 0,
	allow_times int4 NOT NULL DEFAULT 0,
	create_user VARCHAR(1000) NOT NULL,
	create_time timestamp NOT NULL,
	update_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	CONSTRAINT tsmp_vgroup_pkey PRIMARY KEY (vgroup_id)
);


-- dgr_dev.tsmp_vgroup_authorities_map definition

-- Drop table

-- DROP TABLE tsmp_vgroup_authorities_map;

CREATE TABLE tsmp_vgroup_authorities_map (
	vgroup_id varchar(10) NOT NULL,
	vgroup_authoritie_id varchar(10) NOT NULL,
	CONSTRAINT tsmp_vgroup_authorities_map_pkey PRIMARY KEY (vgroup_id, vgroup_authoritie_id)
);


-- dgr_dev.tsmp_vgroup_group definition

-- Drop table

-- DROP TABLE tsmp_vgroup_group;

CREATE TABLE tsmp_vgroup_group (
	vgroup_id varchar(10) NOT NULL,
	group_id varchar(10) NOT NULL,
	create_time timestamp NOT NULL,
	CONSTRAINT tsmp_vgroup_group_pkey PRIMARY KEY (vgroup_id, group_id)
);


-- dgr_dev.tsmpn_api_module definition

-- Drop table

-- DROP TABLE tsmpn_api_module;

CREATE TABLE tsmpn_api_module (
	id int8 NOT NULL,
	module_name varchar(255) NOT NULL,
	module_version varchar(255) NOT NULL,
	module_app_class varchar(255) NOT NULL,
	module_bytes bytea NOT NULL,
	module_md5 varchar(255) NOT NULL,
	module_type varchar(255) NOT NULL,
	upload_time timestamp NOT NULL,
	uploader_name varchar(255) NOT NULL,
	status_time timestamp NULL,
	status_user varchar(255) NULL,
	active bool NOT NULL,
	node_task_id int8 NULL,
	org_id varchar(255) NULL DEFAULT NULL::character varying,
	target_version varchar(30) NULL DEFAULT NULL::character varying,
	CONSTRAINT tsmpn_api_module_pkey PRIMARY KEY (id),
	CONSTRAINT uk_api_module_2 UNIQUE (module_name, module_version)
);


-- dgr_dev.tsmpn_node_task definition

-- Drop table

-- DROP TABLE tsmpn_node_task;

CREATE TABLE tsmpn_node_task (
	id int8 NOT NULL,
	task_signature varchar(255) NOT NULL,
	task_id varchar(255) NOT NULL,
	task_arg varchar(4000) NULL,
	coordination varchar(255) NOT NULL,
	execute_time timestamp NOT NULL,
	notice_node varchar(255) NOT NULL,
	node varchar(255) NOT NULL,
	notice_time timestamp NOT NULL,
	CONSTRAINT tsmpn_node_task_pk PRIMARY KEY (id),
	CONSTRAINT tsmpn_node_task_uq UNIQUE (task_signature, task_id)
);


-- dgr_dev.tsmpn_site definition

-- Drop table

-- DROP TABLE tsmpn_site;

CREATE TABLE tsmpn_site (
	site_id bigserial NOT NULL,
	site_code varchar(30) NOT NULL,
	site_memo text NULL,
	active bool NOT NULL,
	create_user VARCHAR(1000) NULL,
	create_time timestamp NOT NULL,
	update_user VARCHAR(1000) NULL,
	update_time timestamp NULL,
	protocol_type varchar(20) NOT NULL,
	binding_ip varchar(20) NOT NULL,
	binding_port int4 NOT NULL,
	app_pool varchar(255) NOT NULL,
	root_path text NULL,
	clr_version varchar(30) NULL,
	CONSTRAINT pk__tsmpn_site PRIMARY KEY (site_id)
);


-- dgr_dev.tsmpn_site_module definition

-- Drop table

-- DROP TABLE tsmpn_site_module;

CREATE TABLE tsmpn_site_module (
	site_id int4 NOT NULL,
	module_id int4 NOT NULL,
	node_task_id int4 NULL,
	CONSTRAINT pk__tsmpn_site__module PRIMARY KEY (site_id)
);


-- dgr_dev.tsmpn_site_node definition

-- Drop table

-- DROP TABLE tsmpn_site_node;

CREATE TABLE tsmpn_site_node (
	node varchar(30) NOT NULL,
	site_id int4 NOT NULL,
	node_task_id int4 NULL,
	CONSTRAINT pk__tsmpn_site__node PRIMARY KEY (node, site_id)
);


-- dgr_dev.users definition

-- Drop table

-- DROP TABLE users;

CREATE TABLE users (
	username varchar(50) NOT NULL,
	"password" varchar(60) NOT NULL,
	enabled int4 NOT NULL,
	CONSTRAINT users_pkey PRIMARY KEY (username)
);


-- dgr_dev.authorities definition

-- Drop table

-- DROP TABLE authorities;

CREATE TABLE authorities (
	username varchar(50) NOT NULL,
	authority varchar(50) NOT NULL,
	CONSTRAINT authorities_username_authority_key UNIQUE (username, authority),
	CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES users(username)
);


-- dgr_dev.group_authorities definition

-- Drop table

-- DROP TABLE group_authorities;

CREATE TABLE group_authorities (
	group_id bigserial NOT NULL,
	authority varchar(50) NOT NULL,
	CONSTRAINT group_authorities_group_id_key UNIQUE (group_id),
	CONSTRAINT fk_group_authorities_group FOREIGN KEY (group_id) REFERENCES "groups"(id)
);


-- dgr_dev.group_members definition

-- Drop table

-- DROP TABLE group_members;

CREATE TABLE group_members (
	id bigserial NOT NULL,
	username varchar(50) NOT NULL,
	group_id bigserial NOT NULL,
	CONSTRAINT group_members_group_id_key UNIQUE (group_id),
	CONSTRAINT group_members_pkey PRIMARY KEY (id),
	CONSTRAINT fk_group_members_group FOREIGN KEY (group_id) REFERENCES "groups"(id)
);


-- dgr_dev.tsmp_api_detail definition

-- Drop table

-- DROP TABLE tsmp_api_detail;

CREATE TABLE tsmp_api_detail (
	id int8 NOT NULL,
	api_module_id int8 NOT NULL,
	api_key varchar(255) NOT NULL,
	api_name varchar(255) NOT NULL,
	path_of_json varchar(1024) NOT NULL,
	method_of_json varchar(1023) NOT NULL,
	params_of_json varchar(1023) NOT NULL,
	headers_of_json varchar(1023) NOT NULL,
	consumes_of_json varchar(1023) NOT NULL,
	produces_of_json varchar(1023) NOT NULL,
	CONSTRAINT tsmp_api_detail_pkey PRIMARY KEY (id),
	CONSTRAINT uk_api_detail_1 UNIQUE (api_module_id, api_key),
	CONSTRAINT fk_api_detail_1 FOREIGN KEY (api_module_id) REFERENCES tsmp_api_module(id)
);


-- dgr_dev.tsmpn_api_detail definition

-- Drop table

-- DROP TABLE tsmpn_api_detail;

CREATE TABLE tsmpn_api_detail (
	id int8 NOT NULL,
	api_module_id int8 NOT NULL,
	api_key varchar(255) NOT NULL,
	api_name varchar(255) NOT NULL,
	path_of_json varchar(1024) NOT NULL,
	method_of_json varchar(1023) NOT NULL,
	params_of_json varchar(1023) NOT NULL,
	headers_of_json varchar(1023) NOT NULL,
	consumes_of_json varchar(1023) NOT NULL,
	produces_of_json varchar(1023) NOT NULL,
	url_rid bpchar(1) NOT NULL,
	CONSTRAINT tsmpn_api_detail_pkey PRIMARY KEY (id),
	CONSTRAINT uk_api_detail_2 UNIQUE (api_module_id, api_key),
	CONSTRAINT fk_api_detail_2 FOREIGN KEY (api_module_id) REFERENCES tsmpn_api_module(id)
);


-- dgr_dev.tsmpn_node_task_work definition

-- Drop table

-- DROP TABLE tsmpn_node_task_work;

CREATE TABLE tsmpn_node_task_work (
	id int8 NOT NULL,
	node_task_id int8 NOT NULL,
	competitive_id varchar(255) NOT NULL,
	competitive_time timestamp NOT NULL,
	competitive_node varchar(255) NOT NULL,
	node varchar(255) NOT NULL,
	update_time timestamp NOT NULL,
	success bool NULL,
	error_msg varchar(1023) NULL,
	CONSTRAINT tsmpn_node_task_work_pkey PRIMARY KEY (id),
	CONSTRAINT uk_node_task_work_3 UNIQUE (node_task_id, competitive_id),
	CONSTRAINT fk_node_task_work_4 FOREIGN KEY (node_task_id) REFERENCES tsmpn_node_task(id)
);


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
	ac_idp_user_id 			int8 			NOT NULL, 				-- ID 使用 RandomSeqLongUtil.getRandomLongByDefault() 產生 
	user_name 				VARCHAR(400)	NOT NULL, 				-- 使用者名稱(視IdP類型決定) 
	user_alias 				VARCHAR(400), 							-- 使用者別名 
	user_status 			VARCHAR(1) 		NOT NULL DEFAULT '1', 	-- 使用者狀態 1：request(預設)，2：allow，"3：denny 
	user_email 				VARCHAR(500), 							-- 使用者E-Mail 
	org_id 					VARCHAR(200), 							-- 組織ID from TSMP_ORGANIZATION.org_id 
	idp_type 				VARCHAR(50) 	NOT NULL, 				-- IdP類型 例如:"MS" 或 "GOOGLE" 
	code1					int8,									-- 安全驗證碼1
	code2					int8,									-- 安全驗證碼2
	id_token_jwtstr			VARCHAR(4000),							-- IdP ID Token 的 JWT  	
	access_token_jwtstr		VARCHAR(4000),							-- IdP Access Token 的 JWT  	
	refresh_token_jwtstr	VARCHAR(4000),							-- IdP Refresh Token 的 JWT  	
	create_date_time 		timestamp NULL DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間
	create_user 			VARCHAR(1000) 	DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 		timestamp , 								-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料
	update_user 			VARCHAR(1000), 							-- 更新人員 
	version 				INT DEFAULT 1, 							-- 版號 C/U時, 增量+1
	PRIMARY KEY (ac_idp_user_id), 
	UNIQUE (user_name, idp_type) 
);

-- 20230105, v4 新增 SSO IdP授權碼記錄檔, Mini Lee
CREATE TABLE IF NOT EXISTS dgr_ac_idp_auth_code ( 
	ac_idp_auth_code_id int8 			NOT NULL, 				-- ID (流水號) 
	auth_code 			VARCHAR(50) 	NOT NULL, 				-- 授權碼, 即 dgRcode 
	expire_date_time 	int8 			NOT NULL, 				-- 有效期限 超過此期限即不可使用此授權碼 
	status 				VARCHAR(1) 		NOT NULL DEFAULT '0', 	-- 狀態 0：可用；1：已使用；2：失效 
	idp_type 			VARCHAR(50), 							-- IdP類型 例如: "MS" 或 "GOOGLE" 
	user_name 			VARCHAR(400) 	NOT NULL, 				-- 使用者名稱(視IdP類型決定) from DGR_AC_IDP_USER.user_name 
	create_date_time 	timestamp NULL DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	create_user 		VARCHAR(1000) 	DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 	timestamp , 								-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料
	update_user 		VARCHAR(1000), 							-- 更新人員 
	version 			INT 			DEFAULT 1, 				-- 版號 C/U時, 增量+1 
	PRIMARY KEY (ac_idp_auth_code_id), 
	UNIQUE (auth_code) 
);

-- 20230105, v4 新增 SSO IdP資料, Mini Lee
CREATE TABLE IF NOT EXISTS dgr_ac_idp_info ( 
	ac_idp_info_id 		int8 			NOT NULL, 				-- ID 使用 RandomSeqLongUtil 機制產生 
	idp_type 			VARCHAR(50) 	NOT NULL, 				-- IdP類型 例如:"MS" 或 "GOOGLE"  
	client_id 			VARCHAR(400) 	NOT NULL, 				-- 用戶端編號(視IdP類型決定) 
	client_mima 		VARCHAR(200) 	NOT NULL, 				-- 用戶端密碼 
	client_name 		VARCHAR(200), 							-- 用戶端名稱 
	client_status 		VARCHAR(1) 		NOT NULL DEFAULT 'Y', 	-- 用戶端狀態 Y: 啟用 (預設), N: 停用 
	well_known_url 		VARCHAR(4000) 	NOT NULL, 				-- IdP 的 Well Known URL 
	callback_url 		VARCHAR(400) 	NOT NULL, 				-- 已授權的重新導向 URI
	auth_url 			VARCHAR(4000), 							-- IdP 的 Auth URL 
	access_token_url 	VARCHAR(4000), 							-- IdP 的 Access Token URL 
	scope 				VARCHAR(4000), 							-- IdP 的 scope 
	create_date_time 	timestamp NULL DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間
	create_user 		VARCHAR(1000) 	DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 	timestamp, 								-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料
	update_user 		VARCHAR(1000), 							-- 更新人員 
	version 			INT DEFAULT 1, 							-- 版號 C/U時, 增量+1
	PRIMARY KEY (ac_idp_info_id), 
	UNIQUE (idp_type, client_id) 
); 

-- 20230105, v4 拿掉 Authorities 的 FOREIGN KEY (username) REFERENCES users(username), Mini Lee
ALTER TABLE AUTHORITIES DROP CONSTRAINT fk_authorities_users;

-- 20230105, v4 增加 Authorities.username 長度為 NVARCHAR(400), Mini Lee
ALTER TABLE AUTHORITIES ALTER COLUMN USERNAME TYPE VARCHAR(400);

-- 20230223 v4 新增 SSO AC IdP資料 (LDAP), Mini Lee
CREATE TABLE IF NOT EXISTS dgr_ac_idp_info_ldap (  
	ac_idp_info_ldap_id 	BIGINT 			NOT NULL, 				-- ID 
	ldap_url 				VARCHAR(4000) 	NOT NULL, 				-- Ldap登入的URL 
	ldap_dn 				VARCHAR(4000) 	NOT NULL, 				-- Ldap登入的使用者DN 
	ldap_timeout 			INT 			NOT NULL, 				-- Ldap登入的連線timeout,單位毫秒 
	ldap_status 			VARCHAR(1) 		NOT NULL DEFAULT 'Y', 	-- Ldap狀態 
	approval_result_mail 	VARCHAR(4000) 	NOT NULL, 				-- 審核結果收件人,多組以逗號(,)隔開 
	icon_file				VARCHAR(4000),							-- 圖示檔案
	create_date_time 		timestamp NULL DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	create_user 			VARCHAR(1000) 	DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 		timestamp, 								-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料 
	update_user 			VARCHAR(1000), 							-- 更新人員 
	version 				INT 			DEFAULT 1, 				-- 版號 C/U時, 增量+1
	PRIMARY KEY (ac_idp_info_ldap_id) 
);

-- 20230320, v4, SSO AC IdP資料 (LDAP), 增加欄位, Mini Lee
ALTER TABLE DGR_AC_IDP_INFO_LDAP ADD page_title VARCHAR(400);

-- 20230325, v4, 增加長度, Tom
ALTER TABLE TSMP_API_REG ALTER COLUMN METHOD_OF_JSON TYPE VARCHAR(200);
ALTER TABLE TSMP_API_IMP ALTER COLUMN METHOD_OF_JSON TYPE VARCHAR(200);

-- 20230330 TSMP_DP_REQ_ORDERM 更改為 REQ_USER_ID varchar(255) Zoe_Lee
ALTER TABLE TSMP_DP_REQ_ORDERM ALTER COLUMN REQ_USER_ID TYPE varchar(255) ;

-- 20230406 v4 新增websocket proxy, Tom
CREATE TABLE dgr_web_socket_mapping (  
	ws_mapping_id 	        BIGINT 			NOT NULL, 				-- ID 
	site_name 				VARCHAR(50) 	NOT NULL, 			    -- 站點名稱
	target_ws               VARCHAR(200) 	NOT NULL,	            -- web socket server的目標
	memo                    VARCHAR(4000), 				            -- 備註
	create_date_time 		timestamp NULL DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	create_user 			VARCHAR(1000) 	DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 		timestamp, 								-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料 
	update_user 			VARCHAR(1000), 							-- 更新人員 
	version 				INT 			DEFAULT 1, 				-- 版號 C/U時, 增量+1
	keyword_search		    VARCHAR(250),						-- LikeSearch使用: site_name | target_ws
	CONSTRAINT PK_DGR_WEB_SOCKET_MAPPING PRIMARY KEY (ws_mapping_id),
	CONSTRAINT UK_DGR_WEB_SOCKET_MAPPING UNIQUE (site_name)
);

-- 20230327, v4 新增 Gateway IdP Auth記錄檔主檔	, Mini Lee
CREATE TABLE IF NOT EXISTS dgr_gtw_idp_auth_m (
	gtw_idp_auth_m_id BIGINT NOT NULL, -- ID (流水號)
	state VARCHAR(40) NOT NULL, -- 隨機字串UUID
	idp_type VARCHAR(50) NOT NULL, -- IdP類型
	client_id VARCHAR(40) NOT NULL, -- dgR 的 client_id
	auth_code VARCHAR(50), -- 授權碼, 即 dgRcode
	create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間
	create_user VARCHAR(1000) DEFAULT 'SYSTEM', -- 建立人員
	update_date_time TIMESTAMP, -- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料
	update_user VARCHAR(1000), -- 更新人員
	version INT DEFAULT 1, -- 版號 C/U時, 增量+1
	PRIMARY KEY (gtw_idp_auth_m_id),
	UNIQUE (state)
);

-- 20230327 v4 新增 Gateway IdP Auth記錄檔明細檔, Mini Lee
CREATE TABLE IF NOT EXISTS dgr_gtw_idp_auth_d (
	gtw_idp_auth_d_id BIGINT NOT NULL,   					    -- ID (流水號)                                        
	ref_gtw_idp_auth_m_id BIGINT NOT NULL,   					-- MasterPK                                            
	scope VARCHAR(200) NOT NULL,   					            -- OpenID Connect Scope                               
	create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,   	-- 建立日期 資料初始建立的人, 日期時間 				                                           
	create_user VARCHAR(1000) DEFAULT 'SYSTEM',   				-- 建立人員 	                                           
	update_date_time TIMESTAMP,   					            -- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料                                
	update_user VARCHAR(1000),   					            -- 更新人員                                
	version INT DEFAULT 1,   					                -- 版號 C/U時, 增量+1                           
	PRIMARY KEY (gtw_idp_auth_d_id)
);

-- 20230327, v4 新增 Gateway IdP授權碼記錄檔, Mini Lee
CREATE TABLE IF NOT EXISTS dgr_gtw_idp_auth_code (
	gtw_idp_auth_code_id BIGINT NOT NULL,
	auth_code VARCHAR(50) NOT NULL,
	phase VARCHAR(10) NOT NULL,
	status VARCHAR(1) NOT NULL DEFAULT 'A',
	expire_date_time BIGINT NOT NULL,
	idp_type VARCHAR(50) NOT NULL,
	client_id VARCHAR(40),
	user_name VARCHAR(400) NOT NULL,
	user_alias VARCHAR(400),
	user_email VARCHAR(500),
	user_picture VARCHAR(4000),
	id_token_jwtstr VARCHAR(4000),
	access_token_jwtstr VARCHAR(4000),
	refresh_token_jwtstr VARCHAR(4000),
	create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) DEFAULT 'SYSTEM',
	update_date_time TIMESTAMP,
	update_user VARCHAR(1000),
	version INT DEFAULT 1,
	PRIMARY KEY (gtw_idp_auth_code_id),
	UNIQUE (auth_code)
);

-- 20230327 v4 新增 Gateway IdP資料 (Oauth2.0 GOOGLE / MS), Mini Lee
CREATE TABLE IF NOT EXISTS dgr_gtw_idp_info_o (
	gtw_idp_info_o_id BIGINT NOT NULL,
	client_id VARCHAR(40) NOT NULL,
	idp_type VARCHAR(50) NOT NULL,
	status VARCHAR(1) NOT NULL DEFAULT 'Y',
	remark VARCHAR(200),
	idp_client_id VARCHAR(400) NOT NULL,
	idp_client_mima VARCHAR(200) NOT NULL,
	idp_client_name VARCHAR(200),
	well_known_url VARCHAR(4000),
	callback_url VARCHAR(400) NOT NULL,
	auth_url VARCHAR(4000),
	access_token_url VARCHAR(4000),
	scope VARCHAR(4000),
	create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) DEFAULT 'SYSTEM',
	update_date_time TIMESTAMP,
	update_user VARCHAR(1000),
	version INT DEFAULT 1,
	PRIMARY KEY (gtw_idp_info_o_id)
);

-- 20230327 v4 新增 Gateway IdP資料 (LDAP), Mini Lee
CREATE TABLE IF NOT EXISTS dgr_gtw_idp_info_l (
	gtw_idp_info_l_id BIGINT NOT NULL,
	client_id VARCHAR(40) NOT NULL,
	status VARCHAR(1) NOT NULL DEFAULT 'Y',
	remark VARCHAR(200),
	ldap_url VARCHAR(4000) NOT NULL,
	ldap_dn VARCHAR(4000) NOT NULL,
	ldap_timeout INT NOT NULL,
	icon_file VARCHAR(4000),
	page_title VARCHAR(400) NOT NULL,
	create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) DEFAULT 'SYSTEM',
	update_date_time TIMESTAMP,
	update_user VARCHAR(1000),
	version INT DEFAULT 1,
	PRIMARY KEY (gtw_idp_info_l_id)
);

-- 20230327 v4 新增 Gateway IdP資料 (JDBC), Mini Lee
CREATE TABLE IF NOT EXISTS dgr_gtw_idp_info_j (
	gtw_idp_info_j_id BIGINT NOT NULL,
	client_id VARCHAR(40) NOT NULL,
	idp_type VARCHAR(50) NOT NULL,
	status VARCHAR(1) NOT NULL DEFAULT 'Y',
	remark VARCHAR(200),
	host VARCHAR(4000) NOT NULL,
	port INT NOT NULL,
	db_schema VARCHAR(200) NOT NULL,
	db_user_name VARCHAR(200) NOT NULL,
	db_user_mima VARCHAR(200) NOT NULL,
	icon_file VARCHAR(4000),
	page_title VARCHAR(400) NOT NULL,
	create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) DEFAULT 'SYSTEM',
	update_date_time TIMESTAMP,
	update_user VARCHAR(1000),
	version INT DEFAULT 1,
	PRIMARY KEY (gtw_idp_info_j_id)
);


-- 20230407, v4 網站反向代理主檔, Kevin Cheng
CREATE TABLE IF NOT EXISTS dgr_website (
	dgr_website_id int8 NOT NULL,
	website_name varchar(50) NOT NULL,
	website_status varchar(1) NOT NULL DEFAULT 'Y',
	remark varchar(500) NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM',
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	version INT NULL DEFAULT 1,
	keyword_search varchar(600) NULL,
	CONSTRAINT dgr_website_pkey PRIMARY KEY (dgr_website_id)
);

-- 20230407, v4 網站反向代理明細檔, Kevin Cheng
CREATE TABLE IF NOT EXISTS dgr_website_detail (
	dgr_website_detail_id int8 NOT NULL,
	dgr_website_id int8 NOT NULL,
	probability int4 NOT NULL,
	url varchar(1000) NOT NULL,
	content_path varchar(200) NULL,
	create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
	create_user VARCHAR(1000) NULL DEFAULT 'SYSTEM',
	update_date_time timestamp NULL,
	update_user VARCHAR(1000) NULL,
	version INT NULL DEFAULT 1,
	keyword_search varchar(1500) NULL,
	CONSTRAINT dgr_website_detail_pkey PRIMARY KEY (dgr_website_detail_id)
);

-- 20230407, v4 入口網(DP)的Application	, Kevin Cheng
CREATE TABLE IF NOT EXISTS dp_app (
	dp_application_id BIGINT NOT NULL,                         -- ID
	application_name VARCHAR(50) NOT NULL,                     -- Application名稱
	application_desc VARCHAR(500),                             -- Application說明
	client_id VARCHAR(40) NOT NULL,                            -- CLIENT_ID
	open_apikey_id BIGINT,                                     -- 
	open_apikey_status VARCHAR(1),                             -- DGRK狀態
	user_name VARCHAR(400) NOT NULL,                           -- 使用者名稱(視IdP類型決定)
	id_token_jwtstr VARCHAR(4000) NOT NULL,                    -- IdP ID Token 的 JWT
	create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,      -- 建立日期
	create_user VARCHAR(1000) DEFAULT 'SYSTEM',                 -- 建立人員
	update_date_time TIMESTAMP,                                -- 更新日期
	update_user VARCHAR(1000),                                  -- 更新人員
	version INT DEFAULT 1,                                     -- 版號
	keyword_search varchar(600) NULL,
	PRIMARY KEY (dp_application_id)
);

-- 20230420, v4 入口網(DP)的使用者	, Kevin Cheng
CREATE TABLE IF NOT EXISTS dp_user (
    dp_user_id BIGINT NOT NULL,                           -- ID
    user_name VARCHAR(400) NOT NULL,                     -- 使用者名稱(視IdP類型決定)
    user_alias VARCHAR(200),                              -- 使用者別名
    id_token_jwtstr VARCHAR(4000) NOT NULL,               -- IdP ID Token 的 JWT
    user_identity VARCHAR(1) NOT NULL DEFAULT 'U',                 -- 使用者身份
    create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 建立日期
    create_user VARCHAR(1000) DEFAULT 'SYSTEM',            -- 建立人員
    update_date_time TIMESTAMP,                           -- 更新日期
    update_user VARCHAR(1000),                             -- 更新人員
    version INT DEFAULT 1,                                -- 版號
	keyword_search varchar(800) NULL,
    PRIMARY KEY (dp_user_id),
    UNIQUE (user_name)
);

-- 20230420, v4 入口網(DP)的API_DOC檔案	, Kevin Cheng
CREATE TABLE IF NOT EXISTS dp_file (
    dp_file_id BIGINT NOT NULL,                              -- ID
    file_name VARCHAR(100) NOT NULL,                        -- 檔案名稱
    module_name VARCHAR(150) NOT NULL,                      -- Module Name
    api_key VARCHAR(255) NOT NULL,                                   -- API Key
    blob_data BYTEA NOT NULL,                                -- 檔案本體
    create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,    -- 建立日期
    create_user VARCHAR(1000) DEFAULT 'SYSTEM',               -- 建立人員
    update_date_time TIMESTAMP,                              -- 更新日期
    update_user VARCHAR(1000),                                -- 更新人員
    version INT DEFAULT 1,                                   -- 版號
    PRIMARY KEY (dp_file_id)
);
-- 20230421, TSMP access_token 歷史紀錄, 增加欄位, Mini Lee
ALTER TABLE TSMP_TOKEN_HISTORY ADD IDP_TYPE VARCHAR(50);

-- 20230502, TSMP Token 歷史紀錄, 增加欄位, Mini Lee
ALTER TABLE TSMP_TOKEN_HISTORY ADD ID_TOKEN_JWTSTR VARCHAR(4000);
ALTER TABLE TSMP_TOKEN_HISTORY ADD REFRESH_TOKEN_JWTSTR VARCHAR(4000);

-- 20230515, v4 入口網(DP)的Application	, 刪除欄位 , Kevin Cheng
ALTER TABLE dp_app DROP COLUMN open_apikey_status;

-- 20230531, SSO AC IdP資料 (LDAP), 增加欄位, Mini Lee
ALTER TABLE dgr_ac_idp_info_ldap ADD ldap_base_dn VARCHAR(4000);

-- 20230531, Gateway IdP資料 (LDAP), 增加欄位, Mini Lee
ALTER TABLE dgr_gtw_idp_info_l ADD ldap_base_dn VARCHAR(4000);

-- 20230616, dashboard最新資料, Tom chu
CREATE TABLE IF NOT EXISTS dgr_dashboard_last_data (
	dashboard_id BIGINT NOT NULL,
	dashboard_type INT NOT NULL,
	time_type INT NOT NULL,
	str1 VARCHAR(1000),
	str2 VARCHAR(1000),
	str3 VARCHAR(1000),
	num1 BIGINT,
	num2 BIGINT,
	num3 BIGINT,
	num4 BIGINT,
	sort_num INT DEFAULT 1,                                  
    CONSTRAINT pk_dgr_dashboard_last_data PRIMARY KEY (dashboard_id)
);

--  20230616,dgr_dashboard_api_elapse dashboard   ,zoe Lee
CREATE TABLE IF NOT EXISTS dgr_dashboard_api_elapse (
    id    BIGINT NOT NULL,    -- ID
    rtime    TIMESTAMP NOT NULL,    -- record time
    cid    VARCHAR(50) NOT NULL,    -- token所攜帶的client id
    elapse    INT NOT NULL,    -- API執行耗時(ms)
    module_name    VARCHAR(150) NOT NULL,    -- 模組名稱
    txid    VARCHAR(255) NOT NULL,    -- 其實也就是ApiKey
    api_name    VARCHAR(500) NOT NULL,    -- api名稱
    exe_status    CHAR NOT NULL,    -- 此次API呼叫成功與否
    http_status    INT NOT NULL,    -- response的http status
    PRIMARY KEY(id)
);

-- 20230105, v4 修改 dp_app 欄位型態 , min
ALTER TABLE dp_app ALTER COLUMN application_name TYPE VARCHAR(50);
ALTER TABLE dp_app ALTER COLUMN application_desc TYPE VARCHAR(500);

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

-- 20230706 刪除dgr_dashboard_api_elapse ,zoe Lee
DROP TABLE dgr_dashboard_api_elapse;

-- 20230726, 增加欄位給dashboard統計用, Tom
ALTER TABLE tsmp_api ADD success BIGINT NOT NULL DEFAULT 0;
ALTER TABLE tsmp_api ADD fail BIGINT NOT NULL DEFAULT 0;
ALTER TABLE tsmp_api ADD total BIGINT NOT NULL DEFAULT 0;
ALTER TABLE tsmp_api ADD elapse BIGINT NOT NULL DEFAULT 0;

-- 20230802 , tsmp_req_res_log_history.rtime 格式改為datetime  , Zoe Lee
ALTER TABLE tsmp_req_res_log_history DROP COLUMN rtime;
ALTER TABLE tsmp_req_res_log_history ADD rtime timestamp;

-- 20230802 , tsmp_req_res_log_history增加新欄位year_month  , Zoe Lee
ALTER TABLE tsmp_req_res_log_history ADD rtime_year_month varchar(8);

-- 20230808 , v4 入口網(DP)的 API_VERSION	, Kevin Cheng
CREATE TABLE IF NOT EXISTS dp_api_version (
  dp_api_version_id BIGINT NOT NULL,                    -- ID
  module_name VARCHAR(150) NOT NULL,                    -- Module Name
  api_key VARCHAR(255) NOT NULL,                        -- API Key
  dp_api_version VARCHAR(10) NOT NULL,                  -- API版本號
  start_of_life BIGINT NOT NULL,                        -- API生命週期(起)
  end_of_life BIGINT,                                   -- API生命週期(迄)
  remark VARCHAR(500),                                  -- 備註
  time_zone varchar(200) NOT NULL,                      -- 時區
  create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 建立日期
  create_user VARCHAR(1000) DEFAULT 'SYSTEM',            -- 建立人員
  update_date_time TIMESTAMP,                           -- 更新日期
  update_user VARCHAR(1000),                             -- 更新人員
  version INT DEFAULT 1,                                -- 版號
  PRIMARY KEY (dp_api_version_id)
);

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
	create_date_time 		timestamp NULL DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	create_user 			VARCHAR(1000) DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 		timestamp, 							-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料 
	update_user 			VARCHAR(1000), 						-- 更新人員 
	version 				INT DEFAULT 1, 						-- 版號 C/U時, 增量+1 
	PRIMARY KEY (ac_idp_info_mldap_m_id) 
);


-- 20230824, SSO AC IdP資料 (Multi-LDAP) 明細檔, Mini Lee
CREATE TABLE IF NOT EXISTS dgr_ac_idp_info_mldap_d (  
	ac_idp_info_mldap_d_id 		BIGINT NOT NULL, 					-- ID 
	ref_ac_idp_info_mldap_m_id 	BIGINT NOT NULL, 					-- Master PK 
	order_no 					INT NOT NULL, 						-- 順序 
	ldap_url 					VARCHAR(4000) NOT NULL, 			-- Ldap登入的URL 
	ldap_dn 					VARCHAR(4000) NOT NULL, 			-- Ldap登入的使用者DN 
	ldap_base_dn 				VARCHAR(4000) NOT NULL, 			-- Ldap基礎DN   
	create_date_time 			timestamp NULL DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	create_user 				VARCHAR(1000) DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 			timestamp, 							-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料 
	update_user 				VARCHAR(1000), 						-- 更新人員 
	version 					INT DEFAULT 1, 						-- 版號 C/U時, 增量+1
	PRIMARY KEY (ac_idp_info_mldap_d_id)    
); 


-- 20230829 , dgr_dashboard_es_log  , Zoe Lee
CREATE TABLE dgr_dashboard_es_log (
    id    VARCHAR(63) NOT NULL,    -- ID
    rtime    timestamp NOT NULL,    -- record time
    module_name    VARCHAR(255) NOT NULL,    -- 模組名稱
    orgid    VARCHAR(255) NOT NULL,    -- API隸屬於哪個組織的ID
    txid    VARCHAR(255),    -- 其實也就是ApiKey
    cid    VARCHAR(255),    -- token所攜帶的client id
    exe_status    CHAR NOT NULL,    -- 此次API呼叫成功與否
    elapse    INT NOT NULL,    -- API執行耗時(ms)
    http_status    INT NOT NULL,    -- response的http status
    rtime_year_month    VARCHAR(8),    -- RTIME的年月
    CONSTRAINT pk_dgr_dashboard_es_log  PRIMARY KEY (id)
);


-- 202300906, v4, 移除多餘欄位, Kevin K
ALTER TABLE tsmp_dp_mail_log DROP COLUMN IF EXISTS keyword_search;
-- 202300906, v4, 增加紀錄錯誤訊息欄位, Kevin K
ALTER TABLE tsmp_dp_mail_log ADD COLUMN stack_trace VARCHAR(4000);

-- 20230908, 移除不用的table, Tom
DROP TABLE TSMP_REQ_LOG_HISTORY;
DROP TABLE TSMP_RES_LOG_HISTORY;

-- 20230912, Gateway IdP資料 (API), Mini Lee
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
	page_title 			VARCHAR(400) NOT NULL, 				-- 登入頁標題 
	create_date_time 	timestamp NULL DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	create_user 		VARCHAR(1000) DEFAULT 'SYSTEM', 		-- 建立人員 
	update_date_time 	timestamp, 							-- 更新日期 表示最後Update的人, 日期時間, Null 表示是新建資料 
	update_user 		VARCHAR(1000), 						-- 更新人員 
	version 			INT DEFAULT 1, 						-- 版號 C/U時, 增量+1  
	PRIMARY KEY (gtw_idp_info_a_id)   
);

-- 20230912, Gateway IdP授權碼記錄檔, 增加欄位, Mini Lee
ALTER TABLE DGR_GTW_IDP_AUTH_CODE ADD API_RESP VARCHAR(4000);

-- 20230912, TSMP Token 歷史紀錄, 增加欄位, Mini Lee
ALTER TABLE TSMP_TOKEN_HISTORY ADD API_RESP VARCHAR(4000);

-- 20230914, 移除DP的table, min
DROP TABLE dp_app;
DROP TABLE dp_user;
DROP TABLE dp_file;
DROP TABLE dp_api_version;

-- 20230918, Gateway IdP Auth記錄檔主檔, 增加欄位, Mini Lee
ALTER TABLE dgr_gtw_idp_auth_m ADD redirect_uri VARCHAR(400); 

-- 20230918, TSMP用戶端OAuth2驗證資料(Spring), 增加欄位, Mini Lee
ALTER TABLE oauth_client_details ADD web_server_redirect_uri1 VARCHAR(255); 
ALTER TABLE oauth_client_details ADD web_server_redirect_uri2 VARCHAR(255);
ALTER TABLE oauth_client_details ADD web_server_redirect_uri3 VARCHAR(255);
ALTER TABLE oauth_client_details ADD web_server_redirect_uri4 VARCHAR(255);
ALTER TABLE oauth_client_details ADD web_server_redirect_uri5 VARCHAR(255);

-- 20230919, 來源IP 增加填寫Hostname, 增加欄位長度 , Zoe Lee
ALTER TABLE tsmp_client_host ALTER COLUMN host_ip type varchar(255);

-- 20230920, TSMP API基本資料, 增加欄位 API_RELEASE_TIME, Kevin Cheng
ALTER TABLE TSMP_API ADD API_RELEASE_TIME TIMESTAMP NULL;

-- 20230926, API匯入匯出要有MOCK資料, Tom
ALTER TABLE tsmp_api_imp ADD mock_status_code CHAR(3);
ALTER TABLE tsmp_api_imp ADD mock_headers VARCHAR(2000);
ALTER TABLE tsmp_api_imp ADD mock_body VARCHAR(2000);

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
    create_date_time    timestamp,    -- 建立日期
    create_user    VARCHAR(1000) DEFAULT 'SYSTEM',    -- 建立人員
    update_date_time    timestamp,    -- 更新日期
    update_user    VARCHAR(1000),    -- 更新人員
    version    INT DEFAULT '1',    -- 版號
    keyword_search    VARCHAR(200),    -- LikeSearch使用
    CONSTRAINT PK_ PRIMARY KEY (ac_idp_info_api_id)
);

-- 20231003 , zoe lee 更改 dgr_ac_idp_user user_alias 型態
ALTER TABLE dgr_ac_idp_user ALTER COLUMN user_alias TYPE varchar(400);

-- 20231011, rdb連線資訊, tom
CREATE TABLE dgr_rdb_connection (
    connection_name    VARCHAR(50) NOT NULL,    -- 名稱
    jdbc_url    VARCHAR(200) NOT NULL,    -- 連線URL
    user_name    VARCHAR(100) NOT NULL,    -- 帳號
    mima    VARCHAR(500) NOT NULL,    -- MIMA
    max_pool_size    INT NOT NULL DEFAULT 10,    -- 最大連線數量
    connection_timeout    INT NOT NULL DEFAULT 30000,    -- 連線取得超時設定(ms)
    idle_timeout    INT NOT NULL DEFAULT 600000,    -- 空閒連線的存活時間(ms)
    max_lifetime    INT NOT NULL DEFAULT 1800000,    -- 連線的最大存活時間(ms)
    data_source_property    VARCHAR(4000),    -- DataSourceProperty的設定
    create_date_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,      -- 建立日期
    create_user VARCHAR(1000) DEFAULT 'SYSTEM',            -- 建立人員
    update_date_time timestamp,                           -- 更新日期
    update_user VARCHAR(1000),                             -- 更新人員
    version INT DEFAULT 1,                                -- 版號
    CONSTRAINT pk_dgr_rdb_connection PRIMARY KEY(connection_name)
);

-- 20231019, DGR_WEBSITE 網站反向代理主檔, 增加欄位, Mini Lee
ALTER TABLE dgr_website ADD auth VARCHAR(1) DEFAULT 'N' NOT NULL;
ALTER TABLE dgr_website ADD sql_injection VARCHAR(1) DEFAULT 'N' NOT NULL;
ALTER TABLE dgr_website ADD traffic VARCHAR(1) DEFAULT 'N' NOT NULL;
ALTER TABLE dgr_website ADD xss VARCHAR(1) DEFAULT 'N' NOT NULL;
ALTER TABLE dgr_website ADD xxe VARCHAR(1) DEFAULT 'N' NOT NULL;
ALTER TABLE dgr_website ADD tps INT DEFAULT 0 NOT NULL;
ALTER TABLE dgr_website ADD ignore_api VARCHAR(4000);

-- 20231020, 增加欄位長度, Mini Lee
ALTER TABLE dgr_gtw_idp_auth_m ALTER COLUMN state TYPE VARCHAR(1000);
ALTER TABLE dgr_gtw_idp_auth_code ALTER COLUMN auth_code TYPE VARCHAR(1000);

-- 20231030, DGR_WEBSITE 網站反向代理主檔, 增加欄位, TOM
ALTER TABLE dgr_website ADD show_log VARCHAR(1) DEFAULT 'N' NOT NULL;

-- 20231103, 增加欄位 ,Zoe Lee
ALTER TABLE tsmp_api_reg ADD redirect_by_ip char(1) DEFAULT 'N' NULL;
ALTER TABLE tsmp_api_reg ADD ip_for_redirect1 varchar(2000) NULL;
ALTER TABLE tsmp_api_reg ADD ip_src_url1 varchar(2000) NULL;
ALTER TABLE tsmp_api_reg ADD ip_for_redirect2 varchar(2000) NULL;
ALTER TABLE tsmp_api_reg ADD ip_src_url2 varchar(2000) NULL;
ALTER TABLE tsmp_api_reg ADD ip_for_redirect3 varchar(2000) NULL;
ALTER TABLE tsmp_api_reg ADD ip_src_url3 varchar(2000) NULL;
ALTER TABLE tsmp_api_reg ADD ip_for_redirect4 varchar(2000) NULL;
ALTER TABLE tsmp_api_reg ADD ip_src_url4 varchar(2000) NULL;
ALTER TABLE tsmp_api_reg ADD ip_for_redirect5 varchar(2000) NULL;
ALTER TABLE tsmp_api_reg ADD ip_src_url5 varchar(2000) NULL;

ALTER TABLE tsmp_api_reg ADD header_mask_key varchar(2000) NULL;
ALTER TABLE tsmp_api_reg ADD header_mask_policy char(1) DEFAULT '0'  NULL;
ALTER TABLE tsmp_api_reg ADD header_mask_policy_num int NULL;
ALTER TABLE tsmp_api_reg ADD header_mask_policy_symbol varchar(10)  NULL;

ALTER TABLE tsmp_api_reg ADD body_mask_keyword varchar(2000) NULL;
ALTER TABLE tsmp_api_reg ADD body_mask_policy char(1) DEFAULT '0' NULL;
ALTER TABLE tsmp_api_reg ADD body_mask_policy_num int NULL;
ALTER TABLE tsmp_api_reg ADD body_mask_policy_symbol varchar(10) NULL;

-- 20231108, 增加欄位 ,Zoe Lee
ALTER TABLE tsmp_api_imp ADD redirect_by_ip char(1) DEFAULT 'N' NULL;
ALTER TABLE tsmp_api_imp ADD ip_for_redirect1 varchar(2000) NULL;
ALTER TABLE tsmp_api_imp ADD ip_src_url1 varchar(2000) NULL;
ALTER TABLE tsmp_api_imp ADD ip_for_redirect2 varchar(2000) NULL;
ALTER TABLE tsmp_api_imp ADD ip_src_url2 varchar(2000) NULL;
ALTER TABLE tsmp_api_imp ADD ip_for_redirect3 varchar(2000) NULL;
ALTER TABLE tsmp_api_imp ADD ip_src_url3 varchar(2000) NULL;
ALTER TABLE tsmp_api_imp ADD ip_for_redirect4 varchar(2000) NULL;
ALTER TABLE tsmp_api_imp ADD ip_src_url4 varchar(2000) NULL;
ALTER TABLE tsmp_api_imp ADD ip_for_redirect5 varchar(2000) NULL;
ALTER TABLE tsmp_api_imp ADD ip_src_url5 varchar(2000) NULL;

ALTER TABLE tsmp_api_imp ADD header_mask_key varchar(2000) NULL;
ALTER TABLE tsmp_api_imp ADD header_mask_policy char(1) DEFAULT '0'  NULL;
ALTER TABLE tsmp_api_imp ADD header_mask_policy_num int NULL;
ALTER TABLE tsmp_api_imp ADD header_mask_policy_symbol varchar(10)  NULL;

ALTER TABLE tsmp_api_imp ADD body_mask_keyword varchar(2000) NULL;
ALTER TABLE tsmp_api_imp ADD body_mask_policy char(1) DEFAULT '0' NULL;
ALTER TABLE tsmp_api_imp ADD body_mask_policy_num int NULL;
ALTER TABLE tsmp_api_imp ADD body_mask_policy_symbol varchar(10) NULL;

-- 20231110, Gateway IdP Auth記錄檔主檔	, 增加欄位, Mini Lee
ALTER TABLE dgr_gtw_idp_auth_m ADD code_challenge VARCHAR(1000);
ALTER TABLE dgr_gtw_idp_auth_m ADD code_challenge_method VARCHAR(10);
 
-- 20231110, Gateway IdP授權碼記錄檔, 增加欄位, Mini Lee
ALTER TABLE dgr_gtw_idp_auth_code ADD state VARCHAR(1000);

-- 20231117 刪除 dgr_gtw_idp_info_j, Mini Lee
DROP TABLE dgr_gtw_idp_info_j;

-- 20231117, Gateway IdP資料 (JDBC), Mini Lee
CREATE TABLE dgr_gtw_idp_info_jdbc (  
	GTW_IDP_INFO_JDBC_ID BIGINT NOT NULL, 		-- ID 
	CLIENT_ID 			VARCHAR(40) NOT NULL, 	-- digiRunner 的 client_id 
	STATUS 				VARCHAR(1) NOT NULL DEFAULT 'Y', -- 狀態 
	REMARK 				VARCHAR(200), 			-- 說明 
	CONNECTION_NAME 	VARCHAR(50) NOT NULL, 	-- RDB連線資訊的名稱 
	SQL_PTMT 			VARCHAR(1000) NOT NULL, -- 查詢RDB的SQL(Prepare Statement) 
	SQL_PARAMS	 		VARCHAR(1000) NOT NULL, -- 查詢RDB的SQL參數 
	USER_MIMA_ALG 		VARCHAR(40) NOT NULL, 	-- RDB存放密碼所使用的演算法 
	USER_MIMA_COL_NAME 	VARCHAR(200) NOT NULL, 	-- RDB的密碼欄位名稱 
	IDT_SUB 			VARCHAR(200) NOT NULL, 	-- ID token 的 sub(唯一值) 值,對應RDB的欄位 
	IDT_NAME 			VARCHAR(200), 			-- ID token 的 name 值,對應RDB的欄位 
	IDT_EMAIL 			VARCHAR(200), 			-- ID token 的 email 值,對應RDB的欄位 
	IDT_PICTURE 		VARCHAR(200), 			-- ID token 的 picture 值,對應RDB的欄位 
	ICON_FILE 			VARCHAR(4000), 			-- 登入頁圖示檔案 
	PAGE_TITLE 			VARCHAR(400) NOT NULL, -- 登入頁標題
	CREATE_DATE_TIME 	timestamp NULL DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人
	CREATE_USER 		VARCHAR(1000) DEFAULT 'SYSTEM', -- 建立人員 
	UPDATE_DATE_TIME 	timestamp, 				-- 更新日期 表示最後Update的人
	UPDATE_USER 		VARCHAR(1000), 		-- 更新人員 
	VERSION 			INT DEFAULT 1,  		-- 版號 C/U時, 增量+1 
	PRIMARY KEY (GTW_IDP_INFO_JDBC_ID)    
);

-- 20231123, 增加欄位 ,Zoe Lee
ALTER TABLE tsmp_api ADD label1 varchar(20) NULL;
ALTER TABLE tsmp_api ADD label2 varchar(20) NULL;
ALTER TABLE tsmp_api ADD label3 varchar(20) NULL;
ALTER TABLE tsmp_api ADD label4 varchar(20) NULL;
ALTER TABLE tsmp_api ADD label5 varchar(20) NULL;

ALTER TABLE tsmp_api_imp ADD label1 varchar(20) NULL;
ALTER TABLE tsmp_api_imp ADD label2 varchar(20) NULL;
ALTER TABLE tsmp_api_imp ADD label3 varchar(20) NULL;
ALTER TABLE tsmp_api_imp ADD label4 varchar(20) NULL;
ALTER TABLE tsmp_api_imp ADD label5 varchar(20) NULL;

-- 20231130, TSMP_API_REG.SRC_URL 拿掉NOT NULL ,Zoe Lee
ALTER TABLE tsmp_api_reg ALTER COLUMN src_url DROP NOT NULL;

-- 20231201, 增加固定快取時間欄位, Tom
ALTER TABLE tsmp_api ADD fixed_cache_time INT DEFAULT 0 NOT NULL;
ALTER TABLE tsmp_api_imp ADD api_cache_flag CHAR(1) DEFAULT '1' NOT NULL;
ALTER TABLE tsmp_api_imp ADD fixed_cache_time INT DEFAULT 0 NOT NULL;

-- 20231207, X-Api-Key資料, Mini Lee
CREATE TABLE DGR_X_API_KEY (  
	API_KEY_ID 			BIGINT NOT NULL, 		-- ID 
	CLIENT_ID 			VARCHAR(40) NOT NULL, 	-- digiRunner 的 client_id 
	API_KEY_ALIAS 		VARCHAR(100) NOT NULL, -- X-Api-Key 別名 
	EFFECTIVE_AT 		BIGINT NOT NULL, 		-- 生效日期 
	EXPIRED_AT 			BIGINT NOT NULL, 		-- 到期日期 
	API_KEY 			VARCHAR(100), 			-- X-Api-Key 的值 	
	API_KEY_MASK 		VARCHAR(100) NOT NULL, 	-- X-Api-Key 經過遮罩的值 
	API_KEY_EN 			VARCHAR(100) NOT NULL, 	-- X-Api-Key 經過SHA256 的值 
	CREATE_DATE_TIME 	timestamp NULL DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	CREATE_USER 		VARCHAR(1000) DEFAULT 'SYSTEM', -- 建立人員 
	UPDATE_DATE_TIME 	timestamp, 				-- 更新日期 表示最後Update的人, 日期時間
	UPDATE_USER 		VARCHAR(1000), 		-- 更新人員 
	VERSION 			INT DEFAULT 1, 			-- 版號 C/U時, 增量+1  
	PRIMARY KEY (API_KEY_ID)    
);  

-- 20231207, X-Api-Key與群組關係, Mini Lee
CREATE TABLE DGR_X_API_KEY_MAP (  
	API_KEY_MAP_ID 		BIGINT NOT NULL, 		-- ID 
	REF_API_KEY_ID 		BIGINT NOT NULL, 		-- Master PK 
	GROUP_ID 			VARCHAR(10) NOT NULL, 	-- 群組 ID 
	CREATE_DATE_TIME 	timestamp NULL DEFAULT CURRENT_TIMESTAMP, -- 建立日期 
	CREATE_USER 		VARCHAR(1000) DEFAULT 'SYSTEM', -- 建立人員 
	UPDATE_DATE_TIME 	timestamp, 				-- 更新日期 
	UPDATE_USER 		VARCHAR(1000), 		-- 更新人員 
	VERSION 			INT DEFAULT 1, 			-- 版號
	PRIMARY KEY (API_KEY_MAP_ID)  
);

-- 20231212, 增加欄位 DP_CLIENT_SECRET, Kevin Cheng
ALTER TABLE tsmp_client ADD dp_client_secret VARCHAR(128);

-- 20231222, 調整欄位 dp_client_secret 為 dp_client_entry, 資料型態 nvarchar, Kevin Cheng
ALTER TABLE tsmp_client RENAME COLUMN dp_client_secret TO dp_client_entry;
ALTER TABLE tsmp_client ALTER COLUMN dp_client_entry TYPE VARCHAR(128);

-- 20231228, 調整欄位 dp_client_secret 長度從 128 到 1000, Kevin Cheng
ALTER TABLE tsmp_client ALTER COLUMN dp_client_entry TYPE VARCHAR(1000);

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
	IMPORT_CLIENT_RELATED 	BYTEA NOT NULL, 	-- 匯入的資料
	ANALYZE_CLIENT_RELATED 	BYTEA NOT NULL, 	-- 分析的資料
	CREATE_DATE_TIME 	timestamp NULL DEFAULT CURRENT_TIMESTAMP, -- 建立日期 資料初始建立的人, 日期時間 
	CREATE_USER 		VARCHAR(1000) DEFAULT 'SYSTEM', -- 建立人員 
	UPDATE_DATE_TIME 	timestamp, 				-- 更新日期 表示最後Update的人, 日期時間
	UPDATE_USER 		VARCHAR(1000), 		-- 更新人員 
	VERSION 			INT DEFAULT 1, 			-- 版號 C/U時, 增量+1  
	CONSTRAINT DGR_IMPORT_CLIENT_RELATED_TEMP_PK PRIMARY KEY (TEMP_ID)  
); 

-- 20240402, 增加api狀態, Tom
ALTER TABLE tsmp_api_imp ADD api_status CHAR(1) DEFAULT '2' NOT NULL;

-- 20240402,新增PUBLIC_FLAG,API_RELEASE_TIME匯入欄位, Webber
ALTER TABLE tsmp_api_imp ADD public_flag CHAR(1) NULL;
ALTER TABLE tsmp_api_imp ADD api_release_time timestamp NULL;

-- 20240429 , dgr_web_socket_mapping 新增欄位 ,Zoe Lee
ALTER TABLE dgr_web_socket_mapping ADD auth varchar(1) DEFAULT 'N' NOT NULL;

-- 20240430, 添加兩個欄位用於預定DP上下架功能, Kevin Cheng
ALTER TABLE TSMP_API ADD SCHEDULED_LAUNCH_DATE BIGINT DEFAULT 0;
ALTER TABLE TSMP_API ADD SCHEDULED_REMOVAL_DATE BIGINT DEFAULT 0;

-- 20240516, 添加兩個欄位用於預定DGR API啟用停用功能, Kevin Cheng
ALTER TABLE TSMP_API ADD ENABLE_SCHEDULED_DATE BIGINT DEFAULT 0;
ALTER TABLE TSMP_API ADD DISABLE_SCHEDULED_DATE BIGINT DEFAULT 0;

-- 20240603, TSMP_API_IMP API匯入匯出添加四個欄位,兩個預定DP上下架功能, 兩個預定DGR API啟用停用功能, Webber Luo
ALTER TABLE TSMP_API_IMP ADD  SCHEDULED_LAUNCH_DATE BIGINT DEFAULT 0;
ALTER TABLE TSMP_API_IMP ADD  SCHEDULED_REMOVAL_DATE BIGINT DEFAULT 0;
ALTER TABLE TSMP_API_IMP ADD  ENABLE_SCHEDULED_DATE BIGINT DEFAULT 0;
ALTER TABLE TSMP_API_IMP ADD  DISABLE_SCHEDULED_DATE BIGINT DEFAULT 0;

-- 20240625, 這是DP的dp_app TABLE 因為DGR的用戶端匯出入有用到所以移動到這, Tom
---- start DP的dp_app TABLE ----
-- 20230407, v4 入口網(DP)的Application	, Kevin Cheng
CREATE TABLE IF NOT EXISTS dp_app (
	dp_application_id BIGINT NOT NULL,                         -- ID
	application_name VARCHAR(50) NOT NULL,                     -- Application名稱
	application_desc VARCHAR(500),                             -- Application說明
	client_id VARCHAR(40) NOT NULL,                            -- CLIENT_ID
	open_apikey_id BIGINT,                                     -- 
	open_apikey_status VARCHAR(1),                             -- DGRK狀態
	user_name VARCHAR(400) NOT NULL,                           -- 使用者名稱(視IdP類型決定)
	id_token_jwtstr VARCHAR(4000) NOT NULL,                    -- IdP ID Token 的 JWT
	create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,      -- 建立日期
	create_user VARCHAR(1000) DEFAULT 'SYSTEM',                 -- 建立人員
	update_date_time TIMESTAMP,                                -- 更新日期
	update_user VARCHAR(1000),                                  -- 更新人員
	version INT DEFAULT 1,                                     -- 版號
	keyword_search varchar(600) NULL,
	PRIMARY KEY (dp_application_id)
);

-- 20230515, v4 入口網(DP)的Application	, 刪除欄位 , Kevin Cheng
ALTER TABLE dp_app DROP COLUMN open_apikey_status;

-- 20231105, v4 修改 dp_app 欄位型態 , min
ALTER TABLE dp_app ALTER COLUMN application_name TYPE VARCHAR(50);
ALTER TABLE dp_app ALTER COLUMN application_desc TYPE VARCHAR(500);

-- 20231123, v4 入口網(DP) DP APP 新增 ISS 欄位, Kevin Cheng
ALTER TABLE dp_app ADD iss VARCHAR(4000) NOT NULL DEFAULT 'NULL';

-- 20231128, v4 入口網(DP) 改欄位名, Kevin Cheng
ALTER TABLE dp_app RENAME COLUMN user_name TO dp_user_name;

-- 20231228, 移除 open_apikey_id 欄位, jhmin
ALTER TABLE dp_app DROP COLUMN open_apikey_id;
---- end DP的dp_app TABLE ----

-- 20240718 , 第三方 AC IDP INFO , Kevin Cheng
CREATE TABLE IF NOT EXISTS dgr_ac_idp_info_cus (
    ac_idp_info_cus_id     BIGINT          NOT NULL,                -- ID
    ac_idp_info_cus_name   VARCHAR(200),                           -- 第三方可識別名稱  
    cus_status             VARCHAR(1)      NOT NULL DEFAULT 'Y',    -- Cus 狀態
    cus_login_url          VARCHAR(4000)   NOT NULL,                -- 第三方前端頁面 URL
    cus_backend_login_url  VARCHAR(4000)   NOT NULL,                -- 第三方後端 URL
    cus_user_data_url      VARCHAR(4000)   NOT NULL,                -- 第三方使用者資料 URL
    create_date_time       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP, -- 建立日期
    create_user            VARCHAR(1000)   DEFAULT 'SYSTEM',        -- 建立人員
    update_date_time       TIMESTAMP,                                -- 更新日期
    update_user            VARCHAR(1000),                           -- 更新人員
    version                INT             DEFAULT 1,               -- 版號
    CONSTRAINT DGR_AC_IDP_INFO_CUS_PK PRIMARY KEY (AC_IDP_INFO_CUS_ID)
);

-- 20240911 , DGR_GTW_IDP_INFO_A  ADD COLUMN , Zoe Lee
ALTER TABLE DGR_GTW_IDP_INFO_A ADD  IDT_LIGHT_ID VARCHAR(200);
ALTER TABLE DGR_GTW_IDP_INFO_A ADD  IDT_ROLE_NAME VARCHAR(200);
-- 20240911 , DGR_GTW_IDP_AUTH_CODE  ADD COLUMN , Zoe Lee
ALTER TABLE DGR_GTW_IDP_AUTH_CODE ADD  USER_LIGHT_ID VARCHAR(200);
ALTER TABLE DGR_GTW_IDP_AUTH_CODE ADD  USER_ROLE_NAME VARCHAR(200);

-- 20240902 , CUS GATE IDP INFO , Kevin Cheng
CREATE TABLE IF NOT EXISTS dgr_gtw_idp_info_cus (
    gtw_idp_info_cus_id BIGINT        NOT NULL,                           -- ID
    client_id           VARCHAR(40)   NOT NULL,                           -- digiRunner 的 client_id
    status              VARCHAR(1)    NOT NULL DEFAULT 'Y',               -- 狀態
    cus_login_url       VARCHAR(4000) NOT NULL,                           -- CUS 登入 URL
    cus_user_data_url   VARCHAR(4000) NOT NULL,                           -- CUS 使用者資料 URL
    icon_file           VARCHAR(4000),                                    -- 登入頁圖示檔案
    page_title          VARCHAR(400),                                    -- 登入頁標題
    create_date_time    TIMESTAMP              DEFAULT CURRENT_TIMESTAMP, -- 建立日期
    create_user         VARCHAR(1000)          DEFAULT 'SYSTEM',          -- 建立人員
    update_date_time    TIMESTAMP,                                         -- 更新日期
    update_user         VARCHAR(1000),                                   -- 更新人員
    version             INT                    DEFAULT 1,                 -- 版號
    CONSTRAINT GTW_IDP_INFO_CUS_PK PRIMARY KEY (gtw_idp_info_cus_id)
    );

-- 20241007, AC IdP授權碼記錄檔, 增加欄位, Mini Lee
ALTER TABLE dgr_ac_idp_auth_code ADD api_resp VARCHAR(4000);

-- 20241022 , DGR_BOT_DETECTION , Kevin Cheng
CREATE TABLE IF NOT EXISTS dgr_bot_detection
(
    bot_detection_id   BIGINT        NOT NULL,                           -- ID
    bot_detection_rule VARCHAR(4000) NOT NULL,                           -- 規則
    type               VARCHAR(1)    NOT NULL DEFAULT 'W',               -- 名單種類
    create_date_time   TIMESTAMP               DEFAULT CURRENT_TIMESTAMP, -- 建立日期
    create_user        VARCHAR(1000)         DEFAULT 'SYSTEM',          -- 建立人員
    update_date_time   TIMESTAMP,                                         -- 更新日期
    update_user        VARCHAR(1000),                                   -- 更新人員
    version            INT                    DEFAULT 1,                 -- 版號
    CONSTRAINT BOT_DETECTION_PK PRIMARY KEY (bot_detection_id)
    );

-- 20250120 , TSMP Token 歷史紀錄, Mini Lee
ALTER TABLE tsmp_token_history ALTER COLUMN api_resp TYPE TEXT;
-- 20250120 , SSO AC IdP授權碼記錄檔, Mini Lee
ALTER TABLE dgr_ac_idp_auth_code ALTER COLUMN api_resp TYPE TEXT;
-- 20250120 , Gateway IdP授權碼記錄檔, Mini Lee
ALTER TABLE dgr_gtw_idp_auth_code ALTER COLUMN api_resp TYPE TEXT;

-- 20250203, dashboard相關table建立index(若有資料存在可能要執行一段時間), tom
CREATE INDEX idx_tsmp_req_log ON tsmp_req_log(rtime);
CREATE INDEX idx_dgr_dashboard_es_log ON dgr_dashboard_es_log(rtime);
CREATE INDEX idx_tsmp_req_res_log_history ON tsmp_req_res_log_history(rtime);

-- 20250213, 增加欄位長度, Zoe Lee
ALTER TABLE dgr_rdb_connection ALTER COLUMN mima SET DATA TYPE VARCHAR(2000), ALTER COLUMN mima SET NOT NULL;

-- 20250227, DGR_WEBHOOK_NOTIFY , Webber Luo
CREATE TABLE IF NOT EXISTS dgr_webhook_notify
(
    webhook_notify_id BIGINT       NOT NULL,                   -- ID
    notify_name       VARCHAR(100) NOT NULL,                   -- 通知名稱
    notify_type       VARCHAR(100) NOT NULL,                   -- 通知種類
    enable            VARCHAR(1)   NOT NULL,                   -- 啟用=Y / 停用=N
    message           VARCHAR(2000),                           -- 發送內容
    payload_flag      VARCHAR(1)    DEFAULT '0',               --
    create_date_time  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP, -- 建立日期
    create_user       VARCHAR(1000) DEFAULT 'SYSTEM',          -- 建立人員
    update_date_time  TIMESTAMP,                               -- 更新日期
    update_user       VARCHAR(1000),                           -- 更新人員
    version           INTEGER       DEFAULT 1,                 -- 版號
    PRIMARY KEY (webhook_notify_id)
);

-- 20250227, DGR_WEBHOOK_NOTIFY_FIELD , Webber Luo
CREATE TABLE IF NOT EXISTS dgr_webhook_notify_field
(
    webhook_notify_field_id BIGINT        NOT NULL,                           -- ID
    webhook_notify_id       BIGINT        NOT NULL,                           -- 通知ID
    field_key               VARCHAR(100)  NOT NULL,                           -- KEY
    field_value             VARCHAR(2000) NOT NULL,                           -- VALUE
    field_type              VARCHAR(1)    NOT NULL DEFAULT '0',               -- TYPE
    mapping_url             VARCHAR(2000),                                    -- URL
    create_date_time        TIMESTAMP              DEFAULT CURRENT_TIMESTAMP, -- 建立日期
    create_user             VARCHAR(1000)          DEFAULT 'SYSTEM',          -- 建立人員
    update_date_time        TIMESTAMP,                                        -- 更新日期
    update_user             VARCHAR(1000),                                    -- 更新人員
    version                 INTEGER                DEFAULT 1,                 -- 版號
    PRIMARY KEY (webhook_notify_field_id)
);

-- 20250227, DGR_WEBHOOK_NOTIFY_LOG , Webber Luo
CREATE TABLE IF NOT EXISTS DGR_WEBHOOK_NOTIFY_LOG
(
    webhook_notify_log_id BIGINT      NOT NULL,                    -- ID
    webhook_notify_id     BIGINT      NOT NULL,                    -- WEBHOOK_NOTIFY_ID
    client_id             VARCHAR(40) NOT NULL,                    -- 用戶端
    content               VARCHAR(2000),                           -- 發送內容
    remark                VARCHAR(2000),                           -- 備註
    create_date_time      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP, -- 建立日期
    create_user           VARCHAR(1000) DEFAULT 'SYSTEM',          -- 建立人員
    update_date_time      TIMESTAMP,                               -- 更新日期
    update_user           VARCHAR(1000),                           -- 更新人員
    version               INTEGER       DEFAULT 1,                 -- 版號
    PRIMARY KEY (webhook_notify_log_id)
);

-- 20250227, DGR_WEBHOOK_API_MAP , Webber Luo
CREATE TABLE IF NOT EXISTS dgr_webhook_api_map
(
    webhook_api_map_id BIGINT       NOT NULL,                   -- ID
    api_key            VARCHAR(255) NOT NULL,                   -- API代碼
    module_name        VARCHAR(150) NOT NULL,                   -- API_MODULE
    webhook_notify_id  BIGINT       NOT NULL,                   -- WEBHOOK_NOTIFY_I
    create_date_time   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP, -- 建立日期
    create_user        VARCHAR(1000) DEFAULT 'SYSTEM',          -- 建立人員
    update_date_time   TIMESTAMP,                               -- 更新日期
    update_user        VARCHAR(1000),                           -- 更新人員
    version            INTEGER       DEFAULT 1,                 -- 版號
    PRIMARY KEY (webhook_api_map_id)
);

-- 20250331, DGR_GRPCPROXY_MAP, Alvin Chiu
CREATE TABLE IF NOT EXISTS dgr_grpcproxy_map
(
    grpcproxy_map_id          BIGINT       NOT NULL,                           -- ID
    service_name              VARCHAR(255) NOT NULL,                           -- 目標服務名稱
    proxy_hostname            VARCHAR(255) NOT NULL,                           -- 轉發位置
    target_hostname           VARCHAR(255) NOT NULL,                           -- 目標服務位置
    target_port               INTEGER      NOT NULL,                           -- 目標服務PORT
    connect_timeout_ms        INTEGER      NOT NULL DEFAULT 5000,              -- 連線超時毫秒
    send_timeout_ms           INTEGER      NOT NULL DEFAULT 10000,             -- 發送超時毫秒
    read_timeout_ms           INTEGER      NOT NULL DEFAULT 30000,             -- 讀取超時毫秒
    secure_mode               VARCHAR(10)           DEFAULT 'AUTO',            -- TLS安全模式 (AUTO, SECURE, PLAINTEXT)
    server_cert_content       TEXT,                                            -- X509憑證內容 (PEM格式)
    server_key_content        TEXT,                                            -- 私鑰內容 (PEM格式)
    auto_trust_upstream_certs VARCHAR(1)            DEFAULT 'N',               -- 是否自動信任上游憑證
    trusted_certs_content     TEXT,                                            -- 受信任的CA憑證內容 (PEM格式)
    enable                    VARCHAR(1)            DEFAULT 'N',               -- 啟用/停用
    create_date_time          TIMESTAMP             DEFAULT CURRENT_TIMESTAMP, -- 建立日期
    create_user               VARCHAR(1000)         DEFAULT 'SYSTEM',          -- 建立人員
    update_date_time          TIMESTAMP,                                       -- 更新日期
    update_user               VARCHAR(1000),                                   -- 更新人員
    version                   INTEGER               DEFAULT 1,                 -- 版號
    PRIMARY KEY (grpcproxy_map_id)
);

-- 20250812 , DGR_MTLS_CLIENT_CERT , Zoe Lee
CREATE TABLE IF NOT EXISTS dgr_mtls_client_cert (
    dgr_mtls_client_cert_id    BIGINT NOT NULL,                    -- ID
    host                       VARCHAR(255) NOT NULL,              -- 主機
    port                       INT NOT NULL,                       -- 通訊埠
    root_ca                    VARCHAR(4000) NOT NULL,             -- 根憑證
    client_cert                VARCHAR(4000) NOT NULL,             -- 客戶端憑證
    client_key                 VARCHAR(4000) NOT NULL,             -- 客戶端金鑰
    key_mima                   VARCHAR(2000),                      -- 客戶端憑證密碼
    remark                     VARCHAR(200),                       -- 備註
    enable                     VARCHAR(1) NOT NULL,                -- 啟用=Y / 停用=N
    root_ca_expire_date        TIMESTAMP NOT NULL,                 -- ROOT_CA 的到期時間
    crt_expire_date            TIMESTAMP NOT NULL,                 -- CLIENT_CERT 的到期時間
    create_date_time           TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 建立日期
    create_user                VARCHAR(1000) DEFAULT 'SYSTEM',     -- 建立人員
    update_date_time           TIMESTAMP,                          -- 更新日期
    update_user                VARCHAR(1000),                      -- 更新人員
    version                    INT DEFAULT 1,                      -- 版號
    PRIMARY KEY (dgr_mtls_client_cert_id)
);
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
    create_date_time            TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,      -- 建立日期
    create_user                 VARCHAR(1000)   DEFAULT 'SYSTEM',               -- 建立人員
    update_date_time            TIMESTAMP,                                      -- 更新日期
    update_user                 VARCHAR(1000),                                  -- 更新人員
    version                     INT             DEFAULT 1,                      -- 版號
    ai_provider_enable          VARCHAR(1)      NOT NULL DEFAULT 'Y',           -- Y = 啟用, N = 停用
    CONSTRAINT PK_dgr_ai_provider PRIMARY KEY(ai_provider_id)
);

-- 20250812 , 檢查並建立索引 , Vulcan
CREATE INDEX IF NOT EXISTS index_dgr_ai_provider_ai_provider_name_ai_provider ON dgr_ai_provider (ai_provider_name);
CREATE INDEX IF NOT EXISTS index_dgr_ai_provider_ai_provider_name_ai_model ON dgr_ai_provider (ai_model);

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
    create_date_time            TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,  -- 建立日期
    create_user                 VARCHAR(1000)   DEFAULT 'SYSTEM',           -- 建立人員
    update_date_time            TIMESTAMP,                                  -- 更新日期
    update_user                 VARCHAR(1000),                              -- 更新人員
    version                     INT             DEFAULT 1,                  -- 版號
    CONSTRAINT PK_dgr_ai_apikey PRIMARY KEY(ai_apikey_id)
);

-- 20250812 , 檢查並建立索引 , Vulcan
CREATE INDEX IF NOT EXISTS index_dgr_ai_apikey_ai_provider_id ON dgr_ai_apikey (ai_provider_id);

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
    create_date_time            TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,  -- 建立日期
    CONSTRAINT PK_dgr_ai_apikey_usage PRIMARY KEY(ai_apikey_usage_id)
);

-- 20250812 , 檢查並建立索引 , Vulcan
CREATE INDEX IF NOT EXISTS index_dgr_ai_apikey_usage_ai_apikey_id ON dgr_ai_apikey_usage (ai_apikey_id);

-- 20250812 , 檢查並建立 dgr_ai_prompt_template 表 , Vulcan
CREATE TABLE IF NOT EXISTS dgr_ai_prompt_template (
    ai_prompt_template_id       BIGINT          NOT NULL,               -- ID
    ai_prompt_template_name     VARCHAR(1700)   NOT NULL,               -- AI 提示模板名稱
    ai_prompt_template_content  TEXT            NOT NULL,               -- AI 提示模板內容
    ai_prompt_template_enable   VARCHAR(1)      NOT NULL DEFAULT 'Y',   -- Y = 啟用, N = 停用
    ai_prompt_template_remark   VARCHAR(400),                           -- AI 提示模板備註
    CONSTRAINT PK_dgr_ai_prompt_template PRIMARY KEY(ai_prompt_template_id),
    CONSTRAINT UQ_dgr_ai_prompt_template_name UNIQUE (ai_prompt_template_name)
);

-- 20250812 , 檢查並建立 dgr_ai_prompt_template_binding 表 , Vulcan
CREATE TABLE IF NOT EXISTS dgr_ai_prompt_template_binding (
    ai_consumer_prompt_template_binding_id BIGINT NOT NULL,  -- ID
    ai_prompt_template_id                  BIGINT NOT NULL,  -- AI 提示模板 ID
    ai_apikey_consumer_type                VARCHAR(1000),    -- APIKEY 使用者類型 user / client
    ai_apikey_consumer_id                  VARCHAR(1000),    -- APIKEY 使用者 ID
    CONSTRAINT PK_dgr_ai_prompt_template_binding PRIMARY KEY(ai_consumer_prompt_template_binding_id)
);

-- 20250812 , 檢查並建立索引 , Vulcan
CREATE INDEX IF NOT EXISTS index_dgr_ai_prompt_template_binding_ai_prompt_template_id ON dgr_ai_prompt_template_binding (ai_prompt_template_id);

-- 20250819, TSMP外部API註冊資料, 增加欄位 ,Mini Lee
ALTER TABLE TSMP_API_REG ADD COLUMN IS_CORS_ALLOW_ORIGIN VARCHAR(1) DEFAULT 'N' NULL;
ALTER TABLE TSMP_API_REG ADD COLUMN IS_CORS_ALLOW_METHODS VARCHAR(1) DEFAULT 'N' NULL;
ALTER TABLE TSMP_API_REG ADD COLUMN IS_CORS_ALLOW_HEADERS VARCHAR(1) DEFAULT 'N' NULL;
ALTER TABLE TSMP_API_REG ADD COLUMN CORS_ALLOW_ORIGIN VARCHAR(1000) NULL;
ALTER TABLE TSMP_API_REG ADD COLUMN CORS_ALLOW_METHODS VARCHAR(200) NULL;
ALTER TABLE TSMP_API_REG ADD COLUMN CORS_ALLOW_HEADERS VARCHAR(1000) NULL;

-- 20250819, TSMP API 匯入資料, 增加欄位 ,Mini Lee
ALTER TABLE TSMP_API_IMP ADD COLUMN IS_CORS_ALLOW_ORIGIN VARCHAR(1) DEFAULT 'N' NULL;
ALTER TABLE TSMP_API_IMP ADD COLUMN IS_CORS_ALLOW_METHODS VARCHAR(1) DEFAULT 'N' NULL;
ALTER TABLE TSMP_API_IMP ADD COLUMN IS_CORS_ALLOW_HEADERS VARCHAR(1) DEFAULT 'N' NULL;
ALTER TABLE TSMP_API_IMP ADD COLUMN CORS_ALLOW_ORIGIN VARCHAR(1000) NULL;
ALTER TABLE TSMP_API_IMP ADD COLUMN CORS_ALLOW_METHODS VARCHAR(200) NULL;
ALTER TABLE TSMP_API_IMP ADD COLUMN CORS_ALLOW_HEADERS VARCHAR(1000) NULL;

-- 20250825, dgr_otp , Tom
CREATE TABLE IF NOT EXISTS dgr_otp
(
    otp_id       BIGINT       NOT NULL,                   -- ID
    email        VARCHAR(100) NOT NULL,                   -- email
    otp_code     VARCHAR(10)  NOT NULL,                   -- 驗證碼
    expire_key   VARCHAR(400) NOT NULL,                   -- 到期key
    error_limit  INTEGER      NOT NULL DEFAULT 0,         -- 錯誤上限
    CONSTRAINT pk_dgr_otp PRIMARY KEY (otp_id),
    CONSTRAINT uk_dgr_otp UNIQUE (email)
);

-- 20251003, v4, 加大欄位長度, Mini Lee
ALTER TABLE TSMP_TOKEN_HISTORY ALTER COLUMN USER_NAME TYPE VARCHAR(400);

-- 20260304, TSMP API 匯入資料, 增加欄位 ,Tom
ALTER TABLE tsmp_api_imp ADD COLUMN data_format bpchar(1) DEFAULT '1'::bpchar NULL;

-- 20251230, v4, 新增 dgr_smart_on_fhir_proxy, Kevin Cheng
CREATE TABLE IF NOT EXISTS dgr_smart_on_fhir_proxy (
    sof_proxy_id            BIGINT          NOT NULL,                -- SOF PROXY ID
    sof_proxy_name          VARCHAR(100)    NOT NULL,                -- SOF PROXY 名稱
    sof_proxy_status        VARCHAR(1)      NOT NULL DEFAULT 'Y',    -- SOF PROXY 狀態
    sof_proxy_remark        VARCHAR(500),                            -- SOF PROXY 備註
    sof_proxy_access_token  VARCHAR(1)      NOT NULL DEFAULT 'N',    -- 檢查 ACCESS TOKEN
    sof_proxy_sql_injection VARCHAR(1)      NOT NULL DEFAULT 'N',    -- 檢查SQL Injection
    sof_proxy_traffic       VARCHAR(1)      NOT NULL DEFAULT 'N',    -- 檢查Traffic
    sof_proxy_xss           VARCHAR(1)      NOT NULL DEFAULT 'N',    -- 檢查XSS
    sof_proxy_xxe           VARCHAR(1)      NOT NULL DEFAULT 'N',    -- 檢查XXE
    sof_proxy_show_log      VARCHAR(1)      NOT NULL DEFAULT 'N',    -- SOF PROXY 是否印出 Log
    sof_proxy_sticky        VARCHAR(1)      NOT NULL DEFAULT 'Y',    -- SOF PROXY 是否做 backend sticky
    sof_proxy_tps           INTEGER         NOT NULL DEFAULT 0,      -- 每秒最大請求次數
    sof_proxy_ignore_api    VARCHAR(4000),                           -- 略過不檢查的API,多個以逗號分隔(,)
    sof_proxy_client_id     VARCHAR(4000),                           -- 相關連的 Client ID
    create_date_time        TIMESTAMP       DEFAULT CURRENT_TIMESTAMP, -- 建立日期
    create_user             VARCHAR(1000)   DEFAULT 'SYSTEM',        -- 建立人員
    update_date_time        TIMESTAMP,                               -- 更新日期
    update_user             VARCHAR(1000),                           -- 更新人員
    version                 INTEGER         DEFAULT 1,               -- 版號
    CONSTRAINT pk_dgr_smart_on_fhir_proxy PRIMARY KEY (sof_proxy_id)
);

-- 20260205, v4, 新增 sof_proxy_diversion_resource_types 欄位用於手動指定 Resource Types , Kevin Cheng
CREATE TABLE IF NOT EXISTS dgr_smart_on_fhir_proxy_diversion (
    sof_proxy_id                        BIGINT          NOT NULL,                -- SOF_PROXY_ID
    sof_proxy_diversion_id              BIGINT          NOT NULL,                -- SOF_PROXY_DIVERSION_ID
    sof_proxy_diversion_probability     INTEGER         NOT NULL,                -- 機率
    sof_proxy_diversion_url             VARCHAR(1000)   NOT NULL,                -- 目標URL
    create_date_time                    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP, -- 建立日期
    create_user                         VARCHAR(1000)   DEFAULT 'SYSTEM',        -- 建立人員
    update_date_time                    TIMESTAMP,                               -- 更新日期
    update_user                         VARCHAR(1000),                           -- 更新人員
    version                             INTEGER         DEFAULT 1,               -- 版號
    CONSTRAINT pk_dgr_smart_on_fhir_proxy_diversion PRIMARY KEY (sof_proxy_diversion_id)
);

-- 20260123 v4, 新增 dgr_smart_on_fhir_proxy_sticky, Kevin Cheng
CREATE TABLE IF NOT EXISTS dgr_smart_on_fhir_proxy_sticky (
    sof_proxy_sticky_id        BIGINT          NOT NULL,                -- 主鍵
    sof_proxy_id               BIGINT          NOT NULL,                -- SOF Proxy ID
    sof_proxy_diversion_id     BIGINT          NOT NULL,                -- SOF Proxy Diversion ID
    sof_proxy_sticky_interaction VARCHAR(200),                          -- 互動類型
    sof_proxy_sticky_type      VARCHAR(200),                            -- FHIR resource name
    sof_proxy_sticky_type_id   VARCHAR(200),                            -- FHIR resource id
    sof_proxy_sticky_verb      VARCHAR(50),                             -- HTTP method
    sof_proxy_sticky_path      VARCHAR(2000),                           -- HTTP URL
    sof_proxy_sticky_hashcode  VARCHAR(500),                            -- websitename + resource
    create_date_time           TIMESTAMP       DEFAULT CURRENT_TIMESTAMP, -- 建立日期
    create_user                VARCHAR(1000)   DEFAULT 'SYSTEM',        -- 建立人員
    update_date_time           TIMESTAMP,                               -- 更新日期
    update_user                VARCHAR(1000),                           -- 更新人員
    version                    INTEGER         DEFAULT 1,               -- 版號
    CONSTRAINT pk_dgr_smart_on_fhir_proxy_sticky PRIMARY KEY (sof_proxy_sticky_id)
);

-- 20260213, v4, 每個 diversion 各自指定 FHIR API 根路徑, Kevin Cheng
ALTER TABLE dgr_smart_on_fhir_proxy_diversion ADD COLUMN IF NOT EXISTS sof_proxy_diversion_fhir_base_path VARCHAR(500);

-- 20260302, v4, 新增 URL 改寫開關（預設 Y，啟用時支援 X-Forwarded-* header）, Kevin Cheng
ALTER TABLE dgr_smart_on_fhir_proxy ADD COLUMN IF NOT EXISTS sof_proxy_url_rewrite VARCHAR(1) NOT NULL DEFAULT 'Y';

-- 20260107, dgr_db_sync_history Zoe
CREATE TABLE IF NOT EXISTS dgr_h2_config_sync_history (
    sync_id                             BIGINT          NOT NULL,                -- ID
    sync_type                           VARCHAR(20)     NOT NULL,                -- 同步類型
    schedule_id                         BIGINT,                                  -- 排程ID
    source_id                           VARCHAR(100)    NOT NULL,                -- 來源ID
    target_ids                          VARCHAR(1000)   NOT NULL,                -- 目標ID
    status                              VARCHAR(20)     NOT NULL,                -- 狀態
    current_step                        VARCHAR(100)    NOT NULL,                -- 目前步驟
    progress                            INTEGER         NOT NULL DEFAULT 0,      -- 進度
    error_message                       VARCHAR(2000),                           -- 錯誤訊息
    start_time                          TIMESTAMP       NOT NULL,                -- 開始時間
    end_time                            TIMESTAMP,                               -- 結束時間
    duration                            BIGINT,                                  -- 用時
    create_date_time                    TIMESTAMP,                               -- 建立日期
    create_user                         VARCHAR(1000)   DEFAULT 'SYSTEM',        -- 建立人員
    update_date_time                    TIMESTAMP,                               -- 更新日期
    CONSTRAINT pk_dgr_h2_config_sync_history PRIMARY KEY (sync_id)
);

-- 20260326, v4, 新增 SMART App Launch client 設定表, Kevin Cheng
CREATE TABLE IF NOT EXISTS dgr_smart_client (
    smart_client_id             BIGINT          NOT NULL,                    -- 主鍵
    client_id                   VARCHAR(200)    NOT NULL,                    -- OAuth client ID（FK → tsmp_client.client_id）
    client_type                 VARCHAR(50)     NOT NULL,                    -- 認證方式：public / confidential-symmetric / confidential-asymmetric
    idp_type                    VARCHAR(50)     NOT NULL,                    -- 身份驗證方式：GOOGLE / MS / OIDC / LDAP / JDBC / API / CUS
    idp_client_id               VARCHAR(200)    NOT NULL,                    -- ssotoken 第二段驗證用的 client ID（FK → tsmp_client.client_id）
    allowed_scopes              VARCHAR(4000)   NOT NULL,                    -- 該 client 被授權的 SMART scope，空格分隔
    redirect_uris               VARCHAR(4000),                               -- SMART 專用 redirect URI，多個以換行分隔
    launch_mode                 VARCHAR(20),                                 -- standalone / ehr / both
    auto_approve                VARCHAR(1)      NOT NULL DEFAULT 'N',        -- 是否跳過 consent（Y/N）
    jwks_uri                    VARCHAR(1000),                               -- Client 的 JWK Set URL（非對稱認證，動態抓取公鑰）
    jwks                        TEXT,                                        -- Client 的 JWK Set JSON（非對稱認證，直接存放）
    token_endpoint_auth_method  VARCHAR(50),                                 -- client_secret_basic / client_secret_post / private_key_jwt
    create_date_time            TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,   -- 建立日期
    create_user                 VARCHAR(1000)   DEFAULT 'SYSTEM',            -- 建立人員
    update_date_time            TIMESTAMP,                                   -- 更新日期
    update_user                 VARCHAR(1000),                               -- 更新人員
    version                     INTEGER         DEFAULT 1,                   -- 版號
    CONSTRAINT pk_dgr_smart_client PRIMARY KEY (smart_client_id),
    CONSTRAINT uk_dgr_smart_client_cid UNIQUE (client_id)
);

-- 20260326, v4, 新增 SMART App Launch 授權流程狀態表, Kevin Cheng
CREATE TABLE IF NOT EXISTS dgr_smart_auth_session (
    session_id                  BIGINT          NOT NULL,                    -- 主鍵
    state                       VARCHAR(500)    NOT NULL,                    -- OAuth state，串接整個授權流程
    client_id                   VARCHAR(200)    NOT NULL,                    -- 發起授權的 client ID
    aud                         VARCHAR(1000)   NOT NULL,                    -- 目標 FHIR Server URL
    website_name                VARCHAR(100)    NOT NULL,                    -- 從 aud 解析出的 proxy 名稱
    fhir_base_path              VARCHAR(500)    NOT NULL,                    -- 從 aud 解析出的 FHIR base path
    requested_scope             VARCHAR(4000)   NOT NULL,                    -- 請求的 scope，空格分隔
    granted_scope               VARCHAR(4000),                               -- consent 後授予的 scope，空格分隔
    redirect_uri                VARCHAR(1000)   NOT NULL,                    -- 本次請求的 redirect URI
    code_challenge              VARCHAR(500),                                -- PKCE challenge 值
    code_challenge_method       VARCHAR(10),                                 -- PKCE 方法（S256）
    launch                      VARCHAR(500),                                -- EHR Launch 的 launch context token
    patient_id                  VARCHAR(200),                                -- 患者上下文（授予 patient/ scope 時必填）
    encounter_id                VARCHAR(200),                                -- 就醫上下文（授予 launch/encounter 時）
    auth_code                   VARCHAR(200),                                -- 簽發的授權碼
    auth_code_expire            BIGINT,                                      -- 授權碼到期時間（epoch millis）
    phase                       VARCHAR(20)     NOT NULL DEFAULT 'PENDING',  -- 流程階段：PENDING / AUTHENTICATED / APPROVED / EXCHANGED
    create_date_time            TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,   -- 建立日期
    CONSTRAINT pk_dgr_smart_auth_session PRIMARY KEY (session_id),
    CONSTRAINT uk_dgr_smart_auth_session_state UNIQUE (state)
);

-- 20260429, 新增使用者身份欄位與 refresh token 欄位, Kevin Cheng
ALTER TABLE dgr_smart_auth_session ADD COLUMN IF NOT EXISTS user_name VARCHAR(200);
ALTER TABLE dgr_smart_auth_session ADD COLUMN IF NOT EXISTS user_email VARCHAR(500);
ALTER TABLE dgr_smart_auth_session ADD COLUMN IF NOT EXISTS refresh_token VARCHAR(200);
ALTER TABLE dgr_smart_auth_session ADD COLUMN IF NOT EXISTS refresh_token_expire BIGINT;
ALTER TABLE dgr_smart_auth_session ADD COLUMN IF NOT EXISTS version INTEGER DEFAULT 1;
CREATE INDEX IF NOT EXISTS idx_smart_session_refresh_token ON dgr_smart_auth_session(refresh_token);
ALTER TABLE dgr_smart_auth_session ADD COLUMN IF NOT EXISTS user_alias VARCHAR(500);
ALTER TABLE dgr_smart_auth_session ADD COLUMN IF NOT EXISTS sim_error VARCHAR(100);

-- 20260507, 新增 dgr_smart_launch_context, Kevin Cheng
CREATE TABLE IF NOT EXISTS dgr_smart_launch_context (
    launch_token                VARCHAR(100)    NOT NULL,
    client_id                   VARCHAR(200)    NOT NULL,
    patient_id                  VARCHAR(200),
    encounter_id                VARCHAR(200),
    consumed                    BOOLEAN         NOT NULL DEFAULT FALSE,
    expire_at                   BIGINT          NOT NULL,
    create_date_time            TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_dgr_smart_launch_context PRIMARY KEY (launch_token)
);

-- 20260512, 新增 provider/fhirUser 欄位（fhirUser scope 支援）, Kevin Cheng
ALTER TABLE dgr_smart_launch_context ADD COLUMN IF NOT EXISTS provider_ids VARCHAR(2000);
ALTER TABLE dgr_smart_auth_session ADD COLUMN IF NOT EXISTS provider_candidates VARCHAR(2000);
ALTER TABLE dgr_smart_auth_session ADD COLUMN IF NOT EXISTS fhir_user VARCHAR(500);

-- 20260512, Patient 多選對齊 Provider, Kevin Cheng
ALTER TABLE dgr_smart_launch_context ADD COLUMN IF NOT EXISTS patient_ids VARCHAR(2000);
ALTER TABLE dgr_smart_auth_session ADD COLUMN IF NOT EXISTS patient_candidates VARCHAR(2000);

-- 20260512, fhirUser 類型欄位（區分 Provider EHR vs Patient Portal Launch）, Kevin Cheng
ALTER TABLE dgr_smart_launch_context ADD COLUMN IF NOT EXISTS fhir_user_type VARCHAR(20);
ALTER TABLE dgr_smart_auth_session ADD COLUMN IF NOT EXISTS fhir_user_type VARCHAR(20);

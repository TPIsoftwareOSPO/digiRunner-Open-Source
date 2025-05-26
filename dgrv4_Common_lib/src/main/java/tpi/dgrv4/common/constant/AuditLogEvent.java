package tpi.dgrv4.common.constant;

public enum AuditLogEvent {
	LOGIN("login"), 			// 登入
	LOGOUT("logout"), 			// 登出
	FORCE_LOGOUT("forceLogout"),// 強制登出
	ADD_USER("addUser"), 		// 新增使用者
	DELETE_USER("deleteUser"), 	// 刪除使用者
	UPDATE_USER("updateUser"), 	// 更新使用者
	ADD_ROLE("addRole"), 		// 新增角色
	DELETE_ROLE("deleteRole"), 	// 刪除角色
	UPDATE_ROLE("updateRole"), 	// 更新角色
	UPDATE_USER_PROFILE("updateUserProfile"), 	// 更新使用者個人資料
	ADD_TSMP_SETTING("addTsmpSetting"), 		// 新增TSMP SETTING
	DELETE_TSMP_SETTING("deleteTsmpSetting"), 	// 刪除TSMP SETTING
	UPDATE_TSMP_SETTING("updateTsmpSetting"), 	// 更新TSMP SETTING
	ADD_GROUP("addGroup"),      // 新增群組
	DELETE_GROUP("deleteGroup"), 	// 刪除群組
	UPDATE_GROUP("updateGroup"), 	// 更新群組
	ADD_CLIENT("addClient"), 	   // 新增用戶端
	UPDATE_CLIENT("updateClient"),// 更新用戶端
	DELETE_CLIENT("deleteClient"),// 刪除用戶端
	ADD_REGISTER_API("addRegisterApi"), 		// 新增Register Api
	DELETE_REGISTER_API("deleteRegisterApi"), 	// 刪除Register Api
	UPDATE_REGISTER_API("updateRegisterApi"), 	// 更新Register Api
	ADD_COMPOSER_API("addComposerApi"), 		// 新增Composer Api
	DELETE_COMPOSER_API("deleteComposerApi"), 	// 刪除Composer Api
	UPDATE_COMPOSER_API("updateComposerApi"), 	// 更新Composer Api
	ADD_IDP_USER("addIdPUser"),
	UPDATE_IDP_USER("updateIdPUser"),
	DELETE_IDP_USER("deleteIdPUser"),
	ENABLE_APP_CLIENT("enableAppClient"),   // 啟用 Application Client
	DISABLE_APP_CLIENT("disableAppClient"), // 停用 Application Client
	LOCK_APP_CLIENT("lockAppClient"),       // 鎖定 Application Client
	LIST_API("listApi"),           // 上架 API
	DELIST_API("delistApi"),       // 下架 API
	;

	private String value;

	private AuditLogEvent(String value) {
		this.value = value;
	}

	public String code() {
		return this.name();
	}

	public String value() {
		return this.value;
	}
}

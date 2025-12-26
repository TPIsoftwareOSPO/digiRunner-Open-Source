package tpi.dgrv4.gateway.util;

import java.util.Date;

import tpi.dgrv4.entity.entity.DgrAuditLogD;

public class DgrAuditLogDBuilder {

	private String txnUid;
	private String entityName;
	private String cud;
	private byte[] oldRow;
	private byte[] newRow;
	private String param1;
	private String param2;
	private String param3;
	private String param4;
	private String param5;
	private String stackTrace;
	private Date createDateTime;
	private String createUser;
	private Date updateDateTime;
	private String updateUser;

	public DgrAuditLogDBuilder setTxnUid(String txnUid) {
		this.txnUid = txnUid;
		return this;
	}

	public DgrAuditLogDBuilder setEntityName(String entityName) {
		this.entityName = entityName;
		return this;
	}

	public DgrAuditLogDBuilder setCud(String cud) {
		this.cud = cud;
		return this;
	}

	public DgrAuditLogDBuilder setOldRow(byte[] oldRow) {
		this.oldRow = oldRow;
		return this;
	}

	public DgrAuditLogDBuilder setNewRow(byte[] newRow) {
		this.newRow = newRow;
		return this;
	}

	public DgrAuditLogDBuilder setParam1(String param1) {
		this.param1 = param1;
		return this;
	}

	public DgrAuditLogDBuilder setParam2(String param2) {
		this.param2 = param2;
		return this;
	}

	public DgrAuditLogDBuilder setParam3(String param3) {
		this.param3 = param3;
		return this;
	}

	public DgrAuditLogDBuilder setParam4(String param4) {
		this.param4 = param4;
		return this;
	}

	public DgrAuditLogDBuilder setParam5(String param5) {
		this.param5 = param5;
		return this;
	}

	public DgrAuditLogDBuilder setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
		return this;
	}

	public DgrAuditLogDBuilder setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
		return this;
	}

	public DgrAuditLogDBuilder setCreateUser(String createUser) {
		this.createUser = createUser;
		return this;
	}

	public DgrAuditLogDBuilder setUpdateDateTime(Date updateDateTime) {
		this.updateDateTime = updateDateTime;
		return this;
	}

	public DgrAuditLogDBuilder setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
		return this;
	}

	public DgrAuditLogD build() {
		DgrAuditLogD entity = new DgrAuditLogD();

		entity.setTxnUid(txnUid);
		entity.setEntityName(entityName);
		entity.setCud(cud);
		entity.setOldRow(oldRow);
		entity.setNewRow(newRow);
		entity.setParam1(param1);
		entity.setParam2(param2);
		entity.setParam3(param3);
		entity.setParam4(param4);
		entity.setParam5(param5);
		entity.setStackTrace(stackTrace);

		if (null != createDateTime) {
			entity.setCreateDateTime(createDateTime);
		}

		if (null != createUser) {
			entity.setCreateUser(createUser);
		}

		if (null != updateDateTime) {
			entity.setUpdateDateTime(updateDateTime);
		}

		if (null != updateUser) {
			entity.setUpdateUser(updateUser);
		}

		return entity;
	}
}

package tpi.dgrv4.entity.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import tpi.dgrv4.entity.component.fuzzy.Fuzzy;
import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;

@Entity
@Table(name = "tsmp_role_txid_map")
@EntityListeners(FuzzyEntityListener.class)
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpRoleTxidMap extends BasicFields implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "role_txid_map_id")
	private Long roleTxidMapId;

	@Fuzzy
	@Column(name = "role_id")
	private String roleId;

	@Fuzzy
	@Column(name = "txid")
	private String txid;

	@Column(name = "list_type")
	private String listType;

	/* constructors */

	public TsmpRoleTxidMap() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpRoleTxidMap [roleTxidMapId=" + roleTxidMapId + ", roleId=" + roleId + ", txid=" + txid
				+ ", listType=" + listType + ", getCreateDateTime()=" + getCreateDateTime() + ", getCreateUser()="
				+ getCreateUser() + ", getUpdateDateTime()=" + getUpdateDateTime() + ", getUpdateUser()="
				+ getUpdateUser() + ", getVersion()=" + getVersion() + ", getKeywordSearch()=" + getKeywordSearch()
				+ "]";
	}

	/* getters and setters */

	public Long getRoleTxidMapId() {
		return roleTxidMapId;
	}

	public void setRoleTxidMapId(Long roleTxidMapId) {
		this.roleTxidMapId = roleTxidMapId;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getTxid() {
		return txid;
	}

	public void setTxid(String txid) {
		this.txid = txid;
	}

	public String getListType() {
		return listType;
	}

	public void setListType(String listType) {
		this.listType = listType;
	}

}

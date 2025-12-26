package tpi.dgrv4.dpaa.vo;

import java.io.Serializable;
import java.util.List;

import tpi.dgrv4.entity.entity.DgrGtwIdpInfoA;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoJdbc;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoL;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoO;
import tpi.dgrv4.entity.entity.DgrRdbConnection;
import tpi.dgrv4.entity.entity.DgrXApiKey;
import tpi.dgrv4.entity.entity.DgrXApiKeyMap;
import tpi.dgrv4.entity.entity.DpApp;
import tpi.dgrv4.entity.entity.OauthClientDetails;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpClientGroup;
import tpi.dgrv4.entity.entity.TsmpClientVgroup;
import tpi.dgrv4.entity.entity.TsmpDpClientext;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.entity.TsmpGroupApi;
import tpi.dgrv4.entity.entity.TsmpGroupAuthorities;
import tpi.dgrv4.entity.entity.TsmpGroupAuthoritiesMap;
import tpi.dgrv4.entity.entity.TsmpOpenApiKey;
import tpi.dgrv4.entity.entity.TsmpOpenApiKeyMap;
import tpi.dgrv4.entity.entity.TsmpVgroup;
import tpi.dgrv4.entity.entity.TsmpVgroupAuthoritiesMap;
import tpi.dgrv4.entity.entity.TsmpVgroupGroup;
import tpi.dgrv4.entity.entity.jpql.TsmpClientHost;
import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;

public class AA1120ImportClientRelated implements Serializable{
	private List<TsmpClient> tsmpClientList;
	private List<TsmpClientHost> tsmpClientHostList;
	private List<OauthClientDetails> oauthClientDetailsList;
	private List<TsmpDpClientext> tsmpDpClientextList;
	private List<TsmpGroup> tsmpGroupList;
	private List<TsmpClientGroup> tsmpClientGroupList;
	private List<TsmpVgroup> tsmpVgroupList;
	private List<TsmpClientVgroup> tsmpClientVgroupList;
	private List<TsmpVgroupGroup> tsmpVgroupGroupList;
	private List<TsmpGroupApi> tsmpGroupApiList;
	private List<DgrXApiKey> dgrXApiKeyList;
	private List<DgrXApiKeyMap> dgrXApiKeyMapList;
	private List<TsmpGroupAuthorities> tsmpGroupAuthoritiesList;
	private List<TsmpGroupAuthoritiesMap> tsmpGroupAuthoritiesMapList;
	private List<TsmpVgroupAuthoritiesMap> tsmpVgroupAuthoritiesMapList;
	private List<TsmpSecurityLevel> tsmpSecurityLevelList;
	private List<DpApp> dpAppList;
	private List<TsmpOpenApiKey> tsmpOpenApiKeyList;
	private List<TsmpOpenApiKeyMap> tsmpOpenApiKeyMapList;
	private List<DgrGtwIdpInfoO> dgrGtwIdpInfoOList;
	private List<DgrGtwIdpInfoL> dgrGtwIdpInfoLList;
	private List<DgrGtwIdpInfoA> dgrGtwIdpInfoAList;
	private List<DgrGtwIdpInfoJdbc> dgrGtwIdpInfoJdbcList;
	private List<AA1120DgrApikeyUseApi> dgrApikeyUseApiList;
	private List<DgrRdbConnection> dgrRdbConnectionList ;
	
	public List<TsmpClient> getTsmpClientList() {
		return tsmpClientList;
	}
	public void setTsmpClientList(List<TsmpClient> tsmpClientList) {
		this.tsmpClientList = tsmpClientList;
	}
	public List<TsmpClientHost> getTsmpClientHostList() {
		return tsmpClientHostList;
	}
	public void setTsmpClientHostList(List<TsmpClientHost> tsmpClientHostList) {
		this.tsmpClientHostList = tsmpClientHostList;
	}
	public List<OauthClientDetails> getOauthClientDetailsList() {
		return oauthClientDetailsList;
	}
	public void setOauthClientDetailsList(List<OauthClientDetails> oauthClientDetailsList) {
		this.oauthClientDetailsList = oauthClientDetailsList;
	}
	public List<TsmpDpClientext> getTsmpDpClientextList() {
		return tsmpDpClientextList;
	}
	public void setTsmpDpClientextList(List<TsmpDpClientext> tsmpDpClientextList) {
		this.tsmpDpClientextList = tsmpDpClientextList;
	}
	public List<TsmpGroup> getTsmpGroupList() {
		return tsmpGroupList;
	}
	public void setTsmpGroupList(List<TsmpGroup> tsmpGroupList) {
		this.tsmpGroupList = tsmpGroupList;
	}
	public List<TsmpClientGroup> getTsmpClientGroupList() {
		return tsmpClientGroupList;
	}
	public void setTsmpClientGroupList(List<TsmpClientGroup> tsmpClientGroupList) {
		this.tsmpClientGroupList = tsmpClientGroupList;
	}
	public List<TsmpVgroup> getTsmpVgroupList() {
		return tsmpVgroupList;
	}
	public void setTsmpVgroupList(List<TsmpVgroup> tsmpVgroupList) {
		this.tsmpVgroupList = tsmpVgroupList;
	}
	public List<TsmpClientVgroup> getTsmpClientVgroupList() {
		return tsmpClientVgroupList;
	}
	public void setTsmpClientVgroupList(List<TsmpClientVgroup> tsmpClientVgroupList) {
		this.tsmpClientVgroupList = tsmpClientVgroupList;
	}
	public List<TsmpVgroupGroup> getTsmpVgroupGroupList() {
		return tsmpVgroupGroupList;
	}
	public void setTsmpVgroupGroupList(List<TsmpVgroupGroup> tsmpVgroupGroupList) {
		this.tsmpVgroupGroupList = tsmpVgroupGroupList;
	}
	public List<TsmpGroupApi> getTsmpGroupApiList() {
		return tsmpGroupApiList;
	}
	public void setTsmpGroupApiList(List<TsmpGroupApi> tsmpGroupApiList) {
		this.tsmpGroupApiList = tsmpGroupApiList;
	}
	public List<DgrXApiKey> getDgrXApiKeyList() {
		return dgrXApiKeyList;
	}
	public void setDgrXApiKeyList(List<DgrXApiKey> dgrXApiKeyList) {
		this.dgrXApiKeyList = dgrXApiKeyList;
	}
	public List<DgrXApiKeyMap> getDgrXApiKeyMapList() {
		return dgrXApiKeyMapList;
	}
	public void setDgrXApiKeyMapList(List<DgrXApiKeyMap> dgrXApiKeyMapList) {
		this.dgrXApiKeyMapList = dgrXApiKeyMapList;
	}
	public List<TsmpGroupAuthorities> getTsmpGroupAuthoritiesList() {
		return tsmpGroupAuthoritiesList;
	}
	public void setTsmpGroupAuthoritiesList(List<TsmpGroupAuthorities> tsmpGroupAuthoritiesList) {
		this.tsmpGroupAuthoritiesList = tsmpGroupAuthoritiesList;
	}
	public List<TsmpGroupAuthoritiesMap> getTsmpGroupAuthoritiesMapList() {
		return tsmpGroupAuthoritiesMapList;
	}
	public void setTsmpGroupAuthoritiesMapList(List<TsmpGroupAuthoritiesMap> tsmpGroupAuthoritiesMapList) {
		this.tsmpGroupAuthoritiesMapList = tsmpGroupAuthoritiesMapList;
	}
	public List<TsmpVgroupAuthoritiesMap> getTsmpVgroupAuthoritiesMapList() {
		return tsmpVgroupAuthoritiesMapList;
	}
	public void setTsmpVgroupAuthoritiesMapList(List<TsmpVgroupAuthoritiesMap> tsmpVgroupAuthoritiesMapList) {
		this.tsmpVgroupAuthoritiesMapList = tsmpVgroupAuthoritiesMapList;
	}
	public List<TsmpSecurityLevel> getTsmpSecurityLevelList() {
		return tsmpSecurityLevelList;
	}
	public void setTsmpSecurityLevelList(List<TsmpSecurityLevel> tsmpSecurityLevelList) {
		this.tsmpSecurityLevelList = tsmpSecurityLevelList;
	}
	public List<DpApp> getDpAppList() {
		return dpAppList;
	}
	public void setDpAppList(List<DpApp> dpAppList) {
		this.dpAppList = dpAppList;
	}
	public List<TsmpOpenApiKey> getTsmpOpenApiKeyList() {
		return tsmpOpenApiKeyList;
	}
	public void setTsmpOpenApiKeyList(List<TsmpOpenApiKey> tsmpOpenApiKeyList) {
		this.tsmpOpenApiKeyList = tsmpOpenApiKeyList;
	}
	public List<TsmpOpenApiKeyMap> getTsmpOpenApiKeyMapList() {
		return tsmpOpenApiKeyMapList;
	}
	public void setTsmpOpenApiKeyMapList(List<TsmpOpenApiKeyMap> tsmpOpenApiKeyMapList) {
		this.tsmpOpenApiKeyMapList = tsmpOpenApiKeyMapList;
	}
	public List<DgrGtwIdpInfoO> getDgrGtwIdpInfoOList() {
		return dgrGtwIdpInfoOList;
	}
	public void setDgrGtwIdpInfoOList(List<DgrGtwIdpInfoO> dgrGtwIdpInfoOList) {
		this.dgrGtwIdpInfoOList = dgrGtwIdpInfoOList;
	}
	public List<DgrGtwIdpInfoL> getDgrGtwIdpInfoLList() {
		return dgrGtwIdpInfoLList;
	}
	public void setDgrGtwIdpInfoLList(List<DgrGtwIdpInfoL> dgrGtwIdpInfoLList) {
		this.dgrGtwIdpInfoLList = dgrGtwIdpInfoLList;
	}
	public List<DgrGtwIdpInfoA> getDgrGtwIdpInfoAList() {
		return dgrGtwIdpInfoAList;
	}
	public void setDgrGtwIdpInfoAList(List<DgrGtwIdpInfoA> dgrGtwIdpInfoAList) {
		this.dgrGtwIdpInfoAList = dgrGtwIdpInfoAList;
	}
	public List<DgrGtwIdpInfoJdbc> getDgrGtwIdpInfoJdbcList() {
		return dgrGtwIdpInfoJdbcList;
	}
	public void setDgrGtwIdpInfoJdbcList(List<DgrGtwIdpInfoJdbc> dgrGtwIdpInfoJdbcList) {
		this.dgrGtwIdpInfoJdbcList = dgrGtwIdpInfoJdbcList;
	}
	public List<AA1120DgrApikeyUseApi> getDgrApikeyUseApiList() {
		return dgrApikeyUseApiList;
	}
	public void setDgrApikeyUseApiList(List<AA1120DgrApikeyUseApi> dgrApikeyUseApiList) {
		this.dgrApikeyUseApiList = dgrApikeyUseApiList;
	}
	public List<DgrRdbConnection> getDgrRdbConnectionList() {
		return dgrRdbConnectionList;
	}
	public void setDgrRdbConnectionList(List<DgrRdbConnection> dgrRdbConnectionList) {
		this.dgrRdbConnectionList = dgrRdbConnectionList;
	}
	

	
}

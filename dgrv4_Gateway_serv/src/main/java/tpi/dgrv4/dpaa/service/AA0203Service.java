package tpi.dgrv4.dpaa.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.codec.utils.TimeZoneUtil;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0203GroupInfo;
import tpi.dgrv4.dpaa.vo.AA0203Host;
import tpi.dgrv4.dpaa.vo.AA0203Req;
import tpi.dgrv4.dpaa.vo.AA0203Resp;
import tpi.dgrv4.dpaa.vo.AA0203SecurityLV;
import tpi.dgrv4.dpaa.vo.AA0203VgroupInfo;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpClientGroup;
import tpi.dgrv4.entity.entity.TsmpClientVgroup;
import tpi.dgrv4.entity.entity.TsmpDpClientext;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.entity.TsmpVgroup;
import tpi.dgrv4.entity.entity.jpql.TsmpClientHost;
import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpClientGroupDao;
import tpi.dgrv4.entity.repository.TsmpClientHostDao;
import tpi.dgrv4.entity.repository.TsmpClientVgroupDao;
import tpi.dgrv4.entity.repository.TsmpDpClientextDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.entity.repository.TsmpSecurityLevelDao;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.entity.repository.TsmpVgroupDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0203Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpClientDao tsmpClientDao;
	private TsmpSettingDao tsmpSettingDao;
	private TsmpDpClientextDao tsmpDpClientextDao;
	private TsmpClientHostDao tsmpClientHostDao;
	private TsmpClientGroupDao tsmpClientGroupDao;
	private TsmpClientVgroupDao tsmpClientVgroupDao;
	private TsmpGroupDao tsmpGroupDao;
	private TsmpVgroupDao tsmpVgroupDao;
	private TsmpSecurityLevelDao securityLVDao;
	
	@Autowired
	public AA0203Service(TsmpClientDao tsmpClientDao, TsmpSettingDao tsmpSettingDao,
			TsmpDpClientextDao tsmpDpClientextDao, TsmpClientHostDao tsmpClientHostDao,
			TsmpClientGroupDao tsmpClientGroupDao, TsmpClientVgroupDao tsmpClientVgroupDao, TsmpGroupDao tsmpGroupDao,
			TsmpVgroupDao tsmpVgroupDao, TsmpSecurityLevelDao securityLVDao) {
		super();
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpSettingDao = tsmpSettingDao;
		this.tsmpDpClientextDao = tsmpDpClientextDao;
		this.tsmpClientHostDao = tsmpClientHostDao;
		this.tsmpClientGroupDao = tsmpClientGroupDao;
		this.tsmpClientVgroupDao = tsmpClientVgroupDao;
		this.tsmpGroupDao = tsmpGroupDao;
		this.tsmpVgroupDao = tsmpVgroupDao;
		this.securityLVDao = securityLVDao;
	}

	/**
	 * 1.查詢TSMP_CLIENT資料表，條件TSMP_CLIENT.CLIENT_ID = AA0203Req.clientID AND TSMP_CLIENT.CLIENT_NAME = AA0203Req.clientName ，若沒有查詢到資料則throw RTN Code 1344。
	 * 2.將步驟1查詢到資料轉換成AA0203Resp。
     * 3.查詢TSMP_CLIENT_HOST資料表，條件TSMP_CLIENT_HOST.CLIENT_ID = AA0203Req.clientID，將查詢出來的資料轉換成AA0203Resp.hostList。
     * 4.查詢TSMP_CLIENT_GROUP資料表，條件TSMP_CLIENT_GROUP.CLIENT_ID，若有查到資料則進行步驟5。
     * 5.查詢TSMP_GROUP資料表，條件TSMP_GROUP.GROUP_ID=TSMP_CLIENT_GROUP.GROUP_ID AND VGROUP_FLAG = "0"。將查詢出來的資料轉換成AA0203Resp.groupInfoList。
     * 6.查詢TSMP_CLIENT_VGROUP資料表，條件TSMP_CLIENT_VGROUP.CLIENT_ID =TSMP_CLIENT_GROUP.CLIENT_ID ，若有查到資料則進行步驟7。
     * 7.查詢TSMP_VGROUP資料表，條件TSMP_VGROUP.VGROUP_ID = TSMP_CLIENT_VGROUP.VGROUP_ID ，將查詢出來的資料轉換成AA0203Resp.vgroupInfoList。
	 * */
	public AA0203Resp queryClientDetail(TsmpAuthorization authorization, AA0203Req req) {
		AA0203Resp resp = null;

		try {
			String clientId = req.getClientID();
			String clientName = req.getClientName();
			
			checkParam(req);
			
			//1.查詢TSMP_CLIENT資料表，條件TSMP_CLIENT.CLIENT_ID = AA0203Req.clientID AND TSMP_CLIENT.CLIENT_NAME = AA0203Req.clientName ，若沒有查詢到資料則throw RTN Code 1344。
			TsmpClient tsmpClient = getTsmpClientDao().findFirstByClientIdAndClientName(clientId, clientName);
			if(tsmpClient == null) {
				//用戶端不存在
				throw TsmpDpAaRtnCode._1344.throwing();
			}
			
			//2.將步驟1查詢到資料轉換成AA0203Resp。
			resp = this.respDto(tsmpClient);
			
            //3.查詢TSMP_CLIENT_HOST資料表，條件TSMP_CLIENT_HOST.CLIENT_ID = AA0203Req.clientID，將查詢出來的資料轉換成AA0203Resp.hostList。
			List<TsmpClientHost> hostList = getTsmpClientHostDao().findByClientId(tsmpClient.getClientId());
			this.hostListDto(hostList, resp);
			
			//4.查詢TSMP_CLIENT_GROUP資料表，條件TSMP_CLIENT_GROUP.CLIENT_ID，若有查到資料則進行步驟5。
			List<TsmpClientGroup> clientGroupList = getTsmpClientGroupDao().findByClientId(tsmpClient.getClientId());
			
			//5.查詢TSMP_GROUP資料表，條件TSMP_GROUP.GROUP_ID=TSMP_CLIENT_GROUP.GROUP_ID AND VGROUP_FLAG = "0"。將查詢出來的資料轉換成AA0203Resp.groupInfoList。
			List<AA0203GroupInfo> groupInfoList = new ArrayList<>();
			if(clientGroupList.size() > 0) {
				clientGroupList.forEach(x ->{
					TsmpGroup tsmpGroup = getTsmpGroupDao().findFirstByGroupIdAndVgroupFlag(x.getGroupId(), "0");
					if(tsmpGroup != null) {
						groupInfoList.add(this.groupInfoDto(tsmpGroup));
					}
				});
			}
			resp.setGroupInfoList(groupInfoList);
			
			//6.查詢TSMP_CLIENT_VGROUP資料表，條件TSMP_CLIENT_VGROUP.CLIENT_ID =TSMP_CLIENT_GROUP.CLIENT_ID ，若有查到資料則進行步驟7。
			List<TsmpClientVgroup> clientVgroupList = getTsmpClientVgroupDao().findByClientId(tsmpClient.getClientId());
			
			//7.查詢TSMP_VGROUP資料表，條件TSMP_VGROUP.VGROUP_ID = TSMP_CLIENT_VGROUP.VGROUP_ID ，將查詢出來的資料轉換成AA0203Resp.vgroupInfoList。
			List<AA0203VgroupInfo> vgroupInfoList = new ArrayList<>();
			if(clientVgroupList.size() > 0) {
				clientVgroupList.forEach(x ->{
					TsmpVgroup tsmpVgroup = getTsmpVgroupDao().findById(x.getVgroupId()).orElse(null);
					if(tsmpVgroup != null) {
						vgroupInfoList.add(this.vgroupInfoDto(tsmpVgroup));
					}
				});
			}
			resp.setVgroupInfoList(vgroupInfoList);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	private void checkParam(AA0203Req req) {
		String clientId = req.getClientID();
		String clientName = req.getClientName();
		
		if(StringUtils.isEmpty(clientName)) {
			//1324:用戶端代號:必填參數
			throw TsmpDpAaRtnCode._1324.throwing();
		}
		
		if(StringUtils.isEmpty(clientId)) {
			//1343:用戶端帳號:必填參數
			throw TsmpDpAaRtnCode._1343.throwing();
		}
	}
	
	private AA0203Resp respDto(TsmpClient tsmpClient) {
		AA0203Resp res = new AA0203Resp();
		res.setApiQuota(tsmpClient.getApiQuota() != null ? tsmpClient.getApiQuota().toString() : "");
		res.setApiUsed(tsmpClient.getApiUsed() != null ? tsmpClient.getApiUsed().toString() : "");
		res.setcPriority(tsmpClient.getcPriority() != null ? tsmpClient.getcPriority().toString() : "");
		res.setClientAlias(tsmpClient.getClientAlias());
		res.setClientStartDate(tsmpClient.getStartDate() != null ? TimeZoneUtil.long2UTCstring(tsmpClient.getStartDate()) : "");
		res.setClientEndDate(tsmpClient.getEndDate() != null ? TimeZoneUtil.long2UTCstring(tsmpClient.getEndDate()) : "");
		res.setClientID(tsmpClient.getClientId());
		res.setClientName(tsmpClient.getClientName());
		res.setClientStartTimePerDay(tsmpClient.getStartTimePerDay() != null ? TimeZoneUtil.long2UTCstring(tsmpClient.getStartTimePerDay()) : "");
		res.setClientEndTimePerDay(tsmpClient.getEndTimePerDay() != null ? TimeZoneUtil.long2UTCstring(tsmpClient.getEndTimePerDay()) : "");
		res.setTimeZone(tsmpClient.getTimeZone() != null ? tsmpClient.getTimeZone() : "");
		Optional<String> optCd = DateTimeUtil.dateTimeToString(tsmpClient.getCreateTime(), DateTimeFormatEnum.西元年月日時分秒);
		if(optCd.isPresent()) {
			res.setCreateDate(optCd.get());
		}else {
			res.setCreateDate("");
		}
		res.setCreateUser(tsmpClient.getCreateUser());
		res.setEmails(tsmpClient.getEmails());
		res.setFailTreshhold(tsmpClient.getFailTreshhold() != null ? tsmpClient.getFailTreshhold().toString() : "3");
		res.setOwner(tsmpClient.getOwner());
		res.setMimaFailTimes(tsmpClient.getPwdFailTimes() != null ? tsmpClient.getPwdFailTimes().toString() : "0");
		res.setSignupNum(ServiceUtil.nvl(tsmpClient.getSignupNum()));
		res.setStatus(tsmpClient.getClientStatus());
		res.setTps(tsmpClient.getTps() != null ? tsmpClient.getTps().toString() : "10");
		Optional<String> optUd = DateTimeUtil.dateTimeToString(tsmpClient.getUpdateTime(), DateTimeFormatEnum.西元年月日時分秒);
		if(optUd.isPresent()) {
			res.setUpdateDate(optUd.get());
		}else {
			res.setUpdateDate("");
		}
		res.setUpdateUser(ServiceUtil.nvl(tsmpClient.getUpdateUser()));
		res.setRemark(tsmpClient.getRemark()!=null?tsmpClient.getRemark():"");
		
//		1.查詢TSMP_SETTING資料表，條件 ID = "TSMP_AC_CONF"，將查詢出來的值轉換成JSON物件，取出dp欄位的值。
//		2.若dp欄位的值不為0，則publicFlag="2"
//		3.若dp欄位的值為0，查詢TSMP_DP_CLIENTEXT資料表，條件client_id = TSMP_CLIENT.CLIENT_ID ，取出TSMP_DP_CLIENTEXT.public_flag。
//		int dp = this.getDp();
//		if(dp != 0) {
//			res.setPublicFlag("2");
//		}else {
//			TsmpDpClientext dpClientextVo = getTsmpDpClientextDao().findById(tsmpClient.getClientId()).orElse(null);
//			if(dpClientextVo != null) {
//				res.setPublicFlag(dpClientextVo.getPublicFlag());
//			}else {
//				res.setPublicFlag("");
//			}
//		}
		
		//parser			
		Optional<TsmpDpClientext> tsmpDpClientext = getTsmpDpClientextDao().findById(tsmpClient.getClientId());
		TsmpDpClientext tsmpDpClientext1 = new TsmpDpClientext();
		tsmpDpClientext1.setPublicFlag("2");
		res.setPublicFlag(tsmpDpClientext.orElse(tsmpDpClientext1).getPublicFlag());
		
		
		AA0203SecurityLV aa0203SecurityVo = new AA0203SecurityLV();
		if(tsmpClient.getSecurityLevelId() != null) {
			TsmpSecurityLevel securityVo = getSecurityLVDao().findById(tsmpClient.getSecurityLevelId()).orElse(null);
			if(securityVo != null) {
				aa0203SecurityVo.setSecurityLevelId(securityVo.getSecurityLevelId());
				aa0203SecurityVo.setSecurityLevelName(securityVo.getSecurityLevelName());
				aa0203SecurityVo.setSecurityLevelDesc(securityVo.getSecurityLevelDesc());
			}
		}
		res.setSecurityLV(aa0203SecurityVo);
		
		return res;
	}
	
	private void hostListDto(List<TsmpClientHost> hostList, AA0203Resp resp) {
		List<AA0203Host> aa0203HostList = new ArrayList<>();
		if(hostList != null && hostList.size() > 0) {
			hostList.forEach(hostVo -> {
				AA0203Host vo = new AA0203Host();
				vo.setHostIP(hostVo.getHostIp());
				vo.setHostName(hostVo.getHostName());
				vo.setHostSeq(hostVo.getHostSeq().toString());
				aa0203HostList.add(vo);
			});
		}
		resp.setHostList(aa0203HostList);
	}
	
	private AA0203GroupInfo groupInfoDto(TsmpGroup tsmpGroup) {
		AA0203GroupInfo vo = new AA0203GroupInfo();
		vo.setGroupAlias(tsmpGroup.getGroupAlias());
		vo.setGroupDesc(tsmpGroup.getGroupDesc());
		vo.setGroupID(tsmpGroup.getGroupId());
		vo.setGroupName(tsmpGroup.getGroupName());
		vo.setSecurityLevelID(tsmpGroup.getSecurityLevelId());
		
		//查詢TSMP_SECURITY_LEVEL資料表，條件SECURITY_LEVEL_ID = TSMP_GROUP.SECURITY_LEVEL_ID，取出TSMP_SECURITY_LEVEL.SECURITY_LEVEL_NAME。
		if(tsmpGroup.getSecurityLevelId() != null) {
			TsmpSecurityLevel securityVo = getSecurityLVDao().findById(tsmpGroup.getSecurityLevelId()).orElse(null);
			if(securityVo != null) {
				vo.setSecurityLevelName(securityVo.getSecurityLevelName());
			}
		}
		
		return vo;
	}
	
	private AA0203VgroupInfo vgroupInfoDto(TsmpVgroup tsmpVgroup) {
		AA0203VgroupInfo vo = new AA0203VgroupInfo();
		vo.setVgroupAlias(tsmpVgroup.getVgroupAlias());
		vo.setVgroupDesc(tsmpVgroup.getVgroupDesc());
		vo.setVgroupID(tsmpVgroup.getVgroupId());
		vo.setVgroupName(tsmpVgroup.getVgroupName());
		vo.setSecurityLevelID(tsmpVgroup.getSecurityLevelId());
		
		//查詢TSMP_SECURITY_LEVEL資料表，條件SECURITY_LEVEL_ID = TSMP_GROUP.SECURITY_LEVEL_ID，取出TSMP_SECURITY_LEVEL.SECURITY_LEVEL_NAME。
		if(tsmpVgroup.getSecurityLevelId() != null) {
			TsmpSecurityLevel securityVo = getSecurityLVDao().findById(tsmpVgroup.getSecurityLevelId()).orElse(null);
			if(securityVo != null) {
				vo.setSecurityLevelName(securityVo.getSecurityLevelName());
			}
		}
		
		return vo;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}

	protected TsmpSettingDao getTsmpSettingDao() {
		return tsmpSettingDao;
	}

	protected TsmpDpClientextDao getTsmpDpClientextDao() {
		return tsmpDpClientextDao;
	}

	protected TsmpClientHostDao getTsmpClientHostDao() {
		return tsmpClientHostDao;
	}

	protected TsmpClientGroupDao getTsmpClientGroupDao() {
		return tsmpClientGroupDao;
	}

	protected TsmpGroupDao getTsmpGroupDao() {
		return tsmpGroupDao;
	}

	protected TsmpClientVgroupDao getTsmpClientVgroupDao() {
		return tsmpClientVgroupDao;
	}

	protected TsmpVgroupDao getTsmpVgroupDao() {
		return tsmpVgroupDao;
	}

	protected TsmpSecurityLevelDao getSecurityLVDao() {
		return securityLVDao;
	}
	
	
	

}

package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0238GroupAuthoritiesInfo;
import tpi.dgrv4.dpaa.vo.AA0238GroupInfo;
import tpi.dgrv4.dpaa.vo.AA0238Req;
import tpi.dgrv4.dpaa.vo.AA0238Resp;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.entity.TsmpGroupAuthorities;
import tpi.dgrv4.entity.entity.TsmpGroupAuthoritiesMap;
import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;
import tpi.dgrv4.entity.repository.TsmpGroupAuthoritiesDao;
import tpi.dgrv4.entity.repository.TsmpGroupAuthoritiesMapDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.entity.repository.TsmpSecurityLevelDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0238Service {

	private TPILogger logger = TPILogger.tl;

	private Integer pageSize;
	
	private TsmpGroupDao tsmpGroupDao;
	private TsmpSecurityLevelDao tsmpSecurityLevelDao;
	private TsmpGroupAuthoritiesMapDao tsmpGroupAuthoritiesMapDao;
	private TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao;
	private ServiceConfig serviceConfig;

	@Autowired
	public AA0238Service(TsmpGroupDao tsmpGroupDao, TsmpSecurityLevelDao tsmpSecurityLevelDao,
			TsmpGroupAuthoritiesMapDao tsmpGroupAuthoritiesMapDao, TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao,
			ServiceConfig serviceConfig) {
		super();
		this.tsmpGroupDao = tsmpGroupDao;
		this.tsmpSecurityLevelDao = tsmpSecurityLevelDao;
		this.tsmpGroupAuthoritiesMapDao = tsmpGroupAuthoritiesMapDao;
		this.tsmpGroupAuthoritiesDao = tsmpGroupAuthoritiesDao;
		this.serviceConfig = serviceConfig;
	}


	public AA0238Resp queryGroupList_1(TsmpAuthorization authorization, AA0238Req req) {
		AA0238Resp resp = new AA0238Resp();

		try {
			String groupId = req.getGroupId();
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
			String securityLevelId = req.getSecurityLevelID();
			List<String> groupAuthoritieIdList  = req.getGroupAuthoritiesID();
			
			//1.查詢一般群組(不包含虛擬群組)。
			List<TsmpGroup> tsmpGroupList = getTsmpGroupDao().queryByAA0238Service(groupId, groupAuthoritieIdList, securityLevelId, words, getPageSize());
			
			if(tsmpGroupList.size() == 0) {
				//1298:查無資料
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			//2.將步驟1的查詢結果資料放進，AA0238Resp.groupInfoList，欄位的對應請看AA0238GroupInfo的Remark欄位。
			List<AA0238GroupInfo> groupInfoList = new ArrayList<>();
			tsmpGroupList.forEach(vo ->{
				AA0238GroupInfo infoVo = new AA0238GroupInfo();
				
				if(vo.getSecurityLevelId() == null) {
					infoVo.setSecurityLevelName("");
				}else {
					TsmpSecurityLevel TsmpSecurityLevelVo = getTsmpSecurityLevelDao().findById(vo.getSecurityLevelId()).orElse(null);
					if(TsmpSecurityLevelVo != null) {
						String securityLevelName = TsmpSecurityLevelVo.getSecurityLevelName();
						infoVo.setSecurityLevelName(securityLevelName);
					}else {
						infoVo.setSecurityLevelName("");
					}
				}
				
				infoVo.setGroupName(vo.getGroupName());
				infoVo.setGroupID(vo.getGroupId());
				infoVo.setGroupDesc(vo.getGroupDesc());
				infoVo.setGroupAlias(vo.getGroupAlias());
				infoVo.setCreateTime(DateTimeUtil.dateTimeToString(vo.getCreateTime(), DateTimeFormatEnum.西元年月日時分秒).orElse(""));
				
				groupInfoList.add(infoVo);
			});
			
			//3.Parsing授權核身種類資料。
			groupInfoList.forEach(vo ->{
				//3.1先查詢TSMP_GROUP_AUTHORITIES_MAP資料表，將TSMP_GROUP_AUTHORITIES_MAP.GROUP_AUTHORITIE_ID查詢出來。
				//條件TSMP_GROUP_AUTHORITIES_MAP.GROUP_ID = 每一個AA0238Resp.List<AA0238GroupInfo>.groupID 
				List<TsmpGroupAuthoritiesMap> authMapList = getTsmpGroupAuthoritiesMapDao().findByGroupId(vo.getGroupID());
				List<String> authNameList = new ArrayList<>();
				authMapList.forEach(authMapVo ->{
					//3.2查詢TSMP_GROUP_AUTHORITIES資料表。
					//條件TSMP_GROUP_AUTHORITIES.GROUP_AUTHORITIE_ID = (步驟3.1的查詢結果，每一筆TSMP_GROUP_AUTHORITIES_MAP.GROUP_AUTHORITIE_ID資料)
					TsmpGroupAuthorities authVo = getTsmpGroupAuthoritiesDao().findById(authMapVo.getGroupAuthoritieId()).orElse(null);
					if(authVo != null) {
						authNameList.add(authVo.getGroupAuthoritieName());
					}
				});
				
				//4.將步驟3.2的查詢結果資料放進，AA0238Resp.groupInfoList.groupAuthorities，欄位的對應請看AA0238GroupAuthoritiesInfo的Remark欄位。
				AA0238GroupAuthoritiesInfo infoVo = new AA0238GroupAuthoritiesInfo();
				String oriGroupAuthoritieName = String.join("、", authNameList);
				if(oriGroupAuthoritieName.length() > 100) {
					String groupAuthoritieName = oriGroupAuthoritieName.substring(0, 100) + "...";
					infoVo.setTruncated(true);
					infoVo.setGroupAuthoritieName(groupAuthoritieName);
				}else {
					String groupAuthoritieName = oriGroupAuthoritieName;
					infoVo.setTruncated(false);
					infoVo.setGroupAuthoritieName(groupAuthoritieName);
				}
				infoVo.setOriGroupAuthoritieName(oriGroupAuthoritieName);
				
				vo.setGroupAuthorities(infoVo);
			});
			
			resp.setGroupInfoList(groupInfoList);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//1297:執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}


	protected TsmpGroupDao getTsmpGroupDao() {
		return tsmpGroupDao;
	}
	
	protected TsmpSecurityLevelDao getTsmpSecurityLevelDao() {
		return tsmpSecurityLevelDao;
	}

	protected TsmpGroupAuthoritiesMapDao getTsmpGroupAuthoritiesMapDao() {
		return tsmpGroupAuthoritiesMapDao;
	}

	protected TsmpGroupAuthoritiesDao getTsmpGroupAuthoritiesDao() {
		return tsmpGroupAuthoritiesDao;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa0238");
		return pageSize;
	}

	protected ServiceConfig getServiceConfig() {
		return serviceConfig;
	}
	
	
}

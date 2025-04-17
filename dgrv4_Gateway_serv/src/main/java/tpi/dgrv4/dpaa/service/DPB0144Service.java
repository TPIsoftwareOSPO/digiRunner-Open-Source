package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.TimeZoneUtil;
import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0144Req;
import tpi.dgrv4.dpaa.vo.DPB0144Resp;
import tpi.dgrv4.dpaa.vo.DPB0144RespAudit;
import tpi.dgrv4.dpaa.vo.DPB0144RespAuditItem;
import tpi.dgrv4.dpaa.vo.DPB0144RespAuditItemLogin;
import tpi.dgrv4.dpaa.vo.DPB0144RespLiveNode;
import tpi.dgrv4.dpaa.vo.DPB0144RespLostNode;
import tpi.dgrv4.dpaa.vo.DPB0144RespSummary;
import tpi.dgrv4.dpaa.vo.DPB0144RespSummaryItem;
import tpi.dgrv4.dpaa.vo.DPB0144RespSummaryItemApis;
import tpi.dgrv4.entity.entity.jpql.DgrNodeLostContact;
import tpi.dgrv4.entity.repository.DgrAuditLogMDao;
import tpi.dgrv4.entity.repository.DgrNodeLostContactDao;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.TCP.Packet.RequireAllClientListPacket;
import tpi.dgrv4.gateway.constant.SummaryStatus;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.ClientKeeper;
import tpi.dgrv4.gateway.vo.ComposerInfoData;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0144Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpUserDao tsmpUserDao;
	private TsmpRoleDao tsmpRoleDao;
	private TsmpClientDao tsmpClientDao;
	private TsmpGroupDao tsmpGroupDao;
	private TsmpApiDao tsmpApiDao;
	private DgrAuditLogMDao dgrAuditLogMDao;
	private DgrNodeLostContactDao dgrNodeLostContactDao;
	private TsmpSettingService tsmpSettingService;

	@Autowired
	public DPB0144Service(TsmpUserDao tsmpUserDao, TsmpRoleDao tsmpRoleDao, TsmpClientDao tsmpClientDao,
			TsmpGroupDao tsmpGroupDao, TsmpApiDao tsmpApiDao, DgrAuditLogMDao dgrAuditLogMDao,
			DgrNodeLostContactDao dgrNodeLostContactDao, TsmpSettingService tsmpSettingService) {
		super();
		this.tsmpUserDao = tsmpUserDao;
		this.tsmpRoleDao = tsmpRoleDao;
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpGroupDao = tsmpGroupDao;
		this.tsmpApiDao = tsmpApiDao;
		this.dgrAuditLogMDao = dgrAuditLogMDao;
		this.dgrNodeLostContactDao = dgrNodeLostContactDao;
		this.tsmpSettingService = tsmpSettingService;
	}

	public DPB0144Resp queryMonitor(TsmpAuthorization auth, DPB0144Req req) {

		try {
			int queryDay = getTsmpSettingService().getVal_DGR_QUERY_MONITOR_DAY();
			queryDay = positiveIntegerCheck(queryDay);
			queryDay = 0 - queryDay;
			
			Date start = getDate(queryDay); 
			Date end = getDate(0); // 現在
	
			// 查詢
			DPB0144RespAudit audit = queryAudit(start, end);
			DPB0144RespSummary summary = querySummary();
			
			List<DPB0144RespLostNode> lostNodeList = queryLostNode(start);
			
			DPB0144Resp dpb0144Resp = new DPB0144Resp();
			dpb0144Resp.setAudit(audit);
			dpb0144Resp.setSummary(summary);			
			dpb0144Resp.setLostNodeList(lostNodeList);
			dpb0144Resp.setPeriod(getPeriodString(queryDay));
			
			queryLiveNode(dpb0144Resp);
			
			return dpb0144Resp;
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	// 確認是否為正整數，否則回傳 -1
	private int positiveIntegerCheck(int x) {
		if (Integer.signum(x) > 0) {
			return x;
		}
		return -1;
	}
	
	// 取得 Period 字串，1 = Today , 其他 = Last xxx days
	private String getPeriodString(int x) {
		if (x == 1) {
			return "Today";
		} else {
			return "Last " + Math.abs(x) + " days";
		}
	}
	
	// 查詢 Audit 底下各目標的數量，裝入 vo 後回傳。
	private DPB0144RespAudit queryAudit(Date start, Date end) {

		DPB0144RespAudit dpb0144RespAudit = new DPB0144RespAudit();

		// User 的 CREATE、UPDATE、DELETE 數量。
		DPB0144RespAuditItem user = new DPB0144RespAuditItem();
		user.setCreate(countByEvent(AuditLogEvent.ADD_USER.value(), start, end));
		user.setUpdate(countByEvent(AuditLogEvent.UPDATE_USER.value(), start, end));
		user.setDelete(countByEvent(AuditLogEvent.DELETE_USER.value(), start, end));

		// Role 的 CREATE、UPDATE、DELETE 數量。
		DPB0144RespAuditItem role  = new DPB0144RespAuditItem();
		role.setCreate(countByEvent(AuditLogEvent.ADD_ROLE.value(), start, end));
		role.setUpdate(countByEvent(AuditLogEvent.UPDATE_ROLE.value(), start, end));
		role.setDelete(countByEvent(AuditLogEvent.DELETE_ROLE.value(), start, end));

		// Client 的 CREATE、UPDATE、DELETE 數量。
		DPB0144RespAuditItem client = new DPB0144RespAuditItem();
		client.setCreate(countByEvent(AuditLogEvent.ADD_CLIENT.value(), start, end));
		client.setUpdate(countByEvent(AuditLogEvent.UPDATE_CLIENT.value(), start, end));
		client.setDelete(countByEvent(AuditLogEvent.DELETE_CLIENT.value(), start, end));

		// Group 的 CREATE、UPDATE、DELETE 數量。
		DPB0144RespAuditItem group = new DPB0144RespAuditItem();
		group.setCreate(countByEvent(AuditLogEvent.ADD_GROUP.value(), start, end));
		group.setUpdate(countByEvent(AuditLogEvent.UPDATE_GROUP.value(), start, end));
		group.setDelete(countByEvent(AuditLogEvent.DELETE_GROUP.value(), start, end));

		// RegisterApi 的 CREATE、UPDATE、DELETE 數量。
		DPB0144RespAuditItem registerApi = new DPB0144RespAuditItem();
		registerApi.setCreate(countByEvent(AuditLogEvent.ADD_REGISTER_API.value(), start, end));
		registerApi.setUpdate(countByEvent(AuditLogEvent.UPDATE_REGISTER_API.value(), start, end));
		registerApi.setDelete(countByEvent(AuditLogEvent.DELETE_REGISTER_API.value(), start, end));

		// ComposerApi 的 CREATE、UPDATE、DELETE 數量。
		DPB0144RespAuditItem composerApi = new DPB0144RespAuditItem();		
		composerApi.setCreate(countByEvent(AuditLogEvent.ADD_COMPOSER_API.value(), start, end));
		composerApi.setUpdate(countByEvent(AuditLogEvent.UPDATE_COMPOSER_API.value(), start, end));
		composerApi.setDelete(countByEvent(AuditLogEvent.DELETE_COMPOSER_API.value(), start, end));

		// Login 的 Success 與 Failed 的數量。
		DPB0144RespAuditItemLogin login = new DPB0144RespAuditItemLogin();
		Long success = countByEventAndParam(AuditLogEvent.LOGIN.value(), "SUCCESS", start, end);
		Long fail = countByEventAndParam(AuditLogEvent.LOGIN.value(), "FAILED", start, end);
		
		login.setSuccess(success);
		login.setFail(fail);
		
		dpb0144RespAudit.setUser(user);
		dpb0144RespAudit.setRole(role);
		dpb0144RespAudit.setClient(client);
		dpb0144RespAudit.setGroup(group);
		dpb0144RespAudit.setRegisterApi(registerApi);
		dpb0144RespAudit.setComposerApi(composerApi);
		dpb0144RespAudit.setLogin(login);

		return dpb0144RespAudit;
	}

	// 查詢 Summary 底下各目標的數量，裝入 vo 後回傳。
	private DPB0144RespSummary querySummary() {

		DPB0144RespSummary summary = new DPB0144RespSummary();
		
		// 計算 User 在資料庫內，狀態為 Enabled、Disabled、Locked 的筆數。
		DPB0144RespSummaryItem users = new DPB0144RespSummaryItem();
		users.setEnabled(tsmpUserDao.countByUserStatus(SummaryStatus.ENABLED.value()));
		users.setDisabled(tsmpUserDao.countByUserStatus(SummaryStatus.DISABLED.value()));
		users.setLocked(tsmpUserDao.countByUserStatus(SummaryStatus.LOCKED.value()));

		// 計算 Role 在資料庫內的筆數。
		long roles = tsmpRoleDao.count();
		
		// 計算 Client 在資料庫內，狀態為 Enabled、Disabled、Locked 的筆數。
		DPB0144RespSummaryItem clients = new DPB0144RespSummaryItem();
		clients.setEnabled(
				tsmpClientDao.countByClientStatus(SummaryStatus.ENABLED.value()));
		clients.setDisabled(
				tsmpClientDao.countByClientStatus(SummaryStatus.DISABLED.value()));
		clients.setLocked(
				tsmpClientDao.countByClientStatus(SummaryStatus.LOCKED.value()));

		// 計算 Group 在資料庫內的筆數。
		long groups = tsmpGroupDao.count();
		
		// 計算 Register API 在資料庫內，狀態為 ON、OFF 的筆數。
		DPB0144RespSummaryItemApis registerApis = new DPB0144RespSummaryItemApis();
		registerApis.setOn(tsmpApiDao.countByApiSrcAndApiStatus(SummaryStatus.REGISTER.value(), //
				SummaryStatus.ON.value()));
		registerApis.setOff(tsmpApiDao.countByApiSrcAndApiStatus(SummaryStatus.REGISTER.value(), //
				SummaryStatus.OFF.value()));

		// 計算 Composer API 在資料庫內，狀態為 ON、OFF 的筆數。
		DPB0144RespSummaryItemApis composerApis = new DPB0144RespSummaryItemApis();
		composerApis.setOn(tsmpApiDao.countByApiSrcAndApiStatus(SummaryStatus.COMPOSER.value(), //
				SummaryStatus.ON.value()));
		composerApis.setOff(tsmpApiDao.countByApiSrcAndApiStatus(SummaryStatus.COMPOSER.value(), //
				SummaryStatus.OFF.value()));
		
		summary.setUsers(users);
		summary.setRoles(roles);
		summary.setClients(clients);
		summary.setGroups(groups);
		summary.setRegisterApis(registerApis);
		summary.setComposerApis(composerApis);
		
		return summary; 

	}
	
	private void queryLiveNode(DPB0144Resp dpb0144Resp) {
		//取得全部keeper
		TPILogger.lc.send(new RequireAllClientListPacket());
		dpb0144Wait();
		
	    LinkedList<ClientKeeper> allClientList = null;
	    LinkedList<ComposerInfoData> liveComposerList = null;
	    allClientList = (LinkedList<ClientKeeper>) TPILogger.lc.paramObj.get("allClientList");
	    liveComposerList = (LinkedList<ComposerInfoData>) TPILogger.lc.paramObj.get(RequireAllClientListPacket.allComposerListStr);
	    
	    //裝載回傳資料
	    List<DPB0144RespLiveNode> rsList = new ArrayList<DPB0144RespLiveNode>();
	    if(!CollectionUtils.isEmpty(allClientList)) {
	    	for(ClientKeeper keeperVo : allClientList) {
	    		DPB0144RespLiveNode nodeVo = new DPB0144RespLiveNode();
	    		nodeVo.setIp(keeperVo.getIp());
	    		nodeVo.setNodeName(keeperVo.getUsername());
	    		nodeVo.setPort(keeperVo.getPort());
	    		if(StringUtils.hasText(keeperVo.getStartTime())) {
	    			nodeVo.setStartupTime(TimeZoneUtil.long2UTCstring(Long.valueOf(keeperVo.getStartTime())));
	    		}
	    		nodeVo.setVersion(keeperVo.getVersion());
	    		nodeVo.setRcdCacheSize(keeperVo.getRcdCacheSize());
	    		nodeVo.setDaoCacheSize(keeperVo.getDaoCacheSize());
	    		nodeVo.setFixedCacheSize(keeperVo.getFixedCacheSize());
	    		nodeVo.setKeeperServerIp(keeperVo.getKeeperServerIp());
	    		nodeVo.setKeeperServerPort(keeperVo.getKeeperServerPort());
	    		nodeVo.setServerPort(keeperVo.getServerPort());
	    		nodeVo.setServerServletContextPath(keeperVo.getServerServletContextPath());
	    		nodeVo.setServerSslEnabled(keeperVo.getServerSslEnalbed());
	    		nodeVo.setSpringProfilesActive(keeperVo.getSpringProfilesActive());
	    		nodeVo.setWebLocalIP(keeperVo.getWebLocalIP());
	    		nodeVo.setFqdn(keeperVo.getFqdn());
	    		
	    		rsList.add(nodeVo);
		    }
	    }
	    
	    dpb0144Resp.setLiveNodeList(rsList);
	    dpb0144Resp.setLiveComposerList(liveComposerList);
	}
	
	private List<DPB0144RespLostNode> queryLostNode(Date startDate) {
		List<DgrNodeLostContact> lostList = getDgrNodeLostContactDao().findByCreateTimestampGreaterThanEqual(startDate.getTime(), Sort.by(Sort.Direction.DESC, "lostContactId"));
		List<DPB0144RespLostNode> rsList = new ArrayList<DPB0144RespLostNode>();
		
		for(DgrNodeLostContact lostVo : lostList) {
			DPB0144RespLostNode nodeVo = new DPB0144RespLostNode();
			nodeVo.setIp(lostVo.getIp());
			nodeVo.setLostTime(lostVo.getLostTime());
			nodeVo.setNodeName(lostVo.getNodeName());
			nodeVo.setPort(lostVo.getPort());
			
			rsList.add(nodeVo);
		}
		
		return rsList;
	}

	private Long countByEvent(String eventNo, Date start, Date end) {
		return dgrAuditLogMDao.countByEventNoAndCreateDateTimeBetween(eventNo, start, end);
	}

	private Long countByEventAndParam(String eventNo, String param, Date start, Date end) {
		return dgrAuditLogMDao.countByEventNoAndParam1AndCreateDateTimeBetween(eventNo, param, start, end);
	}
	
	// 傳入數字取得時間，0=現在、7=七天後、-7=七天前。
	private Date getDate(int day) {
		Calendar calendar = Calendar.getInstance();
		
		if (day == 1) {
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
		} else {
			calendar.add(Calendar.HOUR, day * 24);
		}
		
		return calendar.getTime();
	}
	
	private void dpb0144Wait() {
		synchronized (RequireAllClientListPacket.waitKey) {
			try {
				RequireAllClientListPacket.waitKey.wait();
			} catch (InterruptedException e) {
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			    Thread.currentThread().interrupt();
			}
		}
	}
	
	protected DgrNodeLostContactDao getDgrNodeLostContactDao() {
		return dgrNodeLostContactDao;
	}

	public TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
	
	
}

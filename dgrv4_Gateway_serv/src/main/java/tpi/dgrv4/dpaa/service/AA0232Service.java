package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0232Log;
import tpi.dgrv4.dpaa.vo.AA0232Req;
import tpi.dgrv4.dpaa.vo.AA0232Resp;
import tpi.dgrv4.entity.entity.jpql.TsmpClientLog;
import tpi.dgrv4.entity.repository.TsmpClientLogDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0232Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpClientLogDao tsmpClientLogDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public AA0232Service(TsmpClientLogDao tsmpClientLogDao, ServiceConfig serviceConfig) {
		super();
		this.tsmpClientLogDao = tsmpClientLogDao;
		this.serviceConfig = serviceConfig;
	}

	public AA0232Resp queryClientEventLog(TsmpAuthorization authorization, AA0232Req req) {
		AA0232Resp resp = new AA0232Resp();
		try {

			String logSeq = req.getLogSeq();
			String[] keyword = ServiceUtil.getKeywords(req.getKeyword(), " ");
			String startTime = req.getStartTime();
			String endTime = req.getEndTime();
			String eventType = req.getEventType();
			
			TsmpClientLog tsmpClientLog = null;
			
			//若有傳最後一筆的logSeq資料，則查詢logSeq的TsmpClientLog資料。
			if (StringUtils.isEmpty(logSeq)==false) {
				Optional<TsmpClientLog> opt_tsmpClientLog = getTsmpClientLogDao().findById(logSeq);
				if (opt_tsmpClientLog.isPresent()) {
					tsmpClientLog = opt_tsmpClientLog.get();
				}
			}
			
			// 進行 分頁 + 關鍵字 查詢
			List<TsmpClientLog> tsmpClientLogList = getTsmpClientLogDao().findByLogSeqAndEventTypeAndStartTimeAndEndTimeAndKeyword(tsmpClientLog, eventType, startTime, endTime, keyword, getPageSize());
				
			if (tsmpClientLogList == null || tsmpClientLogList.size() == 0) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			List<AA0232Log> aa0232LogList = convertAA0232LogList(tsmpClientLogList);			
			resp.setClientLogList(aa0232LogList);	
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	private List<AA0232Log> convertAA0232LogList(List<TsmpClientLog> tsmpClientLogList){
		List<AA0232Log> aa0232LogList = new ArrayList<AA0232Log>();
		
		for (TsmpClientLog tsmpClientLog : tsmpClientLogList) {
			AA0232Log aa0232Log=new AA0232Log();
			aa0232Log.setAgent(tsmpClientLog.getAgent());
			aa0232Log.setClientID(tsmpClientLog.getClientId());//用戶端帳號
			aa0232Log.setClientIP(tsmpClientLog.getClientIp());//用戶端IP
			if (tsmpClientLog.getCreateTime()!=null) {
				Optional<String> opt_createTime = DateTimeUtil.dateTimeToString(tsmpClientLog.getCreateTime(), DateTimeFormatEnum.西元年月日時分);
				if (opt_createTime.isPresent()) {
					aa0232Log.setCreatTime(opt_createTime.get());//建立時間
				}	
			}
			
			aa0232Log.setEventMsg(tsmpClientLog.getEventMsg());//事件訊息
			
			if (tsmpClientLog.getEventTime()!=null) {
				Optional<String> opt_eventTime = DateTimeUtil.dateTimeToString(tsmpClientLog.getEventTime(), DateTimeFormatEnum.西元年月日時分);
				if (opt_eventTime.isPresent()) {
					aa0232Log.setEventTime(opt_eventTime.get());//事件時間
				}
			}
			
			aa0232Log.setEventType(tsmpClientLog.getEventType());//事件類型
			
			if (tsmpClientLog.getIsLogin()==null) {
				aa0232Log.setIsLogin("FALSE");//是否登入
			}else {
				if (tsmpClientLog.getIsLogin().intValue()==1) {
					aa0232Log.setIsLogin("TRUE");//是否登入
				}else {
					aa0232Log.setIsLogin("FALSE");//是否登入
				}
			}
			
			aa0232Log.setLogSeq(tsmpClientLog.getLogSeq()); //Log序號
			aa0232Log.setTxsn(tsmpClientLog.getTxsn());
			aa0232Log.setUserName(tsmpClientLog.getUserName());
			aa0232LogList.add(aa0232Log);
		}
		
		return aa0232LogList;
	}	
	
	protected TsmpClientLogDao getTsmpClientLogDao() {
		return tsmpClientLogDao;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa0232");
		return this.pageSize;
	}
}

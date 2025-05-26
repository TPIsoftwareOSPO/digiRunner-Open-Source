package tpi.dgrv4.dpaa.service;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.vo.AA0206Req;
import tpi.dgrv4.dpaa.vo.AA0206Resp;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.jpql.TsmpClientLog;
import tpi.dgrv4.entity.repository.SeqStoreDao;
import tpi.dgrv4.entity.repository.TsmpClientLogDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0206Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpClientLogDao tsmpClientLogDao;
	private SeqStoreService seqStoreService;
	private SeqStoreDao seqStoreDao;
	
	@Autowired
	public AA0206Service(TsmpClientLogDao tsmpClientLogDao, SeqStoreService seqStoreService, SeqStoreDao seqStoreDao) {
		super();
		this.tsmpClientLogDao = tsmpClientLogDao;
		this.seqStoreService = seqStoreService;
		this.seqStoreDao = seqStoreDao;
	}

	/**
	 * 1.若AA0206Req.eventMsg.length() >100，則 AA0206Req.eventMsg = AA0206Req.eventMsg.substring(0, 100)。
	 * 2.前端傳送的ReqHeader有txSN、txDate，但TsmpHttpHeader沒有去接收，所以需要修改TsmpHttpHeader程式，去接收txSN、txDate。
	 * 3.新增tsmp_client_log資料表，欄位LOG_SEQ = (請看取序號邏輯)、IS_LOGIN = AA0206Req.isLogin、AGENT = AA0206Req.agent、EVENT_TYPE = AA0206Req.eventType、EVENT_MSG = AA0206Req.eventMsg、TXSN = TsmpHttpHeader.txSN 、EVENT_TIME = TsmpHttpHeader.txDate、CLIENT_ID = TsmpHttpHeader.authorization.clientId、CLIENT_IP = 利用httpServletRequest.getRemoteAddr()來取得、USER_NAME = TsmpHttpHeader.authorization.userName、CREATE_TIME = 現在時間。
	 * */
	public AA0206Resp clientEventLog(TsmpAuthorization authorization, String remoteAddr, ReqHeader header, AA0206Req req) {
		AA0206Resp resp = new AA0206Resp();

		try {
			String agent = req.getAgent();
			String eventMsg = req.getEventMsg();
			String eventType = req.getEventType();
			int isLogin = "true".equalsIgnoreCase(req.getIsLogin()) ? 1 : 0;
			String txSN = header.getTxSN();
			
			//傳進來的字串是20200915T150602+0800,將它變成Date物件
			String strTxDate = header.getTxDate();
			int endDateIndex = header.getTxDate().indexOf("+");
			strTxDate = strTxDate.substring(0,endDateIndex).replaceAll("T", "");
			Date txDate = DateTimeUtil.stringToDateTime(strTxDate, DateTimeFormatEnum.西元年月日時分秒_4).orElse(DateTimeUtil.now());
			
			//1.若AA0206Req.eventMsg.length() >100，則 AA0206Req.eventMsg = AA0206Req.eventMsg.substring(0, 100)。
			if(eventMsg.length() > 100) {
				eventMsg = eventMsg.substring(0, 100);
			}
			
			//3.新增tsmp_client_log資料表，欄位LOG_SEQ = (請看取序號邏輯)、IS_LOGIN = AA0206Req.isLogin、AGENT = AA0206Req.agent、
			//EVENT_TYPE = AA0206Req.eventType、EVENT_MSG = AA0206Req.eventMsg、TXSN = TsmpHttpHeader.txSN 、
			//EVENT_TIME = TsmpHttpHeader.txDate、CLIENT_ID = TsmpHttpHeader.authorization.clientId、
			//CLIENT_IP = 利用httpServletRequest.getRemoteAddr()來取得、USER_NAME = TsmpHttpHeader.authorization.userName、CREATE_TIME = 現在時間。
			TsmpClientLog logVo = new TsmpClientLog();
			logVo.setAgent(agent);
			logVo.setClientId(authorization.getClientId());
			logVo.setClientIp(remoteAddr);
			logVo.setCreateTime(DateTimeUtil.now());
			logVo.setEventMsg(eventMsg);
			logVo.setEventTime(txDate);
			logVo.setEventType(eventType);
			logVo.setIsLogin(isLogin);
			String seq = this.getSeq();
			logVo.setLogSeq(seq);
			logVo.setTxsn(txSN);
			logVo.setUserName(authorization.getUserName());
			getTsmpClientLogDao().save(logVo);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//1297:執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	private String getSeq() {
		//1.取現在時間並用DateTimeFormatter.ofPattern("yyMMddHHmmss")進行字串格式化，將字串格式化後存進"時間變數A"。
		String strDateTime = DateTimeUtil.dateTimeToString(DateTimeUtil.now(), DateTimeFormatEnum.西元年月日時分秒_5).orElse("000000000000");
		
		//2.利用SeqStoreDao.nextSequence("tsmp_client_log", 0L, 1L)來取得sequence，取得"sequence"再除以1000000 後取餘數，
		//餘數位數要六位，不足的向左補0，再存進"變數sequence" 。sequence需要的範圍為 000000~999999
		Long seq = getSeqStoreService().nextSequence("tsmp_client_log", 2000000000L, 1L);
		seq = seq % 1000000;
		NumberFormat formatter = new DecimalFormat("000000");
	    String strSeq = formatter.format(seq); 
		
	    //3.序號為1+"時間變數A"+"變數sequence" 。
		return "1" + strDateTime + strSeq;
	}

	
	protected TsmpClientLogDao getTsmpClientLogDao() {
		return tsmpClientLogDao;
	}

	protected SeqStoreService getSeqStoreService() {
		return seqStoreService;
	}

	protected SeqStoreDao getSeqStoreDao() {
		return seqStoreDao;
	}

}

package tpi.dgrv4.dpaa.service;

import java.util.Date;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.OAuthUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0549Req;
import tpi.dgrv4.dpaa.vo.EmptyBodyResp;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.entity.Users;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.entity.repository.UsersDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@RequiredArgsConstructor
@Service
@Getter(AccessLevel.PROTECTED)
public class AA0549Service {

	private final TsmpUserDao tsmpUserDao;
	private final UsersDao usersDao;
	private final DgrAuditLogService dgrAuditLogService;
	private final TsmpSettingService tsmpSettingService;

	@Transactional
	public EmptyBodyResp updateNewMima(TsmpAuthorization auth, AA0549Req req, InnerInvokeParam iip) {
		EmptyBodyResp resp = new EmptyBodyResp();
		try {
			checkParam(auth.getUserName(), req);
			
			//write Audit Log M
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_USER_PROFILE.value());
			
			updateTsmpUser(auth.getUserName(), iip);
			updateUsers(auth.getUserName(), req, iip);
			
			return resp;
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
	}
	
	private void updateTsmpUser(String userName, InnerInvokeParam iip) {
		//Check whether tsmpUser.username exists
		TsmpUser tsmpUserVo = this.getTsmpUserDao().findFirstByUserName(userName);
		if(tsmpUserVo == null) {
			TPILogger.tl.error("username=" + userName);
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		//Convert old data into String
		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpUserVo);
		
		//update tsmpUser
		tsmpUserVo.setLogonDate(new Date());
		tsmpUserVo.setUpdateTime(new Date());
		tsmpUserVo.setUpdateUser(userName);
		tsmpUserVo = this.getTsmpUserDao().saveAndFlush(tsmpUserVo);
		
		//write Audit Log D
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
				TsmpUser.class.getSimpleName(), TableAct.U.value(), oldRowStr, tsmpUserVo);// U
	}
	
	private void updateUsers(String userName, AA0549Req req, InnerInvokeParam iip) throws Exception {
		//Check whether users.username exists
		Users usersVo = this.getUsersDao().findById(userName).orElse(null);
		if(usersVo == null) {
			TPILogger.tl.error("username=" + userName);
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		
		//Convert old data into String
		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, usersVo);
		
		//update users
		String encodeMima = this.getEncodeMima(req.getNewMima());
		usersVo.setPassword(encodeMima);
		usersVo = this.getUsersDao().saveAndFlush(usersVo);
		
		//write Audit Log D
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
				Users.class.getSimpleName(), TableAct.U.value(), oldRowStr, usersVo);// U
	}
	
	private void checkParam(String userName, AA0549Req req) throws Exception {
		
		if(!StringUtils.hasText(req.getNewMima())) {
			throw TsmpDpAaRtnCode._1234.throwing(); 
		}
		
		if(!StringUtils.hasText(req.getOriMima())) {
			throw TsmpDpAaRtnCode._1236.throwing(); 
		}
		
		boolean checkOriginMima = checkOriginMima(userName, req.getOriMima());
		if(!checkOriginMima)
			throw TsmpDpAaRtnCode._1238.throwing();

		// Check new password length
		String decodeMima = base64Decode(req.getNewMima());
		checkMima(decodeMima);
	}

	private String getEncodeMima(String mima) throws Exception {
		return OAuthUtil.bCryptEncode(mima);
	}
	
	private void checkMima(String decodeMima) {
		//Check password strength
		//檢查密碼強度
		if(!decodeMima.matches(this.getTsmpSettingService().getVal_PWD_STRENGTH())) {
			throw TsmpDpAaRtnCode._1352.throwing("{{newMima}}");
		}
				
		String aa0549_msg = "";
		if(decodeMima.length() > 128) {
			int aa0549_length = decodeMima.length();
			aa0549_msg = String.valueOf(aa0549_length);
			throw TsmpDpAaRtnCode._1248.throwing("128", aa0549_msg);
		}
	}
	
	protected boolean checkOriginMima(String userName, String oriMima) {
		boolean isOriginMima = false;
		Optional<Users> user = getUsersDao().findById(userName);
		if(user.isPresent()) {
			String mima = user.get().getPassword();
			isOriginMima = OAuthUtil.bCryptPasswordCheck(oriMima, mima);
		}else {
			TPILogger.tl.error("username=" + userName);
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		
		return isOriginMima;
	}
	
	protected String base64Decode(String userBlock) throws Exception {
		return new String(ServiceUtil.base64Decode(userBlock));
			
	}
	
	
}

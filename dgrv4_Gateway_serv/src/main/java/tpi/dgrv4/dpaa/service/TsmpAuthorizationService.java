package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class TsmpAuthorizationService {

	private TPILogger logger = TPILogger.tl;
	
	private DgrAcIdpUserDao dgrAcIdpUserDao;

	@Autowired
	public TsmpAuthorizationService(DgrAcIdpUserDao dgrAcIdpUserDao) {
		super();
		this.dgrAcIdpUserDao = dgrAcIdpUserDao;
	}

	public String  getIdTokenJwtstr(String userName, String idpType) {
		if(StringUtils.hasText(idpType) && StringUtils.hasText(userName)) {
			if(userName.toLowerCase().indexOf("b64.") == 0) {
				String[] arrUserName = userName.split("\\.");
				if(arrUserName.length >= 4) {
					DgrAcIdpUser vo = getDgrAcIdpUserDao().findFirstByUserNameAndIdpType(arrUserName[3], idpType);
					if(vo != null) {
						return vo.getIdTokenJwtstr();
					}
				}
			}
		}
		logger.debug("userName = " + userName + ", idpType = "+ idpType);
		return null;
	}

	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return dgrAcIdpUserDao;
	}

}

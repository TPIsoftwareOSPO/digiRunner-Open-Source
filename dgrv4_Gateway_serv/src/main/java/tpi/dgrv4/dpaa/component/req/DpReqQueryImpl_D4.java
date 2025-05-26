package tpi.dgrv4.dpaa.component.req;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd4;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd4Dao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdermDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdersDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DpReqQueryImpl_D4 extends DpReqQueryAbstract<DpReqQueryResp_D4> //
	implements DpReqQueryIfs<DpReqQueryResp_D4> {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpReqOrderd4Dao tsmpDpReqOrderd4Dao;
	private TsmpUserDao tsmpUserDao;

	@Autowired
	public DpReqQueryImpl_D4(TsmpDpReqOrdermDao tsmpDpReqOrdermDao, TsmpDpReqOrdersDao tsmpDpReqOrdersDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, TsmpOrganizationDao tsmpOrganizationDao,
			TsmpUserDao tsmpUserDao, TsmpClientDao tsmpClientDao, TsmpDpFileDao tsmpDpFileDao,
			DpReqServiceFactory dpReqServiceFactory, TsmpDpReqOrderd4Dao tsmpDpReqOrderd4Dao) {
		super(tsmpDpReqOrdermDao, tsmpDpReqOrdersDao, tsmpDpItemsCacheProxy, tsmpOrganizationDao, tsmpUserDao,
				tsmpClientDao, tsmpDpFileDao, dpReqServiceFactory);
		this.tsmpDpReqOrderd4Dao = tsmpDpReqOrderd4Dao;
		this.tsmpUserDao = tsmpUserDao;
	}

	@Override
	protected List<DpReqQueryResp_D4> doQueryDetail(Long reqOrdermId, String locale) {
		// 利用主檔流水號找出明細檔
		List<TsmpDpReqOrderd4> d4List = getTsmpDpReqOrderd4Dao().findByRefReqOrdermId(reqOrdermId);
		if (d4List == null || d4List.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<DpReqQueryResp_D4> d4RespList = new ArrayList<>();
		DpReqQueryResp_D4 d4Resp = null;
		String userName = null;
		for (TsmpDpReqOrderd4 d4 : d4List) {
			userName = getUserName( d4.getUserId() );	// 用使用者ID關聯出使用者名稱
			d4Resp = new DpReqQueryResp_D4();
			d4Resp.setUserId(d4.getUserId());
			d4Resp.setUserName(userName);
			d4Resp.setArticle(d4.getArticle());
			d4RespList.add(d4Resp);
		}
		return d4RespList;
	}

	private String getUserName(String userId) {
		Optional<TsmpUser> opt = getTsmpUserDao().findById(userId);
		if (!opt.isPresent()) {
			return new String();
		}
		return opt.get().getUserName();
	}

	@Override
	protected TsmpMailEvent getTsmpMailEvent(String userId, String recipients, TsmpAuthorization auth,
			DpReqQueryResp<DpReqQueryResp_D4> resp) {
		/* 此為開發範例，不寄送郵件
		String subject = "信件主旨";
		String content = "<h2>信件內容</h2>";
		
		return new TsmpMailEventBuilder() //
		.setSubject(subject)
		.setContent(content)
		.setRecipients(recipients)
		.setCreateUser(auth.getUserName())
		.setRefCode("郵件範本代碼")	// reference: tsmp_dp_mail_tplt.code
		.build();
		*/
		return null;
	}

	protected TsmpDpReqOrderd4Dao getTsmpDpReqOrderd4Dao() {
		return this.tsmpDpReqOrderd4Dao;
	}

	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}

}
	
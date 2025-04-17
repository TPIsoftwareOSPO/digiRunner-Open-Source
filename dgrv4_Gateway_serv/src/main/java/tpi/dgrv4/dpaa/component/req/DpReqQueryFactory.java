package tpi.dgrv4.dpaa.component.req;

import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpReqReviewType;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdermDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class DpReqQueryFactory {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpReqOrdermDao tsmpDpReqOrdermDao;
	private DpReqQueryIfs<DpReqQueryResp_D1> dpReqQueryD1;
	private DpReqQueryIfs<DpReqQueryResp_D2> dpReqQueryD2;
	private DpReqQueryIfs<DpReqQueryResp_D3> dpReqQueryD3;
	private DpReqQueryIfs<DpReqQueryResp_D4> dpReqQueryD4;
	private DpReqQueryIfs<DpReqQueryResp_D5> dpReqQueryD5;

	@Autowired
	public DpReqQueryFactory(TsmpDpReqOrdermDao tsmpDpReqOrdermDao, DpReqQueryIfs<DpReqQueryResp_D1> dpReqQueryD1,
			DpReqQueryIfs<DpReqQueryResp_D2> dpReqQueryD2, DpReqQueryIfs<DpReqQueryResp_D3> dpReqQueryD3,
			DpReqQueryIfs<DpReqQueryResp_D4> dpReqQueryD4, DpReqQueryIfs<DpReqQueryResp_D5> dpReqQueryD5) {
		super();
		this.tsmpDpReqOrdermDao = tsmpDpReqOrdermDao;
		this.dpReqQueryD1 = dpReqQueryD1;
		this.dpReqQueryD2 = dpReqQueryD2;
		this.dpReqQueryD3 = dpReqQueryD3;
		this.dpReqQueryD4 = dpReqQueryD4;
		this.dpReqQueryD5 = dpReqQueryD5;
	}

	/**
	 * @param reqOrdermId
	 * @param exceptionSupplier
	 * @return
	 * @throws X 有傳入 reqOrdermId, 但找不到申請單時所拋出的錯誤
	 */
	public <X extends TsmpDpAaException> DpReqQueryIfs<?> getDpReqQuery( //
			Long reqOrdermId, Supplier<? extends X> exceptionSupplier) throws X {
		String reqType = null;
		if (reqOrdermId != null) {
			Optional<TsmpDpReqOrderm> opt_m = getTsmpDpReqOrdermDao().findById(reqOrdermId);
			if (opt_m.isPresent()) {
				reqType = opt_m.get().getReqType();
			} else {
				throw exceptionSupplier.get();
			}
		}

		if (TsmpDpReqReviewType.API_APPLICATION.isValueEquals(reqType)) {
			return getDpReqQueryD1();
		} else if (TsmpDpReqReviewType.API_ON_OFF.isValueEquals(reqType)) {
			return getDpReqQueryD2();
		} else if (TsmpDpReqReviewType.CLIENT_REG.isValueEquals(reqType)) {
			return getDpReqQueryD3();
		} else if (TsmpDpReqReviewType.THINKPOWER_ARTICLE.isValueEquals(reqType)) {
			return getDpReqQueryD4();
		} else if (TsmpDpReqReviewType.OPEN_API_KEY.isValueEquals(reqType)) {
			return getDpReqQueryD5();
		}
		this.logger.error(String.format("Unknown reqType: %s", reqType));
		throw TsmpDpAaRtnCode.NO_ITEMS_DATA.throwing();
	}

	protected TsmpDpReqOrdermDao getTsmpDpReqOrdermDao() {
		return this.tsmpDpReqOrdermDao;
	}

	protected DpReqQueryIfs<DpReqQueryResp_D1> getDpReqQueryD1() {
		return this.dpReqQueryD1;
	}

	protected DpReqQueryIfs<DpReqQueryResp_D2> getDpReqQueryD2() {
		return this.dpReqQueryD2;
	}

	protected DpReqQueryIfs<DpReqQueryResp_D3> getDpReqQueryD3() {
		return this.dpReqQueryD3;
	}

	protected DpReqQueryIfs<DpReqQueryResp_D4> getDpReqQueryD4() {
		return this.dpReqQueryD4;
	}
	
	protected DpReqQueryIfs<DpReqQueryResp_D5> getDpReqQueryD5() {
		return this.dpReqQueryD5;
	}

}

package tpi.dgrv4.dpaa.component.scheduledEnableDisable;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.constant.TsmpDpItem;
import tpi.dgrv4.dpaa.vo.AA0303Item;
import tpi.dgrv4.entity.entity.TsmpApi;

@Component
public class RemoveIllegalAPIs1557 extends Handler implements ApiPublicFlagHandlerInterface {

	private ApiPublicFlagHandlerInterface next;

	@Override
	public void setNext(ApiPublicFlagHandlerInterface next) {
		this.next = next;
	}

	@Override
	public ApiListAndAlgorithmClassifier handle(ApiPublicFlagHandlerData data, ApiListAndAlgorithmClassifier classifier,
			TsmpApi api) {

		String apiStatus = data.getApiStatus();
		long apiDisableScheduledDate = data.getApiDisableScheduledDate();
		long enableScheduledDate = data.getEnableScheduledDate();

		boolean isGreater = isFirstNumberGreaterThanOrEqualSecondNumber(enableScheduledDate, apiDisableScheduledDate);

		if (TsmpDpItem.isEqualParam1(TsmpDpItem.ENABLE_FLAG_DEACTIVATE, apiStatus) && isGreater
				&& apiDisableScheduledDate != 0) {

			throw TsmpDpAaRtnCode._1557.throwing();

//			List<TsmpApi> ls = getApiList(this.getClass().getSimpleName(), classifier);
//
//			ls.add(api);
//
//			classifier.setAlgorithm(tsmpApi -> {
//			});
//
//			return classifier;
		}

		return next.handle(data, classifier, api);
	}

	@Override
	public List<AA0303Item> getAA0303ItemRespList(List<TsmpApi> apis, String locale) {
		List<AA0303Item> result = new ArrayList<>();

		for (TsmpApi api : apis) {

			AA0303Item resp = getAA0303Item(api, locale);

			resp.setErrMsg(getErrMsg(TsmpDpAaRtnCode._1557, locale));
			resp.setProcessResult(false);

			result.add(resp);
		}

		return result;
	}

}

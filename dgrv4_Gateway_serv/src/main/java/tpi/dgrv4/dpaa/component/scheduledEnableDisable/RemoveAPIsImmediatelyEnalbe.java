package tpi.dgrv4.dpaa.component.scheduledEnableDisable;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import tpi.dgrv4.dpaa.vo.AA0303Item;
import tpi.dgrv4.entity.entity.TsmpApi;

@Component
public class RemoveAPIsImmediatelyEnalbe extends Handler implements ApiPublicFlagHandlerInterface {

	private ApiPublicFlagHandlerInterface next;

	@Override
	public void setNext(ApiPublicFlagHandlerInterface next) {
		this.next = next;
	}

	@Override
	public ApiListAndAlgorithmClassifier handle(ApiPublicFlagHandlerData data, ApiListAndAlgorithmClassifier classifier,
			TsmpApi api) {

		long enableScheduledDate = data.getEnableScheduledDate();
		boolean isEnableScheduledDate = enableScheduledDate == 0;

		if (isEnableScheduledDate) {

			String status = data.getStatus();

			List<TsmpApi> ls = getApiList(this.getClass().getSimpleName(), classifier);

			ls.add(api);
			classifier.setAlgorithm((tsmpApi) -> getEnableAlgorithm().getImmediatelyEnableAlgorithm(tsmpApi, status));

			return classifier;
		}

		return next.handle(data, classifier, api);
	}

	@Override
	public List<AA0303Item> getAA0303ItemRespList(List<TsmpApi> apis, String locale) {

		List<AA0303Item> result = new ArrayList<>();

		for (TsmpApi api : apis) {

			AA0303Item resp = getAA0303Item(api, locale);

			resp.setErrMsg("");
			resp.setProcessResult(true);

			result.add(resp);
		}

		return result;
	}

}

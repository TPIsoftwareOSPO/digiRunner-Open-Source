package tpi.dgrv4.dpaa.component.scheduledEnableDisable;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import tpi.dgrv4.dpaa.vo.AA0303Item;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Component
public class PassDisableDetectApis extends Handler implements ApiPublicFlagHandlerInterface {

	private TPILogger logger = TPILogger.tl;

	@Override
	public void setNext(ApiPublicFlagHandlerInterface next) {
		logger.error("setNext method is not supported.");
		throw new UnsupportedOperationException("setNext is not supported in PassEnableDetectApis");
	}

	@Override
	public ApiListAndAlgorithmClassifier handle(ApiPublicFlagHandlerData data, ApiListAndAlgorithmClassifier classifier,
			TsmpApi api) {

		long disableScheduledDate = data.getDisableScheduledDate();

		List<TsmpApi> ls = getApiList(this.getClass().getSimpleName(), classifier);

		ls.add(api);

		classifier.setAlgorithm( //
				(tsmpApi) -> getDisableAlgorithm() //
						.getScheduledDisableAlgorithm(tsmpApi, disableScheduledDate));

		return classifier;
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

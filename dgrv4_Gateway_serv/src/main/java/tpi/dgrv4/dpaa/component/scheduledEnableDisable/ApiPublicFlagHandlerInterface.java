package tpi.dgrv4.dpaa.component.scheduledEnableDisable;

import java.util.List;

import tpi.dgrv4.dpaa.vo.AA0303Item;
import tpi.dgrv4.entity.entity.TsmpApi;

public interface ApiPublicFlagHandlerInterface {

	void setNext(ApiPublicFlagHandlerInterface next);

	ApiListAndAlgorithmClassifier handle(ApiPublicFlagHandlerData data, ApiListAndAlgorithmClassifier classifier,
			TsmpApi api);

	List<AA0303Item> getAA0303ItemRespList(List<TsmpApi> apis, String locale);

}

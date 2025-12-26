package tpi.dgrv4.common.component.validator;

public class BeforeControllerRespItemBuilderSelector {

	public BeforeControllerRespItemIntBuilder buildInt(String locale) {
		return new BeforeControllerRespItemIntBuilder(locale);
	}

	public BeforeControllerRespItemStringBuilder buildString(String locale) {
		return new BeforeControllerRespItemStringBuilder(locale);
	}

	public BeforeControllerRespItemCollectionBuilder buildCollection(String locale) {
		return new BeforeControllerRespItemCollectionBuilder(locale);
	}

	public BeforeControllerRespItemMapBuilder buildMap(String locale) {
		return new BeforeControllerRespItemMapBuilder(locale);
	}

}
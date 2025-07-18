package tpi.dgrv4.dpaa.constant;

import lombok.Getter;

@Getter
public enum ExportField {
    API_NAME("API_NAME"),
    MODULE_NAME("MODULE_NAME"),
    API_ID("API_ID"),
    API_STATUS("API_STATUS"),
    API_SRC("API_SRC"),
    API_CACHE("API_CACHE"),
    HTTP_METHODS("HTTP_METHODS"),
    NO_AUTH("NO_AUTH"),
    ORG_ID("ORG_ID"),
    SRC_URL("SRC_URL"),
    API_DESC("API_DESC"),
    JWT("JWT"),
    LABEL("LABEL"),
    ENABLE_SCHEDULED_DATE("ENABLE_SCHEDULED_DATE"),
    DISABLE_SCHEDULED_DATE("DISABLE_SCHEDULED_DATE"),
    CREATE_USER("CREATE_USER"),
    UPDATE_USER("UPDATE_USER"),
    CREATE_TIME("CREATE_TIME"),
    UPDATE_TIME("UPDATE_TIME");

    private final String code;

    ExportField(String code) {
        this.code = code;
    }

}

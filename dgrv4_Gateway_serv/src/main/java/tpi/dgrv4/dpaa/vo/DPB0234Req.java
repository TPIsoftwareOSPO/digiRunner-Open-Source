package tpi.dgrv4.dpaa.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

import java.util.Arrays;
import java.util.List;

public class DPB0234Req extends ReqValidator {
    private String keyWords;
    @JsonProperty("xApiKey")
    private String xApiKey;
    private String flag;

    public String getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(String keyWords) {
        this.keyWords = keyWords;
    }

    public String getxApiKey() {
        return xApiKey;
    }

    public void setxApiKey(String xApiKey) {
        this.xApiKey = xApiKey;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    @Override
    public String toString() {
        return "DPB0234Req{" +
                "keyWords='" + keyWords + '\'' +
                ", xApiKey='" + xApiKey + '\'' +
                ", flag='" + flag + '\'' +
                '}';
    }

    @Override
    protected List<BeforeControllerRespItem> provideConstraints(String locale) {
        return Arrays.asList(new BeforeControllerRespItem[] { //
                new BeforeControllerRespItemBuilderSelector() //
                        .buildString(locale) //
                        .field("flag") //
                        .maxLength(10) //
                        .isRequired() //
                        .build(), //
        });
    }
}

package tpi.dgrv4.dpaa.vo;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

import java.util.Arrays;
import java.util.List;

public class DPB0291Req extends ReqValidator {

    private List<String> proxyIds;
    private String enable;

    public List<String> getProxyIds() {
        return proxyIds;
    }

    public void setProxyIds(List<String> proxyIds) {
        this.proxyIds = proxyIds;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    @Override
    public String toString() {
        return "DPB0291Req{" +
                "proxyIds=" + proxyIds +
                ", enable='" + enable + '\'' +
                '}';
    }

    @Override
    protected List<BeforeControllerRespItem> provideConstraints(String locale) {
        return Arrays.asList(new BeforeControllerRespItem[] { //
                new BeforeControllerRespItemBuilderSelector() //
                        .buildString(locale) //
                        .field("enable") //
                        .isRequired() //
                        .build()
        });

    }
}
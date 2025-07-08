package tpi.dgrv4.dpaa.vo;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

import java.util.Arrays;
import java.util.List;

public class DPB0293Req extends ReqValidator {
    private String gRPCProxyMapId;

    public String getgRPCProxyMapId() {
        return gRPCProxyMapId;
    }

    public void setgRPCProxyMapId(String gRPCProxyMapId) {
        this.gRPCProxyMapId = gRPCProxyMapId;
    }

    @Override
    protected List<BeforeControllerRespItem> provideConstraints(String locale) {
        return Arrays.asList(new BeforeControllerRespItem[] { //

                new BeforeControllerRespItemBuilderSelector() //
                        .buildString(locale) //
                        .field("gRPCProxyMapId") //
                        .isRequired() //
                        .build()
        });

    }
}

package tpi.dgrv4.dpaa.vo;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

import java.util.Arrays;
import java.util.List;

public class DPB0231Req extends ReqValidator {
    private String host;
    private Integer port;
    private String rootCa;
    private String clientCert;
    private String clientKey;
    private String keyMima;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getRootCa() {
        return rootCa;
    }

    public void setRootCa(String rootCa) {
        this.rootCa = rootCa;
    }

    public String getClientCert() {
        return clientCert;
    }

    public void setClientCert(String clientCert) {
        this.clientCert = clientCert;
    }

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public String getKeyMima() {
        return keyMima;
    }

    public void setKeyMima(String keyMima) {
        this.keyMima = keyMima;
    }

    @Override
    protected List<BeforeControllerRespItem> provideConstraints(String locale) {
        return Arrays.asList(new BeforeControllerRespItem[] {
                new BeforeControllerRespItemBuilderSelector()
                        .buildString(locale)
                        .field("host")
                        .isRequired()
                        .build(),
                new BeforeControllerRespItemBuilderSelector()
                        .buildInt(locale)
                        .field("port")
                        .isRequired()
                        .build(),
                new BeforeControllerRespItemBuilderSelector()
                        .buildString(locale)
                        .field("rootCa")
                        .isRequired()
                        .build(),
                new BeforeControllerRespItemBuilderSelector()
                        .buildString(locale)
                        .field("clientCert")
                        .isRequired()
                        .build(),
                new BeforeControllerRespItemBuilderSelector()
                        .buildString(locale)
                        .field("clientKey")
                        .isRequired()
                        .build(),
                new BeforeControllerRespItemBuilderSelector()
                        .buildString(locale)
                        .field("keyMima")
                        .maxLength(2000)
                        .build(),


        });
    }
}

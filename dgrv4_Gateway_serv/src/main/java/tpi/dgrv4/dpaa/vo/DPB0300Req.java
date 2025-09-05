package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

@Getter
@Setter
@Accessors(chain = true)
public class DPB0300Req extends ReqValidator {

    private String targetHostName;       // 目標主機名
    private String targetPort;           // 目標端口
    private String connectTimeoutMs;     // 連接超時時間（毫秒）
    private String secureMode;           // 安全模式 (AUTO, SECURE, PLAINTEXT)
    private String autoTrustUpstreamCerts; // 是否自動信任上游證書 (Y/N)
    private String trustedCertsContent;    // 信任的 CA 證書內容 (PEM 格式)
    

    @Override
    protected List<BeforeControllerRespItem> provideConstraints(String locale) {
        return Arrays.asList(new BeforeControllerRespItem[] { //

                new BeforeControllerRespItemBuilderSelector() //
                        .buildString(locale) //
                        .field("targetHostName") //
                        .isRequired() //
                        .build(), //

                new BeforeControllerRespItemBuilderSelector() //
                        .buildString(locale) //
                        .field("targetPort") //
                        .isRequired() //
                        .build(), //

                new BeforeControllerRespItemBuilderSelector() //
                        .buildString(locale) //
                        .field("connectTimeoutMs") //
                        .isRequired() //
                        .build(), //

                new BeforeControllerRespItemBuilderSelector() //
                        .buildString(locale) //
                        .field("secureMode") //
                        .build(), //

        });

    }
}
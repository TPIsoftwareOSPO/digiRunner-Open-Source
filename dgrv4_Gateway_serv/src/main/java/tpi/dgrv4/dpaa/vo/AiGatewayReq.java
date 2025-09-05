package tpi.dgrv4.dpaa.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;
import tpi.dgrv4.dpaa.service.AiGatewayService.Pagination;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AiGatewayReq {

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class AiApiKeyReq extends ReqValidator {
        protected String aiApiKeyName;
        protected Long aiProviderId;
        protected String aiApiKeyCode;
        protected Long usageLimitInputToken;
        protected Long usageLimitOutputToken;
        protected String usageLimitPolicy;
//        protected Long backupKeyId;
        protected String aiApiKeyEnable;

        @Override
        protected List<BeforeControllerRespItem> provideConstraints(String locale) {
            return Collections.emptyList();
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class AiProviderReq extends ReqValidator {
        String aiProviderId;
        String aiProviderName;
        String aiProviderAlias;
        String aiModel;
        String generateAPI;
        String countTokenAPI;
        String aiProviderEnabled;

        @Override
        protected List<BeforeControllerRespItem> provideConstraints(String locale) {
            return Collections.emptyList();
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class DPB0251Req extends AiProviderReq { }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class DPB0253Req extends AiProviderReq { }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class DPB0256Req extends AiApiKeyReq { }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class DPB0258Req extends AiApiKeyReq { }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class DPB0261Req extends ReqValidator {
        String aiPromptInput;
        Long aiApiKeyId;

        @Override
        protected List<BeforeControllerRespItem> provideConstraints(String locale) {
            return Collections.emptyList();
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class AiPromptTemplateReq extends ReqValidator {
        protected String aiPromptTemplateName;
        protected String aiPromptTemplateContent;
        protected String aiPromptTemplateEnable;
        protected String aiPromptTemplateRemark;


        @Override
        protected List<BeforeControllerRespItem> provideConstraints(String locale) {
            return Collections.emptyList();
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class PageReq extends ReqValidator {

        protected Pagination pagination = new Pagination();

        @Override
        protected List<BeforeControllerRespItem> provideConstraints(String locale) {
            return Collections.emptyList();
        }
    }


    public static class DPB0250Req extends PageReq {}
    public static class DPB0255Req extends PageReq {}
    public static class DPB0260Req extends PageReq {}
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class DPB0262Req extends PageReq {
        String aiPromptTemplateEnable;
        String aiPromptTemplateName;
    }
    public static class DPB02671 extends PageReq {}




    public static class DPB0263Req extends AiPromptTemplateReq { }


    public static class DPB0265Req extends AiPromptTemplateReq { }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class DPB0267Req extends ReqValidator {
        String aiApiKeyConsumerType;
        String aiApiKeyConsumerId;
        Long aiPromptTemplateId;

        @Override
        protected List<BeforeControllerRespItem> provideConstraints(String locale) {
            return Collections.emptyList();
        }
    }
}

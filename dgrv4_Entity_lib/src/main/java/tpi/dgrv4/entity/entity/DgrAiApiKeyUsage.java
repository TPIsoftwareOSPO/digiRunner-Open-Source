package tpi.dgrv4.entity.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.dgrSeq.DgrSeq;

import java.util.Date;

@Builder
@Data
@Entity
@Table(name = "dgr_ai_apikey_usage")
@NoArgsConstructor
@AllArgsConstructor
public class DgrAiApiKeyUsage implements DgrSequenced {

    @JsonSerialize(using = ToStringSerializer.class)
    @Id
    @DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
    @Column(name = "ai_apikey_usage_id")
    private Long id;

    @Column(name = "ai_apikey_consumer_type")
    private String aiApikeyConsumerType;

    @Column(name = "ai_apikey_consumer_id")
    private String aiApikeyConsumerId;

    @ManyToOne
    @JoinColumn(name = "ai_apikey_id", referencedColumnName = "ai_apikey_id")
    private DgrAiApiKey dgrAiApiKey;

    @JsonSerialize(using = ToStringSerializer.class)
    @Column(name = "requst_ts")
    private Long requstTs;

    @JsonSerialize(using = ToStringSerializer.class)
    @Column(name = "input_token_count")
    private Long inputTokenCount;

    @JsonSerialize(using = ToStringSerializer.class)
    @Column(name = "output_token_count")
    private Long outputTokenCount;

//    @Column(name = "ai_prompt_template_id")
//    private Long aiPromptTemplateId;

    @Builder.Default
    @Column(name = "create_date_time")
    private Date createDateTime = DateTimeUtil.now();

    @ManyToOne
    @JoinColumn(name = "ai_prompt_template_id", referencedColumnName = "ai_prompt_template_id")
    private DgrAiPromptTemplate dgrAiPromptTemplate;

    @Column(name = "http_transaction_status")
    private String httpTransactionStatus;

    @Column(name = "ai_usage_prompt_input")
    private String aiUsagePromptInput;

    @Column(name = "ai_usage_prompt_output")
    private String aiUsagePromptOutput;

    @Override
    public Long getPrimaryKey() {
        return id;
    }
}

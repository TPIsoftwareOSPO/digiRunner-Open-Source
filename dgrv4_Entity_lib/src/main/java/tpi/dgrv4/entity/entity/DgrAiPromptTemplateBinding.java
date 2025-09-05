package tpi.dgrv4.entity.entity;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.entity.component.dgrSeq.DgrSeq;

@Builder
@Data
@Entity
@Table(name = "dgr_ai_prompt_template_binding")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DgrAiPromptTemplateBinding implements DgrSequenced {

    @JsonSerialize(using = ToStringSerializer.class)
    @Id
    @DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
    @Column(name = "ai_consumer_prompt_template_binding_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ai_prompt_template_id", referencedColumnName = "ai_prompt_template_id")
    private DgrAiPromptTemplate dgrAiPromptTemplate;

    @Column(name = "ai_apikey_consumer_type")
    private String aiApikeyConsumerType;

    @Column(name = "ai_apikey_consumer_id")
    private String aiApikeyConsumerId;

    @Override
    public Long getPrimaryKey() {
        return id;
    }
}

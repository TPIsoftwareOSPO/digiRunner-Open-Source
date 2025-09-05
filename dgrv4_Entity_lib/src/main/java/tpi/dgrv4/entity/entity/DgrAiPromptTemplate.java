package tpi.dgrv4.entity.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.entity.component.dgrSeq.DgrSeq;

import java.util.List;

@Builder
@Data
@Entity
@Table(name = "dgr_ai_prompt_template")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DgrAiPromptTemplate implements DgrSequenced {

    @JsonSerialize(using = ToStringSerializer.class)
    @Id
    @DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
    @Column(name = "ai_prompt_template_id")
    private Long id;

    @Column(name = "ai_prompt_template_name")
    private String aiPromptTemplateName;

    @Column(name = "ai_prompt_template_content")
    private String aiPromptTemplateContent;

    @Column(name = "ai_prompt_template_enable")
    private String aiPromptTemplateEnable;

    @Column(name = "ai_prompt_template_remark")
    private String aiPromptTemplateRemark;

    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "dgrAiPromptTemplate", fetch = FetchType.EAGER)
    private List<DgrAiPromptTemplateBinding> dgrAiPromptTemplateBindings;

    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "dgrAiPromptTemplate", fetch = FetchType.EAGER)
    private List<DgrAiApiKeyUsage> dgrAiApiKeyUsages;

    @Override
    public Long getPrimaryKey() {
        return id;
    }
}

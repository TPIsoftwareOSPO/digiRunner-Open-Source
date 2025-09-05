package tpi.dgrv4.entity.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.persistence.*;
import lombok.*;


import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.dgrSeq.DgrSeq;

import java.util.Date;
import java.util.List;

@Builder
@Data
@Entity
@Table(name = "dgr_ai_provider")
@NoArgsConstructor
@AllArgsConstructor
public class DgrAiProvider implements DgrSequenced {

    @JsonSerialize(using = ToStringSerializer.class)
    @Id
    @DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
    @Column(name = "ai_provider_id")
    @JsonProperty("aiProviderId")
    private Long id;

    @Column(name = "ai_provider_name")
    private String aiProviderName;

    @Column(name = "ai_provider_alias")
    private String aiProviderAlias;

    @Column(name = "ai_model")
    private String aiModel;

    @Column(name = "generate_api")
    private String generateApi;

    @Column(name = "count_token_api")
    private String countTokenApi;

    @Builder.Default
    @Column(name = "create_date_time")
    private Date createDateTime = DateTimeUtil.now();

    @Builder.Default
    @Column(name = "create_user")
    private String createUser = "SYSTEM";

    @Column(name = "update_date_time")
    private Date updateDateTime;

    @Column(name = "update_user")
    private String updateUser;

    @JsonSerialize(using = ToStringSerializer.class)
    @Builder.Default
    @Column(name = "version")
    private Long version = 1L;

    @Builder.Default
    @Column(name = "ai_provider_enable")
    private String aiProviderEnable = "Y";

    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "dgrAiProvider", fetch = FetchType.EAGER)
    private List<DgrAiApiKey> dgrAiApiKeys; // 1 個 provider 對應多個 apikey


    @JsonProperty("aiApiKeysCount")
    public int getAiApiKeyCount() {
        return dgrAiApiKeys != null ? dgrAiApiKeys.size(): 0;
    }

    @Override
    public Long getPrimaryKey() {
        return id;
    }
}

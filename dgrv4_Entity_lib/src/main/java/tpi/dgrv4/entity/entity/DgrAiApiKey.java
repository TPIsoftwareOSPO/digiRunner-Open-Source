package tpi.dgrv4.entity.entity;



import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.dgrSeq.DgrSeq;

import java.util.Date;
import java.util.List;

@Builder
@Data
@Entity
@Table(name = "dgr_ai_apikey")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DgrAiApiKey implements DgrSequenced {

    @JsonSerialize(using = ToStringSerializer.class)
    @Id
    @DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
    @Column(name = "ai_apikey_id")
    private Long id;

    @Column(name = "ai_apikey_name")
    private String aiApikeyName;


    @ManyToOne
    @JoinColumn(name = "ai_provider_id", referencedColumnName = "ai_provider_id")
    private DgrAiProvider dgrAiProvider; // 多個 api key 可能屬於一個 provider

    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "dgrAiApiKey", fetch = FetchType.EAGER)
    private List<DgrAiApiKeyUsage> dgrAiApiKeyUsages; // 1 個 key 對應多個 usage

    @Column(name = "ai_apikey_code")
    private String aiApikeyCode;

    @JsonSerialize(using = ToStringSerializer.class)
    @Builder.Default
    @Column(name = "usage_limit_input_token")
    private Long usageLimitInputToken = 0L;

    @JsonSerialize(using = ToStringSerializer.class)
    @Builder.Default
    @Column(name = "usage_limit_output_token")
    private Long usageLimitOutputToken = 0L;

    @Column(name = "usage_limit_policy")
    private String usageLimitPolicy;

    @JsonSerialize(using = ToStringSerializer.class)
    @Builder.Default
    @Column(name = "usage_input_token_count")
    private Long usageInputTokenCount = 0L;

    @JsonSerialize(using = ToStringSerializer.class)
    @Builder.Default
    @Column(name = "usage_output_token_count")
    private Long usageOutputTokenCount = 0L;

//    @Column(name = "backup_key_id")
//    private Long backupKeyId;

    @Column(name = "ai_apikey_enable")
    private String aiApikeyEnable;

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

    @Override
    public Long getPrimaryKey() {
        return id;
    }
}

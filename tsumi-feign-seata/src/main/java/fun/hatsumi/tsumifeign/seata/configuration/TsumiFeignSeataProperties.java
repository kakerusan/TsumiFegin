package fun.hatsumi.tsumifeign.seata.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TsumiFeign Seata 配置属性
 *
 * @author kakeru
 */
@Data
@ConfigurationProperties(prefix = "tsumifeign.seata")
public class TsumiFeignSeataProperties {

    /**
     * 是否启用Seata事务传播（默认true）
     */
    private boolean enabled = true;

    /**
     * XID传播的HTTP Header名称（默认TX_XID）
     */
    private String xidHeaderName = "TX_XID";

    /**
     * 分支类型Header名称（默认TX_BRANCH_TYPE）
     */
    private String branchTypeHeaderName = "TX_BRANCH_TYPE";

    /**
     * 是否在日志中记录XID（默认false，避免日志泄露）
     */
    private boolean logXid = false;

    /**
     * 超时时间（毫秒），默认30秒
     */
    private long timeout = 30000;
}

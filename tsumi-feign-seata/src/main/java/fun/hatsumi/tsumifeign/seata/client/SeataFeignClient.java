package fun.hatsumi.tsumifeign.seata.client;

import fun.hatsumi.tsumifeign.client.FeignClient;
import fun.hatsumi.tsumifeign.core.RequestTemplate;
import fun.hatsumi.tsumifeign.core.Response;
import fun.hatsumi.tsumifeign.seata.configuration.TsumiFeignSeataProperties;
import io.seata.core.context.RootContext;
import io.seata.core.model.BranchType;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 支持 Seata 事务传播的 Feign 客户端
 * 在HTTP请求中传播全局事务XID，确保下游服务加入当前全局事务
 *
 * @author kakeru
 */
@Slf4j
public class SeataFeignClient implements FeignClient {

    private final FeignClient delegate;
    private final TsumiFeignSeataProperties properties;

    public SeataFeignClient(FeignClient delegate, TsumiFeignSeataProperties properties) {
        this.delegate = delegate;
        this.properties = properties;
    }

    @Override
    public Response execute(RequestTemplate requestTemplate) throws IOException {
        // 1. 获取当前全局事务XID
        String xid = RootContext.getXID();

        // 2. 如果存在XID，添加到请求头
        if (xid != null && !xid.isEmpty()) {
            requestTemplate.addHeader(properties.getXidHeaderName(), xid);
            
            if (properties.isLogXid()) {
                log.debug("Propagating Seata XID in header: {} = {}", 
                         properties.getXidHeaderName(), xid);
            }
        }

        // 3. 传播分支类型（AT/TCC/SAGA）
        BranchType branchType = RootContext.getBranchType();
        if (branchType != null) {
            requestTemplate.addHeader(properties.getBranchTypeHeaderName(), branchType.name());
            
            if (properties.isLogXid()) {
                log.debug("Propagating Seata branch type in header: {} = {}", 
                         properties.getBranchTypeHeaderName(), branchType.name());
            }
        }

        // 4. 执行实际请求
        try {
            Response response = delegate.execute(requestTemplate);
            
            // 5. 检查响应状态
            if (response.getStatus() >= 500 && properties.isLogXid()) {
                log.warn("Seata transaction request returned server error: status={}, xid={}", 
                        response.getStatus(), xid);
            }
            
            return response;
            
        } catch (IOException e) {
            // 记录事务异常信息
            if (properties.isLogXid()) {
                log.error("Seata transaction request failed, xid: {}", xid, e);
            } else {
                log.error("Seata transaction request failed", e);
            }
            throw e;
        }
    }
}

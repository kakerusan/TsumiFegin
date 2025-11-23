package fun.hatsumi.tsumifeign.seata.interceptor;

import fun.hatsumi.tsumifeign.seata.configuration.TsumiFeignSeataProperties;
import io.seata.core.context.RootContext;
import io.seata.core.model.BranchType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Seata 事务拦截器
 * 在服务端接收请求时，从Header中提取XID并绑定到当前线程
 * 确保本地事务能够作为分支事务加入全局事务
 *
 * @author kakeru
 */
@Slf4j
public class SeataTransactionInterceptor implements HandlerInterceptor {

    private final TsumiFeignSeataProperties properties;

    public SeataTransactionInterceptor(TsumiFeignSeataProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        // 1. 从Header中提取XID
        String xid = request.getHeader(properties.getXidHeaderName());

        // 2. 绑定到当前线程上下文
        if (xid != null && !xid.isEmpty()) {
            RootContext.bind(xid);
            
            if (properties.isLogXid()) {
                log.debug("Bound Seata XID to context: {}", xid);
            } else {
                log.debug("Bound Seata XID to context");
            }
        }

        // 3. 提取分支类型
        String branchType = request.getHeader(properties.getBranchTypeHeaderName());
        if (branchType != null && !branchType.isEmpty()) {
            try {
                RootContext.bindBranchType(BranchType.valueOf(branchType));
                
                if (properties.isLogXid()) {
                    log.debug("Bound Seata branch type to context: {}", branchType);
                }
            } catch (IllegalArgumentException e) {
                log.warn("Invalid branch type: {}", branchType);
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        // 清理事务上下文
        String xid = RootContext.getXID();
        if (xid != null && !xid.isEmpty()) {
            RootContext.unbind();
            
            if (properties.isLogXid()) {
                log.debug("Unbound Seata XID from context: {}", xid);
            } else {
                log.debug("Unbound Seata XID from context");
            }
        }
        
        // 清理分支类型
        RootContext.unbindBranchType();
    }
}

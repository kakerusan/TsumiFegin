package fun.hatsumi.tsumifeign.sentinel.rule;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import fun.hatsumi.tsumifeign.sentinel.configuration.TsumiFeignSentinelProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 规则配置器
 * 初始化流控规则和熔断规则
 *
 * @author kakeru
 */
@Slf4j
@Component
public class SentinelRuleConfigurator implements InitializingBean {

    private final TsumiFeignSentinelProperties properties;

    public SentinelRuleConfigurator(TsumiFeignSentinelProperties properties) {
        this.properties = properties;
    }

    @Override
    public void afterPropertiesSet() {
        // 初始化规则
        if (properties.isEnabled()) {
            initFlowRules();
            initDegradeRules();
        }
    }

    /**
     * 初始化流控规则
     */
    private void initFlowRules() {
        if (!properties.getFlowControl().isEnabled()) {
            log.info("Flow control is disabled");
            return;
        }

        List<FlowRule> rules = new ArrayList<>();
        
        // 默认的全局流控规则
        FlowRule defaultRule = new FlowRule();
        defaultRule.setResource("default");
        defaultRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        defaultRule.setCount(properties.getFlowControl().getQpsThreshold());
        rules.add(defaultRule);

        FlowRuleManager.loadRules(rules);
        log.info("Initialized flow control rules: QPS threshold = {}", 
                properties.getFlowControl().getQpsThreshold());
    }

    /**
     * 初始化熔断规则
     */
    private void initDegradeRules() {
        if (!properties.getCircuitBreaker().isEnabled()) {
            log.info("Circuit breaker is disabled");
            return;
        }

        List<DegradeRule> rules = new ArrayList<>();

        // 慢调用比例熔断
        DegradeRule slowCallRule = new DegradeRule();
        slowCallRule.setResource("default");
        slowCallRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        slowCallRule.setCount(properties.getCircuitBreaker().getSlowCallDuration());
        slowCallRule.setSlowRatioThreshold(properties.getCircuitBreaker().getSlowRatioThreshold());
        slowCallRule.setMinRequestAmount(properties.getCircuitBreaker().getMinRequestAmount());
        slowCallRule.setTimeWindow((int) (properties.getCircuitBreaker().getBreakerDuration() / 1000));
        rules.add(slowCallRule);

        // 异常比例熔断
        DegradeRule errorRatioRule = new DegradeRule();
        errorRatioRule.setResource("default");
        errorRatioRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        errorRatioRule.setCount(properties.getCircuitBreaker().getErrorRatioThreshold());
        errorRatioRule.setMinRequestAmount(properties.getCircuitBreaker().getMinRequestAmount());
        errorRatioRule.setTimeWindow((int) (properties.getCircuitBreaker().getBreakerDuration() / 1000));
        rules.add(errorRatioRule);

        // 异常数熔断
        DegradeRule errorCountRule = new DegradeRule();
        errorCountRule.setResource("default");
        errorCountRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
        errorCountRule.setCount(properties.getCircuitBreaker().getErrorCount());
        errorCountRule.setMinRequestAmount(properties.getCircuitBreaker().getMinRequestAmount());
        errorCountRule.setTimeWindow((int) (properties.getCircuitBreaker().getBreakerDuration() / 1000));
        rules.add(errorCountRule);

        DegradeRuleManager.loadRules(rules);
        log.info("Initialized circuit breaker rules: slow ratio = {}, error ratio = {}, error count = {}",
                properties.getCircuitBreaker().getSlowRatioThreshold(),
                properties.getCircuitBreaker().getErrorRatioThreshold(),
                properties.getCircuitBreaker().getErrorCount());
    }

    /**
     * 为特定资源添加流控规则
     */
    public void addFlowRule(String resource, double qps) {
        FlowRule rule = new FlowRule();
        rule.setResource(resource);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(qps);

        List<FlowRule> rules = new ArrayList<>(FlowRuleManager.getRules());
        rules.add(rule);
        FlowRuleManager.loadRules(rules);

        log.info("Added flow rule for resource: {}, QPS: {}", resource, qps);
    }

    /**
     * 为特定资源添加熔断规则
     */
    public void addDegradeRule(String resource, int grade, double count) {
        DegradeRule rule = new DegradeRule();
        rule.setResource(resource);
        rule.setGrade(grade);
        rule.setCount(count);
        rule.setTimeWindow((int) (properties.getCircuitBreaker().getBreakerDuration() / 1000));

        List<DegradeRule> rules = new ArrayList<>(DegradeRuleManager.getRules());
        rules.add(rule);
        DegradeRuleManager.loadRules(rules);

        log.info("Added degrade rule for resource: {}, grade: {}, count: {}", resource, grade, count);
    }
}

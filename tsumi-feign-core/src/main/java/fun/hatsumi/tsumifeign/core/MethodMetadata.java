package fun.hatsumi.tsumifeign.core;

import lombok.Data;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 方法元数据
 * 封装方法的注解信息
 *
 * @author Kakeru
 */
@Data
public class MethodMetadata {

    /**
     * 方法对象
     */
    private Method method;

    /**
     * HTTP 方法
     */
    private String httpMethod;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 请求头
     */
    private String[] headers;

    /**
     * 返回类型
     */
    private Type returnType;

    /**
     * 参数列表
     */
    private List<ParameterMetadata> parameters;

    /**
     * 参数元数据
     */
    @Data
    public static class ParameterMetadata {
        /**
         * 参数索引
         */
        private int index;

        /**
         * 参数类型
         */
        private Type type;

        /**
         * 参数名称
         */
        private String name;

        /**
         * 参数类型（路径、查询、请求体、请求头）
         */
        private ParameterType paramType;
    }

    /**
     * 参数类型枚举
     */
    public enum ParameterType {
        PATH,    // 路径变量
        QUERY,   // 查询参数
        BODY,    // 请求体
        HEADER   // 请求头
    }
}

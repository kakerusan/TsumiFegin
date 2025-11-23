package fun.hatsumi.tsumifeign.contract;

import fun.hatsumi.tsumifeign.annotation.*;
import fun.hatsumi.tsumifeign.core.MethodMetadata;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * 注解契约解析器
 * 解析方法上的注解，生成方法元数据
 *
 * @author Kakeru
 */
@Slf4j
public class AnnotationContract {

    /**
     * 解析方法
     */
    public MethodMetadata parseMethod(Method method) {
        MethodMetadata metadata = new MethodMetadata();
        metadata.setMethod(method);
        metadata.setReturnType(method.getGenericReturnType());

        // 解析 HTTP 方法和路径
        parseHttpMethod(method, metadata);

        // 解析方法参数
        parseParameters(method, metadata);

        return metadata;
    }

    /**
     * 解析 HTTP 方法和路径
     */
    private void parseHttpMethod(Method method, MethodMetadata metadata) {
        if (method.isAnnotationPresent(GetMapping.class)) {
            GetMapping mapping = method.getAnnotation(GetMapping.class);
            metadata.setHttpMethod("GET");
            metadata.setPath(getPath(mapping.value(), mapping.path()));
            metadata.setHeaders(mapping.headers());
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            PostMapping mapping = method.getAnnotation(PostMapping.class);
            metadata.setHttpMethod("POST");
            metadata.setPath(getPath(mapping.value(), mapping.path()));
            metadata.setHeaders(mapping.headers());
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            PutMapping mapping = method.getAnnotation(PutMapping.class);
            metadata.setHttpMethod("PUT");
            metadata.setPath(getPath(mapping.value(), mapping.path()));
            metadata.setHeaders(mapping.headers());
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);
            metadata.setHttpMethod("DELETE");
            metadata.setPath(getPath(mapping.value(), mapping.path()));
            metadata.setHeaders(mapping.headers());
        } else {
            throw new IllegalStateException("Method " + method.getName() + " must have a HTTP method annotation");
        }
    }

    /**
     * 获取路径
     */
    private String getPath(String value, String path) {
        if (value != null && !value.isEmpty()) {
            return value;
        }
        return path != null ? path : "";
    }

    /**
     * 解析方法参数
     */
    private void parseParameters(Method method, MethodMetadata metadata) {
        Parameter[] parameters = method.getParameters();
        List<MethodMetadata.ParameterMetadata> parameterMetadataList = new ArrayList<>();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            MethodMetadata.ParameterMetadata paramMetadata = new MethodMetadata.ParameterMetadata();
            paramMetadata.setIndex(i);
            paramMetadata.setType(parameter.getParameterizedType());

            // 解析参数注解
            if (parameter.isAnnotationPresent(PathVariable.class)) {
                PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
                paramMetadata.setParamType(MethodMetadata.ParameterType.PATH);
                paramMetadata.setName(getParamName(pathVariable.value(), pathVariable.name()));
            } else if (parameter.isAnnotationPresent(RequestParam.class)) {
                RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                paramMetadata.setParamType(MethodMetadata.ParameterType.QUERY);
                paramMetadata.setName(getParamName(requestParam.value(), requestParam.name()));
            } else if (parameter.isAnnotationPresent(RequestBody.class)) {
                paramMetadata.setParamType(MethodMetadata.ParameterType.BODY);
            } else if (parameter.isAnnotationPresent(RequestHeader.class)) {
                RequestHeader requestHeader = parameter.getAnnotation(RequestHeader.class);
                paramMetadata.setParamType(MethodMetadata.ParameterType.HEADER);
                paramMetadata.setName(getParamName(requestHeader.value(), requestHeader.name()));
            } else {
                // 默认作为请求体
                paramMetadata.setParamType(MethodMetadata.ParameterType.BODY);
            }

            parameterMetadataList.add(paramMetadata);
        }

        metadata.setParameters(parameterMetadataList);
    }

    /**
     * 获取参数名
     */
    private String getParamName(String value, String name) {
        if (value != null && !value.isEmpty()) {
            return value;
        }
        return name != null ? name : "";
    }
}

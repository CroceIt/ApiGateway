package com.hjzgg.apigateway.main.resolver;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.base.Defaults;
import com.hjzgg.apigateway.api.annotation.JsonContent;
import com.hjzgg.apigateway.commons.jackson.JacksonHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * 消费JSON内容的规则:
 * <p>
 * 如果请求体为非 JSON Object , 以请求体内容为准, 忽略Query String中的键值对;
 * 如果请求体为 JSON Object, Query String中的键值对以覆盖的方式合并到 JSON Object中。
 * <p>
 * 此类可以支持所有的HTTP 方法调用。
 */
public class JsonDataBinderArgumentResolver implements HandlerMethodArgumentResolver {
    private static final String JSON_DATA_ATTR =
            JsonDataBinderArgumentResolver.class.getName() + ".DATA";

    private final ObjectMapper objectMapper;

    public JsonDataBinderArgumentResolver() {
        objectMapper = JacksonHelper.getMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(JsonContent.class) != null;
    }

    @Override
    public final Object resolveArgument(MethodParameter parameter,
                                        ModelAndViewContainer mavContainer,
                                        NativeWebRequest webRequest,
                                        WebDataBinderFactory binderFactory) throws Exception {

        Class<?> parameterType = parameter.getParameterType();
        Object arg = resolveArgument(parameter, webRequest);
        if (arg == null) {
            //缺省值
            if (parameterType.getName().equals(Optional.class.getName())) {
                return Optional.empty();
            }
            if (parameterType.isPrimitive()) {
                return Defaults.defaultValue(parameterType);
            }
        }
        return arg;
    }

    private static String resolveName(MethodParameter parameter) {
        JsonContent jsonContent = parameter.getParameterAnnotation(JsonContent.class);
        //Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(jsonContent);
        //return (String) annotationAttributes.get("name");

        if (StringUtils.isNotBlank(jsonContent.name())) {
            return jsonContent.name();
        }
        return parameter.getParameterName();
    }

    public static JsonPointer getJsonPointer(MethodParameter parameter) {
        String key = "/" + resolveName(parameter);
        JsonContent jsonContent = parameter.getParameterAnnotation(JsonContent.class);
        if (null != jsonContent && StringUtils.isNotBlank(jsonContent.path())) {
            //如果是'.'分隔, 转换为'/'
            key = jsonContent.path().replace(".", "/");
            if (!key.startsWith("/")) {
                key = "/" + key;
            }
        }
        return JsonPointer.valueOf(key);
    }

    protected Object resolveArgument(MethodParameter parameter, NativeWebRequest webRequest) throws Exception {
        try {
            JsonNode json = getJson(webRequest);
            if (!json.isObject()) {
                //传入的不是一个JSON ObjectNode, 不需要匹配Path
                return resolveArg(parameter, json);
            }
            JsonPointer jsonPointer = getJsonPointer(parameter);
            JsonNode value = json.at(jsonPointer);
            if (BeanUtils.isSimpleProperty(parameter.getParameterType())) {
                //简单数据类型, 传入了一个ObjectNode, 那么需要匹配Path
                return resolveArg(parameter, value);
            } else {
                //参数是复杂类型, 需要ObjectNode, 传入的是ObjectNode
                if (value.isMissingNode()) {
                    //Path匹配不上, 尝试将整个传入的JSON进行反序列化
                    return resolveArg(parameter, json);
                }
                return resolveArg(parameter, value);
            }
        } catch (JsonProcessingException e) {
            throw new ServletRequestBindingException(
                    "无法从JSON解析需要的参数!(" + parameter + ")", e);
        }
    }

    private Object resolveArg(MethodParameter parameter, JsonNode jsonNode) throws IOException, ServletRequestBindingException {
        try {
            JavaType javaType = JacksonHelper.genJavaType(parameter.getGenericParameterType());

            if (!jsonNode.isMissingNode()) {
                return objectMapper.readValue(jsonNode.toString(), javaType);
            }
            return null;
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new ServletRequestBindingException(
                    "无法从JSON(" + jsonNode + ")解析需要的参数!(" + parameter + ")", e);
        }
    }

    private JsonNode getJson(NativeWebRequest request) throws IOException, ServletRequestBindingException {
        JsonNode data = (JsonNode) request.getAttribute(JSON_DATA_ATTR, RequestAttributes.SCOPE_REQUEST);
        if (null == data) {
            HttpServletRequest req = request.getNativeRequest(HttpServletRequest.class);
            JsonNode requestBody = getRequestBody(req.getInputStream());
            ObjectNode paramData = getParamData(request);
            data = merge(requestBody, paramData);
            request.setAttribute(JSON_DATA_ATTR, data, RequestAttributes.SCOPE_REQUEST);
        }
        return data;
    }

    private ObjectNode getParamData(NativeWebRequest request) {
        Map<String, String[]> parameters = request.getParameterMap();
        JsonNodeFactory nodeFactory = objectMapper.getNodeFactory();
        ObjectNode parameterMap = nodeFactory.objectNode();
        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            String[] value = entry.getValue();
            if (value == null) {
                continue;
            }
            JsonNode valueNode = getValue(nodeFactory, value);
            parameterMap.set(entry.getKey(), valueNode);
        }
        return parameterMap;
    }

    private JsonNode getRequestBody(InputStream inputStream) throws IOException,
            ServletRequestBindingException {
        String content = IOUtils.toString(inputStream, "UTF-8");
        if (!StringUtils.isEmpty(content)) {
            try {
                return objectMapper.readTree(content);
            } catch (JsonProcessingException e) {
                throw new ServletRequestBindingException("请求中的JSON格式非法！", e);
            }
        } else {
            return MissingNode.getInstance();
        }
    }

    private JsonNode merge(JsonNode body, ObjectNode param) {
        if (body.isMissingNode()) {
            //请求体为空, 返回QueryString内容
            return param;
        }
        //请求体不是Object, 忽略QueryParam
        if (!body.isObject()) {
            return body;
        } else {
            Iterator<String> fieldNames = param.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                // Overwrite field
                JsonNode value = param.get(fieldName);
                ((ObjectNode) body).set(fieldName, value);
            }
            if (body.size() == 0) {
                return MissingNode.getInstance();
            }
            return body;
        }
    }

    private JsonNode getValue(JsonNodeFactory nodeFactory, String[] value) {
        JsonNode valueNode;
        if (value.length == 1) {
            valueNode = nodeFactory.textNode(value[0]);
        } else {
            ArrayNode arrayNode = nodeFactory.arrayNode();
            for (String v : value) {
                arrayNode.add(v);
            }
            valueNode = arrayNode;
        }
        return valueNode;
    }
}

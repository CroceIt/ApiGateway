package com.hjzgg.apigateway.api.rpc;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import com.googlecode.jsonrpc4j.ProxyUtil;
import org.springframework.http.HttpMethod;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author hujunzheng
 * @create 2018-08-19 14:12
 **/
public class JsonRpcServlet extends HttpServlet {

    private JsonRpcServer server;

    private JsonRpcResolver resolver;

    public JsonRpcServlet(JsonRpcResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        if (CollectionUtils.isEmpty(resolver.getObjects())) {
            return;
        }
        server = new JsonRpcServer(ProxyUtil.createCompositeServiceProxy(ClassUtils.getDefaultClassLoader()
                , resolver.getObjects().toArray(new Object[]{})
                , resolver.getInterfaces().toArray(new Class[]{})
                , false));
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (HttpMethod.POST == HttpMethod.resolve(req.getMethod())) {
            server.handle(req, resp);
        } else {
            try (ServletOutputStream sos = resp.getOutputStream()) {
                sos.println("only support post");
                sos.flush();
            }
        }
    }
}
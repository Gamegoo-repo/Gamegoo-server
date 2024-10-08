package com.gamegoo.aop;

import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Slf4j
@Component
public class LogAspect {

//    @Pointcut("execution(* com.gamegoo.controller..*.*(..))")
//    public void all() {
//
//    }

    @Pointcut("execution(* com.gamegoo.controller..*.*(..))")
    public void controller() {
    }

//    @Around("all()")
//    public Object logging(ProceedingJoinPoint joinPoint) throws Throwable {
//        long start = System.currentTimeMillis();
//        try {
//            Object result = joinPoint.proceed();
//            return result;
//        } finally {
//            long end = System.currentTimeMillis();
//            long timeinMs = end - start;
//            log.info("{} | time = {}ms", joinPoint.getSignature(), timeinMs);
//        }
//    }

    @Around("controller()")
    public Object loggingBefore(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String controllerName = joinPoint.getSignature().getDeclaringType().getName();
        String methodName = joinPoint.getSignature().getName();
        Map<String, Object> params = new HashMap<>();

        try {
            String decodedURI = URLDecoder.decode(request.getRequestURI(), "UTF-8");
            String clientIp = getClientIp(request); // IP 주소 가져오기

            params.put("controller", controllerName);
            params.put("method", methodName);
            params.put("params", getParams(request));
            params.put("request_uri", decodedURI);
            params.put("http_method", request.getMethod());
            params.put("client_ip", clientIp); // IP 주소 추가

        } catch (Exception e) {
            log.error("LoggerAspect error", e);
        }

        log.info("<REQUEST> [{}] {} | IP: {} | Controller: {} | Method: {} | Params: {}",
            params.get("http_method"), params.get("request_uri"), params.get("client_ip"),
            controllerName, methodName, params.get("params"));

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();

        long executionTime = System.currentTimeMillis() - start;

        log.info("<RESPONSE> [{}] {} | IP: {} | Controller: {} | Method: {} | Execution Time: {}ms",
            params.get("http_method"), params.get("request_uri"), params.get("client_ip"),
            controllerName, methodName, executionTime);

        return result;
    }

    private static JSONObject getParams(HttpServletRequest request) {
        JSONObject jsonObject = new JSONObject();
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String param = params.nextElement();
            String replaceParam = param.replaceAll("\\.", "-");
            jsonObject.put(replaceParam, request.getParameter(param));
        }
        return jsonObject;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // X-Forwarded-For 헤더에 여러 IP가 포함될 경우 첫 번째 IP가 실제 클라이언트 IP
            return ip.split(",")[0].trim();
        }

        // X-Forwarded-For 헤더가 없거나 유효하지 않은 경우 다른 헤더를 확인
        ip = request.getHeader("Proxy-Client-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }

}

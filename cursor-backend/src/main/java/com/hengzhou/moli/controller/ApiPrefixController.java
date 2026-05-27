package com.hengzhou.moli.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;

/**
 * API前缀控制器 - 处理前端请求的 /api 前缀
 * 
 * 由于前端所有请求都加上 /api 前缀（如 /api/auth/login），
 * 但后端 Controller 路径不带 /api（如 /auth/login），
 * 所以需要这个控制器进行路径转换。
 * 
 * 工作原理：
 * 1. 前端请求 /api/auth/login
 * 2. 此控制器接收请求，去掉 /api 前缀
 * 3. 转发请求到 /auth/login（由 AuthController 处理）
 * 4. 返回响应给前端
 */
@Controller
@Slf4j
public class ApiPrefixController {

    private static final String API_PREFIX = "/api";

    /**
     * 处理认证相关请求的转发
     */
    @RequestMapping(value = "/api/auth/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public void forwardAuth(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI().substring(API_PREFIX.length());
        log.info("[API转发] {} -> {}", request.getRequestURI(), path);
        request.getRequestDispatcher(path).forward(request, response);
    }

    /**
     * 处理商品相关请求的转发
     */
    @RequestMapping(value = "/api/product/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public void forwardProduct(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI().substring(API_PREFIX.length());
        log.info("[API转发] {} -> {}", request.getRequestURI(), path);
        request.getRequestDispatcher(path).forward(request, response);
    }

    /**
     * 处理订单相关请求的转发
     */
    @RequestMapping(value = "/api/order/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public void forwardOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI().substring(API_PREFIX.length());
        log.info("[API转发] {} -> {}", request.getRequestURI(), path);
        request.getRequestDispatcher(path).forward(request, response);
    }

    /**
     * 处理用户相关请求的转发
     */
    @RequestMapping(value = "/api/user/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public void forwardUser(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI().substring(API_PREFIX.length());
        log.info("[API转发] {} -> {}", request.getRequestURI(), path);
        request.getRequestDispatcher(path).forward(request, response);
    }

    /**
     * 处理收购相关请求的转发
     */
    @RequestMapping(value = "/api/acquisition/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public void forwardAcquisition(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI().substring(API_PREFIX.length());
        log.info("[API转发] {} -> {}", request.getRequestURI(), path);
        request.getRequestDispatcher(path).forward(request, response);
    }

    /**
     * 处理天气相关请求的转发
     */
    @RequestMapping(value = "/api/weather/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public void forwardWeather(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI().substring(API_PREFIX.length());
        log.info("[API转发] {} -> {}", request.getRequestURI(), path);
        request.getRequestDispatcher(path).forward(request, response);
    }

    /**
     * 处理价格相关请求的转发
     */
    @RequestMapping(value = "/api/price/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public void forwardPrice(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI().substring(API_PREFIX.length());
        log.info("[API转发] {} -> {}", request.getRequestURI(), path);
        request.getRequestDispatcher(path).forward(request, response);
    }

    /**
     * 处理分类相关请求的转发
     */
    @RequestMapping(value = "/api/category/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public void forwardCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI().substring(API_PREFIX.length());
        log.info("[API转发] {} -> {}", request.getRequestURI(), path);
        request.getRequestDispatcher(path).forward(request, response);
    }

    /**
     * 处理管理员相关请求的转发
     */
    @RequestMapping(value = "/api/admin/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public void forwardAdmin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI().substring(API_PREFIX.length());
        log.info("[API转发] {} -> {}", request.getRequestURI(), path);
        request.getRequestDispatcher(path).forward(request, response);
    }

    /**
     * 处理地址相关请求的转发
     */
    @RequestMapping(value = "/api/address/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public void forwardAddress(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI().substring(API_PREFIX.length());
        log.info("[API转发] {} -> {}", request.getRequestURI(), path);
        request.getRequestDispatcher(path).forward(request, response);
    }

    /**
     * 处理消息相关请求的转发
     */
    @RequestMapping(value = "/api/message/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public void forwardMessage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI().substring(API_PREFIX.length());
        log.info("[API转发] {} -> {}", request.getRequestURI(), path);
        request.getRequestDispatcher(path).forward(request, response);
    }

    /**
     * 处理聊天相关请求的转发
     */
    @RequestMapping(value = "/api/chat/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public void forwardChat(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI().substring(API_PREFIX.length());
        log.info("[API转发] {} -> {}", request.getRequestURI(), path);
        request.getRequestDispatcher(path).forward(request, response);
    }

    /**
     * 处理评价相关请求的转发
     */
    @RequestMapping(value = "/api/review/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public void forwardReview(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI().substring(API_PREFIX.length());
        log.info("[API转发] {} -> {}", request.getRequestURI(), path);
        request.getRequestDispatcher(path).forward(request, response);
    }

    /**
     * 处理售后相关请求的转发
     */
    @RequestMapping(value = "/api/refund/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public void forwardRefund(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI().substring(API_PREFIX.length());
        log.info("[API转发] {} -> {}", request.getRequestURI(), path);
        request.getRequestDispatcher(path).forward(request, response);
    }

    /**
     * 处理投诉相关请求的转发
     */
    @RequestMapping(value = "/api/complaint/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public void forwardComplaint(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI().substring(API_PREFIX.length());
        log.info("[API转发] {} -> {}", request.getRequestURI(), path);
        request.getRequestDispatcher(path).forward(request, response);
    }

    /**
     * 处理上传相关请求的转发
     */
    @RequestMapping(value = "/api/upload/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public void forwardUpload(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI().substring(API_PREFIX.length());
        log.info("[API转发] {} -> {}", request.getRequestURI(), path);
        request.getRequestDispatcher(path).forward(request, response);
    }

    /**
     * 处理首页相关请求的转发
     */
    @RequestMapping(value = "/api/home/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public void forwardHome(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI().substring(API_PREFIX.length());
        log.info("[API转发] {} -> {}", request.getRequestURI(), path);
        request.getRequestDispatcher(path).forward(request, response);
    }

    /**
     * 处理商家资质相关请求的转发
     */
    @RequestMapping(value = "/api/shop/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public void forwardShop(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI().substring(API_PREFIX.length());
        log.info("[API转发] {} -> {}", request.getRequestURI(), path);
        request.getRequestDispatcher(path).forward(request, response);
    }

    /**
     * 处理市场相关请求的转发
     */
    @RequestMapping(value = "/api/market/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public void forwardMarket(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI().substring(API_PREFIX.length());
        log.info("[API转发] {} -> {}", request.getRequestURI(), path);
        request.getRequestDispatcher(path).forward(request, response);
    }
}

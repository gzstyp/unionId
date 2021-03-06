package com.fwtai.controller;

import com.alibaba.fastjson.JSONObject;
import com.fwtai.tool.ToolClient;
import com.fwtai.tool.ToolWechat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 获取unionId
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2020-11-17 10:11
 * @QQ号码 444141300
 * @Email service@dwlai.com
 * @官网 http://www.fwtai.com
*/
@RequestMapping("/api")
@RestController
public class ApiController{

    @Resource
    private ToolClient client;

    @GetMapping("/getSsessionKey")
    public void getSsessionKey(final HttpServletRequest request,final HttpServletResponse response){
        final String code = request.getParameter("code");
        System.out.println("code : "+code);
        final JSONObject jsonObject = ToolWechat.getSession(code);
        final String json = client.queryJson(jsonObject);
        client.responseJson(json,response);
    }

    @GetMapping("/unionId")
    public void unionId(final HttpServletRequest request,final HttpServletResponse response){
        final String session_key = request.getParameter("session_key");
        final String encryptedData = request.getParameter("encryptedData");
        final String iv = request.getParameter("iv");
        final JSONObject jsonObject = ToolWechat.decrypt(encryptedData,session_key,iv);
        final String json = client.queryJson(jsonObject);
        System.out.println(jsonObject);
        client.responseJson(json,response);
    }

    // 推荐使用本接口
    // http://192.168.3.108:8181/api/getUnionId?code=1024
    // http://app.fwtai.com/api/getUnionId?code=1024
    @GetMapping("/getUnionId")
    public void getUnionId(final HttpServletRequest request,final HttpServletResponse response){
        final String code = request.getParameter("code");
        final String iv = request.getParameter("iv");
        final String encryptedData = request.getParameter("encryptedData");

        System.out.println("code:");
        System.out.println(code);
        System.out.println("iv:");
        System.out.println(iv);
        System.out.println("encryptedData:");
        System.out.println(encryptedData);


        final JSONObject jsonObject = ToolWechat.getSession(code);
        if(jsonObject != null && jsonObject.containsKey("openid")){
            final String openid = jsonObject.getString("openid");
            final String session_key = jsonObject.getString("session_key");
            final JSONObject result = ToolWechat.decrypt(encryptedData,session_key,iv);
            final String unionId = result.getString("unionId");
            System.out.println("unionId->"+unionId);
            System.out.println(result);
            final String json = client.queryJson(result);
            client.responseJson(json,response);
        }else{
            final String json = client.json(199,"参数有误");
            client.responseJson(json,response);
        }
    }
}
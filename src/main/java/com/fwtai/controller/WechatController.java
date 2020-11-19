package com.fwtai.controller;

import com.alibaba.fastjson.JSONObject;
import com.fwtai.tool.AesCbcUtil;
import com.fwtai.tool.HttpRequest;
import com.fwtai.tool.ToolWechat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取unionId
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2020-11-17 10:11
 * @QQ号码 444141300
 * @Email service@dwlai.com
 * @官网 http://www.fwtai.com
*/
@RequestMapping("/wechat")
@RestController
public class WechatController{

    @PostMapping("/decodeUserInfo")
    public Map decodeUserInfo(final HttpServletRequest request,final HttpServletResponse response){
        final String code = request.getParameter("code");
        final String iv = request.getParameter("iv");
        final String encryptedData = request.getParameter("encryptedData");

        Map map = new HashMap();

        //登录凭证不能为空
        if (code == null || code.length() == 0) {
            map.put("status", 0);
            map.put("msg", "code 不能为空");
            return map;
        }

        //小程序唯一标识   (在微信小程序管理后台获取)
        String wxspAppid = "xxxxxxxxxxxxxxx";
        //小程序的 app secret (在微信小程序管理后台获取)
        String wxspSecret = "xxxxxxxxxxxx";
        //授权（必填）
        String grant_type = "authorization_code";


        //1、向微信服务器 使用登录凭证 code 获取 session_key 和 openid
        //请求参数
        String params = "appid=" + wxspAppid + "&secret=" + wxspSecret + "&js_code=" + code + "&grant_type=" + grant_type;
        //发送请求
        String sr = HttpRequest.sendGet("https://api.weixin.qq.com/sns/jscode2session", params);
        //解析相应内容（转换成json对象）
        JSONObject json = JSONObject.parseObject(sr);
        //获取会话密钥（session_key）
        String session_key = json.get("session_key").toString();
        //用户的唯一标识（openid）
        String openid = (String) json.get("openid");

        //2、对encryptedData加密数据进行AES解密
        try {
            String result = AesCbcUtil.decrypt(encryptedData, session_key, iv, "UTF-8");
            if (null != result && result.length() > 0) {
                map.put("status", 1);
                map.put("msg", "解密成功");

                final JSONObject userInfoJSON = JSONObject.parseObject(result);
                System.out.println(userInfoJSON);
                Map userInfo = new HashMap();
                userInfo.put("openId", userInfoJSON.get("openId"));
                userInfo.put("nickName", userInfoJSON.get("nickName"));
                userInfo.put("gender", userInfoJSON.get("gender"));
                userInfo.put("city", userInfoJSON.get("city"));
                userInfo.put("province", userInfoJSON.get("province"));
                userInfo.put("country", userInfoJSON.get("country"));
                userInfo.put("avatarUrl", userInfoJSON.get("avatarUrl"));
                userInfo.put("unionId", userInfoJSON.get("unionId"));
                map.put("userInfo", userInfo);
                return map;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        map.put("status", 0);
        map.put("msg", "解密失败");
        return map;
    }

    // http://192.168.3.108:8181/wechat/getUnionId?code=1024
    @PostMapping("/getUnionId")
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

        responseJson(code,response);
        final JSONObject jsonObject = ToolWechat.getSession(code);
        if(jsonObject != null && jsonObject.containsKey("openid")){
            final String openid = jsonObject.getString("openid");
            final String session_key = jsonObject.getString("session_key");
            final JSONObject result = ToolWechat.decrypt(encryptedData,session_key,iv);
            final String unionId = result.getString("unionId");
            System.out.println("unionId->"+unionId);
            System.out.println(result);
        }
    }

    public static void responseJson(final String json,final HttpServletResponse response){
        response.setContentType("text/html;charset=utf-8");
        response.setHeader("Cache-Control","no-cache");
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.write(json);
            writer.flush();
        }catch (final IOException e){
            e.printStackTrace();
        }finally{
            if(writer != null){
                writer.close();
                writer = null;
            }
        }
    }

    private String json(final int code,final String msg){
        final JSONObject json = new JSONObject(2);
        json.put("code",code);
        json.put("msg",msg);
        return json.toJSONString();
    }

    public String queryJson(final Object object){
        if(object == null || object.toString().trim().length() <= 0){
            return json(201,"暂无数据");
        }
        final JSONObject json = new JSONObject(3);
        json.put("code",200);
        json.put("msg","操作成功");
        json.put("data",object);
        return json.toJSONString();
    }
}
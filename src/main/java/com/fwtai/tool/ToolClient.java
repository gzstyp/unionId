package com.fwtai.tool;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 客户端接收及响应数据
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2020-11-19 18:18
 * @QQ号码 444141300
 * @Email service@dwlai.com
 * @官网 http://www.fwtai.com
*/
@Component
public class ToolClient{

    public void responseJson(final String json,final HttpServletResponse response){
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

    public String json(final int code,final String msg){
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
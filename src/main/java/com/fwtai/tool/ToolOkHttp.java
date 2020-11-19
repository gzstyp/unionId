package com.fwtai.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * 请求处理(OkHttp拦截器)
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2018-01-08 18:28
 * @QQ号码 444141300
 * @Email service@fwtai.com
 * @官网 http://www.fwtai.com
 */
public final class ToolOkHttp{

    /**GET异步请求,无参数或参数在url里*/
    public static void ajaxGet(final String url,final Callback callback){
        final OkHttpClient client = new OkHttpClient().newBuilder().readTimeout(6000,TimeUnit.MILLISECONDS).connectTimeout(6000,TimeUnit.MILLISECONDS).build();
        final Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(callback);
    }

    /**GET同步请求,无参数或参数在url里*/
    public static Response ajaxGet(final String url) throws IOException{
        final OkHttpClient client = new OkHttpClient().newBuilder().readTimeout(6000,TimeUnit.MILLISECONDS).connectTimeout(6000,TimeUnit.MILLISECONDS).build();
        final Request request = new Request.Builder().url(url).build();
        return client.newCall(request).execute();
    }

    /**POST异步请求,无参数或参数在url里*/
    public static void ajaxPost(final String url,final Callback callback){
        final Request request = postRequest(url);
        final OkHttpClient client = new OkHttpClient.Builder().build();
        client.newCall(request).enqueue(callback);
    }

    /**POST同步请求,无参数或参数在url里*/
    public static Response ajaxPost(final String url)throws IOException{
        final Request request = postRequest(url);
        final OkHttpClient client = new OkHttpClient.Builder().build();
        return client.newCall(request).execute();
    }

    /**POST同步请求,有参数请求*/
    public static Response ajaxPost(final String url,final HashMap<String,String> params)throws IOException{
        final Request request = postRequest(url,params);
        final OkHttpClient client = new OkHttpClient.Builder().build();
        return client.newCall(request).execute();
    }

    /**POST异步请求,有参数请求-推荐*/
    public static void ajaxPost(final String url,final HashMap<String,String> params,final Callback callback){
        final Request request = postRequest(url,params);
        final OkHttpClient client = new OkHttpClient.Builder().build();
        client.newCall(request).enqueue(callback);
    }

    /**组装POST请求参数-不带请求参数*/
    private static Request postRequest(final String url){
        return new Request.Builder().url(url).post(new FormBody.Builder().build()).build();
    }

    /**组装POST请求参数,适用于form表单提交,有带请求参数*/
    private static Request postRequest(final String url,final HashMap<String,String> params){
        final FormBody.Builder builder = new FormBody.Builder();
        if(params != null && params.size() > 0){
            for (final Entry<String,String> entry : params.entrySet()){
                builder.add(entry.getKey(),entry.getValue());
            }
        }
        return new Request.Builder().url(url).post(builder.build()).build();
    }

    /**解析请求返回的数据体*/
    public static String parseResponse(final Response response)throws Exception{
        return response.body().string();
    }

    /**
     * 构造通用的get-request,带请求头的
     * @param url 请求路径
     * @param headers 请求头key-value
    */
    public static Request getRequestHeader(final String url,final HashMap<String,String> headers){
        final Request.Builder builder = new Request.Builder();
        if(headers != null && headers.size() > 0){
            final Headers header = Headers.of(headers);
            return builder.get().url(url).headers(header).build();
        }else{
            return builder.get().url(url).build();
        }
    }

    /**
     * 组装POST请求参数,适用于form表单提交,有带请求头和请求参数
     * @param url 请求的路径
     * @param params 请求的参数
     * @param headers 请求头key-value
    */
    protected static Request postRequestHeader(final String url,final HashMap<String,String> params,final HashMap<String,String> headers){
        final FormBody.Builder builder = new FormBody.Builder();
        if(params != null && params.size() > 0){
            for(final String key : params.keySet()){
                final String value = params.get(key);
                if(value != null && value.length() > 0){
                    builder.add(key,value);
                }
            }
        }
        if(headers != null && headers.size() > 0){
            final Headers header = Headers.of(headers);
            return new Request.Builder().url(url).post(builder.build()).headers(header).build();
        } else {
            return new Request.Builder().url(url).post(builder.build()).build();
        }
    }

    /**
     * POST同步请求,支持请求体和请求头
     * @param object 可以是实体|map|HashMapPOST同步请求,支持请求体和请求头
     * @作者 田应平
     * @QQ 444141300
     * @创建时间 2019年3月22日 18:54:58
    */
    public static String post(final String url,final HashMap<String,String> headerMap,String contentType,final Object object) throws Exception{
        Request.Builder builder = new Request.Builder().url(url);
        if(headerMap != null && headerMap.size() > 0){
            Headers headers = Headers.of(headerMap);
            builder = new Request.Builder().url(url).headers(headers);
        }
        Request request = builder.post(new FormBody.Builder().build()).build();
        if(contentType == null || contentType.length() <= 0){
            contentType = "application/json";
        }
        if(object != null && object.toString().length() >0){
            final String data = JSONObject.parseObject(JSON.toJSONString(object)).toJSONString();
            final RequestBody requestBody = RequestBody.create(MediaType.parse(contentType),data);
            request = builder.post(requestBody).build();
        }
        return new OkHttpClient().newCall(request).execute().body().string();
    }

    /**
     * 多文件上传-图片[不含参数]
     * @param
     * @作者 田应平
     * @QQ 444141300
     * @创建时间 2019/9/30 21:16
     */
    public static void uploadImage(final String url,final HashMap<String,File> images){
        final OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for(final String key : images.keySet()){
            builder.addFormDataPart("files",images.get(key).getName(),RequestBody.create(MediaType.parse("image/jpeg"),images.get(key)));
        }
        final MultipartBody multipartBody = builder.build();
        final Request request = new Request.Builder().url(url).post(multipartBody).build();
        client.newCall(request).enqueue(new Callback(){
            @Override
            public void onResponse(Call call,Response response) throws IOException{
                System.out.println(response.body().string());
            }
            @Override
            public void onFailure(Call call,IOException e){
                System.out.println(e.getMessage());
            }
        });
    }

    /**
     * 多文件上传-图片[含参数]
     * @param
     * @作者 田应平
     * @QQ 444141300
     * @创建时间 2019/9/30 21:16
     */
    public static void uploadImage(final String url,final HashMap<String,File> images,final HashMap<String,String> params){
        final OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for(final String key : params.keySet()){
            builder.addFormDataPart(key,params.get(key));
        }
        for(final String key : images.keySet()){
            builder.addFormDataPart("files",images.get(key).getName(),RequestBody.create(MediaType.parse("image/jpeg"),images.get(key)));
        }
        final MultipartBody multipartBody = builder.build();
        final Request request = new Request.Builder().url(url).post(multipartBody).build();
        client.newCall(request).enqueue(new Callback(){
            @Override
            public void onResponse(Call call,Response response) throws IOException{
                System.out.println(response.body().string());
            }
            @Override
            public void onFailure(Call call,IOException e){
                System.out.println(e.getMessage());
            }
        });
    }

    /**异步请求-带请求头且封装多实体对象请求,即params里的value可以是基本类型(String,Integer)、实体、JSONObject对象*/
    /*public static void requestPostHeaderAsynch(final String url,final JSONObject params,final HashMap<String,String> headers,final IRequest iRequest){
        final OkHttpClient client = new OkHttpClient();
        final Request.Builder requestbuilder = new Request.Builder().url(url);
        for(final String key : headers.keySet()){
            final String value = headers.get(key);
            if(value != null && value.length() > 0){
                requestbuilder.addHeader(key,value);
            }
        }
        final Request request = requestPostBeans(url,params);
        client.newCall(request).enqueue(new Callback() {
            public void onResponse(final Call call,final Response response) throws IOException{
                if(response.code() >= 200 && response.code() < 300){
                    iRequest.onSuccess(response.body().string());
                }
            }
            public void onFailure(final Call call,final IOException exception) {
                iRequest.onFailure(exception);
            }
        });
    }*/

    /**同步请求-带请求头且封装多实体对象请求,即params里的value可以是基本类型(String,Integer)、实体、JSONObject对象*/
    public static String requestPostHeader(final String url,final JSONObject params,final HashMap<String,String> headers) throws Exception {
        final OkHttpClient client = new OkHttpClient();
        final Request.Builder requestbuilder = new Request.Builder().url(url);
        for(final String key : headers.keySet()){
            final String value = headers.get(key);
            if(value != null && value.length() > 0){
                requestbuilder.addHeader(key,value);
            }
        }
        final Request request = requestPostBeans(url,params);
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private static Request requestPostBeans(final String url,final JSONObject params){
        final RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),JSON.toJSONString(params));
        return new Request.Builder().post(requestBody).tag(url+params).url(url).build();
    }

    /**
     * 下载图片,返回下载文件的全路径,在业务里判断是否为空再处理,https://blog.csdn.net/brycegao321/article/details/52131932
     * @param
     * @作者 田应平
     * @QQ 444141300
     * @创建时间 2020/1/19 20:40
    */
    public static String downloadImage(final String filePath,final String url){
        try {
            final OkHttpClient client = new OkHttpClient().newBuilder().readTimeout(60000,TimeUnit.MILLISECONDS).connectTimeout(60000,TimeUnit.MILLISECONDS).build();
            final ResponseBody body = client.newCall(new Request.Builder().url(url).build()).execute().body();
            final InputStream input = body.byteStream();//获取流
            final MediaType mediaType = body.contentType();
            final String subtype = mediaType.subtype();
            final String fullPath = filePath + "." + subtype;
            final File file = new File(fullPath);
            final FileOutputStream out = new FileOutputStream(file);
            int i;
            while ((i = input.read()) != -1){
                out.write(i);
            }
            input.close();
            out.close();
            return fullPath;
        } catch (Exception e) {
            return null;
        }
    }

    /***************************************************************************好使ok*************************************************************************************/
    /**解析请求返回的数据体*/
    /*public static String parseResponse(final Response response)throws IOException{
        return response.body().string();
    }*/

    /**异步请求-ok,数据格式：
     {
     "Data": {
     "Action": "addPerson",
     "PersonInfo": {
     "PersonName": "田应平",
     "PersonId": "ffffffffd9bbfbb2000000000fe97ba0",
     "IDCard": "52262219850117651X",
     "PersonExtension": {
     "PersonCode1": "4000000000"
     },
     "PersonPhoto": "imageBase64"
     },
     "PersonType": 2
     },
     "UUID": "umetq7ckapvu",
     "Name": "personListRequest"
     }
     */
    /*public static void ajaxPost(final String url,final JSONObject params,final Callback callback){
        final OkHttpClient client = new OkHttpClient().newBuilder().readTimeout(6000,TimeUnit.MILLISECONDS).connectTimeout(6000,TimeUnit.MILLISECONDS).build();
        final Request request = requestPostBeans(url,params);
        client.newCall(request).enqueue(callback);
    }*/

    /**同步请求-ok,数据格式：
     {
     "Data": {
     "Action": "addPerson",
     "PersonInfo": {
     "PersonName": "田应平",
     "PersonId": "ffffffffd9bbfbb2000000000fe97ba0",
     "IDCard": "52262219850117651X",
     "PersonExtension": {
     "PersonCode1": "4000000000"
     },
     "PersonPhoto": "imageBase64"
     },
     "PersonType": 2
     },
     "UUID": "umetq7ckapvu",
     "Name": "personListRequest"
     }
     */
    /*public static void ajaxPost(final String url,final JSONObject params)throws IOException{
        final OkHttpClient client = new OkHttpClient().newBuilder().readTimeout(6000,TimeUnit.MILLISECONDS).connectTimeout(6000,TimeUnit.MILLISECONDS).build();
        final Request request = requestPostBeans(url,params);
        final Response response = client.newCall(request).execute();
        final String json = parseResponse(response);
        final HashMap<String,String> map = ToolString.parseJsonObject(json);
        final Integer Code = Integer.parseInt(map.get("Code"));
        if(Code == 1){
            System.out.println("添加成功");
        }else{
            System.out.println("添加失败,原因:" + map.get("Message"));
        }
        System.out.println(json);
    }*/

    /*public static Request requestPostBeans(final String url,final JSONObject params){
        final RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),JSON.toJSONString(params));
        return new Request.Builder().post(requestBody).tag(url+params).url(url).build();
    }

    //GET同步请求,无参数或参数在url里
    private static Response responseGet(String url,final HashMap<String,String> params) throws IOException{
        final OkHttpClient client = new OkHttpClient().newBuilder().readTimeout(6000,TimeUnit.MILLISECONDS).connectTimeout(6000,TimeUnit.MILLISECONDS).build();
        if(params != null && params.size() > 0){
            final StringBuilder sb = new StringBuilder();
            if(!url.contains("?") && !url.endsWith("?")){
                url += "?";
            }else if(url.contains("?") && !url.endsWith("?")){
                url += "&";
            }
            if(params != null && params.size() > 0){
                for(final String key : params.keySet()){
                    final String value = params.get(key);
                    if(value != null && value.length() > 0){
                        sb.append(key).append("=").append(value.trim()).append("&");
                    }
                }
                sb.deleteCharAt(sb.length()-1);
                url = url + sb.toString();
            }
            return client.newCall(new Request.Builder().url(url).build()).execute();
        }else{
            return client.newCall(new Request.Builder().url(url).build()).execute();
        }
    }

    */



    /***************************************************************************好使ok*************************************************************************************/
}
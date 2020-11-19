package com.fwtai.tool;

import com.alibaba.fastjson.JSONObject;
import okhttp3.Response;
import org.apache.tomcat.util.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.Security;

/**
 * 微信接口统一登录
 * @作者 田应平
 * @版本 v1.0
 * @创建时间 2020-11-18 10:36
 * @QQ号码 444141300
 * @Email service@dwlai.com
 * @官网 http://www.fwtai.com
*/
public final class ToolWechat{

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static String wxspAppid = "wxbf1695db9525b4ed";//瞎写的

    private static String wxspSecret = "3444b156e2f2141456df6a226dce57a4";//瞎写的

    //获取 session_key 和 openid
    public static JSONObject getSession(final String code){
        //授权（必填）
        final String grant_type = "authorization_code";
        final String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + wxspAppid + "&secret=" + wxspSecret + "&js_code=" + code + "&grant_type=" + grant_type;
        try {
            final Response response = ToolOkHttp.ajaxGet(url);
            assert response.body() != null;
            final String body = response.body().string();
            return JSONObject.parseObject(body);
        } catch (final Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * AES解密,获取unionId
     *
     * @param data           //密文，被加密的数据
     * @param session_key            //秘钥
     * @param iv             //偏移量
     * @return
     * @throws Exception
     */
    public static JSONObject decrypt(final String data,final String session_key,final String iv){
        //被加密的数据
        final byte[] dataByte = Base64.decodeBase64(data);
        //加密秘钥
        final byte[] keyByte = Base64.decodeBase64(session_key);
        //偏移量
        final byte[] ivByte = Base64.decodeBase64(iv);
        try {
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            final SecretKeySpec spec = new SecretKeySpec(keyByte, "AES");
            final AlgorithmParameters parameters = AlgorithmParameters.getInstance("AES");
            parameters.init(new IvParameterSpec(ivByte));
            cipher.init(Cipher.DECRYPT_MODE, spec, parameters);// 初始化
            final byte[] resultByte = cipher.doFinal(dataByte);
            if (null != resultByte && resultByte.length > 0) {
                return JSONObject.parseObject(new String(resultByte,StandardCharsets.UTF_8));
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
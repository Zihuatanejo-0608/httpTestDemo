package serviceUnit;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import toolsUnit.HuToolHttpUtil;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by andy on 2019/6/12.
 */
public class InitLogin {

    private static String urlHead = "http://101.91.qq.202:9999/";

    public static String encryption(String str) throws Exception{
        String publicKey = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA2OExk3OYNmgWlkVxAOu0z8MM1i1YLvagfgDzEHBjAmQRX8dNVNbETW36Mpfwaw5jKtvcB4Rnm5UZHhrAXx/bCBeQDj5Ow3llyQYVVow8yxQB6h+VO6ukaUW9j7/MwwVgvQt9pnn53UHbNdKUGCIL1Z69/VNn9cZzcKqsc6gwuKO+I2IJl68hA3ya5ZvXLtHs3oyHQb59pzYntZ0BBxLd37StsIUGP3V2RAqpzHE0oLDTJDqAVHi+hIo/4XowVQmnZLGq+INtpllREChP0YVqlh0j/IiTEpEVxNVnaJHHlN5xekSCWR8/h7QYMjJBxOhGacZYaDTl1Yo5ANBGxldWCOU1p8a52oMzWuulDKciQUUXdZ7UmWxeceoKirU0CwArKOn1X+av/dJ0iyL6on5SuLziljhl+2cKAuk7IIiYFYrNkDFmRmkTLn67LwkBEFomGCu1aSW8xOVJ7PL0KVya9xiqy5Ukf4kQfp6b7/GfRJjJKKAv7Ucv9sPuO0mBdUf4ZUf2eLwcINakHlyzqSARCXdExh+IRLyeldw6aEkn/XEjeT0ZDzrFQVD29BdoIlCRXSmvsYffDAgVjCjxZ1CEfcRAWav1uUSmJRD+C/etTu4LZNmyYkIE9VelWniozHbEh9Za6wMkt94acj7hDNkpQNN1EIjZXg4m3xr+lDDQCXUCAwEAAQ==";
        //base64编码的公钥
        byte[] decoded = Base64.decodeBase64(publicKey);

        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
        //RSA加密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        String outStr = Base64.encodeBase64String(cipher.doFinal(str.getBytes("UTF-8")));
        return outStr;
    }

    //api版本
    public static String loginApi(String username,String password) throws Exception{
        String url = urlHead + "auth/login";
        JSONObject request = new JSONObject();
        request.put("username",username);
        //加密密码
        String encryption = encryption(password);
        request.put("password",encryption);
        //获取key
        String key = getSession();
        //获取验证码
        String verifyCode = redisValue(key);
        request.put("verifyCodeInfo",verifyCode);

        JSONObject respone = HuToolHttpUtil.post(url, "", "", request);
        JSONObject data = (JSONObject) respone.get("data");
        String token = data.get("token").toString();
        System.out.println("token:" + data.get("token"));
        System.out.println("================================");
        System.out.println("================================");
        return token;
    }

    //秘钥,废弃
    public static String verifyCodeData() throws Exception{
        String url = urlHead + "auth/verifyCode";
        JSONObject respone = HuToolHttpUtil.get(url, null, null);
        JSONObject data = (JSONObject) respone.get("data");
        String verifyCodeKey = data.get("verifyCodeKey").toString();
        System.out.println(verifyCodeKey);

        return verifyCodeKey;
    }

    //获取sessionId,拼接成为redis的key
    public static String getSession() throws Exception{
        String url = urlHead + "auth/verifyCode";
        HttpResponse response = HttpUtil.createGet(url).timeout(5000).execute();
        if (response.isOk()){
            String sessionId = response.getCookie("JSESSIONID").getValue();
            String key = "verifyCode:" + sessionId;
            return key;
        }
        return null;
    }

    //通过api获取验证码
    public static String redisValue(String key){
        String url = urlHead + "auth/redisValue?key=" + key;
        try{
            JSONObject respone = HuToolHttpUtil.get(url, null, null);
            String data = (String)respone.get("data");
            System.out.println("验证码:" + data);
            return data;
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("获取key接口失败!");
        }
        return null;
    }

}

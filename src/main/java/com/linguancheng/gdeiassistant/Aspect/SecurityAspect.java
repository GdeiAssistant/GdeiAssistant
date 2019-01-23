package com.linguancheng.gdeiassistant.Aspect;

import com.alibaba.fastjson.JSON;
import com.linguancheng.gdeiassistant.Exception.ChargeException.SecurityInvalidException;
import com.linguancheng.gdeiassistant.Pojo.Entity.EncryptedData;
import com.linguancheng.gdeiassistant.Pojo.Entity.RequestSecurity;
import com.linguancheng.gdeiassistant.Pojo.Entity.RequestValidation;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Tools.AESUtils;
import com.linguancheng.gdeiassistant.Tools.RSAUtils;
import com.linguancheng.gdeiassistant.Tools.StringEncryptUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 安全校验和加密逻辑切面
 * <p>
 * 使用方法：当需要加密DataJsonResult类中的Data属性值时
 * 在返回DataJsonResult的方法上添加@RequireSecurity注解
 * <p>
 * 校验和加密过程：服务端通过RequestSecurity类接收客户端的RSA公钥、数字签名和经服务端公钥加密的客户端生成的AES密钥
 * 通过环绕通知增强切面，在方法执行前，利用客户端提供的RSA公钥，对客户端一并提供的数字签名进行校验
 * 若校验不正确，则抛出SecurityInvalidException异常
 * <p>
 * 客户端数字签名校验通过后，执行原方法，并接收返回的DataJsonResult属性
 * <p>
 * 将DataJsonResult中的Data属性序列化为JSON字符串，并使用经服务端私钥解密的客户端提供的加密密钥对其进行AES加密，得到加密后的Data字符串
 * 使用加密后的Data字符串进行SHA1消息摘要，并使用服务端私钥进行加密，得到服务端的数字签名，供客户端校验
 * 使用该加密后的Data字符串和服务端数字签名，生成新的EncryptedDataJsonResult对象，最后转换为JsonString并输出
 */
@Aspect
@Component
@Order(Integer.MIN_VALUE)
public class SecurityAspect {

    @Pointcut("@annotation(com.linguancheng.gdeiassistant.Annotation.RequireSecurity)")
    public void RequestAction() {

    }

    @Around("RequestAction()")
    public DataJsonResult SecurityAction(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object[] args = proceedingJoinPoint.getArgs();
        //获取请求的参数
        Object requestParams = null;
        //获取请求的时间戳、随机值和签名
        RequestValidation requestValidation = null;
        //获取请求的安全参数
        RequestSecurity requestSecurity = null;
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        for (int i = 1; i < methodSignature.getParameterNames().length; i++) {
            if (methodSignature.getParameterNames()[i].equals("requestParams")) {
                requestParams = args[i];
            }
            if (methodSignature.getParameterNames()[i].equals("requestSecurity")) {
                requestSecurity = (RequestSecurity) args[i];
            }
            if (methodSignature.getParameterNames()[i].equals("requestValidation")) {
                requestValidation = (RequestValidation) args[i];
            }
        }
        //校验用户请求安全参数的数字签名
        if (requestParams != null && requestValidation != null && requestSecurity != null) {
            //使用请求参数和请求随机值、时间戳生成消息摘要，与客户端提供的数字签名进行校验
            Map<String, Object> objectMap = new LinkedHashMap<>();
            Field[] fields = requestParams.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                objectMap.put(field.getName(), field.get(requestParams));
            }
            objectMap.put("nonce", requestValidation.getNonce());
            objectMap.put("timestamp", requestValidation.getTimestamp());
            String hash = StringEncryptUtils.SHA1HexString(JSON.toJSONString(objectMap));
            if (new String(RSAUtils.DecryptByteWithPublicKey(Base64.getDecoder().decode(requestSecurity.getClientRSAPublicKey())
                    , Base64.getDecoder().decode(requestSecurity.getClientRSASignature())))
                    .equals(hash)) {
                //数字签名校验通过，解密客户端AES密钥
                String clientAESKey = Base64.getEncoder().encodeToString(RSAUtils.DecryptByteWithPrivateKey(RSAUtils.getPrivateKey()
                        , Base64.getDecoder().decode(requestSecurity.getClientAESKey())));
                DataJsonResult dataJsonResult = (DataJsonResult) proceedingJoinPoint.proceed(args);
                if (dataJsonResult.getData() != null) {
                    //生成加密的JSON字符串数据和数字签名
                    String data = Base64.getEncoder().encodeToString(AESUtils.EncryptByte(Base64.getDecoder().decode(clientAESKey)
                            , JSON.toJSONString(dataJsonResult.getData()).getBytes()));
                    String signature = Base64.getEncoder().encodeToString(RSAUtils.EncryptByteWithPrivateKey(RSAUtils.getPrivateKey()
                            , StringEncryptUtils.SHA1HexString((JSON.toJSONString(dataJsonResult.getData()))).getBytes()));
                    //构建生成加密后的DataJsonResult
                    EncryptedData encryptedData = new EncryptedData();
                    encryptedData.setData(data);
                    encryptedData.setSignature(signature);
                    DataJsonResult<EncryptedData> result = new DataJsonResult<>();
                    result.setCode(dataJsonResult.getCode());
                    result.setMessage(dataJsonResult.getMessage());
                    result.setData(encryptedData);
                    result.setSuccess(dataJsonResult.isSuccess());
                    return result;
                }
            }
        }
        throw new SecurityInvalidException("安全校验不通过");
    }
}

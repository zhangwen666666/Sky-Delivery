package com.sky.utils;

import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Data
@Slf4j
@AllArgsConstructor
public class HwObsUtil {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 文件上传
     * @param bytes
     * @param objectName
     * @return 上传成功返回上传地址，失败返回null
     */
    public String upload(byte[] bytes, String objectName) {
        ObsClient obsClient = null;
        try{
            obsClient = new ObsClient(accessKeyId, accessKeySecret,endpoint);
            obsClient.putObject(bucketName,objectName, new ByteArrayInputStream(bytes));
            //文件访问路径规则 https://BucketName.Endpoint/ObjectName
            StringBuilder stringBuilder = new StringBuilder("https://");
            stringBuilder
                    .append(bucketName)
                    .append(".")
                    .append(endpoint)
                    .append("/")
                    .append(objectName);
            log.info("文件上传到:{}", stringBuilder.toString());
            return stringBuilder.toString();
        }catch (ObsException e) {
            log.error("putObject failed");
            // 请求失败,打印http状态码
            System.out.println("HTTP Code:" + e.getResponseCode());
            // 请求失败,打印服务端错误码
            System.out.println("Error Code:" + e.getErrorCode());
            // 请求失败,打印详细错误信息
            System.out.println("Error Message:" + e.getErrorMessage());
            // 请求失败,打印请求id
            System.out.println("Request ID:" + e.getErrorRequestId());
            System.out.println("Host ID:" + e.getErrorHostId());
            e.printStackTrace();
        } catch (Exception e) {
            log.error("putObject failed");
            // 其他异常信息打印
            e.printStackTrace();
        }finally {
            try {
                obsClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

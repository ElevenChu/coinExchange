package com.elevenchu.controller;

import cn.hutool.core.date.DateUtil;
import com.aliyun.oss.OSS;
import com.elevenchu.model.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Api(tags = "文件上传的控制器")
public class FileController {
    @Autowired
    private OSS ossClient;
    @Value("${oss.bucket.name:coin-exchange-imgs11}")
    private String bucketName;
    @Value("${spring.cloud.alicloud.oss.endpoint}")
    private String endPoint;

    @PostMapping("/image/AliYunImgUpload")
    @ApiOperation("上传文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file",value = "你要上传的文件")
    })
    public R<String> fileUpload(@RequestParam("file")MultipartFile file) throws IOException {
/**1bucketName
 * 2.文件的名称:日期+文件原始名
 * 3.文件的输入流
 */
   String fileName= DateUtil.today().replaceAll(",","/")+"/"+file.getOriginalFilename();
    ossClient.putObject(bucketName,fileName,file.getInputStream());
    return R.ok("https://"+bucketName+"."+endPoint+"/"+fileName);
    }



}

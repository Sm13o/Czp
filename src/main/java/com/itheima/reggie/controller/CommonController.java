package com.itheima.reggie.controller;


import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reegie.path}")
    public String basePath;

    /**
     * 文件上传和下载
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){

//        1.file是一个临时文件，需要转存到指定位置，否则本次临时文件会完成后被删除
//        2.原始文件名
        String of = file.getOriginalFilename();// xxx.jpg
//        从点截取到 .jpg
        String substring = of.substring(of.lastIndexOf("."));

//        3.使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
        String fileName = UUID.randomUUID().toString() + substring;

//        4.创建一个目录对象
        File file1 = new File(basePath);
//        5.判断文件名是否存在
        if (!file1.exists()){
//            不存在，需要创建
            file1.mkdirs();
        }
        try {
//            将临时文件存储在指定位置
            file.transferTo(new File(basePath+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return R.success(fileName);

    }

    /**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        try {
//            输入流 读取文件内容
            FileInputStream fis = new FileInputStream(new File(basePath + name));

//            输出流 将文件写回浏览器，在浏览器展示图片
            ServletOutputStream sos = response.getOutputStream();
//            设置回去的是什么类型的文件
            response.setContentType("image/jpeg");

//          读取流展示图片
            int len=0;
            byte[] bytes = new byte[1024];
           while((len=fis.read(bytes))!=-1){
               sos.write(bytes,0,len);
               sos.flush();
           }

//            关闭流
            sos.close();
            fis.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


package pers.internship.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import pers.internship.demo.entity.User;
import pers.internship.demo.service.UserService;
import pers.internship.demo.util.CommunityUtil;
import pers.internship.demo.util.HostHolder;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Value("${demo.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${demo.path.upload}")
    private String uploadPath;

    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting.html";
    }

    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeaderUrl(MultipartFile headerImage, Model model) {

        // 空值处理
        User user = hostHolder.getUser();
        if (user == null) {
            model.addAttribute("error", "请先登录哦!");
            return "/site/setting.html";
        }
        if (headerImage == null) {
            model.addAttribute("error", "您还尚未选择图片!");
            return "/site/setting.html";
        }

        // 获取图片文件名后缀
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName != null ? fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase() : "";

        // 校验文件格式
        switch (suffix) {
            case "jpg":
            case "jpeg":
            case "png":
                break;
            default:
                model.addAttribute("error", "图片目前仅支持 .jpg .jpeg .png 格式哦");
                return "/site/setting.html";
        }

        // 获取原头像 Web 路径
        fileName = user.getHeaderUrl();

        // 截获原头像文件名
        fileName = fileName.substring(fileName.lastIndexOf("/"));

        // 删除原头像
        File dest = new File(uploadPath + fileName);
        boolean isDelete = dest.delete();

        // 重新随机生成唯一文件名
        fileName = CommunityUtil.generateUUID() + "." + suffix;

        // 确定文件存放路径
        dest = new File(uploadPath + "/" + fileName);
        try {
            // 存放文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("图片上传失败: " + e.getMessage());
            throw new RuntimeException("服务器异常: 图片上传失败 " + e);
        }

        // 更新当前用户头像的 Web 路径
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeaderUrl(user.getId(), headerUrl);

        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeaderImage(@PathVariable String fileName, HttpServletResponse response) {
        // 获取文件存放路径
        fileName = uploadPath + "/" + fileName;

        // 获取文件后缀名
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        System.out.println(suffix);
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fileIS = new FileInputStream(fileName);
                OutputStream responseOS = response.getOutputStream()
        ) {
            byte[] buffer = new byte[1024];
            int bytes = 0;
            while ((bytes = fileIS.read(buffer)) != -1) {
                responseOS.write(buffer, 0, bytes);
            }
        } catch (IOException e) {
            logger.error("头像获取失败: " + e.getMessage());
        }
    }
}

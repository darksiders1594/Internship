package pers.internship.demo.controller;

import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import pers.internship.demo.entity.User;
import pers.internship.demo.service.UserService;
import pers.internship.demo.util.CommunityConstant;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register.html";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login.html";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "提交成功, 我们已向您发送了一封激活邮件");
            model.addAttribute("target", "/index");
            return "/site/operate-result.html";
        } else {
            model.addAttribute("userNameMsg", map.get("userNameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register.html";
        }
    }

    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(@PathVariable("userId") int userId,
                             @PathVariable("code") String code,
                             Model model) {
        switch (userService.activation(userId, code)) {
            case ACTIVATION_FAILURE:
                model.addAttribute("msg", "激活失败, 请检查链接是否有误, 或在网站首页下方二维码联系开发者");
                model.addAttribute("target", "/index");
                return "/site/operate-result.html";
            case ACTIVATION_REPEAT:
                model.addAttribute("msg", "此链接已失效, 您可能已经激活过该账号, 请尝试直接登录");
                model.addAttribute("target", "/login");
                return "/site/operate-result.html";
            case ACTIVATION_SUCCESS:
                model.addAttribute("msg", "激活成功! 让我们一起畅所欲言!");
                model.addAttribute("target", "/login");
                return "/site/operate-result.html";
            default:
                return null;
        }
    }

    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将验证码传给服务器
        session.setAttribute("kaptcha", text);

        // 向浏览器输出图片
        response.setContentType("image/png");
        try {
            OutputStream responseOS = response.getOutputStream();
            ImageIO.write(image, "png", responseOS);
        } catch (IOException e) {
            logger.error("验证码响应失败: " + e.getMessage());
        }

    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(Model model, String userName, String password, String code, boolean rememberMe,
                        HttpServletResponse response, HttpSession session) {

        // 验证码校验
        String kaptcha = (String)session.getAttribute("kaptcha");
        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确");
            return "/site/login.html";
        }

        // 用户名密码校验
        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(userName, password, expiredSeconds);

        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("userLoginMsg", map.get("userLoginMsg"));
            return "/site/login.html";
        }

    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/login";
    }
}

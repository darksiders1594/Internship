package pers.internship.demo.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pers.internship.demo.dao.LoginTicketMapper;
import pers.internship.demo.dao.UserMapper;
import pers.internship.demo.entity.LoginTicket;
import pers.internship.demo.entity.User;
import pers.internship.demo.util.CommunityConstant;
import pers.internship.demo.util.CommunityUtil;
import pers.internship.demo.util.MailClient;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Value("${demo.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    // 通过 ID 查询用户
    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    // 用户注册
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        // 空值情况处理
        if (user == null) {
            throw new IllegalArgumentException("error: 参数不能为空");
        }
        if (StringUtils.isBlank(user.getUserName())) {
            map.put("userNameMsg", "用户名不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }

        // 创建一个临时虚拟用户, 用于进行注册验证
        User virtualUser;

        // 判断用户名是否重复
        virtualUser = userMapper.selectByUserName(user.getUserName());
        if (virtualUser != null) {
            map.put("userNameMsg", "用户名已存在");
            return map;
        }

        // 判断邮箱是否重复
        virtualUser = userMapper.selectByEmail(user.getEmail());
        if (virtualUser != null) {
            map.put("emailMsg", "该邮箱已被使用");
            return map;
        }

        // 判断用户密码格式合理性
        if (user.getPassword().length() < 8) {
            map.put("passwordMsg", "为了您的账号安全, 密码不要小于8位哦");
            return map;
        }

        if (user.getPassword().length() > 16) {
            map.put("passwordMsg", "密码太长啦! 请不要超过16位哦");
            return map;
        }

        // 对用户密码进行安全处理
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));

        // 对用户信息初始化
        user.setStatus(0);
        user.setType(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("/img/header/%d.jpg", new Random().nextInt(10)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 创建激活链接
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();

        // 发送激活邮件
        Context context = new Context();
        context.setVariable("username", user.getUserName());
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation.html", context);
        mailClient.sendMail(user.getEmail(), "账号激活", content);

        return map;
    }

    // 激活链接
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);

        if (user == null) {
            return ACTIVATION_FAILURE;
        }

        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        }

        if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    // 登录凭证
    public Map<String, Object> login(String userName, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(userName)) {
            map.put("userLoginMsg", "请输入用户名");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "请输入密码");
            return map;
        }

        User user = userMapper.selectByUserName(userName);

        // 校验用户名正确性
        if (user == null) {
            map.put("userLoginMsg", "用户名或密码错误");
            return map;
        }
        // 校验用户密码正确性
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("userLoginMsg", "用户名或密码错误");
            return map;
        }
        // 校验激活状态
        if (user.getStatus() != 1) {
            map.put("userLoginMsg", "账号还没有激活哦!");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket", loginTicket.getTicket());

        return map;

    }
}

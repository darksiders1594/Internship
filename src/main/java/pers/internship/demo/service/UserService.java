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

/**
 * @description 该类用于处理与用户相关的业务逻辑
 * @author Darksiders1594
 * @since 1.8
 * @see CommunityConstant 该接口用于维护一些常量
 */
@Service
public class UserService implements CommunityConstant {

    // 注入一个数据访问接口的实现对象, 主要处理用户数据
    @Autowired
    private UserMapper userMapper;

    // 注入一个邮箱客户端类的对象, 用于发送邮件
    @Autowired
    private MailClient mailClient;

    // 注入模板引擎
    @Autowired
    private TemplateEngine templateEngine;

    // 注入一个数据访问接口的实现对象, 主要处理登录凭证
    @Autowired
    private LoginTicketMapper loginTicketMapper;

    // 从配置文件的域名参数取值
    @Value("${demo.path.domain}")
    private String domain;

    // 从配置文件的项目路径参数取值
    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * 该方法根据用户 ID 查询用户
     * @param id 用户的主键 ID
     * @return 返回用户的实体类对象
     * @see User
     */
    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    /**
     * 该方法用于注册用户
     * @param user 用户的实体类对象
     * @return 返回以 Map 类型存放的错误信息
     * @see User
     */
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        // 空值情况处理
        if (user == null) {
            throw new IllegalArgumentException("方法参数不能为空");
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

        // 创建一个临时虚拟用户, 用于注册验证
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

        // 将用户数据正式存入数据库
        userMapper.insertUser(user);

        // 创建激活链接
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();

        // 发送激活邮件
        Context context = new Context();
        context.setVariable("username", user.getUserName());
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation.html", context);
        mailClient.sendMail(user.getEmail(), "账号激活", content);

        // 注册成功, 无错误信息返回
        return null;
    }

    /**
     * 该方法根据用户 ID 更改其账号的激活状态
     * @param userId 用户的主键 ID
     * @param code 激活码
     * @return 返回一个常量, 根据值代表激活状态
     * @see CommunityConstant 每个值的具体意义请查看常量接口
     */
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);

        if (user == null) {
            return ACTIVATION_FAILURE;
        }

        // 若 status 的值为 1, 表示账号已经激活
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

    /**
     * 该方法用于生成登录凭证
     * @param userName 用户名
     * @param password 密码
     * @param expiredSeconds 凭证生效时间 单位: 秒
     * @return 返回以 Map 类型存放的登录凭证或错误信息
     */
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

        // 通过用户名查询用户
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

        // 校验账号激活状态
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

        // 将登录凭证存放数据库
        loginTicketMapper.insertLoginTicket(loginTicket);

        // 以键值对返回凭证信息
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    /**
     * 该方法通过更改凭据状态来退出用户登录
     * @param ticket 凭证的 key
     */
    public void logout(String ticket) {
        // status 为 1 表示凭证失效
        loginTicketMapper.updateStatus(ticket, 1);
    }

    /**
     * 该方法用于找回密码时发送验证码邮件
     * @param email 用户邮箱
     * @param code 验证码
     */
    public void sendCodeEmail(String email, String code) {

        // 空值处理
        if (StringUtils.isBlank(email)) {
            return;
        }
        if (StringUtils.isBlank(code)) {
            return;
        }
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            return;
        }

        // 生成网页模板
        Context context = new Context();
        context.setVariable("username", user.getUserName());
        context.setVariable("code", code);
        String content = templateEngine.process("/mail/forget.html", context);

        // 发送验证码邮件
        mailClient.sendMail(user.getEmail(), "忘记密码", content);

    }

    /**
     * 该方法通过邮箱修改密码
     * @param email 邮箱
     * @param password 新密码
     * @return 返回以 Map 类型存放的错误信息
     */
    public Map<String, Object> updatePassword(String email, String password) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(email)) {
            map.put("emailMsg", "请输入邮箱");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "请输入新的密码");
            return map;
        }
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            map.put("emailMsg", "邮箱异常, 请联系网站开发者获取帮助");
            return map;
        }

        // 判断用户密码格式合理性
        if (password.length() < 8) {
            map.put("passwordMsg", "为了您的账号安全, 密码不要小于8位哦");
            return map;
        }
        if (password.length() > 16) {
            map.put("passwordMsg", "密码太长啦! 请不要超过16位哦");
            return map;
        }

        // 判断新密码是否与旧密码重复
        password = CommunityUtil.md5(password + user.getSalt());
        if (user.getPassword().equals(password)) {
            map.put("passwordMsg", "哎?! 这个密码与旧密码相同哦!");
            return map;
        }

        // 在数据库中更新密码
        userMapper.updatePassword(user.getId(), password);

        return null;
    }

    /**
     * 该方法用于查询登录凭证
     * @param ticket 凭证的 key
     * @return 返回登录凭证的实体类对象
     * @see LoginTicket
     */
    public LoginTicket findLoginTicket(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }

}

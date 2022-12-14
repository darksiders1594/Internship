package pers.internship.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pers.internship.demo.util.MailClient;

@SpringBootTest
@ContextConfiguration(classes = DemoApplication.class)
public class MailTests {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testSendMail() {
        Context context = new Context();
        context.setVariable("username", "darksiders1594");
        String content = templateEngine.process("/mail/activation", context);

        mailClient.sendMail("1594253369@qq.com", "账号激活", content);
    }

}

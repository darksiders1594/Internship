package pers.internship.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import pers.internship.demo.dao.DiscussPostMapper;
import pers.internship.demo.dao.LoginTicketMapper;
import pers.internship.demo.dao.UserMapper;
import pers.internship.demo.entity.DiscussPost;
import pers.internship.demo.entity.LoginTicket;
import pers.internship.demo.entity.User;

import java.util.Date;
import java.util.List;


@SpringBootTest
@ContextConfiguration(classes = DemoApplication.class)
class DemoApplicationTests {

// 这是 User Select 功能相关的测试用例
	@Autowired
	private UserMapper userMapper;

    @Test
    public void testSelectById() {
        User user = userMapper.selectById(101);
        System.out.println(user);
    }

    @Test
    public void testSelectByUserName() {
        User user = userMapper.selectByUserName("Darksiders");
        System.out.println(user);
    }

    @Test
    public void testSelectByEmail() {
        User user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

// 这是 User Insert 功能相关的测试用例
    @Test
    public void testInsertUser() {
        User user = new User();

        user.setUserName("Darksiders");
        user.setPassword("1594253369");
        user.setSalt("123456");
        user.setEmail("darksiders1594@gmail.com");
        user.setType(2);
        user.setStatus(1);
        user.setActivationCode("");
        user.setHeaderUrl("http://images.nowcoder.com/head/771t.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println("成功增加 " + rows + " 条数据");
    }

// 这是 User Update 功能相关的测试用例
    @Test
    public void testUpdatePassword() {
        int rows = userMapper.updatePassword(156, "123456789");
        System.out.println("成功更新 " + rows + " 条数据");
    }

    @Test
    public void testUpdateStatus() {
        int rows = userMapper.updateStatus(165, 1);
        System.out.println("成功更新 " + rows + " 条数据");
    }

// 这是 DiscussPost Select 功能相关的测试用例
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testSelectDiscussPosts() {
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149, 0, 10);
        for (DiscussPost post : list) {
            System.out.println(post);
        }
    }

    @Test
    public void testSelectDiscussPostRows() {
        int rows = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }

// 这是 LoginTicket Insert 功能相关的测试用例
    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Test
    public void testInsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(1);
        loginTicket.setTicket("test");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date());
        int rows = loginTicketMapper.insertLoginTicket(loginTicket);
        System.out.println("成功增加 " + rows + " 条数据");
    }

// 这是 LoginTicket Insert 功能相关的测试用例
    @Test
    public void testSelectByTicket() {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("test");

        System.out.println(loginTicket.toString());
    }

// 这是 LoginTicket Update 功能相关的测试用例
    @Test
    public void testUpdateTicketStatus() {
        int rows = loginTicketMapper.updateStatus("test", 1);

        System.out.println("成功更新 " + rows + " 条数据");
    }
}

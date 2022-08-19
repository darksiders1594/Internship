package pers.internship.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pers.internship.demo.dao.DiscussPostMapper;
import pers.internship.demo.entity.DiscussPost;

import java.util.List;

/**
 * @description 该类用于处理社区讨论贴相关的业务逻辑
 * @author Darksiders1594
 * @since 1.8
 */
@Service
public class DiscussPostService {

    // 注入一个数据访问接口的实现对象, 主要处理讨论贴数据
    @Autowired
    private DiscussPostMapper discussPostMapper;

    /**
     * 该方法根据用户 ID 查询未被拉黑的讨论贴
     * @param userId 用户的主键 ID, 当传入的值为 0 时表示查询全站讨论贴
     * @param offset 查询的起始行
     * @param limit 查询的上限
     * @return 返回值 List 类型用于存放所有帖子的实体类对象
     * @see DiscussPost
     */
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    /**
     * 该方法根据用户 ID 查询未被拉黑的帖子总数
     * @param userId 用户的主键 ID, 当传入的值为 0 时表示查询全站帖子总数
     * @return 返回值 int 类型用于存放帖子总数
     */
    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

}

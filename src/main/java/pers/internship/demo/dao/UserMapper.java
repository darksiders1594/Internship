package pers.internship.demo.dao;

import org.springframework.stereotype.Repository;
import org.apache.ibatis.annotations.Mapper;
import pers.internship.demo.entity.User;

@Repository
@Mapper
public interface UserMapper {
    User selectById(int id);
    User selectByUserName(String userName);
    User selectByEmail(String email);

    int insertUser(User user);

    int updatePassword(int id, String password);

}

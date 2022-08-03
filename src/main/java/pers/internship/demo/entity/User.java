package pers.internship.demo.entity;

import lombok.Data;

import java.util.Date;

@Data
public class User {

    private int id;
    private String userName;
    private String password;
    private String salt;
    private String email;
    private int type;
    private int status;
    private String activationCode;
    private String headerUrl;
    private Date createTime;

}

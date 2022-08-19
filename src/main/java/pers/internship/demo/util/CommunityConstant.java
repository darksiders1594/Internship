package pers.internship.demo.util;

/**
 * @description 该接口用于维护一些常量
 * @author Darksiders1594
 * @since 1.8
 */
public interface CommunityConstant {

    // 账号激活成功
    int ACTIVATION_SUCCESS = 0;

    // 账号激活重复
    int ACTIVATION_REPEAT = 1;

    // 账号激活失败
    int ACTIVATION_FAILURE = 2;

    // 默认状态的登陆凭证有效时间
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    // 信任状态的登陆凭证有效时间
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 90;

}

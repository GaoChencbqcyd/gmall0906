package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;

import java.util.List;

/**
 * @author gaochen
 * @create 2019-01-08 13:57
 */
public interface UserService {
    List<UserInfo> getUserInfoList();

    UserInfo login(UserInfo userInfo);

    void addUserCache(UserInfo userLogin);

    UserAddress getAddressById(String deliveryAddressId);

    List<UserAddress> getAddressListByUserId(String userId);

}

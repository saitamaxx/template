package com.natsu.template;

import com.natsu.template.biz.cache.RedisCacheManager;
import com.natsu.template.dao.UserInfoDAO;
import com.natsu.template.model.UserInfoDO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

/**
 * Created by sunyu on 2021-10-25
 */
@SpringBootTest
class MapperTest {

    @Autowired
    private UserInfoDAO userInfoDAO;

    @Autowired
    private RedisCacheManager redisCacheManager;

    @Test
    void contextLoads() {
    }

    @Test
    void mysqlTest() {
        UserInfoDO info = userInfoDAO.getUserInfoById(34L);
        System.out.println(info);
    }

    @Test
    void redisTest() {
        redisCacheManager.set("redisTest", "abcdefg", 10L, TimeUnit.MINUTES);
    }
}

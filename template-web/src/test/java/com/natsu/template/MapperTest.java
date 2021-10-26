package com.natsu.template;

import com.alibaba.fastjson.JSON;
import com.natsu.template.dao.UserInfoDAO;
import com.natsu.template.domain.UserInfoDO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Created by sunyu on 2021-10-25
 */
@SpringBootTest
class MapperTest {

    @Autowired
    private UserInfoDAO userInfoDAO;

    @Test
    void contextLoads() {
    }

    @Test
    void mysqlTest() {
        UserInfoDO info = userInfoDAO.getUserInfoById(14L);
        System.out.println(JSON.toJSONString(info));
    }
}

package com.natsu.template.dao;

import com.natsu.template.domain.UserInfoDO;
import org.springframework.stereotype.Repository;

/**
 * Created by sunyu on 2019-12-29
 */
@Repository
public interface UserInfoDAO {

    UserInfoDO getUserInfoById(Long id);
}

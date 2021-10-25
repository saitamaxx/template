package com.natsu.template.dao;

import com.natsu.template.model.UserInfoDO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by sunyu on 2019-12-29
 */
@Repository
public interface UserInfoDAO {

    UserInfoDO getUserInfoById(Long id);
}

package com.natsu.template.config;

import com.zaxxer.hikari.HikariDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * Created by sunyu on 2021-10-25
 */
@Configuration
@PropertySource(value = {"classpath:application-${spring.profiles.active}.properties"})
@MapperScan(basePackages = {"com.natsu.template.dao"}, annotationClass = Repository.class, sqlSessionFactoryRef = "sqlSessionFactoryBean")
public class DataSourceConfig {

    @Bean("mainDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public HikariDataSource dataSource() {
        return new HikariDataSource();
    }

    @Bean
    public DataSourceTransactionManager dataSourceTransactionManager(@Qualifier("mainDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public SqlSessionFactoryBean sqlSessionFactoryBean(@Qualifier("mainDataSource") DataSource dataSource) throws IOException {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setConfigLocation(pathMatchingResourcePatternResolver.getResource("classpath:mybatis-config.xml"));
        sqlSessionFactoryBean.setMapperLocations(pathMatchingResourcePatternResolver.getResources("classpath:mappers/*.xml"));
        return sqlSessionFactoryBean;
    }
}

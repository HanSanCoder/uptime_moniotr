package io.hansan.monitor.configs;

/**
 * @Author ：何汉叁
 * @Date ：2025/2/28 17:43
 * @Description：TODO
 */

import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.solon.annotation.Db;
import org.noear.solon.annotation.Configuration;

import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean(name = "db1", typed = true)
    public DataSource db1(@Inject("${solon.dataSource.db1}") HikariDataSource dataSource) {
        return dataSource;
    }
    @Bean
    public void db1_cfg(@Db("db1") org.apache.ibatis.session.Configuration cfg) {
        cfg.setCacheEnabled(false);
    }
}

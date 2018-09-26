package ksyun.dwtransport.config;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.Statement;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "datasource.api")
@MapperScan(basePackages = DbDriverConfig.PACKAGE, sqlSessionFactoryRef = "dcLiveSqlSessionFactoryAPI")
public class DbDriverConfig {
	static final String PACKAGE = "ksyun.dwtransport.mapper";
	private String url;
    private String username;
    private String password;
    
    @Primary
    @Bean(name = "dcLiveDataSourceAPI")
    public DataSource rdsDataSource() {
       return DataSourceUtil.fillExtraProperties(DataSourceBuilder.create().url(url).username(username).password(password).build());
    }
    @Primary
    @Bean(name = "dcLiveSqlSessionFactoryAPI")
    public SqlSessionFactory rdsSqlSessionFactory(@Qualifier("dcLiveDataSourceAPI") DataSource rdsDataSource) throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(rdsDataSource);
        return sessionFactory.getObject();
    }
    @Primary
    @Bean(name = "dcLiveAPIJdbcT")
    public JdbcTemplate createJdbcT(@Qualifier("dcLiveDataSourceAPI") DataSource rdsDataSource) throws Exception {
        return new JdbcTemplate(rdsDataSource);
    }

}

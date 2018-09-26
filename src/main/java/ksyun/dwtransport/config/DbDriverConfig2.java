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

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "datasource.api")
@MapperScan(basePackages = DbDriverConfig2.PACKAGE, sqlSessionFactoryRef = "dcLiveSqlSessionFactoryAPI2")
public class DbDriverConfig2 {
	static final String PACKAGE = "ksyun.dwtransport.mapper2";
	private String url2;
    private String username2;
    private String password2;
    
    @Bean(name = "dcLiveDataSourceAPI2")
    public DataSource rdsDataSource() {
       return DataSourceUtil.fillExtraProperties(DataSourceBuilder.create().url(url2).username(username2).password(password2).build());
    }
    @Bean(name = "dcLiveSqlSessionFactoryAPI2")
    public SqlSessionFactory rdsSqlSessionFactory(@Qualifier("dcLiveDataSourceAPI2") DataSource rdsDataSource) throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(rdsDataSource);
        return sessionFactory.getObject();
    }
    
    @Bean(name = "dcLiveAPIJdbcT2")
    public JdbcTemplate createJdbcT(@Qualifier("dcLiveDataSourceAPI2") DataSource rdsDataSource) throws Exception {
        return new JdbcTemplate(rdsDataSource);
    }

}

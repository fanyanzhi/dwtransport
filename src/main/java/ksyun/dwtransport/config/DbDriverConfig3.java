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
@MapperScan(basePackages = DbDriverConfig3.PACKAGE, sqlSessionFactoryRef = "dcLiveSqlSessionFactoryAPI3")
public class DbDriverConfig3 {
	static final String PACKAGE = "ksyun.dwtransport.mapper3";
	private String url3;
    private String username3;
    private String password3;
    
    @Bean(name = "dcLiveDataSourceAPI3")
    public DataSource rdsDataSource() {
       return DataSourceUtil.fillExtraProperties(DataSourceBuilder.create().url(url3).username(username3).password(password3).build());
    }
    @Bean(name = "dcLiveSqlSessionFactoryAPI3")
    public SqlSessionFactory rdsSqlSessionFactory(@Qualifier("dcLiveDataSourceAPI3") DataSource rdsDataSource) throws Exception {
        final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(rdsDataSource);
        return sessionFactory.getObject();
    }
    
    @Bean(name = "dcLiveAPIJdbcT3")
    public JdbcTemplate createJdbcT(@Qualifier("dcLiveDataSourceAPI3") DataSource rdsDataSource) throws Exception {
        return new JdbcTemplate(rdsDataSource);
    }

}

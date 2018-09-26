package ksyun.dwtransport.config;

import javax.sql.DataSource;

/**
 *
 *
 * @author FANJIAQI
 *
 */
public class DataSourceUtil {
	
	public static DataSource fillExtraProperties(DataSource dataSource) {
        org.apache.tomcat.jdbc.pool.DataSource ret = (org.apache.tomcat.jdbc.pool.DataSource)dataSource;

        ret.setValidationQuery("SELECT 1");
        ret.setTestOnBorrow(true);
        ret.setInitialSize(1);
        ret.setMaxActive(15);
        ret.setMinIdle(1);

        return ret;
    }
}

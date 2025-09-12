package ru.sentidas.rangiffler.data.jdbc;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.p6spy.engine.spy.P6DataSource;
import org.apache.commons.lang3.StringUtils;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class DataSources {

    private DataSources() {
    }

    private static final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();

    public static DataSource dataSource(String jdbcUrl) {

        return dataSources.computeIfAbsent(
                jdbcUrl,
                key -> {
                    AtomikosDataSourceBean dsBean = new AtomikosDataSourceBean();
                    String uniqId = StringUtils.substringAfter(jdbcUrl, "3306/");
                    uniqId = StringUtils.substringBefore(uniqId, "?");

                    dsBean.setUniqueResourceName(uniqId);
                    dsBean.setXaDataSourceClassName("com.mysql.cj.jdbc.MysqlXADataSource");

                    Properties props = new Properties();
                    props.put("url", jdbcUrl);
                    props.put("user", "root");
                    props.put("password", "root");

                    dsBean.setXaProperties(props);
                    dsBean.setPoolSize(3);
                    dsBean.setMaxPoolSize(10);
                    P6DataSource p6DataSource = new P6DataSource(
                            dsBean
                    );
                    try {
                        InitialContext context = new InitialContext();
                        context.bind("java:comp/env/jdbc/" + uniqId, p6DataSource);
                    } catch (NamingException e) {
                        throw new RuntimeException(e);
                    }
                    return p6DataSource;
                }
        );
    }
}

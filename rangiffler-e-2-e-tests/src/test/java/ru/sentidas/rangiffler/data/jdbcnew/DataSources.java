//package ru.sentidas.rangiffler.data.jdbcnew;
//
//import com.atomikos.jdbc.AtomikosDataSourceBean;
//import com.mysql.cj.jdbc.MysqlXADataSource;
//import com.p6spy.engine.spy.P6DataSource;
//import org.apache.commons.lang3.StringUtils;
//
//import javax.naming.InitialContext;
//import javax.naming.NamingException;
//import javax.sql.DataSource;
//import java.sql.SQLException;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class DataSources {
//    private DataSources() {}
//
//    private static final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();
//
//    public static DataSource dataSource(String jdbcUrl) {
//        return dataSources.computeIfAbsent(jdbcUrl, key -> {
//            try {
//                String uniqId = StringUtils.substringAfter(jdbcUrl, "3306/");
//                uniqId = StringUtils.substringBefore(uniqId, "?");
//
//                // XA-датасорс MySQL
//                MysqlXADataSource xa = new MysqlXADataSource();
//                xa.setUrl(jdbcUrl);
//                xa.setUser("root");
//                xa.setPassword("root");
//              //  xa.setPinGlobalTxToPhysicalConnection(true); // критично для MySQL+XA
//
//                AtomikosDataSourceBean dsBean = new AtomikosDataSourceBean();
//                dsBean.setUniqueResourceName(uniqId);
//                dsBean.setXaDataSource(xa);
//                dsBean.setPoolSize(3);
//                dsBean.setMaxPoolSize(10);
//
//                // ⬇️ P6Spy поверх Atomikos
//                DataSource p6 = new P6DataSource(dsBean);
//
//                new InitialContext().rebind("java:comp/env/jdbc/" + uniqId, p6);
//                return p6;
////            } catch (NamingException | SQLException e) {
////                throw new RuntimeException(e);
//           } catch (NamingException e) {
//                throw new RuntimeException(e);
//            }
//        });
//    }
//}

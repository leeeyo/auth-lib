//package com.digi.auth.config;
//
//import com.zaxxer.hikari.HikariDataSource;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import javax.sql.DataSource;
//
//@Configuration
//public class DatabaseConfig {
//
//    @Bean
//    @ConfigurationProperties(prefix = "app.datasource")
//    @ConditionalOnProperty(name = "authentication.type", havingValue = "db")
//    public DataSource getDatabaseContext() {
//        HikariDataSource dataSource = new HikariDataSource();
//        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/mydb");
//        dataSource.setUsername("");
//        dataSource.setPassword("");
//        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
//        return dataSource;
//    }
//
////        @Bean
////        @ConfigurationProperties(prefix = "app.datasource")
////        @ConditionalOnProperty(name = "authentication.type", havingValue = "db")
////
////        public HikariDataSource dataSource() {
////            return DataSourceBuilder.create().type(HikariDataSource.class).build();
////        }
//}

package com.ebi.ega;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
@Configuration
@PropertySource("file:${config}")

public class AppConfig {
    @Autowired
    public Environment env;

    @Bean
    public GetFileIndex getFileIndex() { return new GetFileIndex();}

    @Bean
    public String getConfigProperty (){
        return env.getRequiredProperty("internal_key.path");
    }

    @Bean
    public Encryption encryption (){ return new Encryption();}

    @Bean
    public Decryption decryption (){ return new Decryption();}

    @Bean
    public GetMD5ForFile getMD5ForFile (){ return new GetMD5ForFile();}

    @Bean
    public ProfilerAdaptor profilerAdaptor() {return new ProfilerAdaptor();}



    @Bean
    public DataSource audit() {
        DriverManagerDataSource audit = new DriverManagerDataSource();
        audit.setDriverClassName(env.getRequiredProperty("audit_test.driverClassName"));
        audit.setUrl(env.getRequiredProperty("audit_test.url"));
        audit.setUsername(env.getRequiredProperty("audit_test.user"));
        audit.setPassword(env.getRequiredProperty("audit_test.password"));
        return audit;
    }

    @Bean
    public DataSource profiler() {
        DriverManagerDataSource profiler = new DriverManagerDataSource();
        profiler.setDriverClassName(env.getRequiredProperty("profiler_test.driverClassName"));
        profiler.setUrl(env.getRequiredProperty("profiler_test.url"));
        profiler.setUsername(env.getRequiredProperty("profiler_test.user"));
        profiler.setPassword(env.getRequiredProperty("profiler_test.password"));
        return profiler;
    }
}

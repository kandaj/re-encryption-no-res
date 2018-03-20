package com.ebi.ega;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;

@Configuration
@PropertySource("file:${config}")

public class AppConfig {
    @Autowired
    public Environment env;

    @Bean
    public GetFileIndex getFileIndex() { return new GetFileIndex();}

    @Bean
    public HashMap getConfigProperty (){
        HashMap configuartionKeys = new HashMap();

        configuartionKeys.put("internal_key",env.getRequiredProperty("internal_key.path"));
        configuartionKeys.put("sanger_key",env.getRequiredProperty("sanger_key.path"));
        configuartionKeys.put("public_key",env.getRequiredProperty("public_key.path"));
        configuartionKeys.put("gpg_private_key",env.getRequiredProperty("gpg_private_key.path"));
        configuartionKeys.put("gpg_public_key",env.getRequiredProperty("gpg_public_key.path"));

        return configuartionKeys;
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

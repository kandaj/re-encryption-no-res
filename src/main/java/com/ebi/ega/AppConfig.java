package com.ebi.ega;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@PropertySource("classpath:application.properties")

public class AppConfig {
    @Autowired
    public Environment env;

    @Bean
    public GetFileIndex getFileIndex() { return new GetFileIndex();}

    @Bean
    public HashMap getConfigProperty (){
        HashMap configurationKeys = new HashMap();

        configurationKeys.put("internal_key",env.getRequiredProperty("cgpInternalKeyFile"));
        configurationKeys.put("sanger_key",env.getRequiredProperty("sangerKeyFile"));
        configurationKeys.put("public_key",env.getRequiredProperty("publicGPGKeyFile"));
        configurationKeys.put("gpg_private_key",env.getRequiredProperty("gpgPrivateFile"));
        configurationKeys.put("gpg_public_key",env.getRequiredProperty("gpgPublicFile"));
        configurationKeys.put("gpg_public_key",env.getRequiredProperty("gpgPublicFile"));
        configurationKeys.put("staging_area",env.getRequiredProperty("stagingArea.dir"));

        return configurationKeys;
    }

    @Bean
    public ReEncryption reEncryption (){ return new ReEncryption();}

    @Bean
    public Decryption decryption (){ return new Decryption();}

    @Bean
    public GetMD5ForFile getMD5ForFile (){ return new GetMD5ForFile();}

    @Bean
    public ProfilerAdaptor profilerAdaptor() {return new ProfilerAdaptor();}



    @Bean
    public DataSource audit() {
        DriverManagerDataSource audit = new DriverManagerDataSource();
        audit.setDriverClassName(env.getRequiredProperty("auditDriverClassName"));
        audit.setUrl(env.getRequiredProperty("auditConnection"));
        audit.setUsername(env.getRequiredProperty("auditUser"));
        audit.setPassword(env.getRequiredProperty("auditPassword"));
        return audit;
    }

    @Bean
    public DataSource profiler() {
        DriverManagerDataSource profiler = new DriverManagerDataSource();
        profiler.setDriverClassName(env.getRequiredProperty("profilerClassName"));
        profiler.setUrl(env.getRequiredProperty("profilerConnection"));
        profiler.setUsername(env.getRequiredProperty("profilerUser"));
        profiler.setPassword(env.getRequiredProperty("profilerPassword"));
        return profiler;
    }
}

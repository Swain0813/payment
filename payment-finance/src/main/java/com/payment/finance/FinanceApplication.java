package com.payment.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import tk.mybatis.spring.mapper.MapperScannerConfigurer;

import java.util.Properties;

@EnableFeignClients
@EnableSwagger2
@SpringBootApplication(scanBasePackages = "com.payment")
@EnableEurekaClient
@EnableDiscoveryClient
public class FinanceApplication extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(FinanceApplication.class);
    }

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {

        Properties properties = new Properties();
        properties.put("mappers", "tk.mybatis.mapper.common.Mapper,tk.mybatis.mapper.common.MySqlMapper,com.payment.common.base.IdInserListMapper");

        MapperScannerConfigurer msc = new MapperScannerConfigurer();
        msc.getMapperHelper().setProperties(properties);
        msc.setBasePackage("com.payment.finance.dao");
        return msc;
    }

    public static void main(String[] args) {
        SpringApplication.run(FinanceApplication.class, args);
    }

}

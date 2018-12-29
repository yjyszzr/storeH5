package com.dl.store;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import com.dl.base.configurer.FeignConfiguration;
import com.dl.base.configurer.RestTemplateConfig;
import com.dl.store.configurer.Swagger2;
import com.dl.store.configurer.WebMvcConfigurerStore;
import com.dl.store.core.ProjectConstant;

@SpringBootApplication
@Import({ RestTemplateConfig.class, Swagger2.class, WebMvcConfigurerStore.class, FeignConfiguration.class })
@MapperScan(basePackages = { ProjectConstant.MAPPER_PACKAGE, "com.dl.store.dao3","com.dl.store.dao2","com.dl.store.dao" })
@EnableEurekaClient
@EnableFeignClients({"com.dl.member.api", "com.dl.order.api","com.dl.lottery.api","com.dl.shop.auth.api","com.dl.shop.payment.api"})
@EnableTransactionManagement
public class StoreServiceApplication {

	// swagger-ui.html
	public static void main(String[] args) {
		SpringApplication.run(StoreServiceApplication.class, args);
	}
	
}

package com.dl.store.configurer;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import com.dl.base.aspect.AccessLogAspect;
import com.dl.base.configurer.WebMvcConfigurer;

@Configuration
@Import(AccessLogAspect.class)
public class WebMvcConfigurerStore extends WebMvcConfigurer{

	/** 
     * 防止@EnableMvc把默认的静态资源路径覆盖了，手动设置的方式
     * @param registry
     */
    @Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/META-INF/resources/").setCachePeriod(0);
    }
}

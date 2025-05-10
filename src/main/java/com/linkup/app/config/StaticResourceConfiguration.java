package com.linkup.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfiguration implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // any request to /uploads/** will be served from the file-system folder ./uploads/
        registry
                .addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/")   // relative to working dir; or absolute: "file:/opt/app/uploads/"
                .setCachePeriod(3600)                      // optional: 1 hour cache
                .resourceChain(true);
    }
}

package org.radish.spring.boot.autoconfigure;

import org.sam.shen.core.http.HandlerLogServlet;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author clock
 * @date 2018/12/20 下午3:08
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(name = "radish.log-view-mode", havingValue = "servlet", matchIfMissing = true)
@AutoConfigureAfter(RadishAutoConfiguration.class)
public class RadishLogAutoConfiguration {

    @Bean
    public ServletRegistrationBean logServletRegistrationBean() {
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean();
        servletRegistrationBean.setServlet(new HandlerLogServlet());
        servletRegistrationBean.addUrlMappings("/handler-log");
        return servletRegistrationBean;
    }

}

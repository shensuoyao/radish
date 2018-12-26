package org.radish.spring.boot.autoconfigure;

import org.sam.shen.core.http.HandlerLogServlet;
import org.sam.shen.core.netty.HandlerLogNettyServer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
@ConditionalOnClass({HandlerLogServlet.class, HandlerLogNettyServer.class})
@AutoConfigureAfter(RadishAutoConfiguration.class)
public class RadishLogAutoConfiguration {

    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnProperty(name = "radish.log-view-mode", havingValue = "servlet", matchIfMissing = true)
    public ServletRegistrationBean logServletRegistrationBean() {
        ServletRegistrationBean<HandlerLogServlet> servletRegistrationBean = new ServletRegistrationBean<>();
        servletRegistrationBean.setServlet(new HandlerLogServlet());
        servletRegistrationBean.addUrlMappings("/handler-log");
        return servletRegistrationBean;
    }

    @Bean(initMethod = "start")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "radish.log-view-mode", havingValue = "netty")
    public HandlerLogNettyServer handlerLogNetty(RadishProperties properties) {
        return HandlerLogNettyServer.getInstance(properties.getLogViewNetty().getPort());
    }

}

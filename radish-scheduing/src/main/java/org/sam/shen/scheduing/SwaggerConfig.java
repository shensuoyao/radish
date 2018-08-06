package org.sam.shen.scheduing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author suoyao
 * @date 2018年7月31日 下午3:19:12
  * 
 */
@Configuration
@EnableSwagger2
@ComponentScan(basePackages = { "org.sam.shen.scheduing" })
public class SwaggerConfig {
	
	/**
	 * @author suoyao
	 * @date 2018年7月31日
	 * @return
	 */
	ApiInfo apiInfo() {
		return new ApiInfoBuilder().title("Radish Scheduing APIs")
		        .description("More about radish-scheduing：https://www.github.com/shensuoyao")
		        .termsOfServiceUrl("这里是团队服务URL").version("1.0.1").build();
	}

	/**
	 * @author suoyao
	 * @date 2018年7月31日
	 * @return
	 */
	@Bean
	public Docket customImplementation() {
		return new Docket(DocumentationType.SWAGGER_2).select()
		        .apis(RequestHandlerSelectors.basePackage("org.sam.shen")).paths(PathSelectors.any()).build()
		        .directModelSubstitute(org.joda.time.LocalDate.class, java.sql.Date.class)
		        .directModelSubstitute(org.joda.time.DateTime.class, java.util.Date.class).apiInfo(apiInfo());
	}
}

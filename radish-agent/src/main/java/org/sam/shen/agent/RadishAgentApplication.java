package org.sam.shen.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"org.sam.shen.core"})
@SpringBootApplication
public class RadishAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(RadishAgentApplication.class, args);
	}
}

package com.project.calendar.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration // 스프링 실행 시 설정파일을 읽기 위한 어노테이션
public class SwaggerConfiguration {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("프로젝트 캘린더")
                .version("0.0.1")
                .description("캘린더 API");

        return new OpenAPI()
                .components(new Components())
                .info(info);
    }
}

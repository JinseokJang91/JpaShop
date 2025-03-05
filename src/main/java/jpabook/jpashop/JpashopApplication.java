package jpabook.jpashop;

import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpashopApplication.class, args);
	}

	@Bean
	Hibernate5JakartaModule hibernate5JakartaModule() {
		return new Hibernate5JakartaModule();

		// LAZY LOADING 설정 객체 강제 추출
		// => API 스펙 노출 위험성 (엔티티 노출)
		// => 성능 문제
//		Hibernate5JakartaModule module = new Hibernate5JakartaModule();
//		module.configure(Hibernate5JakartaModule.Feature.FORCE_LAZY_LOADING, true);
//		return module;
	}
}

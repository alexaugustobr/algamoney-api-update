package com.example.algamoney.api.cors;

import com.example.algamoney.api.config.property.AlgamoneyApiProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;
import java.util.List;

@Configuration
public class CorsFilterConfig {

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterFilterRegistrationBean(AlgamoneyApiProperty apiProperty) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);

        config.setAllowedOrigins(List.of(apiProperty.getOriginPermitida()));
        config.setAllowedMethods(Collections.singletonList("*"));
        config.setAllowedHeaders(Collections.singletonList("*"));


        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        
        return bean;
    }
}
//	@Autowired
//	private AlgamoneyApiProperty algamoneyApiProperty;	
//
//    @Override
//    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
//            throws IOException, ServletException {
//        		
//        HttpServletRequest request = (HttpServletRequest) req;
//		HttpServletResponse response = (HttpServletResponse) resp;
//		
//		response.setHeader("Access-Control-Allow-Origin", algamoneyApiProperty.getOriginPermitida());
//        response.setHeader("Access-Control-Allow-Credentials", "true");
//		
//		if ("OPTIONS".equals(request.getMethod()) && algamoneyApiProperty.getOriginPermitida().equals(request.getHeader("Origin"))) {
//			response.setHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, PUT, OPTIONS");
//        	response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Accept");
//        	response.setHeader("Access-Control-Max-Age", "3600");
//			
//			response.setStatus(HttpServletResponse.SC_OK);
//		} else {
//			chain.doFilter(req, resp);
//		}
//        
//    }
    
//}

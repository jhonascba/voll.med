package med.voll.api.infra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfigurations {

    @Autowired
    private SecurityFilter securityFilter;

    @Bean //serve para exportar uma classe para o Spring fazendo com que ele consiga carregá-la e realiza a sua injeção de dependência em outras classes
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        //desabilita ataques do tipo Cross-Site Request Forgery (CSRF)
        //...será desabilitado porque iremos trabalhar com autênticação via token,que já possui essa proteção
        return httpSecurity.csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().authorizeHttpRequests()
                .requestMatchers(HttpMethod.POST, "/login").permitAll() //permitindo que a determinada rota seja permitida sem ter token
                .requestMatchers("/v3/api-docs/**", "swagger-ui.html", "/swagger-ui/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/medicos").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/pacientes").hasRole("ADMIN")
                .anyRequest().authenticated()
                .and().addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class) //filtro criado para ser executado antes do filtro do Security
                .build();
    }

    //injeta o AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    //usar o hashing de senha "BCrypt"
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

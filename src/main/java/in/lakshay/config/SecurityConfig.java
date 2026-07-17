package in.lakshay.config;

import jakarta.servlet.http.HttpServletResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import lombok.extern.slf4j.Slf4j; // for logging

// main security config - handles auth, cors, csrf, etc
// this is the most important security class in the app
@Configuration
@EnableWebSecurity // turns on spring security
@EnableMethodSecurity(prePostEnabled = true) // lets us use @PreAuthorize annotations
@Slf4j // adds logging support
public class SecurityConfig {
	// CORS settings - same as in WebConfig but needed here too
	@Value("${spring.web.cors.allowed-origins:http://localhost:5173}")
	private String allowedOrigins; // frontend origins

	@Value("${spring.web.cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
	private String allowedMethods; // http methods

	@Value("${spring.web.cors.allowed-headers:Authorization,Content-Type,X-Requested-With,Accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers}")
	private String allowedHeaders; // headers browser can send

	@Value("${spring.web.cors.exposed-headers:Authorization,Content-Type}")
	private String exposedHeaders; // headers browser can access

	@Value("${spring.web.cors.allow-credentials:true}")
	private boolean allowCredentials; // cookies etc

	@Value("${spring.web.cors.max-age:3600}")
	private long maxAge; // preflight cache time

	// services and filters we need
	@Autowired
	private UserDetailsService userDetailsService; // loads user details

	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter; // handles JWT auth

	// creates the main auth provider that checks username/password
	@Bean
	public AuthenticationProvider authProvider() {
		// this is the standard spring security auth provider
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService); // how to load users
		provider.setPasswordEncoder(passwordEncoder()); // how to check passwords
		return provider;
	}

	// password encoder for hashing user passwords
	@Bean
	public PasswordEncoder passwordEncoder() {
		// bcrypt with strength 12 - good balance of security vs performance
		// higher = more secure but slower, lower = less secure but faster
		Map<String, PasswordEncoder> encoders = new HashMap<>();
		encoders.put("bcrypt", new BCryptPasswordEncoder(12)); // our main encoder
		// could add more encoders here if needed

		// delegating encoder lets us support multiple formats
		// useful for migrating between password schemes
		return new DelegatingPasswordEncoder("bcrypt", encoders);
	}

	// creates the auth manager that coordinates authentication
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		// get the default manager from spring
		AuthenticationManager manager = authConfig.getAuthenticationManager();

		return manager; // return the configured manager
	}

	// this is where all the security magic happens
	// defines the security filter chain and all the rules
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			// configure CORS - needed for frontend to talk to API
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))

			// disable CSRF - not needed for stateless REST API with JWT
			// CSRF is for browser cookies/session auth
			.csrf(AbstractHttpConfigurer::disable)

			// URL-based security rules
			.authorizeHttpRequests(auth -> auth
				// public endpoints anyone can access
				.requestMatchers("/api/v1/auth/**").permitAll() // login/register
				.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll() // api docs
				.requestMatchers("/error").permitAll() // error pages
				.requestMatchers("/api/v1/movies/**").permitAll() // movie browsing
				.requestMatchers("/api/v1/theaters/**").permitAll() // theater info
				.requestMatchers("/api/v1/showtimes/**").permitAll() // showtime listings
				.requestMatchers("/api/v1/seats/showtimes/**").permitAll() // seat availability
				.requestMatchers("/api/v1/payments/webhook").permitAll() // stripe callbacks
				.requestMatchers("/api/v1/health").permitAll() // for monitoring
				.requestMatchers("/actuator/health/**").permitAll() // render health checks
				.requestMatchers("/api/v1/diagnostics/**").authenticated() // debug stuff - need auth
				.anyRequest().authenticated() // everything else needs login
			)

			// we're stateless - no sessions, just JWT
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)

			// add our auth provider
			.authenticationProvider(authProvider())

			// add JWT filter before the standard auth filter
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

			// handle auth errors nicely
			.exceptionHandling(ex -> ex
				// when someone tries to access something without being logged in
				.authenticationEntryPoint((request, response, authException) -> {
					log.error("Auth failed: {}", authException.getMessage());
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
				})
				// when someone is logged in but doesn't have permission
				.accessDeniedHandler((request, response, accessDeniedException) -> {
					log.error("Permission denied: {}", accessDeniedException.getMessage());
					response.sendError(HttpServletResponse.SC_FORBIDDEN, "Error: Forbidden");
				})
			);

		// finally build and return the config
		return http.build();
	}

	// utility for mapping between DTOs and entities
	@Bean
	public ModelMapper modelMapper() {
		// could configure custom mappings here if needed
		return new ModelMapper();
	}

	// CORS config for security filter chain
	// this is separate from WebConfig's CORS setup
	// spring security needs its own CORS config 🙄
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		// handle multiple origins if needed
		// in prod this is just the frontend URL
		String[] origins = allowedOrigins.split(",");
		for (String origin : origins) {
			// using patterns lets us handle subdomains etc
			configuration.addAllowedOriginPattern(origin.trim());
		}

		// allowed HTTP methods
		configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));

		// headers the browser can send
		configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));

		// headers the browser can read from responses
		configuration.setExposedHeaders(Arrays.asList(exposedHeaders.split(",")));

		// allow cookies and auth headers
		configuration.setAllowCredentials(allowCredentials);

		// browser can cache preflight response for this long
		configuration.setMaxAge(maxAge); // 1 hour

		// apply to all API paths
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	// TODO: add remember-me functionality later?
}

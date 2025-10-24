package com.waturnos.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.waturnos.entity.Tenant;
import com.waturnos.entity.User;
import com.waturnos.repository.TenantRepository;
import com.waturnos.repository.UserRepository;

/**
 * The Class DataInitializer.
 */
@Configuration
public class DataInitializer {

	/**
	 * Inits the data.
	 *
	 * @param userRepository the user repository
	 * @param tenantRepository the tenant repository
	 * @return the command line runner
	 */
	@Bean
	CommandLineRunner initData(UserRepository userRepository, TenantRepository tenantRepository) {
		return args -> {
			// si no hay tenants, creamos uno de ejemplo
			Tenant tenant = tenantRepository.findByName("Barbería Central").orElseGet(() -> {
				Tenant t = new Tenant();
				t.setName("Barbería Central");
				t.setApiKey("ABC123456");
				return tenantRepository.save(t);
			});

			// verificar si ya existe el usuario admin
			if (userRepository.findByEmail("admin@barberia.com").isEmpty()) {
				BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
				String passwordHash = encoder.encode("secret123");

				User admin = new User();
				admin.setName("Admin Barbería");
				admin.setEmail("admin@barberia.com");
				admin.setPasswordHash(passwordHash);
				admin.setRole("OWNER");
				admin.setTenant(tenant);

				userRepository.save(admin);
				System.out.println("✅ Usuario admin creado: admin@barberia.com / secret123");
			} else {
				System.out.println("ℹ️ Usuario admin ya existente, no se creó otro.");
			}
		};
	}
}
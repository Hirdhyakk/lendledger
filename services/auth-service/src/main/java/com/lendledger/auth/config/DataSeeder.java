package com.lendledger.auth.config;

import com.lendledger.auth.domain.UserEntity;
import com.lendledger.auth.repository.UserRepository;
import com.lendledger.common.security.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("!test")
public class DataSeeder {

    @Bean
    CommandLineRunner seedAdmin(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            if (!userRepository.existsByEmail("admin@lendledger.local")) {
                UserEntity admin = new UserEntity();
                admin.setEmail("admin@lendledger.local");
                admin.setPasswordHash(encoder.encode("password"));
                admin.setRole(Role.ADMIN);
                admin.setFullName("System Admin");
                admin.setPhone("9999999999");
                userRepository.save(admin);
            }
            if (!userRepository.existsByEmail("borrower1@lendledger.local")) {
                UserEntity b1 = new UserEntity();
                b1.setEmail("borrower1@lendledger.local");
                b1.setPasswordHash(encoder.encode("password"));
                b1.setRole(Role.BORROWER);
                b1.setFullName("Borrower One");
                userRepository.save(b1);
            }
            if (!userRepository.existsByEmail("borrower2@lendledger.local")) {
                UserEntity b2 = new UserEntity();
                b2.setEmail("borrower2@lendledger.local");
                b2.setPasswordHash(encoder.encode("password"));
                b2.setRole(Role.BORROWER);
                b2.setFullName("Borrower Two");
                userRepository.save(b2);
            }
        };
    }
}

package com.template.letschat;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.mysql.cj.jdbc.MysqlDataSource;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import javax.persistence.*;
import java.time.LocalDateTime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

//import java.util.List;
import java.util.Optional;
//import java.util.stream.Collectors;

import org.mindrot.jbcrypt.BCrypt;
//import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
@SpringBootApplication
@EnableJpaRepositories("com.example.package.repository")
public class LetsChatApplication {

	@Bean
	public DataSource getDataSource() {
		DataSourceBuilder<MysqlDataSource> dataSourceBuilder = DataSourceBuilder.create().type(MysqlDataSource.class);
		dataSourceBuilder.driverClassName("com.mysql.cj.jdbc.Driver");
		dataSourceBuilder.url("jdbc:mysql://localhost:3306/chat_app_db");
		dataSourceBuilder.username("root");
		dataSourceBuilder.password("IamGROOT2!");
		return dataSourceBuilder.build();
	}

	@Entity
	public class User {
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long id;

		public String username;
		public String password;
		public String email;

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}
	public interface UserRepository extends JpaRepository<User, Long> {
		Optional<User> findByUsername(String username);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new PasswordEncoder() {
			@Override
			public String encode(CharSequence rawPassword) {
				return BCrypt.hashpw(rawPassword.toString(), BCrypt.gensalt());
			}

			@Override
			public boolean matches(CharSequence rawPassword, String encodedPassword) {
				return BCrypt.checkpw(rawPassword.toString(), encodedPassword);
			}
		};
	}

	@Entity
	public class FailedLoginAttempt {
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		public Long id;
		public String username;
		public LocalDateTime attemptTime;

		public void setUsername(String username) {
			this.username = username;
		}

		public void setAttemptTime(LocalDateTime attemptTime) {
			this.attemptTime = attemptTime;
		}

		// Constructors, getters, and setters
	}

	// UserService class
	@Service
	public class UserService {
		private final UserRepository userRepository;
		private final PasswordEncoder passwordEncoder;
		private final FailedLoginAttemptRepository failedLoginAttemptRepository;

		public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, FailedLoginAttemptRepository failedLoginAttemptRepository) {
			this.userRepository = userRepository;
			this.passwordEncoder = passwordEncoder;
			this.failedLoginAttemptRepository = failedLoginAttemptRepository;
		}

		public boolean authenticateUser(String username, String password) {
			Optional<User> userOptional = userRepository.findByUsername(username);
			if (userOptional.isPresent()) {
				User user = userOptional.get();
				int failedAttempts = countFailedLoginAttempts(username);
				if (failedAttempts >= 10) {
					// User has exceeded the maximum number of failed attempts, deny further attempts
					return false;
				}

				if (passwordEncoder.matches(password, user.getPassword())) {
					// Password is correct, reset failed attempts
					resetFailedLoginAttempts(username);
					return true;
				} else {
					// Password is incorrect, log the failed attempt
					logFailedLoginAttempt(username);
				}
			}
			return false;
		}

		private int countFailedLoginAttempts(String username) {
			LocalDateTime threshold = LocalDateTime.now().minusMinutes(5); // Consider attempts within the last 5 minutes
			return failedLoginAttemptRepository.countByUsernameAndAttemptTimeAfter(username, threshold);
		}

		private void logFailedLoginAttempt(String username) {
			FailedLoginAttempt attempt = new FailedLoginAttempt();
			attempt.setUsername(username);
			attempt.setAttemptTime(LocalDateTime.now());
			failedLoginAttemptRepository.save(attempt);
		}

		private void resetFailedLoginAttempts(String username) {
			failedLoginAttemptRepository.deleteAllByUsername(username);
		}

		public interface FailedLoginAttemptRepository extends JpaRepository<FailedLoginAttempt, Long> {
			int countByUsernameAndAttemptTimeAfter(String username, LocalDateTime time);

			void deleteAllByUsername(String username);
		}

		public User saveUser(User user) {
			String hashedPassword = passwordEncoder.encode(user.getPassword());
        	user.setPassword(hashedPassword);
			return userRepository.save(user);
		}

		public Optional<User> findUserByUsername(String username) {
			return userRepository.findByUsername(username);
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(LetsChatApplication.class, args);
	}

}

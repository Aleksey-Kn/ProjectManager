package ru.manager.ProgectManager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Properties;

@SpringBootApplication
public class ProjectManagerApplication {

	public static void main(String[] args) throws SocketException {
		SpringApplication.run(ProjectManagerApplication.class, args);
		Iterator<NetworkInterface> it = NetworkInterface.getNetworkInterfaces().asIterator();
		while (it.hasNext()){
			Iterator<InetAddress> iterator = it.next().getInetAddresses().asIterator();
			while (iterator.hasNext()){
				System.out.println(iterator.next());
			}
		}
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}

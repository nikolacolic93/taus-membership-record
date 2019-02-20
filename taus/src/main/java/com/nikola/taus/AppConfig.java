package com.nikola.taus;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

import com.nikola.taus.entities.Contact;
import com.nikola.taus.entities.Person;
import com.nikola.taus.view.MainFrame;


@Configuration
@ComponentScan(basePackages = "com.nikola.taus")
@PropertySource("classpath:prod.properties")
public class AppConfig {
	
	@Autowired
	public Environment env;
	
	@Bean
	public DataSource dataSource() {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName(env.getProperty("db.driver"));
		ds.setUrl(env.getProperty("db.url"));
		ds.setUsername(env.getProperty("db.user"));
		ds.setPassword(env.getProperty("db.pass"));
		return ds;
	}
	
	@Bean
	@Scope(value = "prototype")
	public Contact contact() {
		return new Person();
	}
	
	@Bean(initMethod = "refreshData")
	public MainFrame mainFrame() {
		return new MainFrame();
	}
	
}

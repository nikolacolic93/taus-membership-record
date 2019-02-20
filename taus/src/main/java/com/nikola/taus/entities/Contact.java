package com.nikola.taus.entities;

public interface Contact {
	Long getId();
	
	void setId(Long id);
	
	String getName();

	void setName(String name);

	String getAddress();

	void setAddress(String address);

	String getEmail();

	void setEmail(String email);

	String getTelephone();

	void setTelephone(String telephone);

	String getOrientation();

	void setOrientation(String orientation);

	String getMembership();

	void setMembership(String membership);
	
	
}

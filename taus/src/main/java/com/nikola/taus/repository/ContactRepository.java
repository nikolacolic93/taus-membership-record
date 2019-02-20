package com.nikola.taus.repository;

import java.util.List;

import com.nikola.taus.entities.Contact;

public interface ContactRepository {
	public enum Order{ ASC, DESC };
	
	List<Contact> getContacts();
	
	List<Contact> getContacts(Order order);
	
	Contact getContact(Long id);
	
	Contact getLastContact();
	
	void createContact();
	
	void deleteContact(Long id);
	
	void updateContact(Contact c);
}

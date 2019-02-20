package com.nikola.taus.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.nikola.taus.entities.Contact;
import com.nikola.taus.entities.Person;

@Repository
public class JdbcContactRepository implements ContactRepository {
	private JdbcTemplate template;

	@Autowired
	public JdbcContactRepository(DataSource dataSource) {
		template = new JdbcTemplate(dataSource);
	}

	public List<Contact> getContacts() {
		String sqlTxt = "select * from contacts ORDER BY name";
		return template.query(sqlTxt, new ContactMapper());
	}
	
	public List<Contact> getContacts(Order order) {
		String sqlTxt = "select * from contacts ORDER BY name " + order + ";";
		return template.query(sqlTxt, new ContactMapper());
	}

	public Contact getContact(Long id) {
		String sqlTxt = "select * from contacts where id=?";
		return template.queryForObject(sqlTxt, new ContactMapper(), id);
	}
	
	public Contact getLastContact() {
		String sqlTxt = "SELECT * FROM contacts ORDER BY ID DESC LIMIT 1";
		return template.queryForObject(sqlTxt, new ContactMapper());
	}

	public void createContact() {
		String sqlTxt = "INSERT INTO contacts (name, address, email, telephone, orientation, membership) VALUES ('Novi član','','','','','')";
		template.update(sqlTxt);
	}

	public void deleteContact(Long id) {
		String sqlTxt = "delete from contacts where id = ?";
		template.update(sqlTxt, id);
	}

	public void updateContact(Contact c) {
		String sqlTxt = "UPDATE contacts SET name = ?,  address = ?,  email = ?,  telephone = ?,  orientation = ?,  membership = ? WHERE id = ?";
		template.update(sqlTxt, c.getName(), c.getAddress(), c.getEmail(), c.getTelephone(), c.getOrientation(),
				c.getMembership(), c.getId());
	}

	private class ContactMapper implements RowMapper<Contact> {

		public Contact mapRow(ResultSet rs, int rowNum) throws SQLException {
			Contact contact = new Person();
			contact.setId(rs.getLong("id"));
			contact.setName(rs.getString("name"));
			contact.setAddress(rs.getString("address"));
			contact.setEmail(rs.getString("email"));
			contact.setTelephone(rs.getString("telephone"));
			contact.setOrientation(rs.getString("orientation"));
			contact.setMembership(rs.getString("membership"));
			return contact;
		}
	}
}

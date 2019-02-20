package com.nikola.taus.view;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;

import com.nikola.taus.entities.Contact;
import com.nikola.taus.repository.ContactRepository;
import com.nikola.taus.repository.ContactRepository.Order;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 15L;

	@Autowired
	private ContactRepository repository;

	private JTextField nameTextField, addressTextField, emailTextField, telephoneTextField, orientationTextField,
			membershipTextField;

	private DefaultListModel<Contact> contactsListModel;
	private JList<Contact> contactsList;

	private Action refreshAction;
	private Action newAction;
	private Action saveAction;
	private Action deleteAction;
	private Action printAction;
	private Action sortAZ;
	private Action sortZA;

	private boolean sort = false;
	private Order order;

	public void setSort(boolean sort) {
		this.sort = sort;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	private Contact selected;

	public MainFrame() {
		initActions();
		initComponents();
	}

	KeyListener enter = new KeyAdapter() {
		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				save();
			}
		}
	};

	private JToolBar createToolBar() {
		final JToolBar toolBar = new JToolBar();
		toolBar.add(refreshAction).setToolTipText("Osveži");
		toolBar.add(sortAZ).setToolTipText("A - Z");
		toolBar.add(sortZA).setToolTipText("Z - A");
		toolBar.addSeparator();
		toolBar.add(newAction).setToolTipText("Kreiraj novi kontakt");
		toolBar.add(saveAction).setToolTipText("Dopuni kontakt");
		toolBar.addSeparator();
		toolBar.addSeparator();
		toolBar.add(printAction).setToolTipText("Napravi Excell fajl");
		toolBar.addSeparator();
		toolBar.addSeparator();
		toolBar.add(deleteAction).setToolTipText("Obriši kontakt");
		return toolBar;
	}

	private ImageIcon load(final String name) {
		return new ImageIcon(getClass().getResource("/icons/" + name + ".png"));
	}

	/* ---- REFRESH CONTACT LIST ---- */

	private void refreshData() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		contactsListModel.removeAllElements();
		final List<Contact> contacts;
		if (sort == false) {
			contacts = repository.getContacts();
		} else {
			contacts = repository.getContacts(order);
		}
		final SwingWorker<Void, Contact> worker = new SwingWorker<Void, Contact>() {

			@Override
			protected Void doInBackground() throws Exception {
				for (Contact contact : contacts) {
					publish(contact);
				}
				return null;
			}

			@Override
			protected void process(final List<Contact> chunks) {
				for (final Contact contact : chunks) {
					contactsListModel.addElement(contact);
				}
			}

			@Override
			protected void done() {
				if (selected != null) {
					contactsList.setSelectedValue(contactsListModel.get(contactsListModel.indexOf(selected)), true);
				} else {
					contactsList.setSelectedValue(contactsListModel.firstElement(), true);
					setSelectedContact(contactsListModel.firstElement());
				}
			}
		};

		worker.execute();

		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/* ---- UPDATE EXISTING CONTACT ---- */

	private void save() {
		if (selected != null) {
			selected.setName(nameTextField.getText());
			selected.setAddress(addressTextField.getText());
			selected.setEmail(emailTextField.getText());
			selected.setTelephone(telephoneTextField.getText());
			selected.setOrientation(orientationTextField.getText());
			selected.setMembership(membershipTextField.getText());
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			repository.updateContact(selected);
			refreshData();
			this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	/* ---- CREATE NEW CONTACT ---- */

	private void createNew() {
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		repository.createContact();
		selected = repository.getLastContact();
		refreshData();
		nameTextField.setText("");
		addressTextField.setText("");
		emailTextField.setText("");
		telephoneTextField.setText("");
		orientationTextField.setText("");
		membershipTextField.setText("");
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//		}

	}

	/* ---- DELETE SELECTED CONTACT ---- */

	private void delete() {
		if (selected != null) {
			if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this,
					"Sigurno želite da obrisete kontakt " + selected.getName() + "?", "Brisanje",
					JOptionPane.YES_NO_OPTION)) {
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				repository.deleteContact(selected.getId());
				setSelectedContact(null);
				refreshData();
				this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}

	/* ---- CREATE EXCEL FILE OF EXISTING CONTACTS ---- */

	private void printExcel() {
		String[] columns = { "Ime i prezime", "Adresa", "Email", "Telefon", "Usmerenje", "Članarina" };

		if (!contactsListModel.isEmpty()) {
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			Workbook workbook = new XSSFWorkbook();

			Sheet sheet = workbook.createSheet("Članovi");

			Font headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 14);
			headerFont.setColor(IndexedColors.BLUE.getIndex());

			CellStyle headerCellStyle = workbook.createCellStyle();
			headerCellStyle.setFont(headerFont);

			Row headerRow = sheet.createRow(0);
			for (int i = 0; i < columns.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(columns[i]);
				cell.setCellStyle(headerCellStyle);
			}

			int rowNum = 1;
			for (int i = 0; i < contactsListModel.size(); i++) {
				Contact contact = contactsListModel.elementAt(i);
				Row row = sheet.createRow(rowNum++);
				row.createCell(0).setCellValue(contact.getName());
				row.createCell(1).setCellValue(contact.getAddress());
				row.createCell(2).setCellValue(contact.getEmail());
				row.createCell(3).setCellValue(contact.getTelephone());
				row.createCell(4).setCellValue(contact.getOrientation());
				row.createCell(5).setCellValue(contact.getMembership());
			}

			for (int i = 0; i < columns.length; i++) {
				sheet.autoSizeColumn(i);
			}
			try {
				FileOutputStream fileOut = new FileOutputStream("Članovi.xlsx");
				workbook.write(fileOut);
				fileOut.close();

				workbook.close();
				this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			} catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(this,
						"Štampanje nije uspelo. Proverite da li je excel fajl zatvoren, pa probajte ponovo.", "Štampa",
						JOptionPane.WARNING_MESSAGE);
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "Štampanje nije uspelo.", "Štampa", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	private void initActions() {
		refreshAction = new AbstractAction("Refresh", load("Refresh")) {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				refreshData();
			}
		};

		sortAZ = new AbstractAction("Order A - Z", load("OrderAZ")) {
			private static final long serialVersionUID = 5L;

			@Override
			public void actionPerformed(ActionEvent e) {
				setSort(true);
				setOrder(Order.ASC);
				refreshData();
			}

		};

		sortZA = new AbstractAction("Order Z - A", load("OrderZA")) {
			private static final long serialVersionUID = 6L;

			@Override
			public void actionPerformed(ActionEvent e) {
				setSort(true);
				setOrder(Order.DESC);
				refreshData();

			}
		};

		newAction = new AbstractAction("New", load("New")) {
			private static final long serialVersionUID = 2L;

			public void actionPerformed(ActionEvent e) {

				createNew();
			}

		};

		saveAction = new AbstractAction("Save", load("Save")) {
			private static final long serialVersionUID = 3L;

			public void actionPerformed(ActionEvent e) {
				save();
			}

		};

		printAction = new AbstractAction("Print", load("Excel")) {
			private static final long serialVersionUID = 31L;

			public void actionPerformed(ActionEvent e) {
				printExcel();
			}
		};

		deleteAction = new AbstractAction("Delete", load("Delete")) {
			private static final long serialVersionUID = 4L;

			public void actionPerformed(ActionEvent e) {
				delete();
			}
		};
	}

	private void initComponents() {
		add(createListPane(), BorderLayout.WEST);
		add(createEditor(), BorderLayout.CENTER);
		add(createToolBar(), BorderLayout.PAGE_END);
	}

	private JComponent createListPane() {
		contactsListModel = new DefaultListModel();
		contactsList = new JList(contactsListModel);
		contactsList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					selected = contactsList.getSelectedValue();
					setSelectedContact(selected);
				}
			}
		});

		contactsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		contactsList.setFixedCellWidth(250);
		return new JScrollPane(contactsList);
	}

	private void setSelectedContact(Contact contact) {
		selected = contact;
		if (contact != null) {
			nameTextField.setText(contact.getName());
			addressTextField.setText(contact.getAddress());
			emailTextField.setText(contact.getEmail());
			telephoneTextField.setText(contact.getTelephone());
			orientationTextField.setText(contact.getOrientation());
			membershipTextField.setText(contact.getMembership());
		}
	}

	public JComponent createEditor() {
		final JPanel panel = new JPanel(new GridBagLayout());

		// Name
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(2, 2, 2, 2);
		panel.add(new JLabel("Ime i Prezime"), constraints);

		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.weightx = 1;
		constraints.weighty = 0.2;
		constraints.insets = new Insets(2, 2, 2, 2);
		constraints.fill = GridBagConstraints.BOTH;
		nameTextField = new JTextField();
		nameTextField.addKeyListener(enter);
		panel.add(nameTextField, constraints);

		// Address
		constraints = new GridBagConstraints();
		constraints.gridy = 2;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(2, 2, 2, 2);
		panel.add(new JLabel("Adresa"), constraints);

		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 2;
		constraints.weightx = 1;
		constraints.weighty = 0.2;
		constraints.insets = new Insets(2, 2, 2, 2);
		constraints.fill = GridBagConstraints.BOTH;
		addressTextField = new JTextField();
		addressTextField.addKeyListener(enter);
		panel.add(addressTextField, constraints);

		// Email
		constraints = new GridBagConstraints();
		constraints.gridy = 3;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(2, 2, 2, 2);
		panel.add(new JLabel("Email"), constraints);

		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 3;
		constraints.weightx = 1;
		constraints.weighty = 0.2;
		constraints.insets = new Insets(2, 2, 2, 2);
		constraints.fill = GridBagConstraints.BOTH;
		emailTextField = new JTextField();
		emailTextField.addKeyListener(enter);
		panel.add(emailTextField, constraints);

		// Telephone
		constraints = new GridBagConstraints();
		constraints.gridy = 4;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(2, 2, 2, 2);
		panel.add(new JLabel("Telefon"), constraints);

		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 4;
		constraints.weightx = 1;
		constraints.weighty = 0.2;
		constraints.insets = new Insets(2, 2, 2, 2);
		constraints.fill = GridBagConstraints.BOTH;
		telephoneTextField = new JTextField();
		telephoneTextField.addKeyListener(enter);
		panel.add(telephoneTextField, constraints);

		// Orientation
		constraints = new GridBagConstraints();
		constraints.gridy = 5;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(2, 2, 2, 2);
		panel.add(new JLabel("Usmerenje"), constraints);

		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 5;
		constraints.weightx = 1;
		constraints.weighty = 0.2;
		constraints.insets = new Insets(2, 2, 2, 2);
		constraints.fill = GridBagConstraints.BOTH;
		orientationTextField = new JTextField();
		orientationTextField.addKeyListener(enter);
		panel.add(orientationTextField, constraints);

		// Membership
		constraints = new GridBagConstraints();
		constraints.gridy = 6;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(2, 2, 2, 2);
		panel.add(new JLabel("Članarina"), constraints);

		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 6;
		constraints.weightx = 1;
		constraints.weighty = 0.2;
		constraints.insets = new Insets(2, 2, 2, 2);
		constraints.fill = GridBagConstraints.BOTH;
		membershipTextField = new JTextField();
		membershipTextField.addKeyListener(enter);
		panel.add(membershipTextField, constraints);

		return panel;
	}

	public void init() {
		setTitle("TAUS evidencija");
		setSize(800, 600);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(MainFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
}

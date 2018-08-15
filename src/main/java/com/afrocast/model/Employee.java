package com.afrocast.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Employee {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid2")
	private String id;
	private String empno;
	private String name;
	private String surname;
	private String cellno;
	private String empEmail;
    private String pin;
    private String managerCell;
    private String managerEmail;
    private String language;
    
    public Employee() {}

    public Employee(String empno, String name, String surname, String cellno, String empEmail, String pin, String managerCell, String managerEmail, String language) {
        this.empno = empno;
        this.name = name;
        this.surname = surname;
        this.cellno = cellno;
        this.empEmail = empEmail;
        this.pin = pin;
        this.managerCell = managerCell;
        this.managerEmail = managerEmail;
        this.language = language;
    }



    public String getEmpEmail() {
        return empEmail;
    }

    public void setEmpEmail(String empEmail) {
        this.empEmail = empEmail;
    }

    public String getManagerEmail() {
        return managerEmail;
    }

    public void setManagerEmail(String managerEmail) {
        this.managerEmail = managerEmail;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getEmpno() {
        return empno;
    }

    public void setEmpno(String empno) {
        this.empno = empno;
    }

    public String getManagerCell() {
		return managerCell;
	}

	public void setManagerCell(String managerCell) {
		this.managerCell = managerCell;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getCellno() {
		return cellno;
	}

	public void setCellno(String cellno) {
		this.cellno = cellno;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}
    
    
	

}

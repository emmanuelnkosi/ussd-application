package com.afrocast.dataload;

import com.afrocast.model.Employee;
import com.afrocast.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoad implements CommandLineRunner {

    private EmployeeRepository employeeRepository;

    @Autowired
    public void setProductRepository(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public void run(String... strings) throws Exception {

        Employee employee = new Employee();
        employee.setPin("1234");
        employee.setCellno("27659751627");
        employee.setEmpno("2334");
        employee.setLanguage("English");
        employee.setManagerCell("27659751627");
        employee.setEmpEmail("test@gmail.com");
        employee.setManagerEmail("nkosi.e92@gmail.com");
        employee.setName("test");
        employee.setSurname("test");

        employeeRepository.save(employee);

    }
}

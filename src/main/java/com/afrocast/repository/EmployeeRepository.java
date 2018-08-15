package com.afrocast.repository;

import com.afrocast.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface EmployeeRepository extends JpaRepository<Employee,String> {
      Employee findBycellno(String cell);
      Employee findByempno(String empno);
      Employee findBypin(String pin);
}

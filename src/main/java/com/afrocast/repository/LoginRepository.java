package com.afrocast.repository;

import com.afrocast.model.LoginForm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginRepository extends JpaRepository<LoginForm,String> {
}

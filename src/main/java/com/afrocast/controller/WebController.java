package com.afrocast.controller;

import com.afrocast.model.Employee;
import com.afrocast.model.LoginForm;
import com.afrocast.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.validation.Valid;

@Controller
public class WebController extends WebMvcConfigurerAdapter {

    private EmployeeRepository employeeRepository;


    @Autowired
    public void setProductRepository(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/employees").setViewName("employees");
    }

    @GetMapping("/login")
    public String showForm(LoginForm loginForm) {
        return "login";
    }

    @PostMapping("/login")
    public String validateLoginInfo(Model model, @Valid LoginForm loginForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "login";
        }
        model.addAttribute("user", loginForm.getuserName());
        return "employees";
    }

//    @RequestMapping(path = "/")
//    public String index() {
//        return "index";
//    }


    @RequestMapping(path = "/employees/add", method = RequestMethod.GET)
    public String createEmployee(Model model) {
        model.addAttribute("employee", new Employee());
        return "edit";
    }

    @RequestMapping(path = "employees", method = RequestMethod.POST)
    public String saveProduct(Employee employee) {
        employeeRepository.save(employee);
        return "redirect:/employees";
    }

    @RequestMapping(path = "/employees", method = RequestMethod.GET)
    public String getAllEmployees(Model model) {
        model.addAttribute("employees", employeeRepository.findAll());
        return "employees";
    }

    @RequestMapping(path = "/employees/edit/{id}", method = RequestMethod.GET)
    public String editProduct(Model model, @PathVariable(value = "id") String id) {
        model.addAttribute("employee", employeeRepository.findOne(id));
        return "edit";
    }

    @RequestMapping(path = "/employees/delete/{id}", method = RequestMethod.GET)
    public String deleteProduct(@PathVariable(name = "id") String id) {
       employeeRepository.delete(id);
        return "redirect:/employees";
    }


}

package com.afrocast.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.afrocast.endrequest.EndRequest;
import com.afrocast.login.ApiLogin;
import com.afrocast.model.Employee;
import com.afrocast.repository.EmployeeRepository;
import com.afrocast.util.EmailService;
import com.afrocast.util.Mail;
import infobip.api.client.SendSingleTextualSms;
import infobip.api.config.BasicAuthConfiguration;
import infobip.api.model.sms.mt.send.SMSResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.afrocast.endresponse.EndResponse;
import com.afrocast.generatepin.Pin;
import com.afrocast.propertyreader.PropertyReader;
import com.afrocast.request.Request;
import com.afrocast.response.Response;

import infobip.api.model.sms.mt.send.textual.SMSTextualRequest;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.javamail.MimeMessageHelper;


@RestController
@RequestMapping("ussd")
public class UssdController {
	
	private Map<String,Response> responseMap = new HashMap<>();

    @Autowired
    private EmailService emailService;

	@Autowired
     private EmployeeRepository employeeRepository;
	
	 PropertyReader propertyReader = new PropertyReader("messages.properties");

    Pin pin = new Pin();

	
	@GetMapping("/hello")
	public String hello() {
		return "hello";
	}
	
	@PostMapping("/session/{sessionId}/start")
	public Response start(@RequestBody Request request) {
	   String msisdn =  request.getMsisdn();
        Employee bycellno = employeeRepository.findBycellno(msisdn);
        if(bycellno.getLanguage().equalsIgnoreCase("English")){
            Response response = new Response(Boolean.FALSE,propertyReader.readProperty("welcome.page"),200,"");
            responseMap.put(request.getMsisdn(), response);
            return response;
        }else if(bycellno.getLanguage().equalsIgnoreCase("Sotho")){
            Response response = new Response(Boolean.FALSE,propertyReader.readProperty("welcome1.page"),200,"");
            responseMap.put(request.getMsisdn(), response);
            return response;

        }else{

            Response response = new Response(Boolean.FALSE,propertyReader.readProperty("welcome2.page"),200,"");
            responseMap.put(request.getMsisdn(), response);
            return response;

        }
	}
	
	@PutMapping("/session/{sessionId}/response")
	public Response response(@RequestBody Request request)throws Exception {
		return processResponse(request);
	}
	
	@PutMapping("/session/{sessionId}/end")
	public EndResponse exit(@RequestBody EndRequest request) {
         if (request.getExitCode().equals("200")){
             EndResponse endResponse = new EndResponse("","Session ended normally");
             return  endResponse;

         }else if(request.getExitCode().equals("500")){
             EndResponse endResponse = new EndResponse("","Session aborted by network");
             return  endResponse;

         }else if(request.getExitCode().equals("510")){
             EndResponse endResponse = new EndResponse("","Session aborted by TPA");
             return  endResponse;

         }else if(request.getExitCode().equals("520")){
             EndResponse endResponse = new EndResponse("","Session aborted by user");
             return  endResponse;

         }else if(request.getExitCode().equals("600")){
          EndResponse endResponse = new EndResponse("","Session timeout");
          return  endResponse;
         }

	  return  null;

	}
	
	private Response processResponse(Request request) throws MessagingException {
		Response response =	responseMap.get(request.getMsisdn());

       String userCell =  request.getMsisdn();
        Employee employeByCell = employeeRepository.findBycellno(userCell);
        System.out.println(employeByCell.getLanguage());

         if (employeByCell.getLanguage().equalsIgnoreCase("English")){

             if(response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("welcome.page"))) {

                 if(request.getText().equals("1")) {
                     Response about = new Response(Boolean.FALSE,propertyReader.readProperty("about.page"),200,"");
                     responseMap.replace(request.getMsisdn(), about);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("2")) {
                     Response register = new Response(Boolean.FALSE,propertyReader.readProperty("register.page"),200,"");
                     responseMap.replace(request.getMsisdn(), register);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("3")) {
                     Response login = new Response(Boolean.FALSE,propertyReader.readProperty("login.page"),200,"");
                     responseMap.replace(request.getMsisdn(), login);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("4")) {
                     Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit.page"),200,"");
                     responseMap.replace(request.getMsisdn(), exit);
                     response = responseMap.get(request.getMsisdn());
                     return response;


                 }

             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("register.page")))) {
                 System.out.println(request.getText());

                 if(request.getText()!=null){

                     Employee employee = employeeRepository.findByempno(request.getText());

                     employee.getEmpno();
                     System.out.println();

                     if(employee!=null){
                         ApiLogin apiLogin = new ApiLogin();
                         String userPin = pin.generatePIN();
                         employee.setPin(userPin);
                         employeeRepository.save(employee);
                         SendSingleTextualSms client = new SendSingleTextualSms(new BasicAuthConfiguration(apiLogin.getUsername(), apiLogin.getPassword()));
                         SMSTextualRequest requestBody = new SMSTextualRequest();
                         requestBody.setFrom(apiLogin.getFrom());
                         requestBody.setTo(Arrays.asList(employee.getCellno()));
                         requestBody.setText(userPin);
                         SMSResponse smsResponse = client.execute(requestBody);

                         if(smsResponse!=null){
                             Response login = new Response(Boolean.FALSE,propertyReader.readProperty("login.page"),200,"");
                             responseMap.replace(request.getMsisdn(), login);
                             response = responseMap.get(request.getMsisdn());
                             return response;
                         }else{

                             Response invalidpin = new Response(Boolean.FALSE,propertyReader.readProperty("invalidempno.page"),200,"");
                             responseMap.replace(request.getMsisdn(), invalidpin);
                             response = responseMap.get(request.getMsisdn());
                             return response;

                         }

                     }
                 }

             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("login.page")))) {

                 if(request.getText()!=null){
                     Employee bypin = employeeRepository.findBypin(request.getText());
                     if(bypin!=null){
                         Response mainmenu = new Response(Boolean.FALSE,propertyReader.readProperty("mainmenu.page"),200,"");
                         responseMap.replace(request.getMsisdn(), mainmenu);
                         response = responseMap.get(request.getMsisdn());
                         return response;

                     }else{
                         Response invalidpin = new Response(Boolean.FALSE,propertyReader.readProperty("invalidpin.page"),200,"");
                         responseMap.replace(request.getMsisdn(), invalidpin);
                         response = responseMap.get(request.getMsisdn());
                         return response;

                     }

                 }

             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("mainmenu.page")))) {

                 if(request.getText().equals("1")) {
                     Response message = new Response(Boolean.FALSE,propertyReader.readProperty("messsage.page"),200,"");
                     responseMap.replace(request.getMsisdn(), message);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("2")) {
                     Response notification = new Response(Boolean.FALSE,propertyReader.readProperty("notification.page"),200,"");
                     responseMap.replace(request.getMsisdn(), notification);
                     response = responseMap.get(request.getMsisdn());
                     return response;

                 }else if(request.getText().equals("3")) {
                     Response language = new Response(Boolean.FALSE,propertyReader.readProperty("language.page"),200,"");
                     responseMap.replace(request.getMsisdn(), language);
                     response = responseMap.get(request.getMsisdn());
                     return response;


                 }else if(request.getText().equals("4")) {
                     Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit.page"),200,"");
                     responseMap.replace(request.getMsisdn(), exit);
                     response = responseMap.get(request.getMsisdn());
                     return response;


                 }
             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("invalidempno.page")))) {

                 if(request.getText().equals("1")) {
                     Response register = new Response(Boolean.FALSE,propertyReader.readProperty("register.page"),200,"");
                     responseMap.replace(request.getMsisdn(), register);
                     response = responseMap.get(request.getMsisdn());
                     return response;


                 }else if(request.getText().equals("2")) {
                     Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit.page"),200,"");
                     responseMap.replace(request.getMsisdn(), exit);
                     response = responseMap.get(request.getMsisdn());
                     return response;

                 }

             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("invalidpin.page")))) {

                 if(request.getText().equals("1")) {
                     Response register = new Response(Boolean.FALSE,propertyReader.readProperty("login.page"),200,"");
                     responseMap.replace(request.getMsisdn(), register);
                     response = responseMap.get(request.getMsisdn());
                     return response;

                 }else if(request.getText().equals("2")) {
                     Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit.page"),200,"");
                     responseMap.replace(request.getMsisdn(), exit);
                     response = responseMap.get(request.getMsisdn());
                     return response;

                 }
             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("messsage.page")))) {
                 if(request.getText()!=null){
                     Employee bycellno = employeeRepository.findBycellno(request.getMsisdn());
                     Mail mail = new Mail();
                     mail.setFrom(bycellno.getEmpEmail());
                     mail.setTo(bycellno.getManagerEmail());
                     mail.setSubject("Afrocast USSD App");
                     mail.setContent(request.getText());
                     emailService.sendSimpleMessage(mail);
                     System.out.println(bycellno.getEmpno());
                     ApiLogin apiLogin = new ApiLogin();
                     SendSingleTextualSms client = new SendSingleTextualSms(new BasicAuthConfiguration(apiLogin.getUsername(), apiLogin.getPassword()));
                     SMSTextualRequest requestBody = new SMSTextualRequest();
                     requestBody.setFrom(apiLogin.getFrom());
                     requestBody.setTo(Arrays.asList(bycellno.getManagerCell()));
                     requestBody.setText("Afrocast USSD App" +request.getText());
                     SMSResponse smsResponse = client.execute(requestBody);

                     if(smsResponse!=null){
                         Response smsent = new Response(Boolean.FALSE,propertyReader.readProperty("smssent.page"),200,"");
                         responseMap.replace(request.getMsisdn(), smsent);
                         response = responseMap.get(request.getMsisdn());
                         return response;
                     }


                 }
             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("smssent.page")))) {

                 if(request.getText().equals("1")) {
                     Response mainmenu = new Response(Boolean.FALSE,propertyReader.readProperty("mainmenu.page"),200,"");
                     responseMap.replace(request.getMsisdn(), mainmenu);
                     response = responseMap.get(request.getMsisdn());
                     return response;

                 }else if(request.getText().equals("2")) {
                     Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit.page"),200,"");
                     responseMap.replace(request.getMsisdn(), exit);
                     response = responseMap.get(request.getMsisdn());
                     return response;

                 }

             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("notification.page")))) {

                 if(request.getText().equals("1")) {
                     Response leave = new Response(Boolean.FALSE,propertyReader.readProperty("leave.page"),200,"");
                     responseMap.replace(request.getMsisdn(), leave);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("2")) {
                     Response holidays = new Response(Boolean.FALSE,propertyReader.readProperty("holidays.page"),200,"");
                     responseMap.replace(request.getMsisdn(), holidays);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("3")) {
                     Response overtime = new Response(Boolean.FALSE,propertyReader.readProperty("overtime.page"),200,"");
                     responseMap.replace(request.getMsisdn(), overtime);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("4")) {
                     Response hrcontacts = new Response(Boolean.FALSE, propertyReader.readProperty("hrcontacts.page"), 200, "");
                     responseMap.replace(request.getMsisdn(), hrcontacts);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("")){
                     Response training = new Response(Boolean.FALSE,propertyReader.readProperty("training.page"),200,"");
                     responseMap.replace(request.getMsisdn(), training);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("5")){
                     Response notification = new Response(Boolean.FALSE,propertyReader.readProperty("notification.page"),200,"");
                     responseMap.replace(request.getMsisdn(), notification);
                     response = responseMap.get(request.getMsisdn());
                     return response;

                 }else if(request.getText().equals("6")){
                     Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit.page"),200,"");
                     responseMap.replace(request.getMsisdn(), exit);
                     response = responseMap.get(request.getMsisdn());
                     return response;

                 }

             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("leave.page")))) {

                 if(request.getText().equals("1")){
                     Response notification = new Response(Boolean.FALSE,propertyReader.readProperty("notification.page"),200,"");
                     responseMap.replace(request.getMsisdn(), notification);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("2")){
                     Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit.page"),200,"");
                     responseMap.replace(request.getMsisdn(), exit);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }

             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("holidays.page")))) {

                 if(request.getText().equals("1")){
                     Response notification = new Response(Boolean.FALSE,propertyReader.readProperty("notification.page"),200,"");
                     responseMap.replace(request.getMsisdn(), notification);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("2")){
                     Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit.page"),200,"");
                     responseMap.replace(request.getMsisdn(), exit);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }

             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("overtime.page")))) {

                 if(request.getText().equals("1")){
                     Response notification = new Response(Boolean.FALSE,propertyReader.readProperty("notification.page"),200,"");
                     responseMap.replace(request.getMsisdn(), notification);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("2")){
                     Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit.page"),200,"");
                     responseMap.replace(request.getMsisdn(), exit);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }

             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("training.page")))) {

                 if(request.getText().equals("1")){
                     Response notification = new Response(Boolean.FALSE,propertyReader.readProperty("notification.page"),200,"");
                     responseMap.replace(request.getMsisdn(), notification);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("2")){
                     Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit.page"),200,"");
                     responseMap.replace(request.getMsisdn(), exit);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }

             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("hrcontacts.page")))) {

                 if(request.getText().equals("1")){
                     Response notification = new Response(Boolean.FALSE,propertyReader.readProperty("notification.page"),200,"");
                     responseMap.replace(request.getMsisdn(), notification);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("2")){
                     Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit.page"),200,"");
                     responseMap.replace(request.getMsisdn(), exit);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }

             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("language.page")))) {

                 if(request.getText().equals("1")){
                     String currentUser =  request.getMsisdn();
                     Employee bycellno = employeeRepository.findBycellno(currentUser);
                     if(bycellno!=null){
                         bycellno.setLanguage("English");
                         employeeRepository.save(bycellno);
                         Response mainmenu = new Response(Boolean.FALSE,propertyReader.readProperty("mainmenu.page"),200,"");
                         responseMap.replace(request.getMsisdn(), mainmenu);
                         response = responseMap.get(request.getMsisdn());
                         return response;
                     }
                 }else if(request.getText().equals("2")){
                     String currentUser =  request.getMsisdn();
                     Employee bycellno = employeeRepository.findBycellno(currentUser);
                     if(bycellno!=null){
                         bycellno.setLanguage("Sotho");
                         employeeRepository.save(bycellno);
                         Response mainmenu = new Response(Boolean.FALSE,propertyReader.readProperty("mainmenu1.page"),200,"");
                         responseMap.replace(request.getMsisdn(), mainmenu);
                         response = responseMap.get(request.getMsisdn());
                         return response;
                     }

                 }

             }

         }else if (employeByCell.getLanguage().equalsIgnoreCase("Sotho")){

            if(response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("welcome1.page"))) {

                if(request.getText().equals("1")) {
                    Response about = new Response(Boolean.FALSE,propertyReader.readProperty("about1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), about);
                    response = responseMap.get(request.getMsisdn());
                    return response;
                }else if(request.getText().equals("2")) {
                    Response register = new Response(Boolean.FALSE,propertyReader.readProperty("register1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), register);
                    response = responseMap.get(request.getMsisdn());
                    return response;
                }else if(request.getText().equals("3")) {
                    Response login = new Response(Boolean.FALSE,propertyReader.readProperty("login1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), login);
                    response = responseMap.get(request.getMsisdn());
                    return response;
                }else if(request.getText().equals("4")) {
                    Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), exit);
                    response = responseMap.get(request.getMsisdn());
                    return response;


                }

            }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("register1.page")))) {
                System.out.println(request.getText());

                if(request.getText()!=null){

                    Employee employee = employeeRepository.findByempno(request.getText());

                    employee.getEmpno();
                    System.out.println();

                    if(employee!=null){
                        ApiLogin apiLogin = new ApiLogin();
                        String userPin = pin.generatePIN();
                        employee.setPin(userPin);
                        employeeRepository.save(employee);
                        SendSingleTextualSms client = new SendSingleTextualSms(new BasicAuthConfiguration(apiLogin.getUsername(), apiLogin.getPassword()));
                        SMSTextualRequest requestBody = new SMSTextualRequest();
                        requestBody.setFrom(apiLogin.getFrom());
                        requestBody.setTo(Arrays.asList(employee.getCellno()));
                        requestBody.setText(userPin);
                        SMSResponse smsResponse = client.execute(requestBody);

                        if(smsResponse!=null){
                            Response login = new Response(Boolean.FALSE,propertyReader.readProperty("login.page"),200,"");
                            responseMap.replace(request.getMsisdn(), login);
                            response = responseMap.get(request.getMsisdn());
                            return response;
                        }else{

                            Response invalidpin = new Response(Boolean.FALSE,propertyReader.readProperty("invalidempno1.page"),200,"");
                            responseMap.replace(request.getMsisdn(), invalidpin);
                            response = responseMap.get(request.getMsisdn());
                            return response;

                        }

                    }
                }

            }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("login1.page")))) {

                if(request.getText()!=null){
                    Employee bypin = employeeRepository.findBypin(request.getText());
                    if(bypin!=null){
                        Response mainmenu = new Response(Boolean.FALSE,propertyReader.readProperty("mainmenu1.page"),200,"");
                        responseMap.replace(request.getMsisdn(), mainmenu);
                        response = responseMap.get(request.getMsisdn());
                        return response;

                    }else{
                        Response invalidpin = new Response(Boolean.FALSE,propertyReader.readProperty("invalidpin1.page"),200,"");
                        responseMap.replace(request.getMsisdn(), invalidpin);
                        response = responseMap.get(request.getMsisdn());
                        return response;

                    }

                }

            }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("mainmenu1.page")))) {

                if(request.getText().equals("1")) {
                    Response message = new Response(Boolean.FALSE,propertyReader.readProperty("messsage1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), message);
                    response = responseMap.get(request.getMsisdn());
                    return response;
                }else if(request.getText().equals("2")) {
                    Response notification = new Response(Boolean.FALSE,propertyReader.readProperty("notification1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), notification);
                    response = responseMap.get(request.getMsisdn());
                    return response;

                }else if(request.getText().equals("3")) {
                    Response language = new Response(Boolean.FALSE,propertyReader.readProperty("language1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), language);
                    response = responseMap.get(request.getMsisdn());
                    return response;


                }else if(request.getText().equals("4")) {
                    Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit.page"),200,"");
                    responseMap.replace(request.getMsisdn(), exit);
                    response = responseMap.get(request.getMsisdn());
                    return response;


                }
            }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("invalidempno1.page")))) {

                if(request.getText().equals("1")) {
                    Response register = new Response(Boolean.FALSE,propertyReader.readProperty("register1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), register);
                    response = responseMap.get(request.getMsisdn());
                    return response;


                }else if(request.getText().equals("2")) {
                    Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), exit);
                    response = responseMap.get(request.getMsisdn());
                    return response;

                }

            }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("invalidpin1.page")))) {

                if(request.getText().equals("1")) {
                    Response register = new Response(Boolean.FALSE,propertyReader.readProperty("login1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), register);
                    response = responseMap.get(request.getMsisdn());
                    return response;

                }else if(request.getText().equals("2")) {
                    Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), exit);
                    response = responseMap.get(request.getMsisdn());
                    return response;

                }
            }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("messsage1.page")))) {
                if(request.getText()!=null){
                    Employee bycellno = employeeRepository.findBycellno(request.getMsisdn());
                    Mail mail = new Mail();
                    mail.setFrom(bycellno.getEmpEmail());
                    mail.setTo(bycellno.getManagerEmail());
                    mail.setSubject("Afrocast USSD App");
                    mail.setContent(request.getText());
                    emailService.sendSimpleMessage(mail);
                    System.out.println(bycellno.getEmpno());
                    ApiLogin apiLogin = new ApiLogin();
                    SendSingleTextualSms client = new SendSingleTextualSms(new BasicAuthConfiguration(apiLogin.getUsername(), apiLogin.getPassword()));
                    SMSTextualRequest requestBody = new SMSTextualRequest();
                    requestBody.setFrom(apiLogin.getFrom());
                    requestBody.setTo(Arrays.asList(bycellno.getManagerCell()));
                    requestBody.setText("Afrocast USDD App " +request.getText());
                    SMSResponse smsResponse = client.execute(requestBody);

                    if(smsResponse!=null){
                        Response smsent = new Response(Boolean.FALSE,propertyReader.readProperty("smssent1.page"),200,"");
                        responseMap.replace(request.getMsisdn(), smsent);
                        response = responseMap.get(request.getMsisdn());
                        return response;
                    }

                }
            }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("smssent1.page")))) {

                if(request.getText().equals("1")) {
                    Response mainmenu = new Response(Boolean.FALSE,propertyReader.readProperty("mainmenu1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), mainmenu);
                    response = responseMap.get(request.getMsisdn());
                    return response;

                }else if(request.getText().equals("2")) {
                    Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), exit);
                    response = responseMap.get(request.getMsisdn());
                    return response;

                }

            }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("notification1.page")))) {

                if(request.getText().equals("1")) {
                    Response leave = new Response(Boolean.FALSE,propertyReader.readProperty("leave1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), leave);
                    response = responseMap.get(request.getMsisdn());
                    return response;
                }else if(request.getText().equals("2")) {
                    Response holidays = new Response(Boolean.FALSE,propertyReader.readProperty("holidays1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), holidays);
                    response = responseMap.get(request.getMsisdn());
                    return response;
                }else if(request.getText().equals("3")) {
                    Response overtime = new Response(Boolean.FALSE,propertyReader.readProperty("overtime1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), overtime);
                    response = responseMap.get(request.getMsisdn());
                    return response;
                }else if(request.getText().equals("4")) {
                    Response hrcontacts = new Response(Boolean.FALSE, propertyReader.readProperty("hrcontacts1.page"), 200, "");
                    responseMap.replace(request.getMsisdn(), hrcontacts);
                    response = responseMap.get(request.getMsisdn());
                    return response;
                }else if(request.getText().equals("")){
                    Response training = new Response(Boolean.FALSE,propertyReader.readProperty("training.page"),200,"");
                    responseMap.replace(request.getMsisdn(), training);
                    response = responseMap.get(request.getMsisdn());
                    return response;
                }else if(request.getText().equals("5")){
                    Response notification = new Response(Boolean.FALSE,propertyReader.readProperty("notification1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), notification);
                    response = responseMap.get(request.getMsisdn());
                    return response;

                }else if(request.getText().equals("6")){
                    Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), exit);
                    response = responseMap.get(request.getMsisdn());
                    return response;

                }

            }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("leave.page")))) {

                if(request.getText().equals("1")){
                    Response notification = new Response(Boolean.FALSE,propertyReader.readProperty("notification1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), notification);
                    response = responseMap.get(request.getMsisdn());
                    return response;
                }else if(request.getText().equals("2")){
                    Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), exit);
                    response = responseMap.get(request.getMsisdn());
                    return response;
                }

            }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("holidays1.page")))) {

                if(request.getText().equals("1")){
                    Response notification = new Response(Boolean.FALSE,propertyReader.readProperty("notification1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), notification);
                    response = responseMap.get(request.getMsisdn());
                    return response;
                }else if(request.getText().equals("2")){
                    Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), exit);
                    response = responseMap.get(request.getMsisdn());
                    return response;
                }

            }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("overtime1.page")))) {

                if(request.getText().equals("1")){
                    Response notification = new Response(Boolean.FALSE,propertyReader.readProperty("notification1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), notification);
                    response = responseMap.get(request.getMsisdn());
                    return response;
                }else if(request.getText().equals("2")){
                    Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), exit);
                    response = responseMap.get(request.getMsisdn());
                    return response;
                }

            }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("training1.page")))) {

                if(request.getText().equals("1")){
                    Response notification = new Response(Boolean.FALSE,propertyReader.readProperty("notification1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), notification);
                    response = responseMap.get(request.getMsisdn());
                    return response;
                }else if(request.getText().equals("2")){
                    Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), exit);
                    response = responseMap.get(request.getMsisdn());
                    return response;
                }

            }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("hrcontacts1.page")))) {

                if(request.getText().equals("1")){
                    Response notification = new Response(Boolean.FALSE,propertyReader.readProperty("notification1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), notification);
                    response = responseMap.get(request.getMsisdn());
                    return response;
                }else if(request.getText().equals("2")){
                    Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit1.page"),200,"");
                    responseMap.replace(request.getMsisdn(), exit);
                    response = responseMap.get(request.getMsisdn());
                    return response;
                }

            }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("language1.page")))) {

                if(request.getText().equals("1")){
                    String currentUser =  request.getMsisdn();
                    Employee bycellno = employeeRepository.findBycellno(currentUser);
                    if(bycellno!=null){
                        bycellno.setLanguage("English");
                        employeeRepository.save(bycellno);
                        Response mainmenu = new Response(Boolean.FALSE,propertyReader.readProperty("mainmenu.page"),200,"");
                        responseMap.replace(request.getMsisdn(), mainmenu);
                        response = responseMap.get(request.getMsisdn());
                        return response;
                    }
                }else if(request.getText().equals("2")){
                     String currentUser =  request.getMsisdn();
                    Employee bycellno = employeeRepository.findBycellno(currentUser);
                      if(bycellno!=null){
                          bycellno.setLanguage("Sotho");
                          employeeRepository.save(bycellno);
                          Response mainmenu = new Response(Boolean.FALSE,propertyReader.readProperty("mainmenu1.page"),200,"");
                          responseMap.replace(request.getMsisdn(), mainmenu);
                          response = responseMap.get(request.getMsisdn());
                           return response;
                      }

                }

            }



        }else if (employeByCell.getLanguage().equalsIgnoreCase("Zulu")){

             if(response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("welcome2.page"))) {

                 if(request.getText().equals("1")) {
                     Response about = new Response(Boolean.FALSE,propertyReader.readProperty("about2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), about);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("2")) {
                     Response register = new Response(Boolean.FALSE,propertyReader.readProperty("register2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), register);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("3")) {
                     Response login = new Response(Boolean.FALSE,propertyReader.readProperty("login2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), login);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("4")) {
                     Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), exit);
                     response = responseMap.get(request.getMsisdn());
                     return response;


                 }

             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("register2.page")))) {
                 System.out.println(request.getText());

                 if(request.getText()!=null){

                     Employee employee = employeeRepository.findByempno(request.getText());

                     employee.getEmpno();
                     System.out.println();

                     if(employee!=null){
                         ApiLogin apiLogin = new ApiLogin();
                         String userPin = pin.generatePIN();
                         employee.setPin(userPin);
                         employeeRepository.save(employee);
                         SendSingleTextualSms client = new SendSingleTextualSms(new BasicAuthConfiguration(apiLogin.getUsername(), apiLogin.getPassword()));
                         SMSTextualRequest requestBody = new SMSTextualRequest();
                         requestBody.setFrom(apiLogin.getFrom());
                         requestBody.setTo(Arrays.asList(employee.getCellno()));
                         requestBody.setText(userPin);
                         SMSResponse smsResponse = client.execute(requestBody);

                         if(smsResponse!=null){
                             Response login = new Response(Boolean.FALSE,propertyReader.readProperty("login2.page"),200,"");
                             responseMap.replace(request.getMsisdn(), login);
                             response = responseMap.get(request.getMsisdn());
                             return response;
                         }else{

                             Response invalidpin = new Response(Boolean.FALSE,propertyReader.readProperty("invalidempno2.page"),200,"");
                             responseMap.replace(request.getMsisdn(), invalidpin);
                             response = responseMap.get(request.getMsisdn());
                             return response;

                         }

                     }
                 }

             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("login2.page")))) {

                 if(request.getText()!=null){
                     Employee bypin = employeeRepository.findBypin(request.getText());
                     if(bypin!=null){
                         Response mainmenu = new Response(Boolean.FALSE,propertyReader.readProperty("mainmenu2.page"),200,"");
                         responseMap.replace(request.getMsisdn(), mainmenu);
                         response = responseMap.get(request.getMsisdn());
                         return response;

                     }else{
                         Response invalidpin = new Response(Boolean.FALSE,propertyReader.readProperty("invalidpin2.page"),200,"");
                         responseMap.replace(request.getMsisdn(), invalidpin);
                         response = responseMap.get(request.getMsisdn());
                         return response;

                     }

                 }

             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("mainmenu2.page")))) {

                 if(request.getText().equals("1")) {
                     Response message = new Response(Boolean.FALSE,propertyReader.readProperty("messsage2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), message);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("2")) {
                     Response notification = new Response(Boolean.FALSE,propertyReader.readProperty("notification2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), notification);
                     response = responseMap.get(request.getMsisdn());
                     return response;

                 }else if(request.getText().equals("3")) {
                     Response language = new Response(Boolean.FALSE,propertyReader.readProperty("language2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), language);
                     response = responseMap.get(request.getMsisdn());
                     return response;


                 }else if(request.getText().equals("4")) {
                     Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), exit);
                     response = responseMap.get(request.getMsisdn());
                     return response;


                 }
             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("invalidempno2.page")))) {

                 if(request.getText().equals("1")) {
                     Response register = new Response(Boolean.FALSE,propertyReader.readProperty("register2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), register);
                     response = responseMap.get(request.getMsisdn());
                     return response;


                 }else if(request.getText().equals("2")) {
                     Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), exit);
                     response = responseMap.get(request.getMsisdn());
                     return response;

                 }

             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("invalidpin2.page")))) {

                 if(request.getText().equals("1")) {
                     Response register = new Response(Boolean.FALSE,propertyReader.readProperty("login2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), register);
                     response = responseMap.get(request.getMsisdn());
                     return response;

                 }else if(request.getText().equals("2")) {
                     Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), exit);
                     response = responseMap.get(request.getMsisdn());
                     return response;

                 }
             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("messsage2.page")))) {
                 if(request.getText()!=null){
                     Employee bycellno = employeeRepository.findBycellno(request.getMsisdn());

                     Mail mail = new Mail();
                     mail.setFrom(bycellno.getEmpEmail());
                     mail.setTo(bycellno.getManagerEmail());
                     mail.setSubject("Afrocast USSD App");
                     mail.setContent(request.getText());
                     emailService.sendSimpleMessage(mail);
                     System.out.println(bycellno.getEmpno());
                     ApiLogin apiLogin = new ApiLogin();
                     SendSingleTextualSms client = new SendSingleTextualSms(new BasicAuthConfiguration(apiLogin.getUsername(), apiLogin.getPassword()));
                     SMSTextualRequest requestBody = new SMSTextualRequest();
                     requestBody.setFrom(apiLogin.getFrom());
                     requestBody.setTo(Arrays.asList(bycellno.getManagerCell()));
                     requestBody.setText("Afrocast USSD message "+request.getText());
                     SMSResponse smsResponse = client.execute(requestBody);

                     if(smsResponse!=null){
                         Response smsent = new Response(Boolean.FALSE,propertyReader.readProperty("smssent2.page"),200,"");
                         responseMap.replace(request.getMsisdn(), smsent);
                         response = responseMap.get(request.getMsisdn());
                         return response;
                     }

                 }
             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("smssent2.page")))) {

                 if(request.getText().equals("1")) {
                     Response mainmenu = new Response(Boolean.FALSE,propertyReader.readProperty("mainmenu2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), mainmenu);
                     response = responseMap.get(request.getMsisdn());
                     return response;

                 }else if(request.getText().equals("2")) {
                     Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), exit);
                     response = responseMap.get(request.getMsisdn());
                     return response;

                 }

             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("notification2.page")))) {

                 if(request.getText().equals("1")) {
                     Response leave = new Response(Boolean.FALSE,propertyReader.readProperty("leave2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), leave);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("2")) {
                     Response holidays = new Response(Boolean.FALSE,propertyReader.readProperty("holidays2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), holidays);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("3")) {
                     Response overtime = new Response(Boolean.FALSE,propertyReader.readProperty("overtime2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), overtime);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("4")) {
                     Response hrcontacts = new Response(Boolean.FALSE, propertyReader.readProperty("hrcontacts2.page"), 200, "");
                     responseMap.replace(request.getMsisdn(), hrcontacts);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("")){
                     Response training = new Response(Boolean.FALSE,propertyReader.readProperty("training2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), training);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("5")){
                     Response notification = new Response(Boolean.FALSE,propertyReader.readProperty("notification2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), notification);
                     response = responseMap.get(request.getMsisdn());
                     return response;

                 }else if(request.getText().equals("6")){
                     Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), exit);
                     response = responseMap.get(request.getMsisdn());
                     return response;

                 }

             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("leave2.page")))) {

                 if(request.getText().equals("1")){
                     Response notification = new Response(Boolean.FALSE,propertyReader.readProperty("notification2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), notification);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("2")){
                     Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), exit);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }

             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("holidays2.page")))) {

                 if(request.getText().equals("1")){
                     Response notification = new Response(Boolean.FALSE,propertyReader.readProperty("notification2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), notification);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("2")){
                     Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), exit);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }

             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("overtime2.page")))) {

                 if(request.getText().equals("1")){
                     Response notification = new Response(Boolean.FALSE,propertyReader.readProperty("notification2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), notification);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("2")){
                     Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), exit);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }

             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("training2.page")))) {

                 if(request.getText().equals("1")){
                     Response notification = new Response(Boolean.FALSE,propertyReader.readProperty("notification2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), notification);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("2")){
                     Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), exit);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }

             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("hrcontacts2.page")))) {

                 if(request.getText().equals("1")){
                     Response notification = new Response(Boolean.FALSE,propertyReader.readProperty("notification2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), notification);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }else if(request.getText().equals("2")){
                     Response exit = new Response(Boolean.FALSE,propertyReader.readProperty("exit2.page"),200,"");
                     responseMap.replace(request.getMsisdn(), exit);
                     response = responseMap.get(request.getMsisdn());
                     return response;
                 }

             }else if((response.getUssdMenu().equalsIgnoreCase(propertyReader.readProperty("language2.page")))) {

                 if(request.getText().equals("1")){
                     String currentUser =  request.getMsisdn();
                     Employee bycellno = employeeRepository.findBycellno(currentUser);
                     if(bycellno!=null){
                         bycellno.setLanguage("English");
                         employeeRepository.save(bycellno);
                         Response mainmenu = new Response(Boolean.FALSE,propertyReader.readProperty("mainmenu2.page"),200,"");
                         responseMap.replace(request.getMsisdn(), mainmenu);
                         response = responseMap.get(request.getMsisdn());
                         return response;
                     }
                 }else if(request.getText().equals("2")){
                     String currentUser =  request.getMsisdn();
                     Employee bycellno = employeeRepository.findBycellno(currentUser);
                     if(bycellno!=null){
                         bycellno.setLanguage("Sotho");
                         employeeRepository.save(bycellno);
                         Response mainmenu = new Response(Boolean.FALSE,propertyReader.readProperty("mainmenu2.page"),200,"");
                         responseMap.replace(request.getMsisdn(), mainmenu);
                         response = responseMap.get(request.getMsisdn());
                         return response;
                     }

                 }

             }

         }


		
		
		return null;
	}

}

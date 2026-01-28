package com.vbs.demo.controller;

import com.vbs.demo.dto.DisplayDto;
import com.vbs.demo.dto.LoginDto;
import com.vbs.demo.dto.RaiseQueryDto;
import com.vbs.demo.dto.VerifyDto;
import com.vbs.demo.models.History;
import com.vbs.demo.models.Notification;
import com.vbs.demo.models.RaiseQuery;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.HistoryRepo;
import com.vbs.demo.repositories.NotificationRepo;
import com.vbs.demo.repositories.RaiseQueryRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    UserRepo userRepo;
    @Autowired
    HistoryRepo historyRepo;
    @Autowired
    RaiseQueryRepo raiseQueryRepo;
    @Autowired
    NotificationRepo notificationRepo;

    @PostMapping("/auth/signup")
    public String register(@RequestBody User user){

        if(userRepo.findByUsername(user.getUsername()) != null)
            return "Username Already Exists";

        if(userRepo.findByEmail(user.getEmail()) != null)
            return "Email Already Exists";

        userRepo.save(user);
        user.setAccountNumber("NB"+(100000 + user.getId()));
        userRepo.save(user);
        History h1 = new History();
        h1.setTargetId(user.getAccountNumber());
        h1.setDescription("User Account Self Created");
        historyRepo.save(h1);

        Notification n = new Notification();
        n.setAccountNumber(user.getAccountNumber());
        n.setMessage(" “Welcome to Nova Bank 🎉 Your account has been successfully created.” ");
//        n.setAdminId(adminId);
        n.setReadStatus(false);
//         n.setCreatedAt(LocalDateTime.now());
        notificationRepo.save(n);

        return "Signup Successful";
    }

    @PostMapping("/auth/user/login")
    public String login(@RequestBody LoginDto u)
    {
        User user = userRepo.findByUsername(u.getUsername());
        if(user == null)
        {
            return "User Not Found";
        }
        if(!u.getPassword().equals(user.getPassword()))
        {
            return "Password Incorrect";
        }
        return String.valueOf(user.getAccountNumber());
    }

    @GetMapping("/user/dashboard/{accountNumber}")

    public DisplayDto display(@PathVariable String accountNumber)
    {
        User user = userRepo.findByAccountNumber(accountNumber).orElseThrow(()-> new RuntimeException("Invalid account number"));
        DisplayDto displayDto = new DisplayDto();
        displayDto.setName(user.getName());
        displayDto.setBalance(user.getBalance());
        displayDto.setAccountNumber(user.getAccountNumber());
        return displayDto;
    }

    @PostMapping("/support/verify")
    public String verfiy(@RequestBody VerifyDto obj)
    {
        User user = userRepo.findByAccountNumber(obj.getAccountNumber())
                .orElse(null);
        if(user == null) return "Invalid Account Number";  //this will be redundant as <optional> in repo already handles this
//        but using this show the proper toast message that why passed null
        if(!user.getUsername().equalsIgnoreCase(obj.getUsername())) return "Incorrect Username";
        return "Verified Successfully";
    }

    @PostMapping("/support/raise")
    public String raised(@RequestBody RaiseQueryDto obj)
    {
        User user = userRepo.findByAccountNumber(obj.getAccountNumber())
                .orElse(null);
        if(user == null) return "Invalid Account Number";
        if(obj.getIssueType().isEmpty()) return "Issue Type Cannot Be Empty";
        if(obj.getSubject().isEmpty()) return "Subject Cannot Be Empty";
        if(obj.getDescription().isEmpty()) return "Description Cannot Be Empty";

        RaiseQuery r1 = new RaiseQuery();
        r1.setAccountNumber(obj.getAccountNumber());
        r1.setIssueType(obj.getIssueType());
        r1.setSubject(obj.getSubject());
        r1.setDescription(obj.getDescription());
        r1.setStatus("OPEN");

        raiseQueryRepo.save(r1);

        History h1 = new History();
        h1.setTargetId("TICKET#"+r1.getId());
        h1.setDescription("Support ticket raised by "+r1.getAccountNumber());
        historyRepo.save(h1);

        return "Ticket Raised Successfully";
    }

    @GetMapping("/support/list/{accountNumber}")
    public List<RaiseQuery> getquery(@PathVariable String accountNumber)
    {
        return raiseQueryRepo.findAllByAccountNumber(accountNumber);
    }

    @GetMapping("/notifications/{accountNumber}")
    public List<Notification> getuserNoti(
            @PathVariable String accountNumber)
    {
        return notificationRepo.findByAccountNumberOrderByCreatedAtDesc(accountNumber);
    }

    @PostMapping("/notifications/read/{id}")
    public String markAsRead(@PathVariable int id)
    {
        Notification n = notificationRepo.findById(id)
                .orElseThrow(()->new RuntimeException("Not Found"));
        n.setReadStatus(true);
        notificationRepo.save(n);
        return "Marked as read";
    }

    @PostMapping("/notifications/read-all/{accountNumber}")
    public String markAllRead(@PathVariable String accountNumber)
    {
        List<Notification> list = notificationRepo
                .findByAccountNumberAndReadStatus(accountNumber,false);
        for(Notification n : list)
        {
            n.setReadStatus(true);
        }
        notificationRepo.saveAll(list);
        return "All marked as read";
    }

    @GetMapping("/notifications/unread-count/{accountNumber}")
    public long unreadcount(@PathVariable String accountNumber)
    {
        return notificationRepo.countByAccountNumberAndReadStatus(accountNumber,false);
    }

}

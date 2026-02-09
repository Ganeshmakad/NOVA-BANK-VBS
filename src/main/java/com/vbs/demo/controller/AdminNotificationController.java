package com.vbs.demo.controller;

import com.vbs.demo.dto.NotificationDto;
import com.vbs.demo.models.History;
import com.vbs.demo.models.Notification;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.HistoryRepo;
import com.vbs.demo.repositories.NotificationRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class AdminNotificationController {
    @Autowired
    NotificationRepo notificationRepo;
    @Autowired
    UserRepo userRepo;
    @Autowired
    HistoryRepo historyRepo;

    @PostMapping("/admin/notify")
    public String sendNotification(@RequestBody NotificationDto obj)
    {
       if(obj.getMessage() == null || obj.getMessage().isEmpty())
           return "Message cannot be empty";

       if("ALL".equals(obj.getTarget()))
       {
           List<User> users = userRepo.findAll();
           for (User u : users)
           {
               saveNoti(u.getAccountNumber(), obj.getMessage(), obj.getAdminId());
           }
           History h = new History();
           h.setTargetId("NOTIFICATION");
           h.setDescription("Notification sent by Admin ADM"+obj.getAdminId()+" to All Customers");
           historyRepo.save(h);
           return "Notification sent to all users";
       }

       if("SELECTED".equals(obj.getTarget()))
       {
           if(obj.getAccounts() == null || obj.getAccounts().isEmpty())
               return "No users selected";

           for (String acc : obj.getAccounts())
           {
               saveNoti(acc,obj.getMessage(), obj.getAdminId());
           }
           History h = new History();
           h.setTargetId("NOTIFICATION");
           h.setDescription("Notification sent by Admin ADM"+obj.getAdminId()+" to "+obj.getAccounts().size()+" Customers");
           historyRepo.save(h);
           return "Notification sent to selected users";
       }

       return "Invalid Target";
    }

     void saveNoti(String acc,String msg,int adminId) {
         Notification n = new Notification();
         n.setAccountNumber(acc);
         n.setMessage(msg);
         n.setAdminId(adminId);
         n.setReadStatus(false);
         notificationRepo.save(n);
    }

}

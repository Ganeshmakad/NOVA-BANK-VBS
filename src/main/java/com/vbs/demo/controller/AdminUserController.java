package com.vbs.demo.controller;

import com.vbs.demo.dto.AdminReplyDto;
import com.vbs.demo.dto.RaiseQueryDto;
import com.vbs.demo.dto.UpdateDto;
import com.vbs.demo.models.*;
import com.vbs.demo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class AdminUserController {
    @Autowired
    AdminRepo adminRepo;
    @Autowired
    UserRepo userRepo;
    @Autowired
    TransactionRepo transactionRepo;
    @Autowired
    HistoryRepo historyRepo;
    @Autowired
    RaiseQueryRepo raiseQueryRepo;
    @Autowired
    NotificationRepo notificationRepo;

    @GetMapping("/users")
    public List<User> getAll(@RequestParam String sortBy , @RequestParam String order)
    {
        Sort sort;
        if(order.equalsIgnoreCase("desc"))
        {
            sort = Sort.by(sortBy).descending();
        }
        else
        {
            sort = Sort.by(sortBy).ascending();
        }

        return userRepo.findAll(sort);
    }

    @GetMapping("/users/{keyword}")
    public List<User> getUsers(@PathVariable String keyword)
    {
        return userRepo.findByUsernameContainingIgnoreCase(keyword);
    }

    @PostMapping("/user/update")
    public String update(@RequestBody UpdateDto obj)
    {
        User user = userRepo.findByAccountNumber(obj.getAccountNumber())
                .orElseThrow(()-> new RuntimeException("Invalid Account Number"));
        History h1 = new History();
        if(obj.getField().equalsIgnoreCase("name"))
        {
            if(user.getName().equalsIgnoreCase(obj.getValue())) return "Cannot Be Same";
            h1.setTargetId(user.getAccountNumber());
            h1.setDescription("Changed Name From : "+user.getName()+" to "+obj.getValue());
            user.setName(obj.getValue());
        } else if (obj.getField().equalsIgnoreCase("password")) {
            if(user.getPassword().equals(obj.getValue())) return "Cannot Be Same";
            h1.setTargetId(user.getAccountNumber());
            h1.setDescription("Changed Password");
            user.setPassword(obj.getValue());
        } else if (obj.getField().equalsIgnoreCase("email")) {
            if(user.getEmail().equalsIgnoreCase(obj.getValue())) return "Cannot Be Same";
            User user1 = userRepo.findByEmail(obj.getValue());
            if(user1 != null) return "Email Already Exists";
            h1.setTargetId(user.getAccountNumber());
            h1.setDescription("Changed Email From : "+user.getEmail()+" to "+obj.getValue());
            user.setEmail(obj.getValue());
        }
        else return "Invalid Field";
        userRepo.save(user);
        historyRepo.save(h1);
        return "Updated Successfully";
    }

    @PostMapping("/add/user") //for better security we pass admin id only in header and not in url
    //instead wil use request header to store the admin id for future history feature
    public String addUser(@RequestBody User user ,
                      @RequestHeader("X-ADMIN-ACC") int adminId)
    {
        if(user.getBalance()<0) return "Initial Balance Cannot Be Negative";
        userRepo.save(user);
        user.setAccountNumber("NB"+(100000 + user.getId()));
        userRepo.save(user);
        History h1 = new History();
        h1.setTargetId(user.getAccountNumber());
        h1.setDescription("User Account Created By Admin : ADM"+adminId);
        historyRepo.save(h1);
        if(user.getBalance()>0)
        {
            Transaction t = new Transaction();
            t.setAccountNumber(user.getAccountNumber());
            t.setAmount(user.getBalance());
            t.setCurrBalance(user.getBalance());
            t.setDescription("Rs "+user.getBalance()+" Initial Deposit By Admin");
            transactionRepo.save(t);
        }

        Notification n = new Notification();
        n.setAccountNumber(user.getAccountNumber());
        n.setMessage(" “Welcome to Nova Bank 🎉 Your account has been successfully created.” ");
        n.setAdminId(adminId);
        n.setReadStatus(false);
        notificationRepo.save(n);
        return "New Costumer Successfully Added";
    }

    @PostMapping("/add/admin")
    public String addAdmin(@RequestBody Admin admin,
                           @RequestHeader("X-ADMIN-ACC") int adminId)
    {
        adminRepo.save(admin);
        History h1 = new History();
        h1.setTargetId("ADM"+(admin.getId()));
        h1.setDescription("Admin Account Created By Admin : "+adminId);
        historyRepo.save(h1);
        return "Admin Added Successfully";
    }

    @DeleteMapping("/delete-user/{deleteAccNo}")
    public String delete(
            @PathVariable String deleteAccNo ,
            @RequestHeader ("X-ADMIN-ACC") int adminId)
    {

        // ✅ VERIFY ADMIN
        Admin admin = adminRepo.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Unauthorized Admin"));

        User user = userRepo.findByAccountNumber(deleteAccNo).
                orElseThrow(()->new RuntimeException("Invalid Account Number"));
        if(user.getBalance()>0)
        {
            Notification n = new Notification();
            n.setAccountNumber(user.getAccountNumber());
            n.setMessage(" Your account cannot be deleted because it has an active balance.\n Please clear the balance first.");
            n.setAdminId(adminId);
            n.setReadStatus(false);
            notificationRepo.save(n);

            History h = new History();
            h.setTargetId(user.getAccountNumber());
            h.setDescription("Account deletion blocked: user has non-zero balance (Admin ADM" + adminId + ")");
            historyRepo.save(h);

            return "Cannot Delete Balance Not Zero";
        }

        History h1 = new History();
        h1.setTargetId(user.getAccountNumber());
        h1.setDescription("User Account Deleted By Admin ADM: "+adminId);
        historyRepo.save(h1);
        userRepo.delete(user);
        return "User Deleted Successfully";
    }

    @GetMapping("/support/all")
    public List<RaiseQuery> getAll()
    {
        return raiseQueryRepo.findAll();
    }

    @GetMapping("/support/{ticketId}")
    public RaiseQuery getOne(@PathVariable int ticketId)
    {
        return raiseQueryRepo.findById(ticketId)
                .orElseThrow(()-> new RuntimeException("Ticket Not Found"));
    }

    @PostMapping("/support/reply/{ticketId}")
    public String reply(@PathVariable int ticketId , @RequestBody AdminReplyDto rep,
                        @RequestHeader ("X-ADMIN-ACC") int adminId)
    {
        RaiseQuery t1 = raiseQueryRepo.findById(ticketId).orElse(null);
        if(t1==null) return "Invalid Ticket";
        if(!t1.getStatus().equalsIgnoreCase("open")) return "Ticket Closed Can't Reply";

        if(rep.getReply().equals("")) return "Reply Can't Be Empty";
        t1.setStatus("CLOSED");
        t1.setAdminReply(rep.getReply());
        t1.setReplyDate(LocalDateTime.now());

        raiseQueryRepo.save(t1);

        History h1 = new History();
        h1.setTargetId("TICKET#"+ticketId);
        h1.setDescription("Ticket closed by Admin "+adminId);
        historyRepo.save(h1);

        RaiseQuery r1 = raiseQueryRepo.findById(ticketId)
                .orElseThrow(()->new RuntimeException("Invalid Ticket Id"));

        Notification n = new Notification();
        n.setAccountNumber(r1.getAccountNumber());
        n.setMessage(" Your support request regarding " + t1.getSubject() + " has been resolved.\n Please check the reply for more details.");
        n.setAdminId(adminId);
        n.setReadStatus(false);
        notificationRepo.save(n);

        return "Ticket Closed Successfully";
    }

    @GetMapping("/all-users")
    public List<User> getAllUsers(){
        return userRepo.findAll();
    }
}


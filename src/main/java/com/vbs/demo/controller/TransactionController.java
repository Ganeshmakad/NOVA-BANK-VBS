package com.vbs.demo.controller;


import com.vbs.demo.dto.TransactionDto;
import com.vbs.demo.dto.TransferDto;
import com.vbs.demo.models.Transaction;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.TransactionRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class TransactionController {
    @Autowired
    UserRepo userRepo;
    @Autowired
    TransactionRepo transactionRepo;

    @PostMapping("/user/deposit")
    public String deposit(@RequestBody TransactionDto obj)
    {
        User user = userRepo.findByAccountNumber(obj.getAccountNumber())
                .orElseThrow(()->new RuntimeException("Invalid Account number"));
        if(obj.getAmount()<=0) return "Invalid Amount";
        double newBalance = user.getBalance() + obj.getAmount();
        user.setBalance(newBalance);
        userRepo.save(user);

        Transaction t = new Transaction();
        t.setAmount(obj.getAmount());
        t.setCurrBalance(newBalance);
        t.setDescription("Rs "+ obj.getAmount()+" Deposit Successful");
        t.setAccountNumber(obj.getAccountNumber());
        transactionRepo.save(t);
        return "Deposit Successful";
    }

    @PostMapping("/user/withdraw")
    public String withdraw(@RequestBody TransactionDto obj)
    {
        User user = userRepo.findByAccountNumber(obj.getAccountNumber())
                .orElseThrow(()-> new RuntimeException("Invalid Account Number"));
        if(obj.getAmount()<=0) return "Invalid Amount";
        double newBalance = user.getBalance() - obj.getAmount();
        if(newBalance<0) return "Insufficient Balance";
        user.setBalance(newBalance);
        userRepo.save(user);

        Transaction t = new Transaction();
        t.setAmount(obj.getAmount());
        t.setCurrBalance(newBalance);
        t.setDescription("Rs "+obj.getAmount()+" Withdrawal Successful");
        t.setAccountNumber(obj.getAccountNumber());
        transactionRepo.save(t);
        return "Withdrawal Successful";
    }

    @PostMapping("/user/transfer")
    public String transfer(@RequestBody TransferDto obj)
    {
        User sender = userRepo.findByAccountNumber(obj.getAccountNumber())
                .orElseThrow(()-> new RuntimeException("Invalid Account Number"));
        User rec = userRepo.findByUsername(obj.getUsername());
        if(rec==null) return "Receiver Not Found";
        if(sender.getId() == rec.getId()) return "Self Transfer Not Allowed";
        if(obj.getAmount()<=0) return "Invalid Amount";

        double sbalance = sender.getBalance() - obj.getAmount();
        if(sbalance<0) return "Insufficient Balance";
        double rbalance = rec.getBalance() + obj.getAmount();

        sender.setBalance(sbalance);
        rec.setBalance(rbalance);
        userRepo.save(sender);
        userRepo.save(rec);

        Transaction  t1  = new Transaction();
        Transaction  t2  = new Transaction();

        t1.setAmount(obj.getAmount());
        t1.setCurrBalance(sbalance);
        t1.setDescription("Rs "+obj.getAmount()+" Sent to user "+ rec.getUsername());
        t1.setAccountNumber(sender.getAccountNumber());
        transactionRepo.save(t1);

        t2.setAmount(obj.getAmount());
        t2.setCurrBalance(rbalance);
        t2.setDescription("Rs "+obj.getAmount()+" Received from user "+sender.getUsername());
        t2.setAccountNumber(rec.getAccountNumber());
        transactionRepo.save(t2);
        return "Transfer Successful";
    }

    @GetMapping("/user/passbook/{accountNumber}")
    public List<Transaction> getpassbook(@PathVariable String accountNumber)
    {
        return transactionRepo.findAllByAccountNumber(accountNumber);
    }

}

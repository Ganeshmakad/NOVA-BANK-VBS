package com.vbs.demo.controller;


import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.vbs.demo.dto.TransactionDto;
import com.vbs.demo.dto.TransferDto;
import com.vbs.demo.models.Transaction;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.TransactionRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
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
        if(user.isBlocked())
            return "Account is blocked. Deposit not allowed, Contact Admin";
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
        if(user.isBlocked())
            return "Account is blocked. Withdrawal not allowed, Contact Admin";
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
        if(sender.isBlocked())
            return "Account is blocked. Transfer not allowed, Contact Admin";
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

    @GetMapping("/user/passbook/pdf/{accountNumber}")
    public ResponseEntity<byte[]> downloadPassbook(@PathVariable String accountNumber) throws Exception {

        List<Transaction> list = transactionRepo.findByAccountNumberOrderByDateDesc(accountNumber);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        doc.add(new Paragraph("NOVA BANK")
                .setBold()
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph("Account Statement")
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph("\nAccount Number: " + accountNumber));
        doc.add(new Paragraph("Generated On: " + LocalDate.now()));
        doc.add(new Paragraph("\n"));

        Table table = new Table(4).useAllAvailableWidth();

        table.addHeaderCell("Date");
        table.addHeaderCell("Description");
        table.addHeaderCell("Amount");
        table.addHeaderCell("Balance");

        for(Transaction t : list) {
            table.addCell(t.getDate().toString());
            table.addCell(t.getDescription());
            table.addCell(String.valueOf(t.getAmount()));
            table.addCell(String.valueOf(t.getCurrBalance()));
        }

        doc.add(table);
        doc.close();

        byte[] pdfBytes = out.toByteArray();

        return ResponseEntity.ok()
                .header("Content-Disposition","attachment; filename=passbook.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

}

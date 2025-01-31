package org.szewczyk.pwr.pzwmanager.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.szewczyk.pwr.pzwmanager.model.Order;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class MailService {
    private final JavaMailSender javaMailSender;

    @Resource
    PDFService pdfService;

    @Autowired
    public MailService(JavaMailSender javaMailSender){
        this.javaMailSender = javaMailSender;
    }

//    @SendMail
    public void sendMail(String to, String subject, String text, boolean isHTMLObject, Order orderDetails) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom("e-Zezwolenia <pzwkudowa@wp.pl>");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, isHTMLObject);

        String filePath = new File("").getAbsolutePath();
//        System.out.println("path: " + filePath);
        File dir = new File(filePath + File.separator + "invoices");
        dir.mkdir();

        File pdfInvoice = new File(filePath + File.separator + "invoices" + File.separator + "inv" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssAAA")) + ".pdf");
        try {
            PDDocument invoice = pdfService.createOrderConfirmation(orderDetails);
            invoice.save(pdfInvoice);
            invoice.close();
//            System.out.println("PDF saved");
        } catch (IOException e) {
            System.out.println("Error saving PDF");
            e.printStackTrace();
        }


//        FileSystemResource file = new FileSystemResource(new File("C:" + File.separator + "testy" + File.separator + "test.csv"));
        helper.addAttachment("Potwierdzenie " + LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".pdf", pdfInvoice);

        javaMailSender.send(mimeMessage);
        pdfInvoice.delete();
    }
}

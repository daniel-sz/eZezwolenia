package org.szewczyk.pwr.pzwmanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class MailService {
    private final JavaMailSender javaMailSender;

    @Autowired
    public MailService(JavaMailSender javaMailSender){
        this.javaMailSender = javaMailSender;
    }

//    @SendMail
    public void sendMail(String to, String subject, String text, boolean isHTMLObject) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom("pzwkudowa@wp.pl");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, isHTMLObject);

//        FileSystemResource file = new FileSystemResource(new File("C:" + File.separator + "testy" + File.separator + "test.csv"));
//        helper.addAttachment("Potwierdzenie " + LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE), file);
        javaMailSender.send(mimeMessage);
    }
}

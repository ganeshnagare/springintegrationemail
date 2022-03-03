package com.firm.demo;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.mail.MailReceiver;
import org.springframework.integration.mail.dsl.Mail;
import org.springframework.integration.mail.support.DefaultMailHeaderMapper;
import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;

import javax.mail.internet.MimeMessage;

@Log4j2
@Configuration
@EnableIntegration
public class EmailReceiver {

    @Autowired
    private PollableChannel pop3Channel;

    private MailReceiver receiver;


    @Bean
    public PollableChannel receivedChannel() {
        return new QueueChannel();
    }



    @Bean
    public IntegrationFlow pop3MailFlow() {
        return IntegrationFlows
                .from(Mail.pop3InboundAdapter("pop.gmail.com", 995, "userName", "password")
                                .javaMailProperties(p -> {
                                    p.put("mail.debug", "true");
                                    p.put("mail.pop3.socketFactory.fallback", "false");
                                    p.put("mail.pop3.port", 995);
                                    p.put("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                                    p.put("mail.pop3.socketFactory.port", 995);
                                })
                                .headerMapper(mailHeaderMapper()),                       
                        e -> e.poller(Pollers.fixedRate(5000).maxMessagesPerPoll(1)))
                .handle((payload, header) -> logMail(payload))
                .get();
    }


    public Message logMail(Object payload) {
        Message message = (Message)payload;
        log.info("*******Email[TEST]********* ", payload);
        return message;
    }

    @Bean
    public HeaderMapper<MimeMessage> mailHeaderMapper() {
        return new DefaultMailHeaderMapper();
    }

   

}

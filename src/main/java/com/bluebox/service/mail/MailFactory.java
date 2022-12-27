package com.bluebox.service.mail;

import com.bluebox.AppConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

@RequiredArgsConstructor
@Service
@Scope("prototype")
public class MailFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailFactory.class);
    private String toAddress;
    private String subject;
    private MailTemplate template;
    private final SpringTemplateEngine engine;
    private final MailService service;
    private final AppConfig config;

    public MailFactory to(final String emailAddress) {
        LOGGER.debug("Set email to {}", emailAddress);
        this.toAddress = emailAddress;
        return this;
    }

    public MailFactory withSubject(final String subject) {
        LOGGER.debug("Set subject to {}", subject);
        this.subject = subject;
        return this;
    }

    public ResetPassMailTemplate resetPass() {
        LOGGER.debug("Set template to reset password");
        template = new ResetPassMailTemplate(engine, this);
        return (ResetPassMailTemplate) template;
    }

    public void send() throws MailException {
        if (template == null)
            throw new MailException("There is no template");
        service.send(toAddress, "Food Lover", subject, template.render());
    }

    @RequiredArgsConstructor
    public static class MailTemplate {
        private final Context context = new Context();
        private final SpringTemplateEngine engine;
        protected final MailFactory factory;
        private String templateName;

        protected MailTemplate withVariable(final String name, final Object value) {
            LOGGER.debug("With variable {}:{}", name, value);
            context.setVariable(name, value);
            return this;
        }

        protected MailTemplate withTemplate(final String templateName) {
            this.templateName = templateName;
            return this;
        }

        public MailFactory and() {
            return factory;
        }

        public String render() {
            return engine.process(templateName, context);
        }
    }

    public static class ResetPassMailTemplate extends MailTemplate {
        public ResetPassMailTemplate(SpringTemplateEngine engine, MailFactory factory) {
            super(engine, factory);
            withTemplate("reset-pass-email");
        }

        public ResetPassMailTemplate withToken(final String token) {
            LOGGER.debug("Set link");
            super.withVariable("link", generateLink(token));
            return this;
        }

        private String generateLink(final String token) {
            return factory.config.getResetPasswordUrl() + "?token=" + token;
        }
    }

}

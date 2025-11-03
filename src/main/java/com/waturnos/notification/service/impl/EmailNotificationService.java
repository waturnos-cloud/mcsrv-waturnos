package com.waturnos.notification.service.impl;

import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.waturnos.notification.bean.NotificationRequest;
import com.waturnos.notification.service.NotificationService;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class EmailNotificationService.
 */
@Service("emailNotificationService")

/** The Constant log. */
@Slf4j
public class EmailNotificationService implements NotificationService {
	
	/** The mail sender. */
	private final JavaMailSender mailSender;
	
	/** The template engine. */
	private final SpringTemplateEngine templateEngine; // Para procesar plantillas HTML

	/**
	 * Instantiates a new email notification service.
	 *
	 * @param mailSender the mail sender
	 * @param templateEngine the template engine
	 */
	@Autowired
	public EmailNotificationService(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
		this.mailSender = mailSender;
		this.templateEngine = templateEngine;
	}

	/**
	 * Gets the channel name.
	 *
	 * @return the channel name
	 */
	@Override
	public String getChannelName() {
		return "EMAIL";
	}

	/**
	 * Send notification.
	 *
	 * @param request the request
	 */
	@Override
	public void sendNotification(NotificationRequest request) {
		try {
			Context context = new Context(new Locale(request.getLanguage()));
			if (request.getProperties() != null) {
				for (Map.Entry<String, String> entry : request.getProperties().entrySet()) {
					context.setVariable(entry.getKey(), entry.getValue());
				}
			}
	        String languageCode = request.getLanguage().toLowerCase();
	        String templateName = languageCode + "/" + getTemplateName(request.getType().name());
			String htmlContent = templateEngine.process(templateName, context);
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom("no-reply@turnero.com");
			helper.setTo(request.getEmail());
			helper.setSubject(request.getSubject());
			helper.setText(htmlContent, true);
			mailSender.send(message);
			log.info("✅ Email de " + request.getType() + " enviado a: " + request.getEmail());

		} catch (Exception e) {
			System.err.println("❌ Error al enviar email a " + request.getEmail() + ": " + e.getMessage());
			// Manejo de errores real (lanzar excepción, logging, etc.)
		}
	}

	/**
	 * Resuelve el nombre del archivo de plantilla. Ejemplo: RESET_PASSWORD ->
	 * "reset_password"
	 *
	 * @param notificationType the notification type
	 * @return the template name
	 */
	private String getTemplateName(String notificationType) {
		return notificationType.toLowerCase();
	}
}
package com.waturnos.notification.service.impl;

import org.springframework.stereotype.Service;

import com.waturnos.notification.bean.NotificationRequest;
import com.waturnos.notification.service.NotificationService;

import lombok.extern.slf4j.Slf4j;

@Service("whatsAppNotificationService") 
@Slf4j
public class WhatsAppNotificationService implements NotificationService {

	@Override
    public String getChannelName() {
        return "WHATSAPP";
    }

    @Override
    public void sendNotification(NotificationRequest request) {
        log.info("--- 💬 Enviando por WHATSAPP ---");
        log.info("Número: " + request.getPhone());
        log.info("Idioma: " + request.getLanguage());
        // El subject puede ser nulo o ignorado. Se usa el contenido de las properties.
        String username = request.getProperties().get("USERNAME");
        String message = String.format("Hola %s, haz clic aquí para resetear tu clave: %s", 
                                       username, 
                                       request.getProperties().get("RESET_LINK"));
        log.info("Mensaje: " + message);
        
        // Lógica real de la API de WhatsApp Business.
    }
}
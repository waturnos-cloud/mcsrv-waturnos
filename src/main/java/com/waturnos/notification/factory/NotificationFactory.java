package com.waturnos.notification.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.waturnos.config.NotificationChannelConfig;
import com.waturnos.notification.bean.NotificationRequest;
import com.waturnos.notification.enums.NotificationType;
import com.waturnos.notification.service.NotificationService;

@Component
public class NotificationFactory {

	private final Map<String, NotificationService> serviceMap;
    private final Map<NotificationType, List<NotificationService>> subscriptions;

    public NotificationFactory(
        List<NotificationService> notificationServices,
        NotificationChannelConfig config) {
        
        // 1. Mapear todos los servicios por su nombre de canal (key: "EMAIL", value: EmailService)
        this.serviceMap = notificationServices.stream()
            .collect(Collectors.toMap(NotificationService::getChannelName, Function.identity()));
            
        // 2. Construir el mapa de Suscripciones (key: NotificationType, value: List<NotificationService>)
        this.subscriptions = buildSubscriptions(config.getChannels());
    }

    /**
     * Construye el mapa de suscripciones basado en la configuración externa.
     */
    private Map<NotificationType, List<NotificationService>> buildSubscriptions(Map<String, String> channelConfig) {
        Map<NotificationType, List<NotificationService>> map = new EnumMap<>(NotificationType.class);

        // Iterar sobre las entradas de la configuración (Ej: "EMAIL" -> "RESET_PASSWORD,WELCOME_USER")
        for (Map.Entry<String, String> entry : channelConfig.entrySet()) {
            String channelName = entry.getKey();
            String typesString = entry.getValue();

            NotificationService service = serviceMap.get(channelName);
            if (service == null) {
                System.err.println("Advertencia: No se encontró la implementación para el canal: " + channelName);
                continue;
            }

            // Dividir los tipos y suscribir el servicio a cada uno
            Arrays.stream(typesString.split(","))
                .map(String::trim)
                .map(typeStr -> {
                    try {
                        return NotificationType.valueOf(typeStr);
                    } catch (IllegalArgumentException e) {
                        System.err.println("Error: Tipo de notificación no válido: " + typeStr);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(type -> {
                    map.computeIfAbsent(type, k -> new ArrayList<>()).add(service);
                });
        }
        return map;
    }

    /**
     * Envía la notificación a TODOS los canales suscritos para ese tipo de notificación.
     * Este es el método que llamarás desde tu UserService.
     */
    public void send(NotificationRequest request) {
       this.sendPrivate(request);
    }
    
    /**
     * Send private.
     *
     * @param request the request
     */
    private void sendPrivate(NotificationRequest request) {
        NotificationType type = request.getType();
        
        List<NotificationService> services = subscriptions.getOrDefault(type, Collections.emptyList());

        if (services.isEmpty()) {
            System.out.println("No hay canales suscritos para el tipo de notificación: " + type);
            return;
        }

        System.out.println("Disparando notificación " + type + " a " + services.size() + " canales.");
        
        // Ejecutar el envío en cada servicio suscrito
        for (NotificationService service : services) {
            // **NOTA IMPORTANTE:** Puedes pasar el mismo 'request' a todos, 
            // y cada servicio (Email/WhatsApp) tomará solo los campos que necesita.
            service.sendNotification(request);
        }
    }
    
    
    /**
     * Envía la notificación a TODOS los canales suscritos para ese tipo de notificación.
     * Este es el método que llamarás desde tu UserService.
     */
    @Async
    public void sendAsync(NotificationRequest request) {
    	 this.sendPrivate(request);
    }
}
package com.waturnos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Habilita el procesamiento asíncrono en la aplicación.
 * Los métodos marcados con @Async se ejecutarán en un pool de hilos separado.
 */
@Configuration
@EnableAsync
public class AsyncConfiguration {
    // Aquí podrías configurar el ThreadPoolTaskExecutor (tamaño, nombre de hilos, etc.)
    // Si no se configura, Spring usa un SimpleAsyncTaskExecutor por defecto.
}

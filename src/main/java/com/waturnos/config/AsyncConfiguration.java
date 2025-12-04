package com.waturnos.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Habilita el procesamiento asíncrono en la aplicación.
 * Los métodos marcados con @Async se ejecutarán en un pool de hilos separado.
 */
@Configuration
@EnableAsync
public class AsyncConfiguration {
    
    /**
     * Configura un executor para operaciones asíncronas pesadas como generación de bookings.
     * @return executor configurado
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // Mínimo 2 hilos activos
        executor.setMaxPoolSize(4);  // Máximo 4 hilos para evitar saturar BD
        executor.setQueueCapacity(100); // Cola de espera
        executor.setThreadNamePrefix("async-booking-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}

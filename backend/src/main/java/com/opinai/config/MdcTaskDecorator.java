package com.opinai.config;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // Capturar el contexto de MDC del hilo padre
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            try {
                // Establecer el contexto en el hilo hijo
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                runnable.run();
            } finally {
                // Limpiar el contexto al finalizar el hilo hijo
                MDC.clear();
            }
        };
    }
}

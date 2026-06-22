package com.opinai.service.export;

import com.opinai.model.ExportFormat;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ExporterFactory {

    private final Map<ExportFormat, ExporterStrategy> exporters;

    public ExporterFactory(List<ExporterStrategy> exporterStrategies) {
        this.exporters = exporterStrategies.stream()
                .collect(Collectors.toMap(ExporterStrategy::getFormat, Function.identity()));
    }

    public ExporterStrategy getExporter(ExportFormat format) {
        ExporterStrategy exporter = exporters.get(format);
        if (exporter == null) {
            throw new IllegalArgumentException("Formato de exportación no soportado: " + format);
        }
        return exporter;
    }
}

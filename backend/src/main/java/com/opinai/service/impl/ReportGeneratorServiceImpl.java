package com.opinai.service.impl;

import com.opinai.model.ExportFormat;
import com.opinai.service.ReportGeneratorService;
import com.opinai.service.dto.ProjectReportPayload;
import com.opinai.service.export.ExporterFactory;
import com.opinai.service.export.ExporterStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportGeneratorServiceImpl implements ReportGeneratorService {

    private final ExporterFactory exporterFactory;

    @Override
    public byte[] generate(ProjectReportPayload payload, ExportFormat format) {
        if (payload == null) {
            throw new IllegalArgumentException("El payload del proyecto no puede ser nulo");
        }
        if (format == null) {
            throw new IllegalArgumentException("El formato de exportación no puede ser nulo");
        }
        
        ExporterStrategy exporter = exporterFactory.getExporter(format);
        return exporter.export(payload);
    }
}

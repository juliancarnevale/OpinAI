package com.opinai.service.dto;

import com.opinai.model.ExportFormat;
import lombok.Value;
import java.io.InputStream;

@Value
public class ReportFileStream {
    String fileName;
    ExportFormat format;
    InputStream inputStream;
}

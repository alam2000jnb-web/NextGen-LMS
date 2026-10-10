package com.lms.service.interfaces;

import com.lms.exception.DatabaseOperationException;
import java.util.Map;

public interface Reportable {
    Map<String, Object> generatePerformanceReport() throws DatabaseOperationException;
    Map<String, Double> getCourseCompletionRates() throws DatabaseOperationException;
}

package com.company.hr.event;

import com.company.hr.entity.Employee;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmployeeEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String EMPLOYEE_TOPIC = "employee-events";

    public void publishEmployeeCreatedEvent(Employee employee) {
        publishEvent("EMPLOYEE_CREATED", employee);
    }

    public void publishEmployeeUpdatedEvent(Employee employee) {
        publishEvent("EMPLOYEE_UPDATED", employee);
    }

    public void publishEmployeeDeletedEvent(Employee employee) {
        publishEvent("EMPLOYEE_DELETED", employee);
    }

    private void publishEvent(String eventType, Employee employee) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", eventType);
            event.put("employeeId", employee.getId());
            event.put("employeeNumber", employee.getEmployeeId());
            event.put("email", employee.getEmail());
            event.put("timestamp", LocalDateTime.now().toString());

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(EMPLOYEE_TOPIC, employee.getId().toString(), eventJson);
            
            log.info("Published {} event for employee: {}", eventType, employee.getId());
        } catch (JsonProcessingException e) {
            log.error("Error publishing event for employee: {}", employee.getId(), e);
        }
    }
}

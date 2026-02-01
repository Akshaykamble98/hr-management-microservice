package com.company.hr.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic employeeEventsTopic() {
        return TopicBuilder.name("employee-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic leaveEventsTopic() {
        return TopicBuilder.name("leave-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic attendanceEventsTopic() {
        return TopicBuilder.name("attendance-events")
                .partitions(3)
                .replicas(1)
                .build();
    }
}

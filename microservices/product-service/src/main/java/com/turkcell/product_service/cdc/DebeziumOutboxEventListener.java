package com.turkcell.product_service.cdc;

import java.time.Instant;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.turkcell.product_service.entity.OutboxEvent;
import com.turkcell.product_service.entity.OutboxStatus;
import com.turkcell.product_service.repository.OutboxRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Debezium CDC Listener - PostgreSQL WAL'den gelen Outbox table değişikliklerini dinler.
 * 
 * Debezium tarafından oluşturulan CDC events'i:
 * 1. Parse et (change event yapısını aç)
 * 2. Outbox tablosundaki değişiklikleri kontrol et
 * 3. INSERT/UPDATE events'leri ilgili Kafka topics'lerine yayın
 * 4. Status'u SENT olarak işaretle
 * 
 * Kullanım:
 *  - CDC'yi etkinleştir: app.outbox.cdc.enabled=true
 *  - Polling'i kapat: app.outbox.polling.enabled=false
 */
@Slf4j
@Component
@ConditionalOnProperty(
    name = "app.outbox.cdc.enabled", 
    havingValue = "true"
)
public class DebeziumOutboxEventListener {

    private final OutboxRepository outboxRepository;
    private final StreamBridge streamBridge;
    private final ObjectMapper objectMapper;

    public DebeziumOutboxEventListener(OutboxRepository outboxRepository, 
                                       StreamBridge streamBridge,
                                       ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.streamBridge = streamBridge;
        this.objectMapper = objectMapper;
    }

    /**
     * Debezium tarafından gönderilen Outbox table change events'ini dinle.
     * 
     * CDC event format (Debezium PostgreSQL):
     * {
     *   "before": { ... }, // Eski değer (DELETE/UPDATE için)
     *   "after": { ... },  // Yeni değer (INSERT/UPDATE için)
     *   "source": { ... }, // Kaynak bilgisi (table, lsn, vb.)
     *   "op": "c|r|u|d",   // İşlem: create, read, update, delete
     *   "ts_ms": 1234567890000 // Timestamp
     * }
     */
    @KafkaListener(
        topics = "dbz_outbox", 
        groupId = "product-service-cdc-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOutboxChangeEvent(String eventPayload) {
        try {
            log.debug("Debezium Outbox CDC event alındı: {}", eventPayload);
            
            JsonNode changeEvent = objectMapper.readTree(eventPayload);
            
            // Operation type: c=CREATE, u=UPDATE, d=DELETE, r=READ
            String operation = changeEvent.get("op").asText();
            
            // Sadece INSERT (c) ve UPDATE (u) operasyonlarını işle
            if ("c".equals(operation) || "u".equals(operation)) {
                JsonNode afterData = changeEvent.get("after");
                
                if (afterData != null && !afterData.isNull()) {
                    // CDC event'indeki verilerden OutboxEvent oluştur
                    OutboxEvent outboxEvent = parseOutboxEvent(afterData);
                    
                    // PENDING statusü varsa Kafka'ya gönder
                    if (outboxEvent.getStatus() == OutboxStatus.PENDING) {
                        publishEvent(outboxEvent);
                        
                        // Status'u SENT olarak güncelle
                        outboxEvent.setStatus(OutboxStatus.SENT);
                        outboxEvent.setProcessedAt(Instant.now());
                        outboxRepository.save(outboxEvent);
                        
                        log.info("✅ Outbox event başarıyla Kafka'ya gönderildi: {} ({})", 
                                outboxEvent.getAggregateId(), outboxEvent.getEventType());
                    }
                }
            } else if ("d".equals(operation)) {
                log.warn("Outbox event DELETE edildi - bu normal değildir");
            }
            
        } catch (Exception e) {
            log.error("❌ Debezium Outbox event işlemede hata: {}", e.getMessage(), e);
        }
    }

    /**
     * CDC event JSON'ından OutboxEvent entity'sini parse et.
     */
    private OutboxEvent parseOutboxEvent(JsonNode afterData) throws Exception {
        OutboxEvent event = new OutboxEvent();
        
        event.setId(UUID.fromString(afterData.get("id").asText()));
        event.setAggregateType(afterData.get("aggregate_type").asText());
        event.setAggregateId(afterData.get("aggregate_id").asText());
        event.setEventType(afterData.get("event_type").asText());
        event.setPayload(afterData.get("payload").asText());
        
        // Optional fields
        if (afterData.has("error_message") && !afterData.get("error_message").isNull()) {
            event.setErrorMessage(afterData.get("error_message").asText());
        }
        if (afterData.has("retry_count")) {
            event.setRetryCount(afterData.get("retry_count").asInt());
        }
        
        // Status enum conversion
        String statusStr = afterData.get("status").asText();
        event.setStatus(OutboxStatus.valueOf(statusStr));
        
        // Timestamp parsing
        if (afterData.has("created_at") && !afterData.get("created_at").isNull()) {
            event.setCreatedAt(Instant.parse(afterData.get("created_at").asText()));
        }
        if (afterData.has("processed_at") && !afterData.get("processed_at").isNull()) {
            event.setProcessedAt(Instant.parse(afterData.get("processed_at").asText()));
        }
        
        return event;
    }

    /**
     * Outbox event'ini ilgili Kafka topic'ine yayın.
     * EventType kullanarak dinamik topic seçimi yap (testEvent, orderCreatedEvent, vb.)
     */
    private void publishEvent(OutboxEvent event) {
        try {
            String bindingName = event.getEventType() + "-out-0";
            boolean sent = streamBridge.send(bindingName, event.getPayload());
            
            if (sent) {
                log.info("📤 Event StreamBridge'e gönderildi: {} -> {}", 
                        bindingName, event.getPayload());
            } else {
                log.warn("⚠️ Event StreamBridge'e gönderilemedi: {}", bindingName);
            }
        } catch (Exception e) {
            log.error("❌ Event Kafka'ya göndermede hata: {}", e.getMessage(), e);
            throw new RuntimeException("Event publishing failed", e);
        }
    }
}

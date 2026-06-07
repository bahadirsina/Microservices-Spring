#!/bin/bash

# Debezium PostgreSQL Connector Setup Script
# Kafka Connect REST API üzerinden Debezium PostgreSQL Connector'ı configure et
#
# Kullanım: bash debezium-connector-setup.sh
# 
# Ön-koşullar:
# 1. Kafka Connect çalışıyor (http://localhost:8083)
# 2. PostgreSQL running ve wal_level=logical (docker-compose'ta otomatik)
# 3. Replication slot ve publication oluşturulmuş (postgres-debezium-setup.sql)

CONNECT_URL="http://localhost:8083"
CONNECTOR_NAME="product-db-connector"

echo "🔧 Debezium PostgreSQL Connector setup başlıyor..."
echo "Kafka Connect URL: $CONNECT_URL"

# 1. Eski connector varsa sil (idempotent operation)
echo "📋 Mevcut connector kontrol ediliyor..."
curl -s -X GET "$CONNECT_URL/connectors/$CONNECTOR_NAME" > /dev/null 2>&1

if [ $? -eq 0 ]; then
    echo "⚠️  Eski connector $CONNECTOR_NAME bulundu, siliniyor..."
    curl -s -X DELETE "$CONNECT_URL/connectors/$CONNECTOR_NAME" | jq .
    
    # Silme işleminin tamamlanması için bekle
    sleep 2
fi

# 2. Yeni connector oluştur
echo "✅ Yeni Debezium PostgreSQL Connector oluşturuluyor..."

curl -s -X POST "$CONNECT_URL/connectors" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "'$CONNECTOR_NAME'",
    "config": {
      "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
      
      # PostgreSQL Bağlantı Bilgileri
      "database.hostname": "product-db",
      "database.port": "5432",
      "database.user": "postgres",
      "database.password": "test12345",
      "database.dbname": "products",
      
      # Debezium Yapılandırması
      "plugin.name": "pgoutput",
      "publication.name": "dbz_publication",
      "slot.name": "dbz_slot",
      
      # Capture Configuration
      "table.include.list": "public.outbox",
      "topic.prefix": "dbz",
      
      # Event Handling
      "heartbeat.interval.ms": "10000",
      "heartbeat.action.query": "SELECT 1;",
      
      # Transformations
      "transforms": "route",
      "transforms.route.type": "org.apache.kafka.connect.transforms.RegexRouter",
      "transforms.route.regex": "([^.]+)\\.([^.]+)\\.([^.]+)",
      "transforms.route.replacement": "$3",
      
      # Connection Pool
      "max.queue.size": 8192,
      "poll.interval.ms": 1000,
      
      # Logging
      "errors.log.enable": true,
      "errors.log.include.messages": true
    }
  }' | jq .

echo ""
echo "✅ Connector kurulumu tamamlandı!"
echo ""
echo "📊 Connector status kontrol ediliyor..."
sleep 3

curl -s -X GET "$CONNECT_URL/connectors/$CONNECTOR_NAME/status" | jq .

echo ""
echo "🎯 Debezium events monitoring için:"
echo "   kafka-ui: http://localhost:8080"
echo "   Kafka topics: dbz_outbox"
echo ""
echo "💾 PostgreSQL replication status:"
echo "   SELECT * FROM pg_replication_slots;"
echo "   SELECT * FROM pg_stat_replication;"

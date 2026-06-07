-- PostgreSQL Debezium Setup Script
-- Debezium PostgreSQL Connector için gerekli configurasyonları ayarla
-- 
-- Kullanım:
-- psql -h localhost -p 5433 -U postgres -d products -f postgres-debezium-setup.sql

-- 1. Replication slot oluştur (eğer yoksa)
-- NOT: Logical replication plugin olarak 'pgoutput' kullanılır (PostgreSQL 10+)
SELECT CASE 
    WHEN NOT EXISTS (SELECT 1 FROM pg_replication_slots WHERE slot_name = 'dbz_slot')
    THEN 
        -- Slot oluştur (output plugin: pgoutput)
        (SELECT pg_create_logical_replication_slot('dbz_slot', 'pgoutput'))::text
    ELSE 
        'Replication slot dbz_slot already exists'::text
END;

-- 2. Publication oluştur (eğer yoksa) - Outbox tablosu için
-- Publication: Hangi tablolardaki değişikliklerin replicate edileceğini belirler
CREATE PUBLICATION IF NOT EXISTS dbz_publication FOR TABLE public.outbox;

-- 3. Publication'a Outbox tablosunu ekle (sadece güvenlik için verify)
-- Outbox tablosu zaten publication'da, ama kontrol edelim
-- SELECT schemaname, tablename FROM pg_publication_tables 
-- WHERE schemaname = 'public' AND tablename = 'outbox';

-- 4. Verification Queries - Debezium setup'ın doğru olduğunu kontrol et
-- Replication slot'ları listele
SELECT slot_name, plugin, slot_type, active FROM pg_replication_slots;

-- Publication'ları listele
SELECT pubname, schemaname, tablename FROM pg_publication_tables;

-- Outbox tablosu varlığını kontrol et
SELECT EXISTS(SELECT 1 FROM information_schema.tables 
    WHERE table_schema = 'public' AND table_name = 'outbox');

-- Outbox tablosunun yapısını kontrol et
\d public.outbox

-- 5. Debezium connector health check için gerekli permissions
-- (PostgreSQL user 'postgres' zaten replication yapabilir)
-- Eğer başka bir user kullanıyorsan şunu çalıştır:
-- ALTER ROLE postgres WITH REPLICATION;
-- ALTER ROLE postgres WITH SUPERUSER;

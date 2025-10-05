# --- Server ---
server.port=8080

# --- DataSource (PostgreSQL) ---
spring.datasource.url=jdbc:postgresql://localhost:5432/hotel
spring.datasource.username=hotel_user
spring.datasource.password=change_me

# --- JPA/Hibernate ---
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.jdbc.time_zone=UTC

# --- Swagger / OpenAPI ---
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html

# --- Caching (Caffeine) ---
spring.cache.type=caffeine
spring.cache.cache-names=inventoryByRoom,inventorySearch
spring.cache.caffeine.spec=maximumSize=10000,expireAfterWrite=10m,recordStats

# --- Security (JWT) ---
app.jwt.secret=CHANGE_ME
app.jwt.access-token-ttl=PT15M
app.jwt.refresh-token-ttl=PT7D

# --- Stripe ---
stripe.secretKey=sk_test_xxx
stripe.webhookSecret=whsec_xxx

# --- CORS (comma-separated) ---
app.cors.allowed-origins=http://localhost:3000

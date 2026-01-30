# Wallet API - Deployment Guide

## Production Deployment

This guide covers deploying the Wallet API to production environments.

## Docker Deployment

### Building the Docker Image

```bash
# Build the image
docker build -t wallet-api:1.0.0 .

# Tag for registry
docker tag wallet-api:1.0.0 your-registry/wallet-api:1.0.0

# Push to registry
docker push your-registry/wallet-api:1.0.0
```

### Running with Docker Compose (Production)

Create a `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: ${DB_NAME}
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - wallet-network
    restart: always
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER}"]
      interval: 10s
      timeout: 5s
      retries: 5

  wallet-api:
    image: wallet-api:1.0.0
    environment:
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: ${DB_NAME}
      DB_USER: ${DB_USER}
      DB_PASSWORD: ${DB_PASSWORD}
      DB_POOL_SIZE: 50
      DB_MIN_IDLE: 10
      SERVER_PORT: 8080
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - wallet-network
    restart: always
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          cpus: '1'
          memory: 1G

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
      - ./ssl:/etc/nginx/ssl:ro
    depends_on:
      - wallet-api
    networks:
      - wallet-network
    restart: always

volumes:
  postgres-data:

networks:
  wallet-network:
    driver: bridge
```

### Production Environment Variables

Create `.env.prod`:

```bash
# Database
DB_NAME=wallet_production
DB_USER=prod_user
DB_PASSWORD=<strong-random-password>
DB_POOL_SIZE=50
DB_MIN_IDLE=10

# Server
SERVER_PORT=8080
```

### Deploy Production

```bash
export $(cat .env.prod | xargs)
docker-compose -f docker-compose.prod.yml up -d
```

## Kubernetes Deployment

### Deployment Manifest

Create `k8s/deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: wallet-api
  labels:
    app: wallet-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: wallet-api
  template:
    metadata:
      labels:
        app: wallet-api
    spec:
      containers:
      - name: wallet-api
        image: wallet-api:1.0.0
        ports:
        - containerPort: 8080
          name: http
        env:
        - name: DB_HOST
          valueFrom:
            configMapKeyRef:
              name: wallet-config
              key: db-host
        - name: DB_NAME
          valueFrom:
            configMapKeyRef:
              name: wallet-config
              key: db-name
        - name: DB_USER
          valueFrom:
            secretKeyRef:
              name: wallet-secrets
              key: db-user
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: wallet-secrets
              key: db-password
        - name: DB_POOL_SIZE
          value: "50"
        - name: DB_MIN_IDLE
          value: "10"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5

---
apiVersion: v1
kind: Service
metadata:
  name: wallet-api-service
spec:
  selector:
    app: wallet-api
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8080
  type: LoadBalancer
```

### ConfigMap

Create `k8s/configmap.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: wallet-config
data:
  db-host: postgres
  db-name: wallet_production
```

### Secrets

```bash
kubectl create secret generic wallet-secrets \
  --from-literal=db-user=prod_user \
  --from-literal=db-password=<strong-random-password>
```

### Deploy to Kubernetes

```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
```

## Performance Tuning

### Database Connection Pool

For 1000+ RPS, adjust connection pool:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### Thread Pool Configuration

```yaml
spring:
  threads:
    virtual:
      enabled: true  # Enable virtual threads (Java 19+)
```

### Database Indexes

Ensure indexes are created:

```sql
CREATE INDEX CONCURRENTLY idx_wallet_id ON wallets(wallet_id);
CREATE INDEX CONCURRENTLY idx_transaction_wallet ON transactions(wallet_id);
CREATE INDEX CONCURRENTLY idx_transaction_created ON transactions(created_at);
```

## Monitoring and Logging

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

### Metrics

Enable Micrometer metrics:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
```

### Logging

Monitor logs:

```bash
docker-compose logs -f wallet-api
```

## Database Backup

### PostgreSQL Backup

```bash
docker-compose exec postgres pg_dump -U wallet_user wallet_db > backup.sql
```

### Restore Backup

```bash
docker-compose exec -T postgres psql -U wallet_user wallet_db < backup.sql
```

## SSL/TLS Configuration

### NGINX SSL Setup

Create `nginx.conf`:

```nginx
upstream wallet_api {
    server wallet-api:8080;
}

server {
    listen 443 ssl http2;
    server_name api.example.com;

    ssl_certificate /etc/nginx/ssl/cert.pem;
    ssl_certificate_key /etc/nginx/ssl/key.pem;

    location / {
        proxy_pass http://wallet_api;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

server {
    listen 80;
    server_name api.example.com;
    return 301 https://$server_name$request_uri;
}
```

## Troubleshooting

### Connection Pool Exhaustion

Symptoms: `HikariPool - Connection is not available, request timed out`

Solution:
- Increase `maximum-pool-size`
- Check for connection leaks in code
- Monitor active connections

### Database Locks

Symptoms: Slow queries, timeout errors

Solution:
- Monitor locks: `SELECT * FROM pg_locks;`
- Tune timeout: `statement_timeout = '30s'`
- Use pessimistic locking (already implemented)

### Memory Usage

Monitor and adjust:

```bash
docker stats wallet-api
```

Increase heap size:

```bash
JAVA_OPTS="-Xmx2G -Xms1G"
```

## Security Checklist

- [ ] Change default database password
- [ ] Enable HTTPS/SSL
- [ ] Configure firewall rules
- [ ] Use secrets management (Vault, AWS Secrets Manager)
- [ ] Enable audit logging
- [ ] Set up rate limiting
- [ ] Configure CORS policies
- [ ] Use API keys/authentication
- [ ] Regular security updates
- [ ] Database encryption at rest

## Load Testing

### Using Apache JMeter

1. Create test plan with 1000 threads
2. Add HTTP request sampler
3. Set ramp-up period to 60 seconds
4. Run for 5-10 minutes

### Using k6

```javascript
import http from 'k6/http';
import { check } from 'k6';

export let options = {
  stages: [
    { duration: '2m', target: 1000 },
    { duration: '5m', target: 1000 },
    { duration: '2m', target: 0 },
  ],
};

export default function () {
  let url = 'http://localhost:8080/api/v1/wallet';
  let payload = JSON.stringify({
    walletId: '550e8400-e29b-41d4-a716-446655440000',
    operationType: 'DEPOSIT',
    amount: 1.00,
  });

  let params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  let res = http.post(url, payload, params);
  check(res, {
    'status is 200': (r) => r.status === 200,
  });
}
```

## Maintenance

### Regular Tasks

- Monitor disk space (database growth)
- Review and rotate logs
- Apply security patches
- Backup database weekly
- Monitor application metrics
- Review slow query logs
- Update dependencies

## Support

For deployment issues, check:
1. Application logs
2. Database connectivity
3. Environment variables
4. Resource limits
5. Network configuration

DEPLOYMENT

Ensure environment variables are set for database:

export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/wallet
export SPRING_DATASOURCE_USERNAME=wallet
export SPRING_DATASOURCE_PASSWORD=1234


Build and run Docker containers:

docker-compose up --build


Test API endpoints via Postman or cURL.
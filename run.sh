#!/bin/bash

# Wallet API Helper Scripts

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Functions
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}→ $1${NC}"
}

# Command handlers
start_docker() {
    print_info "Starting services with Docker Compose..."
    docker-compose -f "$SCRIPT_DIR/docker-compose.yml" up -d
    print_success "Services started"
    print_info "API available at: http://localhost:8080"
}

stop_docker() {
    print_info "Stopping services..."
    docker-compose -f "$SCRIPT_DIR/docker-compose.yml" down
    print_success "Services stopped"
}

restart_docker() {
    print_info "Restarting services..."
    docker-compose -f "$SCRIPT_DIR/docker-compose.yml" restart
    print_success "Services restarted"
}

logs_docker() {
    docker-compose -f "$SCRIPT_DIR/docker-compose.yml" logs -f "$@"
}

build_local() {
    print_info "Building locally with Maven..."
    mvn clean install
    print_success "Build complete"
}

test_local() {
    print_info "Running tests..."
    mvn test
    print_success "Tests completed"
}

run_local() {
    print_info "Running application locally..."
    mvn spring-boot:run
}

health_check() {
    print_info "Checking application health..."
    response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)
    if [ "$response" -eq 200 ]; then
        print_success "Application is healthy"
    else
        print_error "Application health check failed (HTTP $response)"
    fi
}

test_api() {
    WALLET_ID="${1:-550e8400-e29b-41d4-a716-446655440000}"
    
    print_info "Testing API with wallet ID: $WALLET_ID"
    
    # Create wallet
    print_info "Creating wallet..."
    curl -X POST http://localhost:8080/api/v1/wallet \
        -H "Content-Type: application/json" \
        -d '{
            "walletId": "'$WALLET_ID'",
            "operationType": "DEPOSIT",
            "amount": 1000.00
        }' | jq .
    
    # Get balance
    print_info "Getting balance..."
    curl -X GET http://localhost:8080/api/v1/wallets/$WALLET_ID | jq .
    
    # Withdraw
    print_info "Withdrawing 500..."
    curl -X POST http://localhost:8080/api/v1/wallet \
        -H "Content-Type: application/json" \
        -d '{
            "walletId": "'$WALLET_ID'",
            "operationType": "WITHDRAW",
            "amount": 500.00
        }' | jq .
}

docker_build() {
    print_info "Building Docker image..."
    docker-compose -f "$SCRIPT_DIR/docker-compose.yml" build
    print_success "Docker image built"
}

# Main
show_usage() {
    cat << EOF
Wallet API Helper Script

Usage: $0 <command> [options]

Commands:
    start           Start Docker services (docker-compose up -d)
    stop            Stop Docker services
    restart         Restart Docker services
    logs            Show Docker logs (use: logs [service-name])
    build-docker    Build Docker image
    
    build           Build project locally with Maven
    test            Run tests
    run             Run application locally
    
    health          Check application health
    test-api        Test API endpoints
    
    help            Show this help message

Examples:
    $0 start
    $0 logs wallet-api
    $0 test-api 550e8400-e29b-41d4-a716-446655440000

EOF
}

# Route commands
case "${1:-help}" in
    start)
        start_docker
        ;;
    stop)
        stop_docker
        ;;
    restart)
        restart_docker
        ;;
    logs)
        logs_docker "${@:2}"
        ;;
    build-docker)
        docker_build
        ;;
    build)
        build_local
        ;;
    test)
        test_local
        ;;
    run)
        run_local
        ;;
    health)
        health_check
        ;;
    test-api)
        test_api "${2:-}"
        ;;
    help|--help|-h)
        show_usage
        ;;
    *)
        print_error "Unknown command: $1"
        show_usage
        exit 1
        ;;
esac

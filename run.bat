@echo off
REM Wallet API Helper Script for Windows

setlocal enabledelayedexpansion

REM Colors and formatting
color 0A

if "%1"=="" (
    call :show_usage
    exit /b 0
)

if /i "%1"=="start" (
    call :start_docker
    exit /b %ERRORLEVEL%
)

if /i "%1"=="stop" (
    call :stop_docker
    exit /b %ERRORLEVEL%
)

if /i "%1"=="restart" (
    call :restart_docker
    exit /b %ERRORLEVEL%
)

if /i "%1"=="logs" (
    call :logs_docker %2
    exit /b %ERRORLEVEL%
)

if /i "%1"=="build-docker" (
    call :docker_build
    exit /b %ERRORLEVEL%
)

if /i "%1"=="build" (
    call :build_local
    exit /b %ERRORLEVEL%
)

if /i "%1"=="test" (
    call :test_local
    exit /b %ERRORLEVEL%
)

if /i "%1"=="run" (
    call :run_local
    exit /b %ERRORLEVEL%
)

if /i "%1"=="health" (
    call :health_check
    exit /b %ERRORLEVEL%
)

if /i "%1"=="test-api" (
    call :test_api %2
    exit /b %ERRORLEVEL%
)

if /i "%1"=="help" goto show_usage
if /i "%1"=="--help" goto show_usage
if /i "%1"=="-h" goto show_usage

echo Error: Unknown command: %1
call :show_usage
exit /b 1

:start_docker
echo [*] Starting services with Docker Compose...
docker-compose up -d
if !ERRORLEVEL! equ 0 (
    echo [+] Services started
    echo [*] API available at: http://localhost:8080
) else (
    echo [-] Failed to start services
)
exit /b !ERRORLEVEL!

:stop_docker
echo [*] Stopping services...
docker-compose down
if !ERRORLEVEL! equ 0 (
    echo [+] Services stopped
) else (
    echo [-] Failed to stop services
)
exit /b !ERRORLEVEL!

:restart_docker
echo [*] Restarting services...
docker-compose restart
if !ERRORLEVEL! equ 0 (
    echo [+] Services restarted
) else (
    echo [-] Failed to restart services
)
exit /b !ERRORLEVEL!

:logs_docker
if "%~1"=="" (
    docker-compose logs -f
) else (
    docker-compose logs -f %1
)
exit /b !ERRORLEVEL!

:docker_build
echo [*] Building Docker image...
docker-compose build
if !ERRORLEVEL! equ 0 (
    echo [+] Docker image built
) else (
    echo [-] Failed to build Docker image
)
exit /b !ERRORLEVEL!

:build_local
echo [*] Building locally with Maven...
call mvn clean install
if !ERRORLEVEL! equ 0 (
    echo [+] Build complete
) else (
    echo [-] Build failed
)
exit /b !ERRORLEVEL!

:test_local
echo [*] Running tests...
call mvn test
if !ERRORLEVEL! equ 0 (
    echo [+] Tests completed
) else (
    echo [-] Tests failed
)
exit /b !ERRORLEVEL!

:run_local
echo [*] Running application locally...
call mvn spring-boot:run
exit /b !ERRORLEVEL!

:health_check
echo [*] Checking application health...
for /f %%A in ('powershell -Command "try { $response = Invoke-WebRequest -Uri http://localhost:8080/actuator/health -UseBasicParsing; $response.StatusCode } catch { 0 }"') do set "response=%%A"
if "!response!"=="200" (
    echo [+] Application is healthy
    exit /b 0
) else (
    echo [-] Application health check failed ^(HTTP !response!^)
    exit /b 1
)

:test_api
set WALLET_ID=%~1
if "!WALLET_ID!"=="" set WALLET_ID=550e8400-e29b-41d4-a716-446655440000

echo [*] Testing API with wallet ID: !WALLET_ID!

echo [*] Creating wallet...
curl -X POST http://localhost:8080/api/v1/wallet ^
  -H "Content-Type: application/json" ^
  -d "{\"walletId\": \"!WALLET_ID!\", \"operationType\": \"DEPOSIT\", \"amount\": 1000.00}"

echo.
echo [*] Getting balance...
curl -X GET http://localhost:8080/api/v1/wallets/!WALLET_ID!

echo.
echo [*] Withdrawing 500...
curl -X POST http://localhost:8080/api/v1/wallet ^
  -H "Content-Type: application/json" ^
  -d "{\"walletId\": \"!WALLET_ID!\", \"operationType\": \"WITHDRAW\", \"amount\": 500.00}"

echo.
exit /b 0

:show_usage
echo.
echo Wallet API Helper Script
echo.
echo Usage: %0 ^<command^> [options]
echo.
echo Commands:
echo   start           Start Docker services
echo   stop            Stop Docker services
echo   restart         Restart Docker services
echo   logs            Show Docker logs
echo   build-docker    Build Docker image
echo.
echo   build           Build project locally with Maven
echo   test            Run tests
echo   run             Run application locally
echo.
echo   health          Check application health
echo   test-api        Test API endpoints
echo.
echo   help            Show this help message
echo.
echo Examples:
echo   %0 start
echo   %0 logs
echo   %0 test-api 550e8400-e29b-41d4-a716-446655440000
echo.
exit /b 0

endlocal

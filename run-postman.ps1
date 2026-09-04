# Прогон постман-тестов (sprint.json) локально — воспроизводит то, что делает CI на GitHub.
#
# Как работает:
#   1. пересобирает приложение (jar) из текущего кода
#   2. поднимает приложение на чистой H2-базе на порту 8080
#   3. ждёт, пока приложение станет доступно
#   4. запускает newman по tests/postman/sprint.json c baseUrl=http://localhost:8080
#   5. останавливает приложение и выводит итог
#
# Порт 8080 критичен: часть postman-запросов в test-скриптах (pm.sendRequest)
# хардкодит http://localhost:8080, поэтому приложение должно слушать именно 8080.
#
# Требования: mvn, java (JDK 21), node, установленный глобально newman.
# Использование:
#   powershell -ExecutionPolicy Bypass -File .\run-postman.ps1

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $ProjectRoot

$Collection  = "tests/postman/sprint.json"
$Port        = 8080
$TempDir     = Join-Path $env:TEMP "filmorate-postman"
$DbDir       = Join-Path $TempDir "db"
$CpDirectory = Join-Path $ProjectRoot "target/classes"   # чтобы рабочая директория корневая не блокировала repo-db

if (-not (Test-Path $Collection)) {
    Write-Host "Не найден файл коллекции: $Collection" -ForegroundColor Red
    exit 1
}

# 1. Пересборка
Write-Host "`n=== 1/5  Сборка jar (mvn -DskipTests package) ===" -ForegroundColor Cyan
& mvn -DskipTests package 2>&1 | Select-String -Pattern "BUILD SUCCESS|BUILD FAILURE|ERROR"
if ($LASTEXITCODE -ne 0) {
    Write-Host "Сборка не удалась." -ForegroundColor Red
    exit 1
}
$Jar = (Resolve-Path "target/filmorate-0.0.1-SNAPSHOT.jar").Path

# 2. Чистая база
if (Test-Path $TempDir) {
    Remove-Item -Recurse -Force $TempDir
}
New-Item -ItemType Directory -Force -Path $DbDir | Out-Null
$DbUrl = "jdbc:h2:file:" + ($DbDir.Replace('\', '/')) + "/filmorate"

# 3. Запуск приложения
Write-Host "`n=== 2/5  Запуск приложения на порту $Port (чистая БД) ===" -ForegroundColor Cyan
$StdOut = Join-Path $TempDir "app.out.log"
$StdErr = Join-Path $TempDir "app.err.log"
$App = Start-Process -FilePath "java" -ArgumentList @("-jar", $Jar, "--server.port=$Port", "--spring.datasource.url=$DbUrl") -WindowStyle Hidden -RedirectStandardOutput $StdOut -RedirectStandardError $StdErr -PassThru
Write-Host "PID приложения: $($App.Id)"

# 4. Ожидание готовности
Write-Host "`n=== 3/5  Ожидание готовности приложения ===" -ForegroundColor Cyan
$up = $false
for ($i = 0; $i -lt 40; $i++) {
    if ($App.HasExited) {
        Write-Host "Приложение завершилось с ошибкой при старте:" -ForegroundColor Red
        if (Test-Path $StdErr) { Get-Content $StdErr -Tail 15 }
        if (Test-Path $StdOut) { Get-Content $StdOut -Tail 15 }
        exit 1
    }
    try {
        # Любой HTTP-ответ (включая 404 от '/') означает, что сервер поднялся.
        $resp = Invoke-WebRequest -Uri "http://localhost:$Port" -UseBasicParsing -TimeoutSec 2
        $up = $true
        break
    } catch {
        # Проверяем: сервер ответил (даже ошибкой 4xx/5xx) или сетевая недоступность?
        if ($_.Exception.Response) {
            $up = $true
            break
        }
        Start-Sleep -Milliseconds 1000
    }
}
if (-not $up) {
    Write-Host "Приложение не поднялось за отведённое время." -ForegroundColor Red
    Stop-Process -Id $App.Id -Force -ErrorAction SilentlyContinue
    exit 1
}
Write-Host "Приложение доступно."

# 5. Прогон newman
Write-Host "`n=== 4/5  Прогон newman (sprint.json) ===" -ForegroundColor Cyan
try {
    & node "$env:APPDATA\npm\node_modules\newman\bin\newman.js" run $Collection --delay-request 50 --env-var "baseUrl=http://localhost:$Port" -r cli
} finally {
    # 6. Остановка приложения
    Stop-Process -Id $App.Id -Force -ErrorAction SilentlyContinue
    Write-Host "`n=== 5/5  Приложение остановлено ===" -ForegroundColor Cyan
}
exit $LASTEXITCODE
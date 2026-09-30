$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

# Tự động tìm Java nếu JAVA_HOME chưa cấu hình
if (-not $env:JAVA_HOME) {
    if (Test-Path 'C:/Program Files/Java/jdk-26.0.2.1') {
        $env:JAVA_HOME = 'C:/Program Files/Java/jdk-26.0.2.1'
        $env:Path = $env:JAVA_HOME + '/bin;' + $env:Path
    } elseif (Test-Path 'C:/Program Files/Java/jdk-21*') {
        $foundJdk = Get-Item 'C:/Program Files/Java/jdk-21*' | Select-Object -First 1
        $env:JAVA_HOME = $foundJdk.FullName
        $env:Path = $env:JAVA_HOME + '/bin;' + $env:Path
    }
}

if (-not (Test-Path '.env')) {
    Write-Host "Chưa có file .env. Đang tự động chạy setup.ps1 để khởi tạo cấu hình..." -ForegroundColor Yellow
    & ./setup.ps1
}

Write-Host "Đang build dự án (bỏ qua unit test khi khởi động nhanh)..." -ForegroundColor Cyan
& ./mvnw.cmd -B -ntp package -DskipTests
if ($LASTEXITCODE -ne 0) { throw 'Build failed.' }

Write-Host "Khởi động ứng dụng Spring Boot JWT Nimbus tại http://localhost:8005/login ..." -ForegroundColor Green
& java -jar target/24133045_BuiThanhPhuc_JWT_Nimbus-1.0.0.jar



param(
    [string]$DbUser = '',
    [string]$DbPassword = '',
    [string]$ReferenceProject = 'D:\java\BTAP09\24133045_BuiThanhPhuc_Security_VD12'
)
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

if (-not (Test-Path '.env')) {
    Write-Host "[1/3] Khởi tạo file cấu hình .env..." -ForegroundColor Cyan
    $reference = @{}
    if (Test-Path (Join-Path $ReferenceProject '.env')) {
        foreach ($line in Get-Content -LiteralPath (Join-Path $ReferenceProject '.env')) {
            if ($line -match '^\s*([^#=\s]+)\s*=(.*)$') { $reference[$Matches[1]] = $Matches[2] }
        }
    }
    
    $finalUser = if ($DbUser) { $DbUser } elseif ($reference['DB_USERNAME']) { $reference['DB_USERNAME'] } else { 'root' }
    $finalPass = if ($DbPassword) { $DbPassword } elseif ($reference['DB_PASSWORD']) { $reference['DB_PASSWORD'] } else { 'root' }

    $bytes = New-Object byte[] 32
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
    $secret = [Convert]::ToBase64String($bytes)

    $content = @(
        'SERVER_PORT=8005',
        'DB_URL=jdbc:mysql://localhost:3306/tuan10_jwt_nimbus?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8',
        ("DB_USERNAME=$finalUser"),
        ("DB_PASSWORD=$finalPass"),
        ("JWT_SECRET=$secret"),
        'JWT_EXPIRATION=3600000'
    )
    [System.IO.File]::WriteAllLines((Join-Path $PSScriptRoot '.env'), $content, (New-Object System.Text.UTF8Encoding($false)))
    Write-Host "  -> Đã tạo file .env với khóa bí mật JWT ngẫu nhiên (32 bytes Base64)." -ForegroundColor Green
} else {
    Write-Host "[1/3] File .env đã tồn tại, giữ nguyên cấu hình hiện tại." -ForegroundColor Green
}

$config = @{}
foreach ($line in Get-Content '.env') {
    if ($line -match '^\s*([^#=\s]+)\s*=(.*)$') { $config[$Matches[1]] = $Matches[2] }
}

Write-Host "[2/3] Cấu hình cơ sở dữ liệu MySQL..." -ForegroundColor Cyan
$mysql = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
if (-not (Test-Path $mysql)) {
    $found = Get-Command mysql.exe -ErrorAction SilentlyContinue
    if ($found) { $mysql = $found.Source } else { $mysql = $null }
}

if ($mysql -and (Test-Path $mysql)) {
    $previousPassword = $env:MYSQL_PWD
    try {
        $env:MYSQL_PWD = $config['DB_PASSWORD']
        Get-Content -Raw -Encoding utf8 'database/schema.sql' | & $mysql --host=localhost --port=3306 "--user=$($config['DB_USERNAME'])" --default-character-set=utf8mb4
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  -> Đã tạo/cập nhật cơ sở dữ liệu: tuan10_jwt_nimbus và tuan10_jwt_nimbus_test." -ForegroundColor Green
        } else {
            Write-Host "  [CẢNH BÁO] Không thể tự động chạy schema.sql qua mysql.exe. Vui lòng mở database/schema.sql và chạy trên MySQL Workbench." -ForegroundColor Yellow
        }
    } catch {
        Write-Host "  [CẢNH BÁO] Lỗi khi kết nối MySQL qua script: $_" -ForegroundColor Yellow
        Write-Host "  Vui lòng đảm bảo MySQL đang chạy và chạy file database/schema.sql thủ công." -ForegroundColor Yellow
    } finally {
        $env:MYSQL_PWD = $previousPassword
    }
} else {
    Write-Host "  [CHÚ Ý] Không tìm thấy mysql.exe trong PATH. Vui lòng nhập thủ công file database/schema.sql vào MySQL Workbench/DBeaver." -ForegroundColor Yellow
}

Write-Host "[3/3] Hoàn tất chuẩn bị môi trường!" -ForegroundColor Green

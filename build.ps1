# Fluxis Economy - Otomatik Derleme Scripti
# SkyWind Alliance

Write-Host "--- Fluxis Derleme Islemi Basliyor ---" -ForegroundColor Yellow

# 1. Java Kontrolü
if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Host "HATA: Java bulunamadi! Lutfen JDK 17 veya uzeri yukleyin." -ForegroundColor Red
    return
}

# 2. Maven Kontrolü veya Indirme
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Host "Maven bulunamadi, tasinabilir Maven indiriliyor..." -ForegroundColor Cyan
    $mvnUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
    $output = "$PSScriptRoot\maven.zip"
    $extractPath = "$PSScriptRoot\maven_temp"

    if (-not (Test-Path $extractPath)) {
        Invoke-WebRequest -Uri $mvnUrl -OutFile $output
        Expand-Archive -Path $output -DestinationPath $extractPath
        Remove-Item $output
    }
    $mvnPath = Get-ChildItem -Path "$extractPath\apache-maven-*\bin\mvn.cmd" | Select-Object -ExpandProperty FullName
} else {
    $mvnPath = "mvn"
}

# 3. Derleme
Write-Host "Plugin paketleniyor..." -ForegroundColor Green
& $mvnPath clean package

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nBASARILI! Plugin hazır." -ForegroundColor Green
    Write-Host "Dosya: target\fluxis-1.0.0-SNAPSHOT.jar" -ForegroundColor White
} else {
    Write-Host "`nDERLEME HATASI! Lutfen yukaridaki loglari kontrol et." -ForegroundColor Red
}

Write-Host "`nCikmak icin bir tusa basin..."
$null = [Console]::ReadKey()

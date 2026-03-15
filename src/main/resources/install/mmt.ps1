$CallDir = Get-Location

$LocalJar = Join-Path $CallDir ".mmt/mmt.jar"
$UserJar = Join-Path $HOME ".mmt/mmt.jar"

if (Test-Path $LocalJar) {
    & java -jar $LocalJar @args
    exit $LASTEXITCODE
}

if (Test-Path $UserJar) {
    & java -jar $UserJar @args
    exit $LASTEXITCODE
}

Write-Host "mmt.jar not found"
exit 1
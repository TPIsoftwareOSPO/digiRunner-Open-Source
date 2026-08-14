[CmdletBinding()]
param(
    [ValidateRange(2000, 9999)]
    [int] $Year = (Get-Date).Year,

    [Nullable[datetime]] $ExpiryDate
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$expiry = if ($null -eq $ExpiryDate) {
    [datetime]::new($Year, 12, 31)
} else {
    $ExpiryDate.Date
}

$angularRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\..\..\..')).Path
$gatewayRoot = Split-Path -Parent $angularRoot
$repositoryRoot = Split-Path -Parent $gatewayRoot

$gatewayJar = Get-ChildItem -LiteralPath (Join-Path $gatewayRoot 'build\libs') `
    -Filter 'dgrv4-gateway-*.jar' -File -ErrorAction SilentlyContinue |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1
if ($null -eq $gatewayJar) {
    throw "Gateway JAR not found under $gatewayRoot\build\libs. Build the backend first."
}

$commonLibDirectory = Join-Path $repositoryRoot 'dgrv4_Common_lib\build\libs'
$commonJar = Get-ChildItem -LiteralPath $commonLibDirectory `
    -Filter 'dgrv4-common-*.jar' -File -ErrorAction SilentlyContinue |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1
if ($null -eq $commonJar) {
    throw "Common library JAR not found under $commonLibDirectory. Build dgrv4_Common_lib first."
}

$dependencyDirectory = Join-Path $gatewayRoot 'build\libs\lib'
if (-not (Test-Path -LiteralPath $dependencyDirectory -PathType Container)) {
    throw "Gateway dependency directory not found: $dependencyDirectory"
}

$helperSource = Join-Path $PSScriptRoot 'InvokeLicense.java'
$temporaryDirectory = Join-Path ([IO.Path]::GetTempPath()) `
    ('dgr-alpha-license-' + [guid]::NewGuid().ToString('N'))
[void] (New-Item -ItemType Directory -Path $temporaryDirectory)

try {
    & javac -d $temporaryDirectory $helperSource
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to compile the temporary LicenseUtil invoker. javac exit code: $LASTEXITCODE"
    }

    $loaderPath = "$temporaryDirectory,$($commonJar.FullName),$dependencyDirectory"
    & java `
        "-Dloader.path=$loaderPath" `
        '-Dloader.main=InvokeLicense' `
        -cp $gatewayJar.FullName `
        org.springframework.boot.loader.launch.PropertiesLauncher `
        $expiry.Year $expiry.Month $expiry.Day
    if ($LASTEXITCODE -ne 0) {
        throw "License generation failed. java exit code: $LASTEXITCODE"
    }
} finally {
    if (Test-Path -LiteralPath $temporaryDirectory) {
        [IO.Directory]::Delete($temporaryDirectory, $true)
    }
}

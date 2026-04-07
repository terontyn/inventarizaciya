$requiredPaths = @(
    "README.md",
    ".gitignore",
    ".editorconfig",
    "settings.gradle.kts",
    "build.gradle.kts",
    "Documentation",
    "services/user-service/build.gradle.kts",
    "services/user-service/src/main/java/com/itam/userservice/boot/UserServiceApplication.java",
    "services/user-service/src/main/resources/application.yml",
    "services/asset-service/build.gradle.kts",
    "services/asset-service/src/main/java/com/itam/assetservice/boot/AssetServiceApplication.java",
    "services/asset-service/src/main/resources/application.yml",
    "ops/docker/docker-compose.local.yml",
    "scripts/db/init-local.sql",
    ".github/workflows/bootstrap-check.yml"
)

$missing = @()

foreach ($path in $requiredPaths) {
    if (-not (Test-Path $path)) {
        $missing += $path
    }
}

if ($missing.Count -gt 0) {
    Write-Error ("Repository baseline structure is incomplete. Missing paths:`n- " + ($missing -join "`n- "))
    exit 1
}

Write-Host "Repository baseline structure check passed."

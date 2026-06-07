# Build native installer with jpackage
# Requires JDK 17+ with jpackage tool

$AppName = "GestionMorgue"
$Version = "1.0.0"
$JarPath = "target\gestionmorgue-$Version.jar"
$OutputDir = "target\installer"

# Build the JAR first
Write-Host "Building application JAR..."
& "C:\apache\apache-maven-3.9.11\bin\mvn.cmd" package -DskipTests
if (-not $?) { exit 1 }

# Get JavaFX JMODs or use modular runtime
$JavaHome = [System.Environment]::GetEnvironmentVariable("JAVA_HOME")
if (-not $JavaHome) { $JavaHome = "C:\Program Files\Java\jdk-17" }

# Build with jpackage
Write-Host "Building native installer..."
& "$JavaHome\bin\jpackage.exe" `
    --type msi `
    --name $AppName `
    --app-version $Version `
    --description "Application de gestion de morgue" `
    --vendor "GestionMorgue" `
    --input target `
    --main-jar "gestionmorgue-$Version.jar" `
    --main-class com.gestionmorgue.App `
    --java-options "-Xmx512m" `
    --dest $OutputDir `
    --win-dir-chooser `
    --win-menu `
    --win-shortcut

Write-Host "Installer created at: $OutputDir\$AppName-$Version.msi"

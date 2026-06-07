$javadir = "$env:USERPROFILE\.m2\repository\org\openjfx"
$ver = "17.0.6"
$modpath = @("javafx-base","javafx-controls","javafx-graphics","javafx-fxml") | ForEach-Object {
    "$javadir\$_\$ver\$_-$ver-win.jar"
}
$modpathStr = $modpath -join ';'
$jar = Join-Path $PSScriptRoot "target\gestionmorgue-1.0.0.jar"
Start-Process -WindowStyle Hidden -FilePath "javaw" -ArgumentList @(
    "--module-path", "`"$modpathStr`"",
    "--add-modules", "javafx.controls,javafx.fxml",
    "-jar", "`"$jar`""
)
Write-Host "Application démarrée. Vérifiez votre bureau."

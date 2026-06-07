@echo off
cd /d "%~dp0"

set JAVAFX_BASE=%USERPROFILE%\.m2\repository\org\openjfx
set JAVAFX_VER=17.0.6

set MOD_PATH=%JAVAFX_BASE%\javafx-base\%JAVAFX_VER%\javafx-base-%JAVAFX_VER%-win.jar;%JAVAFX_BASE%\javafx-controls\%JAVAFX_VER%\javafx-controls-%JAVAFX_VER%-win.jar;%JAVAFX_BASE%\javafx-graphics\%JAVAFX_VER%\javafx-graphics-%JAVAFX_VER%-win.jar;%JAVAFX_BASE%\javafx-fxml\%JAVAFX_VER%\javafx-fxml-%JAVAFX_VER%-win.jar

start javaw --module-path "%MOD_PATH%" --add-modules javafx.controls,javafx.fxml -jar "target\gestionmorgue-1.0.0.jar"
exit

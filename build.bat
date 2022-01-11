@echo off

REM Uncomment and change the next line to customize your java 11 path
REM SET JAVA_HOME="\your\java\path"

call "%~dp0.\gradlew.bat" build
if ERRORLEVEL 1 goto :Error

goto :EOF

:Error
exit /b 1

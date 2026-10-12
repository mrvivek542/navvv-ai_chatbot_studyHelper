@echo off
setlocal
echo ========================================================
echo         StudyMind AI - Starting Application
echo ========================================================

REM Check if mvn is available in system PATH
where mvn >nul 2>nul
if %errorlevel% equ 0 (
    echo [INFO] System Maven detected. Starting Spring Boot...
    mvn spring-boot:run
    goto end
)

REM Check if portable maven exists in parent directory
if exist "..\maven\apache-maven-3.9.9\bin\mvn.cmd" (
    echo [INFO] Portable Maven detected in parent directory. Starting Spring Boot...
    "..\maven\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run
    goto end
)

echo [ERROR] Apache Maven was not found on your system PATH or in ..\maven\.
echo Please ensure Java 21+ and Maven 3.9+ are installed.
echo Visit https://maven.apache.org/download.cgi to download Maven.
pause

:end
endlocal

@echo off
chcp 65001 > nul

echo ===============================================
echo                Duke Test Runner
echo ===============================================
echo.

echo [1/4] Checking Java version...
java -version

REM check if using Java 21
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVAVER=%%g
)

set JAVAVER=%JAVAVER:"=%
for /f "delims=. tokens=1" %%v in ("%JAVAVER%") do (
    set MAJORVER=%%v
)

if NOT "%MAJORVER%"=="21" (
    echo.
    echo =============== ERROR ===============
    echo Please use Java 21 ^(current: %MAJORVER%^)
    echo ===================================
    exit /b 1
)

echo [√] Java version check passed
echo.

echo [2/4] Setting up test environment...
if exist ACTUAL.TXT (
    del ACTUAL.TXT
    echo Cleaned up previous test outputs
)

echo.
echo [3/4] Building with Gradle...
cd ..
call gradlew.bat shadowJar -q
IF ERRORLEVEL 1 (
    echo.
    echo =============== ERROR ===============
    echo         BUILD FAILURE
    echo ===================================
    exit /b 1
)
cd text-ui-test

echo [√] Build successful
echo.

echo [4/4] Running tests...
java -cp ..\build\libs\javafx-jeff.jar Duke < input.txt > ACTUAL.TXT

FC ACTUAL.TXT EXPECTED.TXT > nul
if ERRORLEVEL 1 (
    echo.
    echo =============== ERROR ===============
    echo           Tests FAILED
    echo ===================================
    exit /b 1
) else (
    echo.
    echo ============= SUCCESS ==============
    echo         All tests passed
    echo ===================================
    exit /b 0
)

@echo off
setlocal
rem ==========================================================
rem  Build CorridorHavoc.jar from the source code in main\
rem  Needs a JDK (javac and jar), version 8 or newer
rem ==========================================================
cd /d "%~dp0"

where javac >nul 2>nul
if errorlevel 1 (
    echo [ERROR] javac was not found. Install a JDK 8 or newer to build the game.
    goto failed
)

if exist build rmdir /s /q build
mkdir build

echo Compiling...
rem --release 8 makes the jar run on Java 8 and newer (only JDK 9+ knows this option)
javac --release 8 -encoding UTF-8 -d build main\*.java 2>nul
if errorlevel 1 (
    rem JDK 8 does not know --release, so try again without it
    javac -encoding UTF-8 -d build main\*.java
    if errorlevel 1 goto failed
)

echo Copying images and sounds...
xcopy /e /i /q /y main\res build\res >nul

echo Creating CorridorHavoc.jar...
jar cfe CorridorHavoc.jar Main -C build .
if errorlevel 1 goto failed

rmdir /s /q build
echo Done: CorridorHavoc.jar
if /i not "%~1"=="nopause" pause
exit /b 0

:failed
echo [ERROR] Build failed.
if /i not "%~1"=="nopause" pause
exit /b 1

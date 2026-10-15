@echo off
setlocal
title Corridor Havoc
rem ==========================================================
rem  Double-click this file to play Corridor Havoc
rem ==========================================================
cd /d "%~dp0"

rem ---- find Java (PATH first, then JAVA_HOME) ----
set "JAVA_EXE="
where java >nul 2>nul
if not errorlevel 1 set "JAVA_EXE=java"
if not defined JAVA_EXE if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

if not defined JAVA_EXE (
    echo.
    echo  Java was not found on this computer.
    echo  Please install Java 8 or newer, then run Play.bat again.
    echo  Free download: https://adoptium.net
    echo.
    pause
    exit /b 1
)

rem ---- build the game first if the jar is missing ----
if not exist "CorridorHavoc.jar" (
    echo CorridorHavoc.jar was not found, building it from the source code...
    call "%~dp0build.bat" nopause
    if errorlevel 1 (
        pause
        exit /b 1
    )
)

rem ---- start the game ----
echo Starting Corridor Havoc...
"%JAVA_EXE%" -jar CorridorHavoc.jar
if errorlevel 1 (
    echo.
    echo  The game closed with an error. See the message above.
    echo  If it says UnsupportedClassVersionError, install Java 8 or newer.
    echo.
    pause
)

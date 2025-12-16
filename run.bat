@echo off
title Food Delivery System
color 0A

echo ======================================
echo   FOOD DELIVERY SYSTEM BACKEND
echo ======================================
echo.

REM Check for required files
if not exist "lib\gson-2.8.9.jar" (
    echo ❌ ERROR: Missing lib\gson-2.8.9.jar
    echo Download from: https://repo1.maven.org/maven2/com/google/code/gson/gson/2.8.9/gson-2.8.9.jar
    pause
    exit /b 1
)

if not exist "lib\sqlite-jdbc-3.41.2.1.jar" (
    echo ❌ ERROR: Missing lib\sqlite-jdbc-3.41.2.1.jar
    echo Download from: https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.41.2.1/sqlite-jdbc-3.41.2.1.jar
    pause
    exit /b 1
)

echo ✅ Required JAR files found
echo.

REM Create directories if they don't exist
if not exist "database\" mkdir database
if not exist "frontend\" mkdir frontend

echo 🔧 Compiling...
javac -cp ".;lib\*" -encoding UTF-8 Main.java Database/*.java Login/*.java Order/*.java FoodItem/*.java Cart/*.java Menu/*.java PlaceOrder/*.java User/*.java

if %errorlevel% neq 0 (
    echo.
    echo ❌ COMPILATION FAILED!
    echo Check for errors above
    pause
    exit /b 1
)

echo.
echo ✅ Compilation successful!
echo.
echo 🚀 Starting server...
echo 📡 URL: http://localhost:8080
echo 📁 Frontend: http://localhost:8080/index.html
echo.
echo ⚠️  Press Ctrl+C to stop
echo ======================================
echo.

java -cp ".;lib\*" Main

echo.
echo ======================================
echo ⏹️  Server stopped
echo ======================================
pause
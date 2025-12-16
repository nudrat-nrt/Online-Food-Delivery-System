@echo off
echo ======================================
echo   CLEANING AND COMPILING PROJECT
echo ======================================
echo.

echo 1. Deleting old .class files...
del /Q *.class 2>nul
rmdir /S /Q Login 2>nul
rmdir /S /Q Order 2>nul
rmdir /S /Q FoodItem 2>nul
rmdir /S /Q Cart 2>nul
rmdir /S /Q Menu 2>nul
rmdir /S /Q Database 2>nul
rmdir /S /Q PlaceOrder 2>nul
rmdir /S /Q User 2>nul

echo 2. Recreating package directories...
mkdir Login Order FoodItem Cart Menu Database PlaceOrder User

echo 3. Compiling all Java files...
echo Compiling Main.java...
javac -cp ".;lib\*" -encoding UTF-8 Main.java

if %errorlevel% neq 0 (
    echo ❌ Compilation failed!
    pause
    exit /b 1
)

echo 4. Compiling package files...
javac -cp ".;lib\*" -encoding UTF-8 Database/*.java
javac -cp ".;lib\*" -encoding UTF-8 Login/*.java
javac -cp ".;lib\*" -encoding UTF-8 Order/*.java
javac -cp ".;lib\*" -encoding UTF-8 FoodItem/*.java
javac -cp ".;lib\*" -encoding UTF-8 Cart/*.java
javac -cp ".;lib\*" -encoding UTF-8 Menu/*.java
javac -cp ".;lib\*" -encoding UTF-8 PlaceOrder/*.java
javac -cp ".;lib\*" -encoding UTF-8 User/*.java

echo.
echo ✅ Compilation complete!
echo.
dir *.class
echo.
echo Ready to run...
pause
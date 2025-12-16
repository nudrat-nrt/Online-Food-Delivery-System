@echo off
echo ========== DIAGNOSTIC INFORMATION ==========
echo.

echo 1. Java version:
java -version
echo.

echo 2. Current directory:
cd
echo.

echo 3. Files in current directory:
dir /B
echo.

echo 4. Checking Main.java:
if exist "Main.java" (
    echo Main.java exists
    echo First line: 
    type Main.java | findstr /B "package"
) else (
    echo ❌ Main.java not found!
)
echo.

echo 5. Checking package structure:
if exist "Database\" echo Database/ exists
if exist "Login\" echo Login/ exists
if exist "Order\" echo Order/ exists
if exist "FoodItem\" echo FoodItem/ exists
echo.

echo 6. Checking lib folder:
if exist "lib\" (
    echo lib/ exists
    dir /B lib\
) else (
    echo ❌ lib/ not found!
)
echo.

echo 7. Compilation test:
javac -version
echo.

pause
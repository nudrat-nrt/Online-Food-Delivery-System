@echo off
echo Compiling with package structure...

REM Compile Main.java first
javac -cp ".;lib\*" -d . Main.java

REM Then compile packages
javac -cp ".;lib\*" -d . Database/*.java
javac -cp ".;lib\*" -d . Login/*.java
javac -cp ".;lib\*" -d . Order/*.java
javac -cp ".;lib\*" -d . FoodItem/*.java
javac -cp ".;lib\*" -d . Cart/*.java
javac -cp ".;lib\*" -d . Menu/*.java
javac -cp ".;lib\*" -d . PlaceOrder/*.java
javac -cp ".;lib\*" -d . User/*.java

echo Running...
java -cp ".;lib\*" Main
pause

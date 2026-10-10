@echo off
echo ===================================================
echo   Building NextGen LMS (Java Desktop Application)
echo ===================================================

if not exist bin mkdir bin

echo Compiling Java source files...
javac -cp "lib/*" -d bin src\com\lms\*.java src\com\lms\model\*.java src\com\lms\db\*.java src\com\lms\service\interfaces\*.java src\com\lms\exception\*.java src\com\lms\concurrent\*.java src\com\lms\util\*.java src\com\lms\ui\*.java src\com\lms\ui\common\*.java src\com\lms\ui\admin\*.java src\com\lms\ui\instructor\*.java src\com\lms\ui\student\*.java src\com\lms\web\*.java src\com\lms\test\*.java

if %ERRORLEVEL% equ 0 (
    echo [SUCCESS] Compilation successful! Binaries placed in /bin.
) else (
    echo [ERROR] Compilation failed.
)
pause

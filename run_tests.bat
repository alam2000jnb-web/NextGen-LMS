@echo off
echo ===================================================
echo   Running LMS Rubric Verification Test Suite
echo ===================================================

java -cp "bin;lib/*" com.lms.test.LMSVerificationTest
pause

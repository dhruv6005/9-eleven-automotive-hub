@echo off
setlocal
set MAVEN_VERSION=3.9.11
if "%MAVEN_HOME%"=="" set MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%
if exist "%MAVEN_HOME%\bin\mvn.cmd" goto run
set TMP_DIR=%TEMP%\maven-bootstrap-%RANDOM%%RANDOM%
mkdir "%TMP_DIR%"
echo Downloading Maven %MAVEN_VERSION% (first run only)...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Invoke-WebRequest 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip' -OutFile '%TMP_DIR%\maven.zip'; Expand-Archive '%TMP_DIR%\maven.zip' -DestinationPath '%TMP_DIR%'"
if errorlevel 1 exit /b 1
if not exist "%USERPROFILE%\.m2\wrapper\dists" mkdir "%USERPROFILE%\.m2\wrapper\dists"
if exist "%MAVEN_HOME%" rmdir /s /q "%MAVEN_HOME%"
move "%TMP_DIR%\apache-maven-%MAVEN_VERSION%" "%MAVEN_HOME%" >nul
:run
call "%MAVEN_HOME%\bin\mvn.cmd" %*
exit /b %ERRORLEVEL%

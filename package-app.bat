@echo off
echo === Generando archivo JAR con Maven === 
call mvn clean package -DskipTests 
if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] La compilacion con Maven ha fallado. Revisa los errores anteriores.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo === Verificando WiX Toolset === 
candle --version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERROR] No se encuentra 'candle.exe'. Asegurate de que WiX Toolset v3.x este instalado y su carpeta 'bin' anadida al PATH.
    pause
    exit /b 1
)

echo === Creando el paquete ejecutable === 

:: 1. Crear una carpeta de entrada limpia para jpackage
if exist target\instalador-entrada rd /s /q target\instalador-entrada
mkdir target\instalador-entrada

:: 2. Buscar el JAR de Spring Boot (el que no es 'original-') y copiarlo
set "JAR_NAME="
for %%f in (target\*.jar) do (
    if not "%%~nf"=="original-%%~nf" (
        copy "%%f" "target\instalador-entrada\" > nul
        set JAR_NAME=%%~nxf
    ) 
)

if "%JAR_NAME%"=="" (
    echo [ERROR] No se pudo encontrar el archivo JAR en la carpeta target.
    pause
    exit /b 1
)

echo JAR detectado: %JAR_NAME% 

set "APP_NAME=PortalBascket"
set "VERSION=1.0.0"

call jpackage ^
  --type exe ^
  --dest "dist" ^
  --name "%APP_NAME%" ^
  --app-version "%VERSION%" ^
  --input "target\instalador-entrada" ^
  --main-jar "%JAR_NAME%" ^
  --win-shortcut ^
  --win-console ^
  --win-menu ^
  --win-dir-chooser ^
  --win-per-user-install ^
  --main-class org.springframework.boot.loader.JarLauncher ^
  --verbose ^
  --vendor "ClubBaloncesto" ^
  --description "Panel de Administracion del Club"

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] La creacion del instalador con jpackage ha fallado. Revisa los errores arriba.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo === Proceso finalizado. Revisa la carpeta /dist === 
pause
Write-Host "=== Generando archivo JAR con Maven ==="
& mvn clean package -DskipTests

Write-Host ""
Write-Host "=== Creando el paquete ejecutable ==="
# Ajusta el nombre del JAR segun lo que genere tu pom.xml (ej. pagebuilder-0.0.1-SNAPSHOT.jar)
$JAR_NAME = "page-builder-0.0.1-SNAPSHOT.jar"
$APP_NAME = "AdminBaloncesto"
$VERSION = "1.0.0"

& jpackage `
  --type exe `
  --dest dist `
  --name $APP_NAME `
  --app-version $VERSION `
  --input target `
  --main-jar $JAR_NAME `
  --win-shortcut `
  --win-menu `
  --vendor "ClubBaloncesto" `
  --description "Panel de Administracion del Club"

Write-Host ""
Write-Host "=== Proceso finalizado. Revisa la carpeta /dist ==="
Read-Host "Presiona Enter para continuar"
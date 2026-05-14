Write-Host "=== Generando archivo JAR con Maven ==="
& mvn clean package -DskipTests

Write-Host "" # Salto de línea
Write-Host "=== Creando el paquete ejecutable ===" # Mensaje de estado
# Ajusta el nombre del JAR según lo que genere tu pom.xml (ej. page-builder-0.0.1-SNAPSHOT.jar)
$NOMBRE_JAR = "page-builder-0.0.1-SNAPSHOT.jar" # Nombre del archivo JAR
$NOMBRE_APP = "AdminBaloncesto" # Nombre de la aplicación
$VERSION = "1.0.0" # Versión de la aplicación

& jpackage `
  --type exe ` # Tipo de instalador: ejecutable de Windows
  --dest dist ` # Directorio de destino para el instalador
  --name $NOMBRE_APP ` # Nombre de la aplicación
  --app-version $VERSION ` # Versión de la aplicación
  --input target ` # Directorio de entrada donde se encuentra el JAR
  --main-jar $NOMBRE_JAR ` # Archivo JAR principal
  --win-shortcut ` # Crea un acceso directo en el escritorio
  --win-menu ` # Crea una entrada en el menú de inicio
  --vendor "ClubBaloncesto" ` # Nombre del proveedor
  --description "Panel de Administracion del Club" # Descripción del instalador

Write-Host "" # Salto de línea
Write-Host "=== Proceso finalizado. Revisa la carpeta /dist ===" # Mensaje de finalización
Read-Host "Presiona Enter para continuar" # Pausa el script hasta que el usuario presione Enter
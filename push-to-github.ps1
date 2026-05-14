Write-Host "=== Preparando Commit y Push a GitHub ===" -ForegroundColor Cyan

# 1. Validar que existe un repositorio
if (-not (Test-Path .git)) { # Si no existe un repositorio Git
    Write-Host "Inicializando repositorio Git local..." -ForegroundColor Gray # Mensaje de inicialización
    git init # Inicializa un nuevo repositorio Git
}

# 1.5. Configurar Git LFS para archivos grandes (ej: .exe, .jar)
Write-Host "Configurando Git LFS para archivos pesados..." -ForegroundColor Gray
git lfs install
git lfs track "target/*.jar"
git lfs track "dist/*.exe"
git lfs track "dist/*.zip"
git lfs track "*.zip"
git add .gitattributes

# 2. Verificar si hay un repositorio remoto configurado
$REMOTO = git remote # Obtiene el nombre del repositorio remoto
if ([string]::IsNullOrWhiteSpace($REMOTO)) { # Si no hay un repositorio remoto configurado
    Write-Host "No hay un repositorio remoto configurado." -ForegroundColor Yellow # Mensaje de advertencia
    $urlRepositorio = Read-Host "Introduce la URL de tu repositorio en GitHub (ej: https://github.com/usuario/repo.git)" # Pide la URL del repositorio
    if (-not [string]::IsNullOrWhiteSpace($urlRepositorio)) { # Si se proporcionó una URL
        git remote add origin $urlRepositorio # Añade el repositorio remoto con el nombre 'origin'
        Write-Host "Remoto 'origin' añadido correctamente." -ForegroundColor Green # Mensaje de éxito
    } else {
        Write-Host "Error: No se puede continuar sin una URL remota." -ForegroundColor Red # Mensaje de error
        exit # Sale del script
    }
}

# 2. Obtener nombre de la rama actual
$RAMA = git branch --show-current # Obtiene el nombre de la rama actual
if ([string]::IsNullOrWhiteSpace($RAMA)) { # Si no se pudo determinar la rama actual
    git branch -M main # Renombra la rama actual a 'main' si no tiene nombre
    $RAMA = "main" # Establece la rama a 'main'
}

# 3. Añadir todos los cambios detectados
Write-Host "Añadiendo archivos..." # Mensaje de estado
git add . # Añade todos los archivos modificados y nuevos al área de preparación

# 4. Sincronizar y crear el commit
$mensaje = Read-Host "Mensaje del commit"
if ([string]::IsNullOrWhiteSpace($mensaje)) { $mensaje = "Actualización automática" }

Write-Host "Sincronizando con el servidor remoto..." -ForegroundColor Gray
git pull origin $RAMA --rebase # Descarga cambios previos para evitar errores de subida

git commit -m "$mensaje" # Crea un commit con el mensaje proporcionado

# 5. Subir cambios
Write-Host "Subiendo a GitHub..." -ForegroundColor Yellow # Mensaje de estado
git push -u origin $RAMA # Sube los cambios a la rama remota 'origin'

Write-Host "`n=== ¡Cambios subidos con éxito a la rama $RAMA! ===" -ForegroundColor Green # Mensaje de éxito
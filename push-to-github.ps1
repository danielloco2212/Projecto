Write-Host "=== Preparando Commit y Push a GitHub ===" -ForegroundColor Cyan

# 1. Validar que existe un repositorio
if (-not (Test-Path .git)) {
    Write-Host "Inicializando repositorio Git local..." -ForegroundColor Gray
    git init
}

# 2. Verificar si hay un repositorio remoto configurado
$REMOTE = git remote
if ([string]::IsNullOrWhiteSpace($REMOTE)) {
    Write-Host "No hay un repositorio remoto configurado." -ForegroundColor Yellow
    $repoUrl = Read-Host "Introduce la URL de tu repositorio en GitHub (ej: https://github.com/usuario/repo.git)"
    if (-not [string]::IsNullOrWhiteSpace($repoUrl)) {
        git remote add origin $repoUrl
        Write-Host "Remoto 'origin' añadido correctamente." -ForegroundColor Green
    } else {
        Write-Host "Error: No se puede continuar sin una URL remota." -ForegroundColor Red
        exit
    }
}

# 2. Obtener nombre de la rama actual
$BRANCH = git branch --show-current
if ([string]::IsNullOrWhiteSpace($BRANCH)) {
    git branch -M main
    $BRANCH = "main"
}

# 3. Añadir todos los cambios detectados
Write-Host "Añadiendo archivos..."
git add .

# 4. Crear el commit
$mensaje = Read-Host "Mensaje del commit"
if ([string]::IsNullOrWhiteSpace($mensaje)) { $mensaje = "Actualización automática" }
git commit -m "$mensaje"

# 5. Subir cambios
Write-Host "Subiendo a GitHub..." -ForegroundColor Yellow
git push -u origin $BRANCH

Write-Host "`n=== ¡Cambios subidos con éxito a la rama $BRANCH! ===" -ForegroundColor Green
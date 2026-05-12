Feature: Login de Administrador en la Aplicación de Escritorio
  Como administrador del club
  Quiero poder iniciar sesión en la aplicación de escritorio
  Para gestionar la información del club
 
  Scenario: Inicio de sesión exitoso con credenciales válidas
    Given que el administrador está en la pantalla de login
    When ingresa "admin" como usuario y "password" como contraseña
    And hace clic en el botón "Iniciar Sesión"
    Then debería ver la ventana principal de administración
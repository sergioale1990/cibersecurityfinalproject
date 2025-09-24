# cibersecurityfinalproject
Administrador de contraseñas usando algoritmos de encriptación, proyecto final de la materia de fundamentos de ciberseguridad

## PREREQUISITOS
Tener instalado los siguientes paquetes:

- Java17
- Gradle
- Git
- MySQL

## CREACION DE LA BASE DE DATOS
Ejecutar los siguientes comandos para la creacion de la base de datos y creacion de usuario con permisos de administrador
- CREATE DATABASE vault CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
- CREATE USER 'vault'@'%' IDENTIFIED BY 'vaultpass';
- GRANT ALL PRIVILEGES ON vault.* TO 'vault'@'%';
- FLUSH PRIVILEGES;

## INSTRUCCIONES PARA EJECUTAR
Ejecutar el siguiente comando
- .\gradlew bootJar --> esto generara el archivo jar para ejecutarlo
- java -jar .\app\build\libs\app-1.0.0.jar --> Ejecuta la aplicacion
- Creacion del master password


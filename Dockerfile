# Usa Java 21 para coincidir con la version configurada en Maven.
FROM eclipse-temurin:21-jdk

# Define el directorio de trabajo donde vivira la app dentro del contenedor.
WORKDIR /usrapp/bin

# Expone una variable de entorno para el puerto (el servidor puede usarla si se implementa).
ENV PORT=6000

# Copia todo el directorio de build de Maven para incluir clases y posibles dependencias.
COPY target /usrapp/bin/target

# Arranca la clase principal del microframework con clases compiladas y dependencias si existen.
CMD ["java","-cp","./target/classes:./target/dependency/*","org.main.MicroSpringBoot"]

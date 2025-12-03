#!/bin/bash

# Script para iniciar WATurnos API con memoria aumentada

echo "🚀 Iniciando WATurnos API con memoria aumentada..."
echo "   Memoria inicial (Xms): 1024MB"
echo "   Memoria máxima (Xmx): 4096MB"
echo ""

cd "$(dirname "$0")"

# Opción 1: Ejecutar con Maven (desarrollo)
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xms1024m -Xmx4096m -XX:+UseG1GC"

# Opción 2: Compilar y ejecutar JAR (producción)
# Descomentar las siguientes líneas si prefieres usar JAR:
# echo "📦 Compilando proyecto..."
# mvn clean package -DskipTests
# echo "▶️  Ejecutando JAR..."
# java -Xms512m -Xmx2048m -XX:+UseG1GC -jar target/waturnos-api-1.0.0.jar

#!/bin/bash

# Carrega as variáveis do arquivo .env
if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
else
    echo "[terraform] Erro: Arquivo .env não encontrado."
    exit 1
fi

# Verifica se o método foi passado como argumento
if [ -z "$1" ]; then
    echo "[terraform] Erro: Nenhum método especificado (plan, apply, etc.)."
    exit 1
fi

METHOD=$1
shift

PARAMS="$@"

terraform $METHOD $PARAMS \
-var "aws_region=$AWS_REGION" \
-var "db_hacka_username=$DB_HACKA_USERNAME" \
-var "db_hacka_password=$DB_HACKA_PASSWORD" \
-var "db_hacka_identifier=$DB_HACKA_IDENTIFIER" \
-var "db_hacka_name=$DB_HACKA_NAME" \
-var "db_hacka_port=$DB_HACKA_PORT" \
-var "dockerhub_username=$DOCKERHUB_USERNAME"
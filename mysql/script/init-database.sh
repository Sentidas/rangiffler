#!/bin/bash
set -eu

mysql_exec() { mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" -e "$1"; }

create_db() {
  local db="$1"
  echo "  Creating database '${db}'"
  mysql_exec "CREATE DATABASE IF NOT EXISTS \`${db}\`;"
}

if [ -n "${CREATE_DATABASES:-}" ]; then
  echo "Multiple database creation requested: ${CREATE_DATABASES}"
  for db in $(echo "${CREATE_DATABASES}" | tr ',' ' '); do
    create_db "$db"
  done
  echo "Multiple databases created"
else
  echo "CREATE_DATABASES is empty; nothing to create."
fi

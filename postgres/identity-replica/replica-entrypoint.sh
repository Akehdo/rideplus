#!/bin/sh
set -e

if [ ! -s "$PGDATA/PG_VERSION" ]; then
  rm -rf "$PGDATA"/*
  until pg_isready -h identity-db-primary -p 5432 -U replicator; do
    sleep 2
  done

  export PGPASSWORD=replica_pass
  pg_basebackup -h identity-db-primary -D "$PGDATA" -U replicator -Fp -Xs -P -R
  chmod 700 "$PGDATA"
fi

exec docker-entrypoint.sh postgres

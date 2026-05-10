#!/bin/bash
# Pre-flight check — run before docker-compose up to catch missing or placeholder secrets.
#
# Secrets that are absent or still set to "CHANGE_ME" will cause silent failures:
# Spring Boot starts but authentication breaks (JWT_SECRET), emails bounce (MAIL_PASSWORD),
# or databases reject connections (DB passwords). This script surfaces those problems before
# any container is created, when they are cheap to fix.
#
# Usage: ./scripts/validate-secrets.sh

set -e

# Load .env from the project root, stripping comment lines so xargs can parse key=value pairs.
ENV_FILE="$(dirname "$0")/../.env"

if [ ! -f "$ENV_FILE" ]; then
    echo "ERROR: .env not found. Copy .env.example to .env and fill in the values."
    exit 1
fi

export $(grep -v '^#' "$ENV_FILE" | xargs)

ERRORS=0

# Checks that a variable is set, non-empty, and not still the placeholder value.
# A placeholder left over from .env.example is as dangerous as a missing variable —
# it allows the application to start in an insecure or broken state.
require() {
    local name="$1"
    local value="${!name}"

    if [ -z "$value" ]; then
        echo "  MISSING : $name"
        ERRORS=$((ERRORS + 1))
    elif [ "$value" = "CHANGE_ME" ]; then
        echo "  CHANGE_ME : $name — replace the placeholder in .env"
        ERRORS=$((ERRORS + 1))
    fi
}

echo "Validating secrets..."

# JWT — both services must share the same secret or authentication breaks entirely.
require JWT_SECRET

# Databases — weak or missing passwords allow unauthenticated access to stored data.
require MONGO_DB_PASSWORD
require POSTGRESQL_DB_PASSWORD

# Mail — notification emails silently fail if SMTP credentials are wrong.
require MAIL_USERNAME
require MAIL_PASSWORD

# Twilio — WhatsApp notifications silently fail if these are missing.
require TWILIO_ACCOUNT_SID
require TWILIO_AUTH_TOKEN
require TWILIO_WHATSAPP_FROM

# nginx / Let's Encrypt — wrong domain causes certificate issuance to fail.
require DOMAIN_NAME
require CERTBOT_EMAIL

# Grafana — left as "admin" exposes the metrics dashboard to anyone who finds the port.
require GRAFANA_ADMIN_PASSWORD

if [ $ERRORS -gt 0 ]; then
    echo ""
    echo "Found $ERRORS secret(s) that need attention. Fix them in .env before continuing."
    exit 1
fi

echo "All secrets are set. Safe to run docker-compose up."

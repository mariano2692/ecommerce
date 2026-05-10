#!/bin/bash
# Bootstrap script — run this ONCE before starting docker-compose for the first time.
#
# The problem: nginx needs a certificate to start its HTTPS server block, but
# Let's Encrypt can only issue the certificate after nginx is already serving port 80.
# This script breaks that deadlock: creates a temporary self-signed certificate so
# nginx can start, uses the running nginx to complete the ACME challenge, then replaces
# the dummy cert with the real one and reloads nginx.
#
# Usage:
#   1. Set DOMAIN_NAME and CERTBOT_EMAIL in .env
#   2. Ensure the domain's DNS A record points to this server's public IP
#   3. chmod +x nginx/init-letsencrypt.sh && ./nginx/init-letsencrypt.sh
#   4. docker-compose up -d  (every subsequent start requires no extra steps)

# Exit immediately if any command fails. Without this, the script would continue
# past a failed openssl or docker-compose call and leave the system in a broken state.
set -e

# Load variables from .env so the caller doesn't need to export them manually.
# grep -v '^#' strips comment lines before xargs parses the key=value pairs.
if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
fi

if [ -z "$DOMAIN_NAME" ]; then
    echo "Error: DOMAIN_NAME is not set in .env"
    exit 1
fi

if [ -z "$CERTBOT_EMAIL" ]; then
    echo "Error: CERTBOT_EMAIL is not set in .env"
    exit 1
fi

CERT_PATH="./nginx/certbot/conf/live/$DOMAIN_NAME"

# Guard against re-running after a successful issuance. Repeating the bootstrap
# would delete a valid certificate and trigger an unnecessary renewal request.
# Let's Encrypt enforces rate limits (5 duplicate certificates per week per domain).
if [ -d "$CERT_PATH" ]; then
    echo "Certificate already exists at $CERT_PATH — skipping bootstrap."
    exit 0
fi

echo "### Creating temporary self-signed certificate for $DOMAIN_NAME ..."
mkdir -p "$CERT_PATH"

# -x509      : output a self-signed certificate rather than a CSR
# -nodes     : do not encrypt the private key (nginx reads it without a passphrase)
# -newkey    : generate a fresh RSA-4096 key pair alongside the certificate
# -days 1    : intentionally short-lived — this cert exists only to unblock nginx startup
#              and will be deleted before Let's Encrypt issues the real one
openssl req -x509 -nodes -newkey rsa:4096 -days 1 \
    -keyout "$CERT_PATH/privkey.pem" \
    -out    "$CERT_PATH/fullchain.pem" \
    -subj   "/CN=$DOMAIN_NAME"

echo "### Starting nginx with the temporary certificate ..."
# --force-recreate ensures nginx picks up the freshly created dummy cert even if a
# stale container from a previous attempt is still present.
docker-compose up --force-recreate -d nginx

echo "### Waiting for nginx to be ready ..."
# A fixed sleep is an approximation. nginx typically starts in under a second,
# but Docker networking and volume mounts can add a few seconds on slow hosts.
sleep 5

echo "### Removing temporary certificate ..."
# The entire live/ directory is removed so certbot creates the real certificate
# structure from scratch. Leaving the dummy files would cause certbot to treat
# this domain as already issued and skip the request.
rm -rf "./nginx/certbot/conf/live"

echo "### Requesting certificate from Let's Encrypt ..."
# --webroot instructs certbot to place the ACME challenge token in /var/www/certbot,
# which is the volume nginx is already serving at /.well-known/acme-challenge/.
# Let's Encrypt fetches that token over HTTP to confirm we control the domain.
# --agree-tos and --non-interactive are required for unattended (scriptable) operation.
docker-compose run --rm certbot certonly \
    --webroot \
    --webroot-path=/var/www/certbot \
    --email "$CERTBOT_EMAIL" \
    --agree-tos \
    --non-interactive \
    -d "$DOMAIN_NAME"

echo "### Reloading nginx with the real certificate ..."
# nginx -s reload applies the new certificate without dropping active connections,
# unlike a full container restart which would cause a brief outage.
docker-compose exec nginx nginx -s reload

echo "### Done. HTTPS is active for $DOMAIN_NAME."
echo "### Run 'docker-compose up -d' to start all services."

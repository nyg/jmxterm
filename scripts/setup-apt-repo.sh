#!/usr/bin/env bash
#
# Generate a GPG key for signing the jmxsh apt repository.
#
# Usage:
#   ./scripts/setup-apt-repo.sh
#
# This script will:
#   1. Generate a dedicated GPG key (no passphrase, for CI use)
#   2. Export the public key to docs/apt/gpg.asc
#   3. Export the private key and print instructions for adding it as a GitHub secret
#
set -euo pipefail

KEY_NAME="jmxsh apt repo"
KEY_EMAIL="nyg@users.noreply.github.com"
PUBLIC_KEY_PATH="docs/apt/gpg.asc"

# Use an isolated keyring so existing keys don't interfere
export GNUPGHOME=$(mktemp -d)
trap 'rm -rf "$GNUPGHOME"' EXIT

echo "==> Generating GPG key for apt repository signing..."

# Generate key (no passphrase, RSA 4096, no expiry)
gpg --batch --gen-key <<EOF
%no-protection
Key-Type: RSA
Key-Length: 4096
Subkey-Type: RSA
Subkey-Length: 4096
Name-Real: ${KEY_NAME}
Name-Email: ${KEY_EMAIL}
Expire-Date: 0
%commit
EOF

# Get the key fingerprint
FINGERPRINT=$(gpg --list-keys --with-colons "${KEY_EMAIL}" | awk -F: '/^fpr:/{print $10; exit}')
echo "==> Key fingerprint: ${FINGERPRINT}"

# Export public key
gpg --armor --export "${FINGERPRINT}" > "${PUBLIC_KEY_PATH}"
echo "==> Public key exported to ${PUBLIC_KEY_PATH}"

# Export private key to a temp file (outside GNUPGHOME so it survives cleanup)
PRIVATE_KEY_FILE=$(mktemp /tmp/jmxsh-apt-key.XXXXXX)
gpg --armor --export-secret-keys "${FINGERPRINT}" > "${PRIVATE_KEY_FILE}"
echo "==> Private key exported to ${PRIVATE_KEY_FILE}"

cat <<INSTRUCTIONS

================================================================================
  SETUP COMPLETE
================================================================================

  1. The public key has been written to: ${PUBLIC_KEY_PATH}
     Commit and push this file.

  2. Add the private key as a GitHub Actions secret:

     gh secret set APT_GPG_PRIVATE_KEY < "${PRIVATE_KEY_FILE}"

     Or manually paste the contents of ${PRIVATE_KEY_FILE} into:
     https://github.com/nyg/jmxsh/settings/secrets/actions

  3. After adding the secret, delete the private key file:

     rm -f "${PRIVATE_KEY_FILE}"

  4. The next release will automatically sign and publish the apt repository.

================================================================================
INSTRUCTIONS

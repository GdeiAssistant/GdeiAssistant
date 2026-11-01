#!/usr/bin/env bash
set -euo pipefail
platform="$(uname -s)-$(uname -m)"
case "$platform" in
  Linux-x86_64)
    target=linux_x64
    checksum=551f6fc83ea457d62a0d98237cbad105af8d557003051f41f3e7ca7b3f2470eb
    ;;
  Darwin-x86_64)
    target=darwin_x64
    checksum=dfe101a4db2255fc85120ac7f3d25e4342c3c20cf749f2c20a18081af1952709
    ;;
  Darwin-arm64)
    target=darwin_arm64
    checksum=b40ab0ae55c505963e365f271a8d3846efbc170aa17f2607f13df610a9aeb6a5
    ;;
  *) echo "Unsupported Gitleaks platform" >&2; exit 1 ;;
esac
archive="$(mktemp "${RUNNER_TEMP:?}/gitleaks.XXXXXX")"
trap 'rm -f "$archive"' EXIT
curl --fail --silent --show-error --location --retry 2 \
  "https://github.com/gitleaks/gitleaks/releases/download/v8.30.1/gitleaks_8.30.1_${target}.tar.gz" \
  --output "$archive"
if [[ "$platform" == Linux-* ]]; then
  echo "$checksum  $archive" | sha256sum --check --status
else
  echo "$checksum  $archive" | shasum -a 256 --check --status
fi
mkdir -p "$RUNNER_TEMP/gitleaks-bin"
tar -xzf "$archive" -C "$RUNNER_TEMP/gitleaks-bin" gitleaks

#!/usr/bin/env bash
set -euo pipefail

if [[ -d "$HOME/.nvm/versions/node" ]]; then
  mapfile -t node_bins < <(ls -1d "$HOME"/.nvm/versions/node/*/bin/node 2>/dev/null | sort -V)

  for ((i=${#node_bins[@]}-1; i>=0; i--)); do
    candidate="${node_bins[$i]}"
    if "$candidate" -e "process.exit(0)" >/dev/null 2>&1; then
      export PATH="$(dirname "$candidate"):$PATH"
      break
    fi
  done
fi

exec "$@"

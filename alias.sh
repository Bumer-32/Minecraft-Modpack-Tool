#!/usr/bin/env bash

# Run with: source ./alias.sh

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC2139
alias mmt="$SCRIPT_DIR/mmt.sh"

#!/usr/bin/env bash
# Prints the CHANGELOG.md section of one version, without its "# x.y.z" heading.
# Fails when the version has no section or the section is empty.
#
# Usage: extract-changelog.sh <version> [changelog file]
#   version: 1.6.0 or v1.6.0
set -euo pipefail

VERSION="${1:?usage: extract-changelog.sh <version> [changelog file]}"
VERSION="${VERSION#v}"
FILE="${2:-CHANGELOG.md}"

# Take the lines between "# <version>" and the next level-1 heading,
# then drop the blank lines around them. Older headings carry a date,
# like "# 1.0.0-dev8 (2025-09-27)", so only the first word is compared.
# awk reads to the end instead of exiting early, so tr never gets SIGPIPE.
SECTION=$(tr -d '\r' < "$FILE" | awk -v ver="$VERSION" '
  /^# / {
    heading = $2
    sub(/^v/, "", heading)
    if (found) done = 1
    found = !done && (heading == ver)
    next
  }
  found { print }
' | sed -e '/./,$!d' | sed -e ':a' -e '/^\n*$/{$d;N;ba' -e '}')

if [ -z "$SECTION" ]; then
  echo "No changelog for $VERSION in $FILE. Add a \"# $VERSION\" section." >&2
  exit 1
fi

printf '%s\n' "$SECTION"

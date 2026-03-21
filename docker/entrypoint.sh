#!/bin/bash
# ──────────────────────────────────────────────────────────────────────────────
# Docker Entrypoint — Selenium BDD Framework
# Accepts Maven goals and system properties as CMD arguments
# ──────────────────────────────────────────────────────────────────────────────

set -e

echo "──────────────────────────────────────────────────────"
echo " Selenium Java BDD Framework"
echo " Java:    $(java -version 2>&1 | head -1)"
echo " Maven:   $(mvn -v | head -1)"
echo " Chrome:  $(google-chrome --version)"
echo " Browser: ${BROWSER:-chrome}"
echo " Env:     ${ENV:-qa}"
echo " Tags:    ${TAGS:-@smoke}"
echo "──────────────────────────────────────────────────────"

# Run Maven with all passed arguments
exec mvn "$@" \
  -Dbrowser="${BROWSER:-chrome}" \
  -Denv="${ENV:-qa}" \
  -Dheadless="${HEADLESS:-true}" \
  -Dremote="${REMOTE:-false}" \
  -Dgrid.url="${GRID_URL:-http://localhost:4444/wd/hub}" \
  -Dmaven.test.failure.ignore=true \
  -q

EXIT_CODE=$?

echo "──────────────────────────────────────────────────────"
echo " Test execution complete. Exit code: ${EXIT_CODE}"
echo " Allure results: /app/target/allure-results"
echo "──────────────────────────────────────────────────────"

exit $EXIT_CODE

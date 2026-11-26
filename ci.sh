#!/usr/bin/env bash
# CI for this repo, run from its root by .github/workflows/ci.yml:
#   bash ci.sh build java
#   bash ci.sh format google-java-format
# Optional env (set in the workflow): SETUP
set -uo pipefail
mode=$1; shift
TMP=${RUNNER_TEMP:-$(mktemp -d)}
group() { [ -n "${GITHUB_ACTIONS:-}" ] && echo "::group::$1" || echo "== $1"; }
endgroup() { [ -n "${GITHUB_ACTIONS:-}" ] && echo "::endgroup::" || true; }
fail() { echo "${GITHUB_ACTIONS:+::error::}$1 failed"; status=1; }
# top-most folders holding one of the given marker files (nested ones are built by their parent)
tops() { find . \( -name node_modules -o -name build -o -name .git -o -name .venv \) -prune -o \( "$@" \) -print \
  | xargs -n1 dirname | sort -u | awk '{ for (i in top) if (index($0 "/", top[i] "/") == 1) next; top[NR] = $0; print }'; }
status=0

if [ -n "${SETUP:-}" ] && [ "$mode" = build ]; then RUNNER_TEMP=$TMP bash -ec "$SETUP" || exit 1; fi

for kind in "$@"; do case "$mode:$kind" in
build:java)
  for dir in $(tops -name settings.gradle -o -name settings.gradle.kts -o -name build.gradle -o -name build.gradle.kts -o -name pom.xml); do
    group "$dir"
    if [ -f "$dir/pom.xml" ]; then (cd "$dir" && mvn -B -q verify)
    elif [ -x "$dir/gradlew" ]; then (cd "$dir" && ./gradlew build)
    else (cd "$dir" && gradle build); fi || fail "$dir"
    endgroup
  done ;;
format:google-java-format)
  [ -f "$TMP/gjf.jar" ] || curl -sSL -o "$TMP/gjf.jar" https://github.com/google/google-java-format/releases/download/v2.0.0/google-java-format-2.0.0-all-deps.jar
  git ls-files -z '*.java' | xargs -0 -r java -jar "$TMP/gjf.jar" --aosp --skip-reflowing-long-strings --dry-run --set-exit-if-changed || fail google-java-format ;;
*) echo "unknown $mode kind: $kind" >&2; exit 2 ;;
esac; done
exit $status

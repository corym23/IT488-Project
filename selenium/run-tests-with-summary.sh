#!/usr/bin/env bash
# Run mvn test -q and print a compact summary: Failures, Errors, Skipped, runtime
set -o pipefail
ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"
start_ts=$(date +%s)
# Run mvn quiet
mvn -q test
mvn_exit=$?
end_ts=$(date +%s)
runtime=$((end_ts - start_ts))

# Aggregate results from surefire-reports XML files
failures=0
errors=0
skipped=0
total_time=0
for f in target/surefire-reports/*.xml; do
  [ -f "$f" ] || continue
  line=$(grep -m1 -o '<testsuite[^>]*>' "$f" || true)
  if [ -n "$line" ]; then
    fi=$(echo "$line" | sed -n 's/.*failures="\([0-9]*\)".*/\1/p')
    er=$(echo "$line" | sed -n 's/.*errors="\([0-9]*\)".*/\1/p')
    sk=$(echo "$line" | sed -n 's/.*skipped="\([0-9]*\)".*/\1/p')
    tm=$(echo "$line" | sed -n 's/.*time="\([0-9.]*\)".*/\1/p')
    fi=${fi:-0}; er=${er:-0}; sk=${sk:-0}; tm=${tm:-0}
    failures=$((failures + fi))
    errors=$((errors + er))
    skipped=$((skipped + sk))
    total_time=$(awk -v a="$total_time" -v b="$tm" 'BEGIN{printf "%.3f", a+b}')
  fi
done

# Fallback: parse TEST-*.txt files if XMLs are not present or empty
if [ "$failures" -eq 0 ] && [ "$errors" -eq 0 ] && [ "$skipped" -eq 0 ]; then
  for t in target/surefire-reports/TEST-*.txt target/surefire-reports/*.txt; do
    [ -f "$t" ] || continue
    line=$(grep -m1 "Tests run:" "$t" || true)
    if [ -n "$line" ]; then
      fi=$(echo "$line" | sed -n 's/.*Failures: *\([0-9]*\).*/\1/p')
      er=$(echo "$line" | sed -n 's/.*Errors: *\([0-9]*\).*/\1/p')
      sk=$(echo "$line" | sed -n 's/.*Skipped: *\([0-9]*\).*/\1/p')
      timeStr=$(echo "$line" | sed -n 's/.*Time elapsed: *\([0-9.]*\) sec.*/\1/p')
      fi=${fi:-0}; er=${er:-0}; sk=${sk:-0}; timeStr=${timeStr:-0}
      failures=$((failures + fi))
      errors=$((errors + er))
      skipped=$((skipped + sk))
      total_time=$(awk -v a="$total_time" -v b="$timeStr" 'BEGIN{printf "%.3f", a+b}')
    fi
  done
fi

# Print summary
printf "Failures: %d, Errors: %d, Skipped: %d, Runtime: %ds (tests time: %ss)\n" "$failures" "$errors" "$skipped" "$runtime" "$total_time"

exit $mvn_exit

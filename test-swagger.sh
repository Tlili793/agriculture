#!/bin/bash
for i in 1 2 3 4 5 6; do
  sleep 10
  code=$(curl -s -o /dev/null -w '%{http_code}' --max-time 5 http://localhost:8081/swagger-ui.html 2>/dev/null)
  echo "Attempt $i: HTTP $code"
  if [ "$code" = "302" ] || [ "$code" = "200" ]; then
    echo "SUCCESS - Swagger UI is accessible"
    # Also test the doc proxy routes
    for svc in refrigerator ordering reclamation logistics marketplace; do
      doc_code=$(curl -s -o /dev/null -w '%{http_code}' --max-time 5 "http://localhost:8081/services/$svc/v3/api-docs" 2>/dev/null)
      echo "  /services/$svc/v3/api-docs -> HTTP $doc_code"
    done
    exit 0
  fi
done
echo "FAILED - Gateway swagger not accessible after 60s"
exit 1


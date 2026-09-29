#!/usr/bin/env bash
# The google-services plugin fails the build without a config file. The values are fake and
# deliberately not key-shaped, so secret scanning has nothing to match.
set -euo pipefail

package_name=com.useinsider.ecommerce

for module in "$@"; do
  cat > "$module/google-services.json" <<JSON
{
  "project_info": {
    "project_number": "000000000000",
    "project_id": "ci-fake",
    "storage_bucket": "ci-fake.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:000000000000:android:0000000000000000",
        "android_client_info": {
          "package_name": "$package_name"
        }
      },
      "oauth_client": [],
      "api_key": [
        {
          "current_key": "ci-fake-api-key"
        }
      ],
      "services": {
        "appinvite_service": {
          "other_platform_oauth_client": []
        }
      }
    }
  ],
  "configuration_version": "1"
}
JSON
done

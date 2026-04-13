#!/bin/sh

mvn package

mkdir -p container_data
mkdir -p container_data/hytale-release
cd container_data
if [ ! -f hytale-release/release.zip ]; then
	curl https://downloader.hytale.com/hytale-downloader.zip -o downloader.zip || exit 1
	unzip -o downloader.zip || exit 1
	chmod +x hytale-downloader-linux-amd64
	rm -f hytale-downloader-windows-amd64.exe QUICKSTART.md
	mkdir -p hytale-release
	./hytale-downloader-linux-amd64 -download-path hytale-release/release.zip
	rm -f downloader.zip
fi

echo "Setting up tokens..."
cd hytale-release
if [ ! -f ./refresh_token ]; then
	DEVICE=$(curl -s -X POST "https://oauth.accounts.hytale.com/oauth2/device/auth" \
		-H "Content-Type: application/x-www-form-urlencoded" \
		-d "client_id=hytale-server" \
		-d "scope=openid offline auth:server" || exit 1)

	DEVICE_CODE=$(echo "$DEVICE" | jq -r '.device_code')
	VERIFY_URI=$(echo "$DEVICE" | jq -r '.verification_uri_complete')

	echo "Enter the following url in your browser: $VERIFY_URI"
	echo "Waiting for authorization..."

	while true; do
		sleep 5
		TOKEN=$(curl -s -X POST "https://oauth.accounts.hytale.com/oauth2/token" \
			-H "Content-Type: application/x-www-form-urlencoded" \
			-d "client_id=hytale-server" \
			-d "grant_type=urn:ietf:params:oauth:grant-type:device_code" \
			-d "device_code=$DEVICE_CODE" || exit 1)

		REFRESH_TOKEN=$(echo "$TOKEN" | jq -r '.refresh_token')

		if [ "$REFRESH_TOKEN" != "null" ] && [ -n "$REFRESH_TOKEN" ]; then
			echo "$REFRESH_TOKEN" > ./refresh_token
			echo "Login successful"
			break
		fi
	done
else
	REFRESH_TOKEN=$(cat ./refresh_token)
fi

AUTH=$(curl -s -X POST "https://oauth.accounts.hytale.com/oauth2/token" \
	-H "Content-Type: application/x-www-form-urlencoded" \
	-d "client_id=hytale-server" \
	-d "grant_type=refresh_token" \
	-d "refresh_token=$REFRESH_TOKEN" || exit 1)
NEW_REFRESH=$(echo "$AUTH" | jq -r '.refresh_token')
ACCESS_TOKEN=$(echo "$AUTH" | jq -r '.access_token')
PROFILE_UUID=$((curl -s "https://account-data.hytale.com/my-account/get-profiles" \
	-H "Authorization: Bearer $ACCESS_TOKEN" | jq -r '.profiles[0].uuid') || exit 1)
SESSION=$(curl -s -X POST "https://sessions.hytale.com/game-session/new" \
	-H "Authorization: Bearer $ACCESS_TOKEN" \
	-H "Content-Type: application/json" \
	-d "{\"uuid\": \"$PROFILE_UUID\"}" || exit 1)
export SESSION_TOKEN=$(echo "$SESSION" | jq -r '.sessionToken')
export IDENTITY_TOKEN=$(echo "$SESSION" | jq -r '.identityToken')

AUTH1=$(curl -s -X POST "https://oauth.accounts.hytale.com/oauth2/token" \
	-H "Content-Type: application/x-www-form-urlencoded" \
	-d "client_id=hytale-server" \
	-d "grant_type=refresh_token" \
	-d "refresh_token=$NEW_REFRESH" || exit 1)
NEW_REFRESH1=$(echo "$AUTH1" | jq -r '.refresh_token')
ACCESS_TOKEN1=$(echo "$AUTH1" | jq -r '.access_token')
PROFILE_UUID1=$((curl -s "https://account-data.hytale.com/my-account/get-profiles" \
	-H "Authorization: Bearer $ACCESS_TOKEN1" | jq -r '.profiles[0].uuid') || exit 1)
SESSION1=$(curl -s -X POST "https://sessions.hytale.com/game-session/new" \
	-H "Authorization: Bearer $ACCESS_TOKEN1" \
	-H "Content-Type: application/json" \
	-d "{\"uuid\": \"$PROFILE_UUID1\"}" || exit 1)
export SESSION_TOKEN1=$(echo "$SESSION1" | jq -r '.sessionToken')
export IDENTITY_TOKEN1=$(echo "$SESSION1" | jq -r '.identityToken')

if [ "$NEW_REFRESH1" != "null" ] && [ -n "$NEW_REFRESH1" ]; then
	echo "$NEW_REFRESH1" > ./refresh_token
fi

cd ../..
jar=$(ls target/DystellarCoreHytale-*.jar | grep -v original | head -1)
cp "$jar" container_data/hytale-release/DystellarCore.jar

export HYTALE_RELEASE_NAME="release.zip"

docker compose up -d --build

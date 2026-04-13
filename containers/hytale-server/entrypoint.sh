#!/bin/sh

mkdir -p "$BASEDIR"
sleep 1
cd "$BASEDIR"

if [ ! -d Server/ ]; then
	unzip -o "$HYTALE_RELEASE_PATH" -d .
fi
mkdir -p Server
mkdir -p Server/mods
cp "$JAR_PLUGIN_PATH" Server/mods/DystellarCore.jar
mkdir -p Server/mods/gg.dystellar_Core

cat > Server/mods/gg.dystellar_Core/setup.json << EOF
{
	"debug_mode": true,
	"server_name": "$SERVER_NAME",
	"scoreboard": true,
	"allow_banned_players": false,
	"resourcepack": false,
	"automated_messages": true,
	"automated_messages_rate": 240,
	"prevent_weather": true,
	"forced_weather": "Zone1_Sunny",
	"public_ip": "localhost",
	"host": "$BACKEND",
	"api": "http://$BACKEND",
	"websocket_endpoint": "ws://$BACKEND/api/core/create_ws",
	"api_key": "$API_KEY"
}
EOF
cd Server

exec java -Xmx$RAM_ALLOC -jar HytaleServer.jar --assets ../Assets.zip --session-token "$SESSION_TOKEN" --identity-token "$IDENTITY_TOKEN"

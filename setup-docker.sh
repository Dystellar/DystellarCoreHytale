#!/bin/sh

mvn clean package

mkdir -p container_data
cd container_data
if [ ! -d ./hytale-release ]; then
	curl https://downloader.hytale.com/hytale-downloader.zip -o downloader.zip || exit 1
	unzip -o downloader.zip || exit 1
	chmod +x hytale-downloader-linux-amd64
	rm -f hytale-downloader-windows-amd64.exe QUICKSTART.md
	mkdir -p hytale-release
	./hytale-downloader-linux-amd64 -download-path hytale-release/
fi

if [ ! -f ./hytale-release/auth.enc ]; then
	cd hytale-release
	echo "A temporary server will start, login using 'auth login browser/device', save it as Encrypted and then stop the server with 'stop'"
	sleep 5
	latest=$(ls *.zip | sort -V | tail -1)
	mkdir -p tmp
	unzip -o "$latest" -d tmp/ || exit 1
	cd tmp/
	chmod +x start.sh
	./start.sh
	cd ..

	if [ ! -f tmp/Server/auth.enc ]; then
		echo "Login failed or incorrect, auth.enc file not found"
		exit 1
	fi

	mv tmp/Server/auth.enc .
	rm -rf tmp/
fi
cd ..
jar=$(ls target/DystellarCoreHytale-*.jar | grep -v original | head -1)
cp "$jar" container_data/hytale-release/DystellarCore.jar

hytale_release=$(ls *.zip | sort -V | tail -1)

export HYTALE_RELEASE_NAME="$hytale_release"

docker compose up -d

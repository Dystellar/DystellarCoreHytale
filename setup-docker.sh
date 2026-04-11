#!/bin/sh

mvn clean package

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

if [ ! -f ./hytale-release/auth.enc ]; then
	cd hytale-release
	echo "A temporary server will start, login using 'auth login browser/device', save it as Encrypted and then stop the server with 'stop'"
	mkdir -p tmp
	unzip -o release.zip -d tmp/ || exit 1
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
	cd ..
fi
cd ..
jar=$(ls target/DystellarCoreHytale-*.jar | grep -v original | head -1)
cp "$jar" container_data/hytale-release/DystellarCore.jar

export HYTALE_RELEASE_NAME="release.zip"

docker compose up -d

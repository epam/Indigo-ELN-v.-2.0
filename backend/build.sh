#!/bin/bash

# install libs, if quarkus.native.container-build=false
#sudo apt-get install build-essential libz-dev zlib1g-dev

echo '##### Running tests'
./gradlew test
if [ $? != 0 ]; then
  powershell.exe -ExecutionPolicy Bypass -Command "New-BurntToastNotification -Text 'build.sh', 'Tests failed!'"
  exit 1
fi

echo '##### Building images'
./gradlew :eln:eln-lambda:build :reports:reports-lambda:build :signature:signature-lambda:build --info --profile

echo '##### Running build and integration tests'
./gradlew build ":integrationTests:quarkusIntTest" #--info 2>&1 >integrationTests.log
if [ $? != 0 ]; then
  powershell.exe -ExecutionPolicy Bypass -Command "New-BurntToastNotification -Text 'build.sh', 'Build/integration tests failed!'"
  exit 1
fi

powershell.exe -ExecutionPolicy Bypass -Command "New-BurntToastNotification -Text 'build.sh', 'Build completed'"

#!/bin/bash

./setup.sh && ./01_OracleJDK8.sh && sleep 10 && ./02_GraalEE.sh && sleep 10 && ./03_GraalCE.sh && sleep 10 && ./04_AdoptOpenJDK8Hotspot.sh && sleep 10 && ./05_AdoptOpenJDK8OpenJ9.sh && sleep 10 && ./06_OpenJDK11Hotspot.sh && sleep 10 && ./07_OpenJDK11Graal.sh && sleep 10 && ./08_GraalEEHotspot.sh

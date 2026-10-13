#!/bin/sh
cd "$(dirname "$0")"
exec java --add-modules jdk.httpserver --source 17 src/main/java/demo/DemoServer.java 8767 site

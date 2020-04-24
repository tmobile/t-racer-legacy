# Overview

This repo contains a simple docker-compose setup with:

1. Telegraf listening on 8186 as an InfluxDB ingress point
2. Telegraf shooting data collected both locally (CPU, RAM, etc) and via TCP 8186, as mentioned above
3. InfluxDB to house that data on
4. Grafana to visualize that data from Influx

# Usage

Presuming you have Docker (and Docker Compose) installed, simply type: 

`docker-compose up` (or `docker-compose up -d` if you don't want to see the logging)

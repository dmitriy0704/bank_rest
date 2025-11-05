
```shell
# Логи пушатся по адресу:
loki.write "local" {
    endpoint {
        url = "http://loki:3100/loki/api/v1/push"
    }
}

# Обнаружение докер контейнеров. Сканирует Docker через сокет. Обновление списка контейнеров через 5сек
discovery.docker "containers" {
  host             = "unix:///var/run/docker.sock" #Linux: unix:///home/dmitriy/.docker/desktop/docker.sock Mac:unix:///Users/dmitriyfolomkin/.docker/run/docker.sock
  refresh_interval = "5s"
}

# извлекает имя контейнара -> 'container'. 
# Добавляет статические метки: 'job=docker', 'host=hostname'. 
# Создает метку 'service' из имени контейнера. 
# Добавляет метку 'stream' (stdout/stderr)
discovery.relabel "containers" {
  targets = discovery.docker.containers.targets

  rule {
    source_labels = ["__meta_docker_container_name"]
    regex         = "/(.*)"
    target_label  = "container"
  }

  rule {
    target_label = "job"
    replacement  = "docker"
  }

  rule {
    target_label = "host"
    replacement  = constants.hostname
  }

  rule {
    source_labels = ["__meta_docker_container_name"]
    regex         = "/(.*)"
    target_label  = "service"
  }

  rule {
    source_labels = ["__meta_docker_container_log_stream"]
    target_label  = "stream"
  }
}

# Источник логов из контейнеров loki.source.docker "containers"{}. 
# Читает логи всех Docker - контейнеров. 
# Применяет правила relabel. 
# Отправляет логи в loki. 
loki.source.docker "containers" {
  host          = "unix:///var/run/docker.sock"
  targets       = discovery.docker.containers.targets
  relabel_rules = discovery.relabel.containers.rules
  forward_to    = [loki.write.local.receiver]
}


# Приемник OpenTelemetry(OTLP) otelcol.receiver.otlp "default"{...} 
# Слушает traces/metrics* на портах 4317(gRPC) и 4318(HTTP). 
# Трейсы перенаправляются в Tempo. Метрики -> в Prometheus 
otelcol.receiver.otlp "default" {
  grpc {
    endpoint = "0.0.0.0:4317"
  }
  http {
    endpoint = "0.0.0.0:4318"
  }
  output {
    traces = [otelcol.exporter.otlp.tempo.input]
    metrics = [otelcol.exporter.prometheus.default.input]
  }
}


# Экспортеры 
# Tempo ('otelcol.exporter.otlp "tempo"') -> 
# получает трейсы, работает без TLS(insecure = true)
otelcol.exporter.otlp "tempo" {
  client {
    endpoint = "tempo:4317"
    tls {
      insecure = true
    }
  }
}

# Принимает метрики
otelcol.exporter.prometheus "default" {
  forward_to = []
}


```
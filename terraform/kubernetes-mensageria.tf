# ConfigMap com as definitions
resource "kubernetes_config_map" "rabbitmq_definitions" {
  metadata {
    name = "rabbitmq-definitions"
  }

  data = {
    "definitions.json" = file("${path.module}/rabbitmq/definitions.json")
  }
}

# Deployment do RabbitMQ
resource "kubernetes_deployment" "messagequeue_deployment" {
  metadata {
    name = "messagequeue"
  }
  spec {
    replicas = 1
    selector {
      match_labels = {
        app = "messagequeue"
      }
    }
    template {
      metadata {
        labels = {
          app = "messagequeue"
        }
      }
      spec {
        container {
          name  = "messagequeue"
          image = "rabbitmq:4.0.5-management-alpine"

          port {
            container_port = 5672
          }
          port {
            container_port = 15672
          }

          env {
            name  = "RABBITMQ_DEFAULT_VHOST"
            value = "/"
          }
          env {
            name  = "RABBITMQ_DEFAULT_USER"
            value = "guest"
          }
          env {
            name  = "RABBITMQ_DEFAULT_PASS"
            value = "guest"
          }
          env {
            name  = "RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS"
            value = "-rabbitmq_management load_definitions \"/etc/rabbitmq/definitions.json\""
          }

          volume_mount {
            name       = "rabbitmq-definitions"
            mount_path = "/etc/rabbitmq/definitions.json"
            sub_path   = "definitions.json"
          }
        }

        volume {
          name = "rabbitmq-definitions"

          config_map {
            name = kubernetes_config_map.rabbitmq_definitions.metadata[0].name
          }
        }
      }
    }
  }
}

# Serviço do RabbitMQ
resource "kubernetes_service" "messagequeue_service" {
  metadata {
    name = "messagequeue"
  }
  spec {
    selector = {
      app = "messagequeue"
    }
    port {
      name        = "amqp"
      protocol    = "TCP"
      port        = 5672
      target_port = 5672
    }
    port {
      name        = "management"
      protocol    = "TCP"
      port        = 15672
      target_port = 15672
    }
  }
}
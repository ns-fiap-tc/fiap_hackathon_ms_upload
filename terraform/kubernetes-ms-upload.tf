resource "kubernetes_secret" "secrets-ms-upload" {
  metadata {
    name = "secrets-ms-upload"
  }

  type = "Opaque"

  data = {
    /*DB_HOST             = data.kubernetes_service.mongodb-service.metadata[0].name
    DB_PORT             = var.db_hacka_port
    DB_NAME             = var.db_hacka_name
    DB_USER             = var.db_hacka_username
    DB_PASS             = var.db_hacka_password*/

    MESSAGE_QUEUE_HOST   = kubernetes_service.messagequeue_service.metadata[0].name
    //NOTIFICACAO_SERVICE_HOST = data.kubernetes_service.service-ms-produto.metadata[0].name
  }

  lifecycle {
    prevent_destroy = false
  }
}

# MS UPLOAD 
resource "kubernetes_deployment" "deployment-ms-upload" {
  metadata {
    name      = "deployment-ms-upload"
    namespace = "default"
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        app = "deployment-ms-upload"
      }
    }

    template {
      metadata {
        labels = {
          app = "deployment-ms-upload"
        }
      }

      spec {
        toleration {
          key      = "key"
          operator = "Equal"
          value    = "value"
          effect   = "NoSchedule"
        }

        container {
          name  = "deployment-ms-upload-container"
          image = "${var.dockerhub_username}/fiap_hackathon_ms_upload:latest"

          resources {
            requests = {
              memory : "512Mi"
              cpu : "500m"
            }
            limits = {
              memory = "1Gi"
              cpu    = "1"
            }
          }

          env_from {
            secret_ref {
              name = kubernetes_secret.secrets-ms-upload.metadata[0].name
            }
          }

          port {
            container_port = "8080"
          }

          # Liveness Probe para verificar se a aplicação está "viva"
          liveness_probe {
            http_get {
              path = "/actuator/health" 
              port = 8080
            }
            initial_delay_seconds = 60 # Espera 120s antes da primeira verificação
            period_seconds        = 10  # Verifica a cada 10s
            timeout_seconds       = 5   # Considera falha se não responder em 5s
            failure_threshold     = 3   # Tenta 3 vezes antes de reiniciar o container
          }

          # Readiness Probe para verificar se a aplicação está pronta para receber tráfego
          readiness_probe {
            http_get {
              path = "/actuator/health"
              port = 8080
            }
            initial_delay_seconds = 60 # Espera 120s antes de marcar como "pronto"
            period_seconds        = 10
            timeout_seconds       = 5
            failure_threshold     = 3
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "service-ms-upload" {
  metadata {
    name      = "service-ms-upload"
    namespace = "default"
    annotations = {
      "service.beta.kubernetes.io/aws-load-balancer-type" : "nlb",
      "service.beta.kubernetes.io/aws-load-balancer-scheme" : "internal",
      "service.beta.kubernetes.io/aws-load-balancer-cross-zone-load-balancing-enabled" : "true"
    }
  }
  spec {
    selector = {
      app = "deployment-ms-upload"
    }
    port {
      port = "80"
      target_port = "8080"
    }
    type = "LoadBalancer"
  }
}

# Horizontal Pod Autoscaler (HPA)
resource "kubernetes_horizontal_pod_autoscaler_v2" "hpa-ms-upload" {
  metadata {
    name      = "hpa-ms-upload"
    namespace = "default"
  }

  spec {
    scale_target_ref {
      api_version = "apps/v1"
      kind        = "Deployment"
      name        = kubernetes_deployment.deployment-ms-upload.metadata[0].name
    }

    min_replicas = 1
    max_replicas = 5

    metric {
      type = "Resource"
      resource {
        name = "cpu"
        target {
          type                = "Utilization"
          average_utilization = 70 # Escala se o uso médio de CPU passar de 70% do "request"
        }
      }
    }
  }
}
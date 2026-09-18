variable "aws_region" {
  description = "Région AWS où déployer l'infrastructure"
  type        = string
  default     = "us-west-1"
}

variable "vpc_cidr" {
  description = "CIDR block du VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidr" {
  description = "CIDR du sous-réseau public (instance prod)"
  type        = string
  default     = "10.0.1.0/24"
}

variable "availability_zone" {
  description = "Zone de disponibilité"
  type        = string
  default     = "us-west-1a"
}

variable "instance_type" {
  description = "Type d'instance EC2 (t2.micro pour l'exam)"
  type        = string
  default     = "t3.micro"
}

variable "admin_ip" {
  description = "IP publique de l'administrateur autorisée en SSH (format CIDR, ex: 1.2.3.4/32)"
  type        = string
}

variable "key_pair_name" {
  description = "Nom de la paire de clés SSH sur AWS"
  type        = string
  default     = "exam-key"
}

variable "public_key_path" {
  description = "Chemin local vers la clé publique SSH"
  type        = string
  default     = "~/.ssh/id_rsa.pub"
}

variable "project_name" {
  description = "Préfixe utilisé pour nommer les ressources AWS"
  type        = string
  default     = "exam-prod"
}

variable "app_image" {
  description = "Image Docker Hub de l'application"
  type        = string
  default     = "VOTRE_USERNAME/tp-devops-exam:latest"
}

variable "domain_name" {
  description = "Nom de domaine principal pour accéder aux outils (ex: mondomaine.com)"
  type        = string
  default     = "mondomaine.com"
}

variable "acme_email" {
  description = "Email pour les certificats Let's Encrypt via Traefik"
  type        = string
  default     = "admin@mondomaine.com"
}

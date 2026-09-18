# =========================================================
# Infrastructure AWS - Exam DevOps (1 instance EC2 prod)
# 1 VPC, 1 subnet public, 1 EC2 t2.micro, 1 Security Group
# Tous les outils (Docker, Traefik, SonarQube, Prometheus,
# Grafana, App) tournent sur une seule instance.
# =========================================================

# --- AMI Amazon Linux 2023 (dernière version officielle) ---
data "aws_ami" "amazon_linux_2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# --- VPC ---
resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "${var.project_name}-vpc"
  }
}

# --- Internet Gateway ---
resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name = "${var.project_name}-igw"
  }
}

# --- Subnet public (instance prod) ---
resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.main.id
  cidr_block              = var.public_subnet_cidr
  availability_zone       = var.availability_zone
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.project_name}-public-subnet"
  }
}

# --- Route table publique -> IGW ---
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = {
    Name = "${var.project_name}-public-rt"
  }
}

resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}

# =========================================================
# Security Group - Instance Prod
# Ports ouverts : 22 (SSH admin), 80 (HTTP), 443 (HTTPS)
# 9000 (SonarQube), 9090 (Prometheus), 3000 (Grafana), 8080 (App)
# =========================================================
resource "aws_security_group" "prod_sg" {
  name        = "${var.project_name}-prod-sg"
  description = "SG instance Prod - HTTP/HTTPS/SSH + outils DevOps"
  vpc_id      = aws_vpc.main.id

  ingress {
    description = "SSH admin only"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.admin_ip]
  }

  ingress {
    description = "HTTP (Traefik)"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "HTTPS (Traefik TLS)"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "SonarQube"
    from_port   = 9000
    to_port     = 9000
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "Prometheus"
    from_port   = 9090
    to_port     = 9090
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "Grafana (metrics port 3000)"
    from_port   = 3000
    to_port     = 3000
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    description = "Application backend"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    description = "Allow all outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-prod-sg"
  }
}

# =========================================================
# Key Pair SSH
# =========================================================
resource "aws_key_pair" "admin" {
  key_name   = var.key_pair_name
  public_key = file(var.public_key_path)
}

# =========================================================
# Elastic IP - IP fixe pour pointer le DNS
# =========================================================
resource "aws_eip" "prod" {
  domain = "vpc"

  tags = {
    Name = "${var.project_name}-eip"
  }
}

resource "aws_eip_association" "prod" {
  instance_id   = aws_instance.prod.id
  allocation_id = aws_eip.prod.id
}

# =========================================================
# EC2 Instance - Prod (unique)
# Toute la stack tourne ici : Traefik, SonarQube,
# Prometheus, Grafana, Application
# =========================================================
resource "aws_instance" "prod" {
  ami                         = data.aws_ami.amazon_linux_2023.id
  instance_type               = var.instance_type
  subnet_id                   = aws_subnet.public.id
  vpc_security_group_ids      = [aws_security_group.prod_sg.id]
  key_name                    = aws_key_pair.admin.key_name
  associate_public_ip_address = true

  # Augmenter le stockage pour SonarQube + Grafana + Prometheus
  root_block_device {
    volume_size = 20
    volume_type = "gp3"
  }

  user_data = templatefile("${path.module}/user_data/prod_init.sh.tpl", {
    project_name = var.project_name
  })

  tags = {
    Name        = "${var.project_name}-ec2"
    Environment = "prod"
  }
}

output "prod_public_ip" {
  description = "IP publique Elastic de l'instance Prod (à pointer dans votre DNS)"
  value       = aws_eip.prod.public_ip
}

output "prod_instance_id" {
  description = "ID de l'instance EC2 prod"
  value       = aws_instance.prod.id
}

output "ssh_prod" {
  description = "Commande SSH pour se connecter à l'instance Prod"
  value       = "ssh -i ~/.ssh/id_rsa ec2-user@${aws_eip.prod.public_ip}"
}

output "ansible_inventory_hint" {
  description = "Copier cette IP dans ansible/inventory/hosts.ini"
  value       = "prod ansible_host=${aws_eip.prod.public_ip} ansible_user=ec2-user ansible_ssh_private_key_file=~/.ssh/id_rsa"
}

output "urls" {
  description = "URLs d'accès aux outils (après configuration DNS)"
  value = {
    grafana   = "https://grafana.${var.domain_name}"
    sonarqube = "https://sonarqube.${var.domain_name}"
    app       = "https://app.${var.domain_name}"
    prometheus = "http://${aws_eip.prod.public_ip}:9090"
  }
}

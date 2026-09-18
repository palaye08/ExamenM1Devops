#!/bin/bash
# =========================================================
# Script d'initialisation de l'instance Prod (Amazon Linux 2023)
# Exécuté une seule fois au premier démarrage par cloud-init.
# Ansible prend le relais pour la configuration complète.
# =========================================================
set -euo pipefail

# Mise à jour du système
dnf update -y

# Installation des prérequis minimaux (Ansible fera le reste)
dnf install -y python3 python3-pip curl wget git

# Activation du service cloud-init pour les logs
systemctl enable cloud-init-local.service || true

echo "Instance ${project_name} initialisée - Ansible prend le relais." \
  >> /var/log/cloud-init-output.log

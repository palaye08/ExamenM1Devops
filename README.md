# TP DevOps - Exam

Application Spring Boot déployée sur une instance EC2 AWS (environnement prod) avec la stack complète DevOps :
**Terraform** (infrastructure) · **Ansible** (configuration) · **Traefik** (reverse proxy + HTTPS) · **SonarQube** · **Prometheus** · **Grafana** · **GitHub Actions** (CI/CD)

---

## Architecture

```
                    Internet
                        │
                    ┌───▼────┐
                    │Traefik │  :80 / :443
                    └───┬────┘
          ┌─────────────┼─────────────┐
          ▼             ▼             ▼
   app.domaine.com  grafana.dom  sonarqube.dom
       :8080            :3000        :9000
                        │
                   Prometheus
                    (targets)
                   /    |    \
               App  NodeExp  Sonar
              :8080  :9100   :9000
```

**1 seule instance EC2 t2.micro** héberge tous les outils via Docker.

---

## Structure du projet

```
tp-devops-exam/
├── .github/
│   └── workflows/
│       └── ci-cd.yml          # Pipeline CI/CD GitHub Actions
├── terraform/
│   ├── main.tf                # Infrastructure AWS (VPC, EC2, SG, EIP)
│   ├── variables.tf           # Variables Terraform
│   ├── outputs.tf             # Sorties (IP publique, commande SSH...)
│   ├── provider.tf            # Provider AWS
│   ├── terraform.tfvars.example  # Exemple de variables (copier -> .tfvars)
│   └── user_data/
│       └── prod_init.sh.tpl   # Script cloud-init de l'instance
├── ansible/
│   ├── ansible.cfg            # Configuration Ansible
│   ├── site.yml               # Playbook principal
│   ├── inventory/
│   │   └── hosts.ini          # Inventaire (IP à remplir après terraform)
│   ├── group_vars/all/
│   │   ├── vars.yml           # Variables partagées
│   │   └── vault.yml          # Secrets (chiffrer avec ansible-vault !)
│   └── roles/
│       ├── common/            # Docker + dépendances système
│       ├── traefik/           # Reverse proxy + Let's Encrypt
│       ├── sonarqube/         # Analyse qualité du code
│       ├── monitoring/        # Prometheus + Grafana + Node Exporter
│       └── app/               # Déploiement de l'application
├── src/                       # Sources Spring Boot
├── Dockerfile                 # Build multi-stage Maven → JRE
├── pom.xml                    # Dépendances Maven (Actuator + Prometheus)
└── .gitignore
```

---

## Étape 1 – Pré-requis

- Compte AWS avec accès IAM (droits EC2, VPC)
- Clé SSH générée : `ssh-keygen -t rsa -b 4096 -f ~/.ssh/id_rsa`
- Terraform >= 1.5 installé
- Ansible >= 2.14 installé
- Un nom de domaine avec accès à la zone DNS
- Compte Docker Hub

---

## Étape 2 – Terraform : Créer l'instance EC2

```bash
cd terraform/

# Copier et remplir les variables
cp terraform.tfvars.example terraform.tfvars
# Éditer terraform.tfvars avec votre IP, votre domaine, etc.

# Initialiser Terraform
terraform init

# Vérifier le plan
terraform plan

# Créer l'infrastructure
terraform apply
```

**Récupérer l'IP publique :**
```bash
terraform output prod_public_ip
# Exemple : 52.3.4.5
```

---

## Étape 3 – Configuration DNS

Dans votre gestionnaire de domaine, créer ces enregistrements DNS :

| Type | Nom                    | Valeur        |
|------|------------------------|---------------|
| A    | `app.votredomaine.com`      | IP_EC2 |
| A    | `grafana.votredomaine.com`  | IP_EC2 |
| A    | `sonarqube.votredomaine.com`| IP_EC2 |

---

## Étape 4 – Ansible : Installer les outils

```bash
cd ansible/

# Remplir l'inventaire avec l'IP obtenue
# Éditer inventory/hosts.ini :
# prod_server ansible_host=52.3.4.5 ...

# Adapter les variables dans group_vars/all/vars.yml
# (domain_name, acme_email, app_image)

# Chiffrer le fichier de secrets
ansible-vault encrypt group_vars/all/vault.yml

# Tester la connexion
ansible prod -m ping

# Lancer le playbook complet (installe Docker, Traefik, SonarQube, Prometheus, Grafana + App)
ansible-playbook site.yml --ask-vault-pass
```

**Ansible installe dans l'ordre :**
1. 🐳 **Docker** + Docker Compose (rôle `common`)
2. 🔀 **Traefik** (rôle `traefik`) – port 80/443, dashboard port 8081
3. 🔍 **SonarQube** (rôle `sonarqube`) – port 9000
4. 📊 **Prometheus + Grafana** (rôle `monitoring`) – ports 9090, 3000
5. 🚀 **Application** (rôle `app`) – port 8080

---

## Étape 5 – Accès aux outils

| Outil      | Accès via domaine                     | Accès direct (IP)         |
|------------|---------------------------------------|---------------------------|
| **App**       | `https://app.votredomaine.com`     | `http://IP:8080`          |
| **Grafana**   | `https://grafana.votredomaine.com` | `http://IP:3000`          |
| **SonarQube** | `https://sonarqube.votredomaine.com` | `http://IP:9000`       |
| **Prometheus**| –                                   | `http://IP:9090`          |

### Identifiants par défaut

| Outil      | Login  | Mot de passe            |
|------------|--------|-------------------------|
| SonarQube  | admin  | admin (à changer !)     |
| Grafana    | admin  | Voir vault.yml          |

---

## Visualiser les métriques sur le port 3000 (Grafana)

1. Ouvrir `http://IP_EC2:3000` dans votre navigateur
2. Se connecter avec `admin` / mot de passe du vault
3. **Ajouter la datasource Prometheus :**
   - Aller dans ⚙️ Configuration → Data Sources → Add data source
   - Choisir **Prometheus**
   - URL : `http://prometheus:9090`
   - Cliquer **Save & Test**
4. **Importer un dashboard :**
   - ➕ Create → Import
   - Entrer l'ID **`1860`** (Node Exporter Full) → Load
   - Sélectionner la datasource Prometheus → Import
5. Vous verrez les métriques de votre instance EC2 en temps réel :
   - CPU, RAM, Disque, Réseau, I/O

---

## Étape 6 – CI/CD GitHub Actions

### Protéger la branche `main` (push direct bloqué)

Dans votre dépôt GitHub :
1. **Settings → Branches → Add branch protection rule**
2. Branch name pattern : `main`
3. Cocher :
   - ✅ **Require a pull request before merging**
   - ✅ **Require approvals** (1 reviewer)
   - ✅ **Do not allow bypassing the above settings**

### Configurer les secrets GitHub

**Settings → Secrets and variables → Actions → New repository secret :**

| Secret            | Description                                  |
|-------------------|----------------------------------------------|
| `DOCKER_USERNAME` | Votre username Docker Hub                    |
| `DOCKER_PASSWORD` | Token Docker Hub                             |
| `EC2_HOST`        | IP publique de l'instance EC2                |
| `EC2_USER`        | `ec2-user`                                   |
| `SSH_PRIVATE_KEY` | Contenu de `~/.ssh/id_rsa` (clé privée)     |
| `SONAR_TOKEN`     | Token SonarQube (généré dans SonarQube)      |
| `SONAR_HOST_URL`  | `https://sonarqube.votredomaine.com`         |

**Settings → Variables → Actions → New repository variable :**

| Variable      | Description             |
|---------------|-------------------------|
| `DOMAIN_NAME` | `votredomaine.com`      |

### Pipeline CI/CD (déclenchement sur push main)

```
Push sur main
     │
     ▼
[Job 1] Tests unitaires (Maven)
     │
     ▼
[Job 2] Analyse SonarQube
     │
     ▼
[Job 3] Build + Push image Docker Hub
     │
     ▼
[Job 4] Déploiement SSH sur EC2 Prod
```

---

## Commandes utiles

```bash
# Terraform
terraform output                          # Voir toutes les sorties
terraform destroy                         # Détruire l'infrastructure

# Ansible
ansible-playbook site.yml --ask-vault-pass --tags traefik   # Rejouer seulement Traefik
ansible-playbook site.yml --ask-vault-pass --tags monitoring # Rejouer monitoring

# Sur l'EC2 (SSH)
ssh -i ~/.ssh/id_rsa ec2-user@IP_EC2
docker ps                                 # Voir tous les conteneurs
docker logs grafana                       # Logs Grafana
docker logs prometheus                    # Logs Prometheus
docker logs sonarqube                     # Logs SonarQube
docker logs traefik                       # Logs Traefik
```

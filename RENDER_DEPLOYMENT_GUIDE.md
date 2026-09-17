# 🚀 Complete Render.com Deployment Guide for Drishti Backend

This guide outlines step-by-step instructions to deploy the **Drishti Spring Boot Backend**, **PostgreSQL**, and **Neo4j Graph Database** on **Render.com** for free.

---

## 🛠️ Step 1: Create Free PostgreSQL Database on Render

1. Log in to [Render.com Dashboard](https://dashboard.render.com).
2. Click **New +** $\rightarrow$ **PostgreSQL**.
3. Fill in the details:
   - **Name:** `drishti-postgres-db`
   - **Database:** `dristhi`
   - **User:** `dristhi_user`
   - **Region:** Choose nearest (e.g. Oregon / Singapore)
   - **Plan:** Free
4. Click **Create Database**.
5. Once created, copy the **Internal Database URL** or **External Database URL**.
   - Example: `postgres://dristhi_user:password123@drishti-postgres-db.render.com/dristhi`
   - Formatted JDBC URL for Spring Boot: `jdbc:postgresql://drishti-postgres-db.render.com:5432/dristhi?sslmode=require`

---

## 🕸️ Step 2: Create Free Neo4j Database on Neo4j AuraDB

1. Go to [Neo4j Aura Console](https://console.neo4j.io/).
2. Create a free **AuraDB Free** instance.
3. Save the downloaded credentials:
   - **URI:** `neo4j+s://xxxxx.databases.neo4j.io`
   - **Username:** `neo4j`
   - **Password:** `your-saved-password`

---

## 📦 Step 3: Push Code to GitHub

Make sure your repository has:
- `Dockerfile` (already added in root)
- `pom.xml`
- `src/`

Commit and push your changes to GitHub:
```bash
git add .
git commit -m "Add Dockerfile and cloud deployment configuration"
git push origin main
```

---

## 🌐 Step 4: Create Spring Boot Web Service on Render

1. On the Render Dashboard, click **New +** $\rightarrow$ **Web Service**.
2. Connect your GitHub repository: `Drishti`.
3. Configure settings:
   - **Name:** `drishti-backend`
   - **Region:** Same region as your database
   - **Branch:** `main`
   - **Runtime:** **Docker**
   - **Plan:** Free

---

## 🔑 Step 5: Configure Environment Variables in Render

In the **Environment** tab of your Render Web Service, add the following environment variables:

| Environment Variable | Value Example / Description |
| :--- | :--- |
| `PORT` | `8080` |
| `DB_URL` | `jdbc:postgresql://your-db-host:5432/dristhi?sslmode=require` |
| `DB_USERNAME` | `dristhi_user` |
| `DB_PASSWORD` | `your_postgres_password` |
| `NEO4J_URI` | `neo4j+s://xxxxx.databases.neo4j.io` |
| `NEO4J_USERNAME` | `neo4j` |
| `NEO4J_PASSWORD` | `your_aura_password` |
| `NLP_SERVICE_URL` | `https://your-nlp-service.onrender.com` *(or mock URL)* |
| `ML_SERVICE_URL` | `https://your-ml-service.onrender.com` *(or mock URL)* |
| `AGENT_SERVICE_URL` | `https://your-agent-service.onrender.com` *(or mock URL)* |

---

## 🔍 Step 6: Deploy & Verify Health Check

1. Click **Create Web Service**.
2. Render will automatically build the Docker container and start your Spring Boot application.
3. Once live, your backend will be accessible at:
   ```
   https://drishti-backend.onrender.com
   ```
4. Verify health status:
   ```
   https://drishti-backend.onrender.com/actuator/health
   ```
5. Test your endpoints directly or using your Postman collection!

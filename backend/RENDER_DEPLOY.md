# 🚀 Deploy to Render.com - Step by Step Guide

This document provides step-by-step instructions to deploy your Spring Boot backend to Render.com using Docker.

---

## 📋 Prerequisites

- [ ] GitHub account
- [ ] Render.com account (free tier works)
- [ ] Project pushed to GitHub with the `Dockerfile` in the `backend/` directory

---

## 🔧 Step 1: Push Project to GitHub

1. **Initialize Git** (if not already done):
   ```bash
   cd backend
   git init
   git add .
   git commit -m "Add Dockerfile for Render deployment"
   ```

2. **Create GitHub Repository**:
   - Go to [github.com](https://github.com)
   - Click "New Repository"
   - Name: `taskmanager-backend` (or your preferred name)
   - **Important**: Keep it public or ensure your GitHub plan includes private repos

3. **Push to GitHub**:
   ```bash
   git remote add origin https://github.com/YOUR_USERNAME/taskmanager-backend.git
   git branch -M main
   git push -u origin main
   ```

---

## 🎯 Step 2: Create Render Web Service

1. **Log in to Render**:
   - Go to [dashboard.render.com](https://dashboard.render.com)
   - Sign in with your GitHub account

2. **Create New Web Service**:
   - Click **"New +"** → **"Web Service"**

3. **Connect GitHub Repository**:
   - Find and select your repository (`taskmanager-backend`)
   - Click **"Connect"**

4. **Configure the Web Service**:

   | Setting | Value |
   |---------|-------|
   | **Name** | `taskmanager-backend` |
   | **Environment** | `Docker` |
   | **Branch** | `main` (or your branch) |
   | **Root Directory** | `backend` (where Dockerfile is located) |
   | **Build Command** | (leave empty - Dockerfile handles it) |
   | **Start Command** | (leave empty - Dockerfile handles it) |

5. **Select Plan**:
   - Choose **"Free"** for testing (auto-sleeps after 15 min)
   - Or **"Starter"** ($7/month) for always-on

6. **Advanced Settings**:
   - Click "Advanced" to expand
   - **Auto-Deploy**: `Yes`
   - **Pull Request Previews**: Optional

---

## 🔒 Step 3: Configure Environment Variables

In the Render dashboard, add these environment variables:

| Key | Value | Notes |
|-----|-------|-------|
| `PORT` | `10000` | Render assigns this dynamically, but set a default |
| `JWT_SECRET` | `a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6` | Generate your own for production: `openssl rand -base64 32` |
| `JWT_EXPIRATION` | `86400000` | 24 hours in milliseconds |
| `SPRING_PROFILES_ACTIVE` | `prod` | Optional: if you have prod profile |

### To add Environment Variables:
1. Scroll down to **"Environment Variables"** section
2. Click **"Add Environment Variable"**
3. Enter key and value
4. Repeat for all variables

---

## 🚀 Step 4: Deploy

1. Click **"Create Web Service"**
2. Wait for build to start (may take 2-5 minutes first time)
3. Watch logs for progress

### Expected Build Output:
```
==> Building Dockerfile...
==> Docker build started...
==> ... (compilation logs) ...
==> Containerizing...
==> Deploying...
```

---

## 🌐 Step 5: Access Your Application

Once deployed successfully:

1. **Find Your URL**:
   - Render provides a URL like: `https://taskmanager-backend.onrender.com`
   - Find it in the dashboard under "Your site"

2. **Test Endpoints**:
   - API Base URL: `https://taskmanager-backend.onrender.com`
   - Swagger UI: `https://taskmanager-backend.onrender.com/swagger-ui.html`
   - Health Check: `https://taskmanager-backend.onrender.com/actuator/health`

---

## ⚠️ Important Notes

### For H2 Database (Current Setup):
- The app uses in-memory H2 database
- **Data is lost on container restart** (Render restarts containers periodically)
- For production, add a PostgreSQL database on Render

### To Add PostgreSQL on Render:
1. **Create Database**:
   - Render Dashboard → "New +" → "PostgreSQL"
   - Name: `taskmanager-db`
   - Select free plan

2. **Get Connection String**:
   - Click on the database → "Info" tab
   - Copy "Internal Database URL"

3. **Update Environment Variables**:
   ```
   SPRING_DATASOURCE_URL=jdbc:postgresql://[HOST]:[PORT]/[DATABASE]
   SPRING_DATASOURCE_USERNAME=[USERNAME]
   SPRING_DATASOURCE_PASSWORD=[PASSWORD]
   ```

---

## 🔧 Troubleshooting

### Build Fails
- Check Dockerfile is in `backend/` directory
- Verify Root Directory is set to `backend`
- Ensure Java 17 compatibility

### Application Crashes
- Check logs in Render dashboard
- Verify environment variables are set
- Ensure PORT variable is configured

### Out of Memory
- Add to environment variables:
  ```
  JAVA_OPTS=-Xmx512m
  ```

### Slow First Request
- Free tier spins down after 15 min of inactivity
- First request after sleep takes ~30 seconds to wake up

---

## 📝 Quick Reference

| Item | Value |
|------|-------|
| Docker Image | eclipse-temurin:17 |
| Default Port | 10000 (Render assigns) |
| Health Check | `/actuator/health` |
| Swagger | `/swagger-ui.html` |

---

## ✅ Deployment Checklist

- [ ] Dockerfile exists in `backend/` directory
- [ ] Project pushed to GitHub
- [ ] Render account connected to GitHub
- [ ] Web Service created with Docker environment
- [ ] Root Directory set to `backend`
- [ ] Environment variables configured
- [ ] Deployment successful
- [ ] Application accessible via public URL

---

**Happy Coding!** 🎉

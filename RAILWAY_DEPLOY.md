# 🚂 Deploy en Railway - Guía Rápida

## Pasos Rápidos

### 1. Crear Proyecto en Railway
1. Ve a [railway.app](https://railway.app)
2. **"Start a New Project"** → **"Deploy from GitHub repo"**
3. Autoriza Railway (asegúrate de dar acceso a repositorios privados si tu repo es privado)
4. Selecciona tu repositorio

### 2. Crear Base de Datos
1. En Railway → **"+ New"** → **"Database"** → **"Add PostgreSQL"**
2. Railway creará la BD automáticamente

### 3. Configurar Variables de Entorno
En el servicio web → **Variables**, agrega:

```
SPRING_PROFILES_ACTIVE=production
JWT_SECRET_KEY=tu-clave-super-secreta-aqui
```

### 4. Conectar Base de Datos
1. En el servicio web → **Variables**
2. **"Add Reference"** → Selecciona la BD PostgreSQL
3. Railway inyectará automáticamente: `DATABASE_URL`, `PGUSER`, `PGPASSWORD`, etc.

### 5. Deploy
Railway hará deploy automático. Los logs están disponibles en tiempo real.

## Variables de Entorno Importantes

| Variable | Valor | Descripción |
|----------|-------|-------------|
| `SPRING_PROFILES_ACTIVE` | `production` | Activa el perfil de producción |
| `JWT_SECRET_KEY` | `tu-clave` | Clave secreta para JWT |
| `DATABASE_URL` | Auto | Inyectada automáticamente por Railway |
| `PGUSER` | Auto | Inyectada automáticamente |
| `PGPASSWORD` | Auto | Inyectada automáticamente |

## Troubleshooting

### Repositorio no aparece
- Asegúrate de dar acceso a repositorios privados al autorizar Railway
- Si está en GitHub Classroom, puede necesitar permisos de organización

### Error de conexión a BD
- Verifica que la BD esté conectada al servicio web
- Revisa que `SPRING_PROFILES_ACTIVE=production` esté configurado

### Build falla
- Revisa los logs en Railway
- Verifica que `pom.xml` esté correcto

## Archivos Necesarios

✅ `railway.json` - Configuración de Railway  
✅ `application-production.properties` - Configuración para producción  
✅ `pom.xml` - Debe tener driver de PostgreSQL (ya está agregado)

## Checklist

- [ ] Repositorio conectado en Railway
- [ ] Base de datos PostgreSQL creada
- [ ] Variables de entorno configuradas
- [ ] BD conectada al servicio web
- [ ] Deploy exitoso
- [ ] Health check funcionando: `/actuator/health`


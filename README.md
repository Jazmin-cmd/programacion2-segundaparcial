# 📱 ClientSyncApp

ClientSyncApp es una aplicación Android desarrollada en **Java** para la gestión de información de clientes, incluyendo captura de datos, fotos, archivos, registro de errores y sincronización periódica de logs.  

---

## 📝 Requerimientos Implementados

### 1️⃣ Formulario de carga de datos del cliente
- **Campos del formulario**:
  - CI (texto)  
  - Nombre completo (texto)  
  - Dirección (texto)  
  - Teléfono (texto)  
  - Captura de **3 fotos** usando la cámara: `fotoCasa1`, `fotoCasa2`, `fotoCasa3`  

- **Condiciones**:
  - Los datos se envían en **formato JSON**.  
  - Las fotos se envían como archivos **Multipart** junto con los datos JSON.  
  - Se usa **Retrofit** para enviar al endpoint de prueba: `https://webhook.site/`.  

📸 **Extras:** Los ImageViews permiten capturar fotos directamente desde la cámara y mostrarlas en la interfaz antes de enviar.

---

### 2️⃣ Carga múltiple de archivos del cliente
- **Campos del formulario**:
  - CI del cliente (texto)  
  - Selección múltiple de archivos desde el almacenamiento (documentos, videos, imágenes)  

- **Condiciones**:
  - Los archivos seleccionados se comprimen en un único archivo `.zip`.  
  - Se envían junto con el CI mediante **Retrofit Multipart** al endpoint de prueba: `https://webhook.site/`.

---

### 3️⃣ Registro y seguimiento de errores (Auditoría local)
- **Condiciones**:
  - Se utiliza **Room Database** para almacenamiento local.  
  - Entidad `LogApp` con tabla `logs_app`:
    - `id` (clave primaria autoincremental)  
    - `fechaHora`  
    - `descripcionError`  
    - `claseOrigen`  
  - Se registran todos los errores capturados mediante `try-catch` y eventos relevantes.  
  - Los logs pueden visualizarse en **Logcat** y se sincronizan con el servidor.

---

### 4️⃣ Tarea programada con WorkManager
- **Condiciones**:
  - Se implementa una tarea periódica usando **WorkManager** que se ejecuta **cada 5 minutos**.  
  - Al ejecutarse:
    1. Obtiene todos los registros de la tabla `logs_app`.  
    2. Envía los datos al servidor mediante **Retrofit** (`https://webhook.site/`).  
    3. Elimina los registros locales una vez confirmada la sincronización.  

🛠️ **Extras:**  
- Se agregaron **botones de prueba** en la interfaz para:
  - Insertar logs de prueba.  
  - Ejecutar manualmente el Worker y verificar la sincronización.  
- Los resultados pueden revisarse en **Logcat** y en el **webhook de prueba**.

---

### 🔧 Tecnologías
- Java (Android)  
- Retrofit + Gson  
- Room Database  
- WorkManager  
- Multipart File Upload  
- Camera API  

---

### 🚀 Flujo de la aplicación
1. Usuario completa formulario de cliente y captura 3 fotos.  
2. Los datos se envían a `webhook.site` vía JSON + Multipart.  
3. En otra pantalla, el usuario puede seleccionar múltiples archivos y enviarlos como `.zip`.  
4. La app registra errores locales en `logs_app`.  
5. Worker periódico sincroniza automáticamente los logs cada 5 minutos y los elimina tras el envío exitoso.  
6. Botones de prueba permiten generar logs y ejecutar manualmente la sincronización.

---

### 📌 Notas
- Endpoint de prueba: `https://webhook.site/`.  
- La app incluye manejo de permisos de cámara y almacenamiento.  
- Los logs se registran de forma local antes de sincronizar con el servidor.  

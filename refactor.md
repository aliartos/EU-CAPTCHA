Below is a concise summary of the steps to refactor your existing Spring Boot app into a multi‐module project so that its common functionality can be reused in other apps:

---

### 1. Create a Parent Project

• **Set Up a Root POM (or Settings File in Gradle):**  
 – Create a new parent project with packaging set to `pom` (or use a multi-module Gradle settings file).  
 – List your submodules (e.g., “library” and “application”) in the `<modules>` section.

---

### 2. Create the Library Module

• **Extract Common Code:**  
 – Move shared services, models, configuration classes, utilities, etc., from the original app into a new module (e.g., “common-library”).  
 – Remove the main class and any application-specific code (like embedded server configuration).

## • **Configure the Library Module:**  
 – Set the packaging to `jar` in its POM.  
 – Disable or remove the Spring Boot Maven (or Gradle) plugin that creates an executable “fat” JAR (for instance, by setting `<skip>true</skip>` in the plugin configuration).  
 – (Optional) Add auto-configuration support using a configuration class and a `META-INF/spring.factories` file so that its beans can be auto-detected by consuming apps.  
  

### 3. Create the Application Module

• **Retain Your Executable App:**  
 – Keep your original app (with the `@SpringBootApplication` main class) in its own module (e.g., “application”).  
 – Remove any duplicate implementations that now reside in the library module.

• **Add Dependency on the Library:**  
 – In the application module’s POM (or Gradle file), add a dependency on your library module so that the shared components are available at runtime.  
 – Adjust component scanning (if needed) to include the library’s package(s).

---

### 4. Build and Test the Multi-Module Project

• **Compile and Package:**  
 – Run the build from the parent project (using `mvn clean install` or the equivalent Gradle command) to produce both the library JAR and the executable application.  
• **Verify Integration:**  
 – Run the application module to ensure that it picks up the library’s beans and that functionality works as expected.  
 – You can now also use the library module as a dependency in other projects.
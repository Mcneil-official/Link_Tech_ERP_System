# Link Tech ERP

> Java-based ERP application (Spring Boot) — lightweight enterprise resource planning system.

## Overview
- **Project:** Link Tech ERP
- **Type:** Java (Spring Boot) Maven application
- **Main class:** `erp.link_tech_erp.LinkTechErpApplication` (configured in [pom.xml](pom.xml))

This repository contains the source for Link Tech's ERP application, packaged as a runnable fat JAR and prepared for optional Windows installer creation.

## Requirements
- Java JDK 21 or later
- Maven (or use the included Maven wrapper)

## Quick start

Build (Unix/macOS):

```bash
./mvnw clean package
```

Build (Windows PowerShell / CMD):

```powershell
mvnw.cmd clean package
```

Run (packaged JAR):

```bash
java -jar target/link_tech_erp-<version>-all.jar
```

Run via Spring Boot plugin (development):

```bash
./mvnw spring-boot:run
```

## Configuration
- Application properties: [src/main/resources/application.properties](src/main/resources/application.properties)
- Database: PostgreSQL dependency is declared in [pom.xml](pom.xml); sample Supabase schemas are included:
  - [supabase_hrm_schema.sql](supabase_hrm_schema.sql)
  - [supabase_inventory_schema.sql](supabase_inventory_schema.sql)
  - [supabase_sales_schema.sql](supabase_sales_schema.sql)

## Packaging & Installer
- The project uses the Maven Shade plugin to create an "all" (fat) JAR. See the shading configuration in [pom.xml](pom.xml).
- A Maven profile `windows-exe` is provided to run `jpackage` and produce a Windows installer (requires JDK with `jpackage`).
- A portable JRE is included under [Link_Tech_ERP_Release/jre](Link_Tech_ERP_Release/jre) for offline releases.

## Development notes
- Java version is set to 21 in [pom.xml](pom.xml).
- Lombok is used as an optional dependency (annotation processing enabled in the Maven compiler plugin).
- OpenAPI UI is available via `springdoc-openapi` (see dependencies in [pom.xml](pom.xml)).

## Module features
The codebase is organized by feature modules under `src/main/java/erp/link_tech_erp`.

- **HRM (Human Resources)**: employee management (add/view/update/delete), record search, login and dashboards. UI classes are in [src/main/java/erp/link_tech_erp/hrm](src/main/java/erp/link_tech_erp/hrm).
- **Inventory**: product catalog, suppliers, purchase orders, analytics dashboard, table models and Supabase integration. Key UI and client code is in [src/main/java/erp/link_tech_erp/inventory](src/main/java/erp/link_tech_erp/inventory).
- **Sales**: sales order models, repositories, and Supabase REST client for orders. See [src/main/java/erp/link_tech_erp/sales](src/main/java/erp/link_tech_erp/sales).
- **Finance**: financial records, record dialogs, dashboard and services for ledger/transactions. UI and services live in [src/main/java/erp/link_tech_erp/finance](src/main/java/erp/link_tech_erp/finance).
- **Integration / Orchestration**: module launchers, desktop launcher, global login, and cross-module sync/orchestration services (e.g., sales→inventory, HRM→finance). See [src/main/java/erp/link_tech_erp/integration](src/main/java/erp/link_tech_erp/integration).

## UI framework
- The desktop UI is implemented using Java Swing. Look-and-feel is enhanced with `flatlaf` (declared in `pom.xml`) to provide a modern appearance.
- Desktop entry points and frames are under the modules' `ui` packages and `integration` launchers (examples: [src/main/java/erp/link_tech_erp/integration/ErpDesktopLauncher.java](src/main/java/erp/link_tech_erp/integration/ErpDesktopLauncher.java), [src/main/java/erp/link_tech_erp/inventory/MainFrame.java](src/main/java/erp/link_tech_erp/inventory/MainFrame.java)).


## Running tests

Use the Maven wrapper to run tests:

```bash
./mvnw test
```

## Contributing
- Fork the repository, create a feature branch, and open a pull request. Keep changes focused and add tests for new functionality.

## License
No license specified in this repository. Add a `LICENSE` file to declare terms.

## Important files
- [pom.xml](pom.xml)
- [src/main/java](src/main/java)
- [src/main/resources/application.properties](src/main/resources/application.properties)
- [supabase_hrm_schema.sql](supabase_hrm_schema.sql)
- [supabase_inventory_schema.sql](supabase_inventory_schema.sql)
- [supabase_sales_schema.sql](supabase_sales_schema.sql)

---

If you'd like, I can:
- add a minimal contribution guideline or `CONTRIBUTING.md`,
- create a `LICENSE` file,
- or add CI to build and run tests automatically.

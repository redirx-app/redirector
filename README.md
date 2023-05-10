# RDRX Redirectory: Vanity URL Shortening Service
The "ReDiRX" Redirectory service allows users to define custom short links to whatever web resources they want, then access them 
through the (ideally shorter) hostname of this service. For example...
- Assume that the service is running at `https://go/`. 

This service also provides search functionality, and the ability to tag redirects

# Project Components

There are two major pieces that constitute the services that this project provides:
- A Data & Redirect service, written in Java/Spring Boot (see `src/main/java/app/rdrx`), and 
- A UI/SPA written in Angular (see `src/main/web`)

## Data & Redirect Service (Java Spring Boot)
The Spring Boot service provides 3 core services:
1. A REST API to allow the management of redirect/reference records, hosted at `api/`
2. Static file delivery for the SPA built in Angular, hosted at `app/`
3. A Redirect service, which consumes (almost) every other url and attempts to redirect.

This is the list of URLs that are not processed by the redirect service: 
- `/favicon.ico`
- `/api/**`
- `/app/**`
- `/new`

These exceptions are explicitly called out in `RedirectController.java:52` to prevent any possible overlap, but here's the binding for the `redirectResponse(...)`, for reference:
```java
@GetMapping(value = {
    "/{path:(?!(?:new|app|api|favicon\\.ico)$).*}",
    "/{path:(?!(?:new|app|api|favicon\\.ico)$).*}/**",
})
```
The means of this mapping means that anything not explicitly listed here will be processed as a shortlink. If further functionality is needed that requires further reserved urls, be sure to add them here as well as on the controller binding.

### Hosting the Angular Static Files:
The angular build process has been crafted to output everything into the `resources` directory within the `target` build directory, which is configured to be hosted by Spring Boot at `/app/static` within the `application.properties` file. Maven is configured to build and package the angular project in the `test` and `prod` profiles.

## The UI: an AngularJS SPA
Within the UI that is served to interact with this app at `/app/` on the data service, there are 3 views that constitute the app:
- The Home Page/Search page (found at `src/app/search`)
  - This page is also served when the application root is requested (i.e. `https://go/`)
- The new registration page (found at `src/app/new`)
  - This page is also the target of the redirect whenever a user requests a page that has not been registered yet.
- The Manage Redirect page (found at `src/app/manage`)
  - When a user needs to modify a redirect or its metadata, this will be where that happens.

# Building the project

This project utilizes Maven to build the runnable jar file that contains the Spring Boot data service. 
There are several profiles defined in the `pom.xml`
- dev
    - This builds with the minimal amount of steps, and does not update the node package build.
    - This profile runs the data service in dev mode, which means error messages are passed through to the UI
- test-no-ui-build
    - This builds/runs the service in test mode (for spring boot), and does not build the node package in the `src/web/` directory.
- test
    - This builds/runs the service in test mode, and builds the node project for packaging into the runnable JAR.
- prod
    - This builds/runs the service in prod mode, and builds the node project for packaging into the runnable JAR.

Being built in Spring-Boot, this project can leverage the [Spring Boot Maven Goals](https://docs.spring.io/spring-boot/docs/2.6.3/maven-plugin/reference/htmlsingle/#goals) to build, compile, and run the project through the data service.

This means that building this app for production should pretty much just mean calling `./mvnw clean package` to 
build a production jar file.

## Running Locally

To avoid a port conflict, the data service runs on port 8080 while running in dev mode.
This is to leave port 80 available for the Angular UI dev server.

While running in split mode (with the separate UI dev server on `ng serve`), the UI server proxies local data requests
destined for the data service, using the filtering function defined in `src/main/web/.config/proxy.dev.js`.
The rest of the Angular dev server configuration is viewable in `src/main/web/angular.json` around line 74 (look for object `projects`->`go-ui`->`architect`->`serve`).


### Running Data Service in dev mode (no UI build/packaging)
Run the data service using the Maven Wrapper, using the `dev` profile.
```
./mvnw spring-boot:run -P dev
```

### Running Angular Dev Server(separate from data service)
Run the dev server with the Angular CLI `ng serve`. This is configured by `angular.json`.
```
cd src/main/web
ng serve
```

If you're on MacOS or Linux, you will likely need to use `sudo` to host the UI service on port 80.

```
sudo ng serve
```
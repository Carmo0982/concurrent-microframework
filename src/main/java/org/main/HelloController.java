package org.main;

import java.time.LocalDateTime;

/**
 * POJO de ejemplo - Bean cargado por el framework IoC MicroSpringBoot.
 * Sus metodos anotados con @GetMapping se registran como rutas HTTP via reflexion.
 */
@RestController
public class HelloController {

    private int visitCount = 0;

    @GetMapping("/")
    public String index() {
        visitCount++;
        String body = ""
                + "<section class='hero'>"
                + "<h1>MicroSpringBoot</h1>"
                + "<p>Microframework Java con enrutamiento por reflexion e IoC.</p>"
                + "<span class='badge'>Visitas: " + visitCount + "</span>"
                + "</section>"
                + "<section class='grid'>"
                + "<a class='card' href='/hello'><h3>Hello</h3><p>Respuesta basica del controlador.</p></a>"
                + "<a class='card' href='/pi'><h3>PI</h3><p>Consulta el valor de Math.PI.</p></a>"
                + "<a class='card' href='/time'><h3>Hora</h3><p>Fecha y hora actual del servidor.</p></a>"
                + "<a class='card' href='/greet'><h3>Saludo</h3><p>Formulario para saludar por nombre.</p></a>"
                + "<a class='card' href='/greeting'><h3>Greeting</h3><p>Segundo controlador de ejemplo.</p></a>"
                + "<a class='card' href='/app.html'><h3>app.html</h3><p>Contenido estatico desde resources/public.</p></a>"
                + "</section>";
        return renderPage("Inicio", body);
    }

    @GetMapping("/hello")
    public String hello() {
        String body = ""
                + "<h1>Hello World!</h1>"
                + "<p>Respuesta generada por el POJO HelloController via reflexion.</p>"
                + "<a class='button secondary' href='/'>Volver al inicio</a>";
        return renderPage("Hello", body);
    }

    @GetMapping("/pi")
    public String pi() {
        String body = ""
                + "<h1>Valor de PI</h1>"
                + "<p class='metric'>" + Math.PI + "</p>"
                + "<a class='button secondary' href='/'>Volver al inicio</a>";
        return renderPage("PI", body);
    }

    @GetMapping("/time")
    public String time() {
        String body = ""
                + "<h1>Hora del servidor</h1>"
                + "<p class='metric'>" + LocalDateTime.now() + "</p>"
                + "<a class='button secondary' href='/'>Volver al inicio</a>";
        return renderPage("Hora", body);
    }

    @GetMapping("/greet")
    public String greet(@RequestParam(value = "name", defaultValue = "Mundo") String name) {
        String normalizedName = normalizeName(name);
        String safeName = escapeHtml(normalizedName);

        String body = ""
                + "<h1>Saludo personalizado</h1>"
                + "<p>Ingresa tu nombre para generar un saludo mas profesional.</p>"
                + "<form class='greet-form' method='get' action='/greet'>"
                + "<label for='name'>Nombre</label>"
                + "<input id='name' name='name' type='text' placeholder='Ej: Juan Perez' value='" + safeName + "' maxlength='80' required>"
                + "<button class='button' type='submit'>Saludar</button>"
                + "</form>"
                + "<div class='result'><strong>Hola, " + safeName + "!</strong></div>"
                + "<a class='button secondary' href='/'>Volver al inicio</a>";

        return renderPage("Greet", body);
    }

    private String renderPage(String title, String body) {
        return "<!doctype html><html lang='es'><head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>" + title + " - MicroSpringBoot</title>"
                + "<link rel='stylesheet' href='/styles.css'>"
                + "</head><body><main class='container'>"
                + body
                + "</main></body></html>";
    }

    private String normalizeName(String name) {
        if (name == null) {
            return "Mundo";
        }
        String trimmed = name.trim();
        return trimmed.isEmpty() ? "Mundo" : trimmed;
    }

    private String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}


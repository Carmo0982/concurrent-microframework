package org.main;

/**
 * Componente de ejemplo que demuestra el uso de @RequestParam con defaultValue.
 * El framework IoC (MicroSpringBoot) lo detecta automaticamente gracias a @RestController.
 */
@RestController
public class GreetingController {

    @GetMapping("/greeting")
    public String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        String safeName = escapeHtml(normalizeName(name));
        return "<!doctype html><html lang='es'><head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Greeting - MicroSpringBoot</title>"
                + "<link rel='stylesheet' href='/styles.css'>"
                + "</head><body><main class='container'>"
                + "<h1>GreetingController</h1>"
                + "<p>Este saludo proviene de un controlador separado.</p>"
                + "<div class='result'><strong>Hola " + safeName + "</strong></div>"
                + "<a class='button secondary' href='/'>Volver al inicio</a>"
                + "</main></body></html>";
    }

    private String normalizeName(String name) {
        if (name == null) {
            return "World";
        }
        String trimmed = name.trim();
        return trimmed.isEmpty() ? "World" : trimmed;
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


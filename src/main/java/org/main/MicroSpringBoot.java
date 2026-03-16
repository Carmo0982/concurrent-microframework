package org.main;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MicroSpringBoot - Framework IoC que escanea el classpath buscando clases
 * anotadas con @RestController y registra sus metodos @GetMapping automaticamente.
 *
 * Uso (sin argumentos - escaneo automatico):
 *   java -cp target/classes org.main.MicroSpringBoot
 *
 * Uso (con clase especifica - compatibilidad hacia atras):
 *   java -cp target/classes org.main.MicroSpringBoot org.main.HelloController
 */
public class MicroSpringBoot {

    private static final Map<String, Method> routeMap = new HashMap<>();
    private static final Map<Class<?>, Object> beanInstances = new HashMap<>();

    public static void main(String[] args) throws Exception {
        System.out.println("=== MicroSpringBoot ===");

        List<Class<?>> controllers;

        if (args.length >= 1) {
            controllers = new ArrayList<>();
            for (String className : args) {
                controllers.add(Class.forName(className));
            }
        } else {
            controllers = scanClasspath();
        }

        if (controllers.isEmpty()) {
            System.err.println("No se encontraron clases con @RestController en el classpath.");
            System.exit(1);
        }

        for (Class<?> clazz : controllers) {
            if (!clazz.isAnnotationPresent(RestController.class)) {
                System.out.println("[IGNORADO] " + clazz.getName() + " - no tiene @RestController");
                continue;
            }

            System.out.println("Cargando componente: " + clazz.getName());
            Object instance = clazz.getDeclaredConstructor().newInstance();
            beanInstances.put(clazz, instance);

            for (Method m : clazz.getDeclaredMethods()) {
                if (m.isAnnotationPresent(GetMapping.class)) {
                    if (!m.getReturnType().equals(String.class)) {
                        System.out.println("  [IGNORADO] " + m.getName() + "() - tipo de retorno no es String");
                        continue;
                    }
                    String path = m.getAnnotation(GetMapping.class).value();
                    routeMap.put(path, m);
                    System.out.println("  GET " + path + " -> " + m.getName() + "()");
                }
            }
        }

        HttpServer server = new HttpServer(35000, routeMap, beanInstances);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (server.isRunning()) {
                System.out.println("\nApagado solicitado. Cerrando servidor...");
                server.stop();
            }
        }, "microserver-shutdown"));

        server.start();
    }

    /**
     * Escanea todas las entradas del classpath buscando archivos .class
     * y retorna las que tengan la anotacion @RestController.
     */
    private static List<Class<?>> scanClasspath() throws Exception {
        List<Class<?>> found = new ArrayList<>();
        String classpath = System.getProperty("java.class.path");
        String[] entries = classpath.split(File.pathSeparator);

        for (String entry : entries) {
            File file = new File(entry);
            if (file.isDirectory()) {
                scanDirectory(file, file, found);
            }
        }
        return found;
    }

    /**
     * Recorre recursivamente un directorio cargando clases .class
     * y filtrando las que tengan @RestController.
     */
    private static void scanDirectory(File root, File current, List<Class<?>> found) throws Exception {
        File[] files = current.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                scanDirectory(root, f, found);
            } else if (f.getName().endsWith(".class")) {
                String relativePath = root.toURI().relativize(f.toURI()).getPath();
                String className = relativePath
                        .replace("/", ".")
                        .replace("\\", ".")
                        .replaceAll("\\.class$", "");
                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(RestController.class)) {
                        found.add(clazz);
                    }
                } catch (Throwable ignored) {

                }
            }
        }
    }
}

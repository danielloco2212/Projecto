package com.example.pagebuilder; // Asegúrate de mover este archivo a src/main/java/com/example/pagebuilder/

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import java.io.PrintWriter;


@Suite
@SelectClasses({
    PruebaRepositorioClubInformacion.class,
    PruebaInterfazWeb.class,
    PruebaControladorApi.class,
    PruebaCodificacionContrasena.class
})
public class EjecutorPruebas {

    public static void main(String[] args) {
        // Desactivamos el modo headless para permitir que los componentes Swing se instancien en las pruebas
        System.setProperty("java.awt.headless", "false");
        System.out.println("=== Iniciando Suite de Pruebas ===\n");
        // 1. Creamos el lanzador de pruebas de JUnit Platform
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        // 2. Configuramos la solicitud para descubrir los tests definidos en esta Suite
        LauncherDiscoveryRequest solicitud = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(EjecutorPruebas.class))
                .build();

        // 3. Ejecutamos las pruebas
        launcher.execute(solicitud, listener);

        // 4. Imprimimos los resultados detallados en la consola
        PrintWriter escritor = new PrintWriter(System.out);
        listener.getSummary().printTo(escritor);
        if (listener.getSummary().getTestsFailedCount() > 0) {
            System.err.println("\n--- DETALLES DE FALLOS ---");
            listener.getSummary().printFailuresTo(escritor);
        }
        escritor.flush();
        
        // 5. Salimos con código 1 si hubo fallos, útil para automatización
        System.exit(listener.getSummary().getTestsFailedCount() == 0 ? 0 : 1);
    }
}
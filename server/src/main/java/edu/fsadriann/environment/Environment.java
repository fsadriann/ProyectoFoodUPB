package edu.fsadriann.environment;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Singleton que carga la configuración del servidor (IP, puerto y nombre de servicio)
 * desde el archivo {@code config.properties} o variables de entorno/sistema.
 */
public class Environment {

    private String ip;
    private int    port;
    private String serviceName;

    private static final String CONFIG_FILE_PATH =
            "C:/Users/adria/Desktop/Universidad/Estructuras 3er semestre/ProyectoFoodUPB/server/config.properties";

    private static Environment instance;

    private Environment() {
        System.setProperty("config.file.path", CONFIG_FILE_PATH);
        loadConfig();
        this.ip = System.getProperty("server.ip",
                System.getenv().getOrDefault("SERVER_IP", "10.153.96.175"));
        this.port = Integer.parseInt(System.getProperty("server.port",
                System.getenv().getOrDefault("SERVER_PORT", "1808")));
        this.serviceName = System.getProperty("server.name",
                System.getenv().getOrDefault("SERVER_NAME", "tickets"));
    }

    private void loadConfig() {
        Properties config = new Properties();
        try (FileInputStream fin = new FileInputStream(new File(
                System.getProperty("config.file.path",
                        System.getenv().getOrDefault("CONFIG_FILE_PATH", "config.properties"))))) {
            config.load(fin);
            System.setProperty("server.ip",   (String) config.get("SERVER_IP"));
            System.setProperty("server.port", (String) config.get("SERVER_PORT"));
            System.setProperty("server.name", (String) config.get("SERVER_NAME"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retorna la instancia única de la configuración del entorno.
     *
     * @return instancia de {@code Environment}
     */
    public static Environment getInstance() {
        if (instance == null) {
            instance = new Environment();
        }
        return instance;
    }

    /** @return dirección IP del servidor */
    public String getIp()          { return ip; }

    /** @return puerto en el que escucha el registro RMI */
    public int getPort()           { return port; }

    /** @return nombre del servicio RMI */
    public String getServiceName() { return serviceName; }
}

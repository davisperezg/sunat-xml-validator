package com.validator.xml.xsl.services;

import net.sf.saxon.s9api.*;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.xml.transform.stream.StreamSource;
import java.util.HashMap;
import java.util.Map;

@Service
public class XsltCache {
    private final Processor processor;
    private final Map<String, XsltExecutable> stylesheets;

	public XsltCache() {
        this.processor = new Processor(false);
        this.stylesheets = new HashMap<>();
    }

    @PostConstruct
    public void init() {
        // Precarga todos los XSLTs al iniciar el servicio
        loadStylesheet("ValidaExprRegFactura", "sunat_archivos/sfs/VALI/commons/xsl/validation/2.X/ValidaExprRegFactura-2.0.1.xsl");
        // Añade más validadores según necesites
    }

    private void loadStylesheet(String name, String classpathPath) {
    	try {
            // Carga el recurso desde el classpath
            var resource = getClass().getClassLoader().getResource(classpathPath);
            if (resource == null) {
                throw new RuntimeException("XSLT no encontrado en classpath: " + classpathPath);
            }

            // Crea el StreamSource con SystemId
            StreamSource source = new StreamSource(resource.openStream());
            source.setSystemId(resource.toURI().toString()); // Esto es lo más importante

            // Compila
            XsltCompiler compiler = processor.newXsltCompiler();
            XsltExecutable executable = compiler.compile(source);

            // Guarda el ejecutable en el mapa
            stylesheets.put(name, executable);
            System.out.println("✅ XSLT cargado y cacheado: " + name + ".xsl");
        } catch (Exception e) {
            throw new RuntimeException("❌ Error al cargar XSLT '" + name + "': " + e.getMessage(), e);
        }
    }

    public XsltExecutable getStylesheet(String name) {
        XsltExecutable stylesheet = stylesheets.get(name);
        if (stylesheet == null) {
            throw new IllegalArgumentException("Validador XSLT no encontrado: " + name);
        }
        return stylesheet;
    }

    public Processor getProcessor() {
        return processor;
    }
}
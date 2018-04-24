package com.haotian.remote;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

public abstract class AbstractProviderWriter implements ProviderWriter {
    private Configuration cfg;
    private List<RemoteProvider> providers;
    private byte[] data;
    @Override
    public void beforeWrite() {
        cfg = new Configuration(Configuration.VERSION_2_3_23);
        cfg.setClassForTemplateLoading(this.getClass(), "");
        cfg.setWhitespaceStripping(true);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        providers = new ArrayList<RemoteProvider>();
        data = new byte[0];
    }

    @Override
    public void write(RemoteProvider provider) {
        providers.add(provider);
    }

    @Override
    public void afterWrite() {
        try {
            Template template = cfg.getTemplate("remote-provider.ftl");
            ByteArrayOutputStream iobuffer = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(iobuffer);
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("providers", providers);
            Environment tplEnv = template.createProcessingEnvironment(model, writer);
            tplEnv.setLocale(Locale.SIMPLIFIED_CHINESE);
            tplEnv.setNumberFormat("0.##");
            tplEnv.process();
            writer.flush();
            writer.close();
            iobuffer.close();
            data = iobuffer.toByteArray();
        } catch (Throwable e) {
            throw new IllegalStateException("generate remote-provider error", e);
        }
    }

    @Override
    public byte[] toByteArray() {
        return data;
    }

}

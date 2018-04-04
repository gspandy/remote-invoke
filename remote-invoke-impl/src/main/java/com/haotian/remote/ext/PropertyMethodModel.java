package com.haotian.remote.ext;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

import java.util.List;

public class PropertyMethodModel implements TemplateMethodModelEx {

    private String getStringValue(Object value) {
        String stringValue = null;
        if (value instanceof SimpleScalar) {
            stringValue = ((SimpleScalar) value).getAsString();
        } else {
            stringValue = (String) value;
        }
        return stringValue;
    }

    @Override
    public Object exec(List args) throws TemplateModelException {
        if (args.size() != 2 && args.size() != 3) {
            throw new TemplateModelException("PropertyMethodModel requires 3 parameters");
        }
        String key = getStringValue(args.get(0));
        String value = getStringValue(args.get(1));
        boolean valueEmpty = value == null || "".equals(value);
        if (valueEmpty && args.size() == 2) {
            return null;
        }
        return key + "=" + (valueEmpty ? getStringValue(args.get(2)) : value);
    }
}

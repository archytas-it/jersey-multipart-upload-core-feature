package com.archytasit.jersey.multipart.internal;

import com.archytasit.jersey.multipart.FormDataBodyPart;
import com.archytasit.jersey.multipart.MultiPart;
import com.archytasit.jersey.multipart.annotations.FormDataParam;
import com.archytasit.jersey.multipart.internal.valueproviders.*;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.AbstractValueParamProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.model.Parameter;

import javax.inject.Provider;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * The type Form data param value param provider.
 */
public class FormDataParamValueParamProvider  extends AbstractValueParamProvider {

    /**
     * Instantiates a new Form data param value param provider.
     *
     * @param extractorProvider the extractor provider
     */
    public FormDataParamValueParamProvider(Provider<MultivaluedParameterExtractorProvider> extractorProvider) {
        super(extractorProvider, Parameter.Source.ENTITY, Parameter.Source.UNKNOWN);
    }


    @Override
    protected Function<ContainerRequest, ?> createValueProvider(Parameter parameter) {

        final Class<?> rawType = parameter.getRawType();
        // if the Parameter to resolve is an entity or unknown
        if (Parameter.Source.ENTITY == parameter.getSource() || Parameter.Source.UNKNOWN == parameter.getSource()) {
            // if it is a requested Multipart :
            if (MultiPart.class.isAssignableFrom(rawType)) {
                // return the raw Multipart provider
                return new MultiPartEntityValueProvider();
            } else if (parameter.getAnnotation(FormDataParam.class) != null) {
                // else we only deal with annotated @FormDataParam elements
                if (List.class.isAssignableFrom(rawType) || Set.class.isAssignableFrom(rawType)) {
                    // if it is a collection requested
                    final Class genericClazz = ReflectionHelper.getGenericTypeArgumentClasses(parameter.getType()).get(0);
                    if (FormDataBodyPart.class.isAssignableFrom(genericClazz)) {
                        // if it is a FormDataBodyPart or inherited object, for collection
                        return new PartListValueProvider(parameter, genericClazz);
                    } else {
                        // else we deal with MultivalueExtractor and/or ParamConverters and/or MessageBodyWorkers for collection
                        return new CollectionValueProvider(parameter, get(parameter));
                    }
                } else if (FormDataBodyPart.class.isAssignableFrom(rawType)) {
                    // if it is a FormDataBodyPart or inherited object
                    return new PartSingleValueProvider(parameter);
                } else {
                    // else we deal with MultivalueExtractor and/or ParamConverters and/or MessageBodyWorkers
                    return new SingleValueProvider(parameter, get(parameter));
                }
            }
        }
        return null;
    }

    @Override
    public PriorityType getPriority() {
        return Priority.HIGH;
    }
}
